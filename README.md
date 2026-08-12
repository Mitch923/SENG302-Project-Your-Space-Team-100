>[!NOTE]
> For security reasons this is a shallow clone of the original repo.

# SENG302 Project - Your Space - Team 100

Home renovation project using ```gradle```, ```Spring Boot```, ```Thymeleaf```, and ```GitLab CI```.

## How to run

### 1 - Running the project

From the root directory, run the following command:

On Linux:

```
./gradlew bootRun
```

On Windows:

```
gradlew bootRun
```

By default, the application will run on local port
8080 [http://localhost:8080](http://localhost:8080)

### 2 - Using the application

- Clone the repository to your local machine
- Navigate to the root directory and run the project
- Visit localhost:8080 with your chosen browser, this will take you to the home page
- Click My Renovations to see your renovation records
- Click "Create new renovation record" to create a new record
- Click on your renovation record to view it's details and edit it
- You can click "Home" at any time in the navigation bar to return to the home page

## How to run tests

From the root directory, run the following command:

```
./gradlew test
```

## Contributors

- Hannah B.
- Tannin H.
- Finley K.
- Penelope S.
- William T.
- Theo P.
- Mitchell Perrin

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=17797&section=8)

## Information about included build files/scripts

Whilst these scripts and files will not be of use until later in the course when you set up
continuous integration (CI) we have included files with default behaviour, and some for reference.

- `deployment-files-fyi/nginx/sites-available.conf`
    - Reference file showing the VMs NGINX config.
- `deployment-files-fyi/systemd-service/production.service`
    - Reference file showing the production environment service
      configuration. [See here for more information about .service files](https://www.shellhacks.com/systemd-service-file-example/)
- `deployment-files-fyi/systemd-service/staging.service`
    - Reference file showing the staging environment service configuration.
- `runner-scripts/production.sh`
    - Deployment shell script for running the production environment on a VM.
- `runner-scripts/staging.sh`
    - Deployment shell script for running the staging environment on a VM.
- `.gitlab-ci.yml`
    - The (GitLab specific) CI script for the project. This will cause your pipelines to
      fail/timeout until gitlab-runners are set up so feel free to ignore these for
      now. [For more information refer to GitLab's Documentation Here](https://docs.gitlab.com/ee/ci/yaml/gitlab_ci_yaml.html)

## How to obtain API key for Perspective API (for profanity moderation)

- go to [https://console.cloud.google.com/](https://console.cloud.google.com/)
- Log in as the home helper@gmail account found on the wiki
- Go to credentials and copy the perspective API key
- Set as an environment variable PERSPECTIVE_API_KEY in either `env.properties` or as a global env
  variable
- Env variable should be picked up by `application.properties` and functionality should work

## Third-Party Licenses

### Spring Framework

Copyright © 2002-2025 VMware, Inc. <br>
Licensed under the Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0) <br>
Documentation: https://docs.spring.io/spring-framework/reference/index.html

### three.js

Copyright © 2010-2025 three.js authors <br>
Licensed under the MIT License (https://github.com/mrdoob/three.js/blob/dev/LICENSE) <br>
Documentation: https://threejs.org/docs/

### Perspective API

Copyright All rights reserved © Google2025 <br>
Licensed under the Apache License
2.0 (https://github.com/conversationai/perspectiveapi/blob/main/LICENSE) <br>
Documentation: https://developers.perspectiveapi.com/s/docs?language=en_US

### Mapbox API

Copyright All Rights Reserved © Mapbox <br>
Licensed under Proprietary License (https://www.mapbox.com/legal/tos?utm_source=chatgpt.com) <br>
Documentation: https://docs.mapbox.com/api/search/geocoding/

## CC Attributions

### GLTF Models

"Garfield" (https://skfb.ly/6RooI) by GarfDaddy is licensed under Creative Commons
Attribution (http://creativecommons.org/licenses/by/4.0/).

"chicken joe" (https://skfb.ly/p9Rn8) by AidanYT55Twt is licensed under Creative Commons
Attribution (http://creativecommons.org/licenses/by/4.0/).
