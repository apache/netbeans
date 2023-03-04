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
package org.netbeans.libs.jstestdriver.api;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.libs.jstestdriver.JsTestDriverImpl;
import org.netbeans.libs.jstestdriver.JsTestDriverImplementation;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

public final class JsTestDriver {

    private File jsTestDriverJar;
    private JsTestDriverImplementation impl;

    public JsTestDriver(File jsTestDriverJar) {
        this.jsTestDriverJar = jsTestDriverJar;
        impl = createImpl2();
    }

    public void startServer(int port, boolean strictMode, ServerListener listener) {
        impl.startServer(jsTestDriverJar, port, strictMode, listener);
    }
    
    public void stopServer() {
        impl.stopServer();
    }
    
    public boolean isRunning() {
        return impl.isRunning();
    }
    
    public boolean wasStartedExternally() {
        return impl.wasStartedExternally();
    }
    
    public void runTests(String serverURL, boolean strictMode, File baseFolder, File configFile, 
            String testsToRun, TestListener listener, LineConvertor lineConvertor) {
        impl.runTests(jsTestDriverJar, serverURL, strictMode, baseFolder, configFile, 
                testsToRun, listener, lineConvertor);
    }

    private JsTestDriverImplementation createImpl2() {
        return new JsTestDriverImpl();
    }
    
//    private JsTestDriverImplementation createImpl() {
//        ClassLoader cl = getClassLoader();
//        try {
//            if (cl != null) {
//                Class impl = cl.loadClass("org.netbeans.libs.jstestdriver.ext.JsTestDriverImpl");
//                Constructor c = impl.getConstructor();
//                JsTestDriverImplementation f = (JsTestDriverImplementation)c.newInstance();
//                return f;
//            }
//        } catch (Throwable ex) {
//            throw new RuntimeException("cannot instantiate JsTestDriverImpl", ex);
//        }
//        return null;
//    }
//    
//    private ClassLoader getClassLoader() {
//        File extjar = InstalledFileLocator.getDefault().locate("modules/ext/libs.jstestdriver-ext.jar", "org.netbeans.libs.jstestdriver", false); // NOI18N
//        if (extjar == null) {
//            throw new RuntimeException("libs.jstestdriver-ext.jar not found");
//        }
//        List<URL> urls = new ArrayList<URL>();
//        try {
//            urls.add(extjar.toURI().toURL());
//            urls.add(jsTestDriverJar.toURI().toURL());
//            ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[] {}), 
//                    JsTestDriver.class.getClassLoader()/*ClassLoader.getSystemClassLoader()*/);
//            return classLoader;
//        } catch (MalformedURLException m) {
//            throw new RuntimeException("cannot create classloader", m);
//        }
//    }
    
}
