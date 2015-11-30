package org.black.kotlin.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.utils.ProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinCompiler {

    public final static KotlinCompiler INSTANCE = new KotlinCompiler();

    private KotlinCompiler() {
    }

    public void antCompile(KotlinProject proj){
        try {
            makeBuildXml(proj);
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"compile"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void antBuild(KotlinProject proj) {
        try {
            makeBuildXml(proj);
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"build"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void antRun(KotlinProject proj) throws IOException, InterruptedException {
        try {
            makeBuildXml(proj);
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"run"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void makeBuildXml(KotlinProject proj) throws IOException {
        StringBuilder build = new StringBuilder("");
        build.append("<project name=\"Kotlin_Project-impl\" default=\"build\">\n"
                + "    <property name=\"kotlin.lib\"  value=\"");
        build.append(ProjectUtils.KT_HOME).append("lib");
        build.append("\"/> \n"
                + "    <property name=\"build.dir\"   value=\"build\"/>\n"
                + "\n"
                + "    <typedef resource=\"org/jetbrains/kotlin/ant/antlib.xml\" classpath=\"${kotlin.lib}/kotlin-ant.jar\"/>\n"
                + "\n"
                + "    <target name=\"compile\">\n"
                + "        <delete dir=\"${build.dir}/classes\" failonerror=\"false\"/>\n"
                + "        <mkdir dir=\"${build.dir}/classes\"/>\n"
                + "        <javac destdir=\"${build.dir}/classes\" includeAntRuntime=\"false\" srcdir=\"src\">\n"
                + "		    <classpath>");

        List<String> libs = ProjectUtils.getLibs(proj);
        for (String lib : libs) {
            build.append("<pathelement path=\"lib/").append(lib).append("\"/>");
        }
        build.append("</classpath>                \n"
                + "		<withKotlin/>\n"
                + "        </javac>\n"
                + "</target>\n"
                + "\n"
                + "    <target name=\"build\" depends=\"compile\">"
                + "        <jar destfile=\"${build.dir}/${ant.project.name}.jar\">\n"
                + "    	    <zipgroupfileset dir=\"${kotlin.lib}\" includes=\"kotlin-runtime.jar\" />");
        build.append("<zipgroupfileset dir=\"lib\" includes=\"*.jar\" />\n"
                + "            <fileset dir=\"${build.dir}/classes\"/>\n"
                + "	    <manifest>\n"
                + "                <attribute name=\"Main-Class\" value=\"");
        build.append(ProjectUtils.getMainFileClass(proj.getProjectDirectory().getChildren()));
        build.append("\"/>\n"
                + "            </manifest>\n"
                + "        </jar>\n"
                + "    </target>");

        build.append("<target name=\"run\" depends=\"build\">\n"
                + "        <java jar=\"${build.dir}/${ant.project.name}.jar\" fork=\"true\"/>\n"
                + "    </target>\n"
                + "</project>");

        File buildXml = new File(proj.getProjectDirectory().getPath() + "/nbproject/build-impl.xml");

        if (buildXml.exists()) {
            buildXml.delete();
        }

        buildXml.createNewFile();
        PrintWriter writer = new PrintWriter(buildXml);
        // PrintWriter writer = new PrintWriter(proj.getProjectDirectory().getPath() + "/nbproject/build2.xml", "UTF-8");
        writer.print(build.toString());
        writer.close();
    }

}
