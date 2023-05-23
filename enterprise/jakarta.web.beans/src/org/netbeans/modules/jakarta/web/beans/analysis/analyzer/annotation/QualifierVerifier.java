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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;


/**
 * @author ads
 *
 */
public class QualifierVerifier implements TargetVerifier {
    
    
    private static final QualifierVerifier INSTANCE = new QualifierVerifier( false );
    
    private static final QualifierVerifier EVENT_INSTANCE = 
            new QualifierVerifier( true );
    
    private QualifierVerifier(boolean event){
        isEvent = event;
    }
    
    public static QualifierVerifier getInstance( boolean event ){
        if ( event ){
            return EVENT_INSTANCE;
        }
        else {
            return INSTANCE;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.TargetVerifier#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target,
            Set<ElementType> targetTypes )
    {
        boolean hasRequiredTarget = false;
//        if ( isEvent() ){ TODO: any reason why it was different, ins specification event qualifier is just the same as usual, and example have 4 targets but it's just example? see #225556
        //especially it was updated for evvents before but specification is new for all qualifiers. keep for now commented
            boolean hasFieldParameterTarget = targetTypes.contains(
                    ElementType.FIELD) &&
                        targetTypes.contains(ElementType.PARAMETER);
            if ( !hasFieldParameterTarget){
                hasRequiredTarget = (targetTypes.size() == 1 && 
                                        (targetTypes.contains(ElementType.TYPE) || 
                                         targetTypes.contains(ElementType.METHOD) ||
                                         targetTypes.contains(ElementType.FIELD))) ||
                                    (targetTypes.size() == 2 && 
                                         targetTypes.contains(ElementType.METHOD) &&
                                         targetTypes.contains(ElementType.FIELD));//see #244059
            }
            else {
                if ( targetTypes.size() == 2 ){
                    hasRequiredTarget = true;
                }
                else {
                    hasRequiredTarget = 
                            (targetTypes.size() == 4 && 
                                targetTypes.contains( ElementType.METHOD) &&
                                targetTypes.contains( ElementType.TYPE)) || 
                            (targetTypes.size() == 3 && 
                                targetTypes.contains( ElementType.METHOD));
                }
            }
//        }
//        else {
//            hasRequiredTarget = targetTypes.size() == 4 && 
//                targetTypes.contains( ElementType.METHOD) &&
//                        targetTypes.contains(ElementType.FIELD) &&
//                            targetTypes.contains(ElementType.PARAMETER)&&
//                                targetTypes.contains( ElementType.TYPE);
//        }
        
        return hasRequiredTarget;
    }
    
    private boolean isEvent(){
        return isEvent;
    }
    
    private boolean isEvent;
}
