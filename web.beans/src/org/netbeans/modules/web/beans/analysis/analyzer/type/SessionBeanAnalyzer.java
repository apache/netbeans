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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class SessionBeanAnalyzer implements ClassAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel ,
            Result result )
    {
        boolean isSingleton = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.SINGLETON, model.getCompilationController());
        boolean isStateless = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.STATELESS, model.getCompilationController());
        if ( cancel.get() ){
            return;
        }
        try {
            String scope = model.getScope( element );
            if ( isSingleton ) {
                if ( AnnotationUtil.APPLICATION_SCOPED.equals( scope ) || 
                        AnnotationUtil.DEPENDENT.equals( scope ) )
                {
                    return;
                }
                result.requireCdiEnabled(element, model);
                result.addError( element, model,  
                    NbBundle.getMessage(SessionBeanAnalyzer.class, 
                            "ERR_InvalidSingletonBeanScope"));              // NOI18N
            }
            else if ( isStateless ) {
                if ( !AnnotationUtil.DEPENDENT.equals( scope ) )
                {
                    result.addError( element, model,   
                        NbBundle.getMessage(SessionBeanAnalyzer.class, 
                                "ERR_InvalidStatelessBeanScope"));              // NOI18N
                }
            }
        }
        catch (CdiException e) {
            result.requireCdiEnabled(element, model);
            informCdiException(e, element, model, result );
        }
    }
    
    private void informCdiException(CdiException exception , Element element, 
            WebBeansModel model, Result result )
    {
        result.addError(element, model, exception.getMessage());
    }

}
