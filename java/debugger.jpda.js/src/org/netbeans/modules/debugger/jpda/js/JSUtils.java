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
package org.netbeans.modules.debugger.jpda.js;

/**
 *
 * @author Martin
 */
public class JSUtils {
    
    public static final String JS_MIME_TYPE = "text/javascript";    // NOI18N
    public static final String JS_STRATUM = "JS";                   // NOI18N
    
    // Script class for Nashorn built in JDK
    public static final String NASHORN_SCRIPT_JDK = "jdk.nashorn.internal.scripts.Script$";     // NOI18N
    // avoid API type removed warning, but do not use this constant, use explicitly _JDK or _EXT suffixes
    public static final String NASHORN_SCRIPT = NASHORN_SCRIPT_JDK;
    // Script class for external Nashorn
    public static final String NASHORN_SCRIPT_EXT = "org.openjdk.nashorn.internal.scripts.Script$";     // NOI18N
    
    public static final String VAR_THIS = ":this";     // NOI18N
    public static final String VAR_SCOPE = ":scope";   // NOI18N
    public static final String VAR_CALLEE = ":callee"; // NOI18N
    
}
