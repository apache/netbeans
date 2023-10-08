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

package org.netbeans.modules.j2ee.dd.impl.web.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;

/**
 * This class represents javax.servlet.annotation.WebServlet annotation
 * @author Petr Slechta
 */
public class WebServlet extends PersistentObject implements Refreshable {
    private String name;
    private List<String> urlPatterns = new ArrayList<String>();
    private String clazz;

    public WebServlet(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    public boolean refresh(TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> annByType = getHelper().getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror annotationMirror = annByType.get("jakarta.servlet.annotation.WebServlet"); // NOI18N
        if(annotationMirror == null) {
            annotationMirror = annByType.get("javax.servlet.annotation.WebServlet"); // NOI18N
        }
        if (annotationMirror == null) {
            return false;
        }

        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("name", AnnotationParser.defaultValue(
                typeElement.getSimpleName().toString())); // NOI18N
        // Fix for IZ#172425 - Servlet 3.0 model is not updated properly
        urlPatterns.clear();
        parser.expectStringArray("urlPatterns", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String)arrayMember.getValue();
                    urlPatterns.add(value);
                }
                return null;
            }
        }, null);
        parser.expectStringArray("value", new ArrayValueHandler() { // NOI18N
            public Object handleArray(List<AnnotationValue> arrayMembers) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    String value = (String)arrayMember.getValue();
                    urlPatterns.add(value);
                }
                return null;
            }
        }, null);
        ParseResult parseResult = parser.parse(annotationMirror);
        name = parseResult.get("name", String.class); // NOI18N
        clazz = typeElement.getQualifiedName().toString();
        return true;
    }

    public String getName() {
        return name;
    }

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    public String getServletClass() {
        return clazz;
    }

}
