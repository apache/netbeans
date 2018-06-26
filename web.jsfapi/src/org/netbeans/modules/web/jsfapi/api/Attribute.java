/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
