FROM 814633283276.dkr.ecr.us-east-1.amazonaws.com/tomcat:8-jdk8

# Control Java heap and metaspace sizes
ENv MIN_HEAP 256m
ENV MAX_HEAP 1024m
ENV MAX_METASPACE 128m

# This Dockerfile runs an insecure instance of ODK aggregate on the default Tomcat ports
# It is intended to be installed behind an SSL proxy

MAINTAINER Benetech <cadenh@benetech.org>

# Override the JDBC variables from the command line like
# docker run -d -i -t --name odk_aggregate -p 8080:8080 -p 8443:8443 \
# -e "JDBC_URL=jdbc:postgresql://192.168.1.113/odk_unit?autoDeserialize=true" \
# -e "JDBC_USERNAME=odk_unit" -e "JDBC_PASSWORD=odk_unit" -e "JDBC_SCHEMA=odk_unit" pstop
ENV JDBC_URL='jdbc:postgresql://192.168.1.113/odk_db?autoDeserialize=true' \
    JDBC_USERNAME='odk_db' \
    JDBC_PASSWORD='odk_db' \
    JDBC_SCHEMA='odk_db' \
    ROOT_WAR_PATH='/usr/local/tomcat/webapps/ROOT.war'

WORKDIR /usr/local

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY ./aggregate-postgres/target/aggregate-postgres-*.war $ROOT_WAR_PATH

EXPOSE 8080
EXPOSE 8443


