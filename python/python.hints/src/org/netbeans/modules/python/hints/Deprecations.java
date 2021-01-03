/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.hints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.queries.DeprecationQuery;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;

/**
 * Handle deprecaton warnings, for modules listed as obsolete or
 * deprecated in PEP4:
 *   http://www.python.org/dev/peps/pep-0004/
 *
 * Todo: Add a hint to enforce this from PEP8:
- Comparisons to singletons like None should always be done with
'is' or 'is not', never the equality operators.
 *  In general, see the "Programming Recommendations" list from
 *    http://www.python.org/dev/peps/pep-0008/ - there are lots
 *    of thins to check from there.  Check the PyLint list as well.
 *
 *
 */
public class Deprecations extends PythonAstRule {

    @Override
    public Set<Class> getKinds() {
        HashSet<Class> kinds = new HashSet<>();
        kinds.add(Import.class);
        kinds.add(ImportFrom.class);

        return kinds;
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonTree node = context.node;
        if (node instanceof Import) {
            Import imp = (Import)node;
            List<alias> names = imp.getInternalNames();
            if (names != null) {
                for (alias alias : names) {
                    String name = alias.getInternalName();
                    if (DeprecationQuery.isDeprecatedModule(name)) {
                        addDeprecation(name, DeprecationQuery.getDeprecatedModuleDescription(name), context, result);
                    }
                }
            }
        } else {
            assert node instanceof ImportFrom;
            ImportFrom imp = (ImportFrom)node;
            String name = imp.getInternalModule();
            if (DeprecationQuery.isDeprecatedModule(name)) {
                addDeprecation(name, DeprecationQuery.getDeprecatedModuleDescription(name), context, result);
            }
        }
    }

    private void addDeprecation(String module, String rationale, PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        OffsetRange astOffsets = PythonAstUtils.getNameRange(info, context.node);
        OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
        BaseDocument doc = context.doc;
        try {
            if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                    (context.caretOffset == -1 ||
                    Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                List<HintFix> fixList = Collections.emptyList();
                String displayName;
                if (rationale.length() > 0) {
                    displayName = NbBundle.getMessage(Deprecations.class, "DeprecationsMsgDetail", module, rationale);
                } else {
                    displayName = NbBundle.getMessage(Deprecations.class, "DeprecationsMsg", module);
                }
                Hint desc = new Hint(this, displayName, info.getSnapshot().getSource().getFileObject(), lexOffsets, fixList, 1500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getId() {
        return "Deprecations"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Deprecations.class, "Deprecations");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(Deprecations.class, "DeprecationsDesc");
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
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
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
}
