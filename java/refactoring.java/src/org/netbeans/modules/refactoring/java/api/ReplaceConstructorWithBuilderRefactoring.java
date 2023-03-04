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

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;

/**
 * Replace Constructor with Builder Refactoring.
 * This refactoring creates a new Builder class and replaces all new class 
 * expressions with builder calls.
 * <br>
 * <br>
 * For instance it replaces:
 * <pre>
 * Test t = new Test("foo", 1);
 * </pre>
 * with builder pattern
 * <pre>
 * Test t = new TestBuilder().setA("foo").setB(1).createTest();
 * </pre>
 * 
 * @author Jan Becicka
 * @since 1.36
 */
public final class ReplaceConstructorWithBuilderRefactoring extends AbstractRefactoring {

    
    private String builderName;
    private String buildMethodName;
    private List<Setter> setters;

    /**
     * Constructor accepts TreePathHandles representing constructor
     * @param constructor
     */
    public ReplaceConstructorWithBuilderRefactoring(@NonNull TreePathHandle constructor) {
        super(Lookups.singleton(constructor));
    }

    /**
     * Getter for builder name
     * @return fully qualified name of builder
     */
    public @NonNull String getBuilderName() {
        return builderName;
    }

    /**
     * Getter for build method name
     * @return name of build method
     */
    public @NonNull String getBuildMethodName() {
        return buildMethodName;
    }

    /**
     * 
     * @param builderName 
     */
    public void setBuilderName(@NonNull String builderName) {
        this.builderName = builderName;
    }
    
    /**
     * 
     * @param buildMethodName 
     */
    public void setBuildMethodName(@NonNull String buildMethodName) {
        this.buildMethodName = buildMethodName;
    }    

    /**
     * Getter for list of setters
     * @return
     */
    public @NonNull List<Setter> getSetters() {
        if (setters==null) {
            //never return null;
            return Collections.EMPTY_LIST;
        }
        return setters;
    }

    /**
     * setter for list of setters
     * @param setters
     */
    public void setSetters(@NonNull List<Setter> setters) {
        this.setters = setters;
    }

    /**
     * Setter represent one setter of Builder pattern
     */
    public static final class Setter {

        private final String name;
        private final String type;
        private final boolean optional;
        private final String defaultValue;
        private final String varName;

        /**
         * The only way how to create setter.
         * @param name the name of the setter. For instance "setA"
         * @param type the type of the setter. For instance "int"
         * @param defaultValue the default value. Might be null. For instance "1".
         * @param varName the name of the variable. For instance "a".
         * @param optional true if the setter is optional in case, that argument is the same as default value.
         */
        public Setter(
                @NonNull String name,
                @NonNull String type,
                @NullAllowed String defaultValue,
                @NonNull String varName,
                boolean optional) {
            this.name = name;
            this.type = type;
            this.optional = optional;
            this.defaultValue = defaultValue;
            this.varName = varName;
        }

        /**
         * Getter for setter name.
         * @return 
         */
        public @NonNull String getName() {
            return name;
        }

        /**
         * Getter for optional.
         * @return
         */
        public boolean isOptional() {
            return optional;
        }

        
        /**
         * Getter for type.
         * @return 
         */
        public @NonNull String getType() {
            return type;
        }

        /**
         * Getter for default value.
         * @return can return null
         */
        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * Getter for variable name.
         * @return 
         */
        public @NonNull String getVarName() {
            return varName;
        }

    }
    
}
