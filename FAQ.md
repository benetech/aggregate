Frequently Asked Questions
==========================

## How do I enable HTTPS on ODK Aggregate server?

### Modify openssl.cnf file
1. Open openssl.cnf file for writing (default location: ```/etc/ssl/openssl.cnf```.
2. Find  ```v3_ca``` and add lines ```subjectAltName = @alternate_names```and ```keyUsage = digitalSignature, keyEncipherment``` below.
3. Under ```CA_default``` section find ```# copy_extensions = copy``` and uncomment it (remove ```#```).
4. At the end of this file add section (replace =your_ip= with IP address used when building Aggregate):

```
#+BEGIN_EXAMPLE
[ alternate_names ]
IP.1 = your_ip
#+END_EXAMPLE
```

### Creating SSL self signed certificate
1. Create a directory for your certificates (ex. ```mkdir cert```). Go to this directory (```cd cert```).
2. Use OpenSSL to generate certificate: ```sudo openssl req -x509 -nodes -days 365 -newkey rsa:4096 -keyout server.key -out server.crt -reqexts v3_req -extensions v3_ca```
3. When asked for Common Name type IP address used when building Aggregate. Other answers are irrelevant.
4. Convert certificate to DER format (needed later for Android Emulator): =openssl x509 -in server.crt -outform der -out server.der.crt=
5. Convert certificate to PCKS12 (for Tomcat): ```openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name test_server -caname root_ca```
6. Use password ```password```
7. Form keystore (use ```password``` again): =keytool -importkeystore -destkeystore keystore.jks -srckeystore server.p12 -srcstoretype PKCS12 -srcalias test_server -destalias test_server=
8. Move keystore.jks to home folder

Add this lines to server.xml file which is in a conf folder of tomcat:

```xml
   <Connector protocol="org.apache.coyote.http11.Http11NioProtocol"
            port="8443" maxThreads="200"
            scheme="https" secure="true" SSLEnabled="true"
            keystoreFile="${user.home}/keystore.jks" keystorePass="password"
            clientAuth="false" sslProtocol="TLS"/>
```

## What is this local-repository?
There are several jars supplied by the OpenDataKit project that are no longer available in any public Maven repositories.   So that you can check out this project and start using it, they have been provided in the local-repository folder.  An entry in the root pom.xml _should_ allow your Maven build to find these jars without any additional configuration.  **Ideally, you will put these jars in your own private Maven repository.  Storing jars directly in a project like this is not a good practice.**

More information about these jars is available in [jars_built_by_ODK_Team.md](jars_built_by_ODK_Team.md).

If you run into dependency errors with these jars, you may choose to install them manually into your local repository (at ```~/.m2/repository```) using the maven install command like
```
mvn install:install-file -Dfile=odk-tomcatutil-1.0.1.jar -DgroupId=org.opendatakit -DartifactId=odk-tomcatutil -Dversion=1.0.1 -Dpackaging=jar -DupdateReleaseInfo=true
```
Even better, put them in your team's Maven repository so that you don't have to deal with all that.


## Do I have to keep manually loading my war into Tomcat?

To repeatedly reload your newly compiled war into Tomcat, you can use a shell script like this one.  You would rerun this script after a successful run of ```mvn install```.  You could also add additional environment variable exports to this file for your database parameters.

```shell
export CATALINA_HOME=~/tomcat/apache-tomcat-8.5.11
pushd ~/tomcat/apache-tomcat-8.5.11/bin
echo Shutting down tomcat...
./shutdown.sh
echo Deleting logs..
rm ../logs/*
echo Copying Postgres war...
cp ~/aggregate/aggregate-postgres/target/aggregate-postgres-1.0.1.war ~/tomcat/apache-tomcat-8.5.11/webapps/ROOT.war
echo Sleeping...
sleep 30
echo Starting tomcat...
./startup.sh
popd
tail -f ~/tomcat/apache-tomcat-8.5.11/logs/catalina.out
```


