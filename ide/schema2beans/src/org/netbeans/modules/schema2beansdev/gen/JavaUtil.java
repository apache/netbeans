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

package org.netbeans.modules.schema2beansdev.gen;

import java.util.*;
import java.io.*;

/**
 * Various utility methods dealing with Java
 */
public class JavaUtil {
    // This class is not for instantiating.
    private JavaUtil() {
    }

    /**
     * Convert primitives into Objects and leave objects as is.
     * @param expr the text expression representing some value
     * @param classType the current type of @param expr
     *   ('age', 'int') -> 'new java.lang.Integer(age)'
     *   ('age', 'Integer') -> 'age'
     */
    private static String toObject(String expr, String classType, boolean java5) {
        classType = classType.intern();
        if ("boolean" == classType)
            return "("+expr+" ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE)";
        else {
            String objClass = (String) toObjectType.get(classType);
            if (objClass == null) {
                return expr;
            }
            if (java5) {
                return objClass+".valueOf("+expr+")";
            } else {
                return "new "+objClass+"("+expr+")";
            }
        }
    }
    public static String toObject(String expr, String classType, boolean j2me, boolean java5) {
        if (j2me) {
            if ("boolean".equals(classType))
                return "new java.lang.Boolean("+expr+")";
        }
        return toObject(expr, classType, java5);
    }
    
    private static Map toObjectType;
    static {
        toObjectType = new HashMap();
        toObjectType.put("int", "java.lang.Integer");
        toObjectType.put("char", "java.lang.Character");
        toObjectType.put("long", "java.lang.Long");
        toObjectType.put("short", "java.lang.Short");
        toObjectType.put("byte", "java.lang.Byte");
        toObjectType.put("float", "java.lang.Float");
        toObjectType.put("double", "java.lang.Double");
        toObjectType.put("boolean", "java.lang.Boolean");
    }

    /**
     * Take a Java primitive and return it's object type.
     * Return the same type if there isn't an object version.
     */
    public static String toObjectType(String classType) {
        String objClass = (String) toObjectType.get(classType);
        if (objClass == null)
            return classType;
        return objClass;
    }

    private static Map fromObjectType;
    static {
        fromObjectType = new HashMap();
        fromObjectType.put("java.lang.Integer", "int");
        fromObjectType.put("java.lang.Character", "char");
        fromObjectType.put("java.lang.Long", "long");
        fromObjectType.put("java.lang.Short", "short");
        fromObjectType.put("java.lang.Byte", "byte");
        fromObjectType.put("java.lang.Float", "float");
        fromObjectType.put("java.lang.Double", "double");
        fromObjectType.put("java.lang.Boolean", "boolean");
        fromObjectType.put("Integer", "int");
        fromObjectType.put("Character", "char");
        fromObjectType.put("Long", "long");
        fromObjectType.put("Short", "short");
        fromObjectType.put("Byte", "byte");
        fromObjectType.put("Float", "float");
        fromObjectType.put("Double", "double");
        fromObjectType.put("Boolean", "boolean");
    }

    /**
     * Take a Java boxed object (like Integer) and return it's primitive type.
     * Return the same type if there isn't a primitive version.
     */
    public static String fromObjectType(String classType) {
        String objClass = (String) fromObjectType.get(classType);
        if (objClass == null)
            return classType;
        return objClass;
    }

    /**
     * Convert expr into a String.  Similar to toObject.
     * @param expr the value to convert into a String
     * @param type is the name of the current type
     *   ('String', 'value') -> 'value'
     *   ('int', '42') -> '""+42'
     *   ('Integer', 'age') -> 'age.toString()'
     */
    public static String typeToString(String type, String expr) {
        type = (type == null) ? null : type.intern();
        if ("String" == type || "java.lang.String" == type)
            return expr;
        if (type == "java.util.Calendar")
            return "java.text.DateFormat.getInstance().format("+expr+".getTime())";	// See JavaBeansUtil for correctly printing out XML Schema format
        if (type == "boolean")
            return expr+" ? \"true\" : \"false\"";
        if (isPrimitiveType(type))
            return "\"\"+"+expr;
        return expr+".toString()";
    }
    
    /**
     * @param expr is an Object type, and we will convert it
     * to a primitive.
     *   eg: ('java.lang.Double', expr) -> '(java.lang.Double) expr'
     *   eg: ('int', 'obj') -> '((java.lang.Integer)obj).intValue()'
     */
    public static String fromObject(String type, String expr) {
        type = type.intern();
        if (type == "int")
            return "((java.lang.Integer)"+expr+").intValue()";
        if (type == "long")
            return "((java.lang.Long)"+expr+").longValue()";
        if (type == "short")
            return "((java.lang.Short)"+expr+").shortValue()";
        if (type == "byte")
            return "((java.lang.Byte)"+expr+").byteValue()";
        if (type == "float")
            return "((java.lang.Float)"+expr+").floatValue()";
        if (type == "double")
            return "((java.lang.Double)"+expr+").doubleValue()";
        if (type == "char")
            return "((java.lang.Character)"+expr+").charValue()";
        if (type == "boolean")
            return "((java.lang.Boolean)"+expr+").booleanValue()";
        return "("+type+")"+expr;
    }

    /**
     * Is @param className immutable?  An immutable object can hold state,
     * but after it's been constructed that state cannot change.
     */
    public static boolean isImmutable(String className) {
        className = className.intern();
        return immutableTable.containsKey(className);
    }
    private static Map immutableTable;
    static {
        immutableTable = new HashMap();
        immutableTable.put("String", null);
        immutableTable.put("java.lang.String", null);
        immutableTable.put("int", null);
        immutableTable.put("short", null);
        immutableTable.put("long", null);
        immutableTable.put("boolean", null);
        immutableTable.put("char", null);
        immutableTable.put("float", null);
        immutableTable.put("double", null);
        immutableTable.put("byte", null);
        immutableTable.put("java.lang.Boolean", null);
        immutableTable.put("java.lang.Byte", null);
        immutableTable.put("java.lang.Character", null);
        immutableTable.put("java.lang.Double", null);
        immutableTable.put("java.lang.Float", null);
        immutableTable.put("java.lang.Integer", null);
        immutableTable.put("java.lang.Long", null);
        immutableTable.put("java.lang.Short", null);
        immutableTable.put("Boolean", null);
        immutableTable.put("Byte", null);
        immutableTable.put("Character", null);
        immutableTable.put("Double", null);
        immutableTable.put("Float", null);
        immutableTable.put("Integer", null);
        immutableTable.put("Long", null);
        immutableTable.put("Short", null);
        immutableTable.put("java.math.BigInteger", null);
        immutableTable.put("java.math.BigDecimal", null);
        immutableTable.put("javax.xml.namespace.QName", null);
        immutableTable.put("org.netbeans.modules.schema2beans.QName", null);
        immutableTable.put("java.net.URI", null);
    }

    /**
     * Take a String (@param value) and generate Java code to coerce it.
     *   eg:  ('String', "Hello") -> '"Hello"'
     *        ('int', '10') -> '10'
     *        ('Integer', '43') -> 'Integer.valueOf(43)'
     *        ('java.util.Locale', 'Locale.US') -> 'Locale.US'
     */
    public static String instanceFrom(String type, String value) {
        if (type == null)
            return value;
        type = type.intern();
        if (type == "java.lang.String" || type == "String") {
            StringBuffer buf = new StringBuffer("\"");
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                buf.append(escapeCharForInstance(c, '"'));
            }
            buf.append("\"");
            return buf.toString();
        }
        if (type == "java.lang.Character" || type == "Character") {
            return "new "+type+"("+instanceFrom("char", value)+")";
        }
        if (type == "Double" || type == "java.lang.Double" || type == "Integer" ||
            type == "java.lang.Integer" || type == "Boolean" ||
            type == "java.lang.Boolean" || type == "Float" ||
            type == "java.lang.Float" || type == "Short" ||
            type == "java.lang.Short" || type == "Long" ||
            type == "java.lang.Long")
            return "new "+type+"("+value+")";
        if (type == "char[]" || type == "char []")
            return instanceFrom("java.lang.String", value)+".toCharArray()";
        if (type == "char") {
            char c = value.charAt(0);
            return "'"+escapeCharForInstance(c, '\'')+"'";
        }
        if (type == "float")
            return value+"f";
        if (type == "java.math.BigDecimal" || type == "java.math.BigInteger") {
            // We will never need to escape anything inside value here.
            return "new "+type+"(\""+value+"\")";
        }
        return value;
    }

    /**
     * A helper method for instanceFrom.  We escape 1 char at a time here.
     */
    public static String escapeCharForInstance(char c, char illegalChar) {
        if (c == illegalChar)
            return "\\"+illegalChar;
        if (c == '\\')
            return "\\\\";
        if (c == '\b')
            return "\\b";
        if (c == '\t')
            return "\\t";
        if (c == '\n')
            return "\\n";
        if (c == '\f')
            return "\\f";
        if (c == '\r')
            return "\\r";
        if (c < ' ' || c > '\177')
            return uencode(c);
        return ""+c;
    }

    /**
     * Convert @param value2 (a literal) to @param type1 and then
     * compare that to @param value1.  
     * @return Java code in a String, that returns negative, 0, or positive
     *         if value1 < value2, value1 == value2, or value1 > value2.
     *
     * ("foo", "java.math.BigDecimal", "1") -> 'foo.compareTo(new java.math.BigDecimal("1"))'
     */
    public static String compareTo(String value1, String type1, String value2) {
        type1 = type1.intern();
        // FIXME this should be fixed (Java5) as well
        String value2Instance = genParseText(type1, value2, false);
        if (isPrimitiveType(type1)) {
            if ("\"0\"".equals(value2))
                return value1;
            return value1+" - "+value2Instance;
        }
        return value1+".compareTo("+value2Instance+")";
    }

    /**
     * Just like compareTo, but the second value (@param value2Text) is
     * unquoted text.
     */
    public static String compareToText(String value1, String type1, String value2Text) {
        type1 = type1.intern();
        if (type1 == "int" || type1 == "short")
            return value1+" - "+value2Text;
        if (type1 == "long")
            return value1+" - "+value2Text+"L";
        if (type1 == "float")
            return value1+" - "+value2Text+"f";
        if (type1 == "double")
            return value1+" - "+value2Text+"d";
        return compareTo(value1, type1, instanceFrom("java.lang.String", value2Text));
    }

    public static String genParseText(String type, String expr, boolean j2me, boolean java5) {
        if (j2me) {
            return genParseTextME(type, expr);
        } else {
            return genParseText(type, expr, java5);
        }
    }

    /**
     * Take a particular @param type (eg: "java.lang.Integer") and return a
     * String that will parse the @param expr and make it into that type.
     * The value of @param expr should be a String.
     *   eg: ('java.lang.Integer', 'node.getNodeValue()')
     *        ->  'new java.lang.Integer(node.getNodeValue())'
     */
    public static String genParseText(String type, String expr, boolean java5) {
        if (type == null)
            return expr;
        type = type.intern();
        if (type == "java.lang.String" || type == "String")
            return expr;
        if (type == "boolean") {
            // The XML Schema spec part 2, section 3.2.2 says a boolean can
            // have any of these literal values: true, false, 1, 0.
            return "(\"true\".equalsIgnoreCase("+expr+") || \"1\".equals("+expr+"))";
            //return "java.lang.Boolean.valueOf("+expr+").booleanValue()";
        }
        if (type == "java.lang.Boolean" || type == "Boolean") // NOI18N
            return genParseText("boolean", expr, java5)+"? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE"; // NOI18N
        if (type == "java.lang.Integer" || type == "Integer" // NOI18N
                || type == "java.lang.Long" || type == "Long" // NOI18N
                || type == "java.lang.Short" || type == "Short" // NOI18N
                || type == "java.lang.Float" || type == "Float" // NOI18N
                || type == "java.lang.Double" || type == "Double" // NOI18N
                || type == "java.lang.Byte" || type == "Byte") { // NOI18N
            if (java5) {
                return type+".valueOf("+expr+")";	// NOI18N
            } else {
                return "new "+type+"("+expr+")";	// NOI18N
            }
        }
        if (type == "java.math.BigDecimal" || type == "BigDecimal" // NOI18N
                || type == "java.math.BigInteger" || type == "BigInteger" // NOI18N
                || type == "java.lang.StringBuffer" || type == "StringBuffer" // NOI18N
                || type == "java.text.MessageFormat" // NOI18N
                || type == "java.text.AttributedString" // NOI18N
                || type == "java.util.StringTokenizer" // NOI18N
                || type == "java.net.URI" // NOI18N
                || type == "javax.xml.namespace.QName" // NOI18N
                || type == "org.netbeans.modules.schema2beans.QName" // NOI18N
                || type == "java.io.File") { // NOI18N
            return "new "+type+"("+expr+")"; // NOI18N
        }
        if (type == "java.lang.Character") { // NOI18N
            if (java5) {
                return "java.lang.Character.valueOf(("+expr+").charAt(0))"; // NOI18N
            } else {
                return "new java.lang.Character(("+expr+").charAt(0))"; // NOI18N
            }
        }
        if (type == "char[]" || type == "char []")
            return "("+expr+").toCharArray()";
        if (type == "long")
            return "Long.parseLong("+expr+")";
        if (type == "int")
            return "Integer.parseInt("+expr+")";
        if (type == "byte")
            return "Byte.parseByte("+expr+")";
        if (type == "short")
            return "Short.parseShort("+expr+")";
        if (type == "float")
            return "Float.parseFloat("+expr+")";
        if (type == "double")
            return "Double.parseDouble("+expr+")";
        if (type == "char")
            return "("+expr+").charAt(0)";
        if ("java.util.Date"== type)
            return "java.text.DateFormat.getInstance().parse("+expr+")";
        if ("javax.xml.soap.SOAPElement" == type)
            return "javax.xml.soap.SOAPElementFactory.newInstance().create("+expr+").addTextNode("+expr+")";  // The parameter to create probably isn't quite correct.
        // See JavaBeansUtil for correctly printing out XML Schema format of java.util.Calendar
        return "/* UNKNOWN type for parsing:"+type+"*/ "+expr;
    }

    /**
     * Take a particular @param type (eg: "java.lang.Integer") and return a
     * String that will parse the @param expr and make it into that type.
     * The value of @param expr should be a String.
     * These work in *MIDP/J2ME*.
     *   eg: ('java.lang.Integer', 'node.getNodeValue()')
     *        ->  'new java.lang.Integer(node.getNodeValue())'
     */
    public static String genParseTextME(String type, String name) {
        String parm = name;
        type = type.intern();
        if (type == "String" || type == "java.lang.String")
            return parm;
        else if (type == "int")
            parm = "java.lang.Integer.parseInt(" + name + ")";
        else if(type == "short")
            parm = "java.lang.Short.parseShort(" + name + ")";
        else if(type == "long")
            parm = "java.lang.Long.parseLong(" + name + ")";
        else if (type == "boolean")
            parm = "(\"true\".equals("+name+") || \"1\".equals("+name+") || \"TRUE\".equals("+name+") || \"True\".equals("+name+"))";
        else if (type == "char")
            parm = name + ".charAt(0)";
        else if ("char[]" == type)
            parm = name+".toCharArray()";
        else if ("java.lang.Integer" == type)
            parm = "new java.lang.Integer("+genParseTextME("int", name)+")";
        else if ("java.lang.Long" == type)
            parm = "new java.lang.Long("+genParseTextME("long", name)+")";
        else if ("java.lang.Boolean" == type) {
            // The .TRUE or .FALSE trick doesn't seem to compile.
            //parm = "("+genParseTextME("boolean", name)+") ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE";
            parm = "new java.lang.Boolean("+genParseTextME("boolean", name)+")";
        } else if (type == "double")
            parm = "java.lang.Double.parseDouble(" + name + ")";
        else if (type == "float")
            parm = "java.lang.Float.parseFloat(" + name + ")"; 
        else if (type == "byte")
            parm = "java.lang.Byte.parseByte(" + name + ")"; 
        else if ("java.lang.Short" == type)
            parm = "new "+type+"("+genParseTextME("short", name)+")";
        else if ("java.lang.Byte" == type)
            parm = "new "+type+"("+genParseTextME("byte", name)+")";
        else if ("java.lang.Double" == type 
                 || "java.lang.Float" == type
                 || "java.lang.StringBuffer" == type
                 || "java.math.BigInteger" == type
                 || "java.math.BigDecimal" == type)
            parm = "new "+type+"("+name+")";
        /*
        else if ("java.util.Date" == type)
            parm = "???";
        */
        else if ("java.lang.Character" == type)
            parm = "new "+type+"("+name+".charAt(0))";
        return parm;            
    }

    /**
     * Take a particular @param type (eg: "java.lang.Integer") and return a
     * String that will parse the @param expr, making it into that type,
     * and storing the value into @param var.
     * The value of @param expr should be a String.
     */
    public static String genParseText(String type, String expr, String var, boolean java5) {
        return genParseText(type, expr, var, false, java5);
    }
    
    public static String genParseText(String type, String expr, String var,
                                      boolean j2me, boolean java5) {
        if (type == null)
            return expr;
        type = type.intern();
        StringBuffer out = new StringBuffer();
        if (type == "java.util.Calendar") {
            // See JavaBeansUtil for correctly printing out XML Schema format of java.util.Calendar
            out.append(var);
            out.append(" = ");
            out.append("java.util.Calendar.getInstance(); ");
            out.append(var);
            out.append(".setTime(java.text.DateFormat.getInstance().parse(");
            out.append(expr);
            out.append("));");
        } else {
            out.append(var);
            out.append(" = ");
            out.append(genParseText(type, expr, j2me, java5));
            out.append(";");
        }
        return out.toString();
    }

    /**
     * What exceptions might we encounter from doing the result of
     * genParseText.  These exceptions are ones that we'd have to declare.
     */
    public static List exceptionsFromParsingText(String type) {
        return exceptionsFromParsingText(type, true);
    }
    
    public static List exceptionsFromParsingText(String type, boolean fromParsing) {
        List result = new ArrayList();
        if (type == null)
            return result;
        type = type.intern();
        if (type == "java.net.URI")
            result.add("java.net.URISyntaxException");
        if (fromParsing && type == "java.util.Calendar")
            result.add("java.text.ParseException");
        return result;
    }

    public static boolean isPrimitiveType(String className) {
        if (className == null)
            return false;
        className = className.intern();
        return ("long" == className || "int" == className ||
                "char" == className || "short" == className ||
                "double" == className || "float" == className ||
                "byte" == className || "boolean" == className);
    }

    public static Class getPrimitive(String className) {
        className = className.intern();
        if (className == "int")
            return Integer.TYPE;
        if (className == "long")
            return Long.TYPE;
        if (className == "float")
            return Float.TYPE;
        if (className == "double")
            return Double.TYPE;
        if (className == "byte")
            return Byte.TYPE;
        if (className == "boolean")
            return Boolean.TYPE;
        if (className == "char")
            return Character.TYPE;
        if (className == "short")
            return Short.TYPE;
        if (className == "void")
            return Void.TYPE;
        return null;
    }

    public static boolean canProduceNoXMLMetaChars(String className) {
        className = fromObjectType(className).intern();
        return ("long" == className || "int" == className ||
                "short" == className ||
                "double" == className || "float" == className ||
                "byte" == className || "boolean" == className ||
                "java.math.BigDecimal" == className ||
                "java.math.BigInteger" == className);
    }

    public static String nullValueForType(String type) {
        type = type.intern();
        if (type == "long" || type == "int" || type == "short" || type == "char" || type == "byte")
            return "0";
        if (type == "double")
            return "0.0";
        if (type == "float")
            return "0.0f";
        if (type == "boolean")
            return "false";
        return "null";
    }

    /**
     * @param type is the name of a class
     * @return a default value for that type.
     *  eg: X -> new X()
     *      java.math.BigDecimal -> new java.math.BigDecimal("0")
     *      Integer -> Integer.valueOf("0")
     */
    public static String genNewDefault(String type) {
        type = type.intern();
        if ("java.lang.String" == type || "String" == type)
            return "\"\"";
        if (isPrimitiveType(type))
            return nullValueForType(type);
        if (type == "java.lang.Boolean" || type == "Boolean")
            return "java.lang.Boolean.FALSE";
        if (type == "java.lang.Integer" || type == "Integer" || type == "java.lang.Long" || type == "Long" || type == "java.lang.Short" || type == "Short" || type == "java.lang.Float" || type == "Float" || type == "java.lang.Double" || type == "Double" || type == "java.lang.Byte" || type == "Byte" || type == "java.math.BigInteger" || type == "BigInteger" || type == "java.math.BigDecimal" || type == "BigDecimal") {	// NOI18N
            return "new "+type+"(\"0\")";	// NOI18N
        }
        if (type == "java.net.URI" || type == "javax.xml.namespace.QName" || type == "org.netbeans.modules.schema2beans.QName")
            return "new "+type+"(\"\")";	// NOI18N
        if (type == "java.lang.Character")
            return "new java.lang.Character('0')";
        if (type == "java.util.Calendar")
            return "java.util.Calendar.getInstance()";
        if (type == "byte[]")
            return "new byte[0]";
        if (type == "org.w3c.dom.Element")
            return "null";  // No default value
        return "new "+type+"()";
    }

    /**
     * Given a scalar type, figure out how to make it into an int
     * (ie, hash code).
     */
    public static String exprToInt(String type, String expr) {
        type = type.intern();
        if (type == "boolean")
            return expr+" ? 0 : 1";
        else if (type == "byte" || type == "char" || type == "short")
            return "(int) "+expr;
        else if (type == "int")
            return expr;
        else if (type == "long")
            return "(int)("+expr+"^("+expr+">>>32))";
        else if (type == "float")
            return "Float.floatToIntBits("+expr+")";
        else if (type == "double")
            return exprToInt("long", "Double.doubleToLongBits("+expr+")");
        else
            return "("+expr+").hashCode()";
    }

    /**
     * @return an expression to compare to expressions; this can be put right
     * into an if statement.
     *    ('int', 'var1', 'var2') -> 'var1 == var2'
     *    ('String', 'word', '"hello"') -> 'word == null ? "hello" == null : word.equals("hello")'
     *    ('float', 'var1', 'var2') -> 'Float.floatToIntBits(var1) == Float.floatToIntBits(var2)'
     */
    public static String genEquals(String type, String attr1, String attr2) {
        return genEquals(type, attr1, attr2, true);
    }

    /**
     * @param attr1CanBeNull  whether or not attr1 could be null.
     */
    public static String genEquals(String type, String attr1, String attr2,
                                   boolean attr1CanBeNull) {
        type = type.intern();
        if (type == "float") {
            return "Float.floatToIntBits("+attr1+") == Float.floatToIntBits("+attr2+")";
        } else if (type == "double") {
            return "Double.doubleToLongBits("+attr1+") == Double.doubleToLongBits("+attr2+")";
        } else if (isPrimitiveType(type)) {
            return attr1+" == "+attr2;
        } else if (attr1CanBeNull) {
            return attr1+" == null ? "+attr2+" == null : "+attr1+".equals("+attr2+")";
        } else {
            return attr1+".equals("+attr2+")";
        }
    }

    /**
     * Looks for the class and sees if it's cloneable.
     */
    public static boolean isCloneable(String className) {
        if (className == null)
            return false;
        className = className.intern();
        if (className == "java.util.Calendar")
            return true;
        try {
            //System.out.println("Looking for class: "+className);
            Class cls = Class.forName(className);
            if (cls == null)
                return false;
            //System.out.println("Found it");
            if (Cloneable.class.isAssignableFrom(cls)) {
                System.out.println(className+" is cloneable.");
                return true;
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Is the class not an interface and not abstract; i.e., it's possible
     * to call a constructor on this class.  (Note that primitives fail.)
     */
    public static boolean isInstantiable(String className) {
        if (className == null)
            return false;
        className = className.intern();
        if (className == "String" || className == "java.lang.String")
            return true;
        try {
            //System.out.println("Looking for class: "+className);
            Class cls = Class.forName(className);
            if (cls == null)
                return false;
            //System.out.println("Found it: result="+cls.isInterface());
            if (cls.isInterface())
                return false;
            if (java.lang.reflect.Modifier.isAbstract(cls.getModifiers()))
                return false;
            return true;
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') < 0)
                return isInstantiable("java.lang."+className);
            return false;
        }
    }

    /**
     * checkValueToType will make sure that a given value is
     * acceptable for a given type.  To make the problem more
     * tractable, this is limited to simple types.
     *  eg:
     *    ("java.lang.Integer", "1") -> true
     *    ("java.lang.Integer", "1.5") -> false
     *    ("java.lang.String", "ksadjflkjas24#@") -> true
     *    ("short", "12345") -> true
     *    ("short", "123456") -> false
     * Note that the 'tostr' template in javaGenLibrary.xsl has very
     * similar code and any changes should be made there too.
     */
    public static boolean checkValueToType(String type, String value) {
        //System.out.println("checkValueToType: type="+type+" value='"+value+"'");
        // We try to convert it (just like how javaGenLibrary.xsl
        // would generate code for).  If an IllegalArgumentException
        // is thrown, then it's the wrong value for that type.
        try {                                                                   // BEGIN_NOI18N
            if ("java.lang.String".equals(type) || "char[]".equals(type) 
                || "char []".equals(type) || "char".equals(type)
                || "Character".equals(type) || "String".equals(type)
                || "java.lang.Character".equals(type))
                return true;
            else if ("long".equals(type))
                Long.parseLong(value);
            else if ("int".equals(type))
                Integer.parseInt(value);
            else if ("byte".equals(type))
                Byte.parseByte(value);
            else if ("short".equals(type))
                Short.parseShort(value);
            else if ("float".equals(type))
                Float.parseFloat(value);
            else if ("double".equals(type))
                Double.parseDouble(value);
            else if ("boolean".equals(type))
                Boolean.valueOf(value).booleanValue();
            else if ("java.lang.Double".equals(type))
                new java.lang.Double(value);
            else if ("java.lang.Integer".equals(type))
                new java.lang.Integer(value);
            else if ("java.lang.Boolean".equals(type))
                Boolean.valueOf(value);
            else if ("java.lang.Float".equals(type))
                new java.lang.Float(value);
            else if ("java.lang.Short".equals(type))
                new java.lang.Short(value);
            else if ("java.lang.Long".equals(type))
                new java.lang.Long(value);
            else if ("java.math.BigDecimal".equals(type))
                new java.math.BigDecimal(value);
            else if ("java.math.BigInteger".equals(type))
                new java.math.BigInteger(value);
            else if ("java.lang.StringBuffer".equals(type))
                new java.lang.StringBuffer(value);
            else if ("java.text.MessageFormat".equals(type))
                new java.text.MessageFormat(value);
            else if ("java.text.AttributedString".equals(type))
                new java.text.AttributedString(value);
            else if ("java.util.StringTokenizer".equals(type))
                new java.util.StringTokenizer(value);
            else {                                                              // END_NOI18N
                /*
                // Should do some reflection and see if a valueOf method
                // exists and takes a single String as an argument.
                Class clz = getClass(type);
                if (clz != null) {
                    java.lang.reflect.Method meth = 
                        clz.getMethod("valueOf", new Class[] {String.class});
                    if (meth == null || !Modifier.isStatic(meth.getModifiers())) {
                        return false;
                    }
                } else {
                    return false;
                }
                // Could try to invoke the method too, but do we
                // really want to invoke a method from some random class.
                 */
                if ("".equals(value))                                           // NOI18N
                    return false;  // Hack, should check value
                return true;    // for now
            }
        } catch (IllegalArgumentException e) {
            return false;
        /*
        } catch (java.lang.ClassNotFoundException e) {
            LogFlags.lgr.println(LogFlags.DEBUG, LogFlags.module,
                                 LogFlags.DBG_VALIDATE, 100,
                                 "checkValueToType got ClassNotFoundException for type='"+type+"' value='"+value+"'");
            return false;
        } catch (NoSuchMethodException e) {
            LogFlags.lgr.println(LogFlags.DEBUG, LogFlags.module,
                                 LogFlags.DBG_VALIDATE, 100,
                                 "checkValueToType got NoSuchMethodException for type='"+type+"' value='"+value+"'");
            return false;
         */
        }
        return true;
    }

    public static String baseClassOfArray(String className) {
        // Does this handle more than 1 dimensional arrays correctly
        if (className.startsWith("[L") && className.endsWith(";")) {  // NOI18N
            return className.substring(2, className.length()-1);
        }
        return className.substring(0, className.length()-2);
    }

    /**
     * This will return a name from @param fullClassName where everything upto
     * and including the last '.' is removed.
     *   eg: "java.lang.String[]" -> "String[]"
     *       "java.util.ArrayList" -> "ArrayList"
     */
    public static String baseName(String fullClassName) {
        int pos = fullClassName.lastIndexOf('.');
        if (pos == -1)
            return fullClassName;
        return fullClassName.substring(pos+1);
    }

    private static final Class charArrayClass = 
        java.lang.reflect.Array.newInstance(java.lang.Character.TYPE, 0).getClass();

    public static String getCanonicalClassName(Class cls) {
        if (charArrayClass.isAssignableFrom(cls))
            return "char[]";  // NOI18N
        if (cls.isArray())
            return baseClassOfArray(cls.getName())+"[]";
        return cls.getName();
    }

    @Deprecated
    public static int getOptimialHashMapSize(Object[] keys) {
        return getOptimialHashMapSize(keys, keys.length * 8);
    }

    /**
     * Using reflection figure out the optimal initial capacity for a
     * HashMap given some keys.  This uses a load factor of 1.0f.
     * By optimal, the table does not need resizing and there are
     * no lists (or chaining) being done in a HashMap.Entry.
     *
     * @param maxSize the point at which to give up (the maximum size to try)
     */
    @Deprecated
    public static int getOptimialHashMapSize(Object[] keys, int maxSize) {
        int keyLength = keys.length;
        int defaultAnswer = keyLength * 3 / 2;
        try {
            java.lang.reflect.Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true); // requires --add-opens for util package
            for (int tableSize = keyLength + 1; tableSize <= maxSize;
                 tableSize <<= 1) {
                //System.out.println("tableSize="+tableSize);
                HashMap map = new HashMap(tableSize, 1.0f);
                for (int k = 0; k < keyLength; ++k) {
                    map.put(keys[k], null);
                }
                Object[] table = (Object[]) tableField.get(map);
                int nullCount = 0;
                for (int i = 0; i < table.length; ++i) {
                    //System.out.println("table["+i+"]="+table[i]);
                    if (table[i] == null)
                        ++nullCount;
                }
                //System.out.println("nullCount="+nullCount);
                if (table.length - nullCount != keyLength) {
                    //System.out.println("A list had begun.");
                    continue;
                }
                return table.length;
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return defaultAnswer;
        } catch (RuntimeException ex) {
            // todo: remove this workaround post JDK 9+ migration (or entire method)
            // modules on modern JDKs should prefere immutable collections anyway, e.g Map.of();
            if (ex.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException")) {
                return defaultAnswer;
            } else {
                throw ex;
            }
        }
        return defaultAnswer;
    }

    // Convert the @param in stream using native2ascii, assuming it's already
    // UTF-8 encoded.
    public static void native2ascii(Writer out, Reader in) throws java.io.IOException {
        FilterWriter n2afilter = new N2AFilter(out);
        copyStream(n2afilter, in);
    }

    public static class N2AFilter extends FilterWriter {
        public N2AFilter(Writer writer) {
            super(writer);
        }

        public void write(char[] cbuf) throws IOException {
            write(cbuf, 0, cbuf.length);
        }

        public void write(int c) throws IOException {
            write((char) c);
        }

        public void write(char c) throws IOException {
            //System.out.println("c="+c);
            if(c > '\177') {
                out.write(uencode(c));
            } else {
                out.write(c);
            }
        }

        public void write(char ac[], int off, int len) throws IOException {
            int end = off+len;
            for (int k = off; k < end; k++) {
                write(ac[k]);
            }
        }

        public void write(String str) throws IOException {
            write(str.toCharArray(), 0, str.length());
        }

        public void write(String str, int off, int len) throws IOException {
            write(str.toCharArray(), off, len);
        }
    }

    /**
     * Take a character and return the \ u (Unicode) representation of it.
     */
    public static String uencode(char c) {
        StringBuffer result = new StringBuffer("\\u");
        String s1 = Integer.toHexString(c);
        StringBuffer stringbuffer = new StringBuffer(s1);
        stringbuffer.reverse();
        int l = 4 - stringbuffer.length();
        for(int i1 = 0; i1 < l; i1++)
            stringbuffer.append('0');
        
        for(int j1 = 0; j1 < 4; j1++)
            result.append(stringbuffer.charAt(3 - j1));
        return result.toString();
    }

    public static final int BUFFER_SIZE = 4096;
    /**
     * copyStream is not really a Java Utility method, but it's needed by
     * one of them, and so is here.
     * @return the total length of the stream (in char's) copied.
     */
    public static int copyStream(Writer out, Reader in) throws java.io.IOException {
        int len;
        int totalLength = 0;
        char[] buf = new char[BUFFER_SIZE];
        while ((len = in.read(buf, 0, BUFFER_SIZE)) != -1) {
            out.write(buf, 0, len);
            totalLength += len;
        }
        out.flush();
        return totalLength;
    }

    /**
     * copyStream is not really a Java Utility method, but it's needed by
     * one of them, and so is here.
     * @return the total length of the stream (in char's) copied.
     */
    public static int copyStream(OutputStream out, InputStream in) throws java.io.IOException {
        int len;
        int totalLength = 0;
        byte[] buf = new byte[BUFFER_SIZE];
        while ((len = in.read(buf, 0, BUFFER_SIZE)) != -1) {
            out.write(buf, 0, len);
            totalLength += len;
        }
        out.flush();
        return totalLength;
    }

    public static class InputMonitor implements Runnable {
        private InputStream is;
        private OutputStream out;
        
        public InputMonitor(InputStream is, OutputStream out) {
            this.is = is;
            this.out = out;
        }

        /**
         * Copy the contents of the InputStream to the Writer.
         * Remember to close the InputStream or else this Thread will
         * never end.
         */
        public void run() {
            //System.out.println("Starting InputMonitor thread");
            try {
                int c;
                while ((c = is.read()) != -1) {
                    byte ch = (byte)c;
                    out.write(ch);
                }
                out.flush();
            } catch (java.io.IOException e) {
                try {
                    out.write(e.getMessage().getBytes());
                } catch (java.io.IOException e2) {
                    // try only once.
                }
            }
            //System.out.println("Finished InputMonitor thread");
        }
    }

    private static Map reservedWords;
    static {
        reservedWords = new HashMap();
        reservedWords.put("abstract", "_abstract");
        reservedWords.put("assert", "_assert");
        reservedWords.put("boolean", "_boolean");
        reservedWords.put("break", "_break");
        reservedWords.put("byte", "_byte");
        reservedWords.put("case", "_case");
        reservedWords.put("catch", "_catch");
        reservedWords.put("char", "_char");
        reservedWords.put("class", "_class");
        reservedWords.put("const", "_const");
        reservedWords.put("continue", "_continue");
        reservedWords.put("default", "_default");
        reservedWords.put("do", "_do");
        reservedWords.put("double", "_double");
        reservedWords.put("else", "_else");
        reservedWords.put("extends", "_extends");
        reservedWords.put("false", "_false");
        reservedWords.put("final", "_final");
        reservedWords.put("finally", "_finally");
        reservedWords.put("float", "_float");
        reservedWords.put("for", "_for");
        reservedWords.put("goto", "_goto");
        reservedWords.put("if", "_if");
        reservedWords.put("implements", "_implements");
        reservedWords.put("import", "_import");
        reservedWords.put("instanceof", "_instanceof");
        reservedWords.put("int", "_int");
        reservedWords.put("interface", "_interface");
        reservedWords.put("long", "_long");
        reservedWords.put("native", "_native");
        reservedWords.put("new", "_new");
        reservedWords.put("null", "_null");
        reservedWords.put("package", "_package");
        reservedWords.put("private", "_private");
        reservedWords.put("protected", "_protected");
        reservedWords.put("public", "_public");
        reservedWords.put("return", "_return");
        reservedWords.put("short", "_short");
        reservedWords.put("static", "_static");
        reservedWords.put("strictfp", "_strictfp");
        reservedWords.put("super", "_super");
        reservedWords.put("switch", "_switch");
        reservedWords.put("synchronized", "_synchronized");
        reservedWords.put("this", "_this");
        reservedWords.put("throw", "_throw");
        reservedWords.put("throws", "_throws");
        reservedWords.put("transient", "_transient");
        reservedWords.put("true", "_true");
        reservedWords.put("try", "_try");
        reservedWords.put("void", "_void");
        reservedWords.put("volatile", "_volatile");
        reservedWords.put("while", "_while");
    }
    public static boolean reservedWord(String name) {
        return reservedWords.containsKey(name);
    }
}
