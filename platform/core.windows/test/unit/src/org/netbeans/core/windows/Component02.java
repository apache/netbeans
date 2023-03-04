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

package org.netbeans.core.windows;

import org.openide.windows.TopComponent;

/**
 *
 * Test component with persistence type PERSISTENCE_NEVER.
 * 
 * @author  Marek Slama
 *
 */
public class Component02 extends TopComponent {

    static final long serialVersionUID = 6021472310161712876L;
    
    private static final String TC_ID = "component02";
    
    public Component02 () {
    }

    @Override
    protected String preferredID () {
        return TC_ID;
    }
    
    /** Overriden to explicitely set persistence type of TestComponent00
     * to PERSISTENCE_NEVER */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
}
