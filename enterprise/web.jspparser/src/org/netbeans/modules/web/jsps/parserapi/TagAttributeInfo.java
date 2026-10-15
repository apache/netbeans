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
package org.netbeans.modules.web.jsps.parserapi;

public class TagAttributeInfo {
    private String name;
    private String typeName;
    private boolean required;
    private boolean fragment;
    private boolean canBeRequestTime;

    public TagAttributeInfo() {
    }

    public TagAttributeInfo(String name, boolean required, String typeName, boolean canBeRequestTime) {
        this(name, required, typeName, canBeRequestTime, false);
    }

    public TagAttributeInfo(String name, boolean required, String typeName, boolean canBeRequestTime, boolean fragment) {
        this.name = name;
        this.typeName = typeName;
        this.required = required;
        this.canBeRequestTime = canBeRequestTime;
        this.fragment = fragment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isFragment() {
        return fragment;
    }

    public void setFragment(boolean fragment) {
        this.fragment = fragment;
    }

    public boolean isCanBeRequestTime() {
        return canBeRequestTime;
    }

    public void setCanBeRequestTime(boolean canBeRequestTime) {
        this.canBeRequestTime = canBeRequestTime;
    }

}
