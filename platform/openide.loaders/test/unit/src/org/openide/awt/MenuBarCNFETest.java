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

package org.openide.awt;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.loaders.*;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author Jaroslav Tulach
 */
public class MenuBarCNFETest extends NbTestCase implements ContainerListener {
    private DataFolder df;
    private MenuBar mb;
    
    private int add;
    private int remove;
    
    public MenuBarCNFETest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testALotOfClassNotFoundExceptions() throws Exception {
        // crate XML FS from data
        String[] stringLayers = new String [] { "/org/openide/awt/data/testActionOnlyOnce.xml" };
        URL[] layers = new URL[stringLayers.length];

        for (int cntr = 0; cntr < layers.length; cntr++) {
            layers[cntr] = Utilities.class.getResource(stringLayers[cntr]);
        }

        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);

        // build menu
        DataFolder dataFolder = DataFolder.findFolder(system.findResource("FullOfCNFs"));
        CharSequence log = Log.enable("org.openide.awt", Level.INFO);
        MenuBar menuBar = new MenuBar(dataFolder);
        menuBar.waitFinished();

        int componentCount = 0;
        for (Component c : menuBar.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            componentCount++;
        }
        assertEquals("No instances", 0, componentCount);
        AWTTaskTest.waitEQ();

        
        if (log.toString().indexOf("initCause") >= 0) {
            fail("Something wrong with initCause:\n" + log);
        }

    }

    public void componentAdded(ContainerEvent e) {
        add++;
    }

    public void componentRemoved(ContainerEvent e) {
        remove++;
    }
    
    public static final class MyAction extends CallbackSystemAction {
        public static int counter;
        
        public MyAction() {
            counter++;
        }

        public String getName() {
            return "MyAction";
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        
    }

    public static class CreateOnlyOnceAction extends AbstractAction {

        static int instancesCount = 0;
        static StringWriter w;
        static PrintWriter pw;

        public static synchronized CreateOnlyOnceAction create() {
            return new CreateOnlyOnceAction();
        }

        public void actionPerformed(ActionEvent e) {
            // no op
        }

        public CreateOnlyOnceAction() {
            new Exception("created for " + (++instancesCount) + " time").printStackTrace(pw);
            putValue(NAME, "TestAction");
        }

    }


}
