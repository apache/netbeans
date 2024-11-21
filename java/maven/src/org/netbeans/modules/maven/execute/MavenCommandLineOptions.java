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
package org.netbeans.modules.maven.execute;

import java.util.Set;

/**
 *
 * @author Dusan Petrovic
 */
public class MavenCommandLineOptions {
    
    private static final Set<String> OPTIONS_WITH_VALUES = Set.of(
            "activate-profiles",  
            "builder",  
            "color",  
            "define",  
            "encrypt-master-password",  
            "encrypt-password",  
            "file",  
            "global-settings",  
            "global-toolchains",  
            "log-file",  
            "projects",  
            "resume-from",  
            "settings",  
            "threads",  
            "toolchains"
    );
    
    public static boolean optionRequiresValue(String option) {
        return OPTIONS_WITH_VALUES.contains(option);
    }
}
