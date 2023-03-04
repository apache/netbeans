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

package org.netbeans.modules.web.jsf.palette.items;

import java.io.IOException;
import java.text.MessageFormat;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;

public final class JsfTable extends EntityClass implements ActiveEditorDrop, PaletteItem {
    private static String [] BEGIN = {
        "<__HTML__:form>\n <__HTML__:dataTable value=\"#'{'{0}'}'\" var=\"{1}\">\n",
        "<__HTML__:form>\n <h1><__HTML__:outputText value=\"List\"/></h1>\n <__HTML__:dataTable value=\"#'{'{0}'}'\" var=\"{1}\">\n",
    };
    private static String [] END = {
        "</__HTML__:dataTable>\n </__HTML__:form>\n",
        "</__HTML__:dataTable>\n </__HTML__:form>\n",
    };
    private static String [] ITEM = {
        "",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'{3}.{2}'}'\"/>\n</h:column>\n",
        "<h:column>\n <f:facet name=\"header\">\n <h:outputText value=\"{0}\"/>\n </f:facet>\n <h:outputText value=\"#'{'{5}.{2}'}'\">\n <f:convertDateTime pattern=\"{4}\" />\n</h:outputText>\n</h:column>\n"
    };
    
    public JsfTable() {
    }
    
    @Override
    protected String getName() {
        return "Table"; // NOI18N
    }

    @Override
    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(JsfForm.class, "NAME_jsp-JsfTable");
    }
    
    @Override
    protected String createBody(JTextComponent target, boolean surroundWithFView) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();
        if (surroundWithFView) {
            stringBuffer.append(PaletteUtils.createViewTag(jsfLibrariesSupport, target, false)).append("\n"); // NOI18N
        }
        stringBuffer.append(MessageFormat.format(
                BEGIN[formType].replace("__HTML__", jsfLibrariesSupport.getLibraryPrefix(DefaultLibraryInfo.HTML)), //NOI18N
                new Object [] {variable, "item"})); //NOI18N
        
        stringBuffer.append(END[formType].replace("__HTML__", jsfLibrariesSupport.getLibraryPrefix(DefaultLibraryInfo.HTML))); //NOI18N
        if (surroundWithFView) {
            stringBuffer.append(PaletteUtils.createViewTag(jsfLibrariesSupport, target, true)).append("\n"); // NOI18N
        }
        return stringBuffer.toString();
    }
    
    /** @param commands a message that will be added to the end of each line in table,
     *  it will be formated using {0} = iterator variable
     */
    public static void createTable(CompilationController controller, TypeElement bean, String variable, StringBuffer stringBuffer,
            String commands, JpaControllerUtil.EmbeddedPkSupport embeddedPkSupport, String tableVarName) {
        if (tableVarName == null) {
            tableVarName = "item"; //NOI18N
        }
        int formType = 1;
        TypeMirror dateTypeMirror = controller.getElements().getTypeElement("java.util.Date").asType();
        if (bean != null) {
            ExecutableElement[] methods = JpaControllerUtil.getEntityMethods(bean);
            boolean fieldAccess = JpaControllerUtil.isFieldAccess(bean);
            for (ExecutableElement method : methods) {
                String methodName = method.getSimpleName().toString();
                if (methodName.startsWith("get")) {
                    int isRelationship = JpaControllerUtil.isRelationship(method, fieldAccess);
                    String name = methodName.substring(3);
                    String propName = JpaControllerUtil.getPropNameFromMethod(methodName);
                    if (EntityClass.isId(method, fieldAccess)) {
                        TypeMirror rType = method.getReturnType();
                        if (TypeKind.DECLARED == rType.getKind()) {
                            DeclaredType rTypeDeclared = (DeclaredType)rType;
                            TypeElement rTypeElement = (TypeElement) rTypeDeclared.asElement();
                            if (JpaControllerUtil.isEmbeddableClass(rTypeElement)) {
                                if (embeddedPkSupport == null) {
                                    embeddedPkSupport = new JpaControllerUtil.EmbeddedPkSupport();
                                }
                                for (ExecutableElement pkMethod : embeddedPkSupport.getPkAccessorMethods(bean)) {
                                    if (!embeddedPkSupport.isRedundantWithRelationshipField(bean, pkMethod)) {
                                        String pkMethodName = pkMethod.getSimpleName().toString();
                                        String pkPropTitle = pkMethodName.substring(3);
                                        String pkPropName = propName + "." + JpaControllerUtil.getPropNameFromMethod(pkMethodName);
                                        stringBuffer.append(MessageFormat.format(ITEM [1], new Object [] {pkPropTitle, null, pkPropName, tableVarName}));
                                    }
                                }
                            }
                            else {
                                stringBuffer.append(MessageFormat.format(ITEM [1], new Object [] {name, variable, propName, tableVarName}));
                            }
                        }
                    } else if (controller.getTypes().isSameType(dateTypeMirror, method.getReturnType())) {
                        //param 3 - temporal, param 4 - date/time format
                        String temporal = EntityClass.getTemporal(method, fieldAccess);
                        if (temporal == null) {
                            stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                        } else {
                            stringBuffer.append(MessageFormat.format(ITEM [2], new Object [] {name, variable, propName, temporal, EntityClass.getDateTimeFormat(temporal), tableVarName}));
                        }
                    } else if (isRelationship == JpaControllerUtil.REL_NONE || isRelationship == JpaControllerUtil.REL_TO_ONE) {
                        stringBuffer.append(MessageFormat.format(ITEM [formType], new Object [] {name, variable, propName, tableVarName}));
                    }
                }
            }
        }
        stringBuffer.append(MessageFormat.format(commands, new Object [] {tableVarName}));
    }
}
