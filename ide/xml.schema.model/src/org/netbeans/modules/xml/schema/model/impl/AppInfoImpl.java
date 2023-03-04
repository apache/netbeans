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

import java.io.IOException;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class AppInfoImpl extends SchemaComponentImpl implements AppInfo {

    public AppInfoImpl(SchemaModelImpl model) {
	this(model, createNewComponent(SchemaElements.APPINFO, model));
    }

    public AppInfoImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

    public void setURI(String uri) {
        setAttribute(SOURCE_PROPERTY, SchemaAttributes.SOURCE, uri);
    }

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public String getURI() {
        return getAttribute(SchemaAttributes.SOURCE);
    }

    public Element getAppInfoElement() {
        return Element.class.cast(getPeer().cloneNode(true));
    }

    public void setAppInfoElement(Element content) {
        super.updatePeer(CONTENT_PROPERTY, content);
    }

    public Class<? extends SchemaComponent> getComponentType() {
        return AppInfo.class;
    }

    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_PROPERTY, text);
    }

    public String getContentFragment() {
        return super.getXmlFragment();
    }
}
