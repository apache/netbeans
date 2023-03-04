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
package org.netbeans.core.browser.webview;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.browser.api.WebBrowser;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author petrjiricka
 */
public class WebBrowserImplProvider {
    
    private static final Logger log = Logger.getLogger(WebBrowserImplProvider.class.getName());
    
    static WebBrowser createBrowser() {
        return createBrowser(null);
    }

    static WebBrowser createBrowser( File runtimePath ) {
        ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
        if( null == cl )
            cl = WebBrowserImplProvider.class.getClassLoader();
        try {
            if (cl != null) {
                // test that JavaFX has latest required APIs:
                cl.loadClass("com.sun.javafx.scene.web.Debugger");
                Class platform = cl.loadClass("javafx.application.Platform");
                Method m = platform.getMethod("setImplicitExit", boolean.class);
            }
        } catch(Throwable ex)  {
            log.log(Level.INFO, "JavaFX runtime is too old - "
                    + "minimum version required is 2.2.0b20", ex);
            return new NoWebBrowserImpl("JavaFX runtime is too old - "
                    + "minimum version required is 2.2.0b20");
        }
        try {
            if (cl != null) {
                 //return new WebBrowserImpl();
                Class impl = cl.loadClass("org.netbeans.core.browser.webview.ext.WebBrowserImpl");
                Constructor c = impl.getConstructor(new Class[] {});
                return (WebBrowser)c.newInstance(new Object[] {});
            }
        } catch (Throwable ex) {
            log.log(Level.INFO, ex.getMessage(), ex);
            return new NoWebBrowserImpl(ex.getMessage());
        }
        return new NoWebBrowserImpl(new OldRuntimePanel());
    }
}
