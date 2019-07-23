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

package org.netbeans.modules.nashorn.execution;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.nashorn.execution.options.Settings;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin
 */
public class JSExecutor {
    
    private static final String NASHORN_SHELL = "jdk.nashorn.tools.Shell";      // NOI18N
    
    public static void run(JavaPlatform javaPlatform, FileObject js, boolean debug) throws IOException, UnsupportedOperationException {
        LifecycleManager.getDefault().saveAll();
        Map<String, Object> properties = new HashMap<>();
        properties.put(JavaRunner.PROP_PLATFORM, javaPlatform);
        properties.put(JavaRunner.PROP_CLASSNAME, NASHORN_SHELL);
        properties.put(JavaRunner.PROP_EXECUTE_CLASSPATH, getClassPath(js));
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
        return cp;
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
    
}
