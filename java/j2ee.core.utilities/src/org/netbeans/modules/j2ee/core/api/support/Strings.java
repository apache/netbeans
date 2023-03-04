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
package org.netbeans.modules.j2ee.core.api.support;

public final class Strings {

    private Strings() {
    }

    /**
     * Checks whether the given <code>str</code> is an empty string. More
     * specifically, checks whether it is null and or contains only whitespace characters.
     * 
     * @param str the string to check.
     * @return true if the given <code>str</code> was null 
     * or contained only whitespaces, false otherwise.
     */ 
    public static boolean isEmpty(String str){
        return null == str || "".equals(str.trim());
    }
    
}
