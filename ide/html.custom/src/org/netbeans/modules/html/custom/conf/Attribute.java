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
package org.netbeans.modules.html.custom.conf;

/**
 *
 * @author marek
 */
public class Attribute extends Element {

    private final String type;

    public Attribute(String name) {
        this(name, null, null, null, null, null, false);
    }

    public Attribute(String name, String type, String description, String documentation, String documentationURL, Tag parent, boolean required, String... contexts) {
        super(name, description, documentation, documentationURL, parent, required, contexts);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Attribute[");
        sb.append(super.toString());
        sb.append(',');
        sb.append("type=");
        sb.append(type);
        sb.append("]");

        return sb.toString();
    }
}
