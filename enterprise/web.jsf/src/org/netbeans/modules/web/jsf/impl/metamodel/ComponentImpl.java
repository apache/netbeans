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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.jsf.api.metamodel.Component;


/**
 * @author ads
 * @author Martin Fousek
 */
public class ComponentImpl extends PersistentObject implements Component,  Refreshable {

    private String type;
    private String clazz;
    private String namespace;
    private String tagName;
    private Boolean createTag;

    private static final String DEFAULT_COMPONENT_NS = "http://xmlns.jcp.org/jsf/component"; //NOI18N

    protected ComponentImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    @Override
    public String getComponentClass() {
        return clazz;
    }

    @Override
    public String getComponentType() {
        return type;
    }

    // Following three attributes can be specified thru the annotation only for now.

    /**
     * Gets the {@code @FacesComponent} namespace.
     * @return namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the {@code @FacesComponent} tag name.
     * @return tag name
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Whether the {@code @FacesComponent} can be used as tag or not.
     * @return {@code true} is the tag for this {@code @FacesComponent} can be used, {@code false} otherwise
     */
    public boolean isCreateTag() {
        return createTag;
    }

    @Override
    public boolean refresh(TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> types = getHelper().getAnnotationsByType(
                getHelper().getCompilationController().getElements().getAllAnnotationMirrors(typeElement));
        AnnotationMirror annotationMirror = types.get("jakarta.faces.component.FacesComponent"); //NOI18N
        if (annotationMirror == null) {
            annotationMirror = types.get("javax.faces.component.FacesComponent"); // NOI18N
        }
        if (annotationMirror == null) {
            return false;
        }
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString("value", null);                         //NOI18N
        parser.expectString("namespace", null);                     //NOI18N
        parser.expectString("tagName", null);                       //NOI18N
        parser.expectPrimitive("createTag", Boolean.class, null);   //NOI18N
        ParseResult parseResult = parser.parse(annotationMirror);
        type = parseResult.get("value", String.class);              //NOI18N
        clazz = typeElement.getQualifiedName().toString();
        createTag = parseResult.get("createTag", Boolean.class);    //NOI18N
        if (createTag == null) {
            createTag = Boolean.FALSE;
        }
        namespace = parseResult.get("namespace", String.class);     //NOI18N
        if (namespace == null) {
            namespace = DEFAULT_COMPONENT_NS;
        }
        tagName = parseResult.get("tagName", String.class);         //NOI18N
        if (tagName == null) {
            tagName = typeElement.getSimpleName().toString();
            tagName = tagName.substring(0, 1).toLowerCase() + tagName.substring(1);
        }
        return true;
    }
}
