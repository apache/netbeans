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

package org.netbeans.lib.profiler.instrumentation;

import org.netbeans.lib.profiler.classfile.DynamicClassInfo;


/**
 * Specialized subclass of Injector, that provides injection of our standard handleJavaLangReflectMethodInvoke(Method method)
 * call into the java.lang.reflect.Method.invoke() method.
 *
 *  @author Misha Dmitriev
 */
class HandleReflectInvokeCallInjector extends SpecialCallInjector {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HandleReflectInvokeCallInjector(DynamicClassInfo clazz, int baseCPoolCount, int methodIdx) {
        super(clazz, baseCPoolCount, methodIdx);
        targetMethodIdx = CPExtensionsRepository.miContents_HandleReflectInvokeMethodIdx + baseCPoolCount;
        initializeInjectedCode();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    private void initializeInjectedCode() {
        injectedCodeLen = 4;
        injectedCode = new byte[injectedCodeLen];
        injectedCode[0] = (byte) opc_aload_0;
        injectedCode[1] = (byte) opc_invokestatic;
        // Positions 2, 3 are occupied by method index
        injectedCodeMethodIdxPos = 2;
    }
}
