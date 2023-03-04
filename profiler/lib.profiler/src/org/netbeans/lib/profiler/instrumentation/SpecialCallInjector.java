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
import org.netbeans.lib.profiler.global.CommonConstants;


/**
 * Specialized subclass of Injector, that provides special injection of java.lang.Method.invoke() and HttpServer.do*()
 * methods.
 *
 *  @author Tomas Hurka
 */
abstract class SpecialCallInjector extends Injector implements CommonConstants {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    byte[] injectedCode;
    int injectedCodeLen;
    int injectedCodeMethodIdxPos;
    int targetMethodIdx;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    SpecialCallInjector(DynamicClassInfo clazz, int baseCPoolCount, int methodIdx) {
        super(clazz, methodIdx);
        this.baseCPoolCount = baseCPoolCount;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public byte[] instrumentMethod() {
        putU2(injectedCode, injectedCodeMethodIdxPos, targetMethodIdx);

        injectCodeAndRewrite(injectedCode, injectedCodeLen, 0, true);

        // Done very conservatively
        maxStack += 1;

        return createPackedMethodInfo();
    }
}
