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

package org.netbeans.modules.cnd.apt.support;

/**
 * callback interface to get information about macros
 */
public interface APTMacroCallback {

    /** @return true if the macro is defined, otherwise false      */
    public boolean isDefined(APTToken token);
    
    /** @return true if the macro is defined, otherwise false      */
    public boolean isDefined(CharSequence token);
    
    public APTMacro getMacro(APTToken token);
    

    /**
     * remember in stack currently expanded macro 
     * used to prevent recurse in macro expanding
     * @return false if macro already in expanding state
     */   
    public boolean pushExpanding(APTToken token);
    
    /**
     * remove last expanded macro from expanding stack
     */    
    public void popExpanding();
    
    /**
     * check if macro is already somewhere in macro expanding stack
     */    
    public boolean isExpanding(APTToken token);

    public boolean pushPPDefined();

    public boolean popPPDefined();
}
