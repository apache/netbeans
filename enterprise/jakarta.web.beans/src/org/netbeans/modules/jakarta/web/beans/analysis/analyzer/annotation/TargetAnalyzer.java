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
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;


/**
 * @author ads
 *
 */
public abstract class TargetAnalyzer extends RuntimeRetentionAnalyzer 
    implements TargetVerifier 
{

    public boolean hasTarget() {
        Map<String, ? extends AnnotationMirror> types = getHelper()
                .getAnnotationsByType(getElement().getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            handleNoTarget();
        }
        return hasReqiredTarget( target , getDeclaredTargetTypes( getHelper(), 
                target ));
    }
    
    public Set<ElementType> getDeclaredTargetTypes() {
        Map<String, ? extends AnnotationMirror> types = getHelper()
                .getAnnotationsByType(getElement().getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            return Collections.emptySet();
        }
        return getDeclaredTargetTypes( getHelper(), target );
    }
    
    public static Set<ElementType> getDeclaredTargetTypes( 
            AnnotationHelper helper, TypeElement element ) 
    {
        Map<String, ? extends AnnotationMirror> types = helper
                .getAnnotationsByType(element.getAnnotationMirrors());
        AnnotationMirror target = types.get(Target.class.getCanonicalName());
        if (target == null) {
            return Collections.emptySet();
        }
        return getDeclaredTargetTypes( helper, target );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation.TargetVerifier#hasReqiredTarget(javax.lang.model.element.AnnotationMirror, java.util.Set)
     */
    @Override
    public boolean hasReqiredTarget( AnnotationMirror target, 
            Set<ElementType> set ) 
    {
        return getTargetVerifier().hasReqiredTarget(target, set );
    }
    
    protected abstract TargetVerifier getTargetVerifier();

    protected abstract void handleNoTarget();
    
    private static Set<ElementType> getDeclaredTargetTypes(
            AnnotationHelper helper, AnnotationMirror target)
    {
        AnnotationParser parser = AnnotationParser.create(helper);
        final Set<String> elementTypes = new HashSet<String>();
        parser.expectEnumConstantArray( AnnotationUtil.VALUE, 
                helper.resolveType(
                ElementType.class.getCanonicalName()), 
                new ArrayValueHandler() {
                    
                    @Override
                    public Object handleArray( List<AnnotationValue> arrayMembers ) {
                        for (AnnotationValue arrayMember : arrayMembers) {
                            String value = arrayMember.getValue().toString();
                            elementTypes.add(value);
                        }
                        return null;
                    }
                } , null);
        
        parser.parse( target );
        Set<ElementType> result = new HashSet<ElementType>();
        for (String type : elementTypes) {
            ElementType elementType = ElementType.valueOf(ElementType.class, type);
            result.add( elementType );
        }
        return result;
    }

}
