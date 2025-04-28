# UserSecurity Project

## Overview
UserSecurity is a Spring Boot project that implements user authentication and authorization using JWT (JSON Web Tokens). It utilizes PostgreSQL for data persistence and Spring Security for robust security features.

## Key Technologies
- Java 17
- Spring Boot 3.4.4
- Spring Security
- PostgreSQL Database
- JWT Authentication
- JPA/Hibernate
- MapStruct
- Lombok
- Maven

## Getting Started
### Prerequisites
- Java 17 or higher
- Maven 3.6+ 
- PostgreSQL Database

### Setup
1. Clone the repository:
```bash
git clone [repository-url]
cd UserSecurity
```

2. Configure database properties in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

## Project Structure
```
src
├── main
│   ├── java
│   │   └── za.ac.eyetv
│   │       ├── config        # Configuration files
│   │       ├── controllers   # REST endpoints
│   │       ├── models       # Data models/entities
│   │       ├── repositories # Database repositories
│   │       ├── security    # Security configurations
│   │       └── services    # Business logic
│   └── resources
│       └── application.properties
└── test
    └── java                # Test files
```

## Key Features
- JWT-based Authentication
- Role-based Access Control
- Secure Password Hashing
- User Management
- API Security
- Spring Boot Actuator Integration
- Comprehensive Error Handling

## API Endpoints
### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh token

### User Management
- `GET /api/users` - Get all users (Admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

## Security Features
- JWT token-based authentication
- Password encryption using BCrypt
- Role-based authorization
- Secure endpoint protection
- Token validation and refresh mechanisms
- CORS configuration
- XSS protection

## Testing
Run tests using:
```bash
mvn test
```

## Deployment
1. Build the JAR file:
```bash
mvn clean package
```

2. Run the application:
```bash
java -jar target/UserSecurity-0.0.1-SNAPSHOT.jar
```

## Environment Variables
The following environment variables can be configured:
- `SPRING_DATASOURCE_URL` - Database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - Secret key for JWT token generation
- `JWT_EXPIRATION` - Token expiration time in milliseconds

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Best Practices
- Always use HTTPS in production
- Regularly rotate JWT secrets
- Implement proper input validation
- Use environment variables for sensitive data
- Follow API versioning conventions
- Maintain comprehensive test coverage

## License
[License Type] - See LICENSE file for details

## Contact
For support or queries, please contact:
[Your Contact Information]

## Frontend Integration Guide

### API Base URL
- Development: `http://localhost:8080`
- Production: `https://your-api-domain.com`

### Authentication Flow
1. **User Registration**:
```javascript
// POST /api/auth/register
const response = await fetch('/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'user@example.com',
    password: 'password123',
    firstName: 'John',
    lastName: 'Doe'
  })
});
```

2. **User Login**:
```javascript
// POST /api/auth/login
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'user@example.com',
    password: 'password123'
  })
});

// Response contains JWT token
const { token } = await response.json();
```

3. **Making Authenticated Requests**:
```javascript
// Example of authenticated request
const response = await fetch('/api/users', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

4. **Token Refresh**:
```javascript
// POST /api/auth/refresh
const response = await fetch('/api/auth/refresh', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${refreshToken}`,
    'Content-Type': 'application/json'
  }
});

// Response contains new access token
const { token: newToken } = await response.json();
```

### Error Handling
The API returns standard HTTP status codes:
- 200: Success
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

Error response format:
```json
{
  "status": 400,
  "message": "Error message here",
  "timestamp": "2024-03-14T12:00:00Z",
  "path": "/api/endpoint"
}
```

### CORS Configuration
The backend is configured to accept requests from:
- `http://localhost:3000` (Development)
- `https://your-frontend-domain.com` (Production)

To configure additional domains, modify the CORS settings in the backend.

### Security Best Practices
1. Store JWT tokens securely:
   - Use HttpOnly cookies for web applications
   - Use secure storage for mobile applications

2. Implement token refresh logic:
   - Refresh token before expiration
   - Handle token expiration gracefully

3. Protect against common vulnerabilities:
   - Implement CSRF protection
   - Use HTTPS in production
   - Sanitize user input
   - Implement rate limiting

4. Error handling:
   - Never expose sensitive information in error messages
   - Implement proper logging
   - Handle network errors gracefully

### Example Integration (React)
```javascript
// authService.js
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const login = async (username, password) => {
  try {
    const response = await fetch(`${API_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw new Error('Login failed');
    }
    
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const getAuthHeader = () => ({
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});
```

### Rate Limiting
The API implements rate limiting:
- 100 requests per minute for authenticated users
- 20 requests per minute for unauthenticated users

### API Documentation
For detailed API documentation, visit:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs` 