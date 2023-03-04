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
package org.netbeans.modules.javascript2.knockout.index;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Roman Svitanic
 */
public class KnockoutCustomElement {

    private final String name;
    private final String fqn;
    private final List<String> parameters;
    private final URL url;
    private final int offset;

    public KnockoutCustomElement(String name, String fqn, Collection<String> parameters, URL url, int offset) {
        this.name = name;
        this.fqn = fqn;
        this.url = url;
        this.offset = offset;
        this.parameters = new ArrayList<>();
        if (parameters != null && !parameters.isEmpty()) {
            this.parameters.addAll(parameters);
        }
    }

    public String getName() {
        return name;
    }

    public String getFqn() {
        return fqn;
    }

    public URL getDeclarationFile() {
        return url;
    }

    public int getOffset() {
        return offset;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
}
