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

package org.netbeans.modules.ide.ergonomics;

import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.debugger.ui.AttachType;

/**
 *
 * @author Pavel Flaska
 */
public class DebuggerAttachTypesCheck extends NbTestCase {

    public DebuggerAttachTypesCheck(String name) {
        super(name);
    }
    
    public void testGetAllDebuggers() {
       List<? extends AttachType> debug = DebuggerManager.getDebuggerManager().lookup(null, AttachType.class);
       int cnt = 0;
       for (AttachType o : debug) {
           System.setProperty("debugger." + ++cnt, o.getTypeDisplayName());
       }
    }

    public void testGetAllDebuggersReal() {
       List<? extends AttachType> debug = DebuggerManager.getDebuggerManager().lookup(null, AttachType.class);
       int cnt = 0;
       for (AttachType o : debug) {
           String name = System.getProperty("debugger." + ++cnt);
           assertEquals(name, o.getTypeDisplayName());
       }
    }

}
