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
package org.openide.loaders;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;

import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author  pzajac
 */
public class NewObjectPanelTest extends NbTestCase{

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NewObjectPanelTest.class);
    }

    private static class MyChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent event) {}
    }
    /** Creates a new instance of NewObjectPanelTest */
    public NewObjectPanelTest(String name) {
        super(name);
    }

    public void testNewObjectPanelTest() {
        NewObjectPanel panel = new NewObjectPanel();
        JFrame frame = new JFrame("sss");
        
        frame.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        assertNotNull(panel.getNewObjectName());
        assertNotNull(panel.getPreferredSize());
        MyChangeListener list = new MyChangeListener();
        panel.addChangeListener(list);
        panel.removeChangeListener(list);
        panel.addNotify();
        assertNotNull(panel.defaultNewObjectName());
        frame.dispose();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
