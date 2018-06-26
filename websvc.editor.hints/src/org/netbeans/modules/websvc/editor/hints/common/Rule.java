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

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;

/**
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 */
public abstract class Rule<E extends Element> {
    
    public final ErrorDescription[] execute(E subject, ProblemContext ctx){
        if (isApplicable(subject, ctx)){
            return apply(subject, ctx);
        }
        
        return null;
    }
    
    /**
     * A rule is applied to an individual element, called subject.
     *
     * @param subject the element where the rule will be applied.
     * @param ctx     additional information passed onto this test.
     * @return a problem object which represents a violation of a rule. It
     *         returns null, if no violation was detected.
     */
    protected abstract ErrorDescription[] apply(E subject, ProblemContext ctx);
    
    protected abstract boolean isApplicable(E subject, ProblemContext ctx);
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description){
        return createProblem(subject, ctx, description, Severity.ERROR, Collections.<Fix>emptyList());
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description, Severity severity){
        return createProblem(subject, ctx, description, severity, Collections.<Fix>emptyList());
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx, String description,
            Severity severity, Fix fix){
        return createProblem(subject, ctx, description, severity, Collections.singletonList(fix));
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx, String description, Fix fix){
        return createProblem(subject, ctx, description, Severity.ERROR, Collections.singletonList(fix));
    }
    
    public static ErrorDescription createProblem(Element subject, ProblemContext ctx,
            String description, Severity severity, List<Fix> fixes){
        ErrorDescription err = null;
        List<Fix> fixList = fixes == null ? Collections.<Fix>emptyList() : fixes;
        
        // by default place error annotation on the element being checked
        Tree elementTree = ctx.getElementToAnnotate() == null ?
            ctx.getCompilationInfo().getTrees().getTree(subject) : ctx.getElementToAnnotate();
        
        if (elementTree != null){
            Utilities.TextSpan underlineSpan = Utilities.getUnderlineSpan(
                    ctx.getCompilationInfo(), elementTree);
            
            err = ErrorDescriptionFactory.createErrorDescription(
                    severity, description, fixList, ctx.getFileObject(),
                    underlineSpan.getStartOffset(), underlineSpan.getEndOffset());
        }
        return err;
    }
}
