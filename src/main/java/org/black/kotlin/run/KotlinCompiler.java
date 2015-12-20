package org.black.kotlin.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.utils.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Class that is responsible for Kotlin projects compiling.
 *
 * @author Александр
 */
public class KotlinCompiler {

    public final static KotlinCompiler INSTANCE = new KotlinCompiler();

    private KotlinCompiler() {
    }

    /**
     * This method runs compile target of ant build script.
     *
     * @param proj project to compile
     */
    public void antCompile(KotlinProject proj) {
        try {
            makeBuildXml(proj);
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"compile"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method runs build target of ant build script.
     *
     * @param proj project to compile
     */
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

    /**
     * This method runs run target of ant build script.
     *
     * @param proj project to compile
     */
    public void antRun(KotlinProject proj) throws IOException, InterruptedException {
        try {
//            ProjectUtils.findMainWithDetector(proj.getProjectDirectory().getFileObject("src").getChildren());
            makeBuildXml(proj);
            ProjectUtils.getOutputDir(proj);
            FileObject buildImpl = proj.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"run"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method returns ant compile target
     *
     * @param proj Kotlin project
     * @return String with ant compile target
     */
    private String makeCompileTarget(KotlinProject proj) {
        StringBuilder build = new StringBuilder("");
        build.append("    <target name=\"compile\">\n"
                + "        <mkdir dir=\"${build.dir}/classes\"/>\n"
                + "        <javac destdir=\"${build.dir}/classes\" includeAntRuntime=\"false\" srcdir=\"src\">\n"
                + "		    <classpath>\n");

        List<String> libs = ProjectUtils.getLibs(proj);
        for (String lib : libs) {
            build.append("                        <pathelement path=\"lib/").append(lib).append("\"/>");
            build.append("\n");
        }
        build.append("               </classpath>                \n"
                   + "		     <withKotlin/>\n"
                + "        </javac>\n"
                + "    </target>\n");

        return build.toString();
    }

    /**
     * This method returns ant build target
     *
     * @param proj Kotlin project
     * @return String with ant build target
     */
    private String makeBuildTarget(KotlinProject proj) throws IOException {
        StringBuilder build = new StringBuilder("");
        build.append("\n"
                + "    <target name=\"build\" depends=\"compile\">\n"
                + "        <jar destfile=\"${build.dir}/${ant.project.name}.jar\">\n"
                + "    	    <zipgroupfileset dir=\"${kotlin.lib}\" includes=\"kotlin-runtime.jar\" />");
        build.append("<zipgroupfileset dir=\"lib\" includes=\"*.jar\" />\n"
                + "            <fileset dir=\"${build.dir}/classes\"/>\n"
                + "	    <manifest>\n"
                + "                <attribute name=\"Main-Class\" value=\"");
        //TODO change getMainFileClass() method in the future
        build.append(ProjectUtils.getMainFileClass(proj.getProjectDirectory().getChildren()));
        build.append("\"/>\n"
                + "            </manifest>\n"
                + "        </jar>\n"
                + "    </target>\n");

        return build.toString();
    }

    /**
     * This method returns ant run target
     *
     * @param proj Kotlin project
     * @return String with ant run target
     */
    private String makeRunTarget(KotlinProject proj) {
        StringBuilder build = new StringBuilder("");
        build.append("    <target name=\"run\" depends=\"build\">\n"
                + "        <java jar=\"${build.dir}/${ant.project.name}.jar\" fork=\"true\"/>\n"
                + "    </target>\n");

        return build.toString();
    }

    /**
     * This method creates ant build script.
     *
     * @param proj target project.
     * @throws IOException
     */
    private void makeBuildXml(KotlinProject proj) throws IOException {
        StringBuilder build = new StringBuilder("");
        
        build.append("<project name=\"Kotlin_Project-impl\" default=\"build\">\n"
                + "    <property name=\"kotlin.lib\"  value=\"");
        build.append(ProjectUtils.KT_HOME).append("lib");
        build.append("\"/> \n"
                + "    <property name=\"build.dir\"   value=\"build\"/>\n"
                + "\n"
                + "    <typedef resource=\"org/jetbrains/kotlin/ant/antlib.xml\" classpath=\"${kotlin.lib}/kotlin-ant.jar\"/>\n"
                + "\n");

        build.append(makeCompileTarget(proj));
        build.append(makeBuildTarget(proj));
        build.append(makeRunTarget(proj));

        build.append("</project>");
        
        File buildXml = new File(proj.getProjectDirectory().getPath() + "/nbproject/build-impl.xml");

        if (buildXml.exists()) {
            buildXml.delete();
        }

        buildXml.createNewFile();
        PrintWriter writer = new PrintWriter(buildXml);
        writer.print(build.toString());
        writer.close();
    }

}
