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
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;


/**
 * PENDING - merge the include resolver and script resolver into one code; just messages differ,
 * factor out reference resolution.
 * 
 * @author sdedic
 */
public class ScriptResolver  extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    
    private BuildEnvironment env;

    public ScriptResolver() {
    }

    ScriptResolver(BuildEnvironment env) {
        this.env = env;
    }

    @NbBundle.Messages({
        "# {0} - source path",
        "# {1} - original exception message",
        "ERR_invalidScriptPath=Invalid script source path: {0} ({1})",
        "ERR_emptySourcePath=Script source path is empty",
        "# {0} - script path",
        "ERR_unresolvedScriptPath=Script source not found: {0}"
    })
    @Override
    public void visitScript(FxScriptFragment script) {
        String srcPath = script.getSourcePath();
        
        if (srcPath == null) {
            return;
        }
        
        if ("".equals(srcPath)) {
            TextPositions pos = env.getTreeUtilities().positions(script);
            env.addError(new ErrorMark(
                    pos.getStart(), pos.getEnd() - pos.getStart(),
                    "empty-script-source-path",
                    ERR_emptySourcePath(),
                    script
            ));
            env.getAccessor().makeBroken(script);
            return;
        }

        FileObject targetFo = null;
        
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

            URL u;
            try {
                u = new URL(env.getModel().getBaseURL(), srcPath);
                targetFo = URLMapper.findFileObject(u);
            } catch (MalformedURLException ex) {
                TextPositions pos = env.getTreeUtilities().positions(script);
                env.addError(new ErrorMark(
                        pos.getStart(), pos.getEnd() - pos.getStart(),
                        "invalid-script-path",
                        ERR_invalidScriptPath(srcPath, ex.getLocalizedMessage()),
                        script
                ));
                env.getAccessor().makeBroken(script);
                return;
            }
        }
        if (targetFo == null) {
            TextPositions pos = env.getTreeUtilities().positions(script);
            env.addError(new ErrorMark(
                    pos.getStart(), pos.getEnd() - pos.getStart(),
                    "unresolved-script-path",
                    ERR_unresolvedScriptPath(srcPath),
                    script
            ));
            env.getAccessor().makeBroken(script);
        } else {
            URL resolvedUrl = targetFo.toURL();
            env.getAccessor().resolveResource(script, resolvedUrl);
        }
        
        super.visitScript(script); 
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new ScriptResolver(env);
    }
    
}
