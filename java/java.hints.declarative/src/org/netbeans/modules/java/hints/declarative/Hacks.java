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

package org.netbeans.modules.java.hints.declarative;

import com.sun.source.tree.Scope;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class Hacks {

    public static Scope constructScope(CompilationInfo info, String... importedClasses) {
        Collection<String> imports = new LinkedList<>();

        for (String i : importedClasses) {
            imports.add("import " + i + ";\n");
        }

        return Utilities.constructScope(info, Collections.<String, TypeMirror>emptyMap(), imports);
    }

    private static final String SOURCE_LEVEL = "1.8"; //TODO: could be possibly inferred from the current Java platform

    public static Map<String, byte[]> compile(ClassPath compile, final String code) throws IOException {
        DiagnosticListener<JavaFileObject> devNull = (Diagnostic<? extends JavaFileObject> diagnostic) -> {};
        StandardJavaFileManager sjfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(devNull, null, null);

        final Map<String, ByteArrayOutputStream> class2BAOS = new HashMap<>();
        sjfm.setLocation(StandardLocation.CLASS_PATH, toFiles(compile));


        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(sjfm) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                
                class2BAOS.put(className, buffer);
                return new SimpleJavaFileObject(sibling.toUri(), kind) {
                    @Override
                    public OutputStream openOutputStream() throws IOException {
                        return buffer;
                    }
                };
            }
        };

        JavaFileObject file = new SimpleJavaFileObject(URI.create("mem://mem"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return code;
            }
        };
        ToolProvider.getSystemJavaCompiler().getTask(null, jfm, devNull, /*XXX:*/Arrays.asList("-source", SOURCE_LEVEL, "-target", SOURCE_LEVEL, "-proc:none"), null, Arrays.asList(file)).call();

        Map<String, byte[]> result = new HashMap<>();

        for (Map.Entry<String, ByteArrayOutputStream> e : class2BAOS.entrySet()) {
            result.put(e.getKey(), e.getValue().toByteArray());
        }

        return result;
    }

    private static Iterable<? extends File> toFiles(ClassPath cp) {
        List<File> result = new LinkedList<>();

        for (Entry e : cp.entries()) {
            File f = FileUtil.archiveOrDirForURL(e.getURL());

            if (f == null) {
                Logger.getLogger(Hacks.class.getName()).log(Level.INFO, "file == null, url={0}", e.getURL());
                continue;
            }

            result.add(f);
        }

        return result;
    }


    public static @CheckForNull TypeMirror parseFQNType(@NonNull CompilationInfo info, @NonNull String spec) {
        if (spec.isEmpty()) {
            return null;
        }
        
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        //XXX:
        TypeElement scope;

        if (info.getTopLevelElements().isEmpty()) {
            scope = jlObject;
        } else {
            scope = info.getTopLevelElements().iterator().next();
        }
        //XXX end
        
        return info.getTreeUtilities().parseType(spec, /*XXX: jlObject*/scope);
    }

    public static ClasspathInfo createUniversalCPInfo() {
        return Utilities.createUniversalCPInfo();
    }

}
