package dev.andrewjfei.user.management.api.example.controllers.v1;

import dev.andrewjfei.user.management.api.example.controllers.BaseComponentTest;
import dev.andrewjfei.user.management.api.example.daos.FriendshipDao;
import dev.andrewjfei.user.management.api.example.exceptions.UserManagementApiExampleException;
import dev.andrewjfei.user.management.api.example.repositories.v1.FriendshipRepository;
import dev.andrewjfei.user.management.api.example.repositories.v1.UserRepository;
import dev.andrewjfei.user.management.api.example.transactions.requests.TargetUserIdRequest;
import dev.andrewjfei.user.management.api.example.transactions.requests.UserIdRequest;
import dev.andrewjfei.user.management.api.example.transactions.responses.BasicUserResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static dev.andrewjfei.user.management.api.example.enums.Error.USER_FRIEND_REQUEST_ERROR;
import static dev.andrewjfei.user.management.api.example.enums.Error.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class UserControllerComponentTest extends BaseComponentTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    // Joe Smith
    private final String JOE_SMITH_USER_ID = "11afa5b4-c622-4320-a4e9-7c374172b63d";

    private final int JOE_SMITH_NUM_OF_FRIENDS = 1;

    // Casey Wang

    private final String CASEY_WANG_USER_ID = "26770bad-887c-4ef7-a77c-f582d50e201c";

    // Alex Chen

    private final String ALEX_CHEN_USER_ID = "b0a73dff-25a8-4ea6-9a84-3a7338d04ba7";

    public UserControllerComponentTest() {

    }

    /********************* User APIs *********************/

    @Test
    public void testBlockUser_returnsCorrectString() {
        String expected = "User blocked.";

        ResponseEntity<String> response = userController.blockUser();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    /********************* User Friends APIs *********************/

    @Test
    public void testAddFriend_returnsCorrectString() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TargetUserIdRequest request = new TargetUserIdRequest(JOE_SMITH_USER_ID, ALEX_CHEN_USER_ID);

        // When
        ResponseEntity<String> response = userController.addFriend(request);

        // Then
        assertEquals(OK, response.getStatusCode());

        FriendshipDao friendshipDao =
                friendshipRepository.findByRequesterIdAndReceiverId(UUID.fromString(JOE_SMITH_USER_ID), UUID.fromString(ALEX_CHEN_USER_ID));

        assertFalse(friendshipDao.isAccepted());
        assertTrue(friendshipDao.getCreated().isAfter(now));
    }

    @Test
    public void testAddFriend_pendingRequestOrAlreadyFriends_throwsException() {
        // Given
        TargetUserIdRequest request = new TargetUserIdRequest(JOE_SMITH_USER_ID, CASEY_WANG_USER_ID);

        // When
        // Then
        UserManagementApiExampleException userManagementApiExampleException =
                assertThrows(UserManagementApiExampleException.class, () ->  userController.addFriend(request));

        assertEquals(BAD_REQUEST, userManagementApiExampleException.getHttpStatus());
        assertEquals(USER_FRIEND_REQUEST_ERROR, userManagementApiExampleException.getError());
    }

    @Test
    public void testAddFriend_invalidRequesterId_throwsException() {
        // Given
        String invalidUserId = UUID.randomUUID().toString();
        TargetUserIdRequest request = new TargetUserIdRequest(invalidUserId, CASEY_WANG_USER_ID);

        // When
        // Then
        UserManagementApiExampleException userManagementApiExampleException =
                assertThrows(UserManagementApiExampleException.class, () ->  userController.addFriend(request));

        assertEquals(BAD_REQUEST, userManagementApiExampleException.getHttpStatus());
        assertEquals(USER_NOT_FOUND, userManagementApiExampleException.getError());
    }

    @Test
    public void testAddFriend_invalidReceiverId_throwsException() {
        // Given
        String invalidUserId = UUID.randomUUID().toString();
        TargetUserIdRequest request = new TargetUserIdRequest(JOE_SMITH_USER_ID, invalidUserId);

        // When
        // Then
        UserManagementApiExampleException userManagementApiExampleException =
                assertThrows(UserManagementApiExampleException.class, () ->  userController.addFriend(request));

        assertEquals(BAD_REQUEST, userManagementApiExampleException.getHttpStatus());
        assertEquals(USER_NOT_FOUND, userManagementApiExampleException.getError());
    }

    @Test
    public void testFetchAllFriends_returnsCorrectResponse() {
        // Given
        UserIdRequest request = new UserIdRequest(JOE_SMITH_USER_ID);

        // When
        ResponseEntity<List<BasicUserResponse>> response = userController.fetchAllFriends(request);

        // Then
        List<BasicUserResponse> userFriendsList = response.getBody();

        assertEquals(OK, response.getStatusCode());
        assertEquals(JOE_SMITH_NUM_OF_FRIENDS, userFriendsList.size());
    }

    @Test
    public void testFetchAllFriends_invalidUserId_throwsException() {
        // Given
        String invalidUserId = UUID.randomUUID().toString();
        UserIdRequest request = new UserIdRequest(invalidUserId);

        // When
        // Then
        UserManagementApiExampleException userManagementApiExampleException =
                assertThrows(UserManagementApiExampleException.class, () ->  userController.fetchAllFriends(request));

        assertEquals(BAD_REQUEST, userManagementApiExampleException.getHttpStatus());
        assertEquals(USER_NOT_FOUND, userManagementApiExampleException.getError());
    }

    @Test
    public void testRemoveFriend_returnsCorrectString() {
        String expected = "Friend Removed.";

        ResponseEntity<String> response = userController.removeFriend();

        assertEquals(OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

}
