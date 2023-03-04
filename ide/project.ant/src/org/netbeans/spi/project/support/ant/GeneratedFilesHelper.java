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

package org.netbeans.spi.project.support.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ant.AntBuildExtender.Extension;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation.UserQuestionExceptionCallback;
import org.netbeans.spi.project.ant.GeneratedFilesInterceptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helps a project type (re-)generate, and manage the state and versioning of,
 * <code>build.xml</code> and <code>build-impl.xml</code>.
 * @author Jesse Glick
 */
public final class GeneratedFilesHelper {
    
    /**
     * Relative path from project directory to the user build script,
     * <code>build.xml</code>.
     */
    public static final String BUILD_XML_PATH = "build.xml"; // NOI18N
    
    /**
     * Relative path from project directory to the implementation build script,
     * <code>build-impl.xml</code>.
     */
    public static final String BUILD_IMPL_XML_PATH = "nbproject/build-impl.xml"; // NOI18N
    
    /**
     * Path to file storing information about generated files.
     * It should be kept in version control, since it applies equally after a fresh
     * project checkout; it does not apply to running Ant, so should not be in
     * <code>project.properties</code>; and it includes a CRC of <code>project.xml</code>
     * so it cannot be in that file either. It could be stored in some special
     * comment at the end of the build script (e.g.) but many users would just
     * compulsively delete it in this case since it looks weird.
     */
    static final String GENFILES_PROPERTIES_PATH = "nbproject/genfiles.properties"; // NOI18N
    
    /** Suffix (after script path) of {@link #GENFILES_PROPERTIES_PATH} key for <code>project.xml</code> CRC. */
    private static final String KEY_SUFFIX_DATA_CRC = ".data.CRC32"; // NOI18N
    
    /**
     * Suffix (after script path) of {@link #GENFILES_PROPERTIES_PATH} key for stylesheet CRC.
     * May also include a specification version after an '@'.
     */
    private static final String KEY_SUFFIX_STYLESHEET_CRC_PLUS_VERSION = ".stylesheet.CRC32"; // NOI18N
    
    /** Suffix (after script path) of {@link #GENFILES_PROPERTIES_PATH} key for CRC of the script itself. */
    private static final String KEY_SUFFIX_SCRIPT_CRC = ".script.CRC32"; // NOI18N
    
    /**
     * A build script is missing from disk.
     * This is mutually exclusive with the other flags.
     * @see #getBuildScriptState
     */
    public static final int FLAG_MISSING = 2 << 0;
    
    /**
     * A build script has been modified since last generated by
     * {@link #generateBuildScriptFromStylesheet}.
     * <p class="nonnormative">
     * Probably this means it was edited by the user.
     * </p>
     * @see #getBuildScriptState
     */
    public static final int FLAG_MODIFIED = 2 << 1;
    
    /**
     * A build script was generated from an older version of <code>project.xml</code>.
     * It was last generated using a different version of <code>project.xml</code>,
     * so using the current <code>project.xml</code> might produce a different
     * result.
     * <p class="nonnormative">
     * This is quite likely in the case of
     * <code>build.xml</code>; in the case of <code>build-impl.xml</code>, it
     * probably means that the user edited <code>project.xml</code> manually,
     * since if that were modified from {@link AntProjectHelper} methods and
     * the project were saved, the script would have been regenerated
     * already.
     * </p>
     * @see #getBuildScriptState
     */
    public static final int FLAG_OLD_PROJECT_XML = 2 << 2;
    
    /**
     * A build script was generated from an older version of a stylesheet.
     * It was last generated using a different (probably older) version of the
     * XSLT stylesheet, so using the current stylesheet might produce a different
     * result.
     * <p class="nonnormative">
     * Probably this means the project type
     * provider module has been upgraded since the project was last saved (in
     * the case of <code>build-impl.xml</code>) or created (in the case of
     * <code>build.xml</code>).
     * </p>
     * <p>
     * Since org.netbeans.modules.project.ant/1 1.28, the code takes extra steps
     * to prevent "seesawing", or overwriting a script generated by a new
     * stylesheet when using an old stylesheet: the CRC of the stylesheet is
     * augmented wherever possible with the specification version of the module
     * containing that stylesheet.
     * </p>
     * @see #getBuildScriptState
     */
    public static final int FLAG_OLD_STYLESHEET = 2 << 3;

    /**
     * The build script exists, but nothing else is known about it.
     * This flag is mutually exclusive with {@link #FLAG_MISSING} but
     * when set also sets {@link #FLAG_MODIFIED}, {@link #FLAG_OLD_STYLESHEET},
     * and {@link #FLAG_OLD_PROJECT_XML} - since it is not known whether these
     * conditions might obtain, it is safest to assume they do.
     * <p class="nonnormative">
     * Probably this means that <code>nbproject/genfiles.properties</code> was
     * deleted by the user.
     * </p>
     * @see #getBuildScriptState
     */
    public static final int FLAG_UNKNOWN = 2 << 4;
    
    /** Associated project helper, or null if using only a directory. */
    private final AntProjectHelper h;
    
    /** Project directory. */
    private final FileObject dir;
    
    private AntBuildExtender extender;
    
    /**
     * Create a helper based on the supplied project helper handle.
     * @param h an Ant-based project helper supplied to the project type provider
     */
    public GeneratedFilesHelper(AntProjectHelper h) {
        this.h = h;
        dir = h.getProjectDirectory();
    }

    /**
     * Create a helper based on the supplied project helper handle. The created
     * instance is capable of extending the generated files with 3rd party content.
     * @param h an Ant-based project helper supplied to the project type provider
     * @param ex build extensibility support
     * @since org.netbeans.modules.project.ant 1.16
     * 
     */
    public GeneratedFilesHelper(AntProjectHelper h, AntBuildExtender ex) {
        this(h);
        extender = ex;
    }
    
    /**
     * Create a helper based only on a project directory.
     * This can be used to perform the basic refresh functionality
     * without being the owner of the project.
     * It is only intended for use from the offline Ant task to
     * refresh a project, and similar special situations.
     * For normal circumstances please use only
     * {@link GeneratedFilesHelper#GeneratedFilesHelper(AntProjectHelper)}.
     * @param d the project directory
     * @throws IllegalArgumentException if the supplied directory has no <code>project.xml</code>
     */
    public GeneratedFilesHelper(FileObject d) {
        if (d == null || !d.isFolder() || d.getFileObject(AntProjectHelper.PROJECT_XML_PATH) == null) {
            throw new IllegalArgumentException("Does not look like an Ant-based project: " + d); // NOI18N
        }
        h = null;
        dir = d;
    }
    
    /** Workaround for JAXP bug 7150637 / XALANJ-1497. */
    private static final String ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";
    /**
     * Create <code>build.xml</code> or <code>nbproject/build-impl.xml</code>
     * from <code>project.xml</code> plus a supplied XSLT stylesheet.
     * This is the recommended way to create the build scripts from
     * project metadata.
     * <p class="nonnormative">
     * You may wish to first check {@link #getBuildScriptState} to decide whether
     * you really want to overwrite an existing build script. Typically if you
     * find {@link #FLAG_MODIFIED} you should not overwrite it; or ask for confirmation
     * first; or make a backup. Of course if you find neither of {@link #FLAG_OLD_STYLESHEET}
     * nor {@link #FLAG_OLD_PROJECT_XML} then there is no reason to overwrite the
     * script to begin with.
     * </p>
     * <p>
     * Acquires write access.
     * </p>
     * @param path a project-relative file path such as {@link #BUILD_XML_PATH} or {@link #BUILD_IMPL_XML_PATH}
     * @param stylesheet a URL to an XSLT stylesheet accepting <code>project.xml</code>
     *                   as input and producing the build script as output
     * @throws IOException if transforming or writing the output failed
     */
    public void generateBuildScriptFromStylesheet(final String path, final URL stylesheet) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Null path"); // NOI18N
        }
        if (stylesheet == null) {
            throw new IllegalArgumentException("Null stylesheet"); // NOI18N
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    if (h != null) {
                        h.ensureProjectXmlUnmodified("Cannot generate build scripts from a modified project", true); // NOI18N
                    }
                    // Need to use an atomic action since otherwise creating new build scripts might
                    // cause them to not be recognized as Ant scripts.
                    dir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileObject projectXml = dir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                            if (projectXml == null) {
                                throw new IOException("Missing project metadata: " + h.resolveFile(AntProjectHelper.PROJECT_XML_PATH)); // NOI18N
                            }                            
                            byte[] projectXmlData;
                            InputStream is = projectXml.getInputStream();
                            try {
                                projectXmlData = load(is);
                            } finally {
                                is.close();
                            }
                            byte[] stylesheetData;
                            is = stylesheet.openStream();
                            try {
                                stylesheetData = load(is);
                            } finally {
                                is.close();
                            }
                            final byte[] resultData;
                            TransformerFactory tf = TransformerFactory.newInstance();
                            try {
                                StreamSource stylesheetSource = new StreamSource(
                                    new ByteArrayInputStream(stylesheetData), stylesheet.toExternalForm());
                                Transformer t = tf.newTransformer(stylesheetSource);
                                try {
                                    t.setOutputProperty(ORACLE_IS_STANDALONE, "yes");
                                } catch (IllegalArgumentException x) {
                                    // fine, introduced in JDK 7u4
                                }
                                File projectXmlF = FileUtil.toFile(projectXml);
                                assert projectXmlF != null;
                                StreamSource projectXmlSource = new StreamSource(
                                    new ByteArrayInputStream(projectXmlData), BaseUtilities.toURI(projectXmlF).toString());
                                ByteArrayOutputStream result = new ByteArrayOutputStream();
                                t.transform(projectXmlSource, new StreamResult(result));
                                if (BUILD_IMPL_XML_PATH.equals(path)) {
                                    resultData = applyBuildExtensions(result.toByteArray(), extender);
                                } else {
                                    resultData = result.toByteArray();
                                }
                            } catch (TransformerException e) {
                                throw (IOException)new IOException(e.toString()).initCause(e);
                            }
                            // Update genfiles.properties too.
                            final EditableProperties p = new EditableProperties(true);
                            FileObject genfiles = dir.getFileObject(GENFILES_PROPERTIES_PATH);
                            if (genfiles != null && genfiles.isVirtual()) {
                                // #55164: deleted from CVS?
                                genfiles = null;
                            }
                            if (genfiles != null) {
                                is = genfiles.getInputStream();
                                try {
                                    p.load(is);
                                } finally {
                                    is.close();
                                }
                            }
                            p.setProperty(path + KEY_SUFFIX_DATA_CRC,
                                getCrc32(new ByteArrayInputStream(projectXmlData), projectXml));
                            if (genfiles == null) {
                                // New file, set a comment on it. XXX this puts comment in middle if write build-impl.xml before build.xml
                                p.setComment(path + KEY_SUFFIX_DATA_CRC, new String[] {
                                    "# " + NbBundle.getMessage(GeneratedFilesHelper.class, "COMMENT_genfiles.properties_1"), // NOI18N
                                    "# " + NbBundle.getMessage(GeneratedFilesHelper.class, "COMMENT_genfiles.properties_2"), // NOI18N
                                }, false);
                            }
                            String crc = getCrc32(new ByteArrayInputStream(stylesheetData), stylesheet);
                            SpecificationVersion v = stylesheetVersion(stylesheet);
                            if (v != null) {
                                crc += "@" + v; // NOI18N
                            }
                            p.setProperty(path + KEY_SUFFIX_STYLESHEET_CRC_PLUS_VERSION, crc);
                            p.setProperty(path + KEY_SUFFIX_SCRIPT_CRC,
                                computeCrc32(new ByteArrayInputStream(resultData)));
                            if (genfiles == null) {
                                genfiles = FileUtil.createData(dir, GENFILES_PROPERTIES_PATH);
                            }
                            final FileObject _genfiles = genfiles;
                            // You get the Spaghetti Code Award if you can follow the control flow in this method!
                            // Now is the time when you wish Java implemented call/cc.
                            // If you didn't understand that last comment, you don't get the Spaghetti Code Award.
                            final FileSystem.AtomicAction body = new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    // Try to acquire both locks together, since we need them both written.
                                    final FileObject buildScriptXml = FileUtil.createData(dir, path);
                                    assert buildScriptXml.isValid();
                                    FileLock lock1 = buildScriptXml.lock();
                                    try {
                                        FileLock lock2 = _genfiles.lock();
                                        try {
                                            OutputStream os1 = new EolFilterOutputStream(buildScriptXml.getOutputStream(lock1));
                                            try {
                                                OutputStream os2 = _genfiles.getOutputStream(lock2);
                                                try {
                                                    os1.write(resultData);
                                                    p.store(os2);
                                                } finally {
                                                    os2.close();
                                                }
                                            } finally {
                                                os1.close();
                                            }
                                        } finally {
                                            lock2.releaseLock();
                                        }
                                    } finally {
                                        lock1.releaseLock();
                                    }
                                    callInterceptors(dir, path);
                                }
                            };
                            try {
                                body.run();
                            } catch (IOException ioe) {
                                if(!ProjectIDEServices.isUserQuestionException(ioe)) {
                                    throw ioe;
                                }
                                // #57480: need to regenerate build-impl.xml, really.
                                ProjectIDEServices.handleUserQuestionException(ioe, new UserQuestionExceptionCallback() {
                                    public void accepted() {
                                        // Try again.
                                        try {
                                            body.run();
                                        } catch (IOException ioe2) {
                                            if(!ProjectIDEServices.isUserQuestionException(ioe2)) {
                                                Logger.getLogger(GeneratedFilesHelper.this.getClass().getName()).log(Level.SEVERE, null, ioe2);
                                            }                                            
                                            // Need to try one more time - may have locked bSX but not yet gf.
                                            ProjectIDEServices.handleUserQuestionException(ioe2, new UserQuestionExceptionCallback() {
                                                public void accepted() {
                                                    try {
                                                        body.run();
                                                    } catch (IOException e) {
                                                        Logger.getLogger(GeneratedFilesHelper.this.getClass().getName()).log(Level.SEVERE, null, e);
                                                    }
                                                }
                                                public void denied() {}
                                                public void error(IOException e) {
                                                    Logger.getLogger(GeneratedFilesHelper.this.getClass().getName()).log(Level.SEVERE, null, e);
                                                }
                                            });
                                        } 
                                    }
                                    public void denied() {
                                        // OK, skip it.
                                    }
                                    public void error(IOException e) {
                                        Logger.getLogger(GeneratedFilesHelper.this.getClass().getName()).log(Level.SEVERE, null, e);
                                        // Never mind.
                                    }
                                });
                            }
                        }
                    });
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    private byte[] applyBuildExtensions(byte[] resultData, AntBuildExtender ext) {
        if (ext == null) {
            return resultData;
        }
        try {
            ByteArrayInputStream in2 = new ByteArrayInputStream(resultData);
            InputSource is = new InputSource(in2);

            Document doc = XMLUtil.parse(is, false, true, null, null);
            Element el = doc.getDocumentElement();
            Node firstSubnode = el.getFirstChild();
            //TODO check if first one is text and use it as indentation..
            for (Extension extension : AntBuildExtenderAccessor.DEFAULT.getExtensions(ext)) {
                Text after = doc.createTextNode("\n");
                el.insertBefore(after, firstSubnode);
                Element imp = createImportElement(AntBuildExtenderAccessor.DEFAULT.getPath(extension), doc);
                el.insertBefore(imp, after);
                Text before = doc.createTextNode("    ");
                el.insertBefore(before, imp);
                firstSubnode = before;
                NodeList nl = el.getElementsByTagName("target"); //NOI18N
                Map<String, Collection<String>> deps  = AntBuildExtenderAccessor.DEFAULT.getDependencies(extension);
                for (String targetName : deps.keySet()) {
                    Element targetEl = null;
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element elem = (Element)nl.item(i);
                        String at = elem.getAttribute("name"); //NOI18N
                        if (at != null && at.equals(targetName)) {
                            targetEl = elem;
                            break;
                        }
                    }
//                    System.out.println("target name=" + targetName);
//                    System.out.println("target elem=" + targetEl);
                    if (targetEl != null) {
                        Attr depend = targetEl.getAttributeNode("depends"); //NOI18N
                        if (depend == null) {
                            depend = doc.createAttribute("depends"); //NOI18N
                            depend.setValue("");
                            targetEl.setAttributeNode(depend);
                        }
                        String oldVal = depend.getValue();
                        for (String targ : deps.get(targetName)) {
                            oldVal = oldVal + "," + targ; //NOI18N
                        }
                        if (oldVal.startsWith(",")) { //NOI18N
                            oldVal = oldVal.substring(1);
                        }
                        depend.setValue(oldVal);
                    } else {
                        //TODO log??
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLUtil.write(doc, out, "UTF-8"); //NOI18N
            return out.toByteArray();
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return resultData;
        }
        catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
            return resultData;
        }
    }

    private Element createImportElement(String path, Document doc) {
        Element el = doc.createElement("import"); //NOI18N
        el.setAttribute("file", path);
        return el;
    }

    /**
     * Load data from a stream into a buffer.
     */
    private static byte[] load(InputStream is) throws IOException {
        int size = Math.max(1024, is.available()); // #46235
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = is.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
    
    /**
     * Find what state a build script is in.
     * This may be used by a project type provider to decide whether to create
     * or overwrite it, and whether to produce a backup in the latter case.
     * Various abnormal conditions are detected:
     * {@link #FLAG_MISSING}, {@link #FLAG_MODIFIED}, {@link #FLAG_OLD_PROJECT_XML},
     * {@link #FLAG_OLD_STYLESHEET}, and {@link #FLAG_UNKNOWN}.
     * <p class="nonnormative">
     * Currently {@link #FLAG_MODIFIED}, {@link #FLAG_OLD_STYLESHEET}, and
     * {@link #FLAG_OLD_PROJECT_XML} are detected by computing a CRC-32
     * of the script when it is created, as well as the CRC-32s of the
     * stylesheet and <code>project.xml</code>. These CRCs are stored
     * in a special file <code>nbproject/genfiles.properties</code>.
     * The CRCs are based on the textual
     * contents of the files (so even changed to whitespace etc. are considered
     * changes), but are independent of platform newline conventions (since e.g.
     * CVS will by default replace \n with \r\n when checking out on Windows).
     * Changes to external files included into <code>project.xml</code> or the
     * stylesheet (e.g. using XSLT's import facility) are <em>not</em> detected.
     * </p>
     * <p>
     * If there is some kind of I/O error reading any files, {@link #FLAG_UNKNOWN}
     * is returned (in conjunction with {@link #FLAG_MODIFIED},
     * {@link #FLAG_OLD_STYLESHEET}, and {@link #FLAG_OLD_PROJECT_XML} to be safe).
     * </p>
     * <p>
     * Acquires read access.
     * </p>
     * @param path a project-relative path such as {@link #BUILD_XML_PATH} or {@link #BUILD_IMPL_XML_PATH}
     * @param stylesheet a URL to an XSLT stylesheet accepting <code>project.xml</code>
     *                   as input and producing the build script as output
     *                   (should match that given to {@link #generateBuildScriptFromStylesheet})
     * @return a bitwise OR of various flags, or <code>0</code> if the script
     *         is present on disk and fully up-to-date
     */
    public int getBuildScriptState(final String path, final URL stylesheet) {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Integer>() {
                public Integer run() throws IOException {
                    if (h != null) {
                        h.ensureProjectXmlUnmodified("Cannot generate build scripts from a modified project", false); // NOI18N
                    }
                    FileObject script = dir.getFileObject(path);
                    if (script == null || /* #55164 */script.isVirtual()) {
                        return FLAG_MISSING;
                    }
                    int flags = 0;
                    Properties p = new Properties();
                    FileObject genfiles = dir.getFileObject(GENFILES_PROPERTIES_PATH);
                    if (genfiles == null || /* #55164 */genfiles.isVirtual()) {
                        // Who knows? User deleted it; anything might be wrong. Safest to assume
                        // that everything is.
                        return FLAG_UNKNOWN | FLAG_MODIFIED |
                                FLAG_OLD_PROJECT_XML | FLAG_OLD_STYLESHEET;
                    }
                    InputStream is = new BufferedInputStream(genfiles.getInputStream());
                    try {
                        p.load(is);
                    } finally {
                        is.close();
                    }
                    FileObject projectXml = dir.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                    if (projectXml != null && /* #55164 */!projectXml.isVirtual()) {
                        String crc = getCrc32(projectXml);
                        if (!crc.equals(p.getProperty(path + KEY_SUFFIX_DATA_CRC))) {
                            flags |= FLAG_OLD_PROJECT_XML;
                        }
                    } else {
                        // Broken project?!
                        flags |= FLAG_OLD_PROJECT_XML;
                    }
                    String crc = getCrc32(stylesheet);
                    String oldCrcAndV = p.getProperty(path + KEY_SUFFIX_STYLESHEET_CRC_PLUS_VERSION);
                    if (oldCrcAndV == null) {
                        flags |= FLAG_OLD_STYLESHEET;
                    } else if (oldCrcAndV.indexOf('@') != -1) { // NOI18N
                        String[] split = oldCrcAndV.split("@", 2); // NOI18N
                        if (!crc.equals(split[0])) {
                            SpecificationVersion newV = stylesheetVersion(stylesheet);
                            try {
                                if (newV == null || newV.compareTo(new SpecificationVersion(split[1])) >= 0) {
                                    flags |= FLAG_OLD_STYLESHEET;
                                }
                            } catch (NumberFormatException x) { // #212443
                                flags |= FLAG_OLD_STYLESHEET;
                            }
                        }
                    } else if (!crc.equals(oldCrcAndV)) {
                        flags |= FLAG_OLD_STYLESHEET;
                    }
                    crc = getCrc32(script);
                    if (!crc.equals(p.getProperty(path + KEY_SUFFIX_SCRIPT_CRC))) {
                        flags |= FLAG_MODIFIED;
                    }
                    return flags;
                }
            });
        } catch (MutexException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
            return FLAG_UNKNOWN | FLAG_MODIFIED | FLAG_OLD_PROJECT_XML | FLAG_OLD_STYLESHEET;
        }
    }
    
    /**
     * Compute the CRC-32 of the contents of a stream.
     * \r\n and \r are both normalized to \n for purposes of the calculation.
     */
    static String computeCrc32(InputStream is) throws IOException {
        Checksum crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = is.read()) != -1) {
            if (curr != '\n' && last == '\r') {
                crc.update('\n');
            }
            if (curr != '\r') {
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {
            crc.update('\n');
        }
        int val = (int)crc.getValue();
        String hex = Integer.toHexString(val);
        while (hex.length() < 8) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }
    
    // #50440 - cache CRC32's for various files to save time esp. during startup.
    
    private static final Map<URL,String> crcCache = new HashMap<URL,String>();
    private static final Map<URL,Long> crcCacheTimestampsXorSizes = new HashMap<URL,Long>();

    /** Try to find a CRC in the cache according to location of file and last mod time xor size. */
    private static synchronized String findCachedCrc32(URL u, long footprint) {
        String crc = crcCache.get(u);
        if (crc != null) {
            Long l = crcCacheTimestampsXorSizes.get(u);
            assert l != null;
            if (l == footprint) {
                // Cache hit.
                return crc;
            }
        }
        // Cache miss - missing or old.
        return null;
    }
    
    /** Cache a known CRC for a file, using current last mod time xor size. */
    private static synchronized void cacheCrc32(String crc, URL u, long footprint) {
        crcCache.put(u, crc);
        crcCacheTimestampsXorSizes.put(u, footprint);
    }
    
    /** Find (maybe cached) CRC for a file, using a preexisting input stream (not closed by this method). */
    private static String getCrc32(InputStream is, FileObject fo) throws IOException {
        URL u = fo.toURL();
        fo.refresh(); // in case was written on disk and we did not notice yet...
        long footprint = fo.lastModified().getTime() ^ fo.getSize();
        String crc = findCachedCrc32(u, footprint);
        if (crc == null) {
            crc = computeCrc32(is);
            cacheCrc32(crc, u, footprint);
        }
        return crc;
    }

    /** Find the time the file this URL represents was last modified xor its size, if possible. */
    private static long checkFootprint(URL u) {
        File f = FileUtil.archiveOrDirForURL(u);
        if (f != null) {
            return f.lastModified() ^ f.length();
        } else {
            return 0L;
        }
    }
    
    /** Find (maybe cached) CRC for a URL, using a preexisting input stream (not closed by this method). */
    private static String getCrc32(InputStream is, URL u) throws IOException {
        long footprint = checkFootprint(u);
        String crc = null;
        if (footprint != 0L) {
            crc = findCachedCrc32(u, footprint);
        }
        if (crc == null) {
            crc = computeCrc32(is);
            if (footprint != 0L) {
                cacheCrc32(crc, u, footprint);
            }
        }
        return crc;
    }
    
    /** Find (maybe cached) CRC for a file. Will open its own input stream. */
    private static String getCrc32(FileObject fo) throws IOException {
        URL u = fo.toURL();
        fo.refresh();
        long footprint = fo.lastModified().getTime() ^ fo.getSize();
        String crc = findCachedCrc32(u, footprint);
        if (crc == null) {
            InputStream is = fo.getInputStream();
            try {
                crc = computeCrc32(new BufferedInputStream(is));
                cacheCrc32(crc, u, footprint);
            } finally {
                is.close();
            }
        }
        return crc;
    }
    
    /** Find (maybe cached) CRC for a URL. Will open its own input stream. */
    private static String getCrc32(URL u) throws IOException {
        long footprint = checkFootprint(u);
        String crc = null;
        if (footprint != 0L) {
            crc = findCachedCrc32(u, footprint);
        }
        if (crc == null) {
            InputStream is = u.openStream();
            try {
                crc = computeCrc32(new BufferedInputStream(is));
                if (footprint != 0L) {
                    cacheCrc32(crc, u, footprint);
                }
            } finally {
                is.close();
            }
        }
        return crc;
    }

    /**
     * Convenience method to refresh a build script if it can and should be.
     * <p>
     * If the script is not modified, and it is either missing, or the flag
     * <code>checkForProjectXmlModified</code> is false, or it is out of date with
     * respect to either <code>project.xml</code> or the stylesheet (or both),
     * it is (re-)generated.
     * </p>
     * <p>
     * Acquires write access.
     * </p>
     * <p class="nonnormative">
     * Typical usage from {@link ProjectXmlSavedHook#projectXmlSaved} is to call
     * this method for both {@link #BUILD_XML_PATH} and {@link #BUILD_IMPL_XML_PATH}
     * with the appropriate stylesheets and with <code>checkForProjectXmlModified</code>
     * false (the script is certainly out of date relative to <code>project.xml</code>).
     * Typical usage from {@link org.netbeans.spi.project.ui.ProjectOpenedHook#projectOpened} is to call
     * this method for both scripts with the appropriate stylesheets and with
     * <code>checkForProjectXmlModified</code> true.
     * </p>
     * @param path a project-relative path such as {@link #BUILD_XML_PATH} or {@link #BUILD_IMPL_XML_PATH}
     * @param stylesheet a URL to an XSLT stylesheet accepting <code>project.xml</code>
     *                   as input and producing the build script as output
     * @param checkForProjectXmlModified true if it is necessary to check whether the
     *                                script is out of date with respect to
     *                                <code>project.xml</code> and/or the stylesheet
     * @return true if the script was in fact regenerated
     * @throws IOException if transforming or writing the output failed
     */
    public boolean refreshBuildScript(final String path, final URL stylesheet, final boolean checkForProjectXmlModified) throws IOException {
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    int flags = getBuildScriptState(path, stylesheet);
                    if (shouldGenerateBuildScript(flags, checkForProjectXmlModified)) {
                        generateBuildScriptFromStylesheet(path, stylesheet);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }

    private static void callInterceptors(
            @NonNull final FileObject dir,
            @NonNull final String path) {
        final Project prj = FileOwnerQuery.getOwner(dir);
        if (prj != null) {
            final Iterable<? extends GeneratedFilesInterceptor> interceptors =
                    prj.getLookup().lookupAll(GeneratedFilesInterceptor.class);
            for (GeneratedFilesInterceptor interceptor : interceptors) {
                interceptor.fileGenerated(prj, path);
            }
        }
    }
    
    private static boolean shouldGenerateBuildScript(int flags, boolean checkForProjectXmlModified) {
        if ((flags & GeneratedFilesHelper.FLAG_MISSING) != 0) {
            // Yes, need it.
            return true;
        }
        if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0) {
            // No, don't overwrite a user build script.
            // XXX modified build-impl.xml probably counts as a serious condition
            // to warn the user about...
            // Modified build.xml is no big deal.
            return false;
        }
        if (!checkForProjectXmlModified) {
            // OK, assume it is out of date.
            return true;
        }
        // Check whether it is in fact out of date.
        return (flags & (GeneratedFilesHelper.FLAG_OLD_PROJECT_XML |
                         GeneratedFilesHelper.FLAG_OLD_STYLESHEET)) != 0;
    }

     // #45373 - workaround: on Windows make sure that all lines end with CRLF.
     // marcow: Use at least some buffered output!
    private static class EolFilterOutputStream extends BufferedOutputStream {

        private boolean isActive = BaseUtilities.isWindows();
        private int last = -1;
        
        public EolFilterOutputStream(OutputStream os) {
            super(os, 4096);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (isActive) {
                for (int i = off; i < off + len; i++) {
                    write(b[i]);
                }
            }
            else {
                super.write(b, off, len);
            }
        }

        @Override
        public void write(int b) throws IOException {
            if (isActive) {
                if (b == '\n' && last != '\r') {
                    super.write('\r');
                }
                last = b;
            }
            super.write(b);
        }

    }

    /**
     * Cache of stylesheet versions.
     * Package accessible for testing.
     */
    static final Map<URL,SpecificationVersion> STYLESHEET_VERSIONS = new HashMap<URL,SpecificationVersion>();
    /**
     * @param stylesheet a stylesheet usually contained in a module
     * @return the specification version of the containing module, or null
     */
    private static synchronized SpecificationVersion stylesheetVersion(URL stylesheet) {
        if (STYLESHEET_VERSIONS.containsKey(stylesheet)) {
            return STYLESHEET_VERSIONS.get(stylesheet);
        }
        String stylesheetS = stylesheet.toString();
        int idx = stylesheetS.indexOf("!/");
        if (idx != -1) {
            try {
                InputStream is = new URL(stylesheetS.substring(0, idx + 2) + "META-INF/MANIFEST.MF").openStream(); // NOI18N
                try {
                    Manifest m = new Manifest(is);
                    String v = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version"); // NOI18N
                    if (v != null) {
                        SpecificationVersion sv = new SpecificationVersion(v);
                        STYLESHEET_VERSIONS.put(stylesheet, sv);
                        return sv;
                    }
                } finally {
                    is.close();
                }
            } catch (Exception x) {
                Logger.getLogger(GeneratedFilesHelper.class.getName()).log(Level.FINE, "Failed to determine spec version for " + stylesheet, x);
            }
        }
        STYLESHEET_VERSIONS.put(stylesheet, null);
        return null;
    }

}
