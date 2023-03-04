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

package org.netbeans.modules.dbschema.migration.archiver;

/**
 *
 * @author  ludo
 */
public class MapClassName {
    static String LEGACYPREFIX= "com.sun.forte4j.modules.dbmodel.";
    static String CURRENTPREFIX= "org.netbeans.modules.dbschema.";

    public static String getClassNameToken(String realClassName){
        if (realClassName.startsWith(CURRENTPREFIX)){
            realClassName = LEGACYPREFIX + realClassName.substring(CURRENTPREFIX.length());
        }

        return realClassName;
        }

    public static String getRealClassName(String token){
        if (token.startsWith(LEGACYPREFIX)){
            token = CURRENTPREFIX + token.substring(LEGACYPREFIX.length());
        }
        return token;
    }
    public static void main(String[] args){
        
        String S="org.netbeans.modules.dbschema.jdbcimpl.TEST";
        System.out.println(getClassNameToken(S));
    }
    
}
