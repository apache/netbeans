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
package org.openide.actions;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Action;
import javax.swing.JMenuItem;
import junit.framework.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.Presenter;

public class NewActionTest extends TestCase {

    public NewActionTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testTheNewTypesMethodIsCalledJustOnceIssue65534() throws Exception {
        class N extends AbstractNode {
            int cnt;
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w);
            
            public N() {
                super(Children.LEAF);
            }

            public org.openide.util.datatransfer.NewType[] getNewTypes() {
                cnt++;
                org.openide.util.datatransfer.NewType[] retValue;
                
                new Exception("Call " + cnt).printStackTrace(p);

                retValue = super.getNewTypes();
                return retValue;
            }
        }
        
        
        N node = new N();
        
        NewAction a = (NewAction)NewAction.get(NewAction.class);
        
        Action clone = a.createContextAwareInstance(node.getLookup());
        
        if (!(clone instanceof Presenter.Popup)) {
            fail("Does not implement popup: " + clone);
        }
        
        Presenter.Popup p = (Presenter.Popup)clone;
        
        JMenuItem m = p.getPopupPresenter();
        String name = m.getName();
        assertEquals("Just one call to getNewTypes\n" + node.w, 1, node.cnt);
    }
}


