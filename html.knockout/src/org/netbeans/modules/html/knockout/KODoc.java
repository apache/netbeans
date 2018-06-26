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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
        progress = ProgressHandleFactory.createHandle(Bundle.doc_building());
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
            KOUtils.loadURL(url, sw, Charset.forName("UTF-8")); //NOI18N
            //strip off the proper content
            String knockoutDocumentationContent = KOUtils.getKnockoutDocumentationContent(sw.getBuffer().toString());
            //save to cache file
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(cacheFile), "UTF-8")) { // NOI18N
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
