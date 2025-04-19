pick_generator_version := "0.0.1"
download_server_version := "0.0.1"

pick-generator-docker-build:
    docker build -t pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

download-server-docker-build:
    docker build -t download-server:{{ download_server_version }} -f ./docker/download-server/Dockerfile .

cli-package:
    mvn -T 1C package -pl ./llm-article-writer/cli -am -DskipTests

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
