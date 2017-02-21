
About the Dockerfile
--------------------

The included [Dockerfile](Dockerfile) is to run an insecure Postgres instance which will reside behind an SSL proxy.

The [Dockerfile](Dockerfile) is in the root directory.

The following environment variables need to be overridden.

Environment variables to override (with sample values):
+ JDBC\_URL='jdbc:postgresql://192.168.1.113/odk_db?autoDeserialize=true'
+ JDBC\_USERNAME='odk_db'
+ JDBC\_PASSWORD='odk_db'
+ JDBC\_SCHEMA='odk_db'

A database must be created matching the above environment variables.  An SQL script that can be used to create this database is here:

[aggregate-postgres/src/main/resources/create_database.sql](aggregate-postgres/src/main/resources/create_database.sql)

Make sure that the environment variable values match those in the SQL file.

The Tomcat instance uses the default ports of 8080 and 8443.

Build the [Dockerfile](Dockerfile) in the root directory with tag pstop
```shell
docker build -t pstop .
```
Install and start Docker container with:
+ Name ```odk_aggregate```.
+ From image ```pstop```.
+ Forwarding the Tomcat ports
+ Overriding the JDBC environment variables.

```shell
docker run -d -i -t --name odk_aggregate -p 8080:8080 -p 8443:8443 -e "JDBC_URL=jdbc:postgresql://192.168.1.113/odk_unit?autoDeserialize=true" -e "JDBC_USERNAME=odk_unit" -e "JDBC_PASSWORD=odk_unit" -e "JDBC_SCHEMA=odk_unit" pstop
```


