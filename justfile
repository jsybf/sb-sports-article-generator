set dotenv-load := true

pick_generator_version := "0.0.2"
download_server_version := "0.0.2"

echo-env:
    @echo "SB_PICK_MYSQL_HOST=${SB_PICK_MYSQL_HOST}"
    @echo "SB_PICK_MYSQL_PORT=${SB_PICK_MYSQL_PORT}"
    @echo "SB_PICK_MYSQL_USER=${SB_PICK_MYSQL_USER}"
    @echo "SB_PICK_MYSQL_PW=${SB_PICK_MYSQL_PW}"
    @echo "SB_PICK_MYSQL_DB=${SB_PICK_MYSQL_DB}"
    @echo "SB_PICK_CLAUDE_API_KEY=${SB_PICK_CLAUDE_API_KEY}"
    @echo "PICK_GENERATOR_ECR_URI=${PICK_GENERATOR_ECR_URI}"

# ## recipes related to local build,run,test
compile-pick-generator:
    mvn -T 1C package -pl ./pick-generator/cli -am -DskipTests

local-run-pick-generator jar_params claude_api_key:
    SB_PICK_MYSQL_HOST='127.0.0.1' \
    SB_PICK_MYSQL_PORT='3306' \
    SB_PICK_MYSQL_USER='root' \
    SB_PICK_MYSQL_PW='rootpass' \
    SB_PICK_MYSQL_DB='dev' \
    SB_PICK_CLAUDE_API_KEY={{ claude_api_key }} \
    java -jar ./pick-generator/cli/target/cli-fatjar-{{ pick_generator_version }}.jar {{ jar_params }}

local-mysql-up:
    docker run -d --rm \
      --name mysql \
      -e MYSQL_DATABASE=dev \
      -e MYSQL_ROOT_PASSWORD=rootpass \
      -e TZ=Asia/Seoul \
      -p 3306:3306 \
      mysql:8.4
    @echo "sleep 10sec. waiting for mysql to be ready for connection"
    sleep 10
    mysql --host 127.0.0.1 --user=root --password=rootpass dev < ./ddl/ddl.sql

local-mysql-down:
    docker stop mysql

# ## recipes related to cloudformation deploy
aws-deploy-pick-generator-ecr:
    aws cloudformation deploy \
        --stack-name sb-pick---pick-generator-ecr \
        --template-file ./aws/pick-generator-ecr-cfn.yml

aws-deploy-ecs-cluster:
    aws cloudformation deploy \
        --stack-name sb-pick---ecs-cluster \
        --template-file ./aws/ecs-cluster-cfn.yml

aws-deploy-pick-generator-taskdef:
    aws cloudformation deploy \
        --stack-name sb-pick---pick-generator-taskdef \
        --template-file ./aws/pick-generator-taskdef-cfn.yml \
        --capabilities CAPABILITY_IAM

### recipes related to docker

push-pick-generator: build-pick-generator
    aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin ${PICK_GENERATOR_ECR_URI}
    docker tag sb-pick/pick-generator:{{ pick_generator_version }} ${PICK_GENERATOR_ECR_URI}/sb-pick/pick-generator:{{ pick_generator_version }}
    docker tag sb-pick/pick-generator:{{ pick_generator_version }} ${PICK_GENERATOR_ECR_URI}/sb-pick/pick-generator:latest
    docker push  ${PICK_GENERATOR_ECR_URI}/sb-pick/pick-generator:{{ pick_generator_version }}
    docker push  ${PICK_GENERATOR_ECR_URI}/sb-pick/pick-generator:latest

build-pick-generator:
    docker buildx build --platform linux/arm64 -t sb-pick/pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

build-download-server:
    docker buildx build --platform linux/arm64 -t gitp/download-server:{{ pick_generator_version }} -f ./docker/download-server/Dockerfile .

push-download-server:  build-download-server
    docker push gitp/download-server:{{ pick_generator_version }}

### recipes related to ecs-run

# parma should be form like '"--include", "hockey.*"'
aws-run-pick-generator param:
    aws ecs run-task \
        --no-cli-pager \
        --cluster sb-pick-cluster \
        --task-definition sb-pick---pick-generator \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[subnet-01d0a43fcbda4da53],assignPublicIp=ENABLED}" \
        --overrides ' \
            { \
                "containerOverrides": [ \
                    { \
                        "name": "batch-job", \
                        "environment": [ \
                            {"name": "SB_PICK_MYSQL_HOST", "value": "'"${SB_PICK_MYSQL_HOST}"'"}, \
                            {"name": "SB_PICK_MYSQL_PORT", "value": "'"${SB_PICK_MYSQL_PORT}"'"}, \
                            {"name": "SB_PICK_MYSQL_USER", "value": "'"${SB_PICK_MYSQL_USER}"'"}, \
                            {"name": "SB_PICK_MYSQL_PW", "value": "'"${SB_PICK_MYSQL_PW}"'"}, \
                            {"name": "SB_PICK_MYSQL_DB", "value": "'"${SB_PICK_MYSQL_DB}"'"}, \
                            {"name": "SB_PICK_CLAUDE_API_KEY", "value": "'"${SB_PICK_CLAUDE_API_KEY}"'"} \
                        ], \
                        "command": ["java", "-jar", "cli.jar", {{ param }}] \
                    } \
                ] \
            } \
        '
