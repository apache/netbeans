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
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
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
        progress = ProgressHandle.createHandle(Bundle.doc_building());
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
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), StandardCharsets.UTF_8)) { // NOI18N
                writer.append("<!doctype html><html><head><title>AngularJS documentation</title></head><body>");
                Utils.loadURL(url, writer, StandardCharsets.UTF_8);
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
