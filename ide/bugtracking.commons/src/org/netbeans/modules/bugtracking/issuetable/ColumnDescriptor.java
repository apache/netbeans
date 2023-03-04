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

package org.netbeans.modules.bugtracking.issuetable;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport.ReadOnly;

/**
 *
 * @author Tomas Stupka
 */
/**
 * Describes a particular column in the queries table
 */
public class ColumnDescriptor<T> extends ReadOnly<T> {
    private int width;
    private boolean visible;
    private boolean alwaysVisible;
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
        this(name, type, displayName, shortDescription, -1); // -1 means default
    }
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription, int width) {
        this(name, type, displayName, shortDescription, width, true, false);
    }
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription, int width, boolean visible) {
        this(name, type, displayName, shortDescription, width, visible, false);
    }
    public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription, int width, boolean visible, boolean alwaysVisible) {
        super(name, type, displayName, shortDescription);
        this.width = width;
        this.visible = visible;
        this.alwaysVisible = alwaysVisible;
    }
    @Override
    public T getValue() throws IllegalAccessException, InvocationTargetException {
        return null;
    }
    public int getWidth() {
        return width;
    }

    boolean isVisible() {
        return visible;
    }

    void setVisible(boolean visible) {
        this.visible = visible;
    }

    boolean alwaysVisible() {
        return alwaysVisible;
    }

}

