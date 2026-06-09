FROM quay.io/keycloak/keycloak:26.1.3

COPY --chmod=0755 keycloak/init-clients.sh /opt/keycloak/init/init-clients.sh
COPY keycloak/user-profile-attributes.json /opt/keycloak/init/user-profile-attributes.json

ENTRYPOINT ["/bin/sh", "/opt/keycloak/init/init-clients.sh"]
