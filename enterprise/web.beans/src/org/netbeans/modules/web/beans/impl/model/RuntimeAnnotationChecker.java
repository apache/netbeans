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
package org.netbeans.modules.web.beans.impl.model;

import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetAnalyzer;


/**
 * @author ads
 *
 */
public abstract class RuntimeAnnotationChecker extends TargetAnalyzer {
    
    protected static final String VALUE = "value";                // NOI18N
    
    public void init( TypeElement element, AnnotationModelHelper helper  ) {
        init( (Element)element , helper.getHelper() );
    }

    public boolean check() {
        List<? extends AnnotationMirror> annotations = getElement()
                .getAnnotationMirrors();
        boolean hasAnnotation = getAnnotation().stream().anyMatch(s -> getHelper().hasAnnotation(annotations, s));

        if (!hasAnnotation) {
            // this is not subject annotation , just return false
            return false;
        }
        
        if ( !hasRuntimeRetention() ){
            getLogger().log(Level.WARNING, "Annotation "
                    + getElement().getQualifiedName()
                    + " declared as one of " +getAnnotation()+" but has wrong retention policy."
                    + " Correct retention policy is "
                    + RetentionPolicy.RUNTIME.toString());// NOI18N
            return false;
        }
        
        return hasTarget();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.RuntimeRetentionAnalyzer#handleNoRetention()
     */
    @Override
    protected void handleNoRetention() {
        getLogger().log(Level.WARNING, "Annotation "
                + getElement().getQualifiedName()
                + "declared as one of " +getAnnotation()+" but has no Retention");// NOI18N
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.annotation.TargetAnalyzer#handleNoTarget()
     */
    @Override
    protected void handleNoTarget() {
        getLogger().log(Level.WARNING, "Annotation "
                + getElement().getQualifiedName()
                + "declared as one of " +getAnnotation()+" but has no Target");// NOI18N
    }
    
    protected abstract Logger getLogger();
    
    protected abstract List<String> getAnnotation();
    
    @Override
    protected TypeElement getElement(){
        return (TypeElement)super.getElement();
    }
}
