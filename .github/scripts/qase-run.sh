#!/usr/bin/env bash

set -euo pipefail

script_directory=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
repository_root=$(cd "$script_directory/../.." && pwd)
config_file="$repository_root/qase.config.json"
api_base_url=${QASE_API_BASE_URL:-https://api.qase.io/v1}
api_token=${QASE_TESTOPS_API_TOKEN:-}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Required command is not available: $1" >&2
    exit 1
  fi
}

require_command curl
require_command jq

if [[ -z "$api_token" ]]; then
  echo "QASE_TESTOPS_API_TOKEN is required" >&2
  exit 1
fi

if [[ ! -f "$config_file" ]]; then
  echo "Qase configuration file not found: $config_file" >&2
  exit 1
fi

project_code=${QASE_TESTOPS_PROJECT:-$(jq -er '.testops.project | select(type == "string" and length > 0)' "$config_file")}
if [[ ! "$project_code" =~ ^[A-Za-z0-9_-]{2,10}$ ]]; then
  echo "Invalid Qase project code: $project_code" >&2
  exit 1
fi

api_base_url=${api_base_url%/}

qase_request() {
  local method=$1
  local endpoint=$2
  local payload=${3:-}
  local arguments=(
    --silent
    --show-error
    --fail-with-body
    --connect-timeout 10
    --max-time 60
    --request "$method"
    --header "Token: $api_token"
    --header "Accept: application/json"
  )

  if [[ -n "$payload" ]]; then
    arguments+=(--header "Content-Type: application/json" --data "$payload")
  fi

  curl "${arguments[@]}" "$api_base_url/$endpoint"
}

create_run() {
  local title=${1:-}
  local environment_slug=${2:-}
  local description=${3:-}

  if [[ -z "$title" || ${#title} -gt 255 ]]; then
    echo "Qase run title must contain between 1 and 255 characters" >&2
    exit 1
  fi
  if [[ ! "$environment_slug" =~ ^[a-z0-9][a-z0-9-]*$ ]]; then
    echo "Invalid Qase environment slug: $environment_slug" >&2
    exit 1
  fi
  if (( ${#description} > 10000 )); then
    echo "Qase run description must not exceed 10000 characters" >&2
    exit 1
  fi

  local payload
  payload=$(jq -cn \
    --arg title "$title" \
    --arg description "$description" \
    --arg environment "$environment_slug" \
    '{
      title: $title,
      description: $description,
      environment_slug: $environment,
      is_autotest: true
    }')

  local response
  response=$(qase_request POST "run/$project_code" "$payload")
  if ! jq -e '.status == true and (.result.id | type == "number")' <<< "$response" >/dev/null; then
    echo "Qase did not return a valid run identifier" >&2
    exit 1
  fi
  jq -r '.result.id' <<< "$response"
}

complete_run() {
  local run_id=${1:-}
  if [[ ! "$run_id" =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid Qase run identifier: $run_id" >&2
    exit 1
  fi

  local response
  response=$(qase_request POST "run/$project_code/$run_id/complete")
  if ! jq -e '.status == true' <<< "$response" >/dev/null; then
    echo "Qase did not confirm run completion" >&2
    exit 1
  fi
}

case "${1:-}" in
  create)
    create_run "${2:-}" "${3:-}" "${4:-}"
    ;;
  complete)
    complete_run "${2:-}"
    ;;
  *)
    echo "Usage: $0 create <title> <environment-slug> [description] | complete <run-id>" >&2
    exit 1
    ;;
esac
