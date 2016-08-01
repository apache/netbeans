/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.bundledcompiler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
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
                targetDir + "/kotlinc/lib\">\n"
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

//    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
    private static void unZipFile(InputStream fis, FileObject destDir) throws IOException {
//        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }
    
    public static void getKotlinc(ClassLoader cl) {
        try {
            InputStream inputStream = cl.getResourceAsStream("org/black/kotlin/kotlinc/kotlinc.zip");

            FileObject destFileObj = FileUtil.toFileObject(Places.getUserDirectory());
            unZipFile(inputStream, destFileObj);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
