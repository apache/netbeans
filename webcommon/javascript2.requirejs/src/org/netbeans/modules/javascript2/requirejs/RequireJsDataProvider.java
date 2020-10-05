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
package org.netbeans.modules.javascript2.requirejs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.modules.Places;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Pisl
 */
@NbBundle.Messages({
    "doc.building=Loading RequireJS Documentation",
    "# {0} - the documentation URL",
    "doc.cannotGet=Cannot load RequireJS documentation from \"{0}\".",
    "doc.notFound=Documentation not found."
})
public class RequireJsDataProvider {

    private static final Logger LOG = Logger.getLogger(RequireJsDataProvider.class.getSimpleName()); //NOI18N
    private static RequestProcessor RP = new RequestProcessor(RequireJsDataProvider.class);
    private static RequireJsDataProvider INSTANCE;
    private boolean loadingStarted;
    private ProgressHandle progress;

    private static final String CACHE_FOLDER_NAME = "requirejs-doc"; //NOI18N
    private static final String API_FILE = "api.html"; //NOI18N
    public static final String API_URL = "http://requirejs.org/docs/api.html";

    private static final int URL_CONNECTION_TIMEOUT = 1000; //ms
    private static final int URL_READ_TIMEOUT = URL_CONNECTION_TIMEOUT * 3; //ms

    private static final String SEARCH_TEXT = "<p id=\"config-"; //NOI18N
    

    /**
     * Translating names from documentation to the real option names
     */
    private final static HashMap<String, String> TRANSLATE_NAME = new HashMap<>();

    static {
        TRANSLATE_NAME.put("moduleconfig", "config");//NOI18N
        // remove this option
        TRANSLATE_NAME.put("map-notes", ""); //NOI18N
    }

    private RequireJsDataProvider() {
        loadingStarted = false;
    }
    
    public static synchronized RequireJsDataProvider getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new RequireJsDataProvider();
        }
        return INSTANCE;
    }

    public Collection<String> getConfigurationOptions() {
        Collection<String> options = getConfigurationOptionsFromDoc();
        if (options.isEmpty()) {
            options = getConfigurationOptionsStatic();
        }
        return options;
    }

    public String getDocForHtmlTagAttribute(final String attributeName) {
        String api = getContentApiFile();
        if (api != null) {
            String searchText = "href=\"#" + attributeName + "\" name=\"" + attributeName + "\""; //NOI18N
            int start = api.indexOf(searchText);
            if (start > 0) {
                String tmpText = api.substring(0, start);
                start = tmpText.lastIndexOf("<h3>"); // NOI18N
                int end = api.indexOf("<h3>", start + 10); //NOI18N
                if (start < end) {
                    return api.substring(start, end);
                }
            }
        }
        return "";
    }
    
    public String getDocFocOption(String name) {
        String api = getContentApiFile();
        if (api != null) {
            int index = api.indexOf(SEARCH_TEXT + name);
            if (index == -1 && TRANSLATE_NAME.containsValue(name)) {
                for (Map.Entry<String, String> entry : TRANSLATE_NAME.entrySet()) {
                    if (entry.getValue().equals(name)) {
                        index = api.indexOf(SEARCH_TEXT + entry.getKey());
                        break;
                    }

                }
            }
            if (index > 0) {
                int start = index;
                index = api.indexOf(SEARCH_TEXT, start + SEARCH_TEXT.length());
                if (index == -1) {
                    index = api.indexOf("</div>", start + SEARCH_TEXT.length());
                }
                if (index > 0) {
                    return api.substring(start, index);
                }
            }
        }
        return Bundle.doc_notFound();
    }

    private void startLoading() {
        LOG.fine("start loading doc"); //NOI18N

        progress = ProgressHandleFactory.createHandle(Bundle.doc_building());
        progress.start(1);

    }

    private Collection<String> getConfigurationOptionsFromDoc() {
        List<String> result = new ArrayList<>();

        String api = getContentApiFile();
        if (api != null) {
            int index = api.indexOf(SEARCH_TEXT);
            while (index > 0) {
                int start = index + SEARCH_TEXT.length();
                index = api.indexOf('"', start);
                String option = api.substring(start, index);
                if (TRANSLATE_NAME.containsKey(option)) {
                    option = TRANSLATE_NAME.get(option);
                }
                if (!(option == null || option.isEmpty())) { 
                    result.add(option);
                }
                index = api.indexOf(SEARCH_TEXT, index);
            }
        }
        return result;
    }

    public static File getCachedAPIFile() {
        String pathFile =  new StringBuilder().append(CACHE_FOLDER_NAME).append('/').append(API_FILE).toString();
        File cacheFile = Places.getCacheSubfile(pathFile);
        return cacheFile;
    }
    
    private String getContentApiFile() {
        String result = null;
        try {
            File cacheFile = getCachedAPIFile();
            if (!cacheFile.exists()) {

                //if any of the files is not loaded yet, start the loading process
                if (!loadingStarted) {
                    loadingStarted = true;
                    startLoading();
                }
                //load from web and cache locally
                loadDoc(cacheFile);
                if (progress != null) {
                    progress.progress(1);
                    progress.finish();
                    progress = null;
                }

                LOG.log(Level.FINE, "Loading doc finished."); //NOI18N
            }
            result = getFileContent(cacheFile);
        } catch (URISyntaxException | IOException ex) {
            loadingStarted = false;
            if (progress != null) {
                progress.finish();
                progress = null;
            }
            LOG.log(Level.INFO, "Cannot load RequireJS documentation from \"{0}\".", new Object[]{API_URL}); //NOI18N
            LOG.log(Level.INFO, "", ex);
        }
        return result;
    }

    private Collection<String> getConfigurationOptionsStatic() {
        List<String> known = new ArrayList<>();
        for (ConfigOption option : ConfigOption.values()) {
            known.add(option.getName());
        }
        return known;
    }

    private void loadDoc(File cacheFile) throws URISyntaxException, MalformedURLException, IOException {
        LOG.fine("start loading doc"); //NOI18N
        URL url = new URI(API_URL).toURL();
        synchronized (cacheFile) {
            String tmpFileName = cacheFile.getAbsolutePath() + ".tmp";
            File tmpFile = new File(tmpFileName);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8")) { // NOI18N
                loadURL(url, writer, Charset.forName("UTF-8")); //NOI18N
                writer.close();
                boolean success = tmpFile.renameTo(cacheFile);
                if (!success) {
                    LOG.log(Level.WARNING, "Renaming {0} to {1] was not successful.", new Object[]{tmpFile.getAbsolutePath(), cacheFile.getAbsolutePath()});
                }
            } finally {
                if (tmpFile.exists()) {
                    boolean success = tmpFile.delete();
                    if (!success) {
                        LOG.log(Level.WARNING, "Deleting {0} faild.", new Object[]{tmpFile.getAbsolutePath()});
                    }
                }
            }

        }
    }

    private void loadURL(URL url, Writer writer, Charset charset) throws IOException {
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        URLConnection con = url.openConnection();
        con.setConnectTimeout(URL_CONNECTION_TIMEOUT);
        con.setReadTimeout(URL_READ_TIMEOUT);
        con.connect();
        Reader r = new InputStreamReader(new BufferedInputStream(con.getInputStream()), charset);
        char[] buf = new char[2048];
        int read;
        while ((read = r.read(buf)) != -1) {
            writer.write(buf, 0, read);
        }
        r.close();
    }

    private String getFileContent(File file) throws IOException {
        Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8"); // NOI18N
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[2048];
            int read;
            while ((read = r.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        } finally {
            r.close();
        }
        return sb.toString();
    }
}
