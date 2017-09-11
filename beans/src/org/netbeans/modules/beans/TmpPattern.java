/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.beans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;

import static org.netbeans.modules.beans.BeanUtils.*;

/** Contains classes good for analysis time. These objects have to be only
 * inside the CancellableTask()
 *
 * @author phrebejk
 */
public class TmpPattern {

   
    public static class Property {
        
        ExecutableElement getterMethod;
        ExecutableElement setterMethod;
        VariableElement estimatedField;
        TypeMirror type;        
        String name;
        
        public Property( CompilationInfo javac, ExecutableElement getterMethod,
                         ExecutableElement setterMethod )
            throws IntrospectionException {

            this.getterMethod = getterMethod;
            this.setterMethod = setterMethod;

            type = findPropertyType(javac);
            name = findPropertyName();
        }
        
        /** Package private constructor. Merges two property descriptors. Where they
        * conflict, gives the second argument (y) priority over the first argumnet (x).
        * @param x The first (lower priority) PropertyPattern.
        * @param y The second (higher priority) PropertyPattern.
        */
        Property( CompilationInfo javac, Property x, Property y ) {
            
            // Figure out the merged getterMethod
            ExecutableElement xr = x.getterMethod;
            ExecutableElement yr = y.getterMethod;
            getterMethod = xr;

            // Normaly give priority to y's getterMethod
            if ( yr != null ) {
                getterMethod = yr;
            }

            // However, if both x and y reference read method in the same class,
            // give priority to a boolean "is" method over boolean "get" method. 
            if ( xr != null && yr != null &&
                 xr.getEnclosingElement() == yr.getEnclosingElement() &&
                 xr.getReturnType().getKind() == TypeKind.BOOLEAN &&
                 yr.getReturnType().getKind() == TypeKind.BOOLEAN &&
                 nameAsString(xr).indexOf(IS_PREFIX) == 0 &&    
                 nameAsString(yr).indexOf(GET_PREFIX) == 0 ) { 
                getterMethod = xr;
            }

            setterMethod = x.setterMethod;
            if ( y.setterMethod != null ) {
                setterMethod = y.setterMethod;
            }

            // PENDING bound and constrained
            /*
            bound = x.bound | y.bound;
            constrained = x.constrained | y.constrained
            */

            try {
                type = findPropertyType(javac);
            }
            catch ( IntrospectionException ex ) {
                //System.out.println (x.getName() + ":" +  y.getName()); // NOI18N
                //System.out.println (x.getType() + ":" + y.getType() ); // NOI18N
                throw new IllegalStateException("Mixing invalid PropertyPattrens", ex); // NOI18N
            }

            name = findPropertyName();
        }
        
        public PropertyPattern createPattern( PatternAnalyser analyser ) throws IntrospectionException {
            return new PropertyPattern( analyser, getterMethod, setterMethod, estimatedField, type, name );
        }
        
        // Private methods -----------------------------------------------------
        
        /** Resolves the type of the property from type of getter and setter.
         * @throws IntrospectionException if the property doesnt folow the design patterns
         * @return The type of the property.
         */
        TypeMirror findPropertyType(CompilationInfo javac) throws IntrospectionException {

            TypeMirror resolvedType = null;

            if ( getterMethod != null ) {
                if ( !getterMethod.getParameters().isEmpty() ) {
                    throw new IntrospectionException( "bad read method arg count" ); // NOI18N
                }
                resolvedType = getterMethod.getReturnType();
                if ( resolvedType.getKind() == TypeKind.VOID ) {                
                    throw new IntrospectionException( "read method " + getterMethod.getSimpleName() + // NOI18N
                                                      " returns void" ); // NOI18N
                }
            }
            
            if ( setterMethod != null ) {
                List<? extends VariableElement> params = setterMethod.getParameters();
                if ( params.size() != 1 ) {
                    throw new IntrospectionException( "bad write method arg count" ); // NOI18N
                }
                VariableElement param = params.get(0);
                if ( resolvedType != null && !javac.getTypes().isSameType(resolvedType, param.asType()) ) {
                    throw new IntrospectionException( "type mismatch between read and write methods" ); // NOI18N
                }
                resolvedType = param.asType();
            }
            return resolvedType;
        }

        /** Based on names of getter and setter resolves the name of the property.
         * @return Name of the property
         */
        String findPropertyName() {
            String methodName = null;

            if ( getterMethod != null )
                methodName = nameAsString(getterMethod);
            else if ( setterMethod != null )
                methodName = nameAsString(setterMethod);
            else {
                return null;
            }

            return  methodName.startsWith( IS_PREFIX ) ? // NOI18N
                    Introspector.decapitalize( methodName.substring(2) ) :
                    Introspector.decapitalize( methodName.substring(3) );
        }
       
    }
    
    public static class IdxProperty extends Property {
        
        private ExecutableElement indexedGetterMethod;
        private ExecutableElement indexedSetterMethod;
        private TypeMirror indexedType;
        
        /** Creates new IndexedProperty just one of the methods indexedGetterMethod
         * and indexedSetterMethod may be null. 
         * @param patternAnalyser patternAnalyser which creates this Property.
         * @param getterMethod getterMethod may be <CODE>null</CODE>.
         * @param setterMethod setterMethod may be <CODE>null</CODE>.
         * @param indexedGetterMethod getterMethod of the property or <CODE>null</CODE>.
         * @param indexedSetterMethod setterMethod of the property or <CODE>null</CODE>.
         * @throws IntrospectionException If specified methods do not follow beans Property rules.
         */  
        public IdxProperty( CompilationInfo ci,
                            ExecutableElement getterMethod, ExecutableElement setterMethod,
                            ExecutableElement indexedGetterMethod, ExecutableElement indexedSetterMethod )
        throws IntrospectionException {

            super ( ci, getterMethod, setterMethod );

            this.indexedGetterMethod = indexedGetterMethod;
            this.indexedSetterMethod = indexedSetterMethod;

            indexedType = findIndexedPropertyType(ci);
            
            if (this.type == null && this.indexedType != null) {
                this.type = ci.getTypes().getArrayType(this.indexedType);
            }
            
            name = findIndexedPropertyName();
        }
        
         /** Package private constructor. Merges two property descriptors. Where they
         * conflict, gives the second argument (y) priority over the first argumnet (x).
         * @param x The first (lower priority) PropertyPattern.
         * @param y The second (higher priority) PropertyPattern.
         */
        IdxProperty( CompilationInfo javac, Property x, Property y )  {
            super(javac, x, y);
            if ( x instanceof IdxProperty ) {
                IdxProperty ix = (IdxProperty)x;
                indexedGetterMethod = ix.indexedGetterMethod;
                indexedSetterMethod = ix.indexedSetterMethod;
                indexedType = ix.indexedType;
                type = type == null? ix.type: type;
            }
            if ( y instanceof IdxProperty ) {
                IdxProperty iy = (IdxProperty)y;
                if ( iy.indexedGetterMethod != null )
                    indexedGetterMethod = iy.indexedGetterMethod;
                if ( iy.indexedSetterMethod != null )
                    indexedSetterMethod = iy.indexedSetterMethod;
                indexedType = iy.indexedType;
                type = type == null? iy.type: type;
            }
            name  = findIndexedPropertyName();
        }

        
        public IdxPropertyPattern createPattern( PatternAnalyser analyser) throws IntrospectionException {
            return new IdxPropertyPattern(analyser,
                                          getterMethod, setterMethod, 
                                          indexedGetterMethod, indexedSetterMethod,
                                          estimatedField,
                                          type,
                                          indexedType,
                                          name);
        }
        
        
        // Private methods -----------------------------------------------------
        
        /** Resolves the indexed type of the property from type of getter and setter.
         * Checks for conformance to Beans design patterns.
         * @throws IntrospectionException if the property does not follow the design patterns
         */
        private TypeMirror findIndexedPropertyType(CompilationInfo javac) throws IntrospectionException {

            indexedType = null;

            if ( indexedGetterMethod != null ) {
                List<? extends VariableElement> params = indexedGetterMethod.getParameters();
                if ( params.size() != 1 ) {
                    throw new IntrospectionException( "bad indexed read method arg count" ); // NOI18N
                }
                VariableElement param = params.get(0);
                if ( param.asType().getKind() != TypeKind.INT ) {
                    throw new IntrospectionException( "not int index to indexed read method" ); // NOI18N
                }
                indexedType = indexedGetterMethod.getReturnType();
                if ( indexedType.getKind() ==TypeKind.VOID ) {
                    throw new IntrospectionException( "indexed read method return void" ); // NOI18N
                }
            }

            if (indexedSetterMethod != null ) {
                List<? extends VariableElement> params = indexedSetterMethod.getParameters();
                if ( params.size() != 2 ) {
                    throw new IntrospectionException( "bad indexed write method arg count" ); // NOI18N
                }
                VariableElement param1 = params.get(0);
                if ( param1.asType().getKind() != TypeKind.INT ) {
                    throw new IntrospectionException( "non int index to indexed write method" ); // NOI18N
                }
                VariableElement param2 = params.get(1);
                if (indexedType != null && !javac.getTypes().isSameType(indexedType, param2.asType()) ) {
                    throw new IntrospectionException(
                        "type mismatch between indexed read and write methods" ); // NOI18N
                }
                indexedType = param2.asType();
            }

            //type = indexedType;

            TypeMirror propType = type;
            if ( propType != null &&
                ( (propType.getKind() != TypeKind.ARRAY) || !javac.getTypes().isSameType(indexedType, ((ArrayType)propType).getComponentType()))) {
                throw new IntrospectionException(
                    "type mismatch between property type and indexed type" ); // NOI18N
            }
            return indexedType;
        }

        /** Based on names of indexedGetter and indexedSetter resolves the name
         * of the indexed property.
         * @return Name of the indexed property
         */ 
        String findIndexedPropertyName() {
            String superName = findPropertyName();

            if ( superName == null ) {
                String methodName = null;

                if ( indexedGetterMethod != null )
                    methodName = nameAsString(indexedGetterMethod);
                else if ( indexedSetterMethod != null )
                    methodName = nameAsString(indexedSetterMethod);
                else
                    throw new InternalError( "Indexed property with all methods == null" ); // NOI18N

                return methodName.startsWith( IS_PREFIX ) ? // NOI18N
                       Introspector.decapitalize( methodName.substring(2) ) :
                       Introspector.decapitalize( methodName.substring(3) );
            }
            else
                return superName;
        }

    }
    
    public static class EventSet {
        
        ExecutableElement addListenerMethod;
        ExecutableElement removeListenerMethod;
        boolean isUnicast;
        TypeMirror type;        
        String name;
        
        public EventSet( CompilationInfo ci,
                         ExecutableElement addListenerMethod, 
                         ExecutableElement removeListenerMethod ) {
            
            if ( addListenerMethod == null || removeListenerMethod == null  )
                throw new NullPointerException();

            this.addListenerMethod = addListenerMethod;
            this.removeListenerMethod = removeListenerMethod;

            isUnicast = testUnicast( ci );
            type = findEventSetType();
            name = findEventSetName();
        }
        
        /*
        * Package-private constructor
        * Merge two event set descriptors.  Where they conflict, give the
        * second argument (y) priority over the first argument (x).
        *
        * @param x  The first (lower priority) EventSetDescriptor
        * @param y  The second (higher priority) EventSetDescriptor
        */
        EventSet( EventSet x, EventSet y) {
            //super(x,y);

            /*
            listenerMethodDescriptors = x.listenerMethodDescriptors;
            if (y.listenerMethodDescriptors != null) {
             listenerMethodDescriptors = y.listenerMethodDescriptors;
        }
            if (listenerMethodDescriptors == null) {
             listenerMethods = y.listenerMethods;
        }
            */
            addListenerMethod = y.addListenerMethod;
            removeListenerMethod = y.removeListenerMethod;
            isUnicast = y.isUnicast;
            type = y.type;
            name = y.name;

            /*
            if (!x.inDefaultEventSet || !y.inDefaultEventSet) {
             inDefaultEventSet = false;
        }
            */
        }
        
        EventSetPattern createPattern( PatternAnalyser analyser) {
            return new EventSetPattern( analyser, addListenerMethod, removeListenerMethod, name, type, isUnicast);
        }

        // Private methods -----------------------------------------------------
        
        /** Finds the Type of property.*/
        private TypeMirror findEventSetType() {
            return addListenerMethod.getParameters().get(0).asType();
        }

        /** Decides about the name of the event set from names of the methods */
        private String findEventSetName() {
            String compound = nameAsString(addListenerMethod).substring(3);
            return name = Introspector.decapitalize( compound );
        }

        /** Test if this EventSet pattern is unicast */
        private boolean testUnicast( CompilationInfo ci ) {
            return BeanUtils.isThrowing(ci, addListenerMethod, "java.util.TooManyListenersException"); // NOI18N
        }
    
    }
    
    
    
}

