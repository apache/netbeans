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

package org.netbeans.modules.beans;

import java.lang.reflect.Modifier;
import java.beans.Introspector;
import java.text.MessageFormat;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;


/** Singleton with static methods for generating bodies of and
 * additional elements for bean patterns.
 * @author Petr Hrebejk
 */
final class BeanPatternGenerator {
//    private static final String THIS_QUALIFIER = "this."; // NOI18N
//    /** Constant for one Tab */
//    private static final String TAB = "  "; // NOI18N
//    /** Constant for two Tabs */
//    private static final String TABx2 = TAB + TAB;
//    /** Constant for three Tabs */
//    private static final String TABx3 = TABx2 + TAB;
//
//    /**
//     * Helper method; creates a suitable string for referencing an instance field.
//     * @param base the base name to create the string from
//     * @param adjustName 
//     */
//    static String createFieldName(String base, boolean adjustName, boolean paramClash) {
//        if (!adjustName) {
//            if (!paramClash)
//                return base;
//            else
//                return new StringBuffer(THIS_QUALIFIER).append(base).toString();
//        } else {
//            String propertyStyle = PropertyActionSettings.getDefault().getPropStyle();
//            return  new StringBuffer(propertyStyle).append(base).toString();
//        }
//    }
//
//    /** Generates the body of the setter method of Property.
//     * @param name Name of the property
//     * @param type Type of the property
//     * @param bound Is the property bound?
//     * @param constrained Is the property constrained?
//     * @param withSet Should be the set command of property private field generated.
//     * @param withSupport Generate the firing of (Veto|Property)Change Events?
//     * @param supportName Name of field containing <CODE>PropertyChangeListeners</CODE>.
//     * @param vetoSupportName Name of field containing <CODE>VetoableChangeListeners</CODE>.
//     * @return Sring containing the body of the setter method.
//     */
//    static String propertySetterBody(String name, Type type,
//                                     boolean bound, boolean constrained,
//                                     boolean withSet, boolean withSupport,
//                                     String supportName, String vetoSupportName) throws JmiException {
//         return propertySetterBody(name, type, bound, constrained, withSet, withSupport, supportName,
//            vetoSupportName, true);
//    }
//    
//    static String propertySetterBody(String name, Type type,
//                                     boolean bound, boolean constrained,
//                                     boolean withSet, boolean withSupport,
//                                     String supportName, String vetoSupportName, boolean adjustName) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        StringBuffer setterBody = new StringBuffer( 200 );
//        String decoratedName = createFieldName(name, adjustName, true);
//        
//        setterBody.append( "\n" ); // NOI18N
//        if ( withSupport ) {
//            /* Generates body in the form:
//               PropType oldPropName = this.propName;
//               this.propName = propName;
//               changes.firePropertyChange(propName, oldPropName, propName );
//            */
//
//            setterBody.append( TAB + type.getName() );
//            setterBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//            setterBody.append( " = " ).append( decoratedName ).append( ";\n"); // NOI18N
//
//            if ( constrained ) {
//                setterBody.append( TAB + vetoSupportName ).append( ".fireVetoableChange(\"").append( name ).append( "\", " ); // NOI18N
//
//                if ( type instanceof PrimitiveType) {
//                    setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" ); // NOI18N
//                    setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//                    setterBody.append( "), " ); // NOI18N
//                    setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" ); // NOI18N
//                    setterBody.append( name ).append( "));\n" ); // NOI18N
//                }
//                else {
//                    setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//                    setterBody.append( ", " ).append( name ).append( ");\n" ); // NOI18N
//                }
//                if ( !bound ) {
//                    setterBody.append( TAB ).append( decoratedName ); // NOI18N
//                    setterBody.append( " = " ).append( name ).append( ";\n"); // NOI18N
//                }
//            }
//            if ( bound ) {
//                setterBody.append( TAB ).append( decoratedName ); // NOI18N
//                setterBody.append( " = " ).append( name ).append( ";\n"); // NOI18N
//                setterBody.append( TAB + supportName ).append( ".firePropertyChange (\"").append( name ).append( "\", " ); // NOI18N
//
//                if ( type instanceof PrimitiveType) {
//                    setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" ); // NOI18N
//                    setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//                    setterBody.append( "), " ); // NOI18N
//                    setterBody.append( "new ").append( getWrapperClassName( type )).append( " (" ); // NOI18N
//                    setterBody.append( name ).append( "));\n" ); // NOI18N
//                }
//                else {
//                    setterBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//                    setterBody.append( ", " ).append( name ).append( ");\n" ); // NOI18N
//                }
//            }
//        }
//        else if ( withSet ) {
//            /* Generates body in the form:
//               this.propName = propName;
//             */
//            setterBody.append( TAB ).append( decoratedName ); // NOI18N
//            setterBody.append( " = " ).append( name ).append( ";\n" ); // NOI18N
//        }
//        return setterBody.toString();
//    }
//
//    /** Generates the body of the setter method of IndexedProperty.
//     * @param name Name of the property
//     * @param indexedType Indexed type of the property
//     * @param bound Is the property bound?
//     * @param constrained Is the property constrained?
//     * @param withSet Should be the set command of property private field generated.
//     * @param withSupport Generate the firing of (Veto|Property)Change Events?
//     * @param supportName Name of field containing <CODE>PropertyChangeListeners</CODE>.
//     * @param vetoSupportName Name of field containing <CODE>VetoableChangeListeners</CODE>.
//     * @return Sring containing the body of the setter method.
//     */
//    static String idxPropertySetterBody( String name, Type indexedType,
//                                         boolean bound, boolean constrained,
//                                         boolean withSet, boolean withSupport,
//                                         String supportName,
//                                         String vetoSupportName ) throws JmiException {
//        return idxPropertySetterBody(name, indexedType, bound, constrained,
//            withSet, withSupport, supportName, vetoSupportName, true);
//    }
//    
//    static String idxPropertySetterBody( String name, Type indexedType,
//                                         boolean bound, boolean constrained,
//                                         boolean withSet, boolean withSupport,
//                                         String supportName,
//                                         String vetoSupportName, boolean adjustName ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        StringBuffer setterBody = new StringBuffer( 200 );
//        String decoratedName = createFieldName(name, adjustName, true);
//        setterBody.append( "\n" ); // NOI18N
//
//        if ( withSupport && constrained ) {
//
//            setterBody.append( TAB + indexedType.getName() );
//            setterBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
//            setterBody.append( " = " ).append( decoratedName ); // NOI18N
//            setterBody.append( "[index];\n"); // NOI18N
//        }
//
//        if ( withSet || withSupport ) {
//            /* Generates body in the form:
//               this.propName = propName;
//            */
//            setterBody.append( TAB ).append( decoratedName ); // NOI18N
//            setterBody.append( "[index] = " ).append( name ).append( ";\n" ); // NOI18N
//        }
//
//        if ( withSupport && constrained ) {
//            setterBody.append( TAB + "try {\n" ); // NOI18N
//            setterBody.append( TABx2 + vetoSupportName ).append( ".fireVetoableChange (\"").append( name ).append( "\", " ); // NOI18N
//            setterBody.append( "null, null );\n" ); // NOI18N
//            setterBody.append( TAB + "}\n" ); // NOI18N
//            setterBody.append( TAB + "catch(java.beans.PropertyVetoException vetoException ) {\n" ); //NOI18N
//            setterBody.append( TABx2 ).append( decoratedName ); // NOI18N
//            setterBody.append( "[index] = old").append( Pattern.capitalizeFirstLetter( name ) ).append( ";\n" ) ; // NOI18N
//            setterBody.append( TABx2 + "throw vetoException;\n" ); //NOI18N
//            setterBody.append( TAB  + "}\n" ); //NOI18N
//        }
//
//        if ( withSupport && bound ) {
//            setterBody.append( TAB + supportName ).append( ".firePropertyChange (\"").append( name ).append( "\", " ); // NOI18N
//            setterBody.append( "null, null );\n" ); // NOI18N
//        }
//
//        return setterBody.toString();
//    }
//    
//    static String propertyGetterBody( String name, boolean withReturn ) {
//        return propertyGetterBody(name, withReturn, true);
//    }
//
//    /** Generates the body of the getter method of Property.
//     * @param name Name of the property.
//     * @param withReturn Should be the return command with property private field generated?
//     * @return Sring containing the body of the getter method.
//     */
//    static String propertyGetterBody( String name, boolean withReturn, boolean adjustName ) {
//        StringBuffer getterBody = new StringBuffer( 50 );
//        String decorated = createFieldName(name, adjustName, false);
//        getterBody.append( "\n"); // NOI18N
//        if ( withReturn ) {
//            /* Generates body in the form:
//               return propName;
//             */
//            getterBody.append( TAB + "return " ); // NOI18N
//            getterBody.append( decorated ).append( ";\n" ); // NOI18N
//        }
//        return getterBody.toString();
//    }
//    
//    /** Generates the body of the getter method of IndexedProperty.
//     * @param name Name of the property.
//     * @param withReturn Should be the return command with property private field generated?
//     * @return Sring containing the body of the getter method.
//     */
//    static String idxPropertyGetterBody( String name, boolean withReturn ) {
//        return idxPropertyGetterBody(name, withReturn, true);
//    }
//
//    static String idxPropertyGetterBody( String name, boolean withReturn, boolean adjustName ) {
//        StringBuffer getterBody = new StringBuffer( 50 );
//        String decorated = createFieldName(name, adjustName, false);
//        getterBody.append( "\n"); // NOI18N
//        if ( withReturn ) {
//            /* Generates body in the form:
//               return propName;
//             */
//            getterBody.append( TAB + "return " ); // NOI18N
//            getterBody.append( decorated ).append( "[index];\n" ); // NOI18N
//        }
//        return getterBody.toString();
//    }
//
//
//    /** Gets the <CODE>PropertyChangeSupport</CODE> field in Class. Tryes to find
//     * a field of type <CODE>PropertyChangeSupport</CODE>. If such field doesn't
//     * exist creates a new one with name <CODE>propertyChangeSupport</CODE>.
//     * @param ce Class to operate on.
//     * @throws JmiException If the modification of the source is impossible.
//     * @return Name of foun or newly created <CODE>PropertyChangeSupport</CODE> field.
//     */
//    static String supportField(JavaClass ce) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String supportName = null;
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ce);
//        Type pcsType = jmodel.getType().resolve("java.beans.PropertyChangeSupport"); // NOI18N
//        List/*<Field>*/ fields = JMIUtils.getFields(ce);
//        
//        for (Iterator it = fields.iterator(); it.hasNext();) {
//            Field field = (Field) it.next();
//            if (JMIUtils.equalTypes(pcsType, field.getType())) {
//                supportName = field.getName();
//                break;
//            }
//        }
//
//        if ( supportName == null ) { // Field not found we create new
//            supportName = findFreeFieldName(ce, "propertyChangeSupport"); // NOI18N
//            Field supportField = jmodel.getField().createField();
//            supportField.setName(supportName);
//            supportField.setType(pcsType);
//            supportField.setModifiers( Modifier.PRIVATE );
//            supportField.setInitialValueText( " new java.beans.PropertyChangeSupport(this)" ); // NOI18N
//            supportField.setJavadocText( PatternNode.getString( "COMMENT_PropertyChangeSupport" ) );
//            ce.getFeatures().add( supportField );
//        }
//
//        return supportName;
//    }
//
//    /** Gets the <CODE>VetoableChangeSupport</CODE> field in Class. Tryes to find
//     * a field of type <CODE>VetoableChangeSupport</CODE>. If such field doesn't
//     * exist creates a new one with name <CODE>vetoableChangeSupport</CODE>.
//     * @param ce Class to operate on.
//     * @throws JmiException If the modification of the source is impossible.
//     * @return Name of foun or newly created <CODE>vetoableChangeSupport</CODE> field.
//     */  
//    static String vetoSupportField( JavaClass ce ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String vetoSupportName = null;
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ce);
//        Type vcsType = jmodel.getType().resolve("java.beans.VetoableChangeSupport"); // NOI18N
//        List/*<Field>*/ fields = JMIUtils.getFields(ce);
//        
//        for (Iterator it = fields.iterator(); it.hasNext();) {      // Try to find suitable field
//            Field field = (Field) it.next();
//            if (JMIUtils.equalTypes(vcsType, field.getType())) {
//                vetoSupportName = field.getName();
//                break;
//            }
//        }
//
//        if ( vetoSupportName == null ) { // Field not found we create new
//            vetoSupportName = findFreeFieldName(ce, "vetoableChangeSupport"); // NOI18N
//            Field supportField = jmodel.getField().createField();
//            supportField.setName( vetoSupportName );
//            supportField.setType( vcsType );
//            supportField.setModifiers( Modifier.PRIVATE );
//            supportField.setInitialValueText( " new java.beans.VetoableChangeSupport(this)" ); // NOI18N
//            supportField.setJavadocText( PatternNode.getString( "COMMENT_VetoableChangeSupport" ) );
//            ce.getFeatures().add( supportField );
//        }
//
//        return vetoSupportName;
//    }
//    
//    private static String findFreeFieldName(JavaClass ce, String defName) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String name = defName;
//        for (int i = 1; ce.getField(name, true) != null; i++) {
//            name = defName + '_' + i;
//        }
//        return name;
//    }
//
//    /** If in the class don't exists methods for adding/removing PropertyChangeListeners
//     * for given field adds them.
//     * @param classElement Class to operate on.
//     * @param supportName The <CODE>PropertyChangeSupport</CODE> field the methods will be generated for.
//     * @throws JmiException If the modification of the source is impossible.
//     */
//    static void supportListenerMethods( JavaClass classElement, String supportName ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String addMethodId = "addPropertyChangeListener"; // NOI18N
//        Method addMethod = null;
//        String removeMethodId = "removePropertyChangeListener"; // NOI18N
//        Method removeMethod = null;
//        String listenerTypeId = "java.beans.PropertyChangeListener"; // NOI18N
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
//        Type listenerType = jmodel.getType().resolve(listenerTypeId);
//
//        addMethod = classElement.getMethod( addMethodId, Collections.singletonList(listenerType), false);
//        if ( addMethod == null ) {
//            addMethod = jmodel.getMethod().createMethod();
//            addMethod.setName( addMethodId );
//            addMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//            addMethod.setModifiers( Modifier.PUBLIC );
//            Parameter param = jmodel.getParameter().createParameter();
//            param.setName("l"); // NOI18N
//            param.setType(listenerType);
//            addMethod.getParameters().add(param);
//
//            StringBuffer body = new StringBuffer( 80 );
//            body.append( "\n" ).append( TAB + supportName ); // NOI18N
//            body.append( ".addPropertyChangeListener(l);\n" ); // NOI18N
//            addMethod.setBodyText( body.toString() );
//
//            /*
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_AddPropertyChangeListener" ), 
//                                                   new Object[] { listenerType.getClassName().getName() } );
//            */                                          
//            addMethod.setJavadocText( PatternNode.getString( "COMMENT_AddPropertyChangeListener" ) );
//            classElement.getFeatures().add( addMethod );
//        }
//
//        removeMethod = classElement.getMethod(removeMethodId, Collections.singletonList(listenerType), false);
//        if ( removeMethod == null ) {
//            removeMethod = jmodel.getMethod().createMethod();
//            removeMethod.setName( removeMethodId );
//            removeMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//            removeMethod.setModifiers( Modifier.PUBLIC );
//            Parameter param = jmodel.getParameter().createParameter();
//            param.setName("l"); // NOI18N
//            param.setType(listenerType);
//            removeMethod.getParameters().add(param);
//
//            StringBuffer body = new StringBuffer( 80 );
//            body.append( "\n" ).append( TAB + supportName ); // NOI18N
//            body.append( ".removePropertyChangeListener(l);\n" ); // NOI18N
//            removeMethod.setBodyText( body.toString() );
//            removeMethod.setJavadocText( PatternNode.getString( "COMMENT_RemovePropertyChangeListener" ) );
//            classElement.getFeatures().add( removeMethod );
//        }
//    }
//
//
//    /** If in the class don't exists methods for adding/removing VetoableChangeListeners
//     * for given field adds them.
//     * @param classElement Class to operate on.
//     * @param supportName The <CODE>vetoableChangeSupport</CODE> field the methods will be generated for.
//     * @throws JmiException If the modification of the source is impossible.
//     */
//    static void vetoSupportListenerMethods( JavaClass classElement, String supportName ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String addMethodId = "addVetoableChangeListener"; // NOI18N
//        Method addMethod = null;
//        String removeMethodId = "removeVetoableChangeListener"; // NOI18N
//        Method removeMethod = null;
//        String listenerTypeId = "java.beans.VetoableChangeListener"; // NOI18N
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
//        Type listenerType = jmodel.getType().resolve(listenerTypeId);
//
//        addMethod = classElement.getMethod( addMethodId, Collections.singletonList(listenerType), false);
//        if ( addMethod == null ) {
//            addMethod = jmodel.getMethod().createMethod();
//            addMethod.setName( addMethodId );
//            addMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//            addMethod.setModifiers( Modifier.PUBLIC );
//            Parameter param = jmodel.getParameter().createParameter();
//            param.setName("l"); // NOI18N
//            param.setType(listenerType);
//            addMethod.getParameters().add(param);
//
//            StringBuffer body = new StringBuffer( 80 );
//            body.append( "\n" ).append( TAB + supportName ); // NOI18N
//            body.append( ".addVetoableChangeListener (l);\n" ); // NOI18N
//            addMethod.setBodyText( body.toString() );
//            addMethod.setJavadocText( PatternNode.getString( "COMMENT_AddVetoableChangeListener" ) );
//            classElement.getFeatures().add( addMethod );
//        }
//
//        removeMethod = classElement.getMethod(removeMethodId, Collections.singletonList(listenerType), false);
//        if ( removeMethod == null ) {
//            removeMethod = jmodel.getMethod().createMethod();
//            removeMethod.setName( removeMethodId );
//            removeMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//            removeMethod.setModifiers( Modifier.PUBLIC );
//            Parameter param = jmodel.getParameter().createParameter();
//            param.setName("l"); // NOI18N
//            param.setType(listenerType);
//            removeMethod.getParameters().add(param);
//
//            StringBuffer body = new StringBuffer( 80 );
//            body.append( "\n" ).append( TAB + supportName ); // NOI18N
//            body.append( ".removeVetoableChangeListener (l);\n" ); // NOI18N
//            removeMethod.setBodyText( body.toString() );
//            removeMethod.setJavadocText( PatternNode.getString( "COMMENT_RemoveVetoableChangeListener" ) );
//            classElement.getFeatures().add( removeMethod );
//        }
//    }
//
//    /** Ensures that the listeners array list exists. Used for generating
//     * multicast event source support implemented by java.util.ArrayList.
//     * Searches the source for suitable field. If the field does not exists
//     * creates new one.
//     * @param ce Class to operate on.
//     * @param type Type of the Event Listener.
//     * @throws JmiException If the modification of the source is impossible.
//     * @return found or newly created field.
//     */
//    static Field listenersArrayListField( JavaClass ce, Type type, boolean create ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        Field ret = null;
//        String fieldName = null;
//        String simpleTypeName = ((JavaClass) type).getSimpleName();
//        String fieldNameToFind = Introspector.decapitalize( simpleTypeName ) + "List"; // NOI18N
//
//        String fieldTypeId = "java.util.ArrayList"; // NOI18N
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ce);
//        Type fieldType = jmodel.getType().resolve(fieldTypeId);
//        
//        List/*<Field>*/ fields = JMIUtils.getFields(ce);
//        for (Iterator it = fields.iterator(); it.hasNext();) {      // Try to find suitable field
//            Field field = (Field) it.next();
//            if (JMIUtils.equalTypes(fieldType, field.getType()) && fieldNameToFind.equals(field.getName())) {
//                fieldName = fieldNameToFind;
//                ret = field;
//                break;
//            }
//        }
//
//        if ( fieldName == null && create) { // Field not found we create new
//            fieldName = fieldNameToFind;
//            Field field = jmodel.getField().createField();
//            field.setName( fieldName );
//            field.setType( fieldType );
//            field.setModifiers( Modifier.PRIVATE | Modifier.TRANSIENT );
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_ListenerArrayList" ),
//                                                   new Object[] { simpleTypeName } );
//            field.setJavadocText( comment );
//
//            ce.getFeatures().add( field );
//            ret = field;
//        }
//
//        return ret;
//    }
//
//    /** Ensure the listenersList  exists. Used for generating
//     * multicast event source support implemented by javax.swing.event.EventListenerList.
//     * Searches the source for suitable field. If the field does not exists
//     * creates new one.
//     * @param ce Class to operate on.
//     * @param type Type of the Event Listener.
//     * @throws JmiException If the modification of the source is impossible.
//     * @return Field newly created field.
//     */
//    static Field eventListenerListField( JavaClass ce, Type type, boolean create ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String fieldName = null;
//        Field ret = null;
//
//        String fieldTypeId = "javax.swing.event.EventListenerList"; // NOI18N
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ce);
//        Type fieldType = jmodel.getType().resolve(fieldTypeId);
//        List/*<Field>*/ fields = JMIUtils.getFields(ce);
//        
//        for (Iterator it = fields.iterator(); it.hasNext();) {      // Try to find suitable field
//            Field field = (Field) it.next();
//            if (JMIUtils.equalTypes(fieldType, field.getType())) {
//                fieldName = field.getName();
//                ret = field;
//                break;
//            }
//        }
//
//        if ( fieldName == null && create) { // Field not found we create new
//            fieldName = "listenerList"; // NOI18N
//            Field field = jmodel.getField().createField();
//            field.setName( fieldName );
//            field.setType( fieldType );
//            field.setModifiers( Modifier.PRIVATE );
//            field.setInitialValueText( " null" ); // NOI18N
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_EventListenerList" ),
//                                                   new Object[] { ((JavaClass) type).getSimpleName() } );
//            field.setJavadocText( comment );
//
//            ce.getFeatures().add( field );
//            ret = field;
//        }
//
//        return ret;
//    }
//
//    /** Ensure that listener field for unicast exists. Used for generating
//     * unicast event source support.
//     * Searches the source for suitable field. If the field does not exists
//     * creates new one.
//     * @param ce Class to operate on.
//     * @param type Type of the Event Listener.
//     * @throws JmiException If the modification of the source is impossible.
//     * @return Field newly created field.
//     */
//    static Field unicastListenerField( JavaClass ce, Type type, boolean create ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        Field ret = null;
//        String fieldName = null;
//        String typeSimpleName = ((JavaClass) type).getSimpleName();
//        String fieldNameToFind = Introspector.decapitalize( typeSimpleName );
//        if ( fieldNameToFind.equals( typeSimpleName ) ) {
//            fieldNameToFind = "listener" + fieldNameToFind; // NOI18N
//        }
//
//        List/*<Field>*/ fields = JMIUtils.getFields(ce);
//        
//        for (Iterator it = fields.iterator(); it.hasNext();) {      // Try to find suitable field
//            Field field = (Field) it.next();
//            if (JMIUtils.equalTypes(type, field.getType()) && fieldNameToFind.equals(field.getName())) {
//                fieldName = fieldNameToFind;
//                ret = field;
//                break;
//            }
//        }
//
//        if ( fieldName == null && create) { // Field not found we create new
//            fieldName = fieldNameToFind;
//            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ce);
//            Field field = jmodel.getField().createField();
//            field.setName( fieldName );
//            field.setType( type );
//            field.setModifiers( Modifier.PRIVATE  | Modifier.TRANSIENT );
//            field.setInitialValueText( " null" ); // NOI18N
//            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_UnicastEventListener" ),
//                                                   new Object[] { ((JavaClass) type).getSimpleName() } );
//            field.setJavadocText( comment );
//            ce.getFeatures().add( field );
//            ret = field;
//        }
//        return ret;
//    }
//
//    static String mcAddBody( Type type, int implementation, String listenerList ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String fieldName = Introspector.decapitalize( ((JavaClass) type).getSimpleName() ) + "List"; // NOI18N
//
//        StringBuffer body = new StringBuffer( 50 );
//
//        if ( listenerList == null )
//            listenerList = "listenerList"; // NOI18N
//
//        body.append( "\n"); // NOI18N
//
//        if ( implementation == 1 ) {
//            body.append( TAB + "if (" ).append( fieldName ).append( " == null ) {\n" ); // NOI18N
//            body.append( TABx2 ).append( fieldName ).append( " = new java.util.ArrayList ();\n" ); // NOI18N
//            body.append( TAB ).append( "}\n" ); // NOI18N
//            body.append( TAB + fieldName ).append( ".add (listener);\n" ); // NOI18N
//        }
//        else if ( implementation == 2 ) {
//            body.append( TAB + "if (" ).append( listenerList ).append( " == null ) {\n" ); // NOI18N
//            body.append( TABx2 ).append( listenerList ).append( " = new javax.swing.event.EventListenerList();\n" ); // NOI18N
//            body.append( TAB ).append( "}\n" ); // NOI18N
//            body.append( TAB + listenerList ).append( ".add (" ); // NOI18N
//            body.append( type.getName()).append( ".class, listener);\n" ); // NOI18N
//        }
//
//        return body.toString();
//    }
//
//    static String mcRemoveBody( Type type, int implementation, String listenerList ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String fieldName = Introspector.decapitalize( ((JavaClass) type).getSimpleName() ) + "List"; // NOI18N
//
//        if ( listenerList == null )
//            listenerList = "listenerList"; // NOI18N
//
//        StringBuffer body = new StringBuffer( 50 );
//        body.append( "\n"); // NOI18N
//
//        if ( implementation == 1 ) {
//            body.append( TAB + "if (" ).append( fieldName ).append( " != null ) {\n" ); // NOI18N
//            body.append( TABx2 + fieldName ).append( ".remove (listener);\n" ); // NOI18N
//            body.append( TAB ).append( "}\n" ); // NOI18N
//        }
//        else if ( implementation == 2 ) {
//            body.append( TAB + listenerList ).append( ".remove (" ); // NOI18N
//            body.append( type.getName()).append( ".class, listener);\n" ); // NOI18N
//        }
//
//        return body.toString();
//    }
//
//    static String ucAddBody( Type type, int implementation ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String simpleTypeName = ((JavaClass) type).getSimpleName();
//        String fieldName = Introspector.decapitalize( simpleTypeName );
//        if ( fieldName.equals( simpleTypeName ) ) {
//            fieldName = "listener" + fieldName; // NOI18N
//        }
//
//        StringBuffer body = new StringBuffer( 50 );
//
//        body.append( "\n"); // NOI18N
//
//        if ( implementation == 1 ) {
//            body.append( TAB + "if (").append( fieldName ).append( " != null) {\n" ); // NOI18N
//            body.append( TABx2 + "throw new java.util.TooManyListenersException ();\n" ); // NOI18N
//            body.append( TAB + "}\n" ); // NOI18N
//            body.append( TAB + fieldName ).append( " = listener;\n" ); // NOI18N
//        }
//
//        return body.toString();
//    }
//
//    static String ucRemoveBody( Type type, int implementation ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String simpleTypeName = ((JavaClass) type).getSimpleName();
//        String fieldName = Introspector.decapitalize( simpleTypeName );
//        if ( fieldName.equals( simpleTypeName ) ) {
//            fieldName = "listener" + fieldName; // NOI18N
//        }
//
//        StringBuffer body = new StringBuffer( 50 );
//        body.append( "\n"); // NOI18N
//
//        if ( implementation == 1 ) {
//            body.append( TAB + fieldName ).append( " = null;\n" ); // NOI18N
//        }
//
//        return body.toString();
//    }
//
//
//    static void fireMethod( JavaClass classElement, Type type,
//                            Method method, int implementation,
//                            String listenerList,
//                            boolean passEvent )
//    throws JmiException {
//
//        assert JMIUtils.isInsideTrans();
//        if ( listenerList == null )
//            listenerList = "listenerList"; // NOI18N
//
//        String simpleTypeName = ((JavaClass) type).getSimpleName();
//        String methodId = "fire" + // NOI18N
//                                  Pattern.capitalizeFirstLetter( simpleTypeName ) +
//                                  Pattern.capitalizeFirstLetter( method.getName() );
//
//        Method newMethod = null;
//
//        Type eventType = null;
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
//        List/*<Parameter>*/ params = method.getParameters();
//        if ( params.isEmpty() ) {
//            eventType = jmodel.getType().resolve("java.util.EventObject"); // NOI18N
//        } else {
//            eventType = ((Parameter) params.get(0)).getType();
//        }
//
//        JavaClass eventClass = (JavaClass) eventType;
//
//        //addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );
//
//        //if ( addMethod == null ) {
//        newMethod = jmodel.getMethod().createMethod();
//        newMethod.setName( methodId );
//        newMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//        newMethod.setModifiers( Modifier.PRIVATE );
//
//        List/*<Parameter>*/ newMethodParams = generateFireParameters( eventClass, jmodel, passEvent );
//        newMethod.getParameters().addAll( newMethodParams );
//
//        StringBuffer body = new StringBuffer( 80 );
//        body.append( "\n" ); // NOI18N
//
//        if ( implementation == 1 ) {
//            String fieldName = Introspector.decapitalize( simpleTypeName ) + "List"; // NOI18N
//
//            body.append( TAB + "java.util.ArrayList list;\n" ); // NOI18N
//
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( TAB + eventType.getName() ).append( " e = new "); // NOI18N
//                body.append( eventType.getName() ).append( " (" ); // NOI18N
//                body.append( fireParameterstoString( newMethodParams ) );
//                body.append(");\n"); // NOI18N
//            }
//            body.append( TAB + "synchronized (this) {\n" ); // NOI18N
//            body.append( TABx2 + "if (" + fieldName + " == null) return;\n" ); // NOI18N
//            body.append( TABx2 + "list = (java.util.ArrayList)" ); // NOI18N
//            body.append( fieldName ).append( ".clone ();\n" + TAB +"}\n" ); // NOI18N
//            body.append( TAB + "for (int i = 0; i < list.size (); i++) {\n" ); // NOI18N
//            body.append( TABx2 + "((" ).append( type.getName() ); // NOI18N
//            body.append( ")list.get (i)).").append( method.getName() ); // NOI18N
//            body.append(" ("); // NOI18N
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( "e" ); // NOI18N
//            }
//            else {
//                body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
//            }
//            body.append( ");\n" + TAB + "}\n" ); // NOI18N
//        }
//        else if ( implementation == 2 ) {
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( TAB + eventType.getName() ).append( " e = null;\n "); // NOI18N
//            }
//            body.append( TAB + "if (" + listenerList + " == null) return;\n"); // NOI18N
//            body.append( TAB + "Object[] listeners = ").append(listenerList).append(".getListenerList ();\n" ); // NOI18N
//            body.append( TAB + "for (int i = listeners.length - 2; i >= 0; i -= 2) {\n"); // NOI18N
//            body.append( TABx2 + "if (listeners[i]==" ).append( type.getName()).append( ".class) {\n" ); // NOI18N
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( TABx3 + "if (e == null)\n" ); // NOI18N
//                body.append( TABx2 + TABx2 + "e = new ").append( eventType.getName() ).append( " (" ); // NOI18N
//                body.append( fireParameterstoString( newMethodParams ) );
//                body.append( ");\n" ); // NOI18N
//            }
//            body.append( TABx3 + "((").append(type.getName()).append(")listeners[i+1]).").append(method.getName()); // NOI18N
//            body.append(" ("); // NOI18N
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( "e" ); // the created event // NOI18N
//            }
//            else {
//                body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
//            }
//            body.append( ");\n" + TABx2 + "}\n" + TAB + "}\n"); // NOI18N
//        }
//
//        newMethod.setBodyText( body.toString() );
//
//        StringBuffer comment = new StringBuffer ( PatternNode.getString( "COMMENT_FireMethodMC" ) );
//        if ( !usesConstructorParameters( eventClass, passEvent ) ) {
//            comment.append( "\n@param event The event to be fired\n" ); // NOI18N
//        }
//        else {
//            comment.append( fireParametersComment( newMethodParams, ((JavaClass) eventType).getSimpleName() ) );
//        }
//        newMethod.setJavadocText( comment.toString() );
//
//        classElement.getFeatures().add( newMethod );
//        //}
//    }
//
//    static void unicastFireMethod( JavaClass classElement, Type type,
//                                   Method method, int implementation,
//                                   boolean passEvent )
//    throws JmiException {
//
//        assert JMIUtils.isInsideTrans();
//        String simpleTypeName = ((JavaClass) type).getSimpleName();
//        String methodId = "fire" + // NOI18N
//                          Pattern.capitalizeFirstLetter( simpleTypeName ) +
//                          Pattern.capitalizeFirstLetter( method.getName() );
//
//        Method newMethod = null;
//
//        Type eventType = null;
//        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(classElement);
//        List/*<Parameter>*/ params = method.getParameters();
//        if ( params.isEmpty() ) {
//            eventType = jmodel.getType().resolve("java.util.EventObject"); // NOI18N
//        } else {
//            eventType = ((Parameter) params.get(0)).getType();
//        }
//
//        JavaClass eventClass = (JavaClass) eventType;
//
//        //addMethod = classElement.getMethod( addMethodId, new Type[] { listenerType }  );
//
//        //if ( addMethod == null ) {
//        newMethod = jmodel.getMethod().createMethod();
//        newMethod.setName( methodId );
//        newMethod.setType( jmodel.getType().resolve("void") ); // NOI18N
//        newMethod.setModifiers( Modifier.PRIVATE );
//
//        List/*<Parameter>*/ newMethodParams = generateFireParameters( eventClass, jmodel, passEvent );
//        newMethod.getParameters().addAll( newMethodParams );
//
//        StringBuffer body = new StringBuffer( 80 );
//        body.append( "\n" ); // NOI18N
//
//        if ( implementation == 1 ) {
//            String fieldName = Introspector.decapitalize( simpleTypeName );
//            if ( fieldName.equals( simpleTypeName ) ) {
//                fieldName = "listener" + fieldName; // NOI18N
//            }
//            
//            body.append(TAB + "if (" + fieldName + " == null) return;\n"); // NOI18N
//
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( TAB + eventType.getName() ).append( " e = new "); // NOI18N
//                body.append( eventType.getName() ).append( " (" ); // NOI18N
//                body.append( fireParameterstoString( newMethodParams ) );
//                body.append(");\n"); // NOI18N
//            }
//
//            body.append( TAB + fieldName ).append( "." ).append( method.getName() ); // NOI18N
//            body.append(" ("); // NOI18N
//            if ( usesConstructorParameters( eventClass, passEvent ) ) {
//                body.append( "e" ); // NOI18N
//            }
//            else {
//                body.append( fireParameterstoString( newMethodParams ) ); // the event parameter
//            }
//            body.append( ");\n" ); // NOI18N
//        }
//
//        newMethod.setBodyText( body.toString() );
//
//        StringBuffer comment = new StringBuffer ( PatternNode.getString( "COMMENT_FireMethodUC" ) );
//        if ( !usesConstructorParameters( eventClass, passEvent ) ) {
//            comment.append( "\n@param event The event to be fired\n" ); // NOI18N
//        }
//        else {
//            comment.append( fireParametersComment( newMethodParams, ((JavaClass) eventType).getSimpleName()) ); // the event parameter
//        }
//        newMethod.setJavadocText( comment.toString() );
//
//        classElement.getFeatures().add( newMethod );
//        //}
//    }
//
//
//
//    static boolean usesConstructorParameters( JavaClass eventClass, boolean passEvent ) throws JmiException {
//
//        assert JMIUtils.isInsideTrans();
//        if ( passEvent || eventClass == null || eventClass.isInterface() || JMIUtils.getConstructors(eventClass).size() > 1 )
//            return false;
//        else
//            return true;
//    }
//
//
//    static List/*<Parameter>*/ generateFireParameters( JavaClass eventClass, JavaModelPackage jmodel, boolean passEvent )
//            throws JmiException {
//
//        assert JMIUtils.isInsideTrans();
//        if ( !usesConstructorParameters( eventClass, passEvent ) ) {
//            Parameter param = jmodel.getParameter().createParameter();
//            param.setName("event"); // NOI18N
//            param.setType(eventClass);
//            return Collections.singletonList(param);
//        }
//        else {
//            Constructor constructor = (Constructor) JMIUtils.getConstructors(eventClass).get(0);
//            List/*<Parameter>*/ params = constructor.getParameters();
//            List/*<Parameter>*/ result = new ArrayList/*<Parameter>*/(params.size());
//            for (Iterator it = params.iterator(); it.hasNext();) {
//                Parameter param = (Parameter) it.next();
//                Parameter newParam = jmodel.getParameter().createParameter();
//                newParam.setName(param.getName()); // NOI18N
//                newParam.setType(param.getType());
//                result.add(newParam);
//            }
//            return result;
//        }
//
//    }
//
//    static String fireParameterstoString( List/*<Parameter>*/  params ) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        StringBuffer buffer = new StringBuffer( 60 );
//        
//        for (Iterator it = params.iterator(); it.hasNext();) {
//            Parameter param = (Parameter) it.next();
//            buffer.append( param.getName() );
//            buffer.append( ", " ); // NOI18N
//        }
//        if (buffer.length() > 2) {
//            return buffer.substring(0, buffer.length() - 2);
//        } else {
//            return buffer.toString();
//        }
//    }
//
//    static String fireParametersComment( List/*<Parameter>*/  params, String evntType ) throws JmiException {
//
//        assert JMIUtils.isInsideTrans();
//        StringBuffer buffer = new StringBuffer( 60 );
//        int i = 0;
//
//        for (Iterator it = params.iterator(); it.hasNext();) {
//            Parameter param = (Parameter) it.next();
//            buffer.append( "\n@param ").append( param.getName() ); // NOI18N
//            buffer.append( " Parameter #" ).append( (i++) + 1 ).append( " of the <CODE>" ); // NOI18N
//            buffer.append( evntType ).append( "<CODE> constructor." ); // NOI18N
//        }
//        buffer.append( "\n" ); // NOI18N
//
//        return buffer.toString();
//    }
//
//    // UTILITY METHODS ----------------------------------------------------------
//
//    /** For primitive {@link org.openide.src.Type type} finds class for wrapping it into object.
//     * E.g. <CODE>Type.BOOLEAN -> Boolean</CODE>
//     * @param type Primitive type.
//     * @return Class which wraps the primitive type.
//     */
//    public static String getWrapperClassName(Type type) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        if (type instanceof ClassDefinition)
//            return type.getName();
//        if (!(type instanceof PrimitiveType)) 
//            throw new IllegalStateException("Unknonw type: " + type); // NOI18N
//        
//        String typeName = type.getName();
//        char[] ctype = typeName.toCharArray();
//        switch (ctype[0]) {
//            case 'b':
//                if (ctype[1] == 'o') // boolean
//                    return "Boolean"; //NOI18N
//                else
//                    return "Byte"; // NOI18N
//            case 'd':
//                return "Double"; // NOI18N
//            case 'f':
//                return "Float"; // NOI18N
//            case 'c':
//                return "Character"; // NOI18N
//            case 'i':
//                return "Integer"; // NOI18N
//            case 'l':
//                return "Long"; // NOI18N
//            case 's':
//                return "Short"; // NOI18N
//            default:
//                return "Object"; // NOI18N
//        }
//    }
//    
}
