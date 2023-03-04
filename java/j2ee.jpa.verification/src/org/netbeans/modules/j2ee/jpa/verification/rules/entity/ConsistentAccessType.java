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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.jpa.verification.fixes.UnifyAccessType;
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
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.ConsistentAccessType",
        displayName = "#ConsistentAccessType.display.name",
        description = "#ConsistentAccessType.desc",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "ConsistentAccessType")
@NbBundle.Messages({
    "ConsistentAccessType.display.name=Check access types for jpa classes",
    "ConsistentAccessType.desc=JPA classes need to have consistent access types for fields/properties"})
public class ConsistentAccessType {

    @TriggerPatterns(value = {
        @TriggerPattern(value = JPAAnnotations.ENTITY),
        @TriggerPattern(value = JPAAnnotations.EMBEDDABLE),
        @TriggerPattern(value = JPAAnnotations.MAPPED_SUPERCLASS),
        @TriggerPattern(value = JPAAnnotations.ID_CLASS)})
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled() || (hc.getPath().getLeaf().getKind() != Tree.Kind.IDENTIFIER || hc.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.ANNOTATION)) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }

        if (((JPAProblemContext) ctx).getAccessType() == AccessType.INCONSISTENT) {
            ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());

            Fix fix1 = new UnifyAccessType.UnifyFieldAccess(ctx.getFileObject(), classHandle);
            Fix fix2 = new UnifyAccessType.UnifyPropertyAccess(ctx.getFileObject(), classHandle);
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
                    NbBundle.getMessage(ConsistentAccessType.class, "MSG_InconsistentAccessType"),
                    fix1, fix2);
        }
        return null;
    }
}
