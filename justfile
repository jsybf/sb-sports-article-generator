pick_generator_version := "0.0.1"
download_server_version := "0.0.1"


### recipes related to local build,run,test
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


pick-generator-docker-build:
    docker build -t gitp/pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

download-server-docker-build:
    docker build -t gitp/download-server:{{ download_server_version }} -f ./docker/download-server/Dockerfile .

cli-package:
    mvn -T 1C package -pl ./pick-generator/cli -am -DskipTests


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
