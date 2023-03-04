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

package org.netbeans.modules.glassfish.spi;

import java.util.Properties;
import org.openide.util.Lookup;


/**
 *
 * @author Peter Williams
 */
public interface GlassfishModuleFactory {
    
    /** Returns true if specified glassfish install supports the module type
     *  in question.
     * 
     * @param glassfishHome
     * @param asenvProps
     * @return true or false depending on whether the referenced glassfish 
     *   install supports this container type.
     */
    public boolean isModuleSupported(String glassfishHome, Properties asenvProps);
    
    /** Creates support object for whatever GlassFish module type this is.
     * 
     * @param instanceLookup Lookup for the owning GlassFish Server Instance.
     *   Used by created support object to access common instance functionality
     *   such as start/stop server.
     * 
     * @return Support object for this GlassFish module type (JRuby, JavaEE, etc.)
     */
    public Object createModule(Lookup instanceLookup);
    
}
