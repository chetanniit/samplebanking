diskquota=1024
healthcheckendpoint=/actuator/health
instances=1
memory=1024

SPRING_PROFILES_ACTIVE=awsdev
JAVA_OPTS=-Duser.timezone=GMT


kubectl create configmap dev-env-vars --from-env-file=env-dev.properties
