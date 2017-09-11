/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
