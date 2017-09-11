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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.j2ee.jpa.model;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@SUN.COM
 */
public class ModelUtils {

    private static final Logger LOG = Logger.getLogger(ModelUtils.class.getName());
    private static final String CACHED_CONTEXT = "cached-jpaProblemContext";
    private static final String CACHED_MTP = "cached-jpaManagedTypeProvider";

    public static Entity getEntity(EntityMappingsMetadata metadata, TypeElement clazz) {
        assert metadata != null : "Metadata is null"; //NOI18N
        assert clazz != null : "TypeElement is null"; //NOI18N
        Name clName = clazz.getQualifiedName();
        for (Entity entity : metadata.getRoot().getEntity()) {
            if (clName.contentEquals(entity.getClass2())) {
                return entity;
            }
        }
        return null;
    }

    public static Entity getEntity(EntityMappingsMetadata metadata, String qualifiedClassName) {
        for (Entity entity : metadata.getRoot().getEntity()) {
            if (qualifiedClassName.equals(entity.getClass2())) {
                return entity;
            }
        }
        return null;
    }

    public static Embeddable getEmbeddable(EntityMappingsMetadata metadata, TypeElement clazz) {
        for (Embeddable embeddable : metadata.getRoot().getEmbeddable()) {
            if (clazz.getQualifiedName().contentEquals(embeddable.getClass2())) {
                return embeddable;
            }
        }
        return null;
    }

    public static MappedSuperclass getMappedSuperclass(EntityMappingsMetadata metadata, TypeElement clazz) {
        for (MappedSuperclass mappedSuperclass : metadata.getRoot().getMappedSuperclass()) {
            if (clazz.getQualifiedName().contentEquals(mappedSuperclass.getClass2())) {
                return mappedSuperclass;
            }
        }
        return null;
    }

    public static TypeElement getTypeElementFromModel(CompilationInfo info, Object modelElement) {
        String className = null;

        if (modelElement instanceof Entity) {
            className = ((Entity) modelElement).getClass2();
        }

        if (className != null) {
            return info.getElements().getTypeElement(className);
        }

        return null;
    }

    public static void resolveJavaElementFromModel(JPAProblemContext problemCtx, AttributeWrapper attr) {
        String attrName = attr.getName();

        attr.setInstanceVariable(getField(problemCtx.getJavaClass(), attrName));
        attr.setAccesor(getAccesor(problemCtx.getJavaClass(), attrName));

        if (attr.getInstanceVariable() != null) {
            attr.setMutator(getMutator(
                    problemCtx.getCompilationInfo(),
                    problemCtx.getJavaClass(),
                    attr.getInstanceVariable()));
        }

        if (problemCtx.getAccessType() == AccessType.FIELD) {
            attr.setJavaElement(attr.getInstanceVariable());
        } else if (problemCtx.getAccessType() == AccessType.PROPERTY) {
            attr.setJavaElement(attr.getAccesor());
        }
    }

    // TODO: reimplement this method to take a type argument and assure 100% accuracy 
    public static ExecutableElement getAccesor(TypeElement clazz, String fieldName) {
        for (ExecutableElement method : getMethod(clazz, getAccesorName(fieldName))) {
            if (method.getParameters().size() == 0) {
                return method;
            }
        }

        for (ExecutableElement method : getMethod(clazz, getBooleanAccesorName(fieldName))) {
            if (method.getParameters().size() == 0) {
                return method;
            }
        }

        return null;
    }

    public static ExecutableElement getMutator(CompilationInfo info, TypeElement clazz, VariableElement field) {
        ExecutableElement matchingMethods[] = ModelUtils.getMethod(
                clazz, ModelUtils.getMutatorName(field.getSimpleName().toString()));

        for (ExecutableElement potentialMutator : matchingMethods) {
            if (potentialMutator.getParameters().size() == 1) {
                TypeMirror argType = potentialMutator.getParameters().get(0).asType();

                if (info.getTypes().isSameType(argType,
                        field.asType())) {
                    return potentialMutator;
                }
            }
        }

        return null;
    }

    public static ExecutableElement[] getMethod(TypeElement clazz, String methodName) {
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();

        for (ExecutableElement method : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (method.getSimpleName().contentEquals(methodName)) {
                methods.add(method);
            }
        }

        return methods.toArray(new ExecutableElement[methods.size()]);
    }

    public static String getAccesorName(String fieldName) {
        return "get" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase()
                + fieldName.substring(1);
    }

    public static String getBooleanAccesorName(String fieldName) {
        return "is" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase()
                + fieldName.substring(1);
    }

    public static String getMutatorName(String fieldName) {
        return "set" //NOI18N
                + Character.toString(fieldName.charAt(0)).toUpperCase()
                + fieldName.substring(1);
    }

    public static String getFieldNameFromAccessor(String accessorName) {
        if (!accessorName.startsWith("get")) { //NOI18N
            throw new IllegalArgumentException("accessor name must start with 'get'");
        }

        return String.valueOf(accessorName.charAt(3)).toLowerCase() + accessorName.substring(4);
    }

    public static VariableElement getField(TypeElement clazz, String fieldName) {
        for (VariableElement field : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            if (field.getSimpleName().contentEquals(fieldName)) {
                return field;
            }
        }

        return null;
    }

    public static MetadataModel<EntityMappingsMetadata> getModel(FileObject sourceFile) {
        EntityClassScope scope = EntityClassScope.getEntityClassScope(sourceFile);

        if (scope != null) {
            return scope.getEntityMappingsModel(false); // false since I guess you only want the entity classes defined in the project
        }
        return null;
    }

    public static Collection<String> extractAnnotationNames(Element elem) {
        Collection<String> annotationsOnElement = new LinkedList<String>();

        for (AnnotationMirror ann : elem.getAnnotationMirrors()) {
            TypeMirror annType = ann.getAnnotationType();
            Element typeElem = ((DeclaredType) annType).asElement();
            String typeName = ((TypeElement) typeElem).getQualifiedName().toString();
            annotationsOnElement.add(typeName);
        }

        return annotationsOnElement;
    }

    public static String shortAnnotationName(String annClass) {
        return "@" + annClass.substring(annClass.lastIndexOf(".") + 1); //NOI18N
    }

    /**
     * Gets problem context used by standard jpa hints. Uses cached value if
     * found, otherwise creates a new one which stores into the CompilationInfo.
     *
     * @param context Hints API context
     * @return jpa hint's context
     */
    public static JPAProblemContext getOrCreateCachedContext(HintContext context) {
        //return createJPAProblemContext(context);
        Object cached = context.getInfo().getCachedValue(CACHED_CONTEXT);
        if (cached == null) {
            LOG.log(Level.FINEST, "HintContext doesn't contain cached JPAProblemContext which is going to be created.");
            JPAProblemContext newContext = createJPAProblemContext(context);
            context.getInfo().putCachedValue(CACHED_CONTEXT, newContext, CompilationInfo.CacheClearPolicy.ON_SIGNATURE_CHANGE);
            return newContext;
        } else {
            LOG.log(Level.FINEST, "JPAProblemContext cached value used.");
            return (JPAProblemContext) cached;
        }
    }

    /**
     * @param context Hints API context
     * @return managed type provider
     */
    public static ManagedTypeProvider getOrCreateCachedMTP(HintContext hc, Project project, EntityMappingsMetadata metaData, Elements elements) {
        //return new ManagedTypeProvider(project, metaData, elements);
        
        Object cached = hc.getInfo().getCachedValue(CACHED_MTP);
        if (cached == null) {
            ManagedTypeProvider newContext = new ManagedTypeProvider(project, metaData, elements);
            hc.getInfo().putCachedValue(CACHED_MTP, newContext, CompilationInfo.CacheClearPolicy.ON_CHANGE);
            return newContext;
        } else {
            return (ManagedTypeProvider) cached;
        }
    }

    private static JPAProblemContext createJPAProblemContext(final HintContext hc) {
        final JPAProblemContext[] result = new JPAProblemContext[1];
        final CompilationInfo info = hc.getInfo();
        final TreePath tp = hc.getPath();
        final FileObject file = info.getFileObject();
        TreePath tp2 = tp;
        for (; tp2 != null && tp2.getLeaf().getKind() != Tree.Kind.CLASS; tp2 = tp2.getParentPath()) {
        }
        final Element javaClass = tp2 != null ? info.getTrees().getElement(tp2) : null;
        if (javaClass == null || !(javaClass instanceof TypeElement) || hc.isCanceled()) {
            return null;
        }

        final Project project = FileOwnerQuery.getOwner(file);
        if (project == null || hc.isCanceled()) {//check after each possibe lengthy operation
            return null;
        }
        hc.getInfo().getElements().getTypeElement("java.lang.String");
        MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(file);
        if (emModel == null || hc.isCanceled()) {
            return null;
        }
        try {
            emModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
                @Override
                public Void run(EntityMappingsMetadata metadata) {
                    if (hc.isCanceled() || (metadata.getRoot().getEntity().length == 0 && metadata.getRoot().getMappedSuperclass().length == 0 && metadata.getRoot().getEmbeddable().length == 0)) {
                        return null;//model either isn't ready or no jpa classes
                    }
                    JPAProblemContext context = new JPAProblemContext();
                    context.setMetaData(metadata);
                    context.setJavaClass((TypeElement) javaClass);

                    // if (!idClass){
                    Object modelElement = ModelUtils.getEntity(metadata, (TypeElement) javaClass);

                    if (modelElement != null) {
                        context.setEntity(true);
                    } else {
                        modelElement = ModelUtils.getEmbeddable(metadata, (TypeElement) javaClass);

                        if (modelElement != null) {
                            context.setEmbeddable(true);
                        } else {
                            modelElement = ModelUtils.getMappedSuperclass(metadata, (TypeElement) javaClass);

                            if (modelElement != null) {
                                context.setMappedSuperClass(true);
                            }
                        }
                    }

                    context.setModelElement(modelElement);
                    // }
//                context.setIdClass(idClass);
                    context.setFileObject(file);
                    context.setCompilationInfo(info);

                    if (context.isJPAClass()) {
                        context.setAccessType(JPAHelper.findAccessType((TypeElement) javaClass, context.getModelElement()));
                    }
                    if (!hc.isCanceled()) {
                        result[0] = context;
                    }
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result[0];
    }
}
