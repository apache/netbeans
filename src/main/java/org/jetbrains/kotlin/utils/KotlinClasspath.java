/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.utils;

import java.util.ArrayList;
import java.util.List;

public class KotlinClasspath {
    
    private static final String LIB_RUNTIME_NAME = "kotlin-runtime.jar";
    
    public static List<String> getKotlinClasspath(){
        List<String> classpath = new ArrayList<String>();
        
        classpath.add(ProjectUtils.KT_HOME + "lib\\" + LIB_RUNTIME_NAME);
        
        return classpath;
    }
    
    public static String getKotlinBootClasspath(){
        return ProjectUtils.KT_HOME + "lib\\" + LIB_RUNTIME_NAME;
    }
    
}
