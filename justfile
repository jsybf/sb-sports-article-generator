pick_generator_version := "0.0.1"
download_server_version := "0.0.1"

pick-generator-docker-build:
    docker build -t pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

download-server-docker-build:
    docker build -t download-server:{{ download_server_version }} -f ./docker/download-server/Dockerfile .

cli-package:
    mvn -T 1C package -pl ./pick-generator/cli -am -DskipTests

local-mysql-up:
    docker run -d --rm \
      --name mysql \
      -e MYSQL_DATABASE=test_db \
      -e MYSQL_ROOT_PASSWORD=root_pass \
      -e TZ=Asia/Seoul \
      -p 3306:3306 \
      mysql:8.4
    @echo "sleep 5sec. waiting for mysql to be ready for connection"
    sleep 5
    mysql --host 127.0.0.1 --user=root --password=root_pass test_db < ./ddl/ddl.sql

local-mysql-down:
    docker stop mysql

pick-server-ec2-up stack_name="pick-download-server":
    #!/usr/bin/env sh
        set -e
        set -x
        aws cloudformation create-stack \
            --no-cli-pager \
            --stack-name {{stack_name}} \
            --template-body file://aws/ec2-cfn.yml \
            --capabilities CAPABILITY_IAM \
            --parameters '[
            {"ParameterKey": "Ec2InstanceType", "ParameterValue":"t4g.small" },
            {"ParameterKey": "Ec2Name", "ParameterValue":"pick-download-server" },
            {"ParameterKey": "EbsVolumeSize", "ParameterValue":"50" }
        ]'
