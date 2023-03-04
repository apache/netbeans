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
package org.netbeans.test.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.jemmy.JemmyException;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Lookup;

/**
 * Designed to replace default URLDisplayer to better serve automated tests
 * requirements. <p> Usage:<br>
 * <pre>
 *      TestURLDisplayer displayer = TestURLDisplayer.getInstance();
 *      displayer.invalidateURL();
 *      // e.g. run a .jsp
 *      displayer.waitURL();
 *      String page = displayer.readURL();
 *      ...
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.awt.HtmlBrowser.URLDisplayer.class, supersedes = "org.netbeans.core.NbTopManager$NbURLDisplayer")
public final class TestURLDisplayer extends HtmlBrowser.URLDisplayer {

    private static TestURLDisplayer instance;
    private boolean isURLValid = false;
    private URL url = null;
    private URLConnection con = null;

    public static synchronized TestURLDisplayer getInstance() {
        if (instance == null) {
            // the instance is registered by META-INF/services/org.openide.awt.HtmlBrowser$URLDisplayer
            Object result = Lookup.getDefault().lookup(HtmlBrowser.URLDisplayer.class);
            // check the instance
            if (!result.getClass().equals(TestURLDisplayer.class)) {
                throw new JemmyException("URL displayer registration failed" + result.getClass());
            }
            instance = (TestURLDisplayer) result;
        }
        return instance;
    }

    @Override
    public synchronized void showURL(URL u) {
        url = u;
        try {
            con = url.openConnection();
        } catch (IOException ex) {
            System.err.println("Cannot open URL: " + url);
            ex.printStackTrace();
        }
        // force to send request
        final URLConnection fc = con;
        new Thread() {

            @Override
            public void run() {
                try {
                    fc.getInputStream();
                } catch (IOException ex) {
                    System.err.println("Cannot read URL: " + url);
                    ex.printStackTrace();
                }
            }
        }.start();

        isURLValid = true;
        notifyAll();
    }

    public synchronized void invalidateURL() {
        url = null;
        con = null;
        isURLValid = false;
    }

    public synchronized URL waitURL() throws InterruptedException {
        while (!isURLValid) {
            wait(60000);
            if (!isURLValid) {
                throw new IllegalStateException("Timeout expired.");
            }
        }
        return url;
    }

    public String readURL() {
        if (!isURLValid || url == null) {
            throw new IllegalStateException("URL is not valid.");
        }
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        try {
            is = con.getInputStream();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
            } finally {
                is.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

    public URL getURL() {
        return url;
    }
}
