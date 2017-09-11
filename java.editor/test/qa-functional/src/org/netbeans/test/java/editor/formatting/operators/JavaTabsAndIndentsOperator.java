/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.formatting.operators;

import java.util.Arrays;
import java.util.List;
import org.netbeans.jemmy.operators.*;

public class JavaTabsAndIndentsOperator extends AllLanguageTabsAndIndentsOperator {

    public static final String JAVA__LANGUAGES = "Java";

    public JavaTabsAndIndentsOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, JAVA__LANGUAGES);
    }

    enum Settings implements OperatorGetter {

        JAVA_EXPANDTAB {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getExpandTabsToSpaces();
                    }
                },
        JAVA_NUMBEROFSPACES {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getNumberOfSpacesPerIndent();
                    }
                },
        JAVA_RIGHTMARGIN {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getRightMargin();
                    }
                },
        JAVA_TABSIZE {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getTabSize();
                    }
                },        
        JAVA_CONTINUATIONINDENTATION {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getContinuationIndentationSize();
                    }
                },
        JAVA_LABELINDENTATION {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getLabelIndentation();
                    }
                },
        JAVA_ABSOLUTELABELINDENTATION {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getAbsoluteLabelIndentation();
                    }
                },
        JAVA_INDENTTOPLEVELMEMBERS {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getIndentTopLevelClassMembers();
                    }
                },
        JAVA_INDENTCASE {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getIndentCaseStatementsInSwitch();
                    }
                },
        JAVA_USEALLLANGUAGES {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((JavaTabsAndIndentsOperator) fpo).getUseAllLanguages();
                    }

                },;

        @Override
        public String key() {
            return this.name() + "_JAVA";
        }
    }

    private JCheckBoxOperator useAllLanguages;
    private JTextFieldOperator continuationIndentationSize;
    private JTextFieldOperator labelIndentation;
    private JCheckBoxOperator absoluteLabelIndentation;
    private JCheckBoxOperator indentTopLevelClassMembers;
    private JCheckBoxOperator indentCaseStatementsInSwitch;

    public JCheckBoxOperator getUseAllLanguages() {
        if (useAllLanguages == null) {
            useAllLanguages = formattingOperator.getCheckboxOperatorByLabel("Use All Languages Settings");
            storeDefaultValue(Settings.JAVA_USEALLLANGUAGES);
        }
        return useAllLanguages;
    }

    public JTextFieldOperator getContinuationIndentationSize() {
        if (continuationIndentationSize == null) {
            continuationIndentationSize = formattingOperator.getTextFieldByLabel("Continuation Indentation Size:");
            storeDefaultValue(Settings.JAVA_CONTINUATIONINDENTATION);
        }
        return continuationIndentationSize;
    }

    public JTextFieldOperator getLabelIndentation() {
        if (labelIndentation == null) {
            labelIndentation = formattingOperator.getTextFieldByLabel("Label Indentation:");
            storeDefaultValue(Settings.JAVA_LABELINDENTATION);
        }
        return labelIndentation;
    }

    public JCheckBoxOperator getAbsoluteLabelIndentation() {
        if (absoluteLabelIndentation == null) {
            absoluteLabelIndentation = formattingOperator.getCheckboxOperatorByLabel("Absolute Label Indentation");
            storeDefaultValue(Settings.JAVA_ABSOLUTELABELINDENTATION);
        }
        return absoluteLabelIndentation;
    }

    public JCheckBoxOperator getIndentTopLevelClassMembers() {
        if (indentTopLevelClassMembers == null) {
            indentTopLevelClassMembers = formattingOperator.getCheckboxOperatorByLabel("Indent Top Level Class Members");
            storeDefaultValue(Settings.JAVA_INDENTTOPLEVELMEMBERS);
        }
        return indentTopLevelClassMembers;
    }

    public JCheckBoxOperator getIndentCaseStatementsInSwitch() {
        if (indentCaseStatementsInSwitch == null) {
            indentCaseStatementsInSwitch = formattingOperator.getCheckboxOperatorByLabel("Indent Case Statements In Switch");
            storeDefaultValue(Settings.JAVA_INDENTCASE);
        }
        return indentCaseStatementsInSwitch;
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) Settings.values());
    }

}
