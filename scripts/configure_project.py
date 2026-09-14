#!/usr/bin/env python3
"""Configure a new project created from this Spring Boot starter.

Run from the repository root:

    ./configure-project
    ./configure-project --app-name orders --group-id com.acme --include-sample-code false
"""

from __future__ import annotations

import argparse
import json
import re
import shutil
import sys
from dataclasses import dataclass
from pathlib import Path

SKIP_DIR_NAMES = {
    ".git",
    ".idea",
    ".mvn",
    ".venv",
    "__pycache__",
    "node_modules",
    "scripts",
    "target",
    "template",
}

SKIP_FILE_NAMES = {
    "configure-project",
    "configure_project.py",
}

TEXT_SUFFIXES = {
    "",
    ".java",
    ".json",
    ".md",
    ".properties",
    ".xml",
    ".yml",
    ".yaml",
    ".txt",
    ".gitignore",
    ".gitattributes",
    ".editorconfig",
}

PACKAGE_RE = re.compile(r"^[a-z]+(\.[a-z][a-z0-9]*)*$")
GROUP_RE = PACKAGE_RE
ARTIFACT_RE = re.compile(r"^[a-z][a-z0-9-]*$")
POM_FIELD_RE = {
    "groupId": re.compile(r"(<groupId>)([^<]+)(</groupId>)"),
    "artifactId": re.compile(r"(<artifactId>)([^<]+)(</artifactId>)"),
    "name": re.compile(r"(<name>)([^<]+)(</name>)"),
    "description": re.compile(r"(<description>)([^<]+)(</description>)"),
}


@dataclass(frozen=True)
class Identity:
    group_id: str
    artifact_id: str
    package: str
    app_name: str
    service_name: str
    application_class: str
    description: str
    server_port: int

    @property
    def package_path(self) -> str:
        return self.package.replace(".", "/")

    @property
    def display_title(self) -> str:
        name = self.application_class.removesuffix("Application")
        return name or to_pascal_case(self.artifact_id)


@dataclass(frozen=True)
class Answers:
    identity: Identity
    include_sample_code: bool
    include_architecture_docs: bool


def to_slug(value: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", value.strip().lower()).strip("-")
    if not slug:
        raise ValueError("Value cannot be empty")
    if not ARTIFACT_RE.match(slug):
        raise ValueError(f"Invalid artifact/slug {slug!r}")
    return slug


def to_pascal_case(value: str) -> str:
    parts = re.split(r"[-_\s.]+", value.strip())
    pascal = "".join(part[:1].upper() + part[1:] for part in parts if part)
    if not pascal:
        raise ValueError("Value cannot be empty")
    return pascal


def package_from_group_and_artifact(group_id: str, artifact_id: str) -> str:
    artifact_package = artifact_id.replace("-", "")
    return f"{group_id}.{artifact_package}"


def validate_package(package: str) -> str:
    if not PACKAGE_RE.match(package):
        raise ValueError(
            f"Invalid Java package {package!r}. Use lowercase dot-separated segments "
            "(Checkstyle: first character of each segment must be a letter)."
        )
    return package


def validate_group_id(group_id: str) -> str:
    if not GROUP_RE.match(group_id):
        raise ValueError(f"Invalid Maven groupId {group_id!r}")
    return group_id


def validate_artifact_id(artifact_id: str) -> str:
    return to_slug(artifact_id)


def repo_root_from(start: Path | None = None) -> Path:
    here = (start or Path(__file__)).resolve()
    if here.is_file():
        here = here.parent
    for candidate in [here, *here.parents]:
        if (candidate / "pom.xml").exists() and (candidate / "template" / "config.json").exists():
            return candidate
    raise FileNotFoundError("Could not find repository root (pom.xml + template/config.json)")


def load_template_config(root: Path) -> dict:
    return json.loads((root / "template" / "config.json").read_text(encoding="utf-8"))


def _first_pom_match(text: str, field: str) -> str | None:
    match = POM_FIELD_RE[field].search(text)
    return match.group(2) if match else None


def _project_pom_section(pom: str) -> str:
    """Return the project coordinate block, skipping the parent POM."""
    parts = re.split(r"</parent>", pom, maxsplit=1)
    return parts[1] if len(parts) == 2 else pom


def detect_identity(root: Path) -> Identity:
    pom = (root / "pom.xml").read_text(encoding="utf-8")
    project = _project_pom_section(pom)
    group_id = _first_pom_match(project, "groupId")
    artifact_id = _first_pom_match(project, "artifactId")
    name = _first_pom_match(project, "name") or artifact_id
    description = _first_pom_match(project, "description") or ""
    if not group_id or not artifact_id:
        raise ValueError("pom.xml is missing groupId or artifactId")

    application_path = find_application_class(root)
    package = read_package(application_path)
    application_class = application_path.stem

    app_name = name or artifact_id
    properties = root / "src/main/resources/application.properties"
    if properties.exists():
        props_match = re.search(
            r"^spring\.application\.name=(.+)$",
            properties.read_text(encoding="utf-8"),
            re.MULTILINE,
        )
        if props_match:
            app_name = props_match.group(1).strip()

    service_name = f"{artifact_id}-service"
    yml = root / "src/main/resources/application.yml"
    server_port = 8080
    if yml.exists():
        yml_text = yml.read_text(encoding="utf-8")
        yml_name = re.search(r"^\s*name:\s*(.+)$", yml_text, re.MULTILINE)
        if yml_name:
            service_name = yml_name.group(1).strip()
        port_match = re.search(r"^\s*port:\s*(\d+)\s*$", yml_text, re.MULTILINE)
        if port_match:
            server_port = int(port_match.group(1))

    return Identity(
        group_id=group_id,
        artifact_id=artifact_id,
        package=package,
        app_name=app_name,
        service_name=service_name,
        application_class=application_class,
        description=description,
        server_port=server_port,
    )


def find_application_class(root: Path) -> Path:
    matches = [
        path
        for path in root.joinpath("src").rglob("*Application.java")
        if "@SpringBootApplication" in path.read_text(encoding="utf-8")
    ]
    if len(matches) != 1:
        raise FileNotFoundError(
            f"Expected exactly one @SpringBootApplication class, found {len(matches)}"
        )
    return matches[0]


def read_package(java_file: Path) -> str:
    match = re.search(
        r"^package\s+([a-z0-9.]+)\s*;",
        java_file.read_text(encoding="utf-8"),
        re.MULTILINE,
    )
    if not match:
        raise ValueError(f"Could not read package from {java_file}")
    return match.group(1)


def prompt_value(label: str, default: str) -> str:
    raw = input(f"{label} [{default}]: ").strip()
    return raw or default


def prompt_bool(label: str, default: bool) -> bool:
    suffix = "Y/n" if default else "y/N"
    raw = input(f"{label} [{suffix}]: ").strip().lower()
    if not raw:
        return default
    if raw in {"y", "yes", "true", "1"}:
        return True
    if raw in {"n", "no", "false", "0"}:
        return False
    raise ValueError(f"Please answer yes or no, got {raw!r}")


def parse_bool(value: str | bool | None, default: bool) -> bool:
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    normalized = value.strip().lower()
    if normalized in {"y", "yes", "true", "1"}:
        return True
    if normalized in {"n", "no", "false", "0"}:
        return False
    raise ValueError(f"Invalid boolean {value!r}")


def collect_answers(current: Identity, args: argparse.Namespace) -> Answers:
    interactive = not args.non_interactive

    app_name = args.app_name or (
        prompt_value("Application name", current.app_name) if interactive else current.app_name
    )
    artifact_default = (
        to_slug(args.artifact_id)
        if args.artifact_id
        else (to_slug(app_name) if args.app_name else current.artifact_id)
    )
    artifact_id = validate_artifact_id(
        args.artifact_id
        or (prompt_value("Maven artifactId", artifact_default) if interactive else artifact_default)
    )
    group_id = validate_group_id(
        args.group_id
        or (prompt_value("Maven groupId", current.group_id) if interactive else current.group_id)
    )
    derived_package = package_from_group_and_artifact(group_id, artifact_id)
    if args.package_name:
        package_default = args.package_name
    elif group_id != current.group_id or artifact_id != current.artifact_id:
        package_default = derived_package
    else:
        package_default = current.package
    package = validate_package(
        args.package_name
        or (prompt_value("Java base package", package_default) if interactive else package_default)
    )
    description = args.description or (
        prompt_value("Project description", current.description)
        if interactive
        else current.description
    )
    port_default = str(args.server_port or current.server_port)
    server_port = int(
        args.server_port
        or (prompt_value("Server port", port_default) if interactive else port_default)
    )
    include_sample_code = (
        parse_bool(args.include_sample_code, True)
        if args.include_sample_code is not None or not interactive
        else prompt_bool("Include sample hello/posts/error endpoints?", True)
    )
    include_architecture_docs = (
        parse_bool(args.include_architecture_docs, True)
        if args.include_architecture_docs is not None or not interactive
        else prompt_bool("Keep architecture write-ups?", True)
    )

    application_class = f"{to_pascal_case(artifact_id)}Application"
    service_name = f"{artifact_id}-service"
    identity = Identity(
        group_id=group_id,
        artifact_id=artifact_id,
        package=package,
        app_name=to_slug(app_name),
        service_name=service_name,
        application_class=application_class,
        description=description,
        server_port=server_port,
    )
    return Answers(
        identity=identity,
        include_sample_code=include_sample_code,
        include_architecture_docs=include_architecture_docs,
    )


def replacement_pairs(current: Identity, new: Identity) -> list[tuple[str, str]]:
    pairs = [
        (current.package, new.package),
        (current.service_name, new.service_name),
        (f"{current.application_class}Tests", f"{new.application_class}Tests"),
        (current.application_class, new.application_class),
        (f"Hello from {current.display_title} Service", f"Hello from {new.display_title} Service"),
        (f"{current.artifact_id}.requests", f"{new.artifact_id}.requests"),
        (f"{current.artifact_id}.operations", f"{new.artifact_id}.operations"),
        (f"{current.artifact_id}.errors", f"{new.artifact_id}.errors"),
        (f"{current.artifact_id}.endpoints", f"{new.artifact_id}.endpoints"),
        (f'tag("service", "{current.artifact_id}")', f'tag("service", "{new.artifact_id}")'),
        (f'put("service", "{current.artifact_id}")', f'put("service", "{new.app_name}")'),
        (f"for service: {current.artifact_id}", f"for service: {new.artifact_id}"),
        (f'timestamp, "{current.artifact_id}", version', f'timestamp, "{new.app_name}", version'),
        (f'timestamp, "{current.artifact_id}")', f'timestamp, "{new.app_name}")'),
        (f"{current.display_title} application", f"{new.display_title} application"),
    ]
    return [(old, new_value) for old, new_value in pairs if old != new_value]


def iter_text_files(root: Path) -> list[Path]:
    files: list[Path] = []
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if any(part in SKIP_DIR_NAMES for part in path.relative_to(root).parts):
            continue
        if path.name in SKIP_FILE_NAMES:
            continue
        if path.suffix not in TEXT_SUFFIXES and path.name not in {
            "mvnw",
            "Dockerfile",
        }:
            continue
        files.append(path)
    return files


def replace_in_text(text: str, pairs: list[tuple[str, str]]) -> str:
    for old, new in sorted(pairs, key=lambda item: len(item[0]), reverse=True):
        text = text.replace(old, new)
    return text


def rewrite_pom(text: str, current: Identity, new: Identity) -> str:
    def replace_first(field: str, value: str, content: str) -> str:
        return POM_FIELD_RE[field].sub(lambda match: f"{match.group(1)}{value}{match.group(3)}", content, count=1)

    # Project coordinates are the first groupId/artifactId after the parent block.
    project_section = re.split(r"</parent>", text, maxsplit=1)
    if len(project_section) != 2:
        raise ValueError("pom.xml is missing a parent block")
    head, tail = project_section
    tail = replace_first("groupId", new.group_id, tail)
    tail = replace_first("artifactId", new.artifact_id, tail)
    tail = replace_first("name", new.artifact_id, tail)
    tail = replace_first("description", new.description, tail)
    return head + "</parent>" + tail


def rewrite_application_files(root: Path, current: Identity, new: Identity) -> None:
    properties = root / "src/main/resources/application.properties"
    if properties.exists():
        text = properties.read_text(encoding="utf-8")
        text = re.sub(
            r"^spring\.application\.name=.*$",
            f"spring.application.name={new.app_name}",
            text,
            flags=re.MULTILINE,
        )
        properties.write_text(text, encoding="utf-8")

    yml = root / "src/main/resources/application.yml"
    if yml.exists():
        text = yml.read_text(encoding="utf-8")
        text = re.sub(r"^(\s*port:\s*)\d+\s*$", rf"\g<1>{new.server_port}", text, flags=re.MULTILINE)
        yml.write_text(text, encoding="utf-8")

    env_file = root / ".cursor/environment.json"
    if env_file.exists():
        data = json.loads(env_file.read_text(encoding="utf-8"))
        data["name"] = new.service_name
        if new.server_port:
            data["ports"] = [new.server_port]
        env_file.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def apply_identity(root: Path, current: Identity, new: Identity) -> None:
    pairs = replacement_pairs(current, new)
    for path in iter_text_files(root):
        original = path.read_text(encoding="utf-8")
        updated = replace_in_text(original, pairs)
        if path.name == "pom.xml":
            updated = rewrite_pom(updated, current, new)
        if updated != original:
            path.write_text(updated, encoding="utf-8")

    rewrite_application_files(root, current, new)
    move_package_tree(root, current, new)
    rename_application_files(root, current, new)


def move_package_tree(root: Path, current: Identity, new: Identity) -> None:
    if current.package == new.package:
        return
    for source_root in (root / "src/main/java", root / "src/test/java"):
        source = source_root / current.package_path
        if not source.exists():
            continue
        destination = source_root / new.package_path
        destination.parent.mkdir(parents=True, exist_ok=True)
        if destination.exists():
            for item in source.iterdir():
                shutil.move(str(item), destination / item.name)
            shutil.rmtree(source)
        else:
            shutil.move(str(source), destination)
        remove_empty_parents(source.parent, stop_at=source_root)


def remove_empty_parents(path: Path, stop_at: Path) -> None:
    current = path
    while current != stop_at and current.is_dir():
        try:
            next(current.iterdir())
            break
        except StopIteration:
            current.rmdir()
            current = current.parent


def rename_application_files(root: Path, current: Identity, new: Identity) -> None:
    if current.application_class == new.application_class:
        return
    for folder in (root / "src/main/java", root / "src/test/java"):
        for old_name, new_name in (
            (f"{current.application_class}.java", f"{new.application_class}.java"),
            (f"{current.application_class}Tests.java", f"{new.application_class}Tests.java"),
        ):
            for path in folder.rglob(old_name):
                path.rename(path.with_name(new_name))


def resolve_listed_files(root: Path, patterns: list[str], identity: Identity) -> list[Path]:
    resolved = []
    for pattern in patterns:
        path = root / pattern.format(package_path=identity.package_path)
        if path.exists():
            resolved.append(path)
    return resolved


def remove_files(paths: list[Path]) -> list[Path]:
    removed = []
    for path in paths:
        if path.exists():
            path.unlink()
            removed.append(path)
    for path in removed:
        parent = path.parent
        while parent.name not in {"java", "resources", "src"} and parent.exists():
            try:
                next(parent.iterdir())
                break
            except StopIteration:
                parent.rmdir()
                parent = parent.parent
    return removed


def apply_optional_removals(
    root: Path,
    config: dict,
    identity: Identity,
    include_sample_code: bool,
    include_architecture_docs: bool,
) -> list[Path]:
    removed: list[Path] = []
    if not include_sample_code:
        removed.extend(remove_files(resolve_listed_files(root, config["sampleFiles"], identity)))
    if not include_architecture_docs:
        removed.extend(remove_files(resolve_listed_files(root, config["architectureDocs"], identity)))
    return removed


def print_plan(current: Identity, answers: Answers) -> None:
    new = answers.identity
    print("Project configuration")
    print(f"  groupId:          {current.group_id} -> {new.group_id}")
    print(f"  artifactId:       {current.artifact_id} -> {new.artifact_id}")
    print(f"  package:          {current.package} -> {new.package}")
    print(f"  application:      {current.application_class} -> {new.application_class}")
    print(f"  app name:         {current.app_name} -> {new.app_name}")
    print(f"  service name:     {current.service_name} -> {new.service_name}")
    print(f"  description:      {current.description} -> {new.description}")
    print(f"  server port:      {current.server_port} -> {new.server_port}")
    print(f"  sample code:      {'keep' if answers.include_sample_code else 'remove'}")
    print(
        f"  architecture docs: {'keep' if answers.include_architecture_docs else 'remove'}"
    )


def configure(root: Path, answers: Answers, dry_run: bool = False) -> list[Path]:
    config = load_template_config(root)
    current = detect_identity(root)
    if dry_run:
        return []
    apply_identity(root, current, answers.identity)
    return apply_optional_removals(
        root,
        config,
        answers.identity,
        answers.include_sample_code,
        answers.include_architecture_docs,
    )


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Customize this starter when creating a new project."
    )
    parser.add_argument("--app-name", help="Application name (slug used for Spring app name)")
    parser.add_argument("--artifact-id", help="Maven artifactId")
    parser.add_argument("--group-id", help="Maven groupId")
    parser.add_argument("--package-name", help="Java base package")
    parser.add_argument("--description", help="Project description")
    parser.add_argument("--server-port", type=int, help="Server port")
    parser.add_argument(
        "--include-sample-code",
        choices=["true", "false", "yes", "no"],
        help="Keep or drop sample hello/posts/error endpoints",
    )
    parser.add_argument(
        "--include-architecture-docs",
        choices=["true", "false", "yes", "no"],
        help="Keep or drop root architecture markdown files",
    )
    parser.add_argument(
        "--non-interactive",
        action="store_true",
        help="Do not prompt; use flags and current defaults",
    )
    parser.add_argument("--yes", action="store_true", help="Apply without a confirmation prompt")
    parser.add_argument("--dry-run", action="store_true", help="Print the plan and exit")
    parser.add_argument(
        "--root",
        type=Path,
        help="Repository root (defaults to auto-detect)",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    root = args.root.resolve() if args.root else repo_root_from()
    current = detect_identity(root)
    answers = collect_answers(current, args)
    print_plan(current, answers)

    if args.dry_run:
        return 0
    if not args.yes:
        confirm = input("Apply these changes? [Y/n]: ").strip().lower()
        if confirm not in {"", "y", "yes"}:
            print("Aborted.")
            return 1

    removed = configure(root, answers)
    print("Applied project configuration.")
    if removed:
        print(f"Removed {len(removed)} optional file(s).")
    print("Next: review git status, then run ./mvnw test")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, FileNotFoundError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        sys.exit(2)
