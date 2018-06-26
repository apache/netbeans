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
package org.netbeans.modules.web.el.hints;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.Node;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ResourceBundles;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Hint for checking unknown resource bundle keys.
 *
 * @author Erno Mononen
 */
public final class ResourceBundleKeys extends ELRule {

    private ResourceBundles resourceBundles;

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(ELHintsProvider.Kind.DEFAULT);
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        FileObject fo = context.parserResult.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return false;
        }
        this.resourceBundles = ResourceBundles.get(fo);
        return resourceBundles.canHaveBundles();
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    protected void run(CompilationContext info, RuleContext context, List<Hint> result) {
        ELParserResult elResult = (ELParserResult)context.parserResult;
        for (ELElement each : elResult.getElements()) {
            if (!each.isValid()) {
                // broken AST, skip (perhaps could try just plain string search)
                continue;
            }
            for (Pair<AstIdentifier, Node> pair : resourceBundles.collectKeys(each.getNode(), info.context())) {
                String clearedKey = pair.second().getImage().replace("'", "").replace("\"", "");
                if (!resourceBundles.isValidKey(pair.first().getImage(), clearedKey)) {
                    Hint hint = new Hint(this,
                            NbBundle.getMessage(ResourceBundleKeys.class, "ResourceBundleKeys_Unknown", clearedKey),
                            elResult.getFileObject(),
                            each.getOriginalOffset(pair.second()),
                            Collections.<HintFix>emptyList(), 200);
                    result.add(hint);
                }
            }
        }
    }
}
