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

package org.netbeans.modules.openide.windows;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class GlobalActionContextImplTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(GlobalActionContextImplTest.class);
    }

    public GlobalActionContextImplTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testCanBlickWithNullMap() {
        TopComponent tc = new TopComponent() {
            {
                associateLookup(Lookups.fixed(10, getActionMap()));
            }
        };
        tc.open();
        tc.requestActive();
        
        assertEquals("Check before", Integer.valueOf(10), Utilities.actionsGlobalContext().lookup(Integer.class));
        
        GlobalActionContextImpl.blickActionMapImpl(null, new Component[] { new JButton() });
        
        assertEquals("Check after", Integer.valueOf(10), Utilities.actionsGlobalContext().lookup(Integer.class));
        
    }
    
}
