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
package org.netbeans.modules.groovy.refactoring.ui;

import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.VariableRefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * WhereUsedQueryUI from the Java refactoring module, only moderately modified for Groovy
 * 
 * @author Martin Matula
 * @author Jan Becicka
 * @author Martin Janicek
 */
public class WhereUsedQueryUI implements RefactoringUI {

    private final RefactoringElement element;
    private final WhereUsedQuery query;
    private final ElementKind kind;
    private final String name;
    private WhereUsedPanel panel;

    
    public WhereUsedQueryUI(RefactoringElement element) {
        this.query = new WhereUsedQuery(Lookups.singleton(element));
        this.query.getContext().add(GroovyProjectUtil.getClasspathInfoFor(element.getFileObject()));
        this.element = element;
        this.name = getElementName();
        this.kind = element.getKind();
    }

    private String getElementName() {
        if (element instanceof VariableRefactoringElement) {
            return ((VariableRefactoringElement) element).getVariableName();
        }
        return element.getName();
    }
    
    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = WhereUsedPanel.create(element, parent);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS,panel.isSearchInComments());
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return query.checkParameters();
        } else if (kind == ElementKind.MODULE || kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
            setForClass();
            return query.checkParameters();
        } else
            return null;
    }
    
    private void setForMethod() {
        query.getContext().add(element);
        query.setRefactoringSource(Lookups.singleton(element));
        query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, panel.isMethodFromBaseClass());
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, panel.isMethodOverriders());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES, panel.isClassFindUsages());
    }
    
    @Override
    public Problem checkParameters() {
        if (kind == ElementKind.METHOD) {
            return query.fastCheckParameters();
        } else if (kind == ElementKind.MODULE || kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
            return query.fastCheckParameters();
        } else
            return null;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return query;
    }

    @Override
    public String getDescription() {
        String desc = null;

        if (panel != null && kind != null) {
            switch (kind) {
                case CONSTRUCTOR: {
                    if (panel.isMethodFindUsages() && panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedAndOverriders", name);
                    } else if (panel.isMethodFindUsages()) {
                        desc = getString("DSC_WhereUsed",  name);
                    } else if (panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedMethodOverriders",  name);
                    }
                    break;
                }
                case METHOD: {
                    if (panel.isMethodFindUsages() && panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedFindUsagesAndMethodOverriders", element.getOwnerNameWithoutPackage(), element.getName());
                    } else if (panel.isMethodFindUsages()) {
                        desc = getString("DSC_WhereUsedFindUsages", element.getOwnerNameWithoutPackage(), element.getName());
                    } else if (panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedMethodOverriders", element.getOwnerNameWithoutPackage(), element.getName());
                    }
                    break;
                }
                case MODULE:
                case CLASS:
                case INTERFACE: {
                    if (!panel.isClassFindUsages()) {
                        if (!panel.isClassSubTypesDirectOnly()) {
                            desc = getString("DSC_WhereUsedFindAllSubTypes", name);
                        } else {
                            desc = getString("DSC_WhereUsedFindDirectSubTypes", name);
                        }
                    }
                    break;
                }
                case PACKAGE: {
                    break;
                }
                case FIELD:
                default: {
                    break;
                }
            }
        }
        if (desc == null) {
            desc = getString("DSC_WhereUsed", name);
        }
        return desc;
    }
    
    private String getString(String key, String ... values) {
        return new MessageFormat(NbBundle.getMessage(WhereUsedQueryUI.class, key)).format (values);
    }

    @Override
    public String getName() {
        return getString("DSC_WhereUsed", name);
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.groovy.refactoring.ui.WhereUsedQueryUI");
    }
}
