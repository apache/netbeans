/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javahelp;

import com.sun.java.help.search.Indexer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.javahelp.HelpSetRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class HelpSetRegistrationProcessor extends LayerGeneratingProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(HelpSetRegistration.class.getCanonicalName());
    }

    protected @Override boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (javax.lang.model.element.Element e : roundEnv.getElementsAnnotatedWith(HelpSetRegistration.class)) {
            HelpSetRegistration r = e.getAnnotation(HelpSetRegistration.class);
            String pkg = ((PackageElement) e).getQualifiedName().toString();
            String hs = pkg.replace('.', '/') + '/' + r.helpSet();
            LayerBuilder builder = layer(e);
            builder.file("Services/JavaHelp/" + pkg.replace('.', '-') + ".xml").contents(""
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<!DOCTYPE helpsetref PUBLIC \"-//NetBeans//DTD JavaHelp Help Set Reference 1.0//EN\" \"http://www.netbeans.org/dtds/helpsetref-1_0.dtd\">\n"
                    + "<helpsetref url=\"nbdocs:/" + hs + "\" merge=\"" + r.merge() + "\"/>\n"
                    ).position(r.position()).write();

            Document doc;
            URI loc;
            try {
                loc = builder.validateResource(hs, e, r, "helpSet", false).toUri();
                if (loc.getScheme() == null) {
                    // JDK #6419926: FileObject.toUri() generates URI without schema
                    loc = Utilities.toURI(new File(loc.toString()));
                }
                doc = XMLUtil.parse(new InputSource(loc.toString()), true, false, XMLUtil.defaultErrorHandler(), new EntityResolver() {
                    public @Override InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        if (publicId.equals("-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN")) {
                            return new InputSource(HelpSetRegistrationProcessor.class.getResource("resources/helpset_1_0.dtd").toString());
                        } else if (publicId.equals("-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN")) {
                            return new InputSource(HelpSetRegistrationProcessor.class.getResource("resources/helpset_2_0.dtd").toString());
                        } else {
                            return null;
                        }
                    }
                });
            } catch (IOException x) {
                throw new LayerGenerationException("Could not parse " + hs + ": " + x, e, processingEnv, r, "helpSet");
            } catch (SAXException x) {
                throw new LayerGenerationException("Could not parse " + hs + ": " + x, e, processingEnv, r, "helpSet");
            }
            String searchDir = null;
            for (Element view : XMLUtil.findSubElements(doc.getDocumentElement())) {
                if (!view.getTagName().equals("view")) {
                    continue;
                }
                Element type = XMLUtil.findElement(view, "type", null);
                if ("javax.help.SearchView".equals(XMLUtil.findText(type))) {
                    Element data = XMLUtil.findElement(view, "data", null);
                    if (data.getAttribute("engine").equals("com.sun.java.help.search.DefaultSearchEngine")) {
                        searchDir = XMLUtil.findText(data)/* XXX better to set XML parser to ignore ws */.trim();
                        break;
                    }
                }
            }
            if (searchDir != null) {
                if ("file".equals(loc.getScheme())) {
                    File d = Utilities.toFile(loc).getParentFile();
                    String out = hs.replaceFirst("/[^/]+$", "/") + searchDir + "/";
                    try {
                        File config = File.createTempFile("jhindexer-config", ".txt");
                        try {
                            AtomicInteger cnt = new AtomicInteger();
                            OutputStream os = new FileOutputStream(config);
                            try {
                                PrintWriter pw = new PrintWriter(os);
                                pw.println("IndexRemove " + d + File.separator);
                                scan(d, pw, cnt, new HashSet<String>(Arrays.asList(r.excludes())), "");
                                pw.flush();
                            } finally {
                                os.close();
                            }
                            processingEnv.getMessager().printMessage(Kind.NOTE, "Indexing " + cnt + " HTML files in " + d + " into " + out);
                            File db = createTempFile("jhindexer-out", "");
                            db.delete();
                            db.mkdir();
                            try {
                                Indexer.main(new String[] {
                                    "-c", config.getAbsolutePath(),
                                    "-db", db.getAbsolutePath()
                                });
                            } finally {
                                for (File f : db.listFiles()) {
                                    FileObject dest = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", out + f.getName(), e);
                                    os = dest.openOutputStream();
                                    try {
                                        InputStream is = new FileInputStream(f);
                                        try {
                                            FileUtil.copy(is, os);
                                        } finally {
                                            is.close();
                                        }
                                    } finally {
                                        os.close();
                                    }
                                    f.delete();
                                }
                                db.delete();
                            }
                        } finally {
                            config.delete();
                        }
                    } catch (Exception x) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Could not run indexer: " + x, e);
                    }
                } else {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot run indexer on non-local files: " + loc, e);
                }
            }
            // XXX try to port CheckHelpSets
        }
        return true;
    }

    /** Create a temp file that can be used for turning into directory
     * and setting as database directory for indexing JavaHelp files.
     * 
     * This method was created because default temp directory on some systems
     * (Mac) contains plus sign "+" in its path. Plus signs are replaced with
     * spaces in JavaHelp indexer because of encoding path to URL and later
     * decoding URL back to path.
     * 
     * #201194
     *
     * This is workaround only. Fixed upstream, see
     * http://java.net/jira/browse/JAVAHELP-33.
     * When a new version of JavaHelp with that fix is released (e.g. 2.0_06)
     * and bundled in NetBeans, standard File.createTempFile can be used instead
     * of this method, and createTempFile, replaceTempFile and isUrlCompatible
     * methods can be removed.
     */
    static File createTempFile(String pref, String suff) throws IOException {

        File f = File.createTempFile(pref, suff); //file in default tmp folder
        if (!isUrlCompatible(f)) {
            if (Utilities.isWindows()) {
                f = replaceTempFile(f, "c:\\Temp", pref, suff);         //NOI18N
            } else if (Utilities.isUnix()) {
                f = replaceTempFile(f, "/tmp", pref, suff);             //NOI18N
            }
        }
        return f;
    }

    /** Try to replace invalid temp file with a better one.
     * On success, original temp file is deleted and newly found one is
     * returned. Otherwise, original temp file is returned.
     */
    private static File replaceTempFile(File origTmpFile, String tmpDirPath,
            String pref, String suff) {

        File tmpDir = new File(tmpDirPath);
        if (tmpDir.isDirectory()) {
            File tmpFile;
            int num = 0;
            do {
                String fileName = pref + System.currentTimeMillis()
                        + "_" + (++num) + suff;                         //NOI18N
                tmpFile = new File(tmpDir, fileName);
            } while (tmpFile.exists()); // file to create cannot already exist
            try {
                tmpFile.createNewFile();
            } catch (Exception e) {
                return origTmpFile;
            }
            if (tmpFile.isFile() && isUrlCompatible(tmpFile)) {
                origTmpFile.delete();
                return tmpFile;
            } else {
                tmpFile.delete();
                return origTmpFile;
            }
        } else {
            return origTmpFile;
        }
    }

    /** Test that file path is preserved if it is encoded to to URL and
     * decoded back to path.
     * 
     * This happens internally in the JavaHelp Indexer and it can damage paths 
     * that contain special characters (e.g. plus sign "+").
     */
    static boolean isUrlCompatible(File f) {

        URL baseURL;
        try {
            baseURL = new URL("file", "", f.getAbsolutePath());         //NOI18N
        } catch (MalformedURLException ex) {
            return false;
        }
        String fileFromUrl = baseURL.getFile();
        String decodedFileFromUrl = URLDecoder.decode(fileFromUrl);
        return f.getAbsolutePath().equals(decodedFileFromUrl);
    }

    private static void scan(File d, PrintWriter pw, AtomicInteger cnt, Set<String> excludes, String path) {
        for (File f : d.listFiles()) {
            String name = f.getName();
            if (f.isDirectory()) {
                scan(f, pw, cnt, excludes, path + name + '/');
            } else if (!excludes.contains(path + name) && name.matches(".+[.]html?$")) {
                pw.println("File " + f);
                cnt.incrementAndGet();
            }
        }
    }

}
