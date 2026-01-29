package com.example.hotels_app.controller;

import com.example.hotels_app.PostgresBaseTest;
import com.example.hotels_app.entity.User;
import com.example.hotels_app.model.request.UpsertUserRequest;
import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.security.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends PostgresBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        Mockito.reset(kafkaTemplate); // Важно для верификации в цикле
    }

    @ParameterizedTest(name = "Test {index}: create user with role {0}")
    @EnumSource(Role.class)
    @DisplayName("""
            Given unauthorized user
            When request to create new user is sent
            Then status is 201 Created
             and user is saved in PostgreSQL
             and message is sent to Kafka
            """
    )
    void whenCreateUserWithAppropriateRole_thenUserCreated(Role role) throws Exception {
        String userName = "User_" + role.name();
        String userEmail = role.name().toLowerCase() + "@example.com";

        UpsertUserRequest request = new UpsertUserRequest(userName, "pass", userEmail, role);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(userName))
                .andExpect(jsonPath("$.role").value(role.toString()));

        User savedUser = userRepository.findByName(userName)
                .orElseThrow(() -> new AssertionError("User not found in database: " + userName));

        assertEquals(userEmail, savedUser.getEmail());
        assertEquals(role, savedUser.getRole());

        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @ParameterizedTest(name = "Test {index}: update user with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given user exists in database
        When authorized user with valid role requests to update user with valid new data
        Then status is 200 OK
         and user data is updated in PostgreSQL
        """)
    void whenUpdateAsUserWithValidRole_thenUserUpdated(String role) throws Exception {

        User user = saveUser("OldName", "oldpass", "old@mail.com", Role.USER);
        String newName = "NewName";
        String newEmail = "new@mail.com";

        UpsertUserRequest request = new UpsertUserRequest(
                newName, "newpass", newEmail, Role.USER
        );

        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                            .with(user("test_auth_user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.email").value(newEmail));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @ParameterizedTest(name = "Test {index}: get user by id with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given user exists in database
        When authorized user with ROLE requests user by id
        Then status is 200 OK
         and response contains correct user data
        """)
    void whenGetByIdWithValidRole_thenUserFound(String role) throws Exception {

        String userName = "User_" + role;
        User user = saveUser(userName, "paswd123", "target@mail.com", Role.USER);

        mockMvc.perform(get("/api/v1/users/{id}", user.getId())
                        .with(user("test_auth_user").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(userName))
                .andExpect(jsonPath("$.email").value("target@mail.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given user exists in database
        When authorized user requests to delete user by id
        Then status is 204 No Content
         and user is removed from database
        """)
    void whenDeleteFromUSerWithAdminRole_thenReturnNoContent() throws Exception {
        User user = saveUser("To Delete", "passwd123", "del@mail.com", Role.USER);

        assertTrue(userRepository.findById(user.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(user.getId()).isEmpty());
    }

    //--- -----------------------NEGATIVE TESTS-----------------------------------------

    @Test
    @DisplayName("""
        Given invalid user data
        When request to create user is sent
        Then status is 400 Bad Request
        """)
    void whenCreateUserWithInvalidData_thenReturn400() throws Exception {
        UpsertUserRequest request = new UpsertUserRequest("", "", "not-an-email", null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given user does not exist in database
        When authorized user requests user by non-existent id
        Then status is 404 Not Found
        """)
    void whenGetByIdNotExit_thenReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/users/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("""
        Given anonymous user
        When request to get all users is sent
        Then status is 401 Unauthorized
        """)
    void whenNotExistUserGetAllUsers_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given username already exists in database
        When request to create user with same name is sent
        Then status is 409 Conflict
         and message indicates username conflict
        """)
    void whenCreateUserWithExistingName_thenConflict() throws Exception {
        String existingName = "ExistingUser";
        saveUser(existingName, "pass", "unique@mail.com", Role.USER);

        UpsertUserRequest request = new UpsertUserRequest(
                existingName, "newpass", "new@mail.com", Role.USER
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name '" + existingName + "' already exists")));

        verify(kafkaTemplate, times(0)).send(any(), any());
    }

    @Test
    @DisplayName("""
        Given email already exists in database
        When request to create user with same email is sent
        Then status is 409 Conflict
         and message indicates email conflict
        """)
    void whenCreateUserWithExistingEmail_thenConflict() throws Exception {
        String existingEmail = "duplicate@example.com";
        saveUser("FirstUser", "pass", existingEmail, Role.USER);

        UpsertUserRequest request = new UpsertUserRequest(
                "SecondUser", "pass", existingEmail, Role.USER
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email '" + existingEmail + "' already exists")));

        verify(kafkaTemplate, times(0)).send(any(), any());
    }

    @ParameterizedTest(name = "Test {index}: update user with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given two users exist in database
        When authorized user with valid role requests to update first user with name of second user
        Then status is 409 Conflict
         and user data in database remains unchanged
        """)
    void whenUpdateUserToExistingName_thenConflict(String role) throws Exception {
        User firstUser = saveUser("UserOne", "pass1", "one@mail.com", Role.USER);
        User secondUser = saveUser("UserTwo", "pass2", "two@mail.com", Role.USER);

        UpsertUserRequest request = new UpsertUserRequest(
                "UserTwo",
                "newpass",
                "new_unique@mail.com",
                Role.USER
        );

        mockMvc.perform(put("/api/v1/users/{id}", firstUser.getId())
                            .with(user("test_auth_user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already exists")));

        User unchangedUser = userRepository.findById(firstUser.getId()).orElseThrow();
        assertEquals("UserOne", unchangedUser.getName());
    }


    private User saveUser(String name, String password, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return userRepository.save(user);
    }
}