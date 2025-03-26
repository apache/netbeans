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
package org.netbeans.nbbuild.extlibs;

import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.util.ElementFilter;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Please note this class is copied during build into apisupport/apisupport.ant.
 * When modifying this class, please ensure the module still compiles and works.
 */
public class SetupLimitModulesProbe {

    public static void main(String[] args) throws IOException {
        String release = args[0];

        String[] excludedModules =
            Arrays.stream(args)
                  .skip(1)
                  .toArray(s -> new String[s]);

        String limitModules = computeLimitModules(release, excludedModules);

        System.out.println(limitModules);
    }

    public static String computeLimitModules(String release, String... excludedModulesIn) throws IOException {
        Set<String> excludedModules = new HashSet<>(List.of(excludedModulesIn));
        List<String> options;

        if ("last".equals(release)) {
            options = List.of("--add-modules", "ALL-SYSTEM", "-classpath", "");
        } else {
            options = List.of("--release", release, "-classpath", "");
        }

        JavacTask task = (JavacTask)
                ToolProvider.getSystemJavaCompiler()
                            .getTask(null, null, null, options, null,
                                     List.of(new JFOImpl(URI.create("mem://Test.java"), "")));

        task.analyze();

        String limitModules =
            task.getElements()
                .getAllModuleElements()
                .stream()
                .filter(m -> !m.getQualifiedName().toString().startsWith("jdk.internal."))
                .filter(m -> !m.isUnnamed())
                .filter(m -> canInclude(m, excludedModules))
                .map(m -> m.getQualifiedName())
                .collect(Collectors.joining(","));

        return limitModules;
    }

    private static boolean canInclude(ModuleElement m, Set<String> excludes) {
        return Collections.disjoint(transitiveDependencies(m), excludes);
    }

    private static Set<String> transitiveDependencies(ModuleElement m) {
        List<ModuleElement> todo = new LinkedList<>();
        Set<ModuleElement> seenModules = new HashSet<>();

        todo.add(m);

        while (!todo.isEmpty()) {
            ModuleElement current = todo.remove(0);

            if (seenModules.add(current)) {
                for (RequiresDirective rd : ElementFilter.requiresIn(current.getDirectives())) {
                    todo.add(rd.getDependency());
                }
            }
        }

        return seenModules.stream()
                          .map(c -> c.getQualifiedName().toString())
                          .collect(Collectors.toSet());
    }

    private static final class JFOImpl extends SimpleJavaFileObject {

        private final String content;

        public JFOImpl(URI uri, String content) {
            super(uri, Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return content;
        }

    }
}
