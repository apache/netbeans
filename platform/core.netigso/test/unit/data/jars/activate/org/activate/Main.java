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

import java.lang.reflect.Method;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Main implements BundleActivator {
    public static BundleContext start;
    public static BundleContext stop;

    @Override
    public void start(BundleContext bc) throws Exception {
        assert start == null;
        start = bc;

        String clazzName = System.getProperty("start.class");
        if (clazzName != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = Class.forName(clazzName, true, cl);
            clazz.newInstance();
        }
        
        String fileObject = System.getProperty("activate.layer.test");
        if (fileObject != null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> util = Class.forName("org.openide.filesystems.FileUtil", true, cl);
            Method m = util.getDeclaredMethod("getConfigFile", String.class);
            Object res = m.invoke(null, fileObject);
            if (res == null) {
                throw new IllegalStateException("FileObject has to be found: " + res);
            }
        }
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        assert stop == null;
        stop = bc;
    }
}

