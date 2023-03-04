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
package org.netbeans.modules.java.nativeimage.debugger;

import org.netbeans.modules.nativeimage.api.debug.NIFrame;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;

public class TestNIVariable implements NIVariable {

    private final String name;
    private final String type;
    private final String value;
    private final NIVariable parent;
    private final NIVariable[] children;
    private final NIFrame frame;

    TestNIVariable(String name, String type, String value, NIVariable parent, NIVariable[] children, NIFrame frame) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.parent = parent;
        this.children = children;
        this.frame = frame;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public NIVariable getParent() {
        return parent;
    }

    @Override
    public int getNumChildren() {
        return children.length;
    }

    @Override
    public NIVariable[] getChildren(int from, int to) {
        return children;
    }

    @Override
    public NIFrame getFrame() {
        return frame;
    }

    @Override
    public String getExpressionPath() {
        return "";
    }

}
