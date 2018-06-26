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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.FieldSignature;
import org.netbeans.modules.groovy.editor.api.completion.MethodSignature;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.spi.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service = CompletionProvider.class)
public class ControllerCompletionProvider implements CompletionProvider {

    private static final Map<MethodSignature, String> INSTANCE_METHODS = new HashMap<MethodSignature, String>();

    // FIXME move it to some resource file, check the grails version - this is for 1.0.4
    static {
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.List"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.List", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.util.Map", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("bindData",
                new String[] {"java.lang.Object", "java.lang.Object", "java.lang.String"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("chain",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N // return value void ?
        INSTANCE_METHODS.put(new MethodSignature("redirect",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.lang.Object"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.lang.String"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.util.Map"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("render",
                new String[] {"java.util.Map", "groovy.lang.Closure"}), "java.lang.Object"); // NOI18N // return value always null
        INSTANCE_METHODS.put(new MethodSignature("withFormat",
                new String[] {"groovy.lang.Closure"}), "java.lang.Object"); // NOI18N
    }

    @Override
    public Map<FieldSignature, CompletionItem> getFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<FieldSignature, CompletionItem> getStaticFields(CompletionContext context) {
        return Collections.emptyMap();
    }

    @Override
    public Map<MethodSignature, CompletionItem> getMethods(CompletionContext context) {
       Map<MethodSignature, CompletionItem> result = new HashMap<MethodSignature, CompletionItem>();
        if (isInControllerFolder(context)) {
            for (Map.Entry<MethodSignature, String> entry : INSTANCE_METHODS.entrySet()) {
                result.put(entry.getKey(), CompletionItem.forDynamicMethod(
                        context.getAnchor(), entry.getKey().getName(), entry.getKey().getParameters(),
                                entry.getValue(), false));
            }
        }
        return result;
    }
    
    @Override
    public Map<MethodSignature, CompletionItem> getStaticMethods(CompletionContext context) {
        return Collections.emptyMap();
    }
    
    private boolean isInControllerFolder(CompletionContext context) {
        if (context.getSourceFile() == null) {
            return false;
        }

        Project project = FileOwnerQuery.getOwner(context.getSourceFile());
        if (project != null) {

            if (isController(context.getSourceFile(), project)) {
                return true;
            }
        }
        return false;
    }

    private boolean isController(FileObject source, Project project) {
        if (source == null || !source.getName().endsWith("Controller")) { // NOI18N
            return false;
        }

        FileObject controllerDir = project.getProjectDirectory().getFileObject("grails-app/controllers"); // NOI18N
        if (controllerDir == null || !controllerDir.isFolder()) {
            return false;
        }

        return FileUtil.isParentOf(controllerDir, source);
    }

}
