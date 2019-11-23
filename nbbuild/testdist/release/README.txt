Binary test distribution for NetBeans
-----------------------------------------

The test distribution contains tests for NetBeans. There are two types
of tests:

unit tests - developed by development team
qa-functional tests -  developed by QE team

How to run tests by JUnit harness
-------------------------------------

Set ANT_OPTS=-Xmx1024m to prevent OOME.
JDK on which you run can be controlled by JAVA_HOME variable (read by Ant)
or by custom.jvm.executable property.

ant -Dnetbeans.dest.dir=/home/joe/netbeans

The 'netbeans.dest.dir' is required property and contains absolute path 
to directory with NetBeans installation. 

Custom properties:
------------------
test.types - unit or qa-functional test type. Default is unit:qa-functional.
    example: unit

test.clusters - just one cluster for which tests should be executed
    example: platform

modules.list - list of modules separated by ':' in format ${cluster}/${code-base-name}
    example: platform/org-openide-filesystems:platform/org-openide-masterfs

test.required.modules - run tests only with listed modules when property is defined
    example: org-openide-explorer.jar,org-openide-master-fs.jar runs modules which needs
          the org-openide-explorer.jar and org-openide-master-fs.jar for test run

test-sys-prop.ignore.random.failures - skips test cases which are marked with 
@RandomlyFails annotation
    example: -Dtest-sys-prop.ignore.random.failures=true

test.config - run only tests specified in test config definition. Regex pattern 
which tests are included in particular config is defined in project.properties
(e.g. test.config.stable.includes=**/ATest.class,**/b/BTest.class).
    example: stable

test.config.default.includes - regex pattern defining which class should be
included in test execution. It is recommended to use together with modules.list
parameter.
    example: **/WritableXMLFileSystemTest.class

custom.jvm.executable - specifies JVM in which are test executed. Full path
to java executable must be supplied. If not specified, default java will be used.
    example: /home/joe/jdk1.7.0/bin/java

custom.jvm.args - property for passing JVM parameters to junit test. Those
custom parameters are added to predefined parameters in one-module.xml. 

Examples with custom properties:
ant -Dnetbeans.dest.dir=/home/joe/netbeans -Dtest.types=unit -Dtest-sys-prop.ignore.random.failures=true -Dtest.clusters=platform
ant -Dnetbeans.dest.dir=/home/joe/netbeans -Dtest.types=unit -Dmodules.list=apisupport/org-netbeans-modules-apisupport-project
ant -Dnetbeans.dest.dir=/home/joe/netbeans -Dtest.types=unit -Dmodules.list=apisupport/org-netbeans-modules-apisupport-project -Dtest.config.default.includes=**/WritableXMLFileSystemTest.class

Generated report:
-----------------
Reports are generated to */results/html folders.

In case reports are not generated automatically you can create them using:

export ANT_OPTS=-Xmx1024m
ant -f all-tests.xml generate-html-results -Dtest.results.dir=unit/results

If you run both unit and qa-functional tests you can merge results together:

ant merge-results

How to build test distribution
------------------------------
cd hg/main
ant build-test-dist
# Build test distribution for one module and its dependencies
ant build-test-dist -Dallmodules=j2ee.kit
