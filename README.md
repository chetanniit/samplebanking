diskquota=1024
healthcheckendpoint=/actuator/health
instances=1
memory=1024

SPRING_PROFILES_ACTIVE=awsdev
JAVA_OPTS=-Duser.timezone=GMT


kubectl create configmap dev-env-vars --from-env-file=env-dev.properties

...
  spec:
    containers:
    - name: xxx
      envFrom:
      - configMapRef:
        name: dev-env-vars



        The directory contains Spring config data files, which are used to define non-secret
application properties in the Spring application context.

To be tested: yaml files.
All the files' will be mounted in a /config volume inside the container, which is the default location for
"Application properties outside of your packaged jar" - see Spring Externalized Configuration

Due to the ordering of Spring property sources (see Spring Externalized Configuration),
these properties will override any defined in resource files inside the jar.
spring.profiles.active will be configured, through an environment variable in the appropriate Env-ConfigMaps file,
so that only the appropriate config data file(s) will be used, e.g.
env-dev.ini defines SPRING_PROFILES_ACTIVE=awsdev

