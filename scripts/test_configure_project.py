#!/usr/bin/env python3
"""Tests for the project configurator."""

from __future__ import annotations

import json
import shutil
import tempfile
import unittest
from pathlib import Path

from scripts.configure_project import (
    Identity,
    Answers,
    apply_identity,
    apply_optional_removals,
    collect_answers,
    configure,
    detect_identity,
    load_template_config,
    package_from_group_and_artifact,
    replacement_pairs,
    resolve_listed_files,
    rewrite_pom,
    to_pascal_case,
    to_slug,
    validate_package,
    build_parser,
)


FIXTURE_POM = """<?xml version="1.0" encoding="UTF-8"?>
<project>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
	</parent>
	<groupId>io.unbyte</groupId>
	<artifactId>sandbox</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>sandbox</name>
	<description>Demo project for Spring Boot</description>
</project>
"""

APPLICATION_JAVA = """package io.unbyte.sandbox;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SandboxApplication {
}
"""

HEALTH_JAVA = """package io.unbyte.sandbox.application.usecase;

public class GetHealthStatusUseCase {
    private static final String SERVICE_NAME = "sandbox-service";
}
"""

SAMPLE_JAVA = """package io.unbyte.sandbox.infrastructure.web.controller;

public class SampleController {
    // Hello from Sandbox Service
}
"""

ARCH_TEST_JAVA = """package io.unbyte.sandbox.architecture;

@AnalyzeClasses(packages = "io.unbyte.sandbox")
public class HexagonalArchitectureTest {
}
"""


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


class HelperTests(unittest.TestCase):
    def test_slug_and_pascal(self) -> None:
        self.assertEqual(to_slug("Orders API"), "orders-api")
        self.assertEqual(to_pascal_case("orders-api"), "OrdersApi")
        self.assertEqual(package_from_group_and_artifact("com.acme", "orders-api"), "com.acme.ordersapi")

    def test_invalid_package_rejected(self) -> None:
        with self.assertRaises(ValueError):
            validate_package("Com.Acme")
        with self.assertRaises(ValueError):
            validate_package("com.acme_app")

    def test_replacement_pairs_are_specific(self) -> None:
        current = Identity(
            group_id="io.unbyte",
            artifact_id="sandbox",
            package="io.unbyte.sandbox",
            app_name="sandbox",
            service_name="sandbox-service",
            application_class="SandboxApplication",
            description="Demo",
            server_port=8080,
        )
        new = Identity(
            group_id="com.acme",
            artifact_id="orders",
            package="com.acme.orders",
            app_name="orders",
            service_name="orders-service",
            application_class="OrdersApplication",
            description="Orders API",
            server_port=9090,
        )
        pairs = dict(replacement_pairs(current, new))
        self.assertEqual(pairs["io.unbyte.sandbox"], "com.acme.orders")
        self.assertEqual(pairs["sandbox-service"], "orders-service")
        self.assertEqual(pairs["SandboxApplication"], "OrdersApplication")
        self.assertNotIn("sandbox", pairs)

    def test_rewrite_pom_keeps_parent_coordinates(self) -> None:
        current = Identity(
            "io.unbyte",
            "sandbox",
            "io.unbyte.sandbox",
            "sandbox",
            "sandbox-service",
            "SandboxApplication",
            "Demo project for Spring Boot",
            8080,
        )
        new = Identity(
            "com.acme",
            "orders",
            "com.acme.orders",
            "orders",
            "orders-service",
            "OrdersApplication",
            "Orders API",
            9090,
        )
        rewritten = rewrite_pom(FIXTURE_POM, current, new)
        self.assertIn("<artifactId>spring-boot-starter-parent</artifactId>", rewritten)
        self.assertIn("<groupId>com.acme</groupId>", rewritten)
        self.assertIn("<artifactId>orders</artifactId>", rewritten)
        self.assertIn("<description>Orders API</description>", rewritten)


class FixtureConfiguratorTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = Path(tempfile.mkdtemp(prefix="starter-config-"))
        write(self.temp / "pom.xml", FIXTURE_POM)
        write(
            self.temp / "src/main/java/io/unbyte/sandbox/SandboxApplication.java",
            APPLICATION_JAVA,
        )
        write(
            self.temp / "src/main/java/io/unbyte/sandbox/application/usecase/GetHealthStatusUseCase.java",
            HEALTH_JAVA,
        )
        write(
            self.temp / "src/main/java/io/unbyte/sandbox/infrastructure/web/controller/SampleController.java",
            SAMPLE_JAVA,
        )
        write(
            self.temp / "src/test/java/io/unbyte/sandbox/architecture/HexagonalArchitectureTest.java",
            ARCH_TEST_JAVA,
        )
        write(
            self.temp / "src/main/resources/application.properties",
            "spring.application.name=sandbox\n",
        )
        write(
            self.temp / "src/main/resources/application.yml",
            "spring:\n  application:\n    name: sandbox-service\nserver:\n  port: 8080\n",
        )
        write(self.temp / "HEXAGONAL_ARCHITECTURE.md", "# Hexagonal\n")
        write(self.temp / "API_ENDPOINTS.md", "# API\n")
        config = {
            "sampleFiles": [
                "src/main/java/{package_path}/infrastructure/web/controller/SampleController.java",
                "API_ENDPOINTS.md",
            ],
            "architectureDocs": ["HEXAGONAL_ARCHITECTURE.md"],
        }
        write(self.temp / "template/config.json", json.dumps(config))

    def tearDown(self) -> None:
        shutil.rmtree(self.temp)

    def test_detect_identity(self) -> None:
        identity = detect_identity(self.temp)
        self.assertEqual(identity.package, "io.unbyte.sandbox")
        self.assertEqual(identity.application_class, "SandboxApplication")
        self.assertEqual(identity.service_name, "sandbox-service")
        self.assertEqual(identity.server_port, 8080)

    def test_rename_and_repackage(self) -> None:
        current = detect_identity(self.temp)
        new = Identity(
            group_id="com.acme",
            artifact_id="orders",
            package="com.acme.orders",
            app_name="orders",
            service_name="orders-service",
            application_class="OrdersApplication",
            description="Orders API",
            server_port=9090,
        )
        apply_identity(self.temp, current, new)

        self.assertFalse(
            (self.temp / "src/main/java/io/unbyte/sandbox/SandboxApplication.java").exists()
        )
        application = self.temp / "src/main/java/com/acme/orders/OrdersApplication.java"
        self.assertTrue(application.exists())
        text = application.read_text(encoding="utf-8")
        self.assertIn("package com.acme.orders;", text)
        self.assertIn("class OrdersApplication", text)

        arch = (
            self.temp
            / "src/test/java/com/acme/orders/architecture/HexagonalArchitectureTest.java"
        ).read_text(encoding="utf-8")
        self.assertIn('packages = "com.acme.orders"', arch)
        self.assertNotIn("io.unbyte.sandbox", arch)

        pom = (self.temp / "pom.xml").read_text(encoding="utf-8")
        self.assertIn("<groupId>com.acme</groupId>", pom)
        self.assertIn("<artifactId>orders</artifactId>", pom)
        self.assertIn("<artifactId>spring-boot-starter-parent</artifactId>", pom)

        props = (self.temp / "src/main/resources/application.properties").read_text(
            encoding="utf-8"
        )
        self.assertIn("spring.application.name=orders", props)
        yml = (self.temp / "src/main/resources/application.yml").read_text(encoding="utf-8")
        self.assertIn("name: orders-service", yml)
        self.assertIn("port: 9090", yml)

    def test_sample_and_docs_can_be_removed(self) -> None:
        current = detect_identity(self.temp)
        apply_identity(self.temp, current, current)
        config = load_template_config(self.temp)
        removed = apply_optional_removals(
            self.temp,
            config,
            current,
            include_sample_code=False,
            include_architecture_docs=False,
        )
        removed_names = {path.name for path in removed}
        self.assertIn("SampleController.java", removed_names)
        self.assertIn("API_ENDPOINTS.md", removed_names)
        self.assertIn("HEXAGONAL_ARCHITECTURE.md", removed_names)
        self.assertFalse(
            (
                self.temp
                / "src/main/java/io/unbyte/sandbox/infrastructure/web/controller/SampleController.java"
            ).exists()
        )
        self.assertTrue(
            (
                self.temp
                / "src/main/java/io/unbyte/sandbox/application/usecase/GetHealthStatusUseCase.java"
            ).exists()
        )

    def test_non_interactive_cli_defaults(self) -> None:
        parser = build_parser()
        args = parser.parse_args(
            [
                "--non-interactive",
                "--yes",
                "--include-sample-code",
                "false",
            ]
        )
        current = detect_identity(self.temp)
        answers = collect_answers(current, args)
        self.assertEqual(answers.identity.package, current.package)
        self.assertFalse(answers.include_sample_code)
        self.assertTrue(answers.include_architecture_docs)

    def test_configure_end_to_end(self) -> None:
        parser = build_parser()
        args = parser.parse_args(
            [
                "--app-name",
                "orders",
                "--group-id",
                "com.acme",
                "--description",
                "Orders API",
                "--server-port",
                "9090",
                "--include-sample-code",
                "false",
                "--include-architecture-docs",
                "true",
                "--non-interactive",
                "--yes",
            ]
        )
        current = detect_identity(self.temp)
        answers = collect_answers(current, args)
        removed = configure(self.temp, answers)
        self.assertTrue((self.temp / "src/main/java/com/acme/orders/OrdersApplication.java").exists())
        self.assertFalse(
            list(self.temp.rglob("SampleController.java")),
        )
        self.assertTrue((self.temp / "HEXAGONAL_ARCHITECTURE.md").exists())
        self.assertTrue(any(path.name == "SampleController.java" for path in removed))


class TemplateManifestTests(unittest.TestCase):
    def test_real_template_paths_exist(self) -> None:
        root = Path(__file__).resolve().parents[1]
        config = load_template_config(root)
        identity = detect_identity(root)
        sample = resolve_listed_files(root, config["sampleFiles"], identity)
        docs = resolve_listed_files(root, config["architectureDocs"], identity)
        missing_sample = [
            pattern.format(package_path=identity.package_path)
            for pattern in config["sampleFiles"]
            if not (root / pattern.format(package_path=identity.package_path)).exists()
        ]
        missing_docs = [
            pattern
            for pattern in config["architectureDocs"]
            if not (root / pattern).exists()
        ]
        self.assertEqual(missing_sample, [])
        self.assertEqual(missing_docs, [])
        self.assertEqual(len(sample), len(config["sampleFiles"]))
        self.assertEqual(len(docs), len(config["architectureDocs"]))


if __name__ == "__main__":
    unittest.main()
