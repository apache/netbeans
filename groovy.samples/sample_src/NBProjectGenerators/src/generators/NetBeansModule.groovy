
package generators

def id = new Date().time
def folder = System.getProperty("user.home") + "/NewModule" + id;
def name = "NewModule" + id;
def pkgs = "org/netbeans/modules/demo"

println "Generating project ${name} in folder ${folder}";

def manifest_mf = """\
Manifest-Version: 1.0
OpenIDE-Module: ${name}
OpenIDE-Module-Layer: ${pkgs}/layer.xml
OpenIDE-Module-Localizing-Bundle: ${pkgs}/Bundle.properties
OpenIDE-Module-Specification-Version: 1.0

"""
def build_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<!-- You may freely edit this file. See harness/README in the NetBeans platform -->
<!-- for some information on what you could do (e.g. targets to override). -->
<!-- If you delete this file and reopen the project it will be recreated. -->
<project name="${name}" basedir=".">
    <description>Builds, tests, and runs the project ${name}.</description>
    <import file="nbproject/build-impl.xml"/>
</project>

"""

def layer_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE filesystem PUBLIC "-//NetBeans//DTD Filesystem 1.1//EN" "http://www.netbeans.org/dtds/filesystem-1_1.dtd">
<filesystem>
</filesystem>

"""

def bundle_properties = """\
OpenIDE-Module-Name=${name}

"""

def build_impl_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<!--
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
-->
<project name="${name}-impl" basedir="..">
    <property file="nbproject/private/platform-private.properties"/>
    <property file="nbproject/platform.properties"/>
    <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-project/2">
        <attribute name="name"/>
        <attribute name="value"/>
        <sequential>
            <property name="@{name}" value="\${@{value}}"/>
        </sequential>
    </macrodef>
    <property file="\${user.properties.file}"/>
    <nbmproject2:property name="harness.dir" value="nbplatform.\${nbplatform.active}.harness.dir" xmlns:nbmproject2="http://www.netbeans.org/ns/nb-module-project/2"/>
    <nbmproject2:property name="netbeans.dest.dir" value="nbplatform.\${nbplatform.active}.netbeans.dest.dir" xmlns:nbmproject2="http://www.netbeans.org/ns/nb-module-project/2"/>
    <fail message="You must define 'nbplatform.\${nbplatform.active}.harness.dir'">
        <condition>
            <not>
                <available file="\${harness.dir}" type="dir"/>
            </not>
        </condition>
    </fail>
    <import file="\${harness.dir}/build.xml"/>
</project>

"""
def platform_properties = """\
nbplatform.active=default

"""
def project_properties = """\
javac.source=1.5
javac.compilerargs=-Xlint -Xlint:-serial

"""
def project_xml = """\
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://www.netbeans.org/ns/project/1">
    <type>org.netbeans.modules.apisupport.project</type>
    <configuration>
        <data xmlns="http://www.netbeans.org/ns/nb-module-project/3">
            <code-name-base>${name}</code-name-base>
            <standalone/>
            <module-dependencies/>
            <public-packages/>
        </data>
    </configuration>
</project>

"""
def base = new File(folder);
base.mkdirs();
def src = new File(folder + File.separator + "src" + File.separator + "${pkgs}");
src.mkdirs();
def nbproject = new File(folder + File.separator + "nbproject");
nbproject.mkdirs();
new File(folder + File.separator  +"build.xml").write(build_xml);
new File(folder + File.separator  +"manifest.mf").write(manifest_mf);
new File("${src}" + File.separator + "layer.xml").write(layer_xml);
new File("${src}" + File.separator + "Bundle.properties").write(bundle_properties);
new File("${nbproject}" + File.separator + "build-impl.xml").write(build_impl_xml);
new File("${nbproject}" + File.separator + "platform.properties").write(platform_properties);
new File("${nbproject}" + File.separator + "project.properties").write(project_properties);
new File("${nbproject}" + File.separator + "project.xml").write(project_xml);

println("Done.")

