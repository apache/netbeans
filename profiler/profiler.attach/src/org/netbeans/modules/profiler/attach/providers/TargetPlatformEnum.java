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

package org.netbeans.modules.profiler.attach.providers;

import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Jaroslav Bachorik
 */
public class TargetPlatformEnum {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String[] jvmNames = new String[] {
                                                 IntegrationUtils.PLATFORM_JAVA_50, IntegrationUtils.PLATFORM_JAVA_60,
                                                 IntegrationUtils.PLATFORM_JAVA_70, IntegrationUtils.PLATFORM_JAVA_80, 
                                                 IntegrationUtils.PLATFORM_JAVA_90, IntegrationUtils.PLATFORM_JAVA_110_BEYOND,
                                                 IntegrationUtils.PLATFORM_JAVA_CVM
                                             };
    public static final TargetPlatformEnum JDK5 = new TargetPlatformEnum(0);
    public static final TargetPlatformEnum JDK6 = new TargetPlatformEnum(1);
    public static final TargetPlatformEnum JDK7 = new TargetPlatformEnum(2);
    public static final TargetPlatformEnum JDK8 = new TargetPlatformEnum(3);
    public static final TargetPlatformEnum JDK9 = new TargetPlatformEnum(4); //TODO - search!
    public static final TargetPlatformEnum JDK110_BEYOND = new TargetPlatformEnum(5);
    public static final TargetPlatformEnum JDK_CVM = new TargetPlatformEnum(6);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int jvmIndex = 0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private TargetPlatformEnum(int index) {
        this.jvmIndex = index;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean equals(Object obj) {
        if (!(obj instanceof TargetPlatformEnum)) {
            return false;
        }

        return ((TargetPlatformEnum) obj).jvmIndex == this.jvmIndex;
    }

    public static Iterator iterator() {
        List<TargetPlatformEnum> jvmList = new ArrayList<>(7);
        jvmList.add(JDK5);
        jvmList.add(JDK6);
        jvmList.add(JDK7);
        jvmList.add(JDK8);
        jvmList.add(JDK9);
        jvmList.add(JDK110_BEYOND);
        jvmList.add(JDK_CVM);

        return jvmList.listIterator();
    }

    public String toString() {
        return jvmNames[this.jvmIndex];
    }
}
