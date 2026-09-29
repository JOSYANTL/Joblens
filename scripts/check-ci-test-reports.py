#!/usr/bin/env python3
"""Fail CI when tests are missing, failed, or silently skipped (including Docker tests)."""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def check_reports(directory: Path) -> tuple[int, int]:
    reports = sorted(directory.glob("TEST-*.xml"))
    if not reports:
        raise ValueError(f"No Surefire test reports found in {directory}")

    required_suite = (
        "com.josyantl.joblens.job.infrastructure.persistence."
        "PostgreSqlJobApplicationIntegrationTest"
    )
    total = 0
    postgres_tests = 0
    for report in reports:
        suite = ET.parse(report).getroot()
        cases = suite.findall("testcase")
        if suite.tag != "testsuite" or not cases:
            raise ValueError(f"Missing test cases in {report.name}")
        for attribute in ("failures", "errors", "skipped"):
            if int(suite.get(attribute, "0")) != 0:
                raise ValueError(f"{report.name} reports {attribute}={suite.get(attribute)}")
        for case in cases:
            if any(case.find(tag) is not None for tag in ("failure", "error", "skipped")):
                raise ValueError(f"Test did not pass: {suite.get('name')}.{case.get('name')}")
        total += len(cases)
        if suite.get("name") == required_suite:
            postgres_tests += len(cases)

    if postgres_tests == 0:
        raise ValueError("Required PostgreSQL integration suite did not execute")
    return total, postgres_tests


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: check-ci-test-reports.py <surefire-reports-directory>", file=sys.stderr)
        return 2
    try:
        total, postgres_tests = check_reports(Path(sys.argv[1]))
    except (ValueError, OSError, ET.ParseError) as error:
        print(f"CI test verification failed: {error}", file=sys.stderr)
        return 1
    print(f"Verified {total} passing tests, including {postgres_tests} PostgreSQL integration tests; no skips.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
