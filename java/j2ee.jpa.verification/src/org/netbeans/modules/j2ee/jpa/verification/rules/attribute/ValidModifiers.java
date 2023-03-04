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
package org.netbeans.modules.j2ee.jpa.verification.rules.attribute;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import org.openide.util.NbBundle;

/**
 * - accesor method for persistent field *must* be:
 *   - public or protected
 *   - non-final
 * - persistent fields *must not* be public
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidModifiers extends JPAEntityAttributeCheck {
    
    public Collection<ErrorDescription> check(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib) {
        
        Set<Modifier> fieldModifiers = attrib.getInstanceVariable() == null ? null
                : attrib.getInstanceVariable().getModifiers();
        
        Set<Modifier> accesorModifiers = attrib.getAccesor() == null ? null
                : attrib.getAccesor().getModifiers();
        
        Set<Modifier> mutatorModifiers = attrib.getMutator() == null ? null
                : attrib.getMutator().getModifiers();
        
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        
        if (fieldModifiers != null){
            if (fieldModifiers.contains(Modifier.PUBLIC)){
                errors.addAll(getErr(ctx, hc, attrib, "MSG_PublicVariable"));//TODO: error by default
            }
        }
        
        if (accesorModifiers != null){
            if (ctx.getAccessType() == AccessType.PROPERTY && !accesorModifiers.contains(Modifier.PUBLIC)
                    && !accesorModifiers.contains(Modifier.PROTECTED)){
                errors.addAll(getErr(ctx, hc, attrib, "MSG_NonPublicAccesor"));//TODO: error by default
            }
            
            if (accesorModifiers.contains(Modifier.FINAL)){
                errors.addAll(getErr(ctx, hc, attrib, "MSG_FinalAccesor"));//TODO: error by default
            }
        }
        
        if (mutatorModifiers != null){
            // See issue 151387
            //if (!mutatorModifiers.contains(Modifier.PUBLIC)
            //        && !mutatorModifiers.contains(Modifier.PROTECTED)){
            if (mutatorModifiers.contains(Modifier.PRIVATE) ) {
                errors.addAll(getErr(ctx, hc, attrib, "MSG_NonPublicMutator"));//TODO: warning by default
            }
            // see issue #108876
//            else if (attrib.getModelElement() instanceof Id
//                    && mutatorModifiers.contains(Modifier.PUBLIC)){
//                errors.add(Rule.createProblem(attrib.getMutator(), ctx,
//                        NbBundle.getMessage(ValidModifiers.class, "MSG_PublicIdMutatorDiscouraged"),
//                        Severity.WARNING));
//            }
            
            if (mutatorModifiers.contains(Modifier.FINAL)){
                errors.addAll(getErr(ctx, hc, attrib, "MSG_FinalMutator"));//TODO: error by default
            }
        }
        
        return errors;
    }
    private static Collection<ErrorDescription> getErr(JPAProblemContext ctx, HintContext hc, AttributeWrapper attrib, String msgKey) {
        Tree elementTree = ctx.getCompilationInfo().getTrees().getTree(attrib.getJavaElement());

        Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                ctx.getCompilationInfo(), elementTree);

        ErrorDescription error = ErrorDescriptionFactory.forSpan(
                hc,
                underlineSpan.getStartOffset(),
                underlineSpan.getEndOffset(),
                NbBundle.getMessage(ValidColumnName.class, msgKey));//TODO: may need to have "error" fo some/ warning for another
        return Collections.singleton(error);
    }
}
