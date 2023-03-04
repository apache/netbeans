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
package org.netbeans.modules.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;


/**
 * @author ads
 *
 */
public class InterceptorBindingVerifier implements TargetVerifier {
    
    private InterceptorBindingVerifier(){
    }
    
    private static final InterceptorBindingVerifier INSTANCE = 
        new InterceptorBindingVerifier();
    
    public static InterceptorBindingVerifier getInstance(){
        return INSTANCE;
    }
    
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target , Set<ElementType>
        targetTypes ) 
    {
        int sz = 1;
        if(targetTypes.size()>0){
            sz = targetTypes.size();
            if(targetTypes.contains( ElementType.TYPE)){
                sz--;
            }
            if ( targetTypes.contains( ElementType.METHOD)) {
                sz--;
            }
            if (targetTypes.contains( ElementType.CONSTRUCTOR) )
            {
                sz--;
            }
        }
        return sz==0;
    }
}
