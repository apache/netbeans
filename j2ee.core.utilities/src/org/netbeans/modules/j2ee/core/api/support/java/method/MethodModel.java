/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2ee.core.api.support.java.method;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.openide.util.Parameters;

/**
 * Immutable model of method.
 * Check {@link MethodModelSupport} for additional support functionality related to this class
 * 
 * @author Martin Adamek
 */
public final class MethodModel {
    
    private final String name;
    private final String returnType;
    private final String body;
    private final List<Variable> parameters; // unmodifiable list
    private final List<String> exceptions; // unmodifiable list
    private final Set<Modifier> modifiers; // unmodifiable set
    private final List<Annotation> annotations; // unmodifiable list
    
    private MethodModel(String name, String returnType, String body, List<Variable> parameters, List<String> exceptions, Set<Modifier> modifiers, List<Annotation> annotations) {
        this.name = name;
        this.returnType = returnType;
        this.body = body;
        this.parameters = Collections.unmodifiableList(parameters);
        this.exceptions = Collections.unmodifiableList(exceptions);
        this.modifiers = Collections.unmodifiableSet(modifiers);
        this.annotations = Collections.unmodifiableList(annotations);
    }
    
    /**
     * Creates new instance of method model. None of the parameters can be null.
     * 
     * @param name name of the method, must be valid Java identifier
     * @param returnType name of return type as written in source code,
     * for non-primitive types fully-qualfied name must be used,
     * must contain at least one non-whitespace character
     * @param body string representation of body, can be null
     * @param parameters list of method parameters, can be empty
     * @param exceptions list of exceptions represented by fully-qualified names of exceptions, can be empty
     * @param modifiers list of modifiers of method, can be empty
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @throws IllegalArgumentException if the paramter returnType does not contain at least one non-whitespace character
     * or the parameter name is not a valid Java identifier
     * @return immutable model of method
     */
    public static MethodModel create(String name, String returnType, String body, List<Variable> parameters, List<String> exceptions, Set<Modifier> modifiers) {
        Parameters.javaIdentifier("name", name);
        Parameters.notWhitespace("returnType", returnType);
        Parameters.notNull("parameters", parameters);
        Parameters.notNull("exceptions", exceptions);
        Parameters.notNull("modifiers", modifiers);
        return new MethodModel(name, returnType, body, parameters, exceptions, modifiers, Collections.<Annotation>emptyList());
    }
    
    /**
     * Creates new instance of method model. None of the parameters can be null.
     *
     * @param name name of the method, must be valid Java identifier
     * @param returnType name of return type as written in source code,
     * for non-primitive types fully-qualfied name must be used,
     * must contain at least one non-whitespace character
     * @param body string representation of body, can be null
     * @param parameters list of method parameters, can be empty
     * @param exceptions list of exceptions represented by fully-qualified names of exceptions, can be empty
     * @param modifiers list of modifiers of method, can be empty
     * @param annotations list of {@code Annotations} represented by fully-qualified names of annotations, can be empty
     * @throws NullPointerException if any of the parameters is <code>null</code>.
     * @throws IllegalArgumentException if the paramter returnType does not contain at least one non-whitespace character
     * or the parameter name is not a valid Java identifier
     * @return immutable model of method
     */
    public static MethodModel create(String name, String returnType, String body, List<Variable> parameters, List<String> exceptions, Set<Modifier> modifiers,  List<Annotation> annotations) {
        Parameters.javaIdentifier("name", name);
        Parameters.notWhitespace("returnType", returnType);
        Parameters.notNull("parameters", parameters);
        Parameters.notNull("exceptions", exceptions);
        Parameters.notNull("modifiers", modifiers);
        Parameters.notNull("annotations", annotations);
        return new MethodModel(name, returnType, body, parameters, exceptions, modifiers, annotations);
    }
    
    /**
     * Immutable type representing class field or method parameter
     */
    public static final class Variable {
        
        private final String type;
        private final String name;
        private final boolean finalModifier;
        
        private Variable(String type, String name, boolean finalModifier) {
            this.type = type;
            this.name = name;
            this.finalModifier = finalModifier;
        }

        /**
         * Creates new instance of a model of class variable or method parameter
         * without final modifier. Same as calling {@link #create(String, String, boolean)}
         * with 3rd argument set to false
         * 
         * @param type name of type as written in source code
         * for non-primitive types fully-qualfied name must be used,
         * must contain at least one non-whitespace character
         * @param name name of the paramter or variable, must be valid Java identifier
         * @throws NullPointerException if any of the parameters is <code>null</code>.
         * @throws IllegalArgumentException if the paramter type does not contain at least one non-whitespace character
         * or the parameter name is not a valid Java identifier
         * @return immutable model of variable or method parameter
         */
        public static Variable create(String type, String name) {
            Parameters.notWhitespace("type", type);
            Parameters.javaIdentifier("name", name);
            return new MethodModel.Variable(type, name, false);
        }
        
        /**
         * Creates new instance of a model of class variable or method parameter
         * 
         * @param type name of type as written in source code
         * for non-primitive types fully-qualfied name must be used,
         * must contain at least one non-whitespace character
         * @param name name of the paramter or variable, must be valid Java identifier
         * @param finalModifier specifies if variable is final (if it has final modifier)
         * @throws NullPointerException if any of the parameters is <code>null</code>.
         * @throws IllegalArgumentException if the paramter type does not contain at least one non-whitespace character
         * or the parameter name is not a valid Java identifier
         * @return immutable model of variable or method parameter
         */
        public static Variable create(String type, String name, boolean finalModifier) {
            Parameters.notWhitespace("type", type);
            Parameters.javaIdentifier("name", name);
            return new MethodModel.Variable(type, name, finalModifier);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Variable other = (Variable) obj;
            if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }
        
        // <editor-fold defaultstate="collapsed" desc="Variable's getters">

        /**
         * Variable or method paramter type, for non-primitive types fully-qualified name is returned
         * 
         * @return non-null value
         */
        public String getType() {
            return type;
        }
        
        /**
         * Variable or method paramter name
         * 
         * @return non-null value
         */
        public String getName() {
            return name;
        }
        
        /**
         * Flag specifying if variable is final or not.
         * 
         * @return true if variable is final, false otherwise
         */
        public boolean getFinalModifier() {
            return finalModifier;
        }
        
    // </editor-fold>

    }

    /**
     * Immutable type representing method annotation
     */
    public static final class Annotation {

        private final String type;
        private final Map<String, Object> arguments;

        private Annotation(String type, Map<String, Object> arguments) {
            this.type = type;
            this.arguments = arguments;
        }

        /**
         * Creates new instance of a model of {@code Annotation}
         *
         * @param type name of annotation type, fully qualified name must be used
         * @throws {@code NullPointerException} if the type parameter is {@code null}
         * @throws {@code IllegalArgumentException} if the parameter type is not fully qualified
         * name of valid annotation
         * @return immutable model of {@code Annotation}
         */
        public static Annotation create(String type) {
            Parameters.notNull("type", type);    //NOI18N
            return new MethodModel.Annotation(type, null);
        }

        /**
         * Creates new instance of a model of {@code Annotation}
         *
         * @param type name of annotation type, fully qualified name must be used
         * @param arguments {@code Map<String, String>} of annotation arguments, key of the map determines
         * argument name and the maps value implies argument value
         * @throws {@code NullPointerException} if any of the parameters is {@code null}
         * @throws {@code IllegalArgumentException} if the parameter type is not fully qualified
         * name of valid annotation
         * @return immutable model of {@code Annotation}
         */
        public static Annotation create(String type, Map<String, String> arguments) {
            Parameters.notNull("type", type);    //NOI18N
            Parameters.notNull("arguments", arguments); //NOI18N
            return new MethodModel.Annotation(type, new HashMap<String, Object>(arguments));
        }

        // <editor-fold defaultstate="collapsed" desc="Annotation's getters">

        public String getType() {
            return type;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }

        // </editor-fold>
    }

    // <editor-fold defaultstate="collapsed" desc="MethodModel's getters">
    
    /**
     * Method name
     * 
     * @return non-null value
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return type, for non-primitive types fully-qualified name is returned
     * 
     * @return non-null value
     */
    public String getReturnType() {
        return returnType;
    }
    
    /**
     * String representation of method body
     * 
     * @return non-null value
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Unmodifiable list of method parameters. Attempts to modify the returned list, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public List<Variable> getParameters() {
        return parameters;
    }
    
    /**
     * Unmodifiable list of method exceptions. Attempts to modify the returned list, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public List<String> getExceptions() {
        return exceptions;
    }
    
    /**
     * Unmodifiable set of method modifiers. Attempts to modify the returned set, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     * 
     * @return non-null value
     */
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    /**
     * Unmodifiable list of annotations. Attempts to modify the returned set, whether
     * direct or via its iterator, result in an <tt>UnsupportedOperationException</tt>.
     *
     * @return non-null value
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }
    
    // </editor-fold>
    
    @Override
    public String toString() {
        return "MethodModel<" + modifiers + "," + returnType + "," + name + "," + parameters + "," + exceptions + ",{" + body + "}>";
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodModel other = (MethodModel) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 13 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        return hash;
    }
    
}
