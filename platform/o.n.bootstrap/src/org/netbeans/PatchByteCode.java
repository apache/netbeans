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

package org.netbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.openide.modules.PatchedPublic;

/**
 * Tool to patch bytecode, currently just to make access modifiers public.
 * Determines when and what to patch based on class-retention annotations.
 * @see PatchedPublic
 * @see #patch
 */
public final class PatchByteCode {
    private static final String DISABLE_PATCHING = PatchByteCode.class.getName() + ".disable"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(PatchByteCode.class.getName());
    
    private static final byte[] RUNTIME_INVISIBLE_ANNOTATIONS, PATCHED_PUBLIC;
    private static final String DESC_CTOR_ANNOTATION = "Lorg/openide/modules/ConstructorDelegate;";
    private static final String DESC_PATCHED_PUBLIC_ANNOTATION = "Lorg/openide/modules/PatchedPublic;";
    private static final String DESC_DEFAULT_CTOR = "()V";
    private static final String CONSTRUCTOR_NAME = "<init>"; // NOI18N
    private static final String PREFIX_EXTEND = "extend."; // NOI18N
    
    static {
        try {
            RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations".getBytes("UTF-8"); // NOI18N
            PATCHED_PUBLIC = DESC_PATCHED_PUBLIC_ANNOTATION.getBytes("UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException x) {
            throw new ExceptionInInitializerError(x);
        }
    }
    
    /**
     * Shared instance, which does just nothing
     */
    private static final PatchByteCode NOP = new PatchByteCode(false, null, null);
    
    /**
     * Shared instance, that performs a very fast PatchedPublic patch on the loaded class
     */
    private static final PatchByteCode PUBLIC_ONLY = new PatchByteCode(true, null, null);
    
    private final boolean patchPublic;
    private final Map<String, String> classToExtend;
    private final ClassLoader theClassLoader;
    
    private PatchByteCode() {
        this(false, null, null);
    }
    
    private PatchByteCode(boolean pub, Map<String, String> classToExtend, ClassLoader ldr) {
        this.patchPublic = pub;
        this.classToExtend = classToExtend;
        this.theClassLoader = ldr;
    }
    
    private void load(URL stream) throws IOException {
        try (InputStream istm = stream.openStream()) {
            Properties props = new Properties();
            props.load(new InputStreamReader(istm, "UTF-8")); // NOI18N
            
            @SuppressWarnings("unchecked")
            Enumeration<String> en = (Enumeration<String>)props.propertyNames();
            
            while (en.hasMoreElements()) {
                String pn = en.nextElement();
                if (pn.startsWith(PREFIX_EXTEND)) {
                    String toExtend = pn.substring(PREFIX_EXTEND.length());
                    String extendWith = props.getProperty(pn);
                    
                    String old;
                    
                    if ((old = classToExtend.put(toExtend, extendWith)) != null) {
                        throw new IOException("Multiple extend instructions for class" + toExtend + ": " + extendWith + " and " + old);
                    }
                }
            }
        }
    }
    
    private PatchByteCode purify() {
        if (classToExtend == null || classToExtend.isEmpty()) {
            return PUBLIC_ONLY;
        } else {
            return this;
        }
    }
    
    static PatchByteCode fromStream(Enumeration<URL> streams, ClassLoader ldr) {
        if (System.getProperty(DISABLE_PATCHING) != null) {
            return NOP;
        }
        PatchByteCode pb = new PatchByteCode(false, new HashMap<String, String>(3), ldr);
        boolean found = false;
        while (streams.hasMoreElements()) {
            URL stream = streams.nextElement();
            try {
                pb.load(stream);
            } catch (IOException ex) {
                // TODO: log
            }
            found = true;
        }
        
        return found ? pb.purify() : NOP;
    }

    byte[] apply(String className, byte[] data) throws IOException {
        if (patchPublic) {
            return patch(data);
        } else if (classToExtend == null) {
            return data;
        }
        // more thorough analysis is needed.
        String extender = classToExtend.get(className);
        if (extender == null) {
            return patch(data);
        }
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        if (l == null) {
            l = PatchByteCode.class.getClassLoader();
        }
        try {
            return (byte[]) patchAsmMethod(l).invoke(null, data, extender, theClassLoader);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Method patchAsmMethod;
    private Method patchAsmMethod(ClassLoader l) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        if (patchAsmMethod == null) {
            Class<?> asm = Class.forName("org.netbeans.core.startup.Asm", true, l);
            patchAsmMethod = asm.getDeclaredMethod("patch", byte[].class, String.class, ClassLoader.class);
            patchAsmMethod.setAccessible(true);
        }
        return patchAsmMethod;
    }
    /**
     * Patches a class if it is needed.
     * @param arr the bytecode
     * @return the enhanced bytecode
     */
    public static byte[] patch(byte[] data) {
        int constant_pool_count = u2(data, /* magic + minor_version + major_version */ 8);
        int[] constantPoolOffsets = new int[constant_pool_count];
        int pos = 10; // 8 + constant_pool_count
        for (int i = 1; i < constant_pool_count; i++) {
            int tag = u1(data, pos++);
            //System.err.println("tag " + tag + " at #" + i + " at location " + pos);
            constantPoolOffsets[i] = pos;
            switch (tag) {
            case 1: // CONSTANT_Utf8
                int len = u2(data, pos);
                //try {System.err.println("UTF-8 constant: " + new String(data, pos + 2, len, "UTF-8"));} catch (UnsupportedEncodingException x) {}
                pos += len + 2;
                break;
            case 3: // CONSTANT_Integer
            case 4: // CONSTANT_Float
            case 9: // CONSTANT_Fieldref
            case 10: // CONSTANT_Methodref
            case 11: // CONSTANT_InterfaceMethodref
            case 12: // CONSTANT_NameAndType
            case 17:    //CONSTANT_ConstantDynamic
            case 18:    //CONSTANT_InvokeDynamic
                pos += 4;
                break;
            case 7: // CONSTANT_Class
            case 8: // CONSTANT_String
            case 16:    //CONSTANT_MethodType
            case 19:    //CONSTANT_Module
            case 20:    //CONSTANT_Package
                pos += 2;
                break;
            case 5: // CONSTANT_Long
            case 6: // CONSTANT_Double
                pos += 8;
                i++; // next entry is ignored
                break;
            case 15: //CONSTANT_MethodHandle
                pos +=3;
                break;
            default:
                throw new IllegalArgumentException("illegal constant pool tag " + tag + " at index " + i + " out of " + constant_pool_count);
            }
        }
        pos += 6; // access_flags + this_class + super_class
        int interfaces_count = u2(data, pos);
        pos += 2; // interfaces_count
        pos += 2 * interfaces_count; // interfaces
        int fields_count = u2(data, pos);
        pos += 2; // fields_count
        for (int i = 0; i < fields_count; i++) {
            pos += 6; // access_flags + name_index + descriptor_index
            int attributes_count = u2(data, pos);
            pos += 2; // attributes_count
            for (int j = 0; j < attributes_count; j++) {
                pos += 2; // attribute_name_index
                int attribute_length = u4(data, pos);
                pos += 4; // attribute_length
                pos += attribute_length; // info
            }
        }
        int methods_count = u2(data, pos);
        pos += 2; // methods_count
        for (int i = 0; i < methods_count; i++) {
            int locationOfAccessFlags = pos;
            pos += 6; // access_flags + name_index + descriptor_index
            int attributes_count = u2(data, pos);
            pos += 2; // attributes_count
            for (int j = 0; j < attributes_count; j++) {
                int locationOfAttributeName = constantPoolOffsets[u2(data, pos)];
                pos += 2; // attribute_name_index
                int attribute_length = u4(data, pos);
                pos += 4; // attribute_length
                if (utf8Matches(data, locationOfAttributeName, RUNTIME_INVISIBLE_ANNOTATIONS)) {
                    int num_annotations = u2(data, pos);
                    int pos2 = pos + 2; // num_annotations
                    for (int k = 0; k < num_annotations; k++) {
                        if (utf8Matches(data, constantPoolOffsets[u2(data, pos2)], PATCHED_PUBLIC)) {
                            // Got it, we are setting the method to be public.
                            data[locationOfAccessFlags + 1] &= 0xF9; // - ACC_PRIVATE - ACC_PROTECTED
                            data[locationOfAccessFlags + 1] |= 0x01; // + ACC_PUBLIC
                        }
                        // XXX skip over annotation body so we can support >1 annotation on the member
                        // (i.e. @PatchedPublic occurs only after other annotations)
                        // but it is tedious to calculate the length of element_value structs
                        continue;
                    }
                }
                pos += attribute_length; // info
            }
        }
        return data;
    }

    private static int u1(byte[] data, int off) {
        byte b = data[off];
        return b >= 0 ? b : b + 256;
    }
    private static int u2(byte[] data, int off) {
        return (u1(data, off) << 8) + u1(data, off + 1);
    }
    private static int u4(byte[] data, int off) {
        return (u2(data, off) << 16) + u2(data, off + 2);
    }

    private static boolean utf8Matches(byte[] data, int off, byte[] expected) {
        if (u2(data, off) != expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[off + 2 + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
    
}
