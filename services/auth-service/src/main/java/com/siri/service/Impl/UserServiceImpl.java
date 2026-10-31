package com.siri.service.Impl;

import com.siri.model.UserRecord;
import com.siri.service.UserService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;

    @Override
    public String createUser(UserRecord userRecord) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(userRecord.firstname());
        userRepresentation.setLastName(userRecord.lastname());
        userRepresentation.setUsername(userRecord.username());
        userRepresentation.setEmail(userRecord.username());
        userRepresentation.setEmailVerified(false);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(userRecord.password());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);

        userRepresentation.setCredentials(List.of(credentialRepresentation));

        UsersResource usersResource = getUsersResource();
        Response response = usersResource.create(userRepresentation);

        log.info("Status code: " + response.getStatus());

        if(!Objects.equals(201, response.getStatus())) {
            throw new RuntimeException("Failed to create user. Status code: " + response.getStatus());
        }
        
        // Extract user ID from Location header
        String location = response.getHeaderString("Location");
        String userId = location.substring(location.lastIndexOf("/") + 1);
        
        log.info("User created successfully with ID: " + userId);

        UserResource createdUserResource = usersResource.get(userId);
        CredentialRepresentation passwordReset = new CredentialRepresentation();
        passwordReset.setType(CredentialRepresentation.PASSWORD);
        passwordReset.setValue(userRecord.password());
        passwordReset.setTemporary(false);
        createdUserResource.resetPassword(passwordReset);
        
        sendVerificationEmail(userId);
        return userId;
    }

    @Override
    public void sendVerificationEmail(String id) {
        UsersResource userResource = getUsersResource();
        userResource.get(id).sendVerifyEmail();
    }

    @Override
    public void deleteUser(String userId) {
        UsersResource userResource = getUsersResource();
        userResource.delete(userId);
    }

    @Override
    public void forgotPassword(String username) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> users = usersResource.searchByUsername(username, true);
        UserRepresentation user = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        //above line we can write like users.get(0) because we are searching by username and it should be unique but to be safe we can filter it like above
        UserResource userResource = usersResource.get(user.getId());
        userResource.executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    @Override
    public UserResource getUser(String userId) {
        UsersResource users = getUsersResource();
        return users.get(userId);
    }

    @Override
    public List<RoleRepresentation> getUserRoles(String userId) {
        return getUser(userId).roles().realmLevel().listAll();
    }

    @Override
    public List<GroupRepresentation> getUserGroups(String userId) {
        return getUser(userId).groups();
    }

    private UsersResource getUsersResource(){
        return keycloak.realm(realm).users();
    }
}
