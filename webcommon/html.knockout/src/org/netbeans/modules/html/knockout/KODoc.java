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
package org.netbeans.modules.html.knockout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.html.knockout.model.Binding;
import org.openide.modules.Places;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "doc.building=Loading Knockout Documentation",
    "# {0} - the documentation URL",
    "doc.cannotGet=Cannot load Knockout documentation from \"{0}\"."
})
public class KODoc {

    private static final Logger LOG = Logger.getLogger(KODoc.class.getSimpleName()); //NOI18N
    private static RequestProcessor RP = new RequestProcessor(KODoc.class);
    private static KODoc INSTANCE;
    private boolean loadingStarted;
    
    private static final String CACHE_FOLDER_NAME = "knockout-doc"; //NOI18N

    public static synchronized KODoc getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new KODoc();
        }
        return INSTANCE;
    }

    /**
     * Gets an html documentation for the given {@link KOHelpItem}.
     *
     * @param binding
     * @return the help or null if the help is not yet loaded
     */
    public String getDirectiveDocumentation(KOHelpItem binding) {
        return getDoc(binding);
    }

    private void startLoading() {
        LOG.fine("start loading doc"); //NOI18N
        Collection<KOHelpItem> items = new ArrayList<>();
        //add the data-attribute help item
        items.add(KOHtmlExtension.KO_DATA_BIND_HELP_ITEM);
        //add bindings
        items.addAll(Arrays.asList(Binding.values()));
        
        bindings = items.iterator();        
        progress = ProgressHandle.createHandle(Bundle.doc_building());
        progress.start(items.size());

        buildDoc();
    }

    private File getCacheFile(KOHelpItem binding) {
        return Places.getCacheSubfile(new StringBuilder().append(CACHE_FOLDER_NAME).append('/').append(binding.getName()).toString());
    }

    private String getDoc(KOHelpItem binding) {
        try {
            File cacheFile = getCacheFile(binding);
            if (!cacheFile.exists()) {
                //load from web and cache locally
                loadDoc(binding, cacheFile);
                
                //if any of the files is not loaded yet, start the loading process
                if(!loadingStarted) {
                    loadingStarted = true;
                    startLoading();
                }
            }
            return KOUtils.getFileContent(cacheFile);
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.INFO, "Cannot load knockout documentation from \"{0}\".", new Object[]{binding.getExternalDocumentationURL()}); //NOI18N
            return Bundle.doc_cannotGet(binding.getExternalDocumentationURL());
        }

    }

    private void loadDoc(KOHelpItem binding, File cacheFile) throws URISyntaxException, MalformedURLException, IOException {
        LOG.fine("start loading doc"); //NOI18N
        String docURL = binding.getExternalDocumentationURL();
        URL url = new URI(docURL).toURL();
        synchronized (cacheFile) {
            StringWriter sw = new StringWriter();
            //load from the URL
            KOUtils.loadURL(url, sw, StandardCharsets.UTF_8);
            //strip off the proper content
            String knockoutDocumentationContent = KOUtils.getKnockoutDocumentationContent(sw.getBuffer().toString());
            //save to cache file
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(cacheFile), StandardCharsets.UTF_8)) {
                writer.append("<!doctype html><html><head><title>Knockout documentation</title></head><body>"); //NOI18N
                writer.append(knockoutDocumentationContent);
                writer.append("</body></html>"); //NOI18N
            }
        }
    }

    private void buildDoc() {
        if (bindings.hasNext()) {
            binding = bindings.next();
            getDoc(binding);
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
    private KOHelpItem binding;
    private Iterator<KOHelpItem> bindings;
    private ProgressHandle progress;
    private int loaded = 0;
}
