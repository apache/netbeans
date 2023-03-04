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

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.util.NbBundle;

/**
 * Helper class to simplify operation over java model.
 */
public final class BeanUtils {

    private BeanUtils() {}
    
    public static final String GET_PREFIX = "get"; // NOI18N
    public static final String SET_PREFIX = "set"; // NOI18N
    public static final String IS_PREFIX = "is"; // NOI18N
    public static final String ADD_PREFIX = "add"; // NOI18N
    public static final String REMOVE_PREFIX = "remove"; // NOI18N

    static final String[] WELL_KNOWN_LISTENERS =  new String[] {
        "java.awt.event.ActionListener", // NOI18N
        "java.awt.event.ContainerListener", // NOI18N
        "java.awt.event.FocusListener", // NOI18N
        "java.awt.event.ItemListener", // NOI18N
        "java.awt.event.KeyListener", // NOI18N
        "java.awt.event.MouseListener", // NOI18N
        "java.awt.event.MouseMotionListener", // NOI18N
        "java.awt.event.WindowListener", // NOI18N
        "java.beans.PropertyChangeListener", // NOI18N
        "java.beans.VetoableChangeListener", // NOI18N
        "javax.swing.event.CaretListener", // NOI18N
        "javax.swing.event.ChangeListener", // NOI18N
        "javax.swing.event.DocumentListener", // NOI18N
        "javax.swing.event.HyperlinkListener", // NOI18N
        "javax.swing.event.MenuListener", // NOI18N
        "javax.swing.event.MouseInputListener", // NOI18N
        "javax.swing.event.PopupMenuListener", // NOI18N
        "javax.swing.event.TableColumnModelListener", // NOI18N
        "javax.swing.event.TableModelListener", // NOI18N
        "javax.swing.event.TreeModelListener", // NOI18N
        "javax.swing.event.UndoableEditListener" // NOI18N
    };

   
    public static final String PROP_TYPE = "type"; // NOI18N
    public static final String PROP_MODE = "mode"; // NOI18N
    public static final String PROP_NAME = "name"; // NOI18N
    public static final String PROP_GETTER = "getter"; // NOI18N
    public static final String PROP_SETTER = "setter"; // NOI18N
    public static final String PROP_ESTIMATEDFIELD = "estimatedField"; // NOI18N
    public static final String PROP_INDEXEDTYPE = "indexedType"; // NOI18N
    public static final String PROP_INDEXEDGETTER = "indexedGetter"; // NOI18N
    public static final String PROP_INDEXEDSETTER = "indexedSetter"; // NOI18N
    public static final String PROP_ADDLISTENER = "addListener"; // NOI18N
    public static final String PROP_REMOVELISTENER = "removeListener"; // NOI18N
    public static final String PROP_ISUNICAST = "isUnicast"; // NOI18N
    
    /** Utility method capitalizes the first letter of string, used to
     * generate method names for patterns
     * @param str The string for capitalization.
     * @return String with the first letter capitalized.
     */
    static String capitalizeFirstLetter( String str ) {
        if ( str == null || str.length() <= 0 )
            return str;

        char chars[] = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    public static String nameAsString(Element e ) {
        return e.getSimpleName().toString();
    }
    
    public static boolean isThrowing( CompilationInfo ci, ExecutableElement method, String name) {
        
        TypeElement ex = ci.getElements().getTypeElement(name);
        if ( ex == null ) {
            return false;
        }
        TypeMirror eType = ex.asType();

        for (TypeMirror t : method.getThrownTypes() ) {
            if ( ci.getTypes().isSubtype(t, eType) ) {
                return true;
            }
        }
        
        return false;
    }
       
    public static String getString( String key ) {
        return NbBundle.getMessage(BeanUtils.class, key);
    }
    
    public static String typeAsString(TypeMirror type) {
        
        switch( type.getKind() ) {
            case DECLARED:
                return nameAsString(((DeclaredType)type).asElement());
            case ARRAY:
                return typeAsString(((ArrayType)type).getComponentType()) + "[]"; // NOI18N
            default:
                return type.toString();
        }
        
    }

    /**
     * Returns all public methods of a type element, whether inherited or
     * declared directly. It excludes methods of {@link Object} that are useless
     * for bean patterns.
     * @param clazz class to search
     * @param javac javac
     * @return list of public methods
     */
    public static List<? extends ExecutableElement> methodsIn(TypeElement clazz, CompilationInfo javac) {
        List<ExecutableElement> result = ElementFilter.methodsIn(javac.getElements().getAllMembers(clazz));
        final TypeElement objectElement = javac.getElements().getTypeElement("java.lang.Object"); // NOI18N
        for (int i = result.size() - 1; i >= 0; i--) {
            ExecutableElement method = result.get(i);
            if (!method.getModifiers().contains(Modifier.PUBLIC) || objectElement == method.getEnclosingElement()) {
                result.remove(i);
            }
        }
        return result;
    }
    
    // Unused methods
    
//    /**
//     * finds all methods of clazz
//     * @param clazz class to query
//     * @return list of methods
//     * @throws JmiException
//     */
//    public static List/*<Method>*/ getMethods(ClassDefinition clazz) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        List features = clazz.getFeatures();
//        List/*<Method>*/ methods = new LinkedList/*<Method>*/();
//        for (Iterator it = features.iterator(); it.hasNext();) {
//            Object f = it.next();
//            if (f instanceof Method) {
//                methods.add(f);
//            }
//        }
//        return methods;
//    }
//    
//    /**
//     * finds all constructors of clazz
//     * @param clazz class to query
//     * @return list of constructors
//     * @throws JmiException
//     */ 
//    public static List/*<Constructor>*/ getConstructors(ClassDefinition clazz) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        List features = clazz.getFeatures();
//        List/*<Constructor>*/ methods = new LinkedList/*<Constructor>*/();
//        for (Iterator it = features.iterator(); it.hasNext();) {
//            Object f = it.next();
//            if (f instanceof Constructor) {
//                methods.add(f);
//            }
//        }
//        return methods;
//    }
//    
//    /**
//     * finds all fields of clazz
//     * @param clazz class to query
//     * @return list of fields
//     * @throws JmiException
//     */ 
//    public static List/*<Field>*/ getFields(JavaClass clazz) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        List features = clazz.getFeatures();
//        List/*<Field>*/ fields = new LinkedList/*<Field>*/();
//        for (Iterator it = features.iterator(); it.hasNext();) {
//            Object f = it.next();
//            if (f instanceof Field) {
//                fields.add(f);
//            }
//        }
//        return fields;
//    }
//    
//    /**
//     * checks if the type is kind of some primitive type
//     * @param type type to query
//     * @param kind ptimitive type kind
//     * @return is kind or not
//     * @throws JmiException
//     */ 
//    public static boolean isPrimitiveType(Type type, PrimitiveTypeKindEnum kind) throws JmiException {
//        assert JMIUtils.isInsideTrans();
//        boolean is = false;
//        if (type instanceof PrimitiveType) {
//            PrimitiveType ptype = (PrimitiveType) type;
//            is = kind.equals(ptype.getKind());
//        }
//        return is;
//    }
//    
//    /**
//     * equals types. In case some type is {@link ParameterizedType} then its definition is taken to equal.
//     * This workarounds deficiency of {@link Type}.equals(). Here is example: You get type1 via
//     * {@link TypeClass#resolve(String)} and type2 via {@link org.netbeans.jmi.javamodel.Field#getType()}.
//     * type1 is subclass of Type but type2 is subclass of ParametrizedType.
//     * <code>type1.equals(type2)</code> is <code>false</code> in all cases.
//     * @param type1 type to equal
//     * @param type2 type to equal
//     * @return
//     * @throws JmiException
//     */ 
//    public static boolean equalTypes(Type type1, Type type2) throws JmiException {
//        if (type1==type2)
//            return true;
//        if (type1 == null || type2 == null) {
//            return false;
//        }
//        type1 = type1 instanceof ParameterizedType?
//                ((ParameterizedType) type1).getDefinition() : type1;
//        type2 = type2 instanceof ParameterizedType?
//                ((ParameterizedType) type2).getDefinition() : type2;
//        
//        return type1.equals(type2);
//    }
}
