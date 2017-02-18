# aggregate

This project is a fork of the opendatakit aggregate project, being worked on by Benetech and incorporating features contributed by [SolDevelo](http://www.soldevelo.com/)

For the original OpenDataKit project, see:
https://github.com/opendatakit/aggregate

Goals
-----
This fork has a number of changes from the original project, along these goals:

1. Simplify the project development environment so that new developers can get up to speed quickly, and it can be maintained by a small team.
  + Remove parts that we don't use.
  + Use conventions of common tools like Maven.
2. Deploy the aggregate server as a war in a Docker container.
  + Create a war from the command line in one step.
  + Easily configure that war in a Docker container.
  + Focus on automation and continuous integration.

Getting Set Up
--------------
See [CONFIGURE.md](CONFIGURE.md).

Changes from the Original OpenDataKit
-------------------------------------

**1. Rely more on Maven Conventions.**

  The OpenDataKit already uses Maven, with the source code for all modules stored in one central folder.  By putting the source code for each module in its respective module directory, it's easier to track down problems and understand how the modules interact.  Tools that expect source code and unit tests to be in Maven's default folders will work without special configuration.
  
**2. Separate out integration tests into the aggregate-integration module.**

  These are currently broken and getting them working again is high on the to do list.

**3.  Decouple the project from Eclipse.**

  All of the Eclipse project files have been removed from the project.  YOu should be able to import the project as a Maven file into Eclipse and use that as your IDE.  Additionally you could import this project into IntelliJ IDEA or the IDE of your choice without any Eclipse baggage.  You could also edit it in a text editor and run builds from the command line.

**4. Narrow database support to MySql and Postgres.**

  By dropping support for Google Application Engine and Microsoft SQL Server, we get the following:
   + Spend less time maintaining code we won't use.
   + Affirm our commitment to open source solutions.
   + Upgrade to Java 8 (was limited by GAE.)
   + Establish clearer component boundary between webapp and database (in this case Tomcat and choice of database)

**5. Remove Bitrock InstallBuilder.**
  We are going to replace this with an automatable, command line-driven solution that requires no human intervention.  Currently, the plan is to allow passing in parameters such as database server parameters as environment variables, for those users uncomfortable recompiling the project war in order to change configuration parameters.

-------
License

This software is licensed under the [**Apache 2.0 license**](http://www.apache.org/licenses/LICENSE-2.0)

