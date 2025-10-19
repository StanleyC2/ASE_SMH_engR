must use 

Java 17
Maven 3.9+

run these

export JAVA_HOME=$(/usr/libexec/java_home -v 17)

build:


mvn clean compile

run:
mvn spring-boot:run


seeded DB

Default Users

The database is seeded with two users (only if empty):

Username	Password	Role
admin	admin123	ROLE_ADMIN
user	user123	ROLE_USER


Endpoints

POST /auth/register — Register a new user
Request body (JSON):

{
"username": "example",
"password": "examplePass",
"email": "example@email.com",
"role": "ROLE_USER"
}


POST /auth/login — Login and receive JWT token
Request body (JSON):

{
"username": "example",
"password": "examplePass"
}

Notes

Passwords are hashed before storage.
H2 console available at /h2-console (if enabled).
JWT tokens required for future secured endpoints.