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

