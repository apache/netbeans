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
