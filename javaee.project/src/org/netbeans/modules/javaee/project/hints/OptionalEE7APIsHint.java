/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
