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

package org.netbeans.lib.profiler.common;


/**
 * Access of global profiling settings (settings such as communication port number or working directory,
 * that are independent of the kind of profiling, such as CPU or Memory).
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 * @author Misha Dmitriev
 */
public interface GlobalProfilingSettings {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    void setCalibrationPortNo(final int value);

    int getCalibrationPortNo();

    /** @param value Name of Java platform to use for profiling. Null value indicates no global platform is selected */
    void setJavaPlatformForProfiling(String value);

    /** @return Name of Java platform to use for profiling. Null value indicates no global platform is selected */
    String getJavaPlatformForProfiling();

    void setPortNo(final int value);

    int getPortNo();
}
