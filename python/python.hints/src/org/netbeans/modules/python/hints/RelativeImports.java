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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.source.PythonParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.ImportFrom;

/**
 * Import statements should be one per line. This quickfix
 * offers to make it so.
 *
 * @todo Ensure that
 *  {@code from __future__ import absolute_import}
 *   is present, at least until Python 2.7
 *
 */
public class RelativeImports extends PythonAstRule {
    @Override
    public Set<Class> getKinds() {
        return Collections.singleton((Class)ImportFrom.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        ImportFrom imp = (ImportFrom)context.node;
        if (imp.getInternalModule() != null && imp.getInternalModule().startsWith(".")) {
            PythonTree node = context.node;
            PythonParserResult info = (PythonParserResult) context.parserResult;
            OffsetRange astOffsets = PythonAstUtils.getNameRange(info, node);
            OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
            BaseDocument doc = context.doc;
            try {
                if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                        (context.caretOffset == -1 ||
                        Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                    List<HintFix> fixList = new ArrayList<>();
                    fixList.add(new RelativeImportsFix(context, imp));
                    String displayName = getDisplayName();
                    Hint desc = new Hint(this, displayName, info.getSnapshot().getSource().getFileObject(), lexOffsets, fixList, 1500);
                    result.add(desc);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public String getId() {
        return "RelativeImports"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RelativeImports.class, "RelativeImports");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, "RelativeImportsDesc");
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

    private static class RelativeImportsFix implements PreviewableFix {
        private final PythonRuleContext context;
        private final ImportFrom imp;

        private RelativeImportsFix(PythonRuleContext context, ImportFrom imp) {
            this.context = context;
            this.imp = imp;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(RelativeImports.class, "RelativeImportsFix");
        }

        @Override
        public boolean canPreview() {
            return true;
        }

        @Override
        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            // Algorithm:
            //  (1) Figure out which package we are in
            //  (2) Subtrack package elements per dot
            //  (3) Replace relative reference

            OffsetRange astRange = PythonAstUtils.getRange(imp);
            if (astRange != OffsetRange.NONE) {
                PythonParserResult info = (PythonParserResult)context.parserResult;
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                if (lexRange != OffsetRange.NONE) {
                    FileObject fo = info.getSnapshot().getSource().getFileObject();
                    if (fo != null) {
                        String path = imp.getInternalModule();
                        int i = 0;
                        for (; i < path.length(); i++) {
                            if (path.charAt(i) != '.') {
                                break;
                            }
                        }
                        int levels = i;
                        path = path.substring(levels);

                        for (int j = 0; j < levels; j++) {
                            if (fo != null) {
                                fo = fo.getParent();
                            }
                        }

                        // Finally, find out the absolute path we are in
                        // Hopefully, I will have access to the python load path
                        // here. But in the mean time, I can just see which 
                        // packages I am in...
                        while (fo != null) {
                            if (fo.getFileObject("__init__.py") != null) { // NOI18N
                                // Yep, we're still in a package
                                if (path.length() > 0) {
                                    path = fo.getName() + "." + path; // NOI18N
                                } else {
                                    path = fo.getName();
                                }
                            }
                            fo = fo.getParent();
                        }
                        String text = doc.getText(lexRange.getStart(), lexRange.getLength());
                        int relativePos = text.indexOf(imp.getInternalModule());
                        if (relativePos != -1) {
                            edits.replace(lexRange.getStart() + relativePos, imp.getInternalModule().length(), path, false, 0);
                        }
                    }
                }
            }

            return edits;
        }

        @Override
        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }
}
