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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmTypeAlias;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.RefactoringModule;
import org.openide.awt.Mnemonics;


/**
 * Based on the WhereUsedPanel in Java refactoring by Jan Becicka.
 */
public class WhereUsedPanel extends JPanel implements CustomRefactoringPanel {

    private final transient CsmObject origObject;
    private transient CsmUID<CsmObject> refObjectUID;

    private final transient ChangeListener parent;
    private String name;
    private Scope defaultScope;
    /** Creates new form WhereUsedPanel */
    public WhereUsedPanel(String name, CsmObject csmObject,ChangeListener parent) {
        setName(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")); // NOI18N
        this.origObject = csmObject;
        this.parent = parent;
        this.name = name;
        this.defaultScope = Scope.CURRENT;
        initComponents();
    }

    public enum Scope {
        ALL,
        CURRENT,
        USER_SPECIFIED
    };
    
    private static final class ProjectScope {
        JLabel label;
        CsmProject project;

        public ProjectScope(JLabel label, CsmProject project) {
            this.label = label;
            this.project = project;
        }

        public CsmProject getProject() {
            return project;
        }

        public JLabel getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label == null? "null" : label.getText(); // NOI18N
        }
    }

    public CsmProject getScopeProject() {
        if (defaultScope == Scope.ALL) {
            return null;
        } else if (defaultScope == Scope.CURRENT) {
            return ((ProjectScope)scope.getItemAt(1)).getProject();
        } else {
            assert defaultScope == Scope.USER_SPECIFIED;
            return ((ProjectScope)scope.getSelectedItem()).getProject();
        }
    }
    
    private volatile boolean initialized = false;
    private CsmClass methodDeclaringSuperClass = null;
    private CsmClass methodDeclaringClass = null;
    private CsmMethod baseVirtualMethod = null;

    /*package*/ String getBaseMethodDescription() {
        if (baseVirtualMethod != null) {
            //CsmVisibility vis = baseVirtualMethod.getVisibility();
            String functionDisplayName = baseVirtualMethod.getSignature().toString();
            String displayClassName = methodDeclaringSuperClass.getName().toString();
            return getString("DSC_MethodUsages", functionDisplayName, displayClassName); // NOI18N
        } else {
            return name;
        }
    }

    /*package*/ CsmClass getMethodDeclaringClass() {
        return isMethodFromBaseClass() ? methodDeclaringSuperClass : methodDeclaringClass;
    }

    public void uninitialize() {
        initialized = false;
    }

    @Override
    public void initialize() {
        try {
            CsmCacheManager.enter();
            initializeImpl();
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    private void initializeImpl() {
        // method is called to make initialization of components out of AWT
        if (initialized) {
            return;
        }
        initFields();

        final List<ProjectScope> currentProjects;
        final ProjectScope allProjects;
        CsmObject refObject = getReferencedObject();
        CsmProject refObjectPrj = null;
        if (CsmKindUtilities.isOffsetable(refObject)) {
            CsmFile refObjFile = ((CsmOffsetable)refObject).getContainingFile();
            if (refObjFile != null) {
                refObjectPrj = refObjFile.getProject();
            }
        }
        if ((refObject != null) && !CsmKindUtilities.isLocalVariable(refObject)) {
            Collection<Project> ps = CsmRefactoringUtils.getContextProjects(this.origObject);
            if (!ps.isEmpty()) {
                defaultScope = Scope.USER_SPECIFIED;
                currentProjects = new ArrayList<>();
                Icon icon = null;
                for (Project p : ps) {
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    icon = pi.getIcon();
                    CsmProject prj = CsmModelAccessor.getModel().getProject(p);
                    ProjectScope prjScope = new ProjectScope(new JLabel(pi.getDisplayName(), icon, SwingConstants.LEFT), prj);
                    currentProjects.add(prjScope);
                }
                allProjects = new ProjectScope(new JLabel(NbBundle.getMessage(WhereUsedPanel.class, "LBL_AllProjects"), icon, SwingConstants.LEFT), null); // NOI18N
            } else {
                defaultScope = Scope.ALL;
                currentProjects = null;
                allProjects = null;
            }
        } else if (CsmKindUtilities.isLocalVariable(refObject) && refObjectPrj != null) {
            defaultScope = Scope.CURRENT;
            ProjectScope prjScope = new ProjectScope(null, refObjectPrj);
            currentProjects = new ArrayList<>();
            currentProjects.add(prjScope);
            allProjects = null;
        } else {
            defaultScope = Scope.ALL;
            currentProjects = null;
            allProjects = null;
        }
        String labelText;
        String _isBaseClassText = null;
        boolean _needVirtualMethodPanel = false;
        boolean _needClassPanel = false;
        if (CsmKindUtilities.isMethod(refObject)) {
            CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) refObject);
//            CsmVisibility vis = ((CsmMember)refObject).getVisibility();
            String functionDisplayName = CsmDisplayUtilities.htmlize(method.getSignature().toString());
            methodDeclaringClass = method.getContainingClass();
            String displayClassName = methodDeclaringClass.getName().toString();
            labelText = getString("DSC_MethodUsages", functionDisplayName, displayClassName); // NOI18N
            CsmVirtualInfoQuery query = CsmVirtualInfoQuery.getDefault();
            if (query.isVirtual(method)) {
                Collection<CsmMethod> baseMethods = query.getTopmostBaseDeclarations(method);
                // use only the first for now
                baseVirtualMethod = baseMethods.isEmpty() ? method : baseMethods.iterator().next();
                assert baseVirtualMethod != null : "virtual method must have start virtual declaration";
                methodDeclaringSuperClass = baseVirtualMethod.getContainingClass();
                if (!method.equals(baseVirtualMethod)) {
                    _isBaseClassText = getString("LBL_UsagesOfBaseClass", methodDeclaringSuperClass.getName().toString()); // NOI18N
                }
                _needVirtualMethodPanel = true;
            }
        } else if (CsmKindUtilities.isFunction(refObject)) {
            String functionFQN = ((CsmFunction)refObject).getSignature().toString();
            functionFQN = CsmDisplayUtilities.htmlize(functionFQN);
            labelText = getString("DSC_FunctionUsages", functionFQN); // NOI18N
        } else if (CsmKindUtilities.isClass(refObject)) {
            CsmDeclaration.Kind classKind = ((CsmDeclaration)refObject).getKind();
            String key;
            if (classKind == CsmDeclaration.Kind.STRUCT) {
                key = "DSC_StructUsages"; // NOI18N
            } else if (classKind == CsmDeclaration.Kind.UNION) {
                key = "DSC_UnionUsages"; // NOI18N
            } else {
                key = "DSC_ClassUsages"; // NOI18N
            }
            labelText = getString(key, ((CsmClassifier)refObject).getQualifiedName().toString());
            _needClassPanel = true;
        } else if (CsmKindUtilities.isTypedef(refObject)) {
            String tdName = ((CsmTypedef)refObject).getQualifiedName().toString();
            labelText = getString("DSC_TypedefUsages", tdName); // NOI18N
        } else if (CsmKindUtilities.isTypeAlias(refObject)) {
            String taName = ((CsmTypeAlias)refObject).getQualifiedName().toString();
            labelText = getString("DSC_TypeAliasUsages", taName); // NOI18N            
        } else if (CsmKindUtilities.isEnum(refObject)) {
            labelText = getString("DSC_EnumUsages", ((CsmEnum)refObject).getQualifiedName().toString()); // NOI18N
        } else if (CsmKindUtilities.isEnumerator(refObject)) {
            CsmEnumerator enmtr = ((CsmEnumerator)refObject);
            labelText = getString("DSC_EnumeratorUsages", enmtr.getName().toString(), enmtr.getEnumeration().getName().toString()); // NOI18N
        } else if (CsmKindUtilities.isField(refObject)) {
            String fieldName = ((CsmField)refObject).getName().toString();
            String displayClassName = ((CsmField)refObject).getContainingClass().getName().toString();
            labelText = getString("DSC_FieldUsages", fieldName, displayClassName); // NOI18N
        } else if (CsmKindUtilities.isVariable(refObject)) {
            String varName = ((CsmVariable)refObject).getName().toString();
            labelText = getString("DSC_VariableUsages", varName); // NOI18N
        } else if (CsmKindUtilities.isFile(refObject)) {
            String fileName = ((CsmFile)refObject).getName().toString();
            labelText = getString("DSC_FileUsages", fileName); // NOI18N
        } else if (CsmKindUtilities.isNamespace(refObject)) {
            String nsName = ((CsmNamespace)refObject).getQualifiedName().toString();
            labelText = getString("DSC_NamespaceUsages", nsName); // NOI18N
//        } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
//            String methodName = element.getName();
//            String className = getClassName(element);
//            labelText = getFormattedString("DSC_ConstructorUsages", methodName, className); // NOI18N
        } else if (CsmKindUtilities.isMacro(refObject)) {
            StringBuilder macroName = new StringBuilder(((CsmMacro)refObject).getName());
            if (((CsmMacro)refObject).getParameters() != null) {
                macroName.append("("); // NOI18N
                Iterator<CharSequence> params = ((CsmMacro)refObject).getParameters().iterator();
                if (params.hasNext()) {
                    macroName.append(params.next());
                    while (params.hasNext()) {
                        macroName.append(", "); // NOI18N
                        macroName.append(params.next());
                    }
                }
                macroName.append(")"); // NOI18N
            }
            labelText = getString("DSC_MacroUsages", macroName.toString()); // NOI18N
        } else if (CsmKindUtilities.isQualified(refObject)) {
            labelText = ((CsmQualifiedNamedElement)refObject).getQualifiedName().toString();
        } else if (refObject != null) {
            labelText = this.name;
        } else {
            labelText = getString("DSC_ElNotAvail", this.name); // NOI18N
        }

        final StringBuilder buf = new StringBuilder();
        int col = 0;
        for(int i = 0; i < labelText.length(); i++) {
            char c = labelText.charAt(i);
            col++;
            buf.append(c);
            if (col > 72 && (c == ' ' || c == ',')) { // NOI18N
                buf.append("<br>"); // NOI18N
                col = 0;
            }
        }

        if (refObject != null) {
            this.name = labelText;
        }
        
//        final Set<Modifier> modifiers = modif;
        final String isBaseClassText = _isBaseClassText;
        final boolean showMethodPanel = _needVirtualMethodPanel;
        final boolean showClassPanel = _needClassPanel;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                remove(classesPanel);
                remove(methodsPanel);
                label.setText(buf.toString());
                // WARNING for now since this feature is not ready yet
//                String combinedLabelText = "<html><font style=\"color: red\">WARNING: This feature is in development and inaccurate!</font><br><br>" + labelText + "</html>"; // NOI18N
//                label.setText(combinedLabelText);
                if (showMethodPanel) {
                    add(methodsPanel, BorderLayout.CENTER);
                    methodsPanel.setVisible(true);
                    if (isBaseClassText != null) {
                        Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
                        m_isBaseClass.setVisible(true);
                        m_isBaseClass.setSelected(true);
                    } else {
                        m_isBaseClass.setVisible(false);
                        m_isBaseClass.setSelected(false);
                    }
//                    if (methodDeclaringSuperClass != null) {
//                        m_overriders.setVisible(true);
//                        m_isBaseClass.setVisible(true);
//                        m_isBaseClass.setSelected(true);
//                        Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
//                    } else {
//                        m_overriders.setVisible(false);
//                        m_isBaseClass.setVisible(false);
//                        m_isBaseClass.setSelected(false);
//                    }                    
                } else if (showClassPanel) {
                    add(classesPanel, BorderLayout.CENTER);
                    classesPanel.setVisible(true);   
                } else {
//                if (element.getKind() == ElementKind.METHOD) {
//                    add(methodsPanel, BorderLayout.CENTER);
//                    methodsPanel.setVisible(true);
//                    m_usages.setVisible(!modifiers.contains(Modifier.STATIC));
//                    // TODO - worry about frozen?
//                    m_overriders.setVisible(modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE));
//                    if (methodDeclaringSuperClass != null) {
//                        m_isBaseClass.setVisible(true);
//                        m_isBaseClass.setSelected(true);
//                        Mnemonics.setLocalizedText(m_isBaseClass, isBaseClassText);
//                    } else {
//                        m_isBaseClass.setVisible(false);
//                        m_isBaseClass.setSelected(false);
//                    }
//                } else if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.MODULE)) {
//                    add(classesPanel, BorderLayout.CENTER);
//                    classesPanel.setVisible(true);
//                } else {
//                    remove(classesPanel);
//                    remove(methodsPanel);
//                    c_subclasses.setVisible(false);
//                    m_usages.setVisible(false);
//                    c_usages.setVisible(false);
//                    c_directOnly.setVisible(false);
                }
                if (currentProjects != null) {
                    Object[] model = new Object[currentProjects.size() + 1];
                    model[0] = allProjects;
                    for (int i = 0; i < currentProjects.size(); i++) {
                        model[i+1] = currentProjects.get(i);
                    }
                    scope.setModel(new DefaultComboBoxModel(model));
                    scope.setRenderer(new JLabelRenderer());
                    if (defaultScope == Scope.CURRENT) {
                        scopePanel.setVisible(false);
                        scope.setSelectedIndex(1);
                    } else {
                        int defaultItem = (Integer) RefactoringModule.getOption("whereUsed.scope", 0); // NOI18N
                        scope.setSelectedIndex(defaultItem);
                    }
                } else {
                    scopePanel.setVisible(false);
                }                
                validate();
            }
        });

        initialized = true;
    }

    /*package*/ CsmMethod getBaseMethod() {
        return baseVirtualMethod;
    }

    /*package*/ CsmObject getReferencedObject() {
        return refObjectUID == null ? null : refObjectUID.getObject();
    }

    /*package*/ String getDescription() {
        return name;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        methodsPanel = new javax.swing.JPanel();
        m_isBaseClass = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        m_overriders = new javax.swing.JCheckBox();
        m_usages = new javax.swing.JCheckBox();
        classesPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        c_subclasses = new javax.swing.JRadioButton();
        c_usages = new javax.swing.JRadioButton();
        c_directOnly = new javax.swing.JRadioButton();
        commentsPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        searchInComments = new javax.swing.JCheckBox();
        scopePanel = new javax.swing.JPanel();
        scopeLabel = new javax.swing.JLabel();
        scope = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        methodsPanel.setLayout(new java.awt.GridBagLayout());

        m_isBaseClass.setSelected(true);
        m_isBaseClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_isBaseClassActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_isBaseClass, gridBagConstraints);
        m_isBaseClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_isBaseClass")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        methodsPanel.add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(m_overriders, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindOverridingMethods")); // NOI18N
        m_overriders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_overridersActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_overriders, gridBagConstraints);
        m_overriders.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindOverridingMethods")); // NOI18N
        m_overriders.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_overriders")); // NOI18N

        m_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(m_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        m_usages.setMargin(new java.awt.Insets(10, 2, 2, 2));
        m_usages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_usagesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        methodsPanel.add(m_usages, gridBagConstraints);
        m_usages.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        m_usages.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_usages")); // NOI18N

        add(methodsPanel, java.awt.BorderLayout.CENTER);

        classesPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        classesPanel.add(jPanel2, gridBagConstraints);

        buttonGroup.add(c_subclasses);
        org.openide.awt.Mnemonics.setLocalizedText(c_subclasses, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindAllSubtypes")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_subclasses, gridBagConstraints);
        c_subclasses.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_subclasses")); // NOI18N

        buttonGroup.add(c_usages);
        c_usages.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(c_usages, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindUsages")); // NOI18N
        c_usages.setMargin(new java.awt.Insets(4, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_usages, gridBagConstraints);
        c_usages.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_usages")); // NOI18N

        buttonGroup.add(c_directOnly);
        org.openide.awt.Mnemonics.setLocalizedText(c_directOnly, org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_FindDirectSubtypesOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        classesPanel.add(c_directOnly, gridBagConstraints);
        c_directOnly.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("ACSD_directOnly")); // NOI18N

        add(classesPanel, java.awt.BorderLayout.CENTER);

        commentsPanel.setLayout(new java.awt.BorderLayout());
        commentsPanel.add(label, java.awt.BorderLayout.NORTH);

        searchInComments.setSelected(((Boolean) RefactoringModule.getOption("searchInComments.whereUsed", //NOI18N
            Boolean.FALSE)).booleanValue());
org.openide.awt.Mnemonics.setLocalizedText(searchInComments, org.openide.util.NbBundle.getBundle(WhereUsedPanel.class).getString("LBL_SearchInComents")); // NOI18N
searchInComments.setMargin(new java.awt.Insets(10, 14, 2, 2));
searchInComments.addItemListener(new java.awt.event.ItemListener() {
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        searchInCommentsItemStateChanged(evt);
    }
    });
    commentsPanel.add(searchInComments, java.awt.BorderLayout.CENTER);
    searchInComments.getAccessibleContext().setAccessibleDescription(searchInComments.getText());

    add(commentsPanel, java.awt.BorderLayout.NORTH);

    scopeLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_Scope_MNEM").charAt(0));
    scopeLabel.setLabelFor(scope);
    scopeLabel.setText(org.openide.util.NbBundle.getMessage(WhereUsedPanel.class, "LBL_Scope")); // NOI18N

    scope.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            scopeActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout scopePanelLayout = new javax.swing.GroupLayout(scopePanel);
    scopePanel.setLayout(scopePanelLayout);
    scopePanelLayout.setHorizontalGroup(
        scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(scopePanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(scopeLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(scope, 0, 296, Short.MAX_VALUE)
            .addContainerGap())
    );
    scopePanelLayout.setVerticalGroup(
        scopePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
        .addComponent(scopeLabel)
        .addComponent(scope, javax.swing.GroupLayout.PREFERRED_SIZE, 20, Short.MAX_VALUE)
    );

    add(scopePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void searchInCommentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchInCommentsItemStateChanged
        // used for change default value for searchInComments check-box.
        // The value is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption("searchInComments.whereUsed", b); // NOI18N
    }//GEN-LAST:event_searchInCommentsItemStateChanged

    private void m_isBaseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_isBaseClassActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_isBaseClassActionPerformed

    private void m_overridersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_overridersActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_overridersActionPerformed

    private void m_usagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_usagesActionPerformed
        parent.stateChanged(null);
    }//GEN-LAST:event_m_usagesActionPerformed

    private void scopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeActionPerformed
        RefactoringModule.setOption("whereUsed.scope", scope.getSelectedIndex()); // NOI18N
    }//GEN-LAST:event_scopeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton c_directOnly;
    private javax.swing.JRadioButton c_subclasses;
    private javax.swing.JRadioButton c_usages;
    private javax.swing.JPanel classesPanel;
    private javax.swing.JPanel commentsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel label;
    private javax.swing.JCheckBox m_isBaseClass;
    private javax.swing.JCheckBox m_overriders;
    private javax.swing.JCheckBox m_usages;
    private javax.swing.JPanel methodsPanel;
    private javax.swing.JComboBox scope;
    private javax.swing.JLabel scopeLabel;
    private javax.swing.JPanel scopePanel;
    private javax.swing.JCheckBox searchInComments;
    // End of variables declaration//GEN-END:variables

    public boolean isMethodFromBaseClass() {
        return m_isBaseClass.isSelected();
    }

    public boolean isMethodOverriders() {
        return m_overriders.isSelected();
    }

    public boolean isClassSubTypes() {
        return c_subclasses.isSelected();
    }

    public boolean isClassSubTypesDirectOnly() {
        return c_directOnly.isSelected();
    }

    public boolean isMethodFindUsages() {
        return m_usages.isSelected();
    }

    public boolean isClassFindUsages() {
        return c_usages.isSelected();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30, orig.height + 80);
    }

    public boolean isSearchInComments() {
        return searchInComments.isSelected();
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
    /*package*/ boolean isVirtualMethod() {
        return baseVirtualMethod != null;
    }
    
    /*package*/ boolean isClass() {
        return CsmKindUtilities.isClass(getReferencedObject());
    }
    
    private void initFields() {
        final CsmObject refObject = getReferencedElement(origObject);
        this.refObjectUID = CsmRefactoringUtils.getHandler(refObject);
        this.name = getSearchElementName(refObject, this.name);
        //System.err.println("initFields: refObject=" + refObject + "\n");
    }
    
    private CsmObject getReferencedElement(CsmObject csmObject) {
        CsmObject out;
        if (csmObject instanceof CsmReference) {
            out = getReferencedElement(((CsmReference)csmObject).getReferencedObject());
        } else {
            out = csmObject;
        }
        return CsmRefactoringUtils.convertToCsmObjectIfNeeded(out);
    }
    
    private String getSearchElementName(CsmObject csmObj, String defaultName) {
        String objName;
        if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName().toString();
        } else {
            System.err.println("Unhandled name for object " + csmObj);
            objName = defaultName;
        }
        return objName;
    }   

    private CsmMethod getOriginalVirtualMethod(CsmMethod csmMethod) {
        return csmMethod;
    }

    private String getString(String key) {
        return NbBundle.getBundle(WhereUsedPanel.class).getString(key);
    }
    
    private String getString(String key, String value) {
        return NbBundle.getMessage(WhereUsedPanel.class, key, value);
    }    
    
    private String getString(String key, String value1, String value2) {
        return NbBundle.getMessage(WhereUsedPanel.class, key, value1, value2);
    }    
    
    private static class JLabelRenderer extends JLabel implements ListCellRenderer {
        public JLabelRenderer () {
            setOpaque(true);
        }
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null && ((ProjectScope)value).getLabel() != null ) {
                setText(((ProjectScope)value).getLabel().getText());
                setIcon(((ProjectScope)value).getLabel().getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }    
}
