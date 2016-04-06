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
     * @param project project to compile
     */
    public void antCompile(KotlinProject project) {
        try {
            makeBuildXml(project);
            FileObject buildImpl = project.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"compile"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method runs build target of ant build script.
     *
     * @param project project to compile
     */
    public void antBuild(KotlinProject project) {
        try {
            makeBuildXml(project);
            FileObject buildImpl = project.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"build"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method runs run target of ant build script.
     *
     * @param project project to compile
     */
    public void antRun(KotlinProject project) throws IOException, InterruptedException {
        try {
            makeBuildXml(project);
            FileObject buildImpl = project.getHelper().getProjectDirectory().getFileObject("build.xml");
            ActionUtils.runTarget(buildImpl, new String[]{"run"}, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method returns ant compile target
     *
     * @param project Kotlin project
     * @return String with ant compile target
     */
    private String makeCompileTarget(KotlinProject project) {
        StringBuilder build = new StringBuilder("");
        build.append("    <target name=\"compile\">\n"
                + "        <mkdir dir=\"${build.dir}/classes\"/>\n"
                + "        <javac destdir=\"${build.dir}/classes\" includeAntRuntime=\"false\" srcdir=\"src\">\n"
                + "		    <classpath>\n");

        List<String> libs = ProjectUtils.getLibs(project);
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
     * @param project Kotlin project
     * @return String with ant build target
     */
    private String makeBuildTarget(KotlinProject project) throws IOException {
        return "" + "\n"
                + "    <target name=\"build\" depends=\"compile\">\n"
                + "        <jar destfile=\"${build.dir}/${ant.project.name}.jar\">\n"
                + "    	    <zipgroupfileset dir=\"${kotlin.lib}\" includes=\"kotlin-runtime.jar\" />" 
                + "<zipgroupfileset dir=\"lib\" includes=\"*.jar\" />\n"
                + "            <fileset dir=\"${build.dir}/classes\"/>\n"
                + "	    <manifest>\n"
                + "                <attribute name=\"Main-Class\" value=\"" +
                ProjectUtils.getMainFileClass(project) +
                "\"/>\n"
                + "            </manifest>\n"
                + "        </jar>\n"
                + "    </target>\n";
    }

    /**
     * This method returns ant run target
     *
     * @return String with ant run target
     */
    private String makeRunTarget() {
        return "" + "    <target name=\"run\" depends=\"build\">\n"
                + "        <java jar=\"${build.dir}/${ant.project.name}.jar\" fork=\"true\"/>\n"
                + "    </target>\n";
    }

    /**
     * This method creates ant build script.
     *
     * @param project target project.
     * @throws IOException
     */
    private void makeBuildXml(KotlinProject project) throws IOException {
        String ktHome = new String(ProjectUtils.KT_HOME.getBytes("UTF-8"));
        String build = "" 
                + "<project name=\"Kotlin_Project-impl\" default=\"build\">\n"
                + "    <property name=\"kotlin.lib\"  value=\"" +
                ktHome + "lib" +
                "\"/> \n"
                + "    <property name=\"build.dir\"   value=\"build\"/>\n"
                + "\n"
                + "    <typedef resource=\"org/jetbrains/kotlin/ant/antlib.xml\" classpath=\"${kotlin.lib}/kotlin-ant.jar\"/>\n"
                + "\n" +
                makeCompileTarget(project) +
                makeBuildTarget(project) +
                makeRunTarget() +
                "</project>";

        File buildXml = new File(project.getProjectDirectory().getPath() + "/nbproject/build-impl.xml");

        if (buildXml.exists()) {
            if (!buildXml.delete()){
                System.err.println("Error while deleting build.xml");
            }
        }

        if (!buildXml.createNewFile()){
            System.err.println("Error while creating build.xml");
        }
        PrintWriter writer = new PrintWriter(buildXml);
        writer.print(build);
        writer.close();
    }

}
