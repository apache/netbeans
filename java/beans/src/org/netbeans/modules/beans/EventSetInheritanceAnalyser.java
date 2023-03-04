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

import java.text.MessageFormat;
import java.text.Format;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Modifier;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  Petr Suchomel
 * @version 0.1
 * utility class, try to detect if given ClassElement has parent which contains given event set
 */
final class EventSetInheritanceAnalyser extends Object {
    
//    /** Used to test if PropertyChangeSupport exists
//     * @param clazz Class which be tested for PropertyChangeSupport
//     * @return Class in which PropertySupport exist, or null
//     */    
//    static ClassMember detectPropertyChangeSupport(JavaClass clazz) throws JmiException {
//        return findSupport(clazz, "java.beans.PropertyChangeSupport" ); // NOI18N
//    }
//
//    /** Used to test if VetoableChangeSupport exists
//     * @param clazz Class which be tested for VetoableChangeSupport
//     * @return Class in which VetoableSupport exist, or null
//     */    
//    static ClassMember detectVetoableChangeSupport(JavaClass clazz) throws JmiException {
//        return findSupport(clazz, "java.beans.VetoableChangeSupport" ); // NOI18N
//    }
//    
//    /** Used to test if given ChangeSupport exists
//     * @param clazz Class which be tested for ChangeSupport
//     * @param supportName full name of ChangeSupport
//     * @return Class in which ChangeSupport exist, or null
//     */    
//    private static ClassMember findSupport(JavaClass clazz, String supportName) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        String propertyChangeField = supportName;
//        
//        if( clazz == null || "java.lang.Object".equals(clazz.getName())) //NOI18N
//            return null;    //no super class given or super class is Object
//        
//        JavaClass superClass = clazz.getSuperClass();
//        if( superClass == null || superClass instanceof UnresolvedClass) //no extends or implements clause
//            return null;
//        
//        List/*<Method>*/ methods = JMIUtils.getMethods(superClass);
//        for (Iterator it = methods.iterator(); it.hasNext();) {
//            Method method = (Method) it.next();
//            if( !Modifier.isPrivate(method.getModifiers()) && method.getParameters().isEmpty() ){
//                Type returnType = method.getType();
//                if( propertyChangeField.equals(returnType.getName()) ){
//                    return method;
//                }
//            }            
//        }            
//        List/*<Field>*/ fields = JMIUtils.getFields(superClass);
//        for (Iterator it = fields.iterator(); it.hasNext();) {
//            Field field = (Field) it.next();
//            if( !Modifier.isPrivate(field.getModifiers()) ){
//                if (propertyChangeField.equals(field.getType().getName())) {
//                    return field;
//                }
//            }            
//        }            
//        return findSupport(superClass, supportName);    //Try to search recursively            
//    }
//
//    static String showInheritanceEventDialog( ClassMember me , String supportTypeName) throws JmiException {        
//        assert JMIUtils.isInsideTrans();
//        String supportName = getInheritanceEventSupportName(me, supportTypeName);
//        if( me != null ){
//            Object msgfields[] = new Object[] {me.getDeclaringClass().getName(), supportTypeName };
//            String msg = MessageFormat.format(PatternNode.getString("MSG_Inheritance_Found"), msgfields);
//            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( msg , NotifyDescriptor.YES_NO_OPTION );
//            DialogDisplayer.getDefault().notify( nd );
//            if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {     
//                return supportName;
//            }
//        }        
//        return null;
//    }
//    
//    static String getInheritanceEventSupportName( ClassMember me , String supportTypeName) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        Format format = SourceNodes.createElementFormat("{n}({p})"); // NOI18N
//        String supportName = null;
//        if( me != null ){
//            if( me instanceof Method )
//                supportName = format.format(me);
//            else
//                supportName = me.getName();   //prepare for later usage            
//        }        
//        return supportName;
//    }
}
