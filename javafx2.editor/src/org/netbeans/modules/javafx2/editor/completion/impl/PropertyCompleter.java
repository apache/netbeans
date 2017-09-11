/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinitionKind;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 * Creates property names completions. Activates in tag names, or attribute names
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class PropertyCompleter extends InstanceCompleter {
    /**
     * Property names which already exist on the tag
     */
    private Set<String> existingPropNames = new HashSet<String>();
    
    public PropertyCompleter() {
    }

    public PropertyCompleter(FxInstance instance, boolean attribute, CompletionContext context) {
        super(instance, attribute, context);
    }
    
    private List<CompletionItem> resultItems = new ArrayList<CompletionItem>();
    
    private boolean itemsFiltered;
    
    private String namePrefix;
    
    public boolean hasMoreItems() {
        return itemsFiltered;
    }
    
    /**
     * Adds properties from the mentioned beaninfo. Does not add properties, whose names
     * are in the 'alreadyAdded' set.
     * 
     * @param beanInfo
     * @param alreadyAdded
     * @param dontMark 
     */
    private void addPropertiesFrom(FxBean beanInfo, Set<String> alreadyAdded, boolean dontMark) {
        if (beanInfo == null) {
            return;
        }
        Collection<String> propNames = filterNames(new ArrayList<String>(attribute ? 
                beanInfo.getSimplePropertyNames() : beanInfo.getPropertyNames()));
        FxBean parentInfo = beanInfo.getSuperclassInfo();

        for (String s : propNames) {
            if (alreadyAdded.contains(s)) {
                continue;
            }
            FxProperty pi = beanInfo.getProperty(s);

            boolean propInherited = parentInfo != null && parentInfo.getProperty(s) != null;

            if (existingPropNames.contains(s)) {
                // if replacing, leave the property being replaced in the list
                if (!s.startsWith(namePrefix) || !ctx.isReplaceExisting()) {
                    continue;
                }
            }

            if (attribute && !pi.isSimple()) {
                continue;
            }

            PropertyElementItem item = new PropertyElementItem(ctx, s, attribute);

            @SuppressWarnings("rawtypes")
            TypeMirrorHandle typeH = pi.getType();
            if (typeH != null) {
                TypeMirror tm = typeH.resolve(ctx.getCompilationInfo());
                if (tm != null) {
                    String typeString = ctx.getCompilationInfo().getTypeUtilities().
                            getTypeName(tm).toString();
                    item.setPropertyType(typeString);
                    item.setPrimitive(FxClassUtils.isSimpleType(tm, ctx.getCompilationInfo()));
                    item.setInherited(dontMark || propInherited);
                    
                    alreadyAdded.add(s);
                }
            }
            item.setMap(pi.getKind() == FxDefinitionKind.MAP);

            resultItems.add(item);
        }
    }
    
    private static final int IMPORTANT_PROPERTIES_TRESHOLD = 10;
    
    /**
     * Adds up to approx IMPORTANT_PROPERTIES_TRESHOLD from the class and superclasses.
     * Stops when # of properties after adding certain beaninfo exceeds the treshold.
     */
    private void addImportantProperties() {
        FxBean beanInfo = getBeanInfo();
        if (beanInfo == null) {
            return;
        }
        HashSet<String> names = new HashSet<String>();
        boolean next = false;
        do {
            addPropertiesFrom(beanInfo.getDeclareadInfo(), names, next);
            if (beanInfo.getBuilder() != null) {
                addPropertiesFrom(beanInfo.getBuilder().getDeclareadInfo(), names, next);
            }
            beanInfo = beanInfo.getSuperclassInfo();
            next = true;
        } while (beanInfo != null && resultItems.size() < IMPORTANT_PROPERTIES_TRESHOLD);
    }

    private void init() {
        namePrefix = ctx.getPrefix();
        if (namePrefix.startsWith("<")) {
            namePrefix = namePrefix.substring(1);
        }
        for (PropertyValue pv : (Collection<PropertyValue>)instance.getProperties()) {
            existingPropNames.add(pv.getPropertyName());
        }
    }
    
    @Override
    public List<CompletionItem> complete() {
        init();

        if (getBeanInfo() == null) {
            return null;
        }
        Set<String> names = new HashSet<String>();
        if (ctx.getCompletionType() == CompletionProvider.COMPLETION_QUERY_TYPE) {
            addImportantProperties();
            if (resultItems.isEmpty()) {
                addPropertiesFrom(getBeanInfo(), names, false);
                addPropertiesFrom(getBeanInfo().getBuilder(), names, false);
            }
        } else if (ctx.getCompletionType() == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            addPropertiesFrom(getBeanInfo(), names, false);
            addPropertiesFrom(getBeanInfo().getBuilder(), names, false);
        }
        if (ctx.getType() == CompletionContext.Type.PROPERTY) {
            String ns = ctx.findFxmlNsPrefix();
            if (instance.getId() == null) {
                if ("id".startsWith(namePrefix) || // NOI18N
                    (ns != null && (ns + ":id").startsWith(namePrefix))) {  // NOI18N
                    // suggest also fx:id
                    PropertyElementItem pi = new PropertyElementItem(ctx, "fx:id", // NOI18N
                            true);
                    pi.setPrimitive(true);
                    pi.setInherited(false);
                    pi.setSystem(true);
                    pi.setNamespaceCreator(CompletionUtils.makeFxNamespaceCreator(ctx));
                    pi.setPropertyType("String"); // NOI18N
                    resultItems.add(pi);
                }
            }
            if (ctx.isRootElement() && ctx.getModel().getController() == null) {
                if ("controller".startsWith(namePrefix) || // NOI18N
                    (ns != null && (ns + ":controller").startsWith(namePrefix))) {  // NOI18N
                    // suggest also fx:id
                    PropertyElementItem pi = new PropertyElementItem(ctx, "fx:controller", // NOI18N
                            true);
                    pi.setPrimitive(true);
                    pi.setInherited(false);
                    pi.setSystem(true);
                    pi.setNamespaceCreator(CompletionUtils.makeFxNamespaceCreator(ctx));
                    pi.setPropertyType("Class"); // NOI18N
                    resultItems.add(pi);
                }
            }
            if (instance instanceof FxNewInstance) {
                FxNewInstance newInst = (FxNewInstance)instance;
                if (newInst.getFactoryMethod() == null &&
                    newInst.getInitValue() == null) {
                    // check that the instance's definition has some constants to suggest
                    if (!newInst.getDefinition().getConstants().isEmpty()) {
                        // suggest fx:constant
                        PropertyElementItem pi = new PropertyElementItem(ctx, "fx:constant", // NOI18N
                                true);
                        pi.setPrimitive(true);
                        pi.setInherited(false);
                        pi.setSystem(true);
                        pi.setNamespaceCreator(CompletionUtils.makeFxNamespaceCreator(ctx));
                        pi.setPropertyType(newInst.getDefinition().getClassName()); // NOI18N
                        resultItems.add(pi);
                    }
                }
            }
        }
        return resultItems;
    }
    
    @Override
    protected InstanceCompleter createCompleter(FxInstance instance, boolean attribute, CompletionContext ctx) {
        return new PropertyCompleter(instance, attribute, ctx);
    }
    
}
