package com.siri.service.Impl;

import com.siri.service.GroupService;
import com.siri.service.RoleService;
import com.siri.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final UserService userService;
    @Value("${app.keycloak.realm}")
    private String realm;
    private final Keycloak keycloak;

    @Override
    public void assignGroup(String userId, String groupId) {
        UserResource user = userService.getUser(userId);
        user.joinGroup(groupId);
    }

    @Override
    public void deleteGroupFromUser(String userId, String groupId) {
        UserResource user = userService.getUser(userId);
        user.leaveGroup(groupId);
    }
}
