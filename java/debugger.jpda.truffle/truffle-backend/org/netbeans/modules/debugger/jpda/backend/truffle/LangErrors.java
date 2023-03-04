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

package org.netbeans.modules.debugger.jpda.backend.truffle;

/**
 * Errors in a language implementation.
 * 
 * @author martin
 */
final class LangErrors {
    
    private static final boolean SUPRESS_EXCEPTIONS = Boolean.getBoolean("truffle.nbdebug.supressLangErrs");
    
    private LangErrors() {}
    
    static void exception(String context, Throwable ex) {
        if (!SUPRESS_EXCEPTIONS) {
            System.err.println(context);
            ex.printStackTrace();
        }
    }
}
