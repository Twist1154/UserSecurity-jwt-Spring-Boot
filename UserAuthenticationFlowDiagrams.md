# User Authentication Flow Diagrams

This document provides visual representations of the authentication flows in the application.

## Registration Flow Diagram

```
┌─────────┐                  ┌───────────────────────┐                  ┌─────────────────────────┐                  ┌────────────┐
│  Client │                  │ AuthenticationController │                  │ AuthenticationService  │                  │ Database   │
└────┬────┘                  └─────────────┬─────────┘                  └────────────┬────────────┘                  └─────┬──────┘
     │                                     │                                          │                                     │
     │  POST /api/auth/register            │                                          │                                     │
     │  {username, email, password}        │                                          │                                     │
     │────────────────────────────────────>│                                          │                                     │
     │                                     │                                          │                                     │
     │                                     │  register(RegisterRequest)               │                                     │
     │                                     │─────────────────────────────────────────>│                                     │
     │                                     │                                          │                                     │
     │                                     │                                          │  Check if email exists              │
     │                                     │                                          │─────────────────────────────────────>
     │                                     │                                          │                                     │
     │                                     │                                          │  Check if username exists           │
     │                                     │                                          │─────────────────────────────────────>
     │                                     │                                          │                                     │
     │                                     │                                          │  Create User entity                 │
     │                                     │                                          │  Encode password                    │
     │                                     │                                          │                                     │
     │                                     │                                          │  Save user                          │
     │                                     │                                          │─────────────────────────────────────>
     │                                     │                                          │                                     │
     │                                     │                                          │  Generate JWT access token          │
     │                                     │                                          │  Generate JWT refresh token         │
     │                                     │                                          │                                     │
     │                                     │  AuthenticationResponse                  │                                     │
     │                                     │<─────────────────────────────────────────│                                     │
     │                                     │                                          │                                     │
     │  ResponseEntity<AuthenticationResponse>                                        │                                     │
     │<────────────────────────────────────│                                          │                                     │
     │                                     │                                          │                                     │
```

## Login Flow Diagram

```
┌─────────┐                  ┌───────────────────────┐                  ┌─────────────────────────┐                  ┌────────────┐
│  Client │                  │ AuthenticationController │                  │ AuthenticationService  │                  │ Database   │
└────┬────┘                  └─────────────┬─────────┘                  └────────────┬────────────┘                  └─────┬──────┘
     │                                     │                                          │                                     │
     │  POST /api/auth/login               │                                          │                                     │
     │  {email, password}                  │                                          │                                     │
     │────────────────────────────────────>│                                          │                                     │
     │                                     │                                          │                                     │
     │                                     │  login(LoginRequest)                     │                                     │
     │                                     │─────────────────────────────────────────>│                                     │
     │                                     │                                          │                                     │
     │                                     │                                          │  Authenticate credentials           │
     │                                     │                                          │  (AuthenticationManager)            │
     │                                     │                                          │                                     │
     │                                     │                                          │  Load user by email                 │
     │                                     │                                          │─────────────────────────────────────>
     │                                     │                                          │                                     │
     │                                     │                                          │  Verify password                    │
     │                                     │                                          │                                     │
     │                                     │                                          │  Generate JWT access token          │
     │                                     │                                          │  Generate JWT refresh token         │
     │                                     │                                          │                                     │
     │                                     │  AuthenticationResponse                  │                                     │
     │                                     │<─────────────────────────────────────────│                                     │
     │                                     │                                          │                                     │
     │  ResponseEntity<AuthenticationResponse>                                        │                                     │
     │<────────────────────────────────────│                                          │                                     │
     │                                     │                                          │                                     │
```

## Logout Flow Diagram

```
┌─────────┐                  ┌───────────────────────┐                  ┌─────────────────────┐                  ┌────────────┐
│  Client │                  │ AuthenticationController │                  │ LogoutService      │                  │ Database   │
└────┬────┘                  └─────────────┬─────────┘                  └─────────┬───────────┘                  └─────┬──────┘
     │                                     │                                      │                                     │
     │  POST /api/auth/logout              │                                      │                                     │
     │  {token, refreshToken}              │                                      │                                     │
     │────────────────────────────────────>│                                      │                                     │
     │                                     │                                      │                                     │
     │                                     │  logout(LogoutRequest)               │                                     │
     │                                     │─────────────────────────────────────>│                                     │
     │                                     │                                      │                                     │
     │                                     │                                      │  Validate tokens                    │
     │                                     │                                      │  Normalize tokens                   │
     │                                     │                                      │                                     │
     │                                     │                                      │  Blacklist access token             │
     │                                     │                                      │─────────────────────────────────────>
     │                                     │                                      │                                     │
     │                                     │                                      │  Blacklist refresh token            │
     │                                     │                                      │─────────────────────────────────────>
     │                                     │                                      │                                     │
     │                                     │  LogoutResponse                      │                                     │
     │                                     │<─────────────────────────────────────│                                     │
     │                                     │                                      │                                     │
     │  ResponseEntity<LogoutResponse>     │                                      │                                     │
     │<────────────────────────────────────│                                      │                                     │
     │                                     │                                      │                                     │
```

## Authentication for Subsequent Requests

```
┌─────────┐                  ┌───────────────────────┐                  ┌─────────────────────┐                  ┌────────────┐
│  Client │                  │ JwtAuthenticationFilter │                  │ JwtService         │                  │ Database   │
└────┬────┘                  └─────────────┬─────────┘                  └─────────┬───────────┘                  └─────┬──────┘
     │                                     │                                      │                                     │
     │  Request with                       │                                      │                                     │
     │  Authorization: Bearer <token>      │                                      │                                     │
     │────────────────────────────────────>│                                      │                                     │
     │                                     │                                      │                                     │
     │                                     │  Extract token                       │                                     │
     │                                     │  from header                         │                                     │
     │                                     │                                      │                                     │
     │                                     │  Extract username                    │                                     │
     │                                     │─────────────────────────────────────>│                                     │
     │                                     │                                      │                                     │
     │                                     │  username                            │                                     │
     │                                     │<─────────────────────────────────────│                                     │
     │                                     │                                      │                                     │
     │                                     │  Load UserDetails                    │                                     │
     │                                     │─────────────────────────────────────────────────────────────────────────────>
     │                                     │                                      │                                     │
     │                                     │  UserDetails                         │                                     │
     │                                     │<─────────────────────────────────────────────────────────────────────────────
     │                                     │                                      │                                     │
     │                                     │  Validate token                      │                                     │
     │                                     │─────────────────────────────────────>│                                     │
     │                                     │                                      │                                     │
     │                                     │  Check if blacklisted                │                                     │
     │                                     │                                      │─────────────────────────────────────>
     │                                     │                                      │                                     │
     │                                     │  isValid result                      │                                     │
     │                                     │<─────────────────────────────────────│                                     │
     │                                     │                                      │                                     │
     │                                     │  Set Authentication in               │                                     │
     │                                     │  SecurityContextHolder               │                                     │
     │                                     │                                      │                                     │
     │  Response                           │                                      │                                     │
     │<────────────────────────────────────│                                      │                                     │
     │                                     │                                      │                                     │
```