/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
