#!/usr/bin/env bash
# Deploy this project to Maven Central using maven-release-plugin goals
# (https://maven.apache.org/maven-release/maven-release-plugin/).
#
# release:prepare prompts for the release version, SCM tag, and next development
# version. Maven's suggested next development version is wrong for versions with a
# qualifier such as 3.0.0-alpha.1 (it suggests 3.0.1-alpha.1-SNAPSHOT instead of
# 3.0.0-alpha.2-SNAPSHOT), so check it and type the correct one when prompted.
#
# The release profile is activated by the release plugin's arguments in the pom, so
# -Prelease is not needed.

# Make a pipeline fail if any command in it fails, so that a Maven failure is not
# hidden by tee succeeding
set -o pipefail

function print_usage() {
  echo "Usage: $0 -h -n -s -t"
  echo
  echo '-h: print this help'
  echo "-n: don't clean up release plugin working directory (by default it is deleted)"
  echo '-s: skip confirmation prompt'
  echo '-t: run tests (they will run twice, once during release:prepare and a second time during release:perform)'
}

# set default values
confirm_release=1
run_tests=0
cleanup=1

# process arguments
while getopts 'hnst' opt; do
  case "$opt" in
    h)
      print_usage
      exit 0
      ;;

    n)
      cleanup=0
      ;;

    s)
      confirm_release=0
      ;;

    t)
      run_tests=1
      ;;

    *)
      print_usage
      exit 1
      ;;
  esac
done

# ensure the current branch is ossrh, main, master, or a release branch
echo "Checking branch is 'ossrh', 'main', 'master', or starts with 'release/'"
current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" != 'ossrh' ] && [ "$current_branch" != 'main' ] && [ "$current_branch" != 'master' ] && [[ ! "$current_branch" =~ ^release/ ]]; then
  echo "WARNING: Must be on ossrh, main, master, or a branch starting with 'release/' to release"
  exit 1
fi
echo "Branch is OK [${current_branch}]"

# fetch latest information from remote repository and ensure there are no remote changes
echo 'Checking repository is up to date'
if ! git fetch; then
  echo 'WARNING: git fetch failed, so cannot tell whether the repository is up to date'
  exit 1
fi
if [ "$(git rev-parse HEAD)" != "$(git rev-parse '@{u}')" ]; then
  echo 'WARNING: Repository is not up to date. Run git pull --rebase (or push) and then re-run this script.'
  exit 1
fi
echo 'Repository is up to date'

# ensure there are no local modifications, staged or unstaged (untracked files are ignored)
if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
  echo 'WARNING: Cannot release because there are local modifications'
  exit 1
fi

# confirm release unless configured to skip confirmation
if [[ "$confirm_release" -eq 1 ]]; then
  read -r -p 'Really deploy to maven central repository (yes/no)? '
  confirmation="${REPLY}"
else
  confirmation='yes'
fi

# perform release, or exit if release not confirmed
if [[ "$confirmation" != 'yes' ]]; then
  echo 'Exit without deploy'
  exit 0
fi

if [[ "$run_tests" -eq 0 ]]; then
  echo 'Tests are skipped during this release'
  extra_args=(-Darguments=-DskipTests)
else
  echo 'Tests will be run during this release'
  extra_args=()
fi

echo "Running Maven to perform release (logging to console and maven-central-deploy.log)"
mvn "${extra_args[@]}" release:clean release:prepare release:perform -e | tee maven-central-deploy.log
mvn_result=$?

if [[ "$cleanup" -eq 1 ]]; then
  if [[ "$mvn_result" -eq 0 ]]; then
    echo "Cleanup: Remove Maven release plugin working directory"
    find . -type d -name checkout -path '*/target/checkout' -exec rm -rf {} +
  else
    echo "Cleanup: Leaving Maven release plugin working directory for debugging because Maven execution failed (code: $mvn_result)"
  fi
else
  echo "Cleanup: Not deleting Maven release plugin working directory because -n was specified"
fi

exit "$mvn_result"
