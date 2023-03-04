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

package org.netbeans.modules.web.jsfapi.api;


/**
 * The attribute element defines an attribute for the nesting
 * tag.  
 * 
 * The attribute element may have several subelements defining:
 * 
 * <li>description        a description of the attribute,
 * <li>name               the name of the attribute,
 * <li>required           whether the attribute is required or optional,
 * <li>type               the type of the attribute,
 * <li>method-signature   method signature of the method expression.
 * <li>default            default value of the attribute if exists, {@code null} otherwise
 *
 * @author marekfukala
 */
public interface Attribute {

    /**
     * Name of the tags attribute
     * 
     * @return non-null name of the tag attribute.
     */
    public String getName();

    /**
     * Text description of the attribute.
     * 
     * @return a text description of the attribute or null if not defined.
     */
    public String getDescription();

    /**
     * Defines if the nesting attribute is required or optional.
     * 
     * @return true if required, false otherwise (also if not defined).
     */
    public boolean isRequired();
    
    /**
     * Defines the Java type of the attributes value.
     * If this element is omitted, the expected type is
     * assumed to be "java.lang.Object".
     * 
     * @return FQN of the type of the attribute or null if not defined.
     */
    public String getType();
    
    /**
     * Returns a method signature that is used to specify the method signature 
     * for MethodExpression attributes. 
     * 
     * @since 1.14
     * @return method signature or null if not defined.
     */
    public String getMethodSignature();

    /**
     * Returns a default value of the attribute if exists.
     *
     * @since 1.34
     * @return default value or {@code null} if not exist.
     */
    public String getDefaultValue();

    
    public static class DefaultAttribute implements Attribute {

        private String name;
        private String description;
        private String type;
        private boolean required;
        private String methodSignature;
        private String defaultValue;

        public DefaultAttribute(String name, String description, boolean required) {
            this(name, description, null, required, null);
        }

        public DefaultAttribute(String name, String description, String type, boolean required, String methodSignature) {
            this(name, description, type, required, methodSignature, null);
        }

        public DefaultAttribute(String name, String description, String type, boolean required, String methodSignature, String defaultValue) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.required = required;
            this.methodSignature = methodSignature;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getDescription() {
            return description;
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
        public boolean isRequired() {
            return required;
        }

        @Override
        public String getMethodSignature() {
            return methodSignature;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Attribute[name=" + getName() + ", required=" + isRequired() + ", defaultValue=" + getDefaultValue() + "]"; //NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DefaultAttribute other = (DefaultAttribute) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.required != other.required) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 13 * hash + (this.required ? 1 : 0);
            return hash;
        }

    }
}
