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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipOutputStream;

/**
 *
 * @author mkozeny
 */
public class JNLPUpdateManifestStartup extends Task {

    private static final String MANIFEST = "META-INF/MANIFEST.MF";  //NOI18N
    private static final String UTF_8 = "UTF-8";    //NOI18N
    private static final String ATTR_CODEBASE = "Codebase"; //NOI18N
    private static final String ATTR_PERMISSIONS = "Permissions";   //NOI18N
    private static final String ATTR_APPLICATION_NAME = "Application-Name"; //NOI18N

    private File jar;

    public void setJar(File jar) {
        this.jar = jar;
    }

    private File destJar;

    public void setDestJar(File destJar) {
        this.destJar = destJar;
    }

    private String appName;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    private File masterJnlp;

    public void setMasterJnlp(File masterJnlp) {
        this.masterJnlp = masterJnlp;
    }
    
    private final String permissions = "all-permissions";

    private final String codebase = "*";

    private SignJar signTask;

    private SignJar getSignTask() {
        if (signTask == null) {
            signTask = (SignJar) getProject().createTask("signjar");
        }
        return signTask;
    }

    public void setAlias(String a) {
        getSignTask().setAlias(a);
    }

    public void setStorePass(String p) {
        getSignTask().setStorepass(p);
    }

    public void setKeystore(String k) {
        getSignTask().setKeystore(k);
    }

    public void setStoreType(String t) {
        getSignTask().setStoretype(t);
    }

    @Override
    public void execute() throws BuildException {
        File tmpFile = null;
        try {
            if (isSigned(jar) == null) {
                tmpFile = extendLibraryManifest(getProject(), jar, destJar, codebase, permissions, appName);
            }
        } catch (IOException | ManifestException ex) {
            getProject().log(
                    "Failed to extend libraries manifests: " + ex.getMessage(), //NOI18N
                    Project.MSG_WARN);
        }
        if (tmpFile != null) {
            sign(tmpFile, destJar);
            deleteTmpFile(tmpFile);
        } else {
            sign(jar, destJar);
        }
    }

    private File extendLibraryManifest(
            final Project prj,
            final File sourceJar,
            final File signedJar,
            final String codebase,
            final String permissions,
            final String appName) throws IOException, ManifestException {
        org.apache.tools.ant.taskdefs.Manifest manifest = null;
        Copy cp = new Copy();
        File tmpFile = new File(String.format("%s.tmp", signedJar.getAbsolutePath()));
        cp.setFile(sourceJar);
        cp.setTofile(tmpFile);
        cp.execute();
        boolean success = false;
        try {
            final Map<String, String> extendedAttrs = new HashMap<>();
            final org.apache.tools.zip.ZipFile zf = new org.apache.tools.zip.ZipFile(sourceJar);
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
                    final ZipOutputStream out = new ZipOutputStream(tmpFile);
                    try {
                        org.apache.tools.zip.ZipEntry masterJnlpEntry = new org.apache.tools.zip.ZipEntry("JNLP-INF/APPLICATION.JNLP");
                        out.putNextEntry(masterJnlpEntry);
                        FileInputStream masterFis = new FileInputStream(masterJnlp.getAbsolutePath());
                        try {
                            copy(masterFis, out);
                        } finally {
                            masterFis.close();
                        }
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
                                    copy(in, out);
                                }
                            } finally {
                                in.close();
                            }
                        }
                    } finally {
                        out.close();
                    }
                    success = true;
                    final StringBuilder message = new StringBuilder("Updating library "). //NOI18N
                            append(safeRelativePath(prj.getBaseDir(), tmpFile)).
                            append(" manifest");    //NOI18N
                    for (Map.Entry<String, String> e : extendedAttrs.entrySet()) {
                        message.append(String.format(" %s: %s,", e.getKey(), e.getValue()));
                    }
                    message.deleteCharAt(message.length() - 1);
                    prj.log(message.toString(), Project.MSG_VERBOSE);
                }
            } finally {
                zf.close();
            }
        } finally {
            if (!success) {
                final Delete rm = new Delete();
                rm.setFile(tmpFile);
                rm.setQuiet(true);
                rm.execute();
                tmpFile = null;
            }
        }
        return tmpFile;
    }

    private static void deleteTmpFile(File tmpFile) {
        final Delete del = new Delete();
        del.setFile(tmpFile);
        del.execute();
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

    /**
     * return alias if signed, or null if not
     */
    private static String isSigned(File f) throws IOException {
        try (JarFile jar = new JarFile(f)) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                Matcher m = SF.matcher(en.nextElement().getName());
                if (m.matches()) {
                    return m.group(1);
                }
            }
            return null;
        }
    }
    private static final Pattern SF = Pattern.compile("META-INF/(.+)\\.SF");

    /**
     * Signs the given files according to the signJars variable value.
     */
    private void sign(File from, File to) {
        if (!from.exists() && from.getParentFile().getName().equals("locale")) {
            // skip missing locale files, probably the best fix for #103301
            log("Localization file " + from + " is referenced, but cannot be found. Skipping.", Project.MSG_WARN);
            return;
        }
        getSignTask().setJar(from);
        if (to != null) {
            // #125970: might be .../modules/locale/something_ja.jar
            to.getParentFile().mkdirs();
        }
        getSignTask().setSignedjar(to);
        getSignTask().setDigestAlg("SHA1");
        getSignTask().execute();

    }
}
