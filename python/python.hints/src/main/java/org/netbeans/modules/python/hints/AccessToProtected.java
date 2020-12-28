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
package org.netbeans.modules.python.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.NameStyle;
import org.openide.util.NbBundle;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;

/**
 * Check direct acces to parent protected variables or methods
 * @author jean-yves Mengant
 */
public class AccessToProtected extends PythonAstRule {
    private final static String ACCESS_PROTECTED_ID = "AccessProtected"; // NOI18N
    private final static String ACCESS_PROTECTED_VARIABLE = "AccessProtectedVariable"; // NOI18N
    private final static String ACCESS_PROTECTED_DESC = "AccessProtectedDesc"; // NOI18N

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Attribute.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        Attribute cur = (Attribute)context.node;
        String curAttr = cur.getInternalAttr();
        if (curAttr == null) {
            return;
        }

        if (NameStyle.isProtectedName(curAttr)) {
            expr curValue = cur.getInternalValue();
            if (curValue instanceof Name) {
                Name nam = (Name)curValue;
                String id = nam.getInternalId();
                if (id.equals("self")) { // NOI18N
                    return; // normal access from class instance
                }
                if (PythonAstUtils.getParentClassFromNode(context.path, null, id) != null) {
                    return; // parent access
                }
                // we should warn here : Access to protected Attributes from non child
                // classes
                OffsetRange range = PythonAstUtils.getRange(cur);
                range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = Collections.emptyList();
                    String message = NbBundle.getMessage(NameRule.class, ACCESS_PROTECTED_VARIABLE, curAttr);
                    Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }
    }

    @Override
    public String getId() {
        return ACCESS_PROTECTED_ID;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, ACCESS_PROTECTED_DESC);
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AccessToProtected.class, ACCESS_PROTECTED_ID);
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
}
