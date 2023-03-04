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
import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.filters.InstrumentationFilter;
import org.netbeans.lib.profiler.global.CommonConstants;


/**
 * This class provides essentially a convenience static-method API that allows one to obtain a version of a particular
 * method, instrumented in a particular predefined way.
 *
 * @author Tomas Hurka
 * @author  Misha Dmitriev
 */
public class InstrumentationFactory implements CommonConstants {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static byte[] instrumentAsProiflePointHitMethod(DynamicClassInfo clazz, int methodIdx, int injType,
                                                           RuntimeProfilingPoint[] points) {
        Injector mi = new ProfilePointHitCallInjector(clazz, clazz.getBaseCPoolCount(injType), methodIdx, points,
                                                      CPExtensionsRepository.normalContents_ProfilePointHitMethodIdx);

        return mi.instrumentMethod();
    }

    public static byte[] instrumentAsReflectInvokeMethod(DynamicClassInfo clazz, int methodIdx) {
        Injector mi = new HandleReflectInvokeCallInjector(clazz, clazz.getBaseCPoolCount(INJ_REFLECT_METHOD_INVOKE), methodIdx);

        return mi.instrumentMethod();
    }

    public static byte[] instrumentAsServletDoMethod(DynamicClassInfo clazz, int methodIdx) {
        Injector mi = new HandleServletDoMethodCallInjector(clazz, clazz.getBaseCPoolCount(INJ_SERVLET_DO_METHOD), methodIdx);

        return mi.instrumentMethod();
    }

    public static byte[] instrumentCodeRegion(DynamicClassInfo clazz, int methodIdx, int bci0, int bci1) {
        Injector mi = new CodeRegionEntryExitCallsInjector(clazz, clazz.getBaseCPoolCount(INJ_CODE_REGION), methodIdx, bci0, bci1);

        return mi.instrumentMethod();
    }

    /** injType is either INJ_OBJECT_ALLOCATIONS or INJ_OBJECT_LIVENESS */
    public static byte[] instrumentForMemoryProfiling(DynamicClassInfo clazz, int methodIdx,
                                                      boolean[] allUnprofiledClassStatusArray, int injType,
                                                      RuntimeProfilingPoint[] points, InstrumentationFilter instrFilter,
                                                      boolean checkForOpcNew, boolean checkForOpcNewArray) {
        Injector mi = new ObjLivenessInstrCallsInjector(clazz, clazz.getBaseCPoolCount(injType), methodIdx,
                                                        allUnprofiledClassStatusArray, instrFilter,
                                                        checkForOpcNew, checkForOpcNewArray);
        mi.insertProfilingPoints(points, CPExtensionsRepository.memoryProfContents_ProfilePointHitMethodIdx);

        return mi.instrumentMethod();
    }

    /**
     * normalInjectionType is either INJ_RECURSIVE_NORMAL_METHOD or INJ_RECURSIVE_SAMPLED_NORMAL_METHOD
     * rootInjectionType is either INJ_RECURSIVE_ROOT_METHOD or INJ_RECURSIVE_SAMPLED_ROOT_METHOD
     */
    public static byte[] instrumentMethod(DynamicClassInfo clazz, int methodIdx, int normalInjectionType, int rootInjectionType,
                                          int markerInjectionType, int methodId, RuntimeProfilingPoint[] points) {
        int baseCPCount0 = clazz.getBaseCPoolCount(normalInjectionType);
        int baseCPCount1;
        int injType;

        if (clazz.isMethodRoot(methodIdx)) {
            baseCPCount1 = clazz.getBaseCPoolCount(rootInjectionType);
            injType = rootInjectionType;
        } else if (clazz.isMethodMarker(methodIdx)) {
            baseCPCount1 = clazz.getBaseCPoolCount(markerInjectionType);
            injType = markerInjectionType;
        } else {
            baseCPCount1 = 0;
            injType = normalInjectionType;
        }

        Injector mi = new MethodEntryExitCallsInjector(clazz, baseCPCount0, baseCPCount1, methodIdx, injType, methodId);
        mi.insertProfilingPoints(points, CPExtensionsRepository.normalContents_ProfilePointHitMethodIdx);

        byte[] res = mi.instrumentMethod();
        clazz.setInstrMethodId(methodIdx, methodId);

        return res;
    }
}
