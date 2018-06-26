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
package org.netbeans.modules.html.angular;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.html.angular.model.Directive;
import org.openide.modules.Places;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "doc.building=Loading AngularJS Documentation",
    "# {0} - the documentation URL",
    "doc.cannotGet=Cannot load AngularJS documentation from \"{0}\"."
})
public class AngularDoc {

    private static final Logger LOG = Logger.getLogger(AngularDoc.class.getSimpleName()); //NOI18N
    private static RequestProcessor RP = new RequestProcessor(AngularDoc.class);
    private static AngularDoc INSTANCE;
    private boolean loadingStarted;

    private static final String CACHE_FOLDER_NAME = "ajs-doc"; //NOI18N
    public static final String DOC_VERSION = System.getProperty("nb.angular.doc.version", "1.4.0"); //NOI18N;

    public static synchronized AngularDoc getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new AngularDoc();
        }
        return INSTANCE;
    }

    /**
     * Gets an html documentation for the given {@link Directive}.
     *
     * @param directive
     * @return the help or null if the help is not yet loaded
     */
    public String getDirectiveDocumentation(Directive directive) {
        return getDoc(directive);
    }

    public String getFunctionDocumentation(FunctionDocUrl functionDocUrl) {
        return getDoc(functionDocUrl);
    }

    private void startLoading() {
        LOG.fine("start loading doc"); //NOI18N
        Directive[] dirs = Directive.values();
        directives = Enumerations.array(dirs);
        progress = ProgressHandleFactory.createHandle(Bundle.doc_building());
        progress.start(dirs.length);

        buildDoc();
    }

    private File getCacheFile(String name) {
        return Places.getCacheSubfile(new StringBuilder().append(CACHE_FOLDER_NAME).append('/').append(DOC_VERSION).append('/').append(name).toString());
    }

    private String getDoc(Directive directive) {
        try {
            File cacheFile = getCacheFile(directive.name());
            if (!cacheFile.exists()) {
                //load from web and cache locally
                loadDoc(directive, cacheFile);

                //if any of the files is not loaded yet, start the loading process
                if (!loadingStarted) {
                    loadingStarted = true;
                    startLoading();
                }
            }
            return Utils.getFileContent(cacheFile);
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.INFO, "Cannot load AngularJS documentation from \"{0}\".", new Object[]{directive.getExternalDocumentationURL_partial()}); //NOI18N
            return Bundle.doc_cannotGet(directive.getExternalDocumentationURL_partial());
        }

    }

    private String getDoc(FunctionDocUrl functionDocUrl) {
        try {
            File cacheFile = getCacheFile(functionDocUrl.getFunctionName());
            if (!cacheFile.exists()) {
                //load from web and cache locally
                loadDoc(new URL(functionDocUrl.getDocumentationPartialUrl()), cacheFile);
            }
            return Utils.getFileContent(cacheFile);
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.INFO, "Cannot load AngularJS documentation from \"{0}\".", new Object[]{functionDocUrl.getDocumentationPartialUrl()}); //NOI18N
            return null;
        }
    }

    private void loadDoc(Directive directive, File cacheFile) throws URISyntaxException, MalformedURLException, IOException {
        String docURL = directive.getExternalDocumentationURL_partial();
        URL url = new URI(docURL).toURL();
        loadDoc(url, cacheFile);
    }

    private void loadDoc(URL url, File cacheFile) throws URISyntaxException, MalformedURLException, IOException {
        LOG.fine("start loading doc"); //NOI18N
        synchronized (cacheFile) {
            String tmpFileName = cacheFile.getAbsolutePath() + ".tmp";
            File tmpFile = new File(tmpFileName);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8")) { // NOI18N
                writer.append("<!doctype html><html><head><title>AngularJS documentation</title></head><body>");
                Utils.loadURL(url, writer, Charset.forName("UTF-8"));
                writer.append("</body></html>");
                writer.close();
                tmpFile.renameTo(cacheFile);
            } finally {
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            }

        }
    }

    private void buildDoc() {
        if (directives.hasMoreElements()) {
            directive = directives.nextElement();
            getDoc(directive);
            progress.progress(++loaded);

            //start next task
            RP.post(new Runnable() {
                @Override
                public void run() {
                    buildDoc();
                }
            });
        } else {
            //stop loading
            progress.finish();
            progress = null;

            LOG.log(Level.FINE, "Loading doc finished."); //NOI18N
        }
    }
    private Directive directive;
    private Enumeration<Directive> directives;
    private ProgressHandle progress;
    private int loaded = 0;

    public static class FunctionDocUrl {

        private static final String DOC_URL_BASE = "https://docs.angularjs.org/api/ng/function/"; //NOI18N
        private static final String PARTIAL_DOC_URL_BASE = "https://code.angularjs.org/" + DOC_VERSION + "/docs/partials/api/ng/function/"; //NOI18N
        private static final String PARTIAL_SUFFIX = ".html"; //NOI18N

        private final String functionName;

        public String getFunctionName() {
            return functionName;
        }

        public FunctionDocUrl(String functionName) {
            this.functionName = functionName;
        }

        public String getDocumentationUrl() {
            return DOC_URL_BASE + functionName;
        }

        public String getDocumentationPartialUrl() {
            return PARTIAL_DOC_URL_BASE + functionName + PARTIAL_SUFFIX;
        }
    }
}
