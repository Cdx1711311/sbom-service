FROM openeuler/openeuler:22.03-lts@sha256:a96e504086acb8cd22d551d28252658e4440a4dae4bdecb3fed524deeb74ea75 AS build

RUN yum update -y && yum install -y \
    git \
    java-17-openjdk \
    python3-pip \
    && rm -rf /var/cache/yum \
    && pip3 install virtualenv

WORKDIR /opt
RUN git clone --recurse-submodules https://ghproxy.com/https://github.com/opensourceways/sbom-service.git
WORKDIR /opt/sbom-service
RUN /bin/bash gradlew bootWar

ENTRYPOINT ["/bin/bash", "/opt/sbom-service/docker-entrypoint.sh"]
