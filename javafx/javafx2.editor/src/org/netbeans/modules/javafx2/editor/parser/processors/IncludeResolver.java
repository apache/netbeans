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
package org.netbeans.modules.javafx2.editor.parser.processors;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
/**
 *
 * @author sdedic
 */
public class IncludeResolver extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    
    private BuildEnvironment env;

    public IncludeResolver() {
    }

    IncludeResolver(BuildEnvironment env) {
        this.env = env;
    }

    @NbBundle.Messages({
        "ERR_emptyIncludePath=Include source cannot be empty.",
        "# {0} - source path",
        "# {1} - processing exception",
        "ERR_invalidSourcePath=Include path {0} is invalid: {1}",
        "# {0} - source path",
        "ERR_unresolvedIncludePath=Included file cannot be found: {0}"
    })
    @Override
    public void visitInclude(FxInclude decl) {
        String srcPath = decl.getSourcePath();
        FileObject targetFo = null;
        
        if ("".equals(srcPath)) {
            TextPositions pos = env.getTreeUtilities().positions(decl);
            env.addError(new ErrorMark(
                    pos.getStart(), pos.getEnd() - pos.getStart(),
                    "empty-include-source-path",
                    ERR_emptyIncludePath(),
                    decl
            ));
            env.getAccessor().makeBroken(decl);
            return;
        }
        if (srcPath.startsWith("/")) {
            // guide: relative to classpath; try to find a resource which match the path
            ClassPath cp = env.getCompilationInfo().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
            if (cp != null) {
                targetFo = cp.findResource(srcPath);
            }
            if (targetFo == null) {
                cp = env.getCompilationInfo().getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                if (cp != null) {
                    targetFo = cp.findResource(srcPath);
                }
            }
        } else {
            try {
                URL u = new URL(env.getModel().getBaseURL(), srcPath);
                u.toURI();
                targetFo = URLMapper.findFileObject(u);
            } catch (URISyntaxException ex) {
                TextPositions pos = env.getTreeUtilities().positions(decl);
                env.addError(new ErrorMark(
                        pos.getStart(), pos.getEnd() - pos.getStart(),
                        "invalid-include-path",
                        ERR_invalidSourcePath(srcPath, ex.getLocalizedMessage()),
                        decl
                ));
            } catch (MalformedURLException ex) {
                TextPositions pos = env.getTreeUtilities().positions(decl);
                env.addError(new ErrorMark(
                        pos.getStart(), pos.getEnd() - pos.getStart(),
                        "invalid-source-path",
                        ERR_invalidSourcePath(srcPath, ex.getLocalizedMessage()),
                        decl
                ));
                env.getAccessor().makeBroken(decl);
                return;
            }
            
        }
        
        if (targetFo == null) {
            TextPositions pos = env.getTreeUtilities().positions(decl);
            env.addError(new ErrorMark(
                    pos.getStart(), pos.getEnd() - pos.getStart(),
                    "unresolved-include-path",
                    ERR_unresolvedIncludePath(srcPath),
                    decl
            ));
            env.getAccessor().makeBroken(decl);
        } else {
            URL resolvedUrl = targetFo.toURL();
            env.getAccessor().resolveResource(decl, resolvedUrl);
        }
    }
    

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new IncludeResolver(env);
    }
    
}
