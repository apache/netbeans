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
