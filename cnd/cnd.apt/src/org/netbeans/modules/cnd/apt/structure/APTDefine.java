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

package org.netbeans.modules.cnd.apt.structure;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * #define directive
 */
public interface APTDefine extends APT {
    /** 
     * returns token of macro name
     */
    public APTToken getName();
    
    /** 
     * returns array of macro params
     * @see isFunctionLike
     * if function-like macro => return is non null
     * otherwise it is null
     */
    public Collection<APTToken> getParams();
    
    /** 
     * returns true if macro was defined as function
     * #define MAX(x,y) ...
     * or 
     * #define A() ...
     */
    public boolean isFunctionLike();

    /** 
     * returns List of tokens of macro body
     */      
    public List<APTToken> getBody();
    
    /**
     * returns true if #define directive is valid
     */
    public boolean isValid();
}
