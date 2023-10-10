/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;

import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.QualifierVerifier;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.TargetVerifier;


/**
 * @author ads
 *
 */
class QualifierChecker extends RuntimeAnnotationChecker implements Checker {

    private static final String QUALIFIER_TYPE_ANNOTATION=
        "jakarta.inject.Qualifier";                               // NOI18N

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
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.Checker#check()
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
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.RuntimeAnnotationChecker#getAnnotation()
     */
    @Override
    protected String getAnnotation() {
        return QUALIFIER_TYPE_ANNOTATION;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.RuntimeAnnotationChecker#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return FieldInjectionPointLogic.LOGGER;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.TargetAnalyzer#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
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
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
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
