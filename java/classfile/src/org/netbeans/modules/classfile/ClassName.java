/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.classfile;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.WeakHashMap;

/**
 * Class representing the name of a Java class.  This class is immutable, and
 * can therefore be safely used by multiple threads.
 * <p>
 * <b>Warning:</b>  The same reference is often returned by the getClassName 
 * factory method for multiple calls which use the same type name as its
 * parameter.  However, no guarantee is made that this behavior will always 
 * be true, so the equals method should always be used to compare ClassName 
 * instances.
 *
 * @author Thomas Ball
 */
public final class ClassName implements Comparable<ClassName>, Comparator<ClassName>, Serializable {

    static final long serialVersionUID = -8444469778945723553L;

    private final String type;
    private final transient String internalName;
    private transient volatile String externalName;
    private transient volatile String packageName;
    private transient volatile String simpleName;

    private static final WeakHashMap<String,WeakReference<ClassName>> cache = 
            new WeakHashMap<String,WeakReference<ClassName>>();

    /**
     * Returns the ClassName object referenced by a class
     * type string (field descriptor), as defined in the 
     * JVM Specification, sections 4.3.2 and 4.2.  
     * <P>
     * Basically, the JVM Specification defines a class type 
     * string where the periods ('.') separating
     * a package name are replaced by forward slashes ('/').
     * Not documented in the second edition of the specification
     * is that periods separating inner and outer classes are
     * replaced with dollar signs ('$').  Array classes have one
     * or more left brackets ('[') prepending the class type.
     * For example:
     * <pre>
     *   java.lang.String         java/lang/String
     *   java.util.HashMap.Entry  java/util/HashMap$Entry
     *   java.lang.Integer[]      [java/lang/Integer
     *   java.awt.Point[][]       [[java/awt/Point
     * </pre>
     * <P>
     * This method also accepts type strings which contain with
     * 'L' and end with ';' characters.  This format is used
     * to reference a class in other type names, such as
     * method arguments.  These two characters are removed from the
     * type string.
     * <P>
     * Because ClassNames are immutable, multiple requests to
     * get the same type string may return identical object
     * references.  This cannot be assumed, however, and the
     * ClassName.equals() method should be used instead of
     * "==" to test for equality.
     *
     * @param classType  the class type string, as defined by the JVM spec.
     * @return the ClassName instance, or null if not found.
     * @throws IllegalArgumentException if classType isn't of the correct
     *                   format.
     */
    public static ClassName getClassName(String classType) {
        // A null superclass name is supposed to be null, but may be
        // an empty string depending on the compiler.
        if (classType == null || classType.length() == 0)
	    return null;

        ClassName cn = getCacheEntry(classType);
        synchronized (cache) {
            cn = getCacheEntry(classType);
            if (cn == null) {
                // check for valid class type
                int i = classType.indexOf('L');
                String _type;
                char lastChar = classType.charAt(classType.length()-1);
                if (i != -1 && lastChar == ';') {
                    // remove 'L' and ';' from type
                    _type = classType.substring(i+1, classType.length()-1);
                    if (i > 0)
                        // add array prefix
                        _type = classType.substring(0, i) + _type;
                    cn = getCacheEntry(_type);
                    if (cn != null)
                        return cn;
                } else {
                    _type = classType;
                }

                cn = new ClassName(_type);
                cache.put(_type, new WeakReference<ClassName>(cn));
            }
        }
	return cn;
    }

    private static ClassName getCacheEntry(String key) {
	WeakReference<ClassName> ref = cache.get(key);
	return ref != null ? ref.get() : null;
    }

    /**
     * Create a ClassName object via its internal type name, as
     * defined by the JVM spec.
     * @param type the internal type name
     */
    private ClassName(String type) {
        this.type = type;

	// internal name is a type stripped of any array designators
	int i = type.lastIndexOf('[');
	internalName = (i > -1) ? type.substring(i+1) : type;
    }

    /**
     * Returns the type string of this class, as stored in the 
     * classfile (it's "raw" form).  For example, an array of
     * Floats would have a type of "[java/lang/Float".
     * @return the raw string format of the class type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the "internal" classname, as defined by the JVM 
     * Specification, without any parameter or return type 
     * information.  For example, the name for the String
     * class would be "java/lang/String".  Inner classes are 
     * separated from their outer class with '$'; such as
     * "java/util/HashMap$Entry".  Array specifications are
     * stripped; use getType() instead.
     * @return the internal name
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * Returns the "external" classname, as defined by the 
     * Java Language Specification, without any parameter
     * or return type information.  For example, the name for the
     * String class would be "java.lang.String".  Inner classes
     * are separated from their outer class with '.'; such as
     * "java.util.HashMap.Entry".  Arrays are shown as having one
     * or more "[]" characters behind the base classname, such
     * as "java.io.Files[]".
     * @return the external name, with array characters if this ClassName 
     *         is an array
     */
    public String getExternalName() {
        return getExternalName(false);
    }

    /**
     * Returns the "external" classname, as defined by the 
     * Java Language Specification, without any parameter
     * or return type information.  For example, the name for the
     * String class would be "java.lang.String".  Inner classes
     * are separated from their outer class with '.'; such as
     * "java.util.HashMap.Entry".  Unless suppressed, arrays are 
     * shown as having one or more "[]" characters behind the 
     * base classname, such as "java.io.Files[]".
     * @param suppressArrays true if array characters should be included
     * @return the external name
     */
    public String getExternalName(boolean suppressArrays) {
        initExternalName();
        int i;
        if (suppressArrays && (i = externalName.indexOf('[')) != -1)
	    return externalName.substring(0, i);
        return externalName;
    }
    
    private synchronized void initExternalName() {
        if (externalName == null) 
            externalName = externalizeClassName();
    }

    /**
     * Return the package portion of this classname.
     * @return the package name
     */
    public String getPackage() {
        if (packageName == null)
            initPackage();
	return packageName;
    }

    private synchronized void initPackage() {
        int i = internalName.lastIndexOf('/');
        packageName = (i != -1) ? 
            internalName.substring(0, i).replace('/', '.') : "";
    }

    /**
     * Returns the classname without any package specification.
     * @return the simple name
     */
    public String getSimpleName() {
        if (simpleName == null)
            initSimpleName();
	return simpleName;
    }
    
    private synchronized void initSimpleName() {
        String pkg = getPackage();
        int i = pkg.length();
        String extName = getExternalName();
        if (i == 0)
            simpleName = extName;  // no package
        else
            simpleName = extName.substring(i + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
	    return true;
	return (obj instanceof ClassName) && 
            type.equals(((ClassName) obj).type);
    }

    /**
     * Compares this ClassName to another Object.  If the Object is a 
     * ClassName, this function compares the ClassName's types.  Otherwise,
     * it throws a <code>ClassCastException</code>.
     *
     * @param   obj the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a string
     *		lexicographically equal to this string; a value less than
     *		<code>0</code> if the argument is a string lexicographically 
     *		greater than this string; and a value greater than
     *		<code>0</code> if the argument is a string lexicographically
     *		less than this string.
     * @see     java.lang.Comparable
     */
    public int compareTo(ClassName obj) {
        // If obj isn't a ClassName, the correct ClassCastException
        // will be thrown by the cast.
        return type.compareTo(obj.type);
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparator.
     */
    public int compare(ClassName o1, ClassName o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return getExternalName();
    }

    // Called from synchronization block, do not call out!
    private String externalizeClassName() {
        StringBuffer sb = new StringBuffer(type);
	int arrays = 0;
	boolean atBeginning = true;
	int length = sb.length();
	for (int i = 0; i < length; i++) {
	    char ch = sb.charAt(i);
	    switch (ch) {
	      case '[': 
		if (atBeginning)
		    arrays++; 
		break;

	      case '/':
	      case '$':
		sb.setCharAt(i, '.');
		atBeginning = false;
		break;

	      default:
		atBeginning = false;
	    }
	}

	if (arrays > 0) {
	    sb.delete(0, arrays);
	    for (int i = 0; i < arrays; i++)
	      sb.append("[]");
	}

        return sb.toString();
    }
    
    /**
     * Empties the cache -- used by unit tests.
     */
    static void clearCache() {
        cache.clear();
    }

    /*
     * Suppress multiple instances of the same type, as well as any
     * immutability attacks (unlikely as that might be).  For more information
     * on this technique, check out Effective Java, Item 57, by Josh Bloch.
     */
    private Object readResolve() throws ObjectStreamException {
        return getClassName(internalName);
    }
}
