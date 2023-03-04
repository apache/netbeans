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
package org.netbeans.modules.javafx2.editor.parser.processors;

import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinitionKind;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstanceCopy;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.MapProperty;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;

/**
 * Matches properties found in the source with the BeanInfos.
 * 
 * @author sdedic
 */
public class PropertyResolver extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    private BuildEnvironment    env;
    private FxInstance  currentInstance;
    private FxBean  beanInfo;
    private ImportProcessor importer;
    
    public PropertyResolver() {
    }

    public PropertyResolver(BuildEnvironment env) {
        this.env = env;
    }
    
    private ImportProcessor getImporter() {
        if (importer != null) {
            return importer;
        }
        ImportProcessor proc = new ImportProcessor(env.getHierarchy(), null, env.getTreeUtilities());
        proc.load(env.getCompilationInfo(), env.getModel());
        
        importer = proc;
        return importer;
    }

    @Override
    public void visitBaseInstance(FxInstance decl) {
        FxInstance save = this.currentInstance;
        FxBean saveInfo = this.beanInfo;
        
        currentInstance = decl;
        beanInfo = env.getBeanInfo(decl.getResolvedName());
        super.visitBaseInstance(decl);
        
        this.beanInfo = saveInfo;
        this.currentInstance = save;
    }
    
    @Override
    public void visitStaticProperty(StaticProperty p) {
        if (doVisitStaticProperty(p)) {
            super.visitStaticProperty(p);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - classname as appears in the source",
        "ERR_undefinedSourceAttachClass=The attached proprty source class ''{0}'' does not exist",
        "# {0} - classname as appears in the source",
        "# {1} - 1st alternative",
        "# {2} - 2nd alternative",
        "ERR_sourceAttachClassAmbiguous=The attached property source class name ''{0}'' is ambigous. Could be {1} or {2}",
        "# {0} - resolved classname - FQN",
        "ERR_unableAnalyseClass=Unable to analyse class ''{0}''",
        "# {1} - attached property name",
        "# {0} - source class name",
        "ERR_attachedPropertyNotExist=The class ''{0}'' does not provide attached property ''{1}''"
    })
    private boolean doVisitStaticProperty(StaticProperty p) {
        // check whether 
        if (beanInfo == null) {
            return true;
        }
        String sourceClassName = p.getSourceClassName();
        // try to resolve the classname, using Importer
        Set<String> names = getImporter().resolveName(sourceClassName);
        
        int offs = env.getTreeUtilities().positions(p).getStart();
        if (names == null) {
            // error - unresolvable thing
            env.addError(new ErrorMark(
                    offs,
                    p.getPropertyName().length(),
                    "undefined-attached-source-class",
                    ERR_undefinedSourceAttachClass(sourceClassName),
                    sourceClassName
            ));
            return true;
        } else if (names.size() > 1) {
            // error - ambiguous name
            Iterator<String> it = names.iterator();
            env.addError(new ErrorMark(
                    offs,
                    p.getPropertyName().length(),
                    "attached-source-class-ambiguous",
                    ERR_sourceAttachClassAmbiguous(sourceClassName, it.next(), it.next()),
                    sourceClassName
            ));
            return true;
        } 
        String resolvedName = names.iterator().next();
        // try to convert to ElementHandle:
        TypeElement resolvedEl = env.getCompilationInfo().getElements().getTypeElement(resolvedName);
        
        ElementHandle<TypeElement> sourceTypeHandle = resolvedEl != null ? ElementHandle.create(resolvedEl) : null;
        TypeMirrorHandle typeHandle = null;
        ElementHandle accessorHandle = null;
        FxProperty pi = null;
        FxBean sourceInfo = env.getBeanInfo(resolvedName);
        
        if (sourceInfo == null) {
            env.addError(new ErrorMark(
                    offs,
                    p.getPropertyName().length(),
                    "unable-analyse-class",
                    ERR_unableAnalyseClass(resolvedName),
                    resolvedName
            ));
            env.getAccessor().makeBroken(p);
        } else {
            String propName = p.getPropertyName();
            pi = sourceInfo.getAttachedProperty(propName);
            if (pi == null) {
                // report error, the attached property does not exist
                env.addError(new ErrorMark(
                        offs,
                        p.getPropertyName().length(),
                        "attached-property-not-exist",
                        ERR_attachedPropertyNotExist(resolvedName, propName),
                        resolvedName, propName
                ));
                env.getAccessor().makeBroken(p);
            } else {
                accessorHandle = pi.getAccessor();
                typeHandle = pi.getType();
            }
        }        
        env.getAccessor().resolve(p, accessorHandle, typeHandle, sourceTypeHandle, pi);
        return true;
    }

    @Override
    public void visitPropertySetter(PropertySetter p) {
        if (beanInfo != null) {
            if (p.isImplicit()) {
                processDefaultProperty(p);
            } else {
                processInstanceProperty(p);
            }
        }
        super.visitPropertySetter(p);
    }
    
    private int[] findContentPositions(PropertySetter p) {
        int start = env.getTreeUtilities().positions(p).getStart();
        int len = 1;
        TokenSequence<XMLTokenId>  seq = (TokenSequence<XMLTokenId>)env.getHierarchy().tokenSequence();
        seq.move(start);
        if (seq.moveNext()) {
            Token<XMLTokenId>   t = seq.token();
            if (t.id() == XMLTokenId.TEXT) {
                String tokenText = t.text().toString();
                String trimmed = tokenText.trim();
                int indexOfTrimmed = tokenText.indexOf(trimmed);
                int indexOfNl = trimmed.indexOf('\n');

                start = seq.offset() + indexOfTrimmed;
                if (indexOfNl > -1) {
                    len = indexOfNl;
                } else {
                    len = trimmed.length();
                }
            } else {
                start = seq.offset();
                len = t.length();
            }
        }
        return new int[] { start, len };
    }
    
    
    
    @NbBundle.Messages({
        "# {0} - class name",
        "ERR_noDefaultProperty=Class {0} has no default property. Place {0} content in a property element.",
        "# {0} - property name",
        "ERR_defaultPropertyClash=The content belongs to property ''{0}'', which has its own element or attribute."
    })
    @SuppressWarnings("unchecked")
    private void processDefaultProperty(PropertySetter p) {
        FxProperty pi = beanInfo.getDefaultProperty();
        if (pi == null) {
            // check if the bean is not a Map or Collection:
            if (beanInfo.isCollection() || beanInfo.isMap()) {
                return;
            }
            int[] posInfo = findContentPositions(p);
            env.addError(new ErrorMark(
                    posInfo[0],
                    posInfo[1],
                    "no-default-property",
                    ERR_noDefaultProperty(beanInfo.getClassName()),
                    beanInfo.getClassName()
            ));
        } else {
            // check if there's an explicit property of that name;
            if (p.getSourceName() == null && currentInstance.getProperty(pi.getName()) != null) {
                int[] posInfo = findContentPositions(p);
                env.addError(new ErrorMark(
                        posInfo[0],
                        posInfo[1],
                        "default-property-clash",
                        ERR_defaultPropertyClash(pi.getName()),
                        p
                ));
                env.getAccessor().makeBroken(p);
            } else {
                env.getAccessor().resolve(p, pi.getAccessor(), pi.getType(), null, pi);
                env.getAccessor().rename(currentInstance, p, pi.getName());
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - full class name",
        "# {1} - property name",
        "ERR_propertyNotExist=Class ''{0}'' does not support property ''{1}''"
    })
    private boolean processInstanceProperty(PropertyValue p) {
        String propName = p.getPropertyName();
        // handle default property:
        
        FxProperty pi = beanInfo.getProperty(propName);
        if (pi == null && beanInfo.getBuilder() != null) {
            pi = beanInfo.getBuilder().getProperty(propName);
            if (pi == null && beanInfo.getBuilder().isMap()) {
                // there's a chance that the Builder implements Map, and any property
                // name is valid then:
                return true;
            }
        }
        int offs = env.getTreeUtilities().positions(p).getStart();

        if (pi == null) {
            env.addError(new ErrorMark(
                    offs,
                    propName.length(),
                    "property-not-exist",
                    ERR_propertyNotExist(beanInfo.getClassName(), propName),
                    beanInfo.getClassName(), propName
            ));
            env.getAccessor().makeBroken(p);
            return false;
        } else {
            env.getAccessor().resolve(p, pi.getAccessor(), pi.getType(), null, pi);
            return true;
        }
    }

    @NbBundle.Messages({
        "# {0} - property name",
        "ERR_propertyHasAttributes={0} is not a readonly Map, it cannot have any attributes"
    })
    @Override
    public void visitMapProperty(MapProperty p) {
        if (beanInfo != null) {
            if (processInstanceProperty(p)) {
                if (p.getPropertyInfo().getKind() != FxDefinitionKind.MAP) {
                    String propName = p.getPropertyName();
                    int offs = env.getTreeUtilities().positions(p).getStart() + 1;
                    env.addError(new ErrorMark(
                            offs,
                            propName.length(),
                            "property-with-attributes",
                            ERR_propertyHasAttributes(propName),
                            propName
                    ));
                }
            }
        }
        super.visitMapProperty(p);
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new PropertyResolver(env);
    }
}
