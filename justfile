pick_generator_version := "0.0.1"
download_server_version := "0.0.1"

# ## recipes related to local build,run,test
compile-pick-generator:
    mvn -T 1C package -pl ./pick-generator/cli -am -DskipTests

local-pick-generator-run jar_params claude_api_key:
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
pick_generator_ecr_uri := "384052067743.dkr.ecr.ap-northeast-2.amazonaws.com"

push-pick-generator: build-pick-generator
    aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin {{ pick_generator_ecr_uri }}
    docker tag sb-pick/pick-generator:{{ pick_generator_version }} {{ pick_generator_ecr_uri }}/sb-pick/pick-generator:{{ pick_generator_version }}
    docker tag sb-pick/pick-generator:{{ pick_generator_version }} {{ pick_generator_ecr_uri }}/sb-pick/pick-generator:latest
    docker push  {{ pick_generator_ecr_uri }}/sb-pick/pick-generator:{{ pick_generator_version }}
    docker push  {{ pick_generator_ecr_uri }}/sb-pick/pick-generator:latest

build-pick-generator:
    docker build -t sb-pick/pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

### recipes related to ecs-run

#parma should be form like '"--include", "hockey.*"'
aws-run-pick-generator param db_pw claude_api_key db_host="54.180.248.188" db_port="3306" db_user="root":
    aws ecs run-task \
        --no-pager \
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
                            {"name": "SB_PICK_MYSQL_HOST", "value": "{{ db_host }}"}, \
                            {"name": "SB_PICK_MYSQL_PORT", "value": "{{ db_port }}"}, \
                            {"name": "SB_PICK_MYSQL_USER", "value": "{{ db_user }}"}, \
                            {"name": "SB_PICK_MYSQL_PW", "value": "{{ db_pw }}"}, \
                            {"name": "SB_PICK_MYSQL_DB", "value": "sb-pick"}, \
                            {"name": "SB_PICK_CLAUDE_API_KEY", "value": "{{ claude_api_key }}"} \
                        ], \
                        "command": ["java", "-jar", "cli.jar", {{ param }}] \
                    } \
                ] \
            } \
        '

### deprecated recipes

download-server-docker-build:
    docker build -t sb-pick/download-server:{{ download_server_version }} -f ./docker/download-server/Dockerfile .

pick-server-ec2-up stack_name="pick-download-server":
    #!/usr/bin/env sh
        set -e
        set -x
        aws cloudformation create-stack \
            --no-cli-pager \
            --stack-name {{ stack_name }} \
            --template-body file://aws/ec2-cfn.yml \
            --capabilities CAPABILITY_IAM \
            --parameters '[
            {"ParameterKey": "Ec2InstanceType", "ParameterValue":"t4g.small" },
            {"ParameterKey": "Ec2Name", "ParameterValue":"pick-download-server" },
            {"ParameterKey": "EbsVolumeSize", "ParameterValue":"50" }
        ]'
