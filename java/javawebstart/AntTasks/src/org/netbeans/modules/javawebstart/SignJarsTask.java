/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javawebstart;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;

/**
 * Version of this file is bundled in 6.5 for backward compatibility
 * @author Milan Kubec
 */
public class SignJarsTask extends Task {
    
    private static final String SIG_START = "META-INF/";
    private static final String SIG_END = ".SF";
    
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
            log("Processing FileSet: " + fs, Project.MSG_VERBOSE);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (String f : files) {
                try {
                    File fl = new File(basedir, f);
                    Set<String> sgs = getSignatures(fl);
                    if (sgs.size() == 0) {
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
        
        log("Files to be signed: " + mainJar.toString() + ", " + files2sign.toString(), Project.MSG_VERBOSE);
        
        // for already signed files generate component jnlp file
        log("Files already signed by requested alias: " + alreadySigned.toString(), Project.MSG_VERBOSE);
        
        StringBuilder signedJarsBuilder = new StringBuilder();
        SignJar signJar = (SignJar) getProject().createTask("signjar");
        signJar.setLocation(getLocation());
        signJar.setKeystore(keystore);
        signJar.setStorepass(storepass);
        signJar.setKeypass(keypass);
        signJar.setAlias(alias);
        signJar.init();
        
        // test main jar if its already signed with passed alias ??
        log("Signing main jar file: " + mainJar, Project.MSG_VERBOSE);
        signJar.setJar(mainJar);
        signJar.execute();
        
        if (files2sign.size() > 0) {
            for (Iterator<File> iter = files2sign.iterator(); iter.hasNext(); ) {
                File f = iter.next();
                log("Signing file: " + f, Project.MSG_VERBOSE);
                signJar.setJar(f);
                signJar.execute();
                signedJarsBuilder.append("\n        <jar href=\"lib/" + f.getName() + "\" download=\"eager\"/>"); // XXX lib ?
            }
        }
        
        if (alreadySigned.size() > 0) {
            for (Iterator<File> iter = alreadySigned.iterator(); iter.hasNext(); ) {
                File f = iter.next();
                log("Adding signed file: " + f, Project.MSG_VERBOSE);
                signedJarsBuilder.append("\n        <jar href=\"lib/" + f.getName() + "\" download=\"eager\"/>"); // XXX lib ?
            }
        }
        
        getProject().setProperty(signedJarsProp, signedJarsBuilder.toString());
        
        StringBuilder compsBuilder = new StringBuilder();
        for (Iterator<Entry<Set<String>,List<File>>> iter = signersMap.entrySet().iterator(); iter.hasNext(); ) {
            Entry<Set<String>,List<File>> entry = iter.next();
            log("Already signed: keystore aliases = " + entry.getKey() + " -> signed jars = " + entry.getValue(), Project.MSG_VERBOSE);
            
            String compName = "jnlpcomponent" + compIndex++;
            createJNLPComponentFile(entry.getKey(), entry.getValue(), compName);
            compsBuilder.append("\n        <extension name=\"" + compName + "\" href=\"" + compName + ".jnlp\"/>");
        }
        
        getProject().setProperty(compProp, compsBuilder.toString());
        
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
        
        File f = new File(destDir, compName + ".jnlp");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(f);
        } catch (IOException ioe) {
            throw new BuildException(ioe, getLocation());
        }
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<jnlp spec=\"1.0+\" codebase=\"" + codebase + "\">");
        writer.println("    <information>");
        writer.println("        <title>" + compName + "</title>");
        writer.println("        <vendor>" + concatSet(aliases) + "</vendor>");
        writer.println("    </information>");
        writer.println("    <security>");
        writer.println("        <all-permissions/>");
        writer.println("    </security>");
        writer.println("    <resources>");
        for (Iterator<File> iter = jars.iterator(); iter.hasNext(); ) {
            writer.println("        <jar href=\"" + getPath(destDir.getAbsolutePath(), iter.next().getAbsolutePath()) + "\" download=\"eager\"/>");
        }
        writer.println("    </resources>");
        writer.println("    <component-desc/>");
        writer.println("</jnlp>");
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
                sb.append(", " + iter.next());
            }
        }
        return sb.toString();
    }
    
}
