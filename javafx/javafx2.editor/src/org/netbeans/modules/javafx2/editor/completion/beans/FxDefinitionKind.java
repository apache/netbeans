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
package org.netbeans.modules.javafx2.editor.completion.beans;

/**
 *
 * @author sdedic
 */
public enum FxDefinitionKind {
    /**
     * Bean info
     */
    BEAN,
    
    /**
     * Event info
     */
    EVENT,
    /**
     * Regular property setter
     */
    SETTER,

    /**
     * Readonly Map. Type correspond to the Map value type.
     */
    MAP,

    /**
     * Readonly list. Type corresponds to the type of list item
     */
    LIST,

    /**
     * Readonly/immutable object, type is getter return type.
     */
    GETTER,

    /**
     * Attached property
     */
    ATTACHED;

    public boolean isWrite() {
        return this == SETTER;
    }
}
