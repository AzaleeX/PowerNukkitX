# Welcome to PowerNukkitX contributing guide <!-- omit in toc -->

Thank you for investing your time in contributing to our project!

In this guide you will get an overview of the contribution workflow from opening an issue, creating a PR, reviewing, and merging the PR.

Use the table of contents icon <img alt="Table of contents icon" src="https://github.com/github/docs/blob/ffe538be0205f4d128f5d4f6e1fab7a5171655b2/contributing/images/table-of-contents.png?raw=true" width="25" height="25" /> on the top left corner of this document to get to a specific section of this guide quickly.

## New contributor guide

To get an overview of the project, read the [README](README.md) file. Here are some resources to help you get started with open source contributions:

- [Finding ways to contribute to open source on GitHub](https://docs.github.com/en/get-started/exploring-projects-on-github/finding-ways-to-contribute-to-open-source-on-github)
- [Set up Git](https://docs.github.com/en/get-started/getting-started-with-git/set-up-git)
- [GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)
- [Collaborating with pull requests](https://docs.github.com/en/github/collaborating-with-pull-requests)


## Getting started

### Issues

#### Create a new issue

If you spot a problem with the project, [search if an issue already exists](https://github.com/PowerNukkitX/PowerNukkitX/issues). If a related issue doesn't exist, you can open a new issue using a relevant [issue form](https://github.com/PowerNukkitX/PowerNukkitX/issues/new/choose).

#### Solve an issue

Scan through our [existing issues](https://github.com/PowerNukkitX/PowerNukkitX/issues) to find one that interests you. You can narrow down the search using `labels` as filters. See "[Label reference](https://docs.github.com/en/contributing/collaborating-on-github-docs/label-reference)" for more information. As a general rule, we don’t assign issues to anyone. If you find an issue to work on, you are welcome to open a PR with a fix.

### Make Changes

1. Fork the repository.
    - [Fork the repo](https://www.jetbrains.com/help/idea/fork-github-projects.html#fork)
    - [Clone your fork](https://www.jetbrains.com/help/idea/manage-projects-hosted-on-github.html#clone-from-GitHub)
2. Install **JDK21** or higher.
3. Make some changes.
4. Build and test your changes:
   - **First build**: Use `buildSkipChores` task to build without tests
   - **Quick iteration**: Use `buildFast` task for rapid development cycles
   - **Clean build**: Use `clean` task to remove all build artifacts
   - **Full build**: Use `build` task for complete build with tests
   
   <img alt="build.png"  src=".github/img/001.png" width="250px"/>
   
5. Verify your changes work correctly:
   - Run relevant tests to ensure you haven't broken anything
   - Start the server to preview your changes
   
   <img alt="img.png" height="100" src=".github/img/002.png"/>

**Build Tips:**
- Use `./gradlew tasks` to see all available build tasks
- The project uses advanced build optimizations:
  - **Parallel execution**: Tasks and tests run in parallel using available CPU cores
  - **Configuration cache**: Dramatically faster build configuration (experimental)
  - **Incremental compilation**: Only recompiles changed files
  - **Forked compilation**: Separate JVM process for compilation with optimized memory
  - **Dependency caching**: Smart caching of dynamic and changing modules
  - **Resource optimization**: Incremental resource processing
- Build tasks optimized for different scenarios:
  - `buildFast`: Fastest - only compiles and creates JAR (no tests, no documentation)
  - `buildSkipChores`: Fast - includes shadowJar but skips tests and documentation
  - `build`: Full build with tests and all artifacts
  - `buildForGithubAction`: Optimized for CI/CD pipelines
- Performance features:
  - Tests run in parallel with automatic JVM forking every 100 tests
  - Compiler uses separate JVM with 2GB heap for main code, 1GB for tests
  - Gradle daemon uses 4GB heap with G1GC and string deduplication
  - Maximum 8 parallel workers for optimal CPU utilization
- The build system uses Gradle 8.10+ with all performance optimizations enabled

### Pull Request

When you're finished with the changes, create a pull request, also known as a PR.
- Fill the "Ready for review" template so that we can review your PR. This template helps reviewers understand your changes as well as the purpose of your pull request.
- Don't forget to [link PR to issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) if you are solving one.
- Enable the checkbox to [allow maintainer edits](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/allowing-changes-to-a-pull-request-branch-created-from-a-fork) so the branch can be updated for a merge.
  Once you submit your PR, a Docs team member will review your proposal. We may ask questions or request additional information.
- We may ask for changes to be made before a PR can be merged, either using [suggested changes](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/incorporating-feedback-in-your-pull-request) or pull request comments. You can apply suggested changes directly through the UI. You can make any other changes in your fork, then commit them to your branch.
- As you update your PR and apply changes, mark each conversation as [resolved](https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/commenting-on-a-pull-request#resolving-conversations).
- If you run into any merge issues, checkout this [git tutorial](https://github.com/skills/resolve-merge-conflicts) to help you resolve merge conflicts and other issues.

### Your PR is merged!

Congratulations :tada::tada: The PowerNukkitX team thanks you :sparkles:.

Once your PR is merged, your contributions will be publicly visible on the PowerNukkitX.

Now that you are part of the PowerNukkitX community.