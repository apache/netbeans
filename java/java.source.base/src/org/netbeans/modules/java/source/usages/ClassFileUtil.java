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

package org.netbeans.modules.java.source.usages;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.Convert.Validation;
import com.sun.tools.javac.util.InvalidUtfException;
import com.sun.tools.javac.util.Name;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.netbeans.modules.classfile.ByteCodes;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ConstantPool;
import org.netbeans.modules.classfile.Method;

/**
 *
 * @author Tomas Zezula
 */
public class ClassFileUtil {
    private static final Logger log = Logger.getLogger(ClassFileUtil.class.getName());

    /** Creates a new instance of ClassFileUtil */
    private ClassFileUtil() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }
    
    
    public static boolean accessesFiledOrMethod (final String[] fieldInfo, final String[] methodInfo, final Code c, final ConstantPool cp) {                
        BytecodeDecoder bd = new BytecodeDecoder (c.getByteCodes());
        for (byte[] iw : bd) {
            switch (((int)iw[0]&0xff)) {
                case ByteCodes.bc_putstatic:
                case ByteCodes.bc_getstatic:
                case ByteCodes.bc_putfield:
                case ByteCodes.bc_getfield:
                    if (fieldInfo != null) {
                        int cpIndex = BytecodeDecoder.toInt(iw[1],iw[2]);                        
                        CPFieldInfo cpFieldInfo = (CPFieldInfo) cp.get(cpIndex);
                        String className = cpFieldInfo.getClassName().getInternalName();
                        String fieldName = cpFieldInfo.getFieldName();
                        String signature = cpFieldInfo.getDescriptor();                           
                        if (fieldInfo[0].equals(className) &&
                            (fieldInfo[1] == null || (fieldInfo[1].equals(fieldName) && fieldInfo[2].equals(signature)))) {
                            return true;
                        }
                    }
                    break;
                case ByteCodes.bc_invokevirtual:
                case ByteCodes.bc_invokestatic:
                case ByteCodes.bc_invokeinterface:                       
                case ByteCodes.bc_invokespecial:
                    if (methodInfo != null) {
                        int cpIndex = BytecodeDecoder.toInt(iw[1],iw[2]);
                        CPMethodInfo cpMethodInfo = (CPMethodInfo) cp.get(cpIndex);
                        String className = cpMethodInfo.getClassName().getInternalName();
                        String methodName = cpMethodInfo.getMethodName();
                        String signature = cpMethodInfo.getDescriptor();                            
                        if (methodInfo[0].equals(className) &&
                            (methodInfo[1] == null || (methodInfo[1].equals(methodName) && methodInfo[2].equals(signature)))) {
                            return true;
                        }
                    }
                    break;
            }                
        }
        return false;
    }
    
    public static boolean accessesFiledOrMethod (final String[] fieldInfo, final String[] methodInfo, final Method m) {
        Code c = m.getCode();
        if (c != null) {
            ConstantPool cp = m.getClassFile().getConstantPool();
            return accessesFiledOrMethod(fieldInfo,methodInfo, c, cp);
        }
        else {
            return false;
        }
    }
    
    public static <T extends Method> Collection<T> accessesFiled (final String[] fieldInfo, final Collection<T> methods) {
        Collection<T> result = new LinkedList<T> ();
        for (T m : methods) {
            if (accessesFiledOrMethod(fieldInfo,null,m)) {
                result.add(m);
            }
        }
        return result;
    }
    
    public static <T extends Method> Collection<T> callsMethod (final String[] methodInfo, final Collection<T> methods) {
        Collection<T> result = new LinkedList<T> ();
        for (T m : methods) {
            if (accessesFiledOrMethod(null,methodInfo,m)) {                
                result.add(m);
            }
        }
        return result;
    }        
    
    
    public static String[] createFieldDescriptor (final VariableElement ve) {
	assert ve != null;
        String[] result = new String[3];
	Element enclosingElement = ve.getEnclosingElement();
        if (enclosingElement != null && enclosingElement.asType().getKind() == TypeKind.NONE) {
            result[0] = "";  //NOI18N
        } else {
	    assert enclosingElement instanceof TypeElement;
            result[0] = encodeClassNameOrArray ((TypeElement) enclosingElement);
        }
        result[1] = ve.getSimpleName().toString();
        StringBuilder sb = new StringBuilder ();
        encodeType(ve.asType(),sb);
        result[2] = sb.toString();        
        return result;
    }        
    
    public static String[] createExecutableDescriptor (final ExecutableElement ee) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Calling createExecutableDescriptor: ExecutableElement = {0}", ee); // NOI18N
        assert ee != null && ee.asType() != null : "Wrong executable element: " + ee; //NOI18N
        final ElementKind kind = ee.getKind();
        final String[] result = (kind == ElementKind.STATIC_INIT || kind == ElementKind.INSTANCE_INIT) ? new String[2] : new String[3];
        final Element enclosingType = ee.getEnclosingElement();
        if (enclosingType != null && enclosingType.asType().getKind() == TypeKind.NONE) {
            result[0] = ""; //NOI18N
        } else {
	    assert enclosingType instanceof TypeElement : enclosingType == null ? "null" : enclosingType.toString() + "(" + enclosingType.getKind()+")"; //NOI18N
            result[0] = encodeClassNameOrArray ((TypeElement)enclosingType);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Result of encodeClassNameOrArray = {0}", result[0]);    // NOI18N
        if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
            final StringBuilder retType = new StringBuilder ();
            if (kind == ElementKind.METHOD) {
                result[1] = ee.getSimpleName().toString();
                if (ee.asType().getKind() == TypeKind.EXECUTABLE) {
                    encodeType(ee.getReturnType(), retType);
                }
            }
            else {
                result[1] = "<init>";   // NOI18N
                retType.append('V');    // NOI18N
            }
            StringBuilder sb = new StringBuilder ();
            sb.append('(');             // NOI18N
            for (VariableElement pd : ee.getParameters()) {
                encodeType(pd.asType(),sb);
            }
            sb.append(')');             // NOI18N
            sb.append(retType);
            result[2] = sb.toString();
        }
        else if (kind == ElementKind.INSTANCE_INIT) {
            result[1] = "<init>";       // NOI18N
        } 
        else if (kind == ElementKind.STATIC_INIT) {
            result[1] = "<cinit>";      // NOI18N
        }
        else {
            throw new IllegalArgumentException ();
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "Result of createExecutableDescriptor = {0}", Arrays.toString(result)); // NOI18N
        return result;
    }
    
    public static String encodeClassNameOrArray (TypeElement td) {
        assert td != null;
        CharSequence qname = td.getQualifiedName();
        TypeMirror enclosingType = td.getEnclosingElement().asType();
        if (qname != null && enclosingType != null && enclosingType.getKind() == TypeKind.NONE && "Array".equals(qname.toString())) {     //NOI18N
            return "[";  //NOI18N
        }
        else {
            return encodeClassName(td);
        }
    }
    
    public static String encodeClassName (TypeElement td) {
        assert td != null;
        StringBuilder sb = new StringBuilder ();
        encodeClassName(td, sb,'.');    // NOI18N
        return sb.toString();
    }
    
    private static void encodeType (final TypeMirror type, final StringBuilder sb) {
	switch (type.getKind()) {
	    case VOID:
		sb.append('V');	    // NOI18N
		break;
	    case BOOLEAN:
		sb.append('Z');	    // NOI18N
		break;
	    case BYTE:
		sb.append('B');	    // NOI18N
		break;
	    case SHORT:
		sb.append('S');	    // NOI18N
		break;
	    case INT:
		sb.append('I');	    // NOI18N
		break;
	    case LONG:
		sb.append('J');	    // NOI18N
		break;
	    case CHAR:
		sb.append('C');	    // NOI18N
		break;
	    case FLOAT:
		sb.append('F');	    // NOI18N
		break;
	    case DOUBLE:
		sb.append('D');	    // NOI18N
		break;
	    case ARRAY:
		sb.append('[');	    // NOI18N
		assert type instanceof ArrayType;
		encodeType(((ArrayType)type).getComponentType(),sb);
		break;
	    case DECLARED:
            {
                sb.append('L');	    // NOI18N
                TypeElement te = (TypeElement) ((DeclaredType)type).asElement();
                encodeClassName(te, sb,'/');
                sb.append(';');	    // NOI18N
                break;
            }
	    case TYPEVAR:
            {
		assert type instanceof TypeVariable;
		TypeVariable tr = (TypeVariable) type;
		TypeMirror upperBound = tr.getUpperBound();
		if (upperBound.getKind() == TypeKind.NULL) {
		    sb.append ("Ljava/lang/Object;");       // NOI18N
		}
		else {
		    encodeType(upperBound, sb);
		}
		break;
            }
            case ERROR:
            {                
                TypeElement te = (TypeElement) ((DeclaredType)type).asElement();
                if (te != null) {
                    sb.append('L');
                    encodeClassName(te, sb,'/');
                    sb.append(';');	    // NOI18N
                    break;
                }                
            }
            case INTERSECTION:
            {
                encodeType(((IntersectionType) type).getBounds().get(0), sb);
                break;
            }
	    default:
		throw new IllegalArgumentException (
                    String.format(
                        "Unsupported type: %s, kind: %s",   //NOI18N
                        type,
                        type.getKind()));
	}                
    }        
            
    public static void encodeClassName (TypeElement te, final StringBuilder sb, final char separator) {
        Name name = ((Symbol.ClassSymbol)te).flatname;
        assert name != null;
        final char[] nameChars = name.toString().toCharArray();
        int charLength = nameChars.length;
        if (separator != '.') {         //NOI18N
            for (int i=0; i<charLength; i++) {
                if (nameChars[i] == '.') {  //NOI18N
                    nameChars[i] = separator;
                }
            }
        }
        sb.append(nameChars,0,charLength);
    }
    
    /**
     * Returns ClassName for jvmType which represents a classref
     * otherwise it returns null;
     */
    public static ClassName getType (final String jvmTypeId) {
        //Both L... (classref) and [L... (array of classref) are important 
        if (jvmTypeId.length()<2) {
            return null; // no classref - simple types
        }
        else if (jvmTypeId.charAt(0) == 'L') {
            //classref
            return ClassName.getClassName(jvmTypeId);
        }
        else if (jvmTypeId.charAt(0) == '[') {
            //Array of classref
            return getType (jvmTypeId.substring(1));
        }
        //No classref - array of simple types
        return null;
    }
    
    public static ClassName getType (final CPClassInfo ci) {
        String type = ci.getName();
        if (type.charAt(0)=='[') {  //NOI18N
            return getType(type);
        }
        else {
            return ci.getClassName();
        }
    }
    
    public static ClassName[] getTypesFromMethodTypeSignature (final String jvmTypeId) {
        Set<ClassName> result = new HashSet<ClassName> ();
        methodTypeSignature (jvmTypeId, new int[] {0}, result);        
        return result.toArray(new ClassName[0]);
    }
    
    public static ClassName[] getTypesFromFiledTypeSignature (final String jvmTypeId) {
        Set<ClassName> result = new HashSet<ClassName> ();
        typeSignatureType (jvmTypeId, new int[] {0}, result, false);        
        return result.toArray(new ClassName[0]);
    }
    
    public static ClassName[] getTypesFromClassTypeSignature (final String jvmTypeId) {
        Set<ClassName> result = new HashSet<ClassName> ();
        classTypeSignature (jvmTypeId, new int[] {0}, result);        
        return result.toArray(new ClassName[0]);
    }
    
    private static char getChar (final String buffer, final int pos) {
        if (pos>=buffer.length()) {
            throw new IllegalStateException ();
        }
        return buffer.charAt(pos);        
    }
    
    private static void classTypeSignature (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {
        char c = getChar (jvmTypeId, pos[0]);                
        if (c == '<') {            
            formalTypeParameters (jvmTypeId, pos, s);
            c = getChar (jvmTypeId, pos[0]);            
        }
        typeSignatureType (jvmTypeId, pos, s, false);
        while (pos[0]<jvmTypeId.length()) {
            typeSignatureType (jvmTypeId, pos, s, false);
        }
    }
    
    private static void methodTypeSignature (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {                
        char c = getChar (jvmTypeId, pos[0]);                
        if (c == '<') {            
            formalTypeParameters (jvmTypeId, pos, s);
            c = getChar (jvmTypeId, pos[0]);            
        }
        if (c!='(') {
            throw new IllegalStateException (jvmTypeId);
        }        
        pos[0]++;
        c = getChar (jvmTypeId, pos[0]);        
        while (c != ')') {
            typeSignatureType (jvmTypeId, pos, s, false);
            c = getChar (jvmTypeId, pos[0]);        
        }
        pos[0]++;
        typeSignatureType (jvmTypeId, pos, s, false);  //returnType
        //Todo: Exceptions
    }
    
    
    private static void formalTypeParam (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {
        //Identifier:[className][:ifaceName]*
        char c;
        do {
            c = getChar(jvmTypeId, pos[0]++);            
        } while (c!=':');
        c = getChar(jvmTypeId, pos[0]);        
        if (c !=':') {
            typeSignatureType(jvmTypeId, pos, s, true);
            c = getChar (jvmTypeId, pos[0]);            
        }
        while (c == ':') {
            pos[0]++;
            typeSignatureType(jvmTypeId, pos, s, true);
            c = getChar (jvmTypeId, pos[0]);            
        }
    }
    
    
    
    private static void formalTypeParameters (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {
        char c = getChar(jvmTypeId, pos[0]++);
        if (c != '<') {
            throw new IllegalArgumentException (jvmTypeId);
        }
        c = getChar (jvmTypeId, pos[0]);
        while (c !='>') {            
            formalTypeParam (jvmTypeId, pos, s);
            c = getChar (jvmTypeId, pos[0]);
        }
        pos[0]++;
    }
    
    private static void typeArgument (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {
        char c = getChar (jvmTypeId, pos[0]);
        if (c == '*') {
            pos[0]++;
            return;
        }
        else if (c == '+' || c == '-') {
            pos[0]++;
            typeSignatureType (jvmTypeId, pos, s, true);
        }        
        else {
            typeSignatureType (jvmTypeId, pos, s, true);
        }
    }
    
    
    private static void typeArgumentsList (final String jvmTypeId, final int[] pos, final Set<ClassName> s) {
        char c = getChar (jvmTypeId, pos[0]++);
        if (c != '<') {
            throw new IllegalStateException (jvmTypeId);            
        }        
        c = getChar (jvmTypeId, pos[0]);
        while (c !='>') {            
            typeArgument (jvmTypeId, pos, s);
            c = getChar (jvmTypeId, pos[0]);
        }
        pos[0]++;
    }

    private static void typeSignatureType (final String jvmTypeId, final int[] pos, final Set<ClassName> s, boolean add) {
        char c = getChar(jvmTypeId, pos[0]++);                
        switch (c) {            
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':            
            case 'S':
            case 'V':
            case 'Z':
                //Do nothing
                break;
            case 'L':
                StringBuilder builder = new StringBuilder ();
                do {
                    builder.append (c);
                    c = getChar(jvmTypeId, pos[0]);                    
                    if (c=='<') {
                        typeArgumentsList (jvmTypeId,pos, s);                        
                        c = getChar(jvmTypeId, pos[0]++);   //;                        
                    }
                    else {
                        pos[0]++;
                    }
                } while (c != ';');
                builder.append (c);
                if (add) {
                    s.add(ClassName.getClassName(builder.toString()));
                }
                break;
            case 'T':
                do {
                    c = getChar(jvmTypeId, pos[0]++);                    
                } while (c != ';');
                break;
            case '[':
                typeSignatureType (jvmTypeId, pos, s, add);
                break;                
        }
    }    
    
}
