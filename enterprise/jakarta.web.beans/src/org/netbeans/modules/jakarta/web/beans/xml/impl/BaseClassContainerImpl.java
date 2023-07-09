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
package org.netbeans.modules.jakarta.web.beans.xml.impl;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.jakarta.web.beans.xml.BeansElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author ads
 *
 */
abstract class BaseClassContainerImpl extends WebBeansComponentImpl {

    private final WebBeansModelImpl model;
    
    BaseClassContainerImpl( WebBeansModelImpl model, Element e ) {
        super(model, e);
        this.model = model;
    }

    public List<String> getClasses(){
        NodeList nl = getPeer().getElementsByTagName(BeansElement.CLASS);
        List<String> result = new ArrayList<String>( nl.getLength());
        if (nl != null) {
            for (int i=0; i<nl.getLength(); i++) {
                if (WebBeansElements.CLASS.getQName(model).equals(
                        getQName(nl.item(i)))) 
                {
                    result.add(getText((Element) nl.item(i)));
                }
            }
        }
        return result;
    }

}
