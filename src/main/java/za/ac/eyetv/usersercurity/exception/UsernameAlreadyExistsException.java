package za.ac.eyetv.usersercurity.exception;

import lombok.Getter;

@Getter
public class UsernameAlreadyExistsException extends RuntimeException {
  private final String username;

  public UsernameAlreadyExistsException(String username) {
    super("Username '" + username + "' is already taken.");
    this.username = username;
  }

  public UsernameAlreadyExistsException(String username, Throwable cause) {
    super("Username '" + username + "' is already taken.", cause);
    this.username = username;
  }

}
