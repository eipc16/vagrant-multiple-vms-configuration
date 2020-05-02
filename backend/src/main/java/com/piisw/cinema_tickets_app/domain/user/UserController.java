package com.piisw.cinema_tickets_app.domain.user;

import com.piisw.cinema_tickets_app.api.AvailableDTO;
import com.piisw.cinema_tickets_app.api.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String CHECK_USERNAME_PATH = "/check/username/{" + USERNAME + "}";
    private static final String CHECK_EMAIL_PATH = "/check/email/{" + EMAIL + "}";
    private static final String ID = "id";
    private static final String ID_PATH = "/{" + ID + "}";

    @GetMapping(CHECK_USERNAME_PATH)
    public AvailableDTO checkUsernameAvailability(@PathVariable(USERNAME) String username) {
        Boolean isAvailable = !userService.userExistsByUsername(username);
        return new AvailableDTO(isAvailable);
    }

    @GetMapping(CHECK_EMAIL_PATH)
    public AvailableDTO checkIfEmailAvailability(@PathVariable(EMAIL) String email) {
        Boolean isAvailable = !userService.userExistsByEmail(email);
        return new AvailableDTO(isAvailable);
    }

    @GetMapping(ID_PATH)
    public UserDTO getUser(@PathVariable(ID) Long id) {
        User user = userService.getExistingUser(id);
        return userService.mapToUserDTO(user);
    }

}
