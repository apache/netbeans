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

package org.apache.tools.ant.module.nodes;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

class AntProperty extends Node.Property<String> {

    private Element el;
    private String name;

    public AntProperty(Element el, String name) {
        this(name);
        this.el = el;
    }

    protected AntProperty(String name) {
        super(String.class);
        setName(name);
        this.name = name;
    }

    protected Element getElement() {
        return el;
    }

    @Override
    public String getValue() {
        Element _el = getElement();
        if (_el == null) { // #9675
            return NbBundle.getMessage(AntProperty.class, "LBL_property_invalid_no_element");
        }
        return _el.getAttribute(name);
    }
    
    @Override
    public boolean canRead() {
        return true;
    }
    
    @Override
    public boolean canWrite() {
        return false;
    }
    
    @Override
    public void setValue(String val) throws IllegalArgumentException{
        throw new IllegalArgumentException();
    }
    
}
