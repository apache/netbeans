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

import org.netbeans.modules.xml.schema.model.Any;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public abstract class CommonAnyImpl extends SchemaComponentImpl implements Any {

    /**
     * Creates a new instance of CommonAnyImpl
     */
    public CommonAnyImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }

    /**
     *
     */
    public void setNamespace(String namespace) {
	setAttribute(NAMESPACE_PROPERTY, SchemaAttributes.NAMESPACE, namespace);
    }
    
    /**
     *
     */
    public void setProcessContents(ProcessContents pc) {
	setAttribute(PROCESS_CONTENTS_PROPERTY, SchemaAttributes.PROCESS_CONTENTS, pc);
    }
    
    public ProcessContents getDefaultProcessContents() {
        return ProcessContents.STRICT;
    }
	
    public ProcessContents getEffectiveProcessContents() {
        ProcessContents v = getProcessContents();
        return v == null ? getDefaultProcessContents() : v;
    }
    
    /**
     *
     */
    public ProcessContents getProcessContents() {
	String s = getAttribute(SchemaAttributes.PROCESS_CONTENTS);
	return s == null? null : Util.parse(ProcessContents.class, s);
    }
    
    public ProcessContents getProcessContentsEffective() {
        ProcessContents v = getProcessContents();
        return v == null ? getProcessContentsDefault() : v;
    }

    public ProcessContents getProcessContentsDefault() {
        return ProcessContents.STRICT;
    }

    /**
     *
     */
    public String getNamespace() {
	return getAttribute(SchemaAttributes.NAMESPACE);
	
    }
    
    public String getNamespaceDefault() {
        return "##any"; //NOI18N
    }

    public String getNameSpaceEffective() {
        String v = getNamespace();
        return v == null ? getNamespaceDefault() : v;
    }
}
