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

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author jprox
 */
public class AllLanguageTabsAndIndentsOperator extends FormattingPanelOperator {

    public static final String TABS__AND__INDENTS = "Tabs And Indents";
    public static final String ALL__LANGUAGES = "All Languages";

    enum Settings implements OperatorGetter {

        EXPANDTAB {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getExpandTabsToSpaces();
                    }
                },
        NUMBEROFSPACES {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getNumberOfSpacesPerIndent();
                    }
                },
        RIGHTMARGIN {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getRightMargin();
                    }
                },
        TABSIZE {
                    @Override
                    public Operator getOperator(FormattingPanelOperator fpo) {
                        return ((AllLanguageTabsAndIndentsOperator) fpo).getTabSize();
                    }
                };

        @Override
        public String key() {
             return this.name()+"_ALLLANGUAGES";
        }                
    }

    public AllLanguageTabsAndIndentsOperator(final FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, ALL__LANGUAGES, TABS__AND__INDENTS);
        switchToPanel();
    }

    protected AllLanguageTabsAndIndentsOperator(final FormattingOptionsOperator formattingOperator, String language) {
        super(formattingOperator, language, TABS__AND__INDENTS);
        switchToPanel();
    }

    @Override
    public Component getSource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private JCheckBoxOperator expandTabsToSpaces;
    private JSpinnerOperator numberOfSpacesPerIndent;
    private JSpinnerOperator tabSize;
    private JSpinnerOperator rightMargin;

    public JCheckBoxOperator getExpandTabsToSpaces() {
        if (expandTabsToSpaces == null) {
            expandTabsToSpaces = formattingOperator.getCheckboxOperatorByLabel("Expand Tabs to Spaces");
            storeDefaultValue(Settings.EXPANDTAB);
        }
        return expandTabsToSpaces;
    }

    public JSpinnerOperator getNumberOfSpacesPerIndent() {
        if (numberOfSpacesPerIndent == null) {
            numberOfSpacesPerIndent = formattingOperator.getSpinnerOperatorByLabel("Number of Spaces per Indent:");
            storeDefaultValue(Settings.NUMBEROFSPACES);
        }
        return numberOfSpacesPerIndent;
    }

    public JSpinnerOperator getTabSize() {
        if (tabSize == null) {
            tabSize = formattingOperator.getSpinnerOperatorByLabel("Tab Size:");
            storeDefaultValue(Settings.TABSIZE);
        }
        return tabSize;
    }

    public JSpinnerOperator getRightMargin() {
        if (rightMargin == null) {
            rightMargin = formattingOperator.getSpinnerOperatorByLabel("Right Margin:");
            storeDefaultValue(Settings.RIGHTMARGIN);
        }
        return rightMargin;

    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[])Settings.values());
    }       
}
