/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
