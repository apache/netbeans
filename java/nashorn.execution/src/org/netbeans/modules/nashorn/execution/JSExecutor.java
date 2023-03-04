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

package org.netbeans.modules.nashorn.execution;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.nashorn.execution.options.Settings;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.JarFileSystem;

/**
 *
 * @author Martin
 */
public class JSExecutor {
    
    private static final String NASHORN_SHELL = "jdk.nashorn.tools.Shell";      // NOI18N
    private static final String JS_SHELL = "com.oracle.truffle.js.shell.JSLauncher";      // NOI18N
    
    public static void run(JavaPlatform javaPlatform, FileObject js, boolean debug) throws IOException, UnsupportedOperationException {
        LifecycleManager.getDefault().saveAll();
        Map<String, Object> properties = new HashMap<>();
        properties.put(JavaRunner.PROP_PLATFORM, javaPlatform);
        final ClassPath path = getClassPath(js);
        final Preferences p = Settings.getPreferences();
        boolean preferNashorn = NashornPlatform.isNashornSupported(javaPlatform);
        if (p.get(Settings.PREF_NASHORN, null) != null) {
            preferNashorn = p.getBoolean(Settings.PREF_NASHORN, false);
        } else {
            if (NashornPlatform.isGraalJsSupported(javaPlatform)) {
                if (NashornPlatform.isGraalJSPreferred(javaPlatform)) {
                    preferNashorn = false;
                }
            }
        }
        if (preferNashorn) {
            try {
                javaPlatform.getBootstrapLibraries().getClassLoader(true).loadClass(NASHORN_SHELL);
                properties.put(JavaRunner.PROP_CLASSNAME, NASHORN_SHELL);
            } catch (ClassNotFoundException ex) {
                properties.put(JavaRunner.PROP_CLASSNAME, JS_SHELL);
            }
        } else {
            properties.put(JavaRunner.PROP_CLASSNAME, JS_SHELL);
        }
        properties.put(JavaRunner.PROP_EXECUTE_CLASSPATH, path);
        properties.put(JavaRunner.PROP_WORK_DIR, js.getParent());
        properties.put(JavaRunner.PROP_APPLICATION_ARGS, getApplicationArgs(js)); // Collections.singletonList(js.getNameExt()));
            if (debug) {
            JavaRunner.execute(JavaRunner.QUICK_DEBUG, properties);
        } else {
            JavaRunner.execute(JavaRunner.QUICK_RUN, properties);
        }
    }
    
    private static ClassPath getClassPath(FileObject js) {
        ClassPath cp = ClassPath.getClassPath(js, ClassPath.EXECUTE);
        if (cp == null) {
            cp = ClassPath.EMPTY;
        }
        ClassPath engine = findGraalJsClassPath();
        return ClassPathSupport.createProxyClassPath(cp, engine);
    }
    
    private static List<String> getApplicationArgs(FileObject js) {
        String options = Settings.getPreferences().get(Settings.PREF_NASHORN_OPTIONS, null);
        String arguments = Settings.getPreferences().get(Settings.PREF_NASHORN_ARGUMENTS, null);
        if (options == null && arguments == null) {
            return Collections.singletonList(js.getNameExt());
        }
        List<String> args = new LinkedList<>();
        if (options != null && !(options = options.trim()).isEmpty()) {
            args.add(options);
        }
        args.add(js.getNameExt());
        if (arguments != null && !(arguments = arguments.trim()).isEmpty()) {
            args.add("--");     // NOI18N
            args.add(arguments);
        }
        return args;
    }
    
    static ClassPath findGraalJsClassPath() {
        List<URL> urls;
        Library graalJsLib = LibraryManager.getDefault().getLibrary("graaljs");
        urls = graalJsLib.getContent("classpath");
        ClassPath nbInstCp = ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
        urls = new ArrayList<>();
        for (ClassPath.Entry e : nbInstCp.entries()) {
            assert e.isValid() : "Invalid entry: " + e;
            try {
                JarFileSystem jfs = (JarFileSystem) e.getRoot().getFileSystem();
                urls.add(new URL("jar:" + jfs.getJarFile().toURI().toURL() + "!/"));
            } catch (FileStateInvalidException | MalformedURLException ignore) {
                throw new AssertionError(ignore);
            }
        }
        return ClassPathSupport.createClassPath(urls.toArray(new URL[0]));
    }
}
