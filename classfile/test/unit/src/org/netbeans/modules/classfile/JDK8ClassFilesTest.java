/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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