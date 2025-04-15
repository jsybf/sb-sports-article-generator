pick_generator_version := "0.0.1"
download_server_version := "0.0.1"

pick-generator-docker-build:
    docker build -t pick-generator:{{ pick_generator_version }} -f ./docker/pick-generator/Dockerfile .

download-server-docker-build:
    docker build -t download-server:{{ download_server_version }} -f ./docker/download-server/Dockerfile .
