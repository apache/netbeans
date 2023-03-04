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

package org.netbeans.modules.javaee.project.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle.Messages;

/**
 * Warn user when implementation of EJBContainer is missing.
 */
@Hint(id="OptionalEE7APIsHint", category="api", displayName="#OptionalEE7APIsHint_DisplayName", description="#OptionalEE7APIsHint_Description", options=Hint.Options.QUERY)
@Messages({
    "OptionalEE7APIsHint_DisplayName=Be aware that this API will be optional in Java EE 7 platform.",
    "OptionalEE7APIsHint_Description=Warn user about usage of APIs from technologies which will be made optional in Java EE 7 specification. These APIs are not deprecated and can be used but because they will be optional they may or may not be available in future Java EE 7 compliant platforms."
})
public class OptionalEE7APIsHint {

    // these packages will be optional after EE6
    private static final List<String> optionalPackages = Arrays.asList(
            "javax.enterprise.deploy.model.",
            "javax.enterprise.deploy.model.exceptions.",
            "javax.enterprise.deploy.shared.",
            "javax.enterprise.deploy.shared.factories.",
            "javax.enterprise.deploy.spi.",
            "javax.enterprise.deploy.spi.exceptions.",
            "javax.enterprise.deploy.spi.factories.",
            "javax.enterprise.deploy.status.",
            "javax.xml.registry.",
            "javax.xml.registry.infomodel.",
            "javax.xml.rpc.",
            "javax.xml.rpc.encoding.",
            "javax.xml.rpc.handler.",
            "javax.xml.rpc.handler.soap.",
            "javax.xml.rpc.holders.",
            "javax.xml.rpc.server.",
            "javax.xml.rpc.soap."
            );
    
    @TriggerTreeKind({Kind.MEMBER_SELECT, Kind.IDENTIFIER})
    public static List<ErrorDescription> run(HintContext context) {
        CompilationInfo info = context.getInfo();
        TreePath treePath = context.getPath();
        Element el = info.getTrees().getElement(treePath);
        if (el == null) {
            return null;
        }
        TypeMirror type = el.asType();
        if (type == null) {
            return null;
        }
        String name = type.toString();
        if (!(name.startsWith("javax.xml.rpc") || name.startsWith("javax.xml.registry") || name.startsWith("javax.enterprise.deploy"))) { // NOI18N
            return null;
        }
        boolean optional = false;
        for (String opt : optionalPackages) {
            if (name.startsWith(opt)) {
                optional = true;
                break;
            }
        }
        
        if (!optional) {
            return null;
        }
        Tree t = treePath.getLeaf();
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
        int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);
        // #205936
        if (start == -1 || end == -1 || end < start) {
            return null;
        }
        List<Fix> fixes = new ArrayList<Fix>();
        return Collections.<ErrorDescription>singletonList(
                ErrorDescriptionFactory.createErrorDescription(
                context.getSeverity(),
                Bundle.OptionalEE7APIsHint_DisplayName(),
                fixes,
                info.getFileObject(),
                start,
                end));
    }

    private OptionalEE7APIsHint() {}

}
