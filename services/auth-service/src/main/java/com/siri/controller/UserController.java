package com.siri.controller;

import com.siri.model.UserRecord;
import com.siri.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    public final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRecord userRecord){
        String userId = userService.createUser(userRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @PutMapping("/{id}/send-verification-email")
    public ResponseEntity<?> sendVerificationEmail(@PathVariable("id") String id){
        userService.sendVerificationEmail(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id){
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("username") String username){
        userService.forgotPassword(username);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable("id") String id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserRoles(id));
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<?> getUserGroups(@PathVariable("id") String id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserGroups(id));
    }
}
