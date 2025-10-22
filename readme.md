ASE_SMH_engR

**_Maxim - notes:_**

make sure to have java 17, maven 3.8+ and git:
```
java -version
mvn -version
git --version
```
Set up!!!
1. **Clone the repository:**

````
git clone <your-git-repo-url>
cd ASE_SMH_engR
````

2. **Build the project using Maven**
```
./mvnw clean install
```

3. **Run the service locally, default is localhost 80 80**

```
/mvnw spring-boot:run
```

Auth endpoints

```

curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

``` 

should return 

```ey_____ some jwt token```


incorrect log in:

```
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "wrongpass"
  }
```

response should be "Invalid username or password
"

for next people using JWT_TOKEN as the auth for protected endpoints

```

curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer <JWT_TOKEN>"```
```

Testing run: ```./mvnw test```

Style check: ```./mvnw checkstyle:check```