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
