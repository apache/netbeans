/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates the syntax and semantics of one or more JNLP files.
 * Any other JNLP fragments referred to recursively from these files will be validated as well.
 * JNLP files must specify a document type, normally:
 * &lt;!DOCTYPE jnlp PUBLIC "-//Sun Microsystems, Inc//DTD JNLP Descriptor 6.0//EN" "http://java.sun.com/dtd/JNLP-6.0.dtd">
 * The codebase specified in the file is used as is if a file: URL;
 * if $$codebase, it is taken to be the immediately containing directory, to match the behavior of JnlpDownloadServlet;
 * if a remote URL, it is also taken to be the immediately containing directory,
 * since otherwise it would be impossible to validate files which were generated with intent to upload to a server.
 * See issue #96630.
 */
public class VerifyJNLP extends Task {

    private List<FileSet> filesets = new ArrayList<FileSet>();
    /**
     * Add one or more JNLP files to validate.
     * Use &lt;fileset file="..."/> if you have just one.
     * Fragments referred to from higher JNLP files are checked automatically and do not need to be specified.
     */
    public void addConfiguredFileset(FileSet fs) {
        filesets.add(fs);
    }

    private File report;
    /**
     * JUnit report file to create rather than halting build if errors are encountered.
     */
    public void setReport(File report) {
        this.report = report;
    }

    private boolean failOnError = true;
    /**
     * Whether to halt the build if there is a verification error. Default true.
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public @Override void execute() throws BuildException {
        Map<String,String> results = new LinkedHashMap<String,String>();
        for (FileSet fs : filesets) {
            DirectoryScanner s = fs.getDirectoryScanner(getProject());
            File basedir = s.getBasedir();
            for (String incl : s.getIncludedFiles()) {
                validate(new File(basedir, incl), results);
            }
        }
        JUnitReportWriter.writeReport(this, null, failOnError ? null : report, results);
    }

    private static void error(File jnlp, Map<String,String> results, String key, String message) {
        results.put(jnlp + "/test" + key, message);
    }

    private void validate(File jnlp, Map<String,String> results) {
        log("Validating: " + jnlp);
        Document doc;
        try {
            doc = XMLUtil.parse(new InputSource(jnlp.toURI().toString()), true, false, new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    fatalError(exception);
                }
                public void error(SAXParseException exception) throws SAXException {
                    fatalError(exception);
                }
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw new SAXException("parse or validation error:\n" +
                            exception.getSystemId() + ":" + exception.getLineNumber() + ": " + exception.getMessage());
                }
            }, new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if ("-//Sun Microsystems, Inc//DTD JNLP Descriptor 6.0//EN".equals(publicId)) {
                        return new InputSource(VerifyJNLP.class.getResource("JNLP-6.0.dtd").toString());
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception x) {
            error(jnlp, results, "Parse", x.getMessage());
            return;
        }
        String codebase = doc.getDocumentElement().getAttribute("codebase");
        URI base;
        if (codebase.equals("$$codebase")) {
            base = jnlp.getParentFile().toURI();
        } else {
            try {
                base = new URI(codebase);
                if (!base.isAbsolute()) {
                    error(jnlp, results, "Codebase", "non-absolute codebase " + base);
                    return;
                }
                if (!"file".equals(base.getScheme())) {
                    // Needed for local validation of a tree intended for eventual upload to a server.
                    base = jnlp.getParentFile().toURI();
                }
            } catch (URISyntaxException x) {
                error(jnlp, results, "Codebase", "invalid codebase " + codebase + "': " + x);
                return;
            }
        }
        Certificate[] existingCertificates = null;
        File existingSignedJar = null;
        NodeList nl = doc.getElementsByTagName("*");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            String href = el.getAttribute("href");
            if (href.length() > 0) {
                URI u;
                try {
                    u = base.resolve(new URI(href));
                } catch (URISyntaxException x) {
                    error(jnlp, results, "Href", "invalid href '" + href + "': " + x);
                    continue;
                }
                assert u.isAbsolute() : u + " not absolute as " + href + " resolved against " + base;
                if ("file".equals(u.getScheme())) {
                    File f = new File(u);
                    if (!f.isFile()) {
                        if (el.getTagName().equals("icon")) {
                            // jnlp.xml in harness generates <icon href="${app.icon}"/> optimistically.
                            // Does not seem to be a problem if it is missing.
                            log(jnlp + ": warning: no such file " + f, Project.MSG_WARN);
                        } else if (! f.exists() && f.getName().startsWith("locale")) {
                            // skip missing locale files, probably the best fix for #103301 (copied from MakeJNLP.java)
                            log("Localization file " + f + " is referenced, but cannot be found. Skipping.", Project.MSG_WARN);
                        }
                        else {
                            error(jnlp, results, "Href", "no such file " + f);
                            continue;
                        }
                    }
                    if (el.getTagName().equals("extension")) {
                        validate(f, results);
                    } else if (el.getTagName().equals("jar") && f.exists()) {
                        try {
                            JarFile jf = new JarFile(f, true);
                            // Try to find signers.
                            try {
                                Enumeration<JarEntry> entries = jf.entries();
                                while (entries.hasMoreElements()) {
                                    JarEntry entry = entries.nextElement();
                                    if (entry.getName().startsWith("META-INF/")) {
                                        // At least MANIFEST.MF is not signed in the normal way, it seems.
                                        continue;
                                    }
                                    if (entry.getSize() < 1) {
                                        // Dirs are not signed.
                                        continue;
                                    }
                                    InputStream is = jf.getInputStream(entry);
                                    int read = 0;
                                    while (read != -1) {
                                        read = is.read();
                                    }
                                    Certificate[] certs = entry.getCertificates();
                                    /*
                                    System.err.println("existingSignedJar=" + existingSignedJar + " existingCertificates=" + Arrays.toString(existingCertificates) +
                                                       " f=" + f + " certs=" + Arrays.toString(certs) + " entry.name=" + entry.getName());
                                     */
                                    if (existingSignedJar != null && !Arrays.equals(certs, existingCertificates)) {
                                        error(jnlp, results, "Signatures", "different signatures (or signing status) between " + existingSignedJar + " and " + f);
                                        break;
                                    }
                                    existingCertificates = certs;
                                    existingSignedJar = f;
                                    break; // just check one representative file
                                }
                            } finally {
                                jf.close();
                            }
                        } catch (IOException x) {
                            error(jnlp, results, "Signatures", "error examining signatures in " + f + ": " + x);
                        }
                    }
                } else {
                    try {
                        u.toURL().openStream().close();
                    } catch (IOException x) {
                        log(jnlp + ": could not open network URL " + u, Project.MSG_WARN);
                        // Do not halt build; might just be a network connectivity issue.
                    }
                }
            }
        }
    }

}
