#!/usr/bin/env bash

set -euo pipefail

script_directory=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
repository_root=$(cd "$script_directory/../.." && pwd)
client="$script_directory/qase-run.sh"
capture_file=$(mktemp)
trap 'rm -f "$capture_file"' EXIT

curl() {
  local response=${QASE_FAKE_RESPONSE:-}
  local exit_code=${QASE_FAKE_EXIT_CODE:-0}
  printf '%s\n' "$@" > "$QASE_CURL_CAPTURE"
  if [[ -z "$response" ]]; then
    response='{"status":true,"result":{"id":321}}'
  fi
  printf '%s\n' "$response"
  return "$exit_code"
}
export -f curl

assert_failure() {
  if "$@" >/dev/null 2>&1; then
    echo "Expected command to fail: $*" >&2
    exit 1
  fi
}

cd "$repository_root"

export QASE_CURL_CAPTURE="$capture_file"
export QASE_TESTOPS_API_TOKEN="test-token"
export QASE_TESTOPS_PROJECT="DA"
export QASE_API_BASE_URL="https://qase.example.test/v1/"

run_id=$(QASE_FAKE_RESPONSE='{"status":true,"result":{"id":321}}' \
  "$client" create "Quality gate | dev | smoke | GitHub #7" dev "Domains: all")
[[ "$run_id" == "321" ]]
grep -Fx 'POST' "$capture_file" >/dev/null
grep -Fx 'https://qase.example.test/v1/run/DA' "$capture_file" >/dev/null

payload=$(awk 'previous == "--data" {print; exit} {previous=$0}' "$capture_file")
jq -e '
  .title == "Quality gate | dev | smoke | GitHub #7"
  and .description == "Domains: all"
  and .environment_slug == "dev"
  and .is_autotest == true
' <<< "$payload" >/dev/null

QASE_FAKE_RESPONSE='{"status":true,"result":null}' "$client" complete 321
grep -Fx 'POST' "$capture_file" >/dev/null
grep -Fx 'https://qase.example.test/v1/run/DA/321/complete' "$capture_file" >/dev/null

public_url=$(QASE_FAKE_RESPONSE='{"status":true,"result":{"url":"https://app.qase.io/public/report/public-token"}}' \
  "$client" share 321)
[[ "$public_url" == "https://app.qase.io/public/report/public-token" ]]
grep -Fx 'PATCH' "$capture_file" >/dev/null
grep -Fx 'https://qase.example.test/v1/run/DA/321/public' "$capture_file" >/dev/null

payload=$(awk 'previous == "--data" {print; exit} {previous=$0}' "$capture_file")
jq -e '.status == true' <<< "$payload" >/dev/null

assert_failure env -u QASE_TESTOPS_API_TOKEN "$client" create "Run" dev
assert_failure "$client" create "" dev
assert_failure "$client" create "Run" 'invalid/environment'
assert_failure "$client" complete 0
assert_failure "$client" share 0
assert_failure env QASE_FAKE_EXIT_CODE=22 "$client" create "Run" dev
assert_failure env QASE_FAKE_EXIT_CODE=22 "$client" complete 321
assert_failure env QASE_FAKE_EXIT_CODE=22 "$client" share 321
assert_failure env QASE_FAKE_RESPONSE='{"status":false,"result":null}' "$client" create "Run" dev
assert_failure env QASE_FAKE_RESPONSE='{"status":false,"result":null}' "$client" complete 321
assert_failure env QASE_FAKE_RESPONSE='{"status":false,"result":null}' "$client" share 321
assert_failure env QASE_FAKE_RESPONSE='{"status":true,"result":{"url":"invalid"}}' "$client" share 321
