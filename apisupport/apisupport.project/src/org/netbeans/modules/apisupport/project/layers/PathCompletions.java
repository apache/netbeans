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

package org.netbeans.modules.apisupport.project.layers;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;

/** Provides code completion items for any annotation that needs to understand
 * the content of a layer.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class PathCompletions implements Processor {
    private static final Logger LOG = Logger.getLogger(PathCompletions.class.getName());
    private ProcessingEnvironment processingEnv;
    
    public static void register() {
        System.setProperty("org.openide.awt.ActionReference.completion", PathCompletions.class.getName());
        LOG.finest("Registering property"); // NOI18N
    }
    
    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.emptySet();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
        Element element,
        AnnotationMirror annotation,
        ExecutableElement member,
        String userText
    ) {
        if (userText.startsWith("\"")) {
            userText = userText.substring(1);
        }
        if (userText.endsWith("\"")) {
            userText = userText.substring(0, userText.length() - 1);
        }
        try {
            LOG.log(Level.FINE, "userText: {0}", userText);
            TreePath path = Trees.instance(processingEnv).getPath(member);
            if (path == null) {
                return Collections.emptySet();
            }
            JavaFileObject sourceFile = path.getCompilationUnit().getSourceFile();
            if (sourceFile == null) {
                return Collections.emptySet();
            }
            URI u = sourceFile.toUri();
            LOG.log(Level.FINE, "uri: {0}", u);
            Project p = FileOwnerQuery.getOwner(u);
            LOG.log(Level.FINE, "project: {0}", p);
            FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(p);
            LOG.log(Level.FINE, "fs: {0}", fs);
            
            FileObject from = fs.findResource(userText);
            LOG.log(Level.FINE, "from1: {0}", from);
            if (from == null) {
                from = fs.findResource(userText.replaceAll("/[^/]*$", ""));
            }
            LOG.log(Level.FINE, "from2: {0}", from);
            List<Completion> arr = new ArrayList<Completion>();
            if (from == null) {
                LOG.fine("No items");
                return arr;
            }
            for (FileObject fo : from.getChildren()) {
                if (fo.isFolder() && fo.getPath().startsWith(userText)) {
                    String localizedName = null;
                    final String name = fo.getNameExt();
                    try {
                        String n = fo.getFileSystem().getDecorator().annotateName(name, Collections.singleton(fo));
                        if (!n.equals(name)) {
                            localizedName = n;
                        }
                    } catch (FileStateInvalidException ex) {
                        // ok
                    }
                    LOG.log(Level.FINE, "Accepting: {0} as {1}", new Object[] { fo, localizedName });
                    if (localizedName == null) {
                        arr.add(Completions.of("\"" + fo.getPath() + "/"));
                    } else {
                        arr.add(Completions.of("\"" + fo.getPath() + "/", localizedName));
                    }
                } else {
                    LOG.log(Level.FINE, "Ignoring: {0}", fo);
                }
            }
            return arr;
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
            return Collections.emptyList();
        }
    }

}
