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
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

public interface SunCmpMappings extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {

    public static final String VERSION_1_0 = "1.0"; //NOI18N
    public static final String VERSION_1_1 = "1.1"; //NOI18N
    public static final String VERSION_1_2 = "1.2"; //NOI18N

    public static final String SUN_CMP_MAPPING = "SunCmpMapping"; // NOI18N

    public void setSunCmpMapping(int index, SunCmpMapping value);
    public SunCmpMapping getSunCmpMapping(int index);
    public int sizeSunCmpMapping();
    public void setSunCmpMapping(SunCmpMapping[] value);
    public SunCmpMapping[] getSunCmpMapping();
    public int addSunCmpMapping(SunCmpMapping value);
    public int removeSunCmpMapping(SunCmpMapping value);
    public SunCmpMapping newSunCmpMapping();

}
