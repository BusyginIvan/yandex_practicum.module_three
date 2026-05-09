#!/bin/sh
set -eu

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
ADMIN_REALM="${ADMIN_REALM:-master}"
ADMIN_USERNAME="${KC_BOOTSTRAP_ADMIN_USERNAME:?KC_BOOTSTRAP_ADMIN_USERNAME is required}"
ADMIN_PASSWORD="${KC_BOOTSTRAP_ADMIN_PASSWORD:?KC_BOOTSTRAP_ADMIN_PASSWORD is required}"

BANK_REALM="${BANK_REALM:-bank}"
BANK_REALM_DISPLAY_NAME="${BANK_REALM_DISPLAY_NAME:-Bank}"

BANK_USERS_GROUP_NAME="${BANK_USERS_GROUP_NAME:-bank-users}"
USER_PROFILE_ATTRIBUTES_PATH="${USER_PROFILE_ATTRIBUTES_PATH:-/opt/keycloak/init/user-profile-attributes.json}"

BANK_UI_CLIENT_ID="${BANK_UI_CLIENT_ID:-bank-ui}"
BANK_UI_CLIENT_SECRET="${BANK_UI_CLIENT_SECRET:-bank-ui-secret}"
BANK_UI_BASE_URL="${BANK_UI_BASE_URL:-http://localhost:8080}"
BANK_UI_REDIRECT_URI="${BANK_UI_REDIRECT_URI:-${BANK_UI_BASE_URL}/login/oauth2/code/keycloak}"
BANK_UI_VALID_REDIRECT_URIS="${BANK_UI_VALID_REDIRECT_URIS:-[\"${BANK_UI_REDIRECT_URI}\",\"${BANK_UI_BASE_URL}\",\"${BANK_UI_BASE_URL}/*\"]}"

CASH_CLIENT_ID="${CASH_CLIENT_ID:-cash}"
CASH_CLIENT_SECRET="${CASH_CLIENT_SECRET:-cash-secret}"

TRANSFERS_CLIENT_ID="${TRANSFERS_CLIENT_ID:-transfers}"
TRANSFERS_CLIENT_SECRET="${TRANSFERS_CLIENT_SECRET:-transfers-secret}"

AUTHORITIES_MAPPER_NAME="${AUTHORITIES_MAPPER_NAME:-authorities}"
ACCOUNTS_READ_ROLE="${ACCOUNTS_READ_ROLE:-accounts:read}"
ACCOUNTS_WRITE_ROLE="${ACCOUNTS_WRITE_ROLE:-accounts:write}"
CASH_WRITE_ROLE="${CASH_WRITE_ROLE:-cash:write}"
TRANSFERS_WRITE_ROLE="${TRANSFERS_WRITE_ROLE:-transfers:write}"
ACCOUNTS_BALANCE_WRITE_ROLE="${ACCOUNTS_BALANCE_WRITE_ROLE:-accounts:balance:write}"
NOTIFICATIONS_CASH_ROLE="${NOTIFICATIONS_CASH_ROLE:-notifications:cash}"


get_client_uuid() {
    /opt/keycloak/bin/kcadm.sh get clients \
        -r "$BANK_REALM" \
        -q clientId="$1" \
        --fields id \
        --format csv \
        --noquotes | tr -d '\r\n'
}

get_group_uuid() {
    /opt/keycloak/bin/kcadm.sh get groups \
        -r "$BANK_REALM" \
        -q search="$1" \
        -q exact=true \
        --fields id \
        --format csv \
        --noquotes | tr -d '\r\n'
}

ensure_authorities_mapper() {
    CLIENT_ID="$1"
    CLIENT_UUID="$2"

    if ! /opt/keycloak/bin/kcadm.sh get "clients/$CLIENT_UUID/protocol-mappers/models" \
        -r "$BANK_REALM" \
        --fields name \
        --format csv \
        --noquotes | grep -Fx "$AUTHORITIES_MAPPER_NAME" >/dev/null 2>&1; then
        /opt/keycloak/bin/kcadm.sh create "clients/$CLIENT_UUID/protocol-mappers/models" \
            -r "$BANK_REALM" \
            -s name="$AUTHORITIES_MAPPER_NAME" \
            -s protocol=openid-connect \
            -s protocolMapper=oidc-usermodel-client-role-mapper \
            -s consentRequired=false \
            -s 'config."multivalued"="true"' \
            -s 'config."userinfo.token.claim"="false"' \
            -s 'config."id.token.claim"="false"' \
            -s 'config."access.token.claim"="true"' \
            -s 'config."claim.name"="authorities"' \
            -s 'config."jsonType.label"="String"' \
            -s 'config."usermodel.clientRoleMapping.clientId"="'"$CLIENT_ID"'"' >/dev/null
        echo "Created protocol mapper '$AUTHORITIES_MAPPER_NAME' for client '$CLIENT_ID'."
    else
        echo "Protocol mapper '$AUTHORITIES_MAPPER_NAME' already exists for client '$CLIENT_ID'."
    fi
}

ensure_service_account_client_role() {
    CLIENT_ID="$1"
    CLIENT_UUID="$2"
    ROLE_NAME="$3"

    if ! /opt/keycloak/bin/kcadm.sh get "clients/$CLIENT_UUID/roles/$ROLE_NAME" -r "$BANK_REALM" >/dev/null 2>&1; then
        /opt/keycloak/bin/kcadm.sh create "clients/$CLIENT_UUID/roles" \
            -r "$BANK_REALM" \
            -s name="$ROLE_NAME" >/dev/null
        echo "Created client role '$ROLE_NAME'."
    else
        echo "Client role '$ROLE_NAME' already exists."
    fi

    /opt/keycloak/bin/kcadm.sh add-roles \
        -r "$BANK_REALM" \
        --uusername "service-account-$CLIENT_ID" \
        --cclientid "$CLIENT_ID" \
        --rolename "$ROLE_NAME" >/dev/null 2>&1 || true
}


/opt/keycloak/bin/kc.sh start-dev & KEYCLOAK_PID=$!

cleanup() { kill "$KEYCLOAK_PID" 2>/dev/null || true; }
trap cleanup INT TERM

echo "Waiting for Keycloak to become available..."
until /opt/keycloak/bin/kcadm.sh config credentials \
    --server "$KEYCLOAK_URL" \
    --realm "$ADMIN_REALM" \
    --user "$ADMIN_USERNAME" \
    --password "$ADMIN_PASSWORD" >/dev/null 2>&1
do
    sleep 2
done


if ! /opt/keycloak/bin/kcadm.sh get "realms/$BANK_REALM" >/dev/null 2>&1; then
    /opt/keycloak/bin/kcadm.sh create realms \
        -s realm="$BANK_REALM" \
        -s enabled=true \
        -s registrationAllowed=true \
        -s displayName="$BANK_REALM_DISPLAY_NAME" >/dev/null
    echo "Created realm '$BANK_REALM'."
else
    echo "Realm '$BANK_REALM' already exists."
fi


/opt/keycloak/bin/kcadm.sh update users/profile \
    -r "$BANK_REALM" \
    -f "$USER_PROFILE_ATTRIBUTES_PATH" >/dev/null


BANK_UI_CLIENT_UUID="$(get_client_uuid "$BANK_UI_CLIENT_ID")"

if [ -z "$BANK_UI_CLIENT_UUID" ]; then
    /opt/keycloak/bin/kcadm.sh create clients -r "$BANK_REALM" \
        -s enabled=true \
        -s protocol=openid-connect \
        -s clientId="$BANK_UI_CLIENT_ID" \
        -s name="Bank UI client" \
        -s publicClient=false \
        -s secret="$BANK_UI_CLIENT_SECRET" \
        -s standardFlowEnabled=true \
        -s directAccessGrantsEnabled=false \
        -s implicitFlowEnabled=false \
        -s serviceAccountsEnabled=false \
        -s frontchannelLogout=true \
        -s "redirectUris=$BANK_UI_VALID_REDIRECT_URIS" \
        -s 'webOrigins=["'"$BANK_UI_BASE_URL"'"]' \
        -s 'attributes."post.logout.redirect.uris"="+"' \
        -s rootUrl="$BANK_UI_BASE_URL" \
        -s baseUrl="$BANK_UI_BASE_URL" >/dev/null

    echo "Created client '$BANK_UI_CLIENT_ID'."
else
    echo "Client '$BANK_UI_CLIENT_ID' already exists. Skipping creation."
fi

BANK_UI_CLIENT_UUID="$(get_client_uuid "$BANK_UI_CLIENT_ID")"


CASH_CLIENT_UUID="$(get_client_uuid "$CASH_CLIENT_ID")"

if [ -z "$CASH_CLIENT_UUID" ]; then
    /opt/keycloak/bin/kcadm.sh create clients -r "$BANK_REALM" \
        -s enabled=true \
        -s protocol=openid-connect \
        -s clientId="$CASH_CLIENT_ID" \
        -s name="Cash service client" \
        -s publicClient=false \
        -s secret="$CASH_CLIENT_SECRET" \
        -s standardFlowEnabled=false \
        -s directAccessGrantsEnabled=false \
        -s implicitFlowEnabled=false \
        -s serviceAccountsEnabled=true \
        -s frontchannelLogout=false >/dev/null

    echo "Created client '$CASH_CLIENT_ID'."
else
    echo "Client '$CASH_CLIENT_ID' already exists. Skipping creation."
fi

CASH_CLIENT_UUID="$(get_client_uuid "$CASH_CLIENT_ID")"


TRANSFERS_CLIENT_UUID="$(get_client_uuid "$TRANSFERS_CLIENT_ID")"

if [ -z "$TRANSFERS_CLIENT_UUID" ]; then
    /opt/keycloak/bin/kcadm.sh create clients -r "$BANK_REALM" \
        -s enabled=true \
        -s protocol=openid-connect \
        -s clientId="$TRANSFERS_CLIENT_ID" \
        -s name="Transfers service client" \
        -s publicClient=false \
        -s secret="$TRANSFERS_CLIENT_SECRET" \
        -s standardFlowEnabled=false \
        -s directAccessGrantsEnabled=false \
        -s implicitFlowEnabled=false \
        -s serviceAccountsEnabled=true \
        -s frontchannelLogout=false >/dev/null

    echo "Created client '$TRANSFERS_CLIENT_ID'."
else
    echo "Client '$TRANSFERS_CLIENT_ID' already exists. Skipping creation."
fi

TRANSFERS_CLIENT_UUID="$(get_client_uuid "$TRANSFERS_CLIENT_ID")"


BANK_USERS_GROUP_UUID="$(get_group_uuid "$BANK_USERS_GROUP_NAME")"

if [ -z "$BANK_USERS_GROUP_UUID" ]; then
    /opt/keycloak/bin/kcadm.sh create groups \
        -r "$BANK_REALM" \
        -s name="$BANK_USERS_GROUP_NAME" >/dev/null
    echo "Created group '$BANK_USERS_GROUP_NAME'."
else
    echo "Group '$BANK_USERS_GROUP_NAME' already exists."
fi

BANK_USERS_GROUP_UUID="$(get_group_uuid "$BANK_USERS_GROUP_NAME")"

/opt/keycloak/bin/kcadm.sh update \
    "realms/$BANK_REALM/default-groups/$BANK_USERS_GROUP_UUID" \
    -n >/dev/null 2>&1 || true


for ROLE_NAME in "$ACCOUNTS_READ_ROLE" "$ACCOUNTS_WRITE_ROLE" "$CASH_WRITE_ROLE" "$TRANSFERS_WRITE_ROLE"
do
    if ! /opt/keycloak/bin/kcadm.sh get "clients/$BANK_UI_CLIENT_UUID/roles/$ROLE_NAME" -r "$BANK_REALM" >/dev/null 2>&1; then
        /opt/keycloak/bin/kcadm.sh create "clients/$BANK_UI_CLIENT_UUID/roles" \
            -r "$BANK_REALM" \
            -s name="$ROLE_NAME" >/dev/null
        echo "Created client role '$ROLE_NAME'."
    else
        echo "Client role '$ROLE_NAME' already exists."
    fi

    /opt/keycloak/bin/kcadm.sh add-roles \
        -r "$BANK_REALM" \
        --gid "$BANK_USERS_GROUP_UUID" \
        --cclientid "$BANK_UI_CLIENT_ID" \
        --rolename "$ROLE_NAME" >/dev/null 2>&1 || true
done


for ROLE_NAME in "$ACCOUNTS_BALANCE_WRITE_ROLE" "$NOTIFICATIONS_CASH_ROLE"
do
    ensure_service_account_client_role \
        "$CASH_CLIENT_ID" \
        "$CASH_CLIENT_UUID" \
        "$ROLE_NAME"
done


for ROLE_NAME in "$ACCOUNTS_BALANCE_WRITE_ROLE"
do
    ensure_service_account_client_role \
        "$TRANSFERS_CLIENT_ID" \
        "$TRANSFERS_CLIENT_UUID" \
        "$ROLE_NAME"
done


ensure_authorities_mapper "$BANK_UI_CLIENT_ID" "$BANK_UI_CLIENT_UUID"
ensure_authorities_mapper "$CASH_CLIENT_ID" "$CASH_CLIENT_UUID"
ensure_authorities_mapper "$TRANSFERS_CLIENT_ID" "$TRANSFERS_CLIENT_UUID"


wait "$KEYCLOAK_PID"
