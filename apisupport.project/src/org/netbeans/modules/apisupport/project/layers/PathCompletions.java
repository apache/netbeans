/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
