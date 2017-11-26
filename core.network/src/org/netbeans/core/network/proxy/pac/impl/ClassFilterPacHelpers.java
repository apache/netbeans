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
package org.netbeans.core.network.proxy.pac.impl;

import jdk.nashorn.api.scripting.ClassFilter;
import org.netbeans.core.network.proxy.pac.PacHelperMethods;

/**
 * Nashorn class filter which helps us create a sandboxed JavaScript execution
 * environment which only has access to the Helper methods, nothing more.
 * 
 * <p>Note that the ClassFilter feature is specific to Nashorn (Rhino had the
 * {@code ClassShutter} class for this purpose), but the feature did not appear 
 * until Java 8u40.
 * 
 * @author lbruun
 */
class ClassFilterPacHelpers implements ClassFilter {

    @Override
    public boolean exposeToScripts(String string) {
        // The only Java class the PAC script is allowed to
        // make use of is the PAC Helpers, nothing more. 
        return string.equals(PacHelperMethods.class.getName());
    }
}
