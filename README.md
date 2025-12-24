# Branch coding exercise from David Benoff

Here is a sample project that demonstrates a REST API which fetches data from Github, transforms it, and returns it to the caller.

It caches each result by username and falls back to cached data if Github is unavailable.

## Comments

* Rather than write my own Github client code, I used a popular and well supported library to abstract this away.  https://hub4j.github.io/github-api/
* One downside of using this library is the two calls to Github cannot be parallelized.  But the reduced boilerplate more than outweighs a trivial performance gain IMHO.
* The integration tests use Wiremock to simulate responses from Github, ensuring we are testing the entire flow, including the Github client library.
* JSON payloads for the tests are stored in external JSON files.  The tests can be easily updated by revving the client library and updating the payloads.
* Uses record class for immutable domain objects.

## Build and run instructions

1. Standard maven build - mvn clean install
2. java -jar .\target\branch-demo-1.0-SNAPSHOT.jar
3. curl http://localhost:8080/users/octocat   