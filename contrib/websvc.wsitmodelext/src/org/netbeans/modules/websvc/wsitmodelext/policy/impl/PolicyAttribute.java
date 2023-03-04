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

package org.netbeans.modules.websvc.wsitmodelext.policy.impl;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Grebac
 */
public enum PolicyAttribute implements Attribute {
        URI("URI"),
        DIGEST("Digest"),
        DIGESTALGORITHM("DigestAlgorithm");
    
    private String name;
    private Class type;
    private Class subtype;
    
    /** Creates a new instance of PolicyAttribute */
    PolicyAttribute(String name) {
        this(name, String.class);
    }
    PolicyAttribute(String name, Class type) {
        this(name, type, null);
    }
    PolicyAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    @Override
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
