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

package org.netbeans.modules.cnd.discovery.api;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface ItemProperties {

    /**
     * List of user include paths
     */
    List<String> getUserInludePaths();

    /**
     * List of user include files
     */
    List<String> getUserInludeFiles();

    /**
     * List of system include paths
     */
    List<String> getSystemInludePaths();

    /**
     * List of user macros
     */
    Map<String,String> getUserMacros();

    /**
     * List of undefined macros
     */
    List<String> getUndefinedMacros();

    /**
     * List of system predefined macros
     */
    Map<String,String> getSystemMacros();
    
    /**
     * Language kind
     */
    LanguageKind getLanguageKind();

    /**
     * Language kind
     */
    LanguageStandard getLanguageStandard();

    /**
     * Compiler name (producer)
     */
    String getCompilerName();

    public enum LanguageKind {
        Unknown,
        C,
        CPP,
        Fortran
    }

    public enum LanguageStandard {
        Unknown,
        C, C89, C99, C11,
        CPP98, CPP11, CPP14, CPP17,
        F77, F90, F95,
        Default
    }
}
