package org.black.kotlin.bundledcompiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

public class BundledCompiler {

    public static void getBundledCompiler() {
        try {
            FileObject build = getBuildFileObject();
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                "Kotlin compiler is not found. Please wait while compiler is loaded.", NotifyDescriptor.INFORMATION_MESSAGE));
            ExecutorTask task = ActionUtils.runTarget(build, new String[]{"download_bundled"}, null);
            task.waitFinished();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static FileObject getBuildFileObject() throws IOException {
        
        File bundledCompilerDir = Places.getUserDirectory();
        File buildFile = new File(bundledCompilerDir.getAbsolutePath() + "/" + "build.xml");
        
        buildFile.createNewFile();
        
        PrintWriter writer = new PrintWriter(buildFile.getAbsolutePath(), "UTF-8");
        writer.print(makeBuildScript());
        writer.close();
        
        FileObject buildScript = FileUtil.toFileObject(buildFile);
        
        return buildScript;
    }

    private static String makeBuildScript() throws UnsupportedEncodingException {
        String targetDir = Places.getUserDirectory().getAbsolutePath().replace("\\","/");
//        String targetDir = Places.getUserDirectory().toURI().getPath();
        return "<project default=\"get_bundled\">\n"
                + "    <property name=\"compiler.tag\" value=\"for_eclipse\" />\n"
                + "    <property name=\"bootstrap_branch\" value=\"1.0.1\"/>\n"
                + "	\n"
                + "    <property name=\"compiler.query\" value=\"${compiler.tag}.tcbuildtag\" />\n"
                + "	\n"
                + "    <property name=\"teamcity-base\" value=\"https://teamcity.jetbrains.com\"/>\n"
                + "    <property name=\"teamcity-kotlin-url\" value = \"${teamcity-base}/guestAuth/repository/download/bt345/${compiler.query}\" />\n"
                + "	\n"
                + "    <property name=\"compiler-artifact-file\" value=\"kotlin-compiler.zip\"/>\n"
                + "    <property name=\"compiler-sources-file\" value=\"kotlin-compiler-sources.jar\"/>\n"
                + "\n"
                + "    <condition property=\"branch\" value=\"?branch=${bootstrap_branch}\" else=\"\">\n"
                + "        <length string=\"${bootstrap_branch}\" trim=\"true\" when=\"greater\" length=\"0\" />\n"
                + "    </condition>\n"
                + "	\n"
                + "    <property name=\"project.name\" value=\"kotlin-bundled-compiler\" />\n"
                + "		\n"
                + "    <condition property=\"target.dir\" value=\"${teamcity.build.workingDir}/lib\" else=\"" +
                targetDir + "/kotlinc\">\n"
                + "        <isset property=\"teamcity.build.workingDir\"/>\n"
                + "    </condition>\n"
                + "		\n"
                + "    <property name=\"download.name\" value=\"downloads\" />\n"
                + "    <property name=\"download.dir\" value=\"${target.dir}/${download.name}\" />\n"
                + "\n"
                + "    <target name=\"download_bundled\">\n"
                + "        <mkdir dir=\"${target.dir}\" />\n"
                + "        <delete includeemptydirs=\"true\">\n"
                + "            <fileset dir=\"${target.dir}\" includes=\"**/*\" excludes=\"${download.name}/*\" />\n"
                + "        </delete>\n"
                + "		\n"
                + "\n"
                + "        <mkdir dir=\"${download.dir}\" />\n"
                + "\n"
                + "        <get \n"
                + "            src=\"${teamcity-kotlin-url}/kotlin-plugin-%7Bbuild.number%7D.zip${branch}\" \n"
                + "            dest=\"${download.dir}/${compiler-artifact-file}\" \n"
                + "            usetimestamp=\"true\" />\n"
                + "		\n"
                + "        <unzip src=\"${download.dir}/${compiler-artifact-file}\" dest=\"${target.dir}\">\n"
                + "            <patternset>\n"
                + "                <include name=\"Kotlin/kotlinc/lib/**/*.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-compiler.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-runtime.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-reflect.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-jdk-annotations.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-runtime-sources.jar\" />\n"
//                + "                <include name=\"Kotlin/kotlinc/lib/kotlin-ant.jar\" />\n"
                + "            </patternset>\n"
                + "            <mapper type=\"flatten\"/>\n"
                + "        </unzip>\n"
                + "		\n"
                + "        <delete dir=\"${download.dir}\"/>\n"
                + "                \n"
                + "    </target>\n"
                + "	\n"
                + "</project>";
    }

}
