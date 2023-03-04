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

package org.netbeans.core.windows.view.ui;

import java.util.Locale;

import org.netbeans.junit.NbTestCase;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class MultiSplitCellTest extends NbTestCase {

    public MultiSplitCellTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }

    private Locale defLocale;
    @Override
    protected void setUp() throws Exception {
//        super.setUp();
//        defLocale = Locale.getDefault();
//        Locale.setDefault(new Locale("te_ST"));
    }
    
    @Override
    protected void tearDown() {
//        Locale.setDefault(defLocale);
    }
    
    public void testResizingDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentResizingEnabled());
    }
    
    public void testMinimumSizeRespected() throws Exception {
//        assertTrue(Switches.isSplitterRespectMinimumSizeEnabled());
//        ViewElement ve = new ViewElement(null, 0.0) {
//
//            @Override
//            public Component getComponent() {
//                JPanel panel = new JPanel();
//                panel.setMinimumSize( new Dimension(1024,1024) );
//                return panel;
//            }
//
//            @Override
//            public boolean updateAWTHierarchy(Dimension availableSpace) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        };
//        MultiSplitCell cell = new MultiSplitCell(ve, 0.5, true);
//        assertTrue( cell.getMinimumSize() == 1024 );
    }
}

