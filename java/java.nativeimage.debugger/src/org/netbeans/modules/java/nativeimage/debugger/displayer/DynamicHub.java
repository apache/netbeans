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
package org.netbeans.modules.java.nativeimage.debugger.displayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.nativeimage.api.debug.NIVariable;

import static org.netbeans.modules.java.nativeimage.debugger.displayer.JavaVariablesDisplayer.PRIVATE;
import static org.netbeans.modules.java.nativeimage.debugger.displayer.JavaVariablesDisplayer.PUBLIC;
import static org.netbeans.modules.java.nativeimage.debugger.displayer.Utils.findChild;
import static org.netbeans.modules.java.nativeimage.debugger.displayer.Utils.getVarsByName;

/**
 * Hub of the native image object.
 */
final class DynamicHub {

    private static final String HUB = "hub";                 // NOI18N
    private static final String HUB_TYPE = "hubType";        // NOI18N
    private static final int HUB_TYPE_INSTANCE = 2; // Instances are less than or equal to 2
    private static final int HUB_TYPE_ARRAY = 4; // Arrays are greater than or equal to 4
    private static final String OBJ_HEADER = "_objhdr";      // NOI18N
    private static final String NAME = "name";               // NOI18N

    private final NIVariable hub;
    private final Map<String, NIVariable> childrenByName;

    enum HubType {

        OBJECT,
        ARRAY;

        static HubType getFrom(int hubType) {
            if (hubType <= HUB_TYPE_INSTANCE) {
                return OBJECT;
            }
            if (hubType >= HUB_TYPE_ARRAY) {
                return ARRAY;
            }
            return null;
        }
    }

    private DynamicHub(NIVariable hub) {
        this.hub = hub;
        this.childrenByName = getVarsByName(hub.getChildren());
    }

    @CheckForNull
    static DynamicHub find(NIVariable var) {
        NIVariable object = findObjectType(var);
        if (object == null) {
            return null;
        }
        NIVariable[] children = object.getChildren();
        NIVariable hub = findChild(children, OBJ_HEADER, PUBLIC, HUB, Class.class.getName(), PRIVATE);
        if (hub != null) {
            return new DynamicHub(hub);
        } else {
            return null;
        }
    }

    @CheckForNull
    private static NIVariable findObjectType(NIVariable var) {
        NIVariable[] children = var.getChildren();
        if (children.length < 1) {
            return null;
        }
        if (children[0].getName().equals(Object.class.getName())) {
            return children[0];
        }
        // Prevent from infinite recursion
        Set<String> visitedNames = new HashSet<>();
        var = children[0];
        do {
            children = var.getChildren();
            if (children.length < 1) {
                return null;
            }
            var = children[0];
            if (var.getName().equals(Object.class.getName())) {
                return var;
            }
        } while (visitedNames.add(var.getName()));
        return null;
    }

    @CheckForNull
    HubType getType() {
        NIVariable hubTypeVar = childrenByName.get(HUB_TYPE);
        if (hubTypeVar == null) {
            return null;
        }
        try {
            int hubType = Integer.parseInt(hubTypeVar.getValue());
            return HubType.getFrom(hubType);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @CheckForNull
    NIVariable findClassNameVar() {
        return childrenByName.get(NAME);
    }
}
