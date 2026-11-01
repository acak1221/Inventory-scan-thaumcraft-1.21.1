"""Validate and assemble a release from actual build outputs; standard library only."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def properties(path):
    return dict(line.split('=', 1) for raw in path.read_text(encoding='utf-8-sig').splitlines()
                if (line := raw.strip()) and not line.startswith('#') and '=' in line)


def metadata(props, tag=''):
    version = props['mod_version']
    if not re.fullmatch(r'[0-9]+\.[0-9]+\.[0-9]+(?:-[0-9A-Za-z.-]+)?(?:\+[0-9A-Za-z.-]+)?', version):
        raise ValueError('mod_version must be a semantic version')
    if tag and tag != 'v' + version:
        raise ValueError(f'Tag {tag!r} does not match mod_version={version}')
    ref = props['thaumcraft_ref']
    if not re.fullmatch(r'[0-9a-f]{40}', ref):
        raise ValueError('thaumcraft_ref must pin a full commit SHA')
    return {'version': version, 'thaumcraft_ref': ref,
            'prerelease': str('-' in version.split('+', 1)[0]).lower()}


def git_commit(directory):
    return subprocess.check_output(['git', '-C', str(directory), 'rev-parse', 'HEAD'], text=True).strip()


def sha256(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def inputs(props):
    home = (ROOT / props['thaumcraft_home']).resolve()
    if git_commit(home) != props['thaumcraft_ref']:
        raise ValueError('Thaumcraft checkout does not match thaumcraft_ref')
    paths = {loader: home / props[f'thaumcraft_{loader}_jar'] for loader in ('fabric', 'neoforge')}
    if not (home / props['thaumcraft_common_jar']).is_file():
        raise ValueError('Mapped compile-only Thaumcraft API is missing')
    return home, paths, {loader + '_sha256': sha256(path) for loader, path in paths.items()}


def test_summary(paths):
    if not paths:
        raise ValueError('No test reports were produced')
    result = dict(passed=0, failed=0, errors=0, skipped=0)
    for path in paths:
        root = ET.parse(path).getroot()
        cases = list(root.iter('testcase'))
        if not cases:
            raise ValueError(f'Empty test report: {path}')
        for case in cases:
            if case.find('failure') is not None:
                result['failed'] += 1
            elif case.find('error') is not None:
                result['errors'] += 1
            elif case.find('skipped') is not None:
                result['skipped'] += 1
            else:
                result['passed'] += 1
    if result['failed'] or result['errors'] or result['skipped']:
        raise ValueError(f'Tests did not all pass: {result}')
    return result


def check_jar(path, loader, version):
    with zipfile.ZipFile(path) as jar:
        names = set(jar.namelist())
        bad = [name for name in names if name.startswith(('thaumcraft/', 'net/minecraft/', 'assets/thaumcraft/', 'data/thaumcraft/'))
               or '/testing/' in name or 'tcinventoryscan_gametest' in name
               or 'tcinventoryscan-gametest' in name or name.endswith('.jar')]
        if bad or not any(name.endswith('.class') for name in names):
            raise ValueError(f'Invalid addon contents: {path.name}; forbidden={bad}')
        if not {'LICENSE', 'THIRD_PARTY_NOTICES.md', 'tcinventoryscan.mixins.json'} <= names:
            raise ValueError(f'Missing metadata, mixins or attribution in {path.name}')
        if jar.testzip() is not None:
            raise ValueError(f'Corrupt jar: {path.name}')
        if loader == 'fabric':
            data = json.loads(jar.read('fabric.mod.json'))
            config = json.loads(jar.read('tcinventoryscan.mixins.json'))
            if data['version'] != version or data['depends']['thaumcraft'] != '*':
                raise ValueError('Incorrect Fabric version or Thaumcraft constraint')
            if not config.get('refmap') or config['refmap'] not in names:
                raise ValueError('Missing Fabric production refmap')
        else:
            text = jar.read('META-INF/neoforge.mods.toml').decode()
            if f'version="{version}"' not in text or not re.search(
                    r'modId="thaumcraft"\s+type="required"\s+versionRange="\*"', text):
                raise ValueError('Incorrect NeoForge version or Thaumcraft constraint')


def prepare(props):
    meta = metadata(props)
    home, _, hashes = inputs(props)
    results = {
        'junit': test_summary(sorted((ROOT / 'common/build/test-results/test').glob('TEST-*.xml'))),
        **{loader + '_native': test_summary([ROOT / loader / 'build/gametest-results.xml'])
           for loader in ('fabric', 'neoforge')},
    }
    output = ROOT / 'build/release'
    if output.exists() and any(output.iterdir()):
        raise ValueError('Release output directory is not empty; remove stale build/release outputs first')
    output.mkdir(parents=True, exist_ok=True)
    artifacts = []
    for loader in ('fabric', 'neoforge'):
        name = f"{props['archives_name']}-{loader}-{meta['version']}.jar"
        path = ROOT / loader / 'build/libs' / name
        check_jar(path, loader, meta['version'])
        shutil.copyfile(path, output / name)
        artifacts.append(dict(loader=loader, file=name, bytes=path.stat().st_size, sha256=sha256(path)))
    info = dict(version=meta['version'], minecraft=props['minecraft_version'], java=21,
                source_commit=git_commit(ROOT), thaumcraft_version_constraint='*',
                tested_thaumcraft=dict(source_commit=git_commit(home),
                                      version=properties(home / 'gradle.properties')['mod_version'], **hashes),
                artifacts=artifacts, tests=results,
                workflow_run=os.environ.get('GITHUB_SERVER_URL', 'https://github.com') + '/' +
                os.environ.get('GITHUB_REPOSITORY', 'acak1221/Inventory-scan-thaumcraft-1.21.1') +
                '/actions/runs/' + os.environ.get('GITHUB_RUN_ID', 'local'),
                limitations='Client visuals, remote multiplayer and other Thaumcraft versions are not checked by this workflow.',
                scope='Standalone addon; external Thaumcraft and dependencies are not bundled.')
    (output / 'BUILD_INFO.json').write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (output / 'SHA256SUMS.txt').write_text(''.join(f"{a['sha256']}  {a['file']}\n" for a in artifacts), encoding='ascii')
    changelog = (ROOT / 'CHANGELOG.md').read_text(encoding='utf-8')
    match = re.search(r'^## ' + re.escape(meta['version']) + r'[^\n]*\n(.*?)(?=^## |\Z)', changelog, re.M | re.S)
    notes = (match.group(1).strip() + '\n\n') if match else ''
    notes += (f"Minecraft {props['minecraft_version']}, Java 21. Thaumcraft 4 Reload with a compatible API is required; "
              "its version number is not restricted. Install one jar for your loader. Dependencies are not bundled.\n\n"
              f"Checks: {results['junit']['passed']} JUnit tests, {results['fabric_native']['passed']} Fabric "
              f"and {results['neoforge_native']['passed']} NeoForge server GameTests passed. "
              f"Thaumcraft source: `{git_commit(home)}`.\n\n"
              "Client visuals, remote multiplayer and other Thaumcraft versions were not checked by this workflow. "
              "Checksums and full build provenance are attached.\n\n"
              "Original Inventory Scanning: Christopher 'BlayTheNinth' Baker; Thaumcraft: Azanor. "
              "See THIRD_PARTY_NOTICES.md for attribution. Release automation configured with OpenAI Codex (GPT-6).\n")
    (output / 'RELEASE_NOTES.md').write_text(notes, encoding='utf-8')
    print(json.dumps(info, indent=2))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=['metadata', 'inputs', 'prepare'])
    parser.add_argument('--tag', default='')
    parser.add_argument('--github-output', type=Path)
    args = parser.parse_args()
    props = properties(ROOT / 'gradle.properties')
    if args.command == 'prepare':
        prepare(props)
        return
    values = metadata(props, args.tag) if args.command == 'metadata' else inputs(props)[2]
    print(json.dumps(values, indent=2))
    if args.github_output:
        with args.github_output.open('a', encoding='utf-8') as stream:
            for key, value in values.items():
                stream.write(f'{key}={value}\n')


if __name__ == '__main__':
    main()
