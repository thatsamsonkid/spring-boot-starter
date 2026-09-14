# Spring Boot starter

Hexagonal Spring WebFlux starter. After you create a new repository from this
template, run the configurator to set the app name, package, and whether sample
APIs should be copied in.

## Create a project

1. Use this repository as a GitHub template (or clone it).
2. From the new project root, run:

```bash
./configure-project
```

The wizard asks for:

- Application name
- Maven `artifactId` / `groupId`
- Java base package
- Description and server port
- Whether to keep the sample hello/posts/error endpoints
- Whether to keep the architecture write-ups

Non-interactive example:

```bash
./configure-project \
  --app-name orders \
  --group-id com.acme \
  --artifact-id orders \
  --package-name com.acme.orders \
  --description "Orders API" \
  --server-port 8080 \
  --include-sample-code false \
  --include-architecture-docs true \
  --non-interactive \
  --yes
```

`--dry-run` prints the plan without changing files.

The default tree is a working sandbox, including sample endpoints. Choosing
"no" for sample code leaves the hexagonal skeleton plus `/api/v1/health`.

## Other ways to do this later

This in-repo wizard is the lightest fit for a GitHub template: the repository
stays a real app, and customization happens once after clone.

If you outgrow it:

- **[Copier](https://copier.readthedocs.io/)** or **[Cookiecutter](https://cookiecutter.readthedocs.io/)** if you want a generate-into-a-new-directory flow with Jinja conditionals.
- **Maven Archetype** (`mvn archetype:generate`) if consumers should stay on Maven tooling and you are willing to publish the archetype.

Questions live in `template/config.json` so you can add more prompts without
rewriting the script.

## Develop

```bash
./mvnw test
python3 -m unittest scripts.test_configure_project
```
