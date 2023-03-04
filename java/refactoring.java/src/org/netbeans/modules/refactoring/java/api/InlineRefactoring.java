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
package org.netbeans.modules.refactoring.java.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used to replace all references of an element with its body
 * or expression.
 * 
 * @since 1.22.0
 * 
 * @author Ralph Ruijs
 */
public final class InlineRefactoring extends AbstractRefactoring {
    
    public static enum Type {
        METHOD,
        TEMP,
        CONSTANT,
        UNSUPPORTED
    }
    
    private Type type;

    /**
     * Creates a new instance of InlineRefactoring.
     * @param selectedElement, element to inline.
     */
    public InlineRefactoring(@NonNull TreePathHandle selectedElement, Type inlineType) {
        super(Lookups.fixed(selectedElement));
        this.type = inlineType;
    }

    public Type getType() {
        return type;
    }
}
