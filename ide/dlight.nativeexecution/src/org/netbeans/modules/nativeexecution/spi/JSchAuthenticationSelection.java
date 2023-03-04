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
package org.netbeans.modules.nativeexecution.spi;

import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.util.Authentication;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public abstract class JSchAuthenticationSelection {
    private static final JSchAuthenticationSelection INSTANCE = new JSchAuthenticationSelectionImpl();
    
    public static JSchAuthenticationSelection find() {
        Collection<? extends JSchAuthenticationSelection> sels = Lookup.getDefault().lookupAll(JSchAuthenticationSelection.class);
        if (sels.isEmpty()) {
            return INSTANCE;
        }
        return sels.iterator().next();
    }
    
    public abstract  boolean initAuthentication(Authentication auth);
    
    private static final  class JSchAuthenticationSelectionImpl extends JSchAuthenticationSelection {

        @Override
        public boolean initAuthentication(Authentication auth) {
            return false;
        }
    
    }   
    
}
