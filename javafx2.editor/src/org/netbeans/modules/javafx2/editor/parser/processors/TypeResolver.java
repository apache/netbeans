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
package org.netbeans.modules.javafx2.editor.parser.processors;

import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.modules.javafx2.editor.parser.BuildEnvironment;
import org.netbeans.modules.javafx2.editor.parser.ModelBuilderStep;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.processors.Bundle.*;
/**
 *
 * @author sdedic
 */
public class TypeResolver extends FxNodeVisitor.ModelTreeTraversal implements ModelBuilderStep {
    private BuildEnvironment    env;
    private ImportProcessor importProcessor;

    public TypeResolver() {
    }

    public TypeResolver(BuildEnvironment env) {
        this.env = env;
    }

    @Override
    public void visitSource(FxModel model) {
        importProcessor = new ImportProcessor(env.getHierarchy(), env, env.getTreeUtilities());
        importProcessor.load(env.getCompilationInfo(), model);
        
        // try to resolve the fx:controller
        String controller = model.getController();
        if (controller != null) {
            int[] pos = env.getTreeUtilities().findAttributePos(model.getRootComponent(), JavaFXEditorUtils.FXML_FX_NAMESPACE, FxXmlSymbols.FX_CONTROLLER, true);
            ElementHandle<TypeElement> handle = resolveClassname(controller, model.getRootComponent(), pos[0]);
            env.getAccessor().resolve(model, handle, null, null, null);
        }
        
        super.visitSource(model);
    }
    
    @NbBundle.Messages({
        "# {0} - full class name",
        "ERR_unableAnalyzeClass=Unable to analyze class {0} for properties.",
        "# {0} - full class name",
        "ERR_notFxInstance=Instances of {0} cannot be created by FXML loader."
    })
    @Override
    public void visitInstance(FxNewInstance decl) {
        String sourceName = decl.getTypeName();
        // try to resolve the sourceName, may be a full classname
        TypeElement el = env.getCompilationInfo().getElements().getTypeElement(sourceName);
        FxBean bean;
        ElementHandle<TypeElement> handle;
        
        if (el == null) {
            int start;
            
            if (decl.isCustomRoot()) {
                int[] pos = env.getTreeUtilities().findAttributePos(decl, null, FxXmlSymbols.FX_ATTR_TYPE, true);
                if (pos == null) {
                    super.visitInstance(decl);
                    return;
                } else {
                    start = pos[0];
                }
            } else {
                start = env.getTreeUtilities().positions(decl).getStart() + 1; // skip ">"
            }
            handle = resolveClassname(sourceName, decl, start);

            String fqn;
            if (handle != null) {
                fqn = handle.getQualifiedName();
            } else {
                fqn = null;
            }
        } else {
            handle = ElementHandle.create(el);
        }
        // if handle == null, the unresolved err was reported already
        if (handle != null) {
            bean = env.getBeanInfo(handle.getQualifiedName());
            if (bean == null) {
                int start = env.getTreeUtilities().positions(decl).getStart() + 1; // skip ">"
                env.addError(new ErrorMark(
                    start, sourceName.length(),
                    "unable-analyze-class",
                    ERR_unableAnalyseClass(handle.getQualifiedName().toString()),
                    handle
                ));
            } else {
                FxBean bean2 = checkBeanInstance(decl, bean);
                if (bean2 != null) {
                    handle = bean2.getJavaType();
                    bean = bean2;
                }
            }
            env.getAccessor().resolve(decl, handle, null, null, bean);
        }
        super.visitInstance(decl);
    }
    
    @NbBundle.Messages({
        "# {0} - factory name",
        "# {1} - factory class name",
        "ERR_factoryTypeNotFound=Factory produces unknown type: {1}.{0}"
    })
    FxBean checkBeanInstance(FxNewInstance decl, FxBean beanInfo) {
        String fM = decl.getFactoryMethod();
        int start = env.getTreeUtilities().positions(decl).getStart() + 1; // skip ">"
        if (fM == null) {
            if (beanInfo.isFxInstance() || beanInfo.getBuilder() != null || decl.isCustomRoot()) {
                return beanInfo;
            }
            env.addError(new ErrorMark(
                start, decl.getTypeName().length(),
                "class-not-fx-instance",
                ERR_notFxInstance(beanInfo.getJavaType().getQualifiedName().toString()),
                beanInfo.getJavaType()
            ));
            return beanInfo;
        }
        TypeMirrorHandle h = beanInfo.getFactoryType(fM);
        TypeMirror tm = h == null ? null : h.resolve(env.getCompilationInfo());
        if (tm == null) {
            env.addError(new ErrorMark(
                start, decl.getTypeName().length(),
                "returned-factory-type-not-found",
                ERR_factoryTypeNotFound(fM, beanInfo.getJavaType().getQualifiedName().toString()),
                beanInfo
            ));
            return beanInfo;
            // error
        }
        TypeElement productType;
        
        switch (tm.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                productType = env.getCompilationInfo().getTypes().boxedClass((PrimitiveType)tm);
                break;
            case DECLARED:
                productType = (TypeElement)((DeclaredType)(env.getCompilationInfo().getTypes().erasure((DeclaredType)tm))).asElement();
                break;
                
            default:
                return null;
        }
        return env.getBeanInfo(productType.getQualifiedName().toString());
    }
    
    @NbBundle.Messages({
        "# {0} - class name",
        "ERR_symbolNotExist=Class does not exist: {0}",
        "# {0} - class name.",
        "# {1} - alternative1",
        "# {2} - alternative2",
        "ERR_symbolAmbiguous=Name {0} is ambiguous. Can be e.g. {1} or {2}",
        "# {0} - the identifier",
        "ERR_invalidJavaIdentifier={0} is not a valid Java identifier"
    })
    private ElementHandle<TypeElement> resolveClassname(String name, FxNode decl, int start) {
        if (!FxXmlSymbols.isQualifiedIdentifier(name)) {
            env.addError(new ErrorMark(
                start, name.length(),
                "invalid-java-identifier",
                ERR_invalidJavaIdentifier(name),
                name
            ));
            return null;
        }
        TypeElement el = env.getCompilationInfo().getElements().getTypeElement(name);
        if (el != null) {
            return ElementHandle.create(el);
        }
        Set<String> names = importProcessor.resolveName(name);

        if (names != null && names.size() == 1) {
            // succssfully resolved:
            String fqn = names.iterator().next();
            
            el = env.getCompilationInfo().getElements().getTypeElement(fqn);
            if (el != null) {
                ElementHandle<TypeElement> handle = ElementHandle.create(el);
                return handle;
            }
        }
        
        // also cover the fall-through case of not found TypeElement
        if (names == null || names.size() == 1) {
            env.addError(new ErrorMark(
                start, name.length(),
                "class-not-exist",
                ERR_symbolNotExist(name),
                name
            ));
        } else {
            Iterator<String> it = names.iterator();

            env.addError(new ErrorMark(
                start, name.length(),
                "class-ambiguous",
                ERR_symbolAmbiguous(name, it.next(), it.next()),
                name
            ));
        }
        return null;
    }

    @Override
    public FxNodeVisitor createVisitor(BuildEnvironment env) {
        return new TypeResolver(env);
    }
 
    
}
