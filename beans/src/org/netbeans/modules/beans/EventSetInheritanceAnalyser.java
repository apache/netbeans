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
