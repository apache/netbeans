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

public class TagVariableInfo {
    private String nameGiven;
    private String nameFromAttribute;
    private String className;
    private boolean declare;
    private int scope;

    public TagVariableInfo() {
    }

    public TagVariableInfo(String nameGiven, String nameFromAttribute, String className, boolean declare, int scope) {
        this.nameGiven = nameGiven;
        this.nameFromAttribute = nameFromAttribute;
        this.className = className;
        this.declare = declare;
        this.scope = scope;
    }

    public String getNameGiven() {
        return nameGiven;
    }

    public void setNameGiven(String nameGiven) {
        this.nameGiven = nameGiven;
    }

    public String getNameFromAttribute() {
        return nameFromAttribute;
    }

    public void setNameFromAttribute(String nameFromAttribute) {
        this.nameFromAttribute = nameFromAttribute;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isDeclare() {
        return declare;
    }

    public void setDeclare(boolean declare) {
        this.declare = declare;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

}
