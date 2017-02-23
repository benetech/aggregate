Frequently Asked Questions
==========================

### What is this local-repository?
There are several jars supplied by the OpenDataKit project that are no longer available in any public Maven repositories.   So that you can check out this project and start using it, they have been provided in the local-repository folder.  An entry in the root pom.xml _should_ allow your Maven build to find these jars without any additional configuration.  **Ideally, you will put these jars in your own private Maven repository.  Storing jars directly in a project like this is not a good practice.**

More information about these jars is available in [jars_built_by_ODK_Team.md](jars_built_by_ODK_Team.md).

If you run into dependency errors with these jars, you may choose to install them manually into your local repository (at ```~/.m2/repository```) using the maven install command like
```
mvn install:install-file -Dfile=odk-tomcatutil-1.0.1.jar -DgroupId=org.opendatakit -DartifactId=odk-tomcatutil -Dversion=1.0.1 -Dpackaging=jar -DupdateReleaseInfo=true
```
Even better, put them in your team's Maven repository so that you don't have to deal with all that.

