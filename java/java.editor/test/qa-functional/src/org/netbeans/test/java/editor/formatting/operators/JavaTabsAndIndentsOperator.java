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
