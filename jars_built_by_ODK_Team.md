
local-repository directory
==========================

The ```local-repository``` directory is not to be confused with the local repository in your maven installation (normally at ```~/.m2/repository```).  This directory has been added so that jars not or no longer available through a public Maven repository can be kept with this project and their dependencies handled seamlessly with pom.xml configuration files.

Using the ```local-repository``` folder is a last resort, after you've exhausted public repository options for a new jar.  However, if a necessary jar is not available in maven, you can add it to the ```local-respository``` folder with the following command:

```shell
mvn deploy:deploy-file -Durl=file:///YOUR-DIRECTORY-PATH-TO-AGGREGATE/aggregate/local-repository \
-Dfile=odk-tomcatutil-1.0.1.jar \
-DgroupId=org.opendatakit \
-DartifactId=odk-tomcatutil \
-Dpackaging=jar \
-Dversion=1.0.1
```

This will create the necessary file framework in the ```local-directory``` directory so that you can include jars in aggregate project POM files.


JARs built by ODK team
==========================

These are the jars in the ```local-repository``` directory.

javarosa-libraries:
-------------------

This is a special build of javarosa using the tree at https://bitbucket.org/m.sundt/javarosa It incorporates multithread-safe KoBo collect changes (from Clayton), abandons J2ME support, exposes bind and prompt attributes, and numerous contributed fixes from SurveyCTO and others.

odk-httpclient-gae-4.5.2:
-------------------------

This can be installed by pulling the original ODK Aggregate (Components) sources and running 'mvn install' in the GaeHttpClient project.

odk-tomcatutil-1.0.1:
---------------------

This can be installed by pulling the original ODK Aggregate (Components) sources and running 'mvn install' in the TomcatUtils project.

gwt-google-maps-v3-1.0.1:
-------------------------

This can be installed by pulling the Aggregate (Components) sources and building the gwt-google-maps-v3 project.

See the original ODK  Aggregate (Components) README.txt file for how this was built. It uses the sources that were originally located here (but have since been removed):
http://code.google.com/p/gwt-google-maps-v3/

gwt-visualization-1.1.2:
------------------------

The sources are in the jar. The full project can be found in the original ODK  Aggregate (Components) project. It is copied from the download formerly available here:
  http://code.google.com/p/gwt-google-apis/
