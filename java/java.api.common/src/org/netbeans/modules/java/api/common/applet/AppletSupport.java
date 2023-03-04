/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.api.common.applet;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;

import org.openide.*;
import org.openide.modules.SpecificationVersion;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.api.java.classpath.*;

import org.netbeans.api.java.platform.*;

import org.netbeans.modules.java.api.common.util.CommonProjectUtils;

/** Support for execution of applets.
*
* @author Ales Novak, Martin Grebac
*/
public class AppletSupport {

    // JDK issue #6193279: Appletviewer does not accept encoded URLs
    private static final SpecificationVersion JDK_15 = new SpecificationVersion("1.5"); // NOI18N

    /** constant for html extension */
    private static final String HTML_EXT = "html"; // NOI18N
    /** constant for class extension */
    private static final String CLASS_EXT = "class"; // NOI18N

    private static final String POLICY_FILE_NAME = "applet";
    private static final String POLICY_FILE_EXT = "policy";
        
    private AppletSupport() {}

    // Used only from unit tests to suppress detection of applet. If value
    // is different from null it will be returned instead.
    public static Boolean unitTestingSupport_isApplet = null;
    
    public static boolean isApplet(final FileObject file) {
        if (file == null) {
            return false;
        }
        // support for unit testing
        if (unitTestingSupport_isApplet != null) {
            return unitTestingSupport_isApplet.booleanValue();
        }
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return false;
        }
        final boolean[] result = new boolean[] {false};
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void run(CompilationController control) throws Exception {
                    if (JavaSource.Phase.ELEMENTS_RESOLVED.compareTo(control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED))<=0) {
                        Elements elements = control.getElements();
                        Trees trees = control.getTrees();
                        Types types = control.getTypes();
                        TypeElement applet = elements.getTypeElement("java.applet.Applet");     //NOI18N
                        TypeElement japplet = elements.getTypeElement("javax.swing.JApplet");   //NOI18N
                        CompilationUnitTree cu = control.getCompilationUnit();
                        List<? extends Tree> topLevels = cu.getTypeDecls();
                        for (Tree topLevel : topLevels) {
                            if (TreeUtilities.CLASS_TREE_KINDS.contains(topLevel.getKind())) {
                                TypeElement type = (TypeElement) trees.getElement(TreePath.getPath(cu, topLevel));
                                if (type != null) {
                                    Set<Modifier> modifiers = type.getModifiers();
                                    if (modifiers.contains(Modifier.PUBLIC) && 
                                        ((applet != null && types.isSubtype(type.asType(), applet.asType())) 
                                        || (japplet != null && types.isSubtype(type.asType(), japplet.asType())))) {
                                            result[0] = true;
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
                
                public void cancel() {}
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }    
    
    /**
    * @return html file with the same name as applet
    */
    private static FileObject generateHtml(FileObject appletFile, FileObject buildDir, FileObject classesDir) throws IOException {
        FileObject htmlFile = buildDir.getFileObject(appletFile.getName(), HTML_EXT);
        
        if (htmlFile == null) {
            htmlFile = buildDir.createData(appletFile.getName(), HTML_EXT);
        }
        
        FileLock lock = htmlFile.lock();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(htmlFile.getOutputStream(lock));
            ClassPath cp = ClassPath.getClassPath(appletFile, ClassPath.EXECUTE);
            ClassPath sp = ClassPath.getClassPath(appletFile, ClassPath.SOURCE);
            String path = FileUtil.getRelativePath(sp.findOwnerRoot(appletFile), appletFile);
            path = path.substring(0, path.length()-5);
            String codebase = FileUtil.getRelativePath(buildDir, classesDir);
            if (codebase == null) {
                codebase = classesDir.toURL().toString();
            }
            fillInFile(writer, path + "." + CLASS_EXT, "codebase=\"" + codebase + "\""); // NOI18N
        } finally {
            lock.releaseLock();
            if (writer != null)
                writer.close();
        }
        return htmlFile;
    }

    /**
    * @return html file with the same name as applet
    */
    public static FileObject generateSecurityPolicy(FileObject projectDir) {

        FileObject policyFile = projectDir.getFileObject(POLICY_FILE_NAME, POLICY_FILE_EXT);
        
        try {
            if (policyFile == null) {
                policyFile = projectDir.createData(POLICY_FILE_NAME, POLICY_FILE_EXT);
            }        
            FileLock lock = policyFile.lock();
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(policyFile.getOutputStream(lock));
                fillInPolicyFile(writer);
            } finally {
                lock.releaseLock();
                if (writer != null)
                    writer.close();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Problem when generating applet policy file: " + ioe); //NOI18N
        }
        return policyFile;
    }
    
    /**
    * @return URL of the html file with the same name as sibling
    */
    public static URL generateHtmlFileURL(FileObject appletFile, FileObject buildDir, FileObject classesDir, String activePlatform) {
        FileObject html = null;
        IOException ex = null;
        if ((appletFile == null) || (buildDir == null) || (classesDir == null)) {
            return null;
        }
        try {
            html = generateHtml(appletFile, buildDir, classesDir);
            if (html!=null) {
                return getHTMLPageURL(html, activePlatform);
            }
            else {
                return null;
            }
        } catch (IOException iex) {
            return null;
        }
    }
    
    
    /**
     * Creates an URL of html page passed to the appletviewer. It workarounds a JDK 1.5 appletviewer
     * bug. The appletviewer is not able to handle escaped URLs. 
     * @param htmlFile html page
     * @param activePlatform identifier of the platform used in the project
     * @return URL of the html page or null
     */
    public static URL getHTMLPageURL (FileObject htmlFile, String activePlatform) {
        assert htmlFile != null : "htmlFile cannot be null";    //NOI18N
        // JDK issue #6193279: Appletviewer does not accept encoded URLs
        JavaPlatform platform = CommonProjectUtils.getActivePlatform(activePlatform);
        boolean workAround6193279 = platform != null    //In case of nonexisting platform don't use the workaround
                && platform.getSpecification().getVersion().compareTo(JDK_15)>=0; //JDK1.5 and higher
        URL url = null;
        if (workAround6193279) {
            File f = FileUtil.toFile(htmlFile);
            try {
                String path = f.getAbsolutePath();
                if (File.separatorChar != '/') {    //NOI18N
                    path = path.replace(File.separatorChar,'/');   //NOI18N
                }
                url = new URL ("file",null,path);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        else {
            url = htmlFile.toURL();
        }
        return url;
    }

    /** fills in file with html source so it is html file with applet
    * @param file is a file to be filled
    * @param name is name of the applet                                     
    */
    private static void fillInFile(PrintWriter writer, String name, String codebase) {
        ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);

        writer.println("<HTML>"); // NOI18N
        writer.println("<HEAD>"); // NOI18N

        writer.print("   <TITLE>"); // NOI18N
        writer.print(bundle.getString("GEN_title"));
        writer.println("</TITLE>"); // NOI18N

        writer.println("</HEAD>"); // NOI18N
        writer.println("<BODY>\n"); // NOI18N

        writer.print(bundle.getString("GEN_warning"));

        writer.print("<H3><HR WIDTH=\"100%\">"); // NOI18N
        writer.print(bundle.getString("GEN_header"));
        writer.println("<HR WIDTH=\"100%\"></H3>\n"); // NOI18N

        writer.println("<P>"); // NOI18N
//        String codebase = getCodebase (name);
        if (codebase == null)
            writer.print("<APPLET code="); // NOI18N
        else
            writer.print("<APPLET " + codebase + " code="); // NOI18N
        writer.print ("\""); // NOI18N

        writer.print(name);
        writer.print ("\""); // NOI18N

        writer.println(" width=350 height=200></APPLET>"); // NOI18N
        writer.println("</P>\n"); // NOI18N

        writer.print("<HR WIDTH=\"100%\"><FONT SIZE=-1><I>"); // NOI18N
        writer.print(bundle.getString("GEN_copy"));
        writer.println("</I></FONT>"); // NOI18N

        writer.println("</BODY>"); // NOI18N
        writer.println("</HTML>"); // NOI18N
        writer.flush();
    }



    /** fills in policy file with all permissions granted
    * @param writer is a file to be filled
    */
    private static void fillInPolicyFile(PrintWriter writer) {
        writer.println("grant {"); // NOI18N
        writer.println("permission java.security.AllPermission;"); // NOI18N
        writer.println("};"); // NOI18N
        writer.flush();
    }
}
