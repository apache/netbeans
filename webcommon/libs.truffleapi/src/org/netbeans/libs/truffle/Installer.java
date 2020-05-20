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
package org.netbeans.libs.truffle;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Modules;
import org.openide.util.*;

public final class Installer extends ModuleInstall {
    public Installer() {
    }

    @Override
    public void restored() {
        final String vmName = System.getProperty("java.vm.name"); // NOI18N
        if (vmName != null && vmName.contains("GraalVM")) { // NOI18N
            registerTruffleLibraries();
        }
    }

    private static void registerTruffleLibraries() {
        Set<File> jars = new LinkedHashSet<>();
        for (ModuleInfo m : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (hasTruffleDependency(m)) {
                jars.addAll(moduleJars(m));
                for (ModuleInfo depModule : allDependencies(m, new LinkedHashSet<>())) {
                    jars.addAll(moduleJars(depModule));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (File file : jars) {
            sb.append(sep);
            sb.append(file.getAbsolutePath());
            sep = File.pathSeparator;
        }
        System.setProperty("truffle.class.path.append", sb.toString()); // NOI18N
    }

    private static List<File> moduleJars(ModuleInfo module) {
        try {
            Class<?> clazz = module.getClass();
            while (!Modifier.isPublic(clazz.getModifiers())) {
                clazz = clazz.getSuperclass();
            }
            Method method = clazz.getMethod("getAllJars"); // NOI18N
            return (List<File>) method.invoke(module);
        } catch (ReflectiveOperationException ex) {
            return Collections.emptyList();
        }
    }

    private static boolean hasTruffleDependency(ModuleInfo m) {
        Object attr = m.getAttribute("Truffle-Class-Path-Append"); // NOI18N
        return "true".equals(attr); // NOI18N
    }

    private static boolean addTruffleDependency(ModuleInfo m) {
        if (m == null) {
            return false;
        }
        Object attr = m.getAttribute("Truffle-Class-Path-Append"); // NOI18N
        if (attr == null) {
            return true;
        }
        return "true".equals(attr); // NOI18N
    }

    private static Set<ModuleInfo> allDependencies(ModuleInfo m, Set<ModuleInfo> collected) {
        if (addTruffleDependency(m) && collected.add(m)) {
            for (Dependency dependency : m.getDependencies()) {
                if (Dependency.TYPE_MODULE == dependency.getType()) {
                    ModuleInfo depModule = Modules.getDefault().findCodeNameBase(cnb(dependency));
                    assert depModule != null : dependency;
                    allDependencies(depModule, collected);
                }
            }
        }
        return collected;
    }

    private static String cnb(Dependency d) {
        String n = d.getName();
        int slash = n.lastIndexOf('/');
        return slash == -1 ? n : n.substring(0, slash);
    }
}
