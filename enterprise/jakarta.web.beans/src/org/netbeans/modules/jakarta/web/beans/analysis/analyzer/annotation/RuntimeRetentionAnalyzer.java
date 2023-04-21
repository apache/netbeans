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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;


/**
 * @author ads
 *
 */
public abstract class RuntimeRetentionAnalyzer {
    
    public void init( Element element, AnnotationHelper helper ) {
        myHelper = helper;
        myElement = element;
    }
    
    public void init( Element element, CompilationInfo info  ) {
        init( element , new AnnotationHelper(info));
    }
    
    public boolean hasRuntimeRetention() {
        Map<String, ? extends AnnotationMirror> types = getHelper()
            .getAnnotationsByType(getElement().getAnnotationMirrors());
        AnnotationMirror retention = types.get(Retention.class.getCanonicalName()); 
        if ( retention == null ) {
            handleNoRetention();
            return false;
        }

        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectEnumConstant(AnnotationUtil.VALUE, getHelper().resolveType(
                RetentionPolicy.class.getCanonicalName()), null);
        
        String retentionPolicy = parser.parse(retention).get(AnnotationUtil.VALUE,
                String.class);
        return RetentionPolicy.RUNTIME.toString().equals(retentionPolicy);
    }
    
    protected abstract void handleNoRetention();

    protected Element getElement(){
        return myElement;
    }
    
    protected AnnotationHelper getHelper(){
        return myHelper;
    }
    
    private AnnotationHelper myHelper;
    private Element myElement;
}
