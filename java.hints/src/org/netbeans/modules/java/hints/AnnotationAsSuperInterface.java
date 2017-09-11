/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
@Hint(displayName = "#DN_AnnotationAsSuperInterface", description = "#DESC_AnnotationAsSuperInterface", category="rules15", id="AnnotationAsSuperInterface", suppressWarnings="AnnotationAsSuperInterface", options=Options.QUERY)
public class AnnotationAsSuperInterface {

    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static Iterable<ErrorDescription> run(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getPath());

        if ( e == null || !(e instanceof TypeElement) ) {
            return null;
        }
        
        List<ErrorDescription> eds = new ArrayList<ErrorDescription>();
        
        for (Tree i : ((ClassTree) ctx.getPath().getLeaf()).getImplementsClause()) {
            Element ie = ctx.getInfo().getTrees().getElement(new TreePath(ctx.getPath(), i));

            if (ie != null && ie.getKind() == ElementKind.ANNOTATION_TYPE) {
                eds.add(ErrorDescriptionFactory.forTree(ctx, i, NbBundle.getMessage(AnnotationAsSuperInterface.class,
                                    "HNT_AnnotationAsSuperInterface",  // NOI18N
                                    ie.getSimpleName().toString())));
            }
        }

        return eds;
    }
    
}
