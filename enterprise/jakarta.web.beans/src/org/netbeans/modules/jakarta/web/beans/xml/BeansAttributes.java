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
package org.netbeans.modules.jakarta.web.beans.xml;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public enum BeansAttributes implements Attribute {
    XMLNS("xmlns", String.class), //NOI18N
    VERSION("version", String.class), //NOI18N
    BEAN_DISCOVERY_MODE("bean-discovery-mode", String.class); //NOI18N

    private final String name;
    private final Class type;

    private BeansAttributes(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Class getMemberType() {
        return null;
    }
}
