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
package org.netbeans.modules.nbcode.integration;

import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 * Set the default debugger persistence to false.
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types={org.netbeans.api.debugger.Properties.Initializer.class})
public class LspDebuggerOptions implements Properties.Initializer {

    private static final String PERSISTENCE_BREAKPOINTS = "debugger.persistence.breakpoints"; // NOI18N
    private static final String PERSISTENCE_WATCHES = "debugger.persistence.watches"; // NOI18N

    @Override
    public String[] getSupportedPropertyNames() {
        return new String[] {
            PERSISTENCE_BREAKPOINTS,
            PERSISTENCE_WATCHES,
        };
    }

    @Override
    public Object getDefaultPropertyValue(String propertyName) {
        if (propertyName.startsWith("debugger.persistence")) {      // NOI18N
            return false;
        } else {
            return null;
        }
    }
    
}
