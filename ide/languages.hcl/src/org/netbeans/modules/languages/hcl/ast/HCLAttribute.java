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
package org.netbeans.modules.languages.hcl.ast;


/**
 *
 * @author Laszlo Kishalmi
 */
public final class HCLAttribute extends HCLAddressableElement {

    final HCLIdentifier name;
    final HCLExpression value;
    final int group;

    public HCLAttribute(HCLContainer parent, HCLIdentifier name, HCLExpression value, int group) {
        super(parent);
        this.name = name;
        this.value = value;
        this.group = group;
    }
 
    @Override
    public String id() {
        return name.id();
    }

    public HCLIdentifier getName() {
        return name;
    }

    public HCLExpression getValue() {
        return value;
    }

    public int getGroup() {
        return group;
    }

    @Override
    public void accept(Visitor v) {
        if (!v.visit(this) && (value != null)) {
            value.accept(v);
        }
    }
    
}
