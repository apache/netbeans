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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.simpleunit.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode.Description;
import org.netbeans.modules.cnd.simpleunit.spi.wizard.AbstractUnitTestIterator;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

public class GenerateTestChooseElementsWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GenerateTestChooseElementsVisualPanel component;
    private final String unitTestKind;
    private final ChangeSupport cs;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Task task;
    private WizardDescriptor wizard;
    
    public GenerateTestChooseElementsWizardPanel(String unitTestKind) {
        this.unitTestKind = unitTestKind;
        cs = new ChangeSupport(this);
    }


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public GenerateTestChooseElementsVisualPanel getComponent() {
        if (component == null) {
            component = new GenerateTestChooseElementsVisualPanel();
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(AbstractUnitTestIterator.CND_UNITTEST_KIND_CPPUNIT.equals(unitTestKind)?
            "CreateCppUnitTestWizardP1" : "CreateTestWizardP1"); // NOI18N
    }

    public boolean isValid() {
        return initialized.get();
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }

    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        cs.fireChange();
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(WizardDescriptor settings) {
        this.wizard = settings;
        if (!initialized.get() && task == null) {
            // initialization may take time
            component.showLoadingNode();
            Runnable runnable = new RunnableImpl();
            task = RequestProcessor.getDefault().post(runnable);
        }
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(AbstractUnitTestIterator.CND_UNITTEST_FUNCTIONS, component.getSelectedElements());
    }

    private final class RunnableImpl implements Runnable {
        public RunnableImpl() {
        }
        private ElementNode.Description topDescription = null;

        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                initialized.set(true);
                component.setRootElement(topDescription);
                fireChangeEvent();
            } else {
                try {
                    initializeTopLevelDescriptions();
                } finally {
                    SwingUtilities.invokeLater(this);
                }
            }
        }

        private void initializeTopLevelDescriptions() {
            Lookup lookup = (Lookup) wizard.getProperty("UnitTestContextLookup"); // NOI18N
            Parameters.notNull("Lookup must be initialized in 'UnitTestContextLookup' property of wizard", lookup); // NOI18N
            DataObject dob = lookup.lookup(DataObject.class);
            if (dob != null) {
                wizard.putProperty(AbstractUnitTestIterator.CND_UNITTEST_DEFAULT_NAME, dob.getName());
                CsmFile file = CsmUtilities.getCsmFile(dob, false, false);
                topDescription = null;
                if (file != null) {
                    Collection<CsmOffsetableDeclaration> delcs = file.getDeclarations();
                    Map<CsmScope, Collection<CsmFunction>> functions = new HashMap<CsmScope, Collection<CsmFunction>>();
                    extractFunctions(functions, delcs);
                    List<Description> topDescrs = convert(file, functions);
                    topDescription = ElementNode.Description.create(topDescrs);
                }
            }
        }

        private void extractFunctions(Map<CsmScope, Collection<CsmFunction>> functions, Collection<CsmOffsetableDeclaration> delcs) {
            for (CsmOffsetableDeclaration decl : delcs) {
                if (CsmKindUtilities.isFunction(decl)) {
                    CsmFunction fun = handleFunction((CsmFunction) decl);
                    if (fun != null) {
                        CsmScope scope = fun.getScope();
                        if (scope != null) {
                            Collection<CsmFunction> scopeFunctions = functions.get(scope);
                            if (scopeFunctions == null) {
                                scopeFunctions = new LinkedHashSet<CsmFunction>();
                            }
                            if (!scopeFunctions.contains(fun)) {
                                scopeFunctions.add(fun);
                                functions.put(scope, scopeFunctions);
                            }
                        }
                    }
                } else if (CsmKindUtilities.isClass(decl)) {
                    extractMethods(functions, (CsmClass) decl);
                } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    CsmNamespaceDefinition nsDef = (CsmNamespaceDefinition) decl;
                    extractFunctions(functions, nsDef.getDeclarations());
                }
            }
        }

        private void extractMethods(Map<CsmScope, Collection<CsmFunction>> functions, CsmClass clazz) {
            Collection<CsmFunction> list = functions.get(clazz);
            if (list == null) {
                list = new LinkedHashSet<CsmFunction>();
            }
            for (CsmMember member : clazz.getMembers()) {
                if (CsmKindUtilities.isFunction(member)) {
                    CsmFunction fun = handleFunction((CsmFunction) member);
                    if (fun != null && !list.contains(fun)) {
                        list.add(fun);
                    }
                }
            }
            if (!list.isEmpty()) {
                functions.put(clazz, list);
            }
        }

        private CsmFunction handleFunction(CsmFunction fun) {
            fun = CsmBaseUtilities.getFunctionDeclaration(fun);
            CsmFunction out = null;
            boolean skip = false;
            if ("main".contentEquals(fun.getName())) { // NOI18N
                // filter out main functions
                if ("main".contentEquals(fun.getQualifiedName())) { // NOI18N
                    skip = true;
                }
            } else if (CsmKindUtilities.isOperator(fun)) {
                // skip static functions
                skip = true;
            } else if (CsmKindUtilities.isMethod(fun)) {
                CsmMethod method = (CsmMethod) fun;
                if (CsmKindUtilities.isDestructor(method)) {
                    // skip destructors
                    skip = true;
                } else if (method.isAbstract()) {
                    // skip abstract methods
                    skip = true;
                } else if (!method.getVisibility().equals(CsmVisibility.PUBLIC)) {
                    // test only public methods
                    skip = true;
                }
            } else {
                CsmScope scope = fun.getScope();
                // skip static functions
                if (CsmKindUtilities.isFile(scope)) {
                    skip = true;
                }
            }
            if (!skip) {
                out = fun;
            }
            return out;
        }

        private List<Description> convert(CsmFile file, Map<CsmScope, Collection<CsmFunction>> functions) {
            List<Description> descrs = new ArrayList<Description>();
            for (Map.Entry<CsmScope, Collection<CsmFunction>> entry : functions.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    CsmObject container = entry.getKey();
                    List<Description> funs = convertFunctions2Descriptions(entry.getValue());
                    if (CsmKindUtilities.isNamespace(container) && ((CsmNamespace)container).isGlobal()) {
                        // global elements put under file container if only one key
                        if (functions.size() == 1) {
                            container = file;
                        } else {
                            descrs.addAll(funs);
                            continue;
                        }
                    }
                    descrs.add(Description.create(container, funs, false, false));
                }
            }
            return descrs;
        }

        private List<Description> convertFunctions2Descriptions(Collection<CsmFunction> funs) {
            List<Description> out = new ArrayList<Description>();
            for (CsmFunction csmFunction : funs) {
                out.add(Description.create(csmFunction, null, true, false));
            }
            return out;
        }
    }
}
