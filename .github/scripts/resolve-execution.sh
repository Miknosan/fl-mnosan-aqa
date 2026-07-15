#!/usr/bin/env bash

set -euo pipefail

environment_name=$(printf '%s' "${1:?Environment is required}" | tr '[:upper:]' '[:lower:]' | xargs)
requested_domains=$(printf '%s' "${2:?Domains are required}" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
test_plan=$(printf '%s' "${3:?Test plan is required}" | tr '[:upper:]' '[:lower:]' | xargs)
publish_qase=$(printf '%s' "${4:-false}" | tr '[:upper:]' '[:lower:]' | xargs)
publish_report=$(printf '%s' "${5:-false}" | tr '[:upper:]' '[:lower:]' | xargs)

if [[ ! "$environment_name" =~ ^[a-z0-9][a-z0-9-]*$ ]]; then
  echo "Environment must match [a-z0-9][a-z0-9-]*" >&2
  exit 1
fi

profile="config/environments/$environment_name.properties"
if [[ ! -f "$profile" ]]; then
  echo "Environment profile not found: $profile" >&2
  exit 1
fi

declared_environment=$(awk -F= '$1 == "test.environment" {print $2}' "$profile" | xargs)
if [[ "$declared_environment" != "$environment_name" ]]; then
  echo "Environment profile declares '$declared_environment' but execution requested '$environment_name'" >&2
  exit 1
fi

case "$test_plan" in
  smoke|regression|sanity) ;;
  *)
    echo "Unsupported test plan: $test_plan" >&2
    exit 1
    ;;
esac

case "$publish_qase" in
  true|false) ;;
  *)
    echo "Qase publication flag must be true or false" >&2
    exit 1
    ;;
esac

case "$publish_report" in
  true|false) ;;
  *)
    echo "Allure publication flag must be true or false" >&2
    exit 1
    ;;
esac

if [[ "$requested_domains" == "all" ]]; then
  requested_domains="booker,hygraph,demoqa"
fi

IFS=',' read -r -a domains <<< "$requested_domains"
if (( ${#domains[@]} == 0 )); then
  echo "At least one domain is required" >&2
  exit 1
fi

matrix_entries=()
selected_domain_list=','
for domain in "${domains[@]}"; do
  if [[ "$selected_domain_list" == *",$domain,"* ]]; then
    continue
  fi
  case "$domain" in
    booker)
      matrix_entries+=('{"domain":"booker","module":"booker-api-tests","ui":false}')
      ;;
    hygraph)
      matrix_entries+=('{"domain":"hygraph","module":"hygraph-graphql-tests","ui":false}')
      ;;
    demoqa)
      matrix_entries+=('{"domain":"demoqa","module":"demoqa-ui-tests","ui":true}')
      ;;
    *)
      echo "Unsupported domain: $domain" >&2
      exit 1
      ;;
  esac
  selected_domain_list+="$domain,"
done

matrix=$(IFS=,; printf '{"include":[%s]}' "${matrix_entries[*]}")
selected_domains=${selected_domain_list#,}
selected_domains=${selected_domains%,}

echo "environment=$environment_name"
echo "domains=$selected_domains"
echo "plan=$test_plan"
echo "publish_qase=$publish_qase"
echo "publish_report=$publish_report"
echo "matrix=$matrix"
