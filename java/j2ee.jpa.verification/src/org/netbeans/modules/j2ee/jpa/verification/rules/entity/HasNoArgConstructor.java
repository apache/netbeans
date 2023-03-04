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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateDefaultConstructor;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 * The class must have a public or protected, no-argument constructor.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.HasNoArgConstructor",
        displayName = "#HasNoArgConstructor.display.name",
        description = "#HasNoArgConstructor.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.WARNING,
        suppressWarnings = "HasNoArgConstructor")
@NbBundle.Messages({
    "HasNoArgConstructor.display.name=Default public/protected constructor",
    "HasNoArgConstructor.desc=JPA classes need to have default public/protected no arg constructor"})
public class HasNoArgConstructor {


    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.ENTITY),
        @TriggerPattern(value = JPAAnnotations.EMBEDDABLE),
        @TriggerPattern(value = JPAAnnotations.ID_CLASS)})
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }
        
        JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }

        TypeElement subject = ctx.getJavaClass();
        
        // If the class is not public, no need to give this warning, yet. 
        // See issue 110170
        if (!subject.getModifiers().contains(Modifier.PUBLIC)) {
            return null;
        }

        boolean hasDefaultContructor = true;

        for (ExecutableElement constr : ElementFilter.constructorsIn(subject.getEnclosedElements())) {
            hasDefaultContructor = false;

            if (constr.getParameters().isEmpty()
                    && (constr.getModifiers().contains(Modifier.PUBLIC)
                    || constr.getModifiers().contains(Modifier.PROTECTED))) {
                return null; // found appropriate constructor
            }
        }

        if (hasDefaultContructor) {
            return null; // OK
        }

        Fix fix = new CreateDefaultConstructor(ctx.getFileObject(),
                ElementHandle.create(ctx.getJavaClass()));
        
        TreePath par = hc.getPath();
        while(par!=null && par.getParentPath()!=null && par.getLeaf().getKind()!= Tree.Kind.CLASS){
            par = par.getParentPath();
        }
        
        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                           ctx.getCompilationInfo(), par.getLeaf());

        return ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(HasNoArgConstructor.class, "MSG_HasNoNoArgConstructor"),
                    fix);
    }
}
