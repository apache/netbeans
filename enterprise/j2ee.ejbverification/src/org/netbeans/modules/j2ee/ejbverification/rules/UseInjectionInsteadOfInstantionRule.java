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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.CallEjbGenerator;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbReferenceSupport;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Hint that inform users that the session bean is instantiated calling
 * its constructor instead of recommended @EJB injection.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@Hint(id="o.n.m.j2ee.ejbverification.UseInjectionInsteadOfInstantionRule",
        displayName = "#UseInjectionInsteadOfInstantionRule.display.name",
        description = "#UseInjectionInsteadOfInstantionRule.desc",
        category = "javaee/ejb",
        enabled = true,
        suppressWarnings = "UseInjectionInsteadOfInstantion")
@NbBundle.Messages({
    "UseInjectionInsteadOfInstantionRule.display.name=Instantiation replaceable with @EJB injection",
    "UseInjectionInsteadOfInstantionRule.desc=Finds instantiations of a bean which can be injected by @EJB annotation",
    "UseInjectionInsteadOfInstantionRule.fix=Replace Instantiation of bean by @EJB injection",
    "UseInjectionInsteadOfInstantionRule.error=Instantiation of bean can be replaced by @EJB injection"
})
public final class UseInjectionInsteadOfInstantionRule {

    private static final RequestProcessor RP = new RequestProcessor("UseInjectionInsteadOfInstantionFixRP", 1);

    private UseInjectionInsteadOfInstantionRule() {
    }

    @TriggerPattern("$type $name = new $clazz()")
    public static ErrorDescription useInjectionInsteadOfInstantion(final HintContext ctx) {
        CompilationInfo cpi = ctx.getInfo();
        final TreePath clazzTreePath = ctx.getVariables().get("$clazz"); //NOI18N
        if (clazzTreePath == null) {
            return null;
        }

        // is valid class?
        Element element = cpi.getTrees().getElement(clazzTreePath);
        if (!(element instanceof TypeElement)) {
            return null;
        }

        final TypeElement enclosing = findNearestTypeElement(ctx);
        if (enclosing == null) {
            return null;
        }

        // class inside EJB environment
        final EJBProblemContext ejbContext = HintsUtils.getOrCacheContext(ctx, enclosing.asType().toString());
        if (ejbContext == null) {
            return null;
        }

        final TypeElement javaClass = (TypeElement) element;
        try {
            return ejbContext.getEjbModule().getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, ErrorDescription>() {
                @Override
                public ErrorDescription run(EjbJarMetadata metadata) {
                    Ejb ejb = metadata.findByEjbClass(ElementUtilities.getBinaryName(javaClass));
                    if (ejb != null) {
                        ReplaceInstantionByInjectionFix fix = new ReplaceInstantionByInjectionFix(ctx, ejbContext.getEjbModule(), enclosing);
                        return ErrorDescriptionFactory.forTree(
                                ctx,
                                ctx.getPath(),
                                Bundle.UseInjectionInsteadOfInstantionRule_error(),
                                fix);
                    }
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static TypeElement findNearestTypeElement(HintContext ctx) {
        Iterator<Tree> iterator = ctx.getPath().iterator();
        while (iterator.hasNext()) {
            Tree next = iterator.next();
            if (next.getKind() == Tree.Kind.CLASS) {
                TreePath path = ctx.getInfo().getTrees().getPath(ctx.getInfo().getCompilationUnit(), next);
                return (TypeElement) ctx.getInfo().getTrees().getElement(path);
            }
        }
        return null;
    }

    private static class ReplaceInstantionByInjectionFix implements Fix {

        private final HintContext context;
        private final EjbJar ejbJar;
        private final TypeElement enclosing;

        public ReplaceInstantionByInjectionFix(HintContext context, EjbJar ejbJar, TypeElement enclosing) {
            this.context = context;
            this.ejbJar = ejbJar;
            this.enclosing = enclosing;
        }

        @Override
        public String getText() {
            return Bundle.UseInjectionInsteadOfInstantionRule_fix();
        }

        @Override
        public ChangeInfo implement() throws Exception {

            RP.post(new Runnable() {

                @Override
                public void run() {
                    // remove instantion
                    try {
                        Fix removeFromParent = JavaFixUtilities.removeFromParent(context, null, context.getPath());
                        removeFromParent.implement();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    // inject reference
                    try {
                        TypeElement javaClass = (TypeElement) context.getInfo().getTrees().getElement(context.getVariables().get("$clazz")); //NOI18N
                        FileObject referencingFO = context.getInfo().getFileObject();
                        String referencingBN = ElementUtilities.getBinaryName(enclosing);
                        String referencedSN = javaClass.getSimpleName().toString();
                        String name = _RetoucheUtil.uniqueMemberName(referencingFO, referencingBN, referencedSN, referencedSN);

                        CallEjbGenerator generator = CallEjbGenerator.create(
                                EjbReferenceSupport.createEjbReference(ejbJar, javaClass.toString()),
                                name,
                                true);

                        generator.addReference(
                            referencingFO,
                            referencingBN,
                            SourceUtils.getFile(ElementHandle.create(javaClass), context.getInfo().getClasspathInfo()),
                            ElementUtilities.getBinaryName(javaClass),
                            null,
                            EjbReference.EjbRefIType.NO_INTERFACE,
                            false,
                            FileOwnerQuery.getOwner(context.getInfo().getFileObject())
                        );
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            return null;
        }
    }

}
