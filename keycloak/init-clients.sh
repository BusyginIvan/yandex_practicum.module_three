#!/bin/sh
set -eu

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
ADMIN_REALM="${ADMIN_REALM:-master}"
ADMIN_USERNAME="${KC_BOOTSTRAP_ADMIN_USERNAME:?KC_BOOTSTRAP_ADMIN_USERNAME is required}"
ADMIN_PASSWORD="${KC_BOOTSTRAP_ADMIN_PASSWORD:?KC_BOOTSTRAP_ADMIN_PASSWORD is required}"

BANK_REALM="${BANK_REALM:-bank}"
BANK_REALM_DISPLAY_NAME="${BANK_REALM_DISPLAY_NAME:-Bank}"

USER_PROFILE_ATTRIBUTES_PATH="${USER_PROFILE_ATTRIBUTES_PATH:-/opt/keycloak/init/user-profile-attributes.json}"

BANK_UI_CLIENT_ID="${BANK_UI_CLIENT_ID:-bank-ui}"
BANK_UI_CLIENT_SECRET="${BANK_UI_CLIENT_SECRET:-bank-ui-secret}"
BANK_UI_BASE_URL="${BANK_UI_BASE_URL:-http://localhost:8080}"
BANK_UI_REDIRECT_URI="${BANK_UI_REDIRECT_URI:-${BANK_UI_BASE_URL}/login/oauth2/code/keycloak}"
BANK_UI_VALID_REDIRECT_URIS="${BANK_UI_VALID_REDIRECT_URIS:-[\"${BANK_UI_REDIRECT_URI}\",\"${BANK_UI_BASE_URL}\",\"${BANK_UI_BASE_URL}/*\"]}"


get_client_uuid() {
    /opt/keycloak/bin/kcadm.sh get clients \
        -r "$BANK_REALM" \
        -q clientId="$1" \
        --fields id \
        --format csv \
        --noquotes | tr -d '\r\n'
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


wait "$KEYCLOAK_PID"
