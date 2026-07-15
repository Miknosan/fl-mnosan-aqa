#!/usr/bin/env bash

set -euo pipefail

script_directory=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
repository_root=$(cd "$script_directory/../.." && pwd)
resolver="$script_directory/resolve-execution.sh"

assert_failure() {
  if "$@" >/dev/null 2>&1; then
    echo "Expected command to fail: $*" >&2
    exit 1
  fi
}

cd "$repository_root"

all_output=$("$resolver" dev all smoke false false)
all_matrix=$(printf '%s\n' "$all_output" | sed -n 's/^matrix=//p')
jq -e '.include | length == 3' <<< "$all_matrix" >/dev/null
jq -e '.include | map(.domain) == ["booker", "hygraph", "demoqa"]' <<< "$all_matrix" >/dev/null

selected_output=$("$resolver" stage demoqa,booker,demoqa regression true true)
selected_matrix=$(printf '%s\n' "$selected_output" | sed -n 's/^matrix=//p')
jq -e '.include | length == 2' <<< "$selected_matrix" >/dev/null
jq -e '.include | map(.domain) == ["demoqa", "booker"]' <<< "$selected_matrix" >/dev/null
grep -qx 'environment=stage' <<< "$selected_output"
grep -qx 'plan=regression' <<< "$selected_output"
grep -qx 'publish_qase=true' <<< "$selected_output"
grep -qx 'publish_report=true' <<< "$selected_output"

assert_failure "$resolver" unknown all smoke false false
assert_failure "$resolver" dev invalid smoke false false
assert_failure "$resolver" dev all invalid false false
assert_failure "$resolver" dev all smoke invalid false
assert_failure "$resolver" dev all smoke false invalid

temporary_root=$(mktemp -d)
trap 'rm -rf "$temporary_root"' EXIT
mkdir -p "$temporary_root/config/environments"
printf 'test.environment=stage\n' > "$temporary_root/config/environments/dev.properties"
if (cd "$temporary_root" && "$resolver" dev all smoke false false) >/dev/null 2>&1; then
  echo "Expected mismatched environment profile to fail" >&2
  exit 1
fi
