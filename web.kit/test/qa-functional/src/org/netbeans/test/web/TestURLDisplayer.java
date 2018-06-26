/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
