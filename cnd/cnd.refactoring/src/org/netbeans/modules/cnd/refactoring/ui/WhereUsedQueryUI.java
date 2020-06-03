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
package org.netbeans.modules.cnd.refactoring.ui;

import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * WhereUsedQueryUI for C/C++
 * 
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private WhereUsedQuery query;
    private WhereUsedPanel panel;
    private final CsmObject origObject;
    private final String name;
    public WhereUsedQueryUI(CsmObject csmObject) {
        this.query = new WhereUsedQuery(Lookups.singleton(csmObject));
        this.origObject = csmObject;
        name = getSearchElementName(this.origObject);
    }
    
    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        // this method returns panel used for displaying config options
        // of refactoring/find usages
        // i.e. panel with checkboxes
        // called from AWT
        if (panel == null) {
            panel = new WhereUsedPanel(name, origObject, parent);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        try {
            // handle parameters defined in panel
            assert panel != null;
            query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS,panel.isSearchInComments());
            
            
            Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(this.origObject, panel.getScopeProject());
            CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
            query.getContext().add(ar);

            query.putValue(WhereUsedQueryConstants.READ_WRITE, false);
            CsmObject refObj = panel.getReferencedObject();
            if (refObj == null) {
                query.setRefactoringSource(Lookup.EMPTY);
            } else {
                if (CsmKindUtilities.isVariable(refObj)) {
                    query.putValue(WhereUsedQueryConstants.READ_WRITE, true);
                }
                query.setRefactoringSource(Lookups.singleton(CsmRefactoringUtils.getHandler(refObj)));
            }
            if (panel.isVirtualMethod()) {
                setForMethod();
                return query.checkParameters();
            } else if (panel.isClass()) {
                setForClass();
                return query.checkParameters();
            } else {
                return null;
            }
        } finally {
            panel.uninitialize();
        }
    }
    
    private void setForMethod() {
        assert panel != null;
//        if (panel.isMethodFromBaseClass()) {
//            CsmObject refObj = panel.getBaseMethod();
//            if (refObj == null) {
//                query.setRefactoringSource(Lookup.EMPTY);
//            } else {
//                query.setRefactoringSource(Lookups.singleton(CsmRefactoringUtils.getHandler(refObj)));
//            }
//        } else {
            CsmObject refObj = panel.getReferencedObject();
            if (refObj == null) {
                query.setRefactoringSource(Lookup.EMPTY);
            } else {
                query.setRefactoringSource(Lookups.singleton(CsmRefactoringUtils.getHandler(refObj)));
            }
//        }
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,panel.isMethodOverriders());
        query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS,panel.isMethodFromBaseClass());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        assert panel != null;
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES,panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES,panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isClassFindUsages());
    }
    
    @Override
    public Problem checkParameters() {
        assert panel != null;
        if (panel.isVirtualMethod()) {
            setForMethod();
            return query.fastCheckParameters();
        } else if (panel.isClass()) {
            setForClass();
            return query.fastCheckParameters();
        } else {
            return null;
        }
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return query;
    }

    @Override
    public String getDescription() {
        // this method returns description displayed in Find Usages tab
        // i.e. "Usages of "name" (2 occurrences]"
        if (panel!=null) {
            String description = panel.getDescription();
            String key = "DSC_WhereUsed"; // NOI18N
            if (panel.isClass()) {
                if (!panel.isClassFindUsages()) {
                    if (panel.isClassSubTypesDirectOnly()) {
                        key = "DSC_WhereUsedFindDirectSubTypes"; // NOI18N
                    } else {
                        key = "DSC_WhereUsedFindAllSubTypes"; // NOI18N
                    }
                }
            } else if (panel.isVirtualMethod()) {
                if (panel.isMethodFromBaseClass()) {
                    description = panel.getBaseMethodDescription();
                }
                if (panel.isMethodOverriders()) {
                    key = panel.isMethodFindUsages() ? 
                        "DSC_WhereUsedUsagesAndMethodOverriders" : // NOI18N
                        "DSC_WhereUsedMethodOverriders"; // NOI18N
                }
            }
            description = description.replace("<html>", "").replace("</html>", ""); // NOI18N
            return getString(key, description);
        }
        return getString("DSC_WhereUsed", name); // NOI18N
    }
    
    private String getString(String key, String value) {
        return NbBundle.getMessage(WhereUsedQueryUI.class, key, value);
    }

    @Override
    public String getName() {
        return getString("LBL_UsagesOf", name); // NOI18N
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }
    
    private String getSearchElementName(CsmObject csmObj) {
        String objName;
        if (csmObj instanceof CsmReference) {
            objName = ((CsmReference)csmObj).getText().toString();
        } else if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName().toString();
        } else if (CsmKindUtilities.isInclude(csmObj)) {
            objName = ((CsmInclude)csmObj).getIncludeName().toString();
        } else if (csmObj != null) {
            objName = "<UNNAMED ELEMENT>"; // NOI18N
        } else {
            CndUtils.assertUnconditional("Null parameter");
            objName = "<UNRESOLVED ELEMENT>"; // NOI18N
        }
        return objName;
    }     
}
