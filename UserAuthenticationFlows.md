# User Authentication Flows

This document describes the data flow for user authentication operations in the application: registration, login, and logout.

## Registration Flow

1. **Client Request**:
   - Client sends a POST request to `/api/auth/register` with a RegisterRequest object containing:
     - username
     - email
     - password

2. **Controller Layer**:
   - `AuthenticationController.register()` receives the request
   - Logs the registration attempt
   - Validates the request data using Jakarta validation annotations
   - Passes the RegisterRequest to the AuthenticationService

3. **Service Layer**:
   - `AuthenticationService.register()` processes the request:
     - Checks if email already exists in the database
     - Checks if username already exists in the database
     - If either exists, throws appropriate exception (EmailAlreadyExistsException or UsernameAlreadyExistsException)
     - Creates a new User entity with:
       - Username from request
       - Email from request
       - Password encoded using BCryptPasswordEncoder
       - Role set to USER
       - Active status set to true
     - Saves the user to the database via UserRepository
     - Creates extra claims map with user role
     - Generates JWT access token using JwtService
     - Generates JWT refresh token using JwtService
     - Returns AuthenticationResponse with tokens, email, username, and role

4. **Response to Client**:
   - Controller returns ResponseEntity with AuthenticationResponse containing:
     - JWT access token
     - JWT refresh token
     - Email
     - Username
     - Role

## Login Flow

1. **Client Request**:
   - Client sends a POST request to `/api/auth/login` with a LoginRequest object containing:
     - email
     - password

2. **Controller Layer**:
   - `AuthenticationController.login()` receives the request
   - Logs the login attempt
   - Validates the request data
   - Passes the LoginRequest to the AuthenticationService

3. **Service Layer**:
   - `AuthenticationService.login()` processes the request:
     - Uses AuthenticationManager to authenticate the user credentials
     - AuthenticationManager delegates to DaoAuthenticationProvider which:
       - Uses UserDetailsService to load the user by email
       - Compares the provided password with the stored hashed password
       - If authentication fails, throws BadCredentialsException (caught and rethrown as PasswordMismatchException)
     - If authentication succeeds, retrieves the user from the database
     - Creates extra claims map with user role
     - Generates JWT access token using JwtService
     - Generates JWT refresh token using JwtService
     - Returns AuthenticationResponse with tokens, email, username, and role

4. **Response to Client**:
   - Controller returns ResponseEntity with AuthenticationResponse containing:
     - JWT access token
     - JWT refresh token
     - Email
     - Username
     - Role

## Logout Flow

1. **Client Request**:
   - Client sends a POST request to `/api/auth/logout` with a LogoutRequest object containing:
     - token (JWT access token)
     - refreshToken (JWT refresh token)

2. **Controller Layer**:
   - `AuthenticationController.logout()` receives the request
   - Logs the logout attempt
   - Validates the request data
   - Passes the tokens to the LogoutService

3. **Service Layer**:
   - `LogoutService.logout()` processes the request:
     - Validates that both tokens are provided
     - Normalizes the tokens (removes "Bearer " prefix if present)
     - Calls JwtService to blacklist the access token
     - Calls JwtService to blacklist the refresh token
     - JwtService extracts claims from the tokens and saves them to the blacklist database
     - Returns LogoutResponse with success message

4. **Response to Client**:
   - Controller returns ResponseEntity with LogoutResponse containing:
     - Success message

## Authentication for Subsequent Requests

After login, the client includes the JWT access token in the Authorization header of subsequent requests:

1. **Request Processing**:
   - JwtAuthenticationFilter intercepts the request
   - Extracts the JWT token from the Authorization header
   - Extracts the user email from the token using JwtService
   - Loads the user details using UserDetailsService
   - Validates the token using JwtService (checks signature, expiration, and blacklist)
   - If valid, creates an authentication token and sets it in the SecurityContextHolder
   - This authenticates the user for the current request

2. **Token Refresh**:
   - When the access token expires, the client can send a refresh request to `/api/auth/refresh`
   - The server validates the refresh token and issues a new access token
   - If the refresh token is invalid or expired, the user must log in again

## Security Measures

- Passwords are encoded using BCryptPasswordEncoder
- JWT tokens are signed with HMAC-SHA256
- Access tokens have a shorter expiration time than refresh tokens
- Tokens are blacklisted on logout to prevent reuse
- Expired blacklisted tokens are cleaned up periodically
- Rate limiting is implemented to prevent brute force attacks