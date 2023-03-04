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
 * Replaces public constructor with factory method.
 * @author lahvac
 * @author Jan Becicka
 * @since 1.36
 */
public final class ReplaceConstructorWithFactoryRefactoring extends AbstractRefactoring {
    
    private String factoryName = "create";

    /**
     * Constructor accepts only TreePathHandles representing constructor.
     * @param constructor
     */
    public ReplaceConstructorWithFactoryRefactoring(@NonNull TreePathHandle constructor) {
        super(Lookups.singleton(constructor));
    }

    /**
     * Name of created factory method.
     * @return name of the factory. Default value is "create".
     */
    public @NonNull String getFactoryName() {
        return factoryName;
    }

    /**
     * Setter for factory method
     * @param factoryName 
     */
    public void setFactoryName(@NonNull String factoryName) {
        this.factoryName = factoryName;
    }

}
