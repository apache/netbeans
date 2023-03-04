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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinitionKind;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=Completer.Factory.class)
public class StaticPropertyCompleter extends InstanceCompleter {
    private Set<String>     directClasses = new HashSet<String>();
    
    private String  propertyPrefix;
    
    private String  classPrefix;
    
    private String simpleClassPrefix;
    
    private List<CompletionItem>    result = new ArrayList<CompletionItem>();

    public boolean hasMoreItems() {
        return false;
    }

    public StaticPropertyCompleter() {
    }
    
    StaticPropertyCompleter(FxInstance instance, boolean attribute, CompletionContext ctx) {
        super(instance, attribute, ctx);
    }
    
    private void evalPrefixes() {
        String s = ctx.getPrefix();
        if (s == null) {
            return;
        }
        if (!attribute && s.startsWith("<")) {
            s = s.substring(1);
        }
        if (s.isEmpty()) {
            return;
        }
        int lastDot = s.lastIndexOf('.');
        if (lastDot == -1) {
            if (Character.isUpperCase(s.charAt(0))) {
                classPrefix = s;
            } else {
                propertyPrefix = s;
            }
        } else {
            propertyPrefix = s.substring(lastDot + 1);
            classPrefix = s.substring(0, lastDot);
            
            lastDot = classPrefix.lastIndexOf('.');
            if (lastDot != -1) {
                simpleClassPrefix = classPrefix.substring(lastDot + 1);
            }
        }
    }
    
    /**
     * Collects directly referenced classes, from the parent chain or imports.
     * 
     * @return 
     */
    private void collectDirectClasses() {
        for (FxNode p : ctx.getParents()) {
            if (p.getKind() != FxNode.Kind.Instance) {
                continue;
            }
            FxInstance parentInstance = (FxInstance)p;
            String parentClass = parentInstance.getResolvedName();
            if (parentClass != null) {
                if (classPrefix != null && 
                    !CompletionUtils.startsWith(CompletionUtils.getSimpleName(parentClass), classPrefix)) {
                    continue;
                }
                directClasses.add(parentClass);
            }
        }
        if (ctx.getCompletionType() == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
            for (ImportDecl decl : ctx.getModel().getImports()) {
                String n = decl.getImportedName();
                
                if (!decl.isWildcard()) {
                    directClasses.add(n);
                }
            }
        }
    }
    
    private Collection<String> findProperties(FxBean bi) {
        Collection<String> c = null;
        
        for (String pn : bi.getAttachedPropertyNames()) {
            if (acceptsProperty(bi, bi.getAttachedProperty(pn))) {
                if (c == null) {
                    c = Collections.singletonList(pn);
                } else {
                    if (c.size() == 1) {
                        c = new ArrayList<String>(c);
                    }
                    c.add(pn);
                }
            }
        }
        return c;
    }
    
    /**
     * Checks whether the 'info' attached-to type corresponds to the enclosing
     * instance type. Also checks that property name matches the prefix.
     * @param info
     * @return 
     */
    private boolean acceptsProperty(FxBean owner, FxProperty info) {
        if (info.getKind() != FxDefinitionKind.ATTACHED) {
            return false;
        }
        // if property prefix is present, the property must match the name.
        if (propertyPrefix != null && !CompletionUtils.startsWith(info.getName(), propertyPrefix)) {
            return false;
        }
        
        if (classPrefix != null) {
            int dot = owner.getClassName().lastIndexOf('.');
            if (!CompletionUtils.startsWith(owner.getClassName().substring(dot + 1), classPrefix)) {
                return false;
            }
        }
        
        // the property may have been already used - check
        Collection<StaticProperty> staticProps = instance.getStaticProperties();
        for (StaticProperty sp : staticProps) {
            if (sp.getSourceName() == null || !sp.getSourceName().equals(info.getName())) {
                continue;
            }
            if (sp.getSourceClassName() == null) {
                continue;
            }
            
            ElementHandle<TypeElement> sH = sp.getSourceClassHandle();
            if (sH == null) {
                if (sp.getSourceClassName().equals(CompletionUtils.getSimpleName(owner.getClassName()))) {
                    return false;
                }
            } else {
                if (sH.getQualifiedName().equals(owner.getClassName())) {
                    return false;
                }
            }
        }
        
        TypeMirrorHandle h = info.getObjectType();
        if (h == null) {
            return false;
        }
        TypeMirror type = h.resolve(ctx.getCompilationInfo());
        if (type == null) {
            return false;
        }
        
        if (!ctx.getCompilationInfo().getTypes().isAssignable(
                instanceType.asType(), type)) {
            return false;
        }
        
        
        return true;
    }

    @Override
    protected InstanceCompleter createCompleter(FxInstance instance, boolean attribute, CompletionContext ctx) {
        if (instance.getJavaType() == null) {
            return null;
        }
        return new StaticPropertyCompleter(instance, attribute, ctx);
    }

    @Override
    public List<CompletionItem> complete() {
        evalPrefixes();
        makeDirectList();
        return result;
    }
    
    private static final int MAX_SAMPLES_LEN = 40;
    
    private void makeDirectList() {
        collectDirectClasses();
        if (classPrefix != null && propertyPrefix == null && directClasses.isEmpty()) {
            propertyPrefix = classPrefix;
            classPrefix = null;
            collectDirectClasses();
        }
        for (String classFullName : directClasses) {
            FxBean beanInfo = ctx.getBeanInfo(classFullName);
            if (beanInfo == null) {
                continue;
            }
            String cn = ctx.getSimpleClassName(classFullName);
            if (cn == null) {
                // FIXME - this should add an import, according to the settings / policy
                cn = classFullName;
            }
            Collection<String> props = findProperties(beanInfo);
            if (props != null) {
                if (directClasses.size() == 1 || props.size() == 1 || ctx.getCompletionType() == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) {
                    // collect individual properties
                    for (String pn : props) {
                        FxProperty pi = beanInfo.getAttachedProperty(pn);
                        TypeMirror pType = pi.getType().resolve(ctx.getCompilationInfo());
                        boolean primitive = FxClassUtils.isPrimitive(pType);
                        
                        String type = ctx.getCompilationInfo().getTypeUtilities().getTypeName(
                                pType).toString();
                        
                        String replacementText = cn + "." + pi.getName(); // NOI18N
                        
                        String propName;
                        
                        if (!cn.equals(classPrefix)) {
                            propName = cn + "." + pi.getName();
                        } else {
                            propName = pi.getName();
                        }

                        AttachedPropertyItem api = new AttachedPropertyItem(
                                ctx, replacementText, 
                                propName, type, primitive);
                        api.setAttribute(attribute);
                        result.add(api);
                    }
                } else {
                    // collect just property names, suggest the class prefix
                    StringBuilder sb = new StringBuilder();
                    for (String pn : props) {
                        if (sb.length() > 0) {
                            sb.append(", "); // NOI18N
                        }
                        int nextLen = sb.length() + pn.length();
                        if (nextLen > MAX_SAMPLES_LEN) {
                            if (MAX_SAMPLES_LEN - sb.length() > 4) {
                                sb.append(pn.substring(0, nextLen - sb.length()));
                            }
                            sb.append("...");
                            break;
                        }
                        sb.append(pn);
                    }
                    
                    int dot = classFullName.lastIndexOf('.');
                    String sn = dot == -1 ? classFullName : classFullName.substring(dot + 1);
                    String replacementText = cn + "."; // NOI18N
                    
                    AttachedPropertyItem api = new AttachedPropertyItem(ctx, 
                            replacementText, sn, sb.toString());
                    api.setAttribute(attribute);
                    result.add(api);
                }
            }
        }
    }
}
