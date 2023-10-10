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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class representing a Java class file.
 *
 * @author Thomas Ball
 */
public class ClassFile {
    private static final Logger LOG = Logger.getLogger(ClassFile.class.getName());

    ConstantPool constantPool;
    int classAccess;
    CPClassInfo classInfo;
    CPClassInfo superClassInfo;
    CPClassInfo[] interfaces;
    Variable[] variables;
    Method[] methods;
    String sourceFileName;
    InnerClass[] innerClasses;
    BootstrapMethod[] bootstrapMethods;
    Module module;
    List<String> modulePackages;
    ClassName moduleMainClz;
    ModuleTarget moduleTarget;
    private AttributeMap attributes;
    private Map<ClassName,Annotation> annotations;
    short majorVersion;
    short minorVersion;
    String typeSignature;
    EnclosingMethod enclosingMethod;
    private boolean includeCode = false;
    
    /** size of buffer in buffered input streams */
    private static final int BUFFER_SIZE = 4096;

     private static final Set<String> badNonJavaClassNames  =
             new HashSet<String>(Arrays.asList(new String[] {";","[","."}));    //NOI18N
    
    /**
     * Create a new ClassFile object.
     * @param classData   an InputStream from which the defining bytes of this
     *                    class or interface are read.
     * @throws IOException if InputStream can't be read, or if the class data
     *         is malformed.
     */
    public ClassFile(InputStream classData) throws IOException {
	this(classData, true);
    }
    
    /**
     * Create a new ClassFile object.
     * @param classFileName the path of a class file.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(String classFileName) throws IOException {
	this(classFileName, true);
    }
    
    /**
     * Create a new ClassFile object.
     * @param file a File instance of a class file.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(File file, boolean  includeCode) throws IOException {
	InputStream is = null;
	this.includeCode = includeCode;
        if( file == null || !file.exists() )
            throw new FileNotFoundException(file != null ? 
					    file.getPath() : "null");
        try {
            is = new BufferedInputStream( new FileInputStream( file ), BUFFER_SIZE);
            load(is);
        } catch (InvalidClassFormatException e) {
            throw new InvalidClassFormatException(file.getPath() + '(' +
						  e.getMessage() + ')');
        } finally {
            if (is != null)
                is.close();
        }                
    }

    /**
     * Create a new ClassFile object.
     * @param classData  an InputStream from which the defining bytes of this
     * class or interface are read.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if InputStream can't be read, or if the class data
     * is malformed.
     */
    public ClassFile(InputStream classData, boolean includeCode) throws IOException {
        if (classData == null)
            throw new IOException("input stream not specified");
	this.includeCode = includeCode;
        try {
            load(classData);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidClassFormatException("invalid classfile format");
        }
    }
    
    /**
     * Create a new ClassFile object.
     * @param classFileName the path of a class file.
     * @param includeCode true if this classfile should support operations
     *                    at the bytecode level.  Specify false to conserve
     *                    memory if code access isn't needed.
     * @throws IOException if file cannot be opened or read.
     **/
    public ClassFile(String classFileName, boolean includeCode) throws IOException {
        InputStream in = null;
	this.includeCode = includeCode;
        try {
            if (classFileName == null)
                throw new IOException("input stream not specified");
            in = new BufferedInputStream(new FileInputStream(classFileName), BUFFER_SIZE);
            load(in);
        } catch (InvalidClassFormatException e) {
            throw new InvalidClassFormatException(classFileName + '(' +
						  e.getMessage() + ')');
        } finally {
            if (in != null)
                in.close();
        }
    }
    
    
    /** Returns the ConstantPool object associated with this ClassFile.
     * @return the constant pool object
     */    
    public final ConstantPool getConstantPool() {
        return constantPool;
    }

    private void load(InputStream classData) throws IOException {
        try {
            DataInputStream in = new DataInputStream(classData);
            constantPool = loadClassHeader(in);
            interfaces = getCPClassList(in, constantPool);
            variables = Variable.loadFields(in, constantPool, this);
            methods = Method.loadMethods(in, constantPool, this, includeCode);
            attributes = AttributeMap.load(in, constantPool);
        } catch (IOException ioe) {
	    throw new InvalidClassFormatException(ioe);
        } catch (ClassCastException cce) {
            //May throw CCE when the class file format is broken,
            //see issue #211402 - the MethodInfo is on place of ClassInfo
            throw new InvalidClassFormatException(cce);
        }
    }

    private ConstantPool loadClassHeader(DataInputStream in) throws IOException {
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw new InvalidClassFormatException();
        }
            
        minorVersion = in.readShort();
        majorVersion = in.readShort();
        int count = in.readUnsignedShort();
        ConstantPool pool = new ConstantPool(count, in);
        classAccess = in.readUnsignedShort();
        classInfo = pool.getClass(in.readUnsignedShort());
        if (classInfo == null)
            throw new InvalidClassFormatException();
        if (isBadNonJavaClassName(classInfo.getName())) {
            throw new InvalidClassFormatException(
                String.format(
                    "Invalid non java class name: %s", //NOI18N
                    classInfo.getName()));
        }
        int index = in.readUnsignedShort();
        if (index != 0) // true for java.lang.Object
            superClassInfo = pool.getClass(index);
        return pool;
    }

    static CPClassInfo[] getCPClassList(DataInputStream in, ConstantPool pool)
      throws IOException {
        int count = in.readUnsignedShort();
        CPClassInfo[] classes = new CPClassInfo[count];
        for (int i = 0; i < count; i++) {
            classes[i] = pool.getClass(in.readUnsignedShort());
        }
        return classes;
    }
    
    /**
     * Returns the access permissions of this class or interface.
     * @return a mask of access flags.
     * @see org.netbeans.modules.classfile.Access
     */
    public final int getAccess() {
        return classAccess;
    }
    
    /** Returns the name of this class.
     * @return the name of this class.
     */
    public final ClassName getName() {
        return classInfo.getClassName();
    }

    /** Returns the name of this class's superclass.  A string is returned
     * instead of a ClassFile object to reduce object creation.
     * @return the name of the superclass of this class.
     */    
    public final ClassName getSuperClass() {
        if (superClassInfo == null)
            return null;
	return superClassInfo.getClassName();
    }
    
    /**
     * @return a collection of Strings describing this class's interfaces.
     */    
    public final Collection<ClassName> getInterfaces() {
        List<ClassName> l = new ArrayList<ClassName>();
        int n = interfaces.length;
        for (int i = 0; i < n; i++)
            l.add(interfaces[i].getClassName());
        return l;
    }
    
    /**
     * Looks up a variable by its name.
     *
     * NOTE: this method only looks up variables defined by this class,
     * and not inherited from its superclass.
     *
     * @param name the name of the variable
     * @return the variable,or null if no such variable in this class.
     */
    public final Variable getVariable(String name) {
        int n = variables.length;
        for (int i = 0; i < n; i++) {
            Variable v = variables[i];
            if (v.getName().equals(name))
                return v;
        }
        return null;
    }
    
    /**
     * @return a Collection of Variable objects representing the fields 
     *         defined by this class.
     */    
    public final Collection<Variable> getVariables() {
        return Arrays.asList(variables);
    }

    /**
     * @return the number of variables defined by this class.
     */    
    public final int getVariableCount() {
        return variables.length;
    }
    
    /**
     * Looks up a method by its name and type signature, as defined
     * by the Java Virtual Machine Specification, section 4.3.3.
     *
     * NOTE: this method only looks up methods defined by this class,
     * and not methods inherited from its superclass.
     *
     * @param name the name of the method
     * @param signature the method's type signature
     * @return the method, or null if no such method in this class.
     */
    public final Method getMethod(String name, String signature) {
        int n = methods.length;
        for (int i = 0; i < n; i++) {
            Method m = methods[i];
            if (m.getName().equals(name) && m.getDescriptor().equals(signature))
                return m;
        }
        return null;
    }
    
    /**
     * @return a Collection of Method objects representing the methods 
     *         defined by this class.
     */    
    public final Collection<Method> getMethods() {
        return Arrays.asList(methods);
    }
    
    /**
     * @return the number of methods defined by this class.
     */    
    public final int getMethodCount() {
        return methods.length;
    }
    
    /**
     * @return the name of the source file the compiler used to create this class.
     */    
    public final String getSourceFileName() {
	if (sourceFileName == null) {
	    DataInputStream in = attributes.getStream("SourceFile"); // NOI18N
	    if (in != null) {
		try {
		    int ipool = in.readUnsignedShort();
		    CPUTF8Info entry = (CPUTF8Info)constantPool.get(ipool);
		    sourceFileName = entry.getName();
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid SourceFile attribute", e);
		}
	    }
	}
        return sourceFileName;
    }
    
    public final boolean isDeprecated() {
	return attributes.get("Deprecated") != null;
    }

    public final boolean isSynthetic() {
        return (classAccess & Access.SYNTHETIC) == Access.SYNTHETIC ||
	    attributes.get("Synthetic") != null;
    }


    /**
     * Returns true if this class is an annotation type.
     * @return true if this class is an annotation type 
     */
    public final boolean isAnnotation() {
	return (classAccess & Access.ANNOTATION) == Access.ANNOTATION;
    }
            
    /**
     * Returns true if this class defines an enum type.
     * @return true if this class defines an enum type
     */
    public final boolean isEnum() {
	return (classAccess & Access.ENUM) == Access.ENUM;
    }
    
    /**
     * Returns true if this class defines a module.
     * @return true if this class defines a module
     * @since 1.51
     */
    public final boolean isModule() {
        return (classAccess & Access.MODULE) == Access.MODULE;
    }

    /**
     * Returns a map of the raw attributes for this classfile.  
     * Field attributes are
     * not returned in this map.
     * @return a map of attribute for this classfile
     * @see org.netbeans.modules.classfile.Field#getAttributes
     */
    public final AttributeMap getAttributes(){
        return attributes;
    }
    
    public final Collection<InnerClass> getInnerClasses(){
	if (innerClasses == null) {
	    DataInputStream in = attributes.getStream("InnerClasses"); // NOI18N
	    if (in != null) {
		try {
		    innerClasses = 
			InnerClass.loadInnerClasses(in, constantPool);
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid InnerClasses attribute", e);
		}
	    } else
		innerClasses = new InnerClass[0];
	}
        return Arrays.asList(innerClasses);
    }
    
    /**Return the content of the <code>BootstrapMethods</code> attribute.
     * 
     * @return the {@link List} of BootstrapMethod
     * @since 1.40
     */
    public final List<BootstrapMethod> getBootstrapMethods(){
	if (bootstrapMethods == null) {
	    DataInputStream in = attributes.getStream("BootstrapMethods"); // NOI18N
	    if (in != null) {
		try {
		    bootstrapMethods = 
			BootstrapMethod.loadBootstrapMethod(in, constantPool);
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid InnerClasses attribute", e);
		}
	    } else
		bootstrapMethods = new BootstrapMethod[0];
	}
        return Arrays.asList(bootstrapMethods);
    }

    /**
     * Returns the content of the <code>Module</code> attribute.
     * @return the {@link Module} or null when there is no <code>Module</code> attribute.
     * @since 1.51
     */
    public final Module getModule() {
        if (module == null) {
            final DataInputStream in = attributes.getStream("Module");  //NOI18N
            if (in != null) {
                try {
                    try {
                        module = new Module (in, constantPool);
                    } finally {
                        in.close();
                    }
                } catch (LegacyClassFile legacy) {
                    LOG.warning(legacy.getMessage());
                } catch (IOException e) {
                    throw new InvalidClassFileAttributeException("invalid Module attribute", e);
                }
            }
        }
        return module;
    }

    /**
       Returns the content of the <code>ModulePackages</code> attribute.
     * @return the {@link List} of packages or null when there is no <code>ModulePackages</code> attribute.
     * @since 1.53
     */
    public final List<String> getModulePackages() {
        if (modulePackages == null) {
            final DataInputStream in = attributes.getStream("ModulePackages");  //NOI18N
            if (in != null) {
                try {
                    try {
                        int cnt = in.readUnsignedShort();
                        String[] pkgs = new String[cnt];
                        for (int i=0; i < cnt; i++) {
                            pkgs[i] = ((CPPackageInfo)constantPool.get(in.readUnsignedShort())).getName();
                        }
                        modulePackages = Collections.unmodifiableList(Arrays.asList(pkgs));
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new InvalidClassFileAttributeException("invalid ModulePackages attribute", e);
                }
            }
        }
        return modulePackages;
    }

    /**
       Returns the content of the <code>ModuleMainClass</code> attribute.
     * @return the module main class or null when there is no <code>ModuleMainClass</code> attribute.
     * @since 1.53
     */
    public final ClassName getModuleMainClass() {
        if (moduleMainClz == null) {
            final DataInputStream in = attributes.getStream("ModuleMainClass");  //NOI18N
            if (in != null) {
                try {
                    try {
                        moduleMainClz = ((CPClassInfo)constantPool.get(in.readUnsignedShort())).getClassName();
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new InvalidClassFileAttributeException("invalid ModuleMainClass attribute", e);
                }
            }
        }
        return moduleMainClz;
    }

    /**
     * Returns the content of the <code>ModuleTarget</code> attribute.
     * @return the {@link ModuleTarget} or null when there is no <code>ModuleTarget</code> attribute.
     * @since 1.53
     */
    public final ModuleTarget getModuleTarget() {
        if (moduleTarget == null) {
            final DataInputStream in = attributes.getStream("ModuleTarget");  //NOI18N
            if (in != null) {
                try {
                    try {
                        moduleTarget = new ModuleTarget (in, constantPool);
                    } finally {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new InvalidClassFileAttributeException("invalid ModuleTarget attribute", e);
                }
            }
        }
        return moduleTarget;
    }

    /**
     * Returns the major version number of this classfile.
     * @return the major version number of this classfile
     */
    public int getMajorVersion() {
	return majorVersion;
    }

    /**
     * Returns the minor version number of this classfile.
     * @return the minor version number of this classfile
     */
    public int getMinorVersion() {
	return minorVersion;
    }

    /**
     * Returns the generic type information associated with this class.  
     * If this class does not have generic type information, then null 
     * is returned.
     * @return the generic type information associated with this class
     */
    public String getTypeSignature() {
	if (typeSignature == null) {
	    DataInputStream in = attributes.getStream("Signature"); // NOI18N
	    if (in != null) {
		try {
		    CPUTF8Info entry = 
			(CPUTF8Info)constantPool.get(in.readUnsignedShort());
		    typeSignature = entry.getName();
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid Signature attribute", e);
		}
	    }
	}
	return typeSignature;
    }

    /**
     * Returns the enclosing method for this class.  A class will have an
     * enclosing class if and only if it is a local class or an anonymous
     * class, and has been compiled with a compiler target level of 1.5 
     * or above.  If no such attribute is present in the classfile, then
     * null is returned.
     * @return enclosing method
     */
    public EnclosingMethod getEnclosingMethod() {
	if (enclosingMethod == null) {
	    DataInputStream in = 
		attributes.getStream("EnclosingMethod"); // NOI18N
	    if (in != null) {
		try {
		    int classIndex = in.readUnsignedShort();
		    int natIndex = in.readUnsignedShort();
		    CPEntry entry = constantPool.get(classIndex);
		    if (entry.getTag() == ConstantPool.CONSTANT_Class)
			enclosingMethod = 
			    new EnclosingMethod(constantPool, 
						(CPClassInfo)entry, 
						natIndex);
		    else
			; // JDK 1.5 beta1 bug
		    in.close();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid EnclosingMethod attribute", e);
		}
	    }
	}
	return enclosingMethod;
    }

    private void loadAnnotations() {
	if (annotations == null)
	    annotations = buildAnnotationMap(constantPool, attributes);
    }

    static Map<ClassName,Annotation> buildAnnotationMap(ConstantPool pool, AttributeMap attrs) {
	Map<ClassName,Annotation> annotations = new HashMap<ClassName,Annotation>(2);
	DataInputStream in = 
	    attrs.getStream("RuntimeVisibleAnnotations"); //NOI18N
	if (in != null) {
	    try {
		Annotation.load(in, pool, true, annotations);
		in.close();
	    } catch (IOException e) {
		throw new InvalidClassFileAttributeException("invalid RuntimeVisibleAnnotations attribute", e);
	    }
	}
	in = attrs.getStream("RuntimeInvisibleAnnotations"); //NOI18N
	if (in != null) {
	    try {
		Annotation.load(in, pool, false, annotations);
		in.close();
	    } catch (IOException e) {
		throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleAnnotations attribute", e);
	    }
	}
	return annotations;
    }

    /**
     * Returns all runtime annotations defined for this class.  Inherited
     * annotations are not included in this collection.
     * @return all runtine annotation
     */
    public final Collection<Annotation> getAnnotations() {
	loadAnnotations();
	return annotations.values();
    }

    /**
     * Returns the annotation for a specified annotation type, or null if
     * no annotation of that type exists for this class.
     * @param annotationClass type of annotation
     * @return annotation
     */
    public final Annotation getAnnotation(final ClassName annotationClass) {
	loadAnnotations();
	return annotations.get(annotationClass);
    }
    
    /**
     * Returns true if an annotation of the specified type is defined for
     * this class.
     * @param annotationClass type of annotation
     * @return true if annotation is present
     */
    public final boolean isAnnotationPresent(final ClassName annotationClass) {
	loadAnnotations();
	return annotations.get(annotationClass) != null;
    }
    
    /** Return the collection of all unique class references in this class.
     *
     * @return a Set of ClassNames specifying the referenced classnames.
     */
    public final Set<ClassName> getAllClassNames() {
        Set<ClassName> set = new HashSet<ClassName>();

        // include all class name constants from constant pool
        Collection<? extends CPClassInfo> c = constantPool.getAllConstants(CPClassInfo.class);
        for (Iterator<? extends CPClassInfo> i = c.iterator(); i.hasNext();) {
            CPClassInfo ci = i.next();
            set.add(ci.getClassName());
        }

	// scan variables and methods for other class references
	// (inner classes will caught above)
	for (int i = 0; i < variables.length; i++)
	    addClassNames(set, variables[i].getDescriptor());
	for (int i = 0; i < methods.length; i++)
	    addClassNames(set, methods[i].getDescriptor());

        return Collections.unmodifiableSet(set);
    }

    private void addClassNames(Set<ClassName> set, String type) {
        int i = 0;
        while ((i = type.indexOf('L', i)) != -1) {
            int j = type.indexOf(';', i);
            if (j > i) {
		// get name, minus leading 'L' and trailing ';'
                String classType = type.substring(i + 1, j);
		set.add(ClassName.getClassName(classType));
                i = j + 1;
            } else
		break;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ClassFile: "); //NOI18N
        sb.append(Access.toString(classAccess));
        sb.append(' ');
        sb.append(classInfo);
        if (isSynthetic())
            sb.append(" (synthetic)"); //NOI18N
        if (isDeprecated())
            sb.append(" (deprecated)"); //NOI18N
        sb.append("\n   source: "); //NOI18N
        sb.append(getSourceFileName());
        sb.append("\n   super: "); //NOI18N
        sb.append(superClassInfo);
	if (getTypeSignature() != null) {
	    sb.append("\n   signature: "); //NOI18N
	    sb.append(typeSignature);
	}
	if (getEnclosingMethod() != null) {
	    sb.append("\n   enclosing method: "); //NOI18N
	    sb.append(enclosingMethod);
	}
        sb.append("\n   ");
	loadAnnotations();
	if (annotations.size() > 0) {
	    Iterator<Annotation> iter = annotations.values().iterator();
	    sb.append("annotations: ");
	    while (iter.hasNext()) {
                sb.append("\n      ");
		sb.append(iter.next().toString());
	    }
	    sb.append("\n   ");
	}
        if (interfaces.length > 0) {
            sb.append(arrayToString("interfaces", interfaces)); //NOI18N
            sb.append("\n   ");
        }
        if (getInnerClasses().size() > 0) {
            sb.append(arrayToString("innerclasses", innerClasses)); //NOI18N
            sb.append("\n   ");
        }
        if (variables.length > 0) {
            sb.append(arrayToString("variables", variables)); //NOI18N
            sb.append("\n   ");
        }
        if (methods.length > 0)
            sb.append(arrayToString("methods", methods)); //NOI18N
        return sb.toString();
    }

    private String arrayToString(String name, Object[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(": ");
        int n = array.length;
        if (n > 0) {
            int i = 0;
            do {
                sb.append("\n      ");
                sb.append(array[i++].toString());
            } while (i < n);
        } else
            sb.append("none"); //NOI18N
        return sb.toString();
    }

    private static boolean isBadNonJavaClassName(final String name) {
        return name.length() == 1 && badNonJavaClassNames.contains(name);
    }
}
