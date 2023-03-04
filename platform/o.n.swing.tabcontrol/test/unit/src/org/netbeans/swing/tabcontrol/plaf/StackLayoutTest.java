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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.lang.ref.WeakReference;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dafe Simonek
 */
public class StackLayoutTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(StackLayoutTest.class);
    }

    public StackLayoutTest(String testName) {
        super(testName);
    }
    
    public void test_100486 () throws Exception {
        StackLayout layout = new StackLayout();
        JPanel panel = new JPanel(layout);
        JLabel testLabel = new JLabel("test label");
        panel.add(testLabel);
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        layout.showComponent(testLabel, panel);
        frame.setVisible(true);
        
        frame.setVisible(false);
        frame.getContentPane().remove(panel);
        panel = null;
        frame = null;
        WeakReference<Component> weakTestLabel = new WeakReference<Component>(testLabel);
        testLabel = null;
        
        assertGC("visibleComp member of StackLayout still not GCed", weakTestLabel, Collections.singleton(layout));
    }
    
}
