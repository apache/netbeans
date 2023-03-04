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
import java.util.Arrays;
import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * 
 * @author Tomasz.Slota@Sun.COM
 */
@Hint(id = "o.n.m.j2ee.jpa.verification.OnlyEntityOrMappedSuperclassCanUseIdClass",
        displayName = "#OnlyEntityOrMappedSuperclassCanUseIdClass.display.name",
        description = "#MSG_OnlyEntityOrMappedSuperclassCanUseIdClass",
        category = "javaee/jpa",
        enabled = true,
        severity = Severity.ERROR,
        suppressWarnings = "OnlyEntityOrMappedSuperclassCanUseIdClass")
@NbBundle.Messages({
    "OnlyEntityOrMappedSuperclassCanUseIdClass.display.name=Verify @IdClass location"})
public class OnlyEntityOrMappedSuperclassCanUseIdClass {
    
    /** Creates a new instance of NonFinalClass */
    @TriggerTreeKind(value = Tree.Kind.CLASS)
    public static ErrorDescription apply(HintContext hc) {
        if (hc.isCanceled()) {//NOI18N
            return null;//we pass only if it is an annotation
        }

        final JPAProblemContext ctx = ModelUtils.getOrCreateCachedContext(hc);
        if (ctx == null || hc.isCanceled()) {
            return null;
        }
        TypeElement subject = ctx.getJavaClass();
        AnnotationMirror isENtityMapped = getFirstAnnotationFromGivenSet(subject,
                Arrays.asList(JPAAnnotations.ENTITY, JPAAnnotations.MAPPED_SUPERCLASS));
        
        if(isENtityMapped != null) {
            return null;
        }
        
        if (Utilities.hasAnnotation(subject, JPAAnnotations.ID_CLASS)){
            
           TreePath par = hc.getPath();
            while (par != null && par.getParentPath() != null && par.getLeaf().getKind() != Tree.Kind.CLASS) {
                par = par.getParentPath();
            }

            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                    ctx.getCompilationInfo(), par.getLeaf());

            return ErrorDescriptionFactory.forSpan(
                    hc,
                    underlineSpan.getStartOffset(),
                    underlineSpan.getEndOffset(),
                    NbBundle.getMessage(OnlyEntityOrMappedSuperclassCanUseIdClass.class, "MSG_OnlyEntityOrMappedSuperclassCanUseIdClass"));
        }
        
        return null;
    }
    private static AnnotationMirror getFirstAnnotationFromGivenSet(TypeElement subject,
            Collection<String> annotationClasses) {

        for (String annClass : annotationClasses) {
            AnnotationMirror foundAnn = Utilities.findAnnotation(subject, annClass);

            if (foundAnn != null) {
                return foundAnn;
            }
        }

        return null;
    }
}
