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

package org.netbeans.modules.cnd.modelimpl.parser;

/**
 *
 */
public class Printf {

    // TODO: it may be easier to use System.out.printf
    public static void printf(String pattern, Object[] args) {
        StringBuilder sb = new StringBuilder();
        int from = 0;
        int pos = pattern.indexOf('%');
        int argNumber = 0;
        while( pos >= 0 ) {
            sb.append(pattern.substring(from,  pos));
            from = pos + 2;
            if( argNumber < args.length ) {
                sb.append(args[argNumber] == null ? "null" : args[argNumber].toString()); // NOI18N
            }
            argNumber++;
            pos = pattern.indexOf('%',  from);
        }
        if( from < pattern.length() ) {
            sb.append(pattern.substring(from));
        }
        System.out.print(sb.toString());
    }
    
}
