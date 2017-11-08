# oauth2-github-demo

Simple demo of a Spring app which uses GitHub OAuth2 for authentication &amp; authorization.

Please review the `pom.xml` file, which includes all of the necessary Maven dependencies (there are several) for this project. Also note that the code assumes that there is an `application.yaml` file, which should generally be on the classpath, or in a `config/` directory which is itself on the classpath. (In IntelliJ IDEA, this file will generally reside in the `src/main/resources/` directory, and will be moved into the `target/classes/` directory by the build process.) This file is _not_ committed to the repository, since it contains the GitHub OAuth2 client secret for the application.
