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
package org.netbeans.core.network.proxy;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.NbLifecycleManager;
import org.netbeans.core.network.proxy.pac.PacParsingException;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluator;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluatorFactory;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluatorNoProxy;
import org.netbeans.core.network.proxy.pac.PacValidationException;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Jirka Rechtacek
 */
public class ProxyAutoConfig {

    private static final Map<String, ProxyAutoConfig> file2pac = new HashMap<String, ProxyAutoConfig>(2);
    private static final RequestProcessor RP = new RequestProcessor(ProxyAutoConfig.class);
    private static final String PROTO_FILE = "file://";

    /**
     * 
     * @param pacFile The string to be parsed into a URI
     * @return ProxyAutoConfig for given pacFile or <code>null</code> if constructor failed
     */
    public static synchronized ProxyAutoConfig get(String pacFile) {
        if (file2pac.get(pacFile) == null) {
            LOGGER.fine("Init ProxyAutoConfig for " + pacFile);
            ProxyAutoConfig instance = null;
            try {
                instance = new ProxyAutoConfig(pacFile);
            } catch (URISyntaxException ex) {
                LOGGER.warning("Parsing " + pacFile + " to URI throws " + ex);
            } finally {
                file2pac.put(pacFile, instance);
            }
        }
        return file2pac.get(pacFile);
    }
    private static final Logger LOGGER = Logger.getLogger(ProxyAutoConfig.class.getName());
    private PacScriptEvaluator evaluator;
    private final Task initTask;
    private final URI pacURI;

    private ProxyAutoConfig(final String pacURL) throws URISyntaxException {
        assert file2pac.get(pacURL) == null : "Only once object for " + pacURL + " must exist.";
        String normPAC = normalizePAC(pacURL);
        pacURI = new URI(normPAC);
        initTask = RP.post(new Runnable() {

            @Override
            public void run() {
                NbLifecycleManager.advancePolicy();
                initEngine();
            }
        });
    }

    public URI getPacURI() {
        return pacURI;
    }

    final void initEngine() {
        String pacSource = null;
        if (pacURI.isAbsolute()) {
            try (InputStream is = downloadPAC(pacURI.toURL())) {
                pacSource = convertInputStreamToString(is, 8192, StandardCharsets.UTF_8);
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, "PAC URL is malformed : ", ex);
                return;
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "InputStream for " + pacURI + " throws ", ex);
                return;
            }
        }
                
        PacScriptEvaluatorFactory factory = Lookup.getDefault().lookup(PacScriptEvaluatorFactory.class);
        if (factory == null) {
            LOGGER.log(Level.WARNING, "No PAC Script Evaluator factory found. Will use dummy evaluator instead.");
            evaluator = new PacScriptEvaluatorNoProxy();
        } else {
            try {
                evaluator = factory.createPacScriptEvaluator(pacSource);
            } catch (PacParsingException ex) {
                LOGGER.log(Level.WARNING, "There was a catastrophic error with the PAC script downloaded from " + pacURI + ". Will use dummy instead. Error was : ", ex);
                evaluator = factory.getNoOpEvaluator();
            }
        }
        
        assert evaluator != null : "JavaScript evaluator cannot be null";
        if (evaluator == null) {
            LOGGER.log(Level.WARNING, "JavaScript evaluator cannot be null");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Proxy> findProxyForURL(URI u) {
        assert initTask != null : "initTask has be to posted.";
        if (!initTask.isFinished()) {
            while (!initTask.isFinished()) {
                try {
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                        }
                    }).waitFinished(100);
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINEST, ex.getMessage(), ex);
                }
            }
        }
        if (evaluator == null) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }
        try {
            return evaluator.findProxyForURL(u);
        } catch (PacValidationException ex) {
            LOGGER.log(Level.WARNING, "Incorrect answer from PAC script : ", ex);
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    private static InputStream downloadPAC (URL pacURL) throws IOException {
        InputStream is;
        URLConnection conn = pacURL.openConnection(Proxy.NO_PROXY);
        is = conn.getInputStream();
        return is;
    }



    protected static String convertInputStreamToString(InputStream in, int initSize, Charset charset) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(initSize);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            buf.write(buffer, 0, length);
        }
        return buf.toString(charset.name());
    }


    private String normalizePAC(String pacURL) {
        int index;
        String inputSanitized = pacURL;
        if ((index = inputSanitized.indexOf("\n")) != -1) { // NOI18N
            inputSanitized = inputSanitized.substring(0, index);
        }
        if ((index = inputSanitized.indexOf("\r")) != -1) { // NOI18N
            inputSanitized = inputSanitized.substring(0, index);
        }
        String fileLocation = pacURL;
        if (fileLocation.startsWith(PROTO_FILE)) {
            fileLocation = fileLocation.substring(PROTO_FILE.length());
        }
        File f = new File(fileLocation);
        if (f.canRead()) {
            inputSanitized = Utilities.toURI(f).toString();
        } else {
            inputSanitized = inputSanitized.replaceAll("\\\\", "/"); //NOI18N
        }
        if ((index = inputSanitized.indexOf(" ")) != -1) { // NOI18N
            inputSanitized = inputSanitized.substring(0, index);
        }
        return inputSanitized.trim();
    }
}
