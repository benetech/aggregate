Configure and Build ODK Aggregate WAR File
=================================

Prerequisites
-------------
It is assumed you have working knowledge of Git, Maven, Tomcat, and your database of choice.

Recommended:
- One of the following databases:
  - Postgres 9.4 or above
  - MySQL 5 or above with MySQL Connector 5.1.41 or above
- Maven 3.3.3 or above
- Tomcat 8 or above
- Java 8

First, install your database
-----------------------------

Install Postgres 9.4 or above or MySQL 5 or above.

You can use an existing installation if you have one.

Second, configure Maven
------------------------

If you haven't installed Maven, install Maven.  This project has been tested with Maven 3.3.9 on Ubuntu 16.04, but it should also work on Windows and Mac environments with Maven.  This document will use Linux conventions for file separators, home directory, environment variables, etc. until there is a demand to add alternative instructions.

Edit your ~/.m2/settings.xml file, and add the following to your ```<profiles>``` section.  [Maven settings.xml reference](https://maven.apache.org/settings.html)

```xml
<profile>
  <id>odk</id>
  <activation><activeByDefault>true</activeByDefault></activation>
  <properties>
    <mysql.client.executable>/usr/bin/mysql</mysql.client.executable>
    <mysql.root.password>password</mysql.root.password>
    <postgres.client.executable>/usr/bin/psql</postgres.client.executable>
    <postgres.root.password>password</postgres.root.password>
    <test.server.hostname>localhost</test.server.hostname>
    <test.server.port>7070</test.server.port>
    <test.server.secure.port>7443</test.server.secure.port>
    <test.server.gae.monitor.port>7075</test.server.gae.monitor.port>
  </properties>
</profile>
```

You can add just the two Postgres lines or the two MySQL lines if you will only be working with one database.  Update the database information as follows:
- ```postgres.root.password```: password for Postgres user "postgres"
- ```postgres.client.executable```: path to Postgres client
- ```mysql.root.password```: password for MySQL user "root"
- ```mysql.client.executable```: path to MySQL client

Third, check out source code
-----------------------------
Clone the project from git@github.com:benetech/aggregate.git.

Fourth, compile the source code
--------------------------------
In the root directory of the your newly acquired source code type:
```shell
mvn install
```
This should do the following:
- Build the submodules
- Create a unit test database called odk_unit
- Run unit tests against the odk_unit database
- Create a war file in ```aggregate/aggregate-postgres/target/aggregate-postgres-1.0.1.war```

By default, the war will be for a Postgres installation.  In order to activate the Maven build profile for a MySQL installation instead, set the environment variable ```DB_PROFILE``` to ```mysql``` like this:
```shell
export DB_PROFILE=mysql
```
and run the ```mvn install``` command.  If you do this, a war file will be created in ```aggregate/aggregate-mysql/target/aggregate-mysql-1.0.1.war```

[What is this local repository?](FAQ.md#what-is-this-local-repository)

Fifth, create a new database
------------------------------------------
The jdbc.properties files in the ```odk-[DATABASE]-*.jar``` files are where the rest of the code gets the database parameters.  The ```odk-[DATABASE]-settings``` module jars become part of the war files, and the ```odk-[DATABASE]-unit-settings``` module jars are used for unit testing.  Currently, all of the parameters are set to ```odk_unit```, for the username, password, database name, and schema name.  This means that if you don't mind the unit tests clobbering your data, you can create an empty database with the unit test setup script in ```odk-[DATABASE]-unit-settings/src/main/resources/sql-scripts/[DATABASE]_setup.sql```, drop your war into your tomcat installation, start it up, and poke around.

However, you will at some point want to use a different database that isn't dropped and recreated by the  unit tests, so use the ```odk-[DATABASE]-unit-settings/src/main/resources/sql-scripts/[DATABASE]_setup.sql``` file as a model to create a new database, and run those commands in your database client.

The database will be empty.  When you start ODK aggregate, it will create tables as it needs them.

Sixth, configure your war with new database parameters.
-------------------------------------------------------
Then plug the same values that you used to create the database into ```odk-[DATABASE]-settings/src/main/resources/jdbc.properties```

Alternatively, you could skip updating the ```jdbc.properties``` file and override those values with environment variables.  Just make sure that those environment variables are available wherever you start Tomcat.  You can override any of the jdbc.properties values by converting it to uppercase and replacing the . with a _.  (Adopting this convention was necessary because bash shell disallows environment variables which contain periods!)

For example, to override the values in ```jdbc.properties``` for my integration test database, I used the following commands in the shell.

```shell
export JDBC_URL=jdbc:postgresql://127.0.0.1/odk_int?autoDeserialize=true
export JDBC_USERNAME=odk_int
export JDBC_PASSWORD=odk_int
export JDBC_SCHEMA=odk_int
```

Seventh, drop the war into Tomcat
--------------------------------
Download Tomcat and explode it onto your development machine.  Go into the webapps directory and delete the ROOT directory.  Rename the war you created with your mvn install command to ROOT.war, and place it in the webapps directory.  Restart Tomcat.

The Tomcat ports used by ODK aggregate are specified in ```odk-common-settings/src/main/java/resources/security.properties```, and must match those used by Tomcat.  They are currently set to the defaults of 8080 for http and 8443 for https, so you should be able to start up a vanilla Tomcat instance without changing them.  However, make sure nothing else is using those ports!

Once you've restarted Tomcat, you should be able to go to **http://localhost:8080** and log in as ```admin```, password ```aggregate```.

You are ready to use ODK Aggregate server.

### You may be interested in the FAQ:
**[Frequently Asked Questions](FAQ.md)**

- [How do I enable HTTPS on ODK Aggregate server?](FAQ.md#how-do-i-enable-https-on-odk-aggregate-server)
