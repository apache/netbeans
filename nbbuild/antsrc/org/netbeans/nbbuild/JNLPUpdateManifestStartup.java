/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        } catch (IOException ex) {
            getProject().log(
                    "Failed to extend libraries manifests: " + ex.getMessage(), //NOI18N
                    Project.MSG_WARN);
        } catch (ManifestException ex) {
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
            final Map<String, String> extendedAttrs = new HashMap<String, String>();
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
        JarFile jar = new JarFile(f);
        try {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                Matcher m = SF.matcher(en.nextElement().getName());
                if (m.matches()) {
                    return m.group(1);
                }
            }
            return null;
        } finally {
            jar.close();
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
        // use reflection for calling getSignTask().setDigestAlg("SHA1");
        try {
            Class sjClass = Class.forName("org.apache.tools.ant.taskdefs.SignJar");
            Method sdaMethod = sjClass.getDeclaredMethod("setDigestAlg", String.class);
            sdaMethod.invoke(getSignTask(), "SHA1");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(MakeJNLP.class.getName()).log(Level.SEVERE, null, ex);
        }
        // end of getSignTask().setDigestAlg("SHA1");
        getSignTask().execute();

    }
}
