"""Failure cases that must prevent publishing unchecked or mislabeled binaries."""
from pathlib import Path
import tempfile
import unittest

import release


class ReleaseValidationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.props = dict(mod_version='1.0.3+1.21.1', thaumcraft_ref='a' * 40)

    def test_tag_must_match_the_built_version(self):
        with self.assertRaises(ValueError):
            release.metadata(self.props, 'v1.0.2+1.21.1')
        self.assertEqual(release.metadata(self.props, 'v1.0.3+1.21.1')['version'], '1.0.3+1.21.1')

    def test_dependency_cannot_silently_follow_a_moving_branch(self):
        self.props['thaumcraft_ref'] = 'main'
        with self.assertRaises(ValueError):
            release.metadata(self.props)

    def test_prerelease_does_not_depend_on_the_minecraft_build_suffix(self):
        self.assertEqual(release.metadata(self.props)['prerelease'], 'false')
        self.props['mod_version'] = '1.1.0-rc.1+1.21.1'
        self.assertEqual(release.metadata(self.props)['prerelease'], 'true')

    def test_missing_or_empty_reports_are_not_a_passing_run(self):
        with self.assertRaises(ValueError):
            release.test_summary([])
        report = self.root / 'empty.xml'
        report.write_text('<testsuite tests="13"/>')
        with self.assertRaises(ValueError):
            release.test_summary([report])

    def test_failure_error_and_skipped_tests_block_publication(self):
        report = self.root / 'results.xml'
        for problem in ('failure', 'error', 'skipped'):
            with self.subTest(problem=problem):
                report.write_text(f'<testsuite><testcase><{problem}/></testcase></testsuite>')
                with self.assertRaises(ValueError):
                    release.test_summary([report])

    def test_counts_come_from_actual_cases_not_a_summary_claim(self):
        report = self.root / 'results.xml'
        report.write_text('<testsuites><testsuite tests="999"><testcase/><testcase/></testsuite></testsuites>')
        self.assertEqual(release.test_summary([report]), dict(passed=2, failed=0, errors=0, skipped=0))


if __name__ == '__main__':
    unittest.main()
