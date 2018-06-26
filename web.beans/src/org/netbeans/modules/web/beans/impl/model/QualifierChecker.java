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
package org.netbeans.modules.web.beans.impl.model;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;

import org.netbeans.modules.web.beans.analysis.analyzer.annotation.QualifierVerifier;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetVerifier;


/**
 * @author ads
 *
 */
class QualifierChecker extends RuntimeAnnotationChecker implements Checker {
    
    private static final String QUALIFIER_TYPE_ANNOTATION=
        "javax.inject.Qualifier";                               // NOI18N
    
    QualifierChecker(){
        this( false );
    }
    
    QualifierChecker( boolean event ){
        isEvent = event;
    }
    
    static QualifierChecker get() {
        // could be changed to cached ThreadLocal access
        return new QualifierChecker();
    }
    
    static QualifierChecker get(boolean event) {
        // could be changed to cached ThreadLocal access
        return new QualifierChecker(event);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Checker#check()
     */
    @Override
    public boolean check() {
        if ( BUILT_IN_QUALIFIERS.contains( getElement().getQualifiedName().toString())){
            return true;
        }
        else {
            return super.check();
        }
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.RuntimeAnnotationChecker#getAnnotation()
     */
    @Override
    protected String getAnnotation() {
        return QUALIFIER_TYPE_ANNOTATION;
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.RuntimeAnnotationChecker#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return FieldInjectionPointLogic.LOGGER;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetAnalyzer#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target,
            Set<ElementType> set )
    {
        boolean hasRequiredTarget = super.hasReqiredTarget(target, set);
        if (!hasRequiredTarget) {
            if ( isEvent ) {
                getLogger().log(Level.WARNING, "Annotation "
                        + getElement().getQualifiedName()
                        + "declared as Qualifier but has wrong target values."
                        + " Correct target values are {METHOD, FIELD, PARAMETER, TYPE}"
                        + " or {FIELD, PARAMETER}");// NOI18N
            }
            else {
                getLogger().log(Level.WARNING, "Annotation "
                        + getElement().getQualifiedName()
                        + "declared as Qualifier but has wrong target values."
                        + " Correct target values are {METHOD, FIELD, PARAMETER, TYPE}");// NOI18N
            }
        }
        return hasRequiredTarget;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
     */
    @Override
    protected TargetVerifier getTargetVerifier() {
        return QualifierVerifier.getInstance( isEvent );
    }
    
    private static final Set<String> BUILT_IN_QUALIFIERS = new HashSet<String>();
    
    static {
        BUILT_IN_QUALIFIERS.add(WebBeansModelProviderImpl.ANY_QUALIFIER_ANNOTATION);
        BUILT_IN_QUALIFIERS.add(WebBeansModelProviderImpl.NEW_QUALIFIER_ANNOTATION);
        BUILT_IN_QUALIFIERS.add(WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION);
        BUILT_IN_QUALIFIERS.add(WebBeansModelProviderImpl.NAMED_QUALIFIER_ANNOTATION);
    }

    private boolean isEvent;

}
