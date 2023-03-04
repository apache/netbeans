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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class AnnotationImpl extends SchemaComponentImpl implements Annotation {

    protected AnnotationImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.ANNOTATION, model));
    }
    /**
     * Creates a new instance of AnnotationImpl
     */
    public AnnotationImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Annotation.class;
	}
    
    /**
     *
     */
    public void removeDocumentation(Documentation documentation) {
	removeChild(DOCUMENTATION_PROPERTY, documentation);
    }
    
    /**
     *
     */
    public void addDocumentation(Documentation documentation) {
	appendChild(DOCUMENTATION_PROPERTY, documentation);
    }
    
    /**
     *
     */
    public void accept(SchemaVisitor visitor) {
	visitor.visit(this);
    }
    
    /**
     *
     */
    public java.util.Collection<Documentation> getDocumentationElements() {
	return getChildren(Documentation.class);
    }

    public void removeAppInfo(AppInfo appInfo) {
        removeChild(Annotation.APPINFO_PROPERTY, appInfo);
    }

    public void addAppInfo(AppInfo appInfo) {
        appendChild(Annotation.APPINFO_PROPERTY, appInfo);
    }

    public Collection<AppInfo> getAppInfos() {
	return getChildren(AppInfo.class);
    }
}
