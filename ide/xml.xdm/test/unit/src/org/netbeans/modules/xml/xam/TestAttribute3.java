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

package org.netbeans.modules.xml.xam;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Nam Nguyen
 */
public enum TestAttribute3 implements Attribute {
    INDEX("index", Integer.class), 
    VALUE("value", String.class),
    TNS("targetNamespace", String.class),
    NAME("name", String.class),
    REF("ref", String.class);

    private String name;
    private Class type;
    private Class subtype;
    
    TestAttribute3(String name, Class type) {
        this.name = name;
        this.type = type;
    }
    TestAttribute3(String name, Class type, Class subtype) {
        this(name, type);
        this.subtype = subtype;
    }

    @Override
    public String getName() { return name; }

    @Override
    public Class getType() { return type; }

    @Override
    public Class getMemberType() { return subtype; }

    @Override
    public String toString() { return name; }
}
