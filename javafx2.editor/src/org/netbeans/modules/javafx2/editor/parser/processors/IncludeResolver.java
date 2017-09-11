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
