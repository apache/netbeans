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

package org.netbeans.installer.infra.utils.platformtester;

import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author ks152834
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Properties properties = System.getProperties();
        
        System.out.println(
                "os.name => " + properties.get("os.name")); // NOI18N
        System.out.println(
                "os.arch => " + properties.get("os.arch")); // NOI18N
        System.out.println(
                "os.version => " + properties.get("os.version")); // NOI18N
        
        System.out.println(
                ""); // NOI18N
        System.out.println(
                "---------------------------------------------"); // NOI18N
        System.out.println(
                "Other properties:"); // NOI18N
        System.out.println(
                ""); // NOI18N
        
        for (Object key: new TreeSet<Object> (properties.keySet())) {        
            System.out.println(key.toString() + 
                    " => " + properties.get(key).toString()); // NOI18N
        }
    }
}
