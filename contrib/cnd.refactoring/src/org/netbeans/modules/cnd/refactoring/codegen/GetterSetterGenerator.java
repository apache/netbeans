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
package org.netbeans.modules.cnd.refactoring.codegen;

import org.netbeans.modules.cnd.modelutil.ui.ElementNode.Description;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import java.awt.Dialog;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.codegen.ui.GetterSetterPanel;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;

/**
 *
 */
public class GetterSetterGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        private static final String ERROR = "<error>"; //NOI18N

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            List<CsmObject> pathList = path.getPath();
            CsmObject last = pathList.get(pathList.size()-1);
            if (!(CsmKindUtilities.isClass(last) || CsmKindUtilities.isField(last))) {
                return ret;
            }
            CsmObject objectUnderOffset = path.getObjectUnderOffset();
            Map<String, List<CsmMethod>> methods = new HashMap<>();
            Map<CsmClass, List<ElementNode.Description>> gDescriptions = new LinkedHashMap<>();
            Map<CsmClass, List<ElementNode.Description>> sDescriptions = new LinkedHashMap<>();
            Map<CsmClass, List<ElementNode.Description>> gsDescriptions = new LinkedHashMap<>();
            Boolean isUpperCase = null;
            for (CsmMember member : GeneratorUtils.getAllMembers(typeElement)) {
                if (CsmKindUtilities.isMethod(member)) {
                    CsmMethod method = (CsmMethod)member;
                    String name = method.getName().toString();
                    List<CsmMethod> l = methods.get(name);
                    if (l == null) {
                        l = new ArrayList<>();
                        methods.put(method.getName().toString(), l);
                    }
                    l.add(method);
                    if (isUpperCase == null) {
                        isUpperCase = GeneratorUtils.checkStartWithUpperCase(method);
                    }
                }
            }
            isUpperCase = isUpperCase != null ? isUpperCase : Boolean.TRUE;
            
            ElementNode.Description theFirstDescription = null;
            for (CsmMember member : GeneratorUtils.getAllMembers(typeElement)) {
                if (CsmKindUtilities.isField(member)) {
                    CsmField variableElement = (CsmField)member;
                    ElementNode.Description description = ElementNode.Description.create(variableElement, null, true, variableElement.equals(objectUnderOffset));
                    if (theFirstDescription == null) {
                        theFirstDescription = description;
                    }
                    boolean hasGetter = GeneratorUtils.hasGetter(variableElement, methods, isUpperCase);
                    boolean hasSetter = GeneratorUtils.isConstant(variableElement) || GeneratorUtils.hasSetter(variableElement, methods, isUpperCase);
                    if (!hasGetter) {
                        List<ElementNode.Description> descriptions = gDescriptions.get(variableElement.getContainingClass());
                        if (descriptions == null) {
                            descriptions = new ArrayList<>();
                            gDescriptions.put(variableElement.getContainingClass(), descriptions);
                        }
                        descriptions.add(description);
                    }
                    if (!hasSetter) {
                        List<ElementNode.Description> descriptions = sDescriptions.get(variableElement.getContainingClass());
                        if (descriptions == null) {
                            descriptions = new ArrayList<>();
                            sDescriptions.put(variableElement.getContainingClass(), descriptions);
                        }
                        descriptions.add(description);
                    }
                    if (!hasGetter && !hasSetter) {
                        List<ElementNode.Description> descriptions = gsDescriptions.get(variableElement.getContainingClass());
                        if (descriptions == null) {
                            descriptions = new ArrayList<>();
                            gsDescriptions.put(variableElement.getContainingClass(), descriptions);
                        }
                        descriptions.add(description);
                    }
                }
            }
            if (!gDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = prepareDescriptions(gDescriptions);
                ret.add(new GetterSetterGenerator(component, path, ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.Kind.GETTERS_ONLY, isUpperCase));
            }
            if (!sDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = prepareDescriptions(sDescriptions);
                ret.add(new GetterSetterGenerator(component, path, ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.Kind.SETTERS_ONLY, isUpperCase));
            }
            if (!gsDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = prepareDescriptions(gsDescriptions);
                ret.add(new GetterSetterGenerator(component, path, ElementNode.Description.create(typeElement, descriptions, false, false), GeneratorUtils.Kind.GETTERS_SETTERS, isUpperCase));
            }
            return ret;
        }

        private List<Description> prepareDescriptions(Map<CsmClass, List<Description>> descripti) {
            boolean selectIfOnlyOne = descripti.size() == 1;
            List<ElementNode.Description> out = new ArrayList<>();
            for (Map.Entry<CsmClass, List<ElementNode.Description>> entry : descripti.entrySet()) {
                List<Description> values = entry.getValue();
                if (selectIfOnlyOne && values.size() == 1) {
                    ElementNode.Description orig = values.get(0);
                    values = new ArrayList<>(1);
                    values.add(ElementNode.Description.create(orig.getElementHandle(), null, true, true));
                }
                out.add(ElementNode.Description.create(entry.getKey(), values, false, false));
            }
            Collections.reverse(out);
            return out;
        }

    }
    private final JTextComponent component;
    private final ElementNode.Description description;
    private final GeneratorUtils.Kind type;
    private final CsmContext contextPath;
    private final boolean isUpperCase;

    /** Creates a new instance of GetterSetterGenerator */
    private GetterSetterGenerator(JTextComponent component, CsmContext path, ElementNode.Description description, GeneratorUtils.Kind type, boolean isUpperCase) {
        this.component = component;
        this.contextPath = path;
        this.description = description;
        this.type = type;
        this.isUpperCase = isUpperCase;
    }

    @Override
    public String getDisplayName() {
        if (type == GeneratorUtils.Kind.GETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter"); //NOI18N
        }
        if (type == GeneratorUtils.Kind.SETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter"); //NOI18N
        }
        return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "GETTER_SETTER"); // NOI18N
        final GetterSetterPanel panel = new GetterSetterPanel(description, type);
        String title = GeneratorUtils.getGetterSetterDisplayName(type);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, title);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            GeneratorUtils.generateGettersAndSetters(contextPath, panel.getVariables(), panel.isMethodInline(), type, isUpperCase);
        }
    }
}
