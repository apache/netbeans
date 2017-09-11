/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle
 */
package org.activate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Main implements BundleActivator {
    public static BundleContext start;
    public static BundleContext stop;
    public static ClassLoader loader;
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public void start(BundleContext bc) throws Exception {
        LOG.log(Level.INFO, "start {0}", bc);
        assert start == null;
        start = bc;
        final String v = System.getProperty("activated.count");
        if (v != null) {
            int x = Integer.parseInt(v);
            System.setProperty("activated.count", "" + (x + 1));
        }
        System.setProperty("activated.ok", "true");
        loader = Thread.currentThread().getContextClassLoader();
        String t = System.getProperty("activated.throw");
        if (t != null) {
            LOG.log(Level.INFO, "Throwing exception {0}", t);
            throw new IllegalStateException(t);
        }
        LOG.info("start finished");
        
        String lib = System.getProperty("activated.library");
        if (lib != null) {
            try {
                System.loadLibrary(lib);
                throw new IllegalStateException("Library " + lib + " should not be available");
            } catch (UnsatisfiedLinkError ex) {
                System.setProperty("activated.library", ex.getMessage());
            }
        }
                

        String seekFor = System.getProperty("activated.checkentries");
        if (seekFor != null) {
            LOG.info("loading bundleentry:");
            URL u = getClass().getResource(seekFor);
            assert "bundleresource".equals(u.getProtocol()) : "Protocol: " + u.getProtocol();
            BufferedReader r = new BufferedReader(new InputStreamReader(u.openStream()));
            String c = r.readLine();
            r.close();
            System.setProperty("activated.entry", c);
            LOG.log(Level.INFO, "entry loaded: {0}", c);

            URLConnection buc = u.openConnection();
            System.setProperty("activated.entry.local", ((URL)call(buc, "getLocalURL")).toExternalForm());
            System.setProperty("activated.entry.file", ((URL)call(buc, "getFileURL")).toExternalForm());
            
            System.getProperties().put("activated.entry.url", u);

            LOG.log(Level.INFO, "BundleURLConnection is OK");
        }
    }

    public void stop(BundleContext bc) throws Exception {
        LOG.log(Level.INFO, "stop {0}", bc);
        assert stop == null;
        stop = bc;
    }
    
    private static Object call(Object obj, String name) throws Exception {
        Class<?> c = obj.getClass();
        return c.getMethod(name).invoke(obj);
    }
    
    public static Class<?> loadClass(String name, ClassLoader ldr) throws ClassNotFoundException {
        LOG.log(Level.INFO, "Trying to load from {0} class named: {1}", new Object[]{ldr, name});
        if (ldr == null) {
            return Class.forName(name);
        }
        return Class.forName(name, true, ldr);
    }    
}

