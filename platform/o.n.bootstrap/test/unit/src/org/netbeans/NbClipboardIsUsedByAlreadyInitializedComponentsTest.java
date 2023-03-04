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

package org.netbeans;

import java.awt.GraphicsEnvironment;
import javax.swing.TransferHandler;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test that verifies that Clipboard is used by swing components.
 * @author Jaroslav Tulach
 * @see "#40693"
 */
public class NbClipboardIsUsedByAlreadyInitializedComponentsTest extends NbClipboardIsUsedBySwingComponentsTest {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbClipboardIsUsedByAlreadyInitializedComponentsTest.class);
    }

    private javax.swing.JTextField field;
    
    public NbClipboardIsUsedByAlreadyInitializedComponentsTest (String name) {
        super(name);
    }

    protected void inMiddleOfSettingUpTheManager() {
        assertNotNull("There is a manager already", System.getSecurityManager());
        // do some strange tricks to initialize the system
        field = new javax.swing.JTextField ();
        TransferHandler.getCopyAction();
        TransferHandler.getCutAction();
        TransferHandler.getPasteAction();
    }
    
    /** overrides to return field that exists since begining and was not instantiated
     * after SecurityManager hack is started */
    protected javax.swing.JTextField getField () {
        return field;
    }
    
}
