FROM tomcat:8-jre8

# Control Java heap and metaspace sizes
ENV MIN_HEAP 256m
ENV MAX_HEAP 1024m
ENV MAX_METASPACE 128m

ENV JAVA_OPTS -server -Xms$MIN_HEAP -Xmx$MAX_HEAP -XX:MaxMetaspaceSize=$MAX_METASPACE -XX:+UseG1GC -Djava.library.path=$CATALINA_HOME/lib:/usr/lib/x86_64-linux-gnu

# This Dockerfile runs an insecure instance of ODK aggregate on the default Tomcat ports
# It is intended to be installed behind an SSL proxy

MAINTAINER Benetech <cadenh@benetech.org>

# Override the JDBC variables from the command line like
# docker run -d -i -t --name odk_aggregate -p 8080:8080 -p 8443:8443 \
# -e "JDBC_URL=jdbc:postgresql://192.168.1.113/odk_unit?autoDeserialize=true" \
# -e "JDBC_USERNAME=odk_unit" -e "JDBC_PASSWORD=odk_unit" -e \
# "JDBC_SCHEMA=odk_unit" -e "EXTERNAL_ROOT_URL=https://odk-qa.benetech.org" pstop

ENV JDBC_URL='jdbc:postgresql://192.168.1.113/odk_db?autoDeserialize=true' \
    JDBC_USERNAME='odk_db' \
    JDBC_PASSWORD='odk_db' \
    JDBC_SCHEMA='odk_db' \
    EXTERNAL_ROOT_URL='https://odk-qa.benetech.org' \
    ROOT_WAR_PATH='/usr/local/tomcat/webapps/ROOT.war' 

WORKDIR /usr/local

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY ./aggregate-postgres/target/aggregate-postgres-*.war $ROOT_WAR_PATH

EXPOSE 8080
EXPOSE 8443


