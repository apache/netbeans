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

package org.apache.tools.ant.module.bridge.impl;

import java.util.Enumeration;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;

/**
 * @author Jesse Glick
 */
final class IntrospectionHelperImpl implements IntrospectionHelperProxy {

    private final IntrospectionHelper helper;

    public IntrospectionHelperImpl(Class c) {
        helper = IntrospectionHelper.getHelper(c);
    }

    public Class getAttributeType(String name) {
        return helper.getAttributeType(name);
    }
    
    @SuppressWarnings("unchecked") // XXX better to make Generics.checkedEnumeration?
    public Enumeration<String> getAttributes() {
        return helper.getAttributes();
    }
    
    public Class getElementType(String name) {
        return helper.getElementType(name);
    }
    
    @SuppressWarnings("unchecked")
    public Enumeration<String> getNestedElements() {
        return helper.getNestedElements();
    }
    
    public boolean supportsCharacters() {
        return helper.supportsCharacters();
    }
    
}
