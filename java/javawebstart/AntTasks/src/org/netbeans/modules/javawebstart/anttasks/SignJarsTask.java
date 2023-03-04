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
package org.netbeans.modules.javawebstart.anttasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.Move;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipOutputStream;

/**
 * 
 * @author Milan Kubec
 * @author Petr Somol
 * @author Tomas Zezula
 */
public class SignJarsTask extends Task {
    
    // jars in /lib signed with different signer/keystore
    // must be included in jnlp resources through jnlp extensions
    public static final String JNLP_COMPONENT_NAME = "jnlpcomponent"; //NOI18N
    public static final String EXTERNAL_JARS_PROP = "jar.files.to.include.through.external.jnlp"; //NOI18N
    public static final String EXTERNAL_JNLPS_PROP = "external.jnlp.component.names"; //NOI18N
    public static final String EXTERNAL_PROP_DELIMITER = ";"; //NOI18N

    private static final String SIG_START = "META-INF/"; //NOI18N
    private static final String SIG_END = ".SF"; //NOI18N
    private static final String MANIFEST = "META-INF/MANIFEST.MF";  //NOI18N
    private static final String UTF_8 = "UTF-8";    //NOI18N
    private static final String ATTR_CODEBASE = "Codebase"; //NOI18N
    private static final String ATTR_PERMISSIONS = "Permissions";   //NOI18N
    private static final String ATTR_APPLICATION_NAME = "Application-Name"; //NOI18N

    
    private int compIndex = 1;
    
    private String keystore;
    public void setKeystore(String s) {
        keystore = s;
    }
    
    private String storepass;
    public void setStorepass(String s) {
        storepass = s;
    }
    
    private String keypass;
    public void setKeypass(String s) {
        keypass = s;
    }
    
    private String alias;
    public void setAlias(String s) {
        alias = s;
    }
    
    private File mainJar;
    public void setMainjar(File f) {
        mainJar = f;
    }
    
    private File destDir;
    public void setDestdir(File f) {
        destDir = f;
    }
    
    private String codebase;
    public void setCodebase(String s) {
        codebase = s;
    }
    
    private String compProp;
    public void setComponentsprop(String s) {
        compProp = s;
    }
    
    private String signedJarsProp;
    public void setSignedjarsprop(String s) {
        signedJarsProp = s;
    }
    
    private List<FileSet> filesets = new LinkedList<FileSet>();
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }
    
    @Override
    public void execute() throws BuildException {
        
        Map<Set<String>,List<File>> signersMap = new HashMap<>(); // Set<signerName> -> List<jarPath>
        List<File> files2sign = new ArrayList<File>();
        List<File> alreadySigned = new ArrayList<File>();
        
        Iterator it = filesets.iterator();
        while (it.hasNext()) {
            FileSet fs = (FileSet) it.next();
            File dir = fs.getDir(getProject());
            if (!dir.exists()) {
                continue;
            }
            log("Processing FileSet: " + fs, Project.MSG_VERBOSE); //NOI18N
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (String f : files) {
                try {
                    File fl = new File(basedir, f);
                    Set<String> sgs = getSignatures(fl);
                    if (sgs.isEmpty()) {
                        files2sign.add(fl);
                    } else {
                        // test if the file is signed with passed alias
                        if (sgs.size() == 1 && sgs.contains(alias.toUpperCase())) {
                            alreadySigned.add(fl);
                        } else {
                            List lst = signersMap.get(sgs);
                            if (lst != null) {
                                lst.add(fl);
                            } else {
                                List<File> nlst = new ArrayList<File>();
                                nlst.add(fl);
                                signersMap.put(sgs, nlst);
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new BuildException(ex, getLocation());
                }
            }
        }
        
        log("Files to be signed: " + mainJar.toString() + ", " + files2sign.toString(), Project.MSG_VERBOSE); //NOI18N
        
        // for already signed files generate component jnlp file
        log("Files already signed by requested alias: " + alreadySigned.toString(), Project.MSG_VERBOSE); //NOI18N
        
        try {
            extendLibrariesManifests(getProject(), mainJar, files2sign);
            for (File signedLib : alreadySigned) {
                getProject().log(
                    String.format(
                        "Not adding security attributes into library: %s the library is already signed.",
                        safeRelativePath(getProject().getBaseDir(),signedLib)),
                    Project.MSG_WARN);
            }
        } catch (IOException ex) {
            getProject().log(
            "Failed to extend libraries manifests: " + ex.getMessage(), //NOI18N
            Project.MSG_WARN);
        }

        StringBuilder signedJarsBuilder = new StringBuilder();
        SignJar signJar = (SignJar) getProject().createTask("signjar"); //NOI18N
        signJar.setLocation(getLocation());
        signJar.setKeystore(keystore);
        signJar.setStorepass(storepass);
        signJar.setKeypass(keypass);
        signJar.setAlias(alias);
        signJar.init();
        
        // test main jar if its already signed with passed alias ??
        log("Signing main jar file: " + mainJar, Project.MSG_VERBOSE); //NOI18N
        signJar.setJar(mainJar);
        signJar.execute();
        
        if (files2sign.size() > 0) {
            for (Iterator<File> iter = files2sign.iterator(); iter.hasNext(); ) {
                File f = iter.next();
                log("Signing file: " + f, Project.MSG_VERBOSE); //NOI18N
                signJar.setJar(f);
                signJar.execute();
                signedJarsBuilder.append("\n        <jar href=\"lib/" + f.getName() + "\" download=\"eager\"/>");  //NOI18N// XXX lib ?
            }
        }
        
        if (alreadySigned.size() > 0) {
            for (Iterator<File> iter = alreadySigned.iterator(); iter.hasNext(); ) {
                File f = iter.next();
                log("Adding signed file: " + f, Project.MSG_VERBOSE); //NOI18N
                signedJarsBuilder.append("\n        <jar href=\"lib/" + f.getName() + "\" download=\"eager\"/>");  //NOI18N// XXX lib ?
            }
        }
        // TODO: is property signedJarsProp actually used anywhere ?
        getProject().setProperty(signedJarsProp, signedJarsBuilder.toString());
        
        StringBuilder compsBuilder = new StringBuilder();
        StringBuilder extJarsBuilder = new StringBuilder();
        StringBuilder extJnlpsBuilder = new StringBuilder();
        for (Iterator<Entry<Set<String>,List<File>>> iter = signersMap.entrySet().iterator(); iter.hasNext(); ) {
            Entry<Set<String>,List<File>> entry = iter.next();
            log("Already signed: keystore aliases = " + entry.getKey() + " -> signed jars = " + entry.getValue(), Project.MSG_VERBOSE); //NOI18N
            
            String compName = JNLP_COMPONENT_NAME + compIndex++;
            createJNLPComponentFile(entry.getKey(), entry.getValue(), compName);
            compsBuilder.append("\n        <extension name=\"" + compName + "\" href=\"" + compName + ".jnlp\"/>"); //NOI18N
            for(File jarFile : entry.getValue()) {
                extJarsBuilder.append(jarFile.getName() + EXTERNAL_PROP_DELIMITER);
            }
            extJnlpsBuilder.append(compName + ".jnlp" + EXTERNAL_PROP_DELIMITER); //NOI18N
        }
        // TODO: is property compProp actually used anywhere ?
        String extPropString = compsBuilder.toString();
        getProject().setProperty(compProp, extPropString);
        String extJarsPropString = extJarsBuilder.toString();
        getProject().setProperty(EXTERNAL_JARS_PROP, extJarsPropString);
        String extJnlpsPropString = extJnlpsBuilder.toString();
        getProject().setProperty(EXTERNAL_JNLPS_PROP, extJnlpsPropString);
    }
    
    /**
     * Returns set of signature aliases used to sign this file
     */
    private static Set<String> getSignatures(File f) throws IOException {
        ZipFile jarFile = null;
        Set<String> signatures = new HashSet<String>(3);
        try {
            jarFile = new ZipFile(f);
            for (Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) jarFile.entries(); en.hasMoreElements(); ) {
                ZipEntry je = en.nextElement();
                if (!je.isDirectory() && je.getName().startsWith(SIG_START) && je.getName().endsWith(SIG_END)) {
                    // there is signature file, get the name
                    String sigName = je.getName().substring(SIG_START.length(), je.getName().indexOf(SIG_END));
                    signatures.add(sigName);
                }
            }
        } finally {
            if (jarFile != null) jarFile.close();
        }
        return signatures;
    }
    
    /**
     * return filename of the JNLP component file ??
     */
    private String createJNLPComponentFile(Set<String> aliases, List<File> jars, String compName) {
        
        File f = new File(destDir, compName + ".jnlp"); //NOI18N
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(f);
        } catch (IOException ioe) {
            throw new BuildException(ioe, getLocation());
        }

        String codebaseTypeProp = getProject().getProperty("jnlp.codebase.type"); //NOI18N // property in project.properties
        String codebaseProp = null;
        if (codebaseTypeProp.equals("local")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.local.codebase.url"); //NOI18N
        } else if (codebaseTypeProp.equals("web")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.codebase.url");  //NOI18N // property in project.properties
        } else if (codebaseTypeProp.equals("user")) { //NOI18N
            codebaseProp = getProject().getProperty("jnlp.codebase.user");  //NOI18N // property in project.properties
        }
        log("jnlp.codebase.url = " + codebaseProp, Project.MSG_VERBOSE); //NOI18N

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //NOI18N
        if (codebase == null || codebase.startsWith("${")) { //NOI18N
            if (codebaseTypeProp == null || codebaseTypeProp.equals("no.codebase") || codebaseProp == null) { //NOI18N
                writer.println("<jnlp href=\"" + compName + ".jnlp\" spec=\"1.0+\">"); //NOI18N
            } else if (codebaseProp != null) {
                writer.println("<jnlp codebase=\"" + codebaseProp + "\" href=\"" + compName + ".jnlp\" spec=\"1.0+\">"); //NOI18N
            }
        } else {
            writer.println("<jnlp codebase=\"" + codebase + "\" href=\"" + compName + ".jnlp\" spec=\"1.0+\">"); //NOI18N
        }
        writer.println("    <information>"); //NOI18N
        writer.println("        <title>" + compName + "</title>"); //NOI18N
        writer.println("        <vendor>" + concatSet(aliases) + "</vendor>"); //NOI18N
        writer.println("    </information>"); //NOI18N
        writer.println("    <security>"); //NOI18N
        writer.println("        <all-permissions/>"); //NOI18N
        writer.println("    </security>"); //NOI18N
        writer.println("    <resources>"); //NOI18N
        for (Iterator<File> iter = jars.iterator(); iter.hasNext(); ) {
            writer.println("        <jar href=\"" + getPath(destDir.getAbsolutePath(), iter.next().getAbsolutePath()) + "\" download=\"eager\"/>"); //NOI18N
        }
        writer.println("    </resources>"); //NOI18N
        writer.println("    <component-desc/>"); //NOI18N
        writer.println("</jnlp>"); //NOI18N
        writer.flush();
        writer.close();
        
        return f.getAbsolutePath();
    
    }
    
    /* Returns the result after subtraction of filePath - dirPath
     */ 
    private String getPath(String dirPath, String filePath) {
        String retVal = null;
        if (filePath.indexOf(dirPath) != -1) {
            retVal = (filePath.substring(dirPath.length() + 1).replace('\\', '/'));
        }
        return retVal;
    }
    
    /* Returns the concatenation of set items, separated by commas
     */
    private String concatSet(Set<String> s) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Iterator iter = s.iterator(); iter.hasNext(); ) {
            if (first) {
                sb.append(iter.next());
            } else {
                sb.append(", " + iter.next()); //NOI18N
            }
        }
        return sb.toString();
    }

    private static void extendLibrariesManifests(
        final Project prj,
        final File mainJar,
        final List<? extends File> libraries) throws IOException {
        String codebase = null;
        String permissions = null;
        String appName = null;
        final JarFile jf = new JarFile(mainJar);
        try {
            final java.util.jar.Manifest mf = jf.getManifest();
            if (mf != null) {
                final Attributes attrs = mf.getMainAttributes();
                codebase = attrs.getValue(ATTR_CODEBASE);
                permissions = attrs.getValue(ATTR_PERMISSIONS);
                appName = attrs.getValue(ATTR_APPLICATION_NAME);
            }
        } finally {
            jf.close();
        }
        prj.log(
            String.format(
                "Application: %s manifest: Codebase: %s, Permissions: %s, Application-Name: %s",    //NOI18N
                safeRelativePath(prj.getBaseDir(), mainJar),
                codebase,
                permissions,
                appName),
            Project.MSG_VERBOSE);
        if (codebase != null || permissions != null || appName != null) {
            for (File library : libraries) {
                try {
                    extendLibraryManifest(prj, library, codebase, permissions, appName);
                } catch (ManifestException mex) {
                    throw new IOException(mex);
                }
            }
        }
    }

    private static void extendLibraryManifest(
        final Project prj,
        final File library,
        final String codebase,
        final String permissions,
        final String appName) throws IOException, ManifestException {
        org.apache.tools.ant.taskdefs.Manifest manifest = null;
        Move mv = new Move();
        final File tmpFile = new File(String.format("%s.tmp", library.getAbsolutePath()));
        mv.setFile(library);
        mv.setTofile(tmpFile);
        mv.execute();
        boolean success = false;
        try {
            final Map<String,String> extendedAttrs = new HashMap<String,String>();
            final org.apache.tools.zip.ZipFile zf = new org.apache.tools.zip.ZipFile(tmpFile);
            try {                
                final org.apache.tools.zip.ZipEntry manifestEntry = zf.getEntry(MANIFEST);
                if (manifestEntry != null) {
                    final Reader in = new InputStreamReader(zf.getInputStream(manifestEntry), Charset.forName(UTF_8));    //NOI18N
                    try {
                        manifest = new org.apache.tools.ant.taskdefs.Manifest(in);
                    } finally {
                        in.close();
                    }
                } else {
                    manifest = new org.apache.tools.ant.taskdefs.Manifest();
                }
                final org.apache.tools.ant.taskdefs.Manifest.Section mainSection = manifest.getMainSection();                
                String attr = mainSection.getAttributeValue(ATTR_CODEBASE);
                if (attr == null) {
                    mainSection.addAttributeAndCheck(new org.apache.tools.ant.taskdefs.Manifest.Attribute(
                        ATTR_CODEBASE,
                        codebase));
                    extendedAttrs.put(ATTR_CODEBASE, codebase);
                }
                attr = mainSection.getAttributeValue(ATTR_PERMISSIONS);
                if (attr == null) {
                    mainSection.addAttributeAndCheck(new org.apache.tools.ant.taskdefs.Manifest.Attribute(
                        ATTR_PERMISSIONS,
                        permissions));
                    extendedAttrs.put(ATTR_PERMISSIONS, permissions);
                }
                attr = mainSection.getAttributeValue(ATTR_APPLICATION_NAME);
                if (attr == null) {
                    mainSection.addAttributeAndCheck(new org.apache.tools.ant.taskdefs.Manifest.Attribute(
                        ATTR_APPLICATION_NAME,
                        appName));
                    extendedAttrs.put(ATTR_APPLICATION_NAME, appName);
                }
                if (!extendedAttrs.isEmpty()) {
                    final Enumeration<? extends org.apache.tools.zip.ZipEntry> zent = zf.getEntries();
                    final ZipOutputStream out = new ZipOutputStream(library);
                    try {
                        while (zent.hasMoreElements()) {
                            final org.apache.tools.zip.ZipEntry entry = zent.nextElement();
                            final InputStream in = zf.getInputStream(entry);
                            try {
                                out.putNextEntry(entry);
                                if (MANIFEST.equals(entry.getName())) {
                                    final PrintWriter manifestOut = new PrintWriter(new OutputStreamWriter(out, Charset.forName(UTF_8)));
                                    manifest.write(manifestOut);
                                    manifestOut.flush();
                                } else {
                                    copy(in,out);
                                }
                            } finally {
                                in.close();
                            }
                        }
                    } finally {
                        out.close();
                    }
                    success = true;
                    final StringBuilder message = new StringBuilder("Updating library ").   //NOI18N
                        append(safeRelativePath(prj.getBaseDir(), library)).
                        append(" manifest");    //NOI18N
                    for (Map.Entry<String,String> e : extendedAttrs.entrySet()) {
                        message.append(String.format(" %s: %s,", e.getKey(), e.getValue()));
                    }
                    message.deleteCharAt(message.length()-1);
                    prj.log(message.toString(), Project.MSG_VERBOSE);
                }
            } finally {
                zf.close();
            }
        } finally {
            if (success) {
                final Delete del = new Delete();
                del.setFile(tmpFile);
                del.execute();
            } else {
                final Delete rm = new Delete();
                rm.setFile(library);
                rm.setQuiet(true);
                rm.execute();
                mv = new Move();
                mv.setFile(tmpFile);
                mv.setTofile(library);
                mv.execute();
            }
        }
    }

    private static void copy(final InputStream in, final OutputStream out) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;
        for (;;) {
            len = in.read(BUFFER);
            if (len == -1) {
                return;
            }
            out.write(BUFFER, 0, len);
        }
    }

    private static String safeRelativePath(File from, File to) {
        try {
            return FileUtils.getRelativePath(from, to);
        } catch (Exception ex) {
            return to.getAbsolutePath();
        }
    }
    
}
