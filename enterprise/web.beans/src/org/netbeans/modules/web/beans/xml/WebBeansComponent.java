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
package org.netbeans.modules.web.beans.xml;

import org.netbeans.modules.xml.xam.dom.DocumentComponent;


/**
 * @author ads
 *
 */
public interface WebBeansComponent extends DocumentComponent<WebBeansComponent> {
    
    String WEB_BEANS_NAMESPACE_OLD = "http://java.sun.com/xml/ns/javaee";   // NOI18N
    String WEB_BEANS_NAMESPACE = "http://xmlns.jcp.org/xml/ns/javaee";   // NOI18N

    WebBeansModel getModel();
    
    Class<? extends WebBeansComponent> getComponentType();
    
    void accept( WebBeansVisitor visitor );
}
