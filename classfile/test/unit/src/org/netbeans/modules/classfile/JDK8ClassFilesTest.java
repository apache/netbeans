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

import java.io.InputStream;
import java.util.Arrays;
import junit.framework.TestCase;
import org.netbeans.modules.classfile.CPMethodHandleInfo.ReferenceKind;

/**
 *
 * @author lahvac
 */
public class JDK8ClassFilesTest extends TestCase {
    
    public JDK8ClassFilesTest(String testName) {
        super(testName);
    }
    
    public void testDefenderMethods() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/WithLambda.classx");
        ClassFile classFile = new ClassFile(classData);
        CPInvokeDynamicInfo invokeDynamic = (CPInvokeDynamicInfo) classFile.getConstantPool().get(2);
        assertEquals(ConstantPool.CONSTANT_InvokeDynamic, invokeDynamic.getTag());
        BootstrapMethod bootstrapMethod = classFile.getBootstrapMethods().get(invokeDynamic.getBootstrapMethod());
        assertEquals("[23, 24, 25]", Arrays.toString(bootstrapMethod.getArguments()));
        CPMethodHandleInfo bootstrapMH = (CPMethodHandleInfo) classFile.getConstantPool().get(bootstrapMethod.getMethodRef());
        assertEquals(ConstantPool.CONSTANT_MethodHandle, bootstrapMH.getTag());
        assertEquals(ReferenceKind.invokeStatic, bootstrapMH.getReferenceKind());
        CPMethodInfo bootstrapMethodInfo = (CPMethodInfo) classFile.getConstantPool().get(bootstrapMH.getReference());
        assertEquals("CallSite metaFactory(MethodHandles$Lookup,String,MethodType,MethodHandle,MethodHandle,MethodType)", bootstrapMethodInfo.getFullMethodName());
        CPNameAndTypeInfo nameAndType = (CPNameAndTypeInfo) classFile.getConstantPool().get(invokeDynamic.getNameAndType());
        assertEquals("()Ljava/lang/Runnable;", nameAndType.getDescriptor());
        assertEquals("lambda", nameAndType.getName());
        CPMethodTypeInfo methodType = (CPMethodTypeInfo) classFile.getConstantPool().get(25);
        assertEquals(ConstantPool.CONSTANT_MethodType, methodType.getTag());
        CPUTF8Info descriptor = (CPUTF8Info) classFile.getConstantPool().get(methodType.getDescriptor());
        assertEquals("()V", descriptor.getName());
    }
}