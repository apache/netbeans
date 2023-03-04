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
package org.openide.util.datatransfer;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import junit.textui.TestRunner;
import org.openide.util.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.AsyncGUIJob;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExTransferableTest extends NbTestCase {
    /** Creates a new instance of UtilProgressCursorTest */
    public ExTransferableTest (String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new NbTestSuite(ExTransferableTest.class));
    }

    public void testExTranferableKeepsOrderOfInsertedDataFlavors () throws Exception {
        HashSet set = new HashSet ();
        set.add (DataFlavor.stringFlavor);
        set.add (DataFlavor.imageFlavor);
        
        DataFlavor[] arr = (DataFlavor[])set.toArray (new DataFlavor[2]);
        
        ExTransferable t = ExTransferable.create (ExTransferable.EMPTY);
        // now insert the DataFlavor but in oposite order than is 
        // according to their hashCodes
        t.put (new Sin (arr[1]));
        t.put (new Sin (arr[0]));
        
        
        List res = Arrays.asList (t.getTransferDataFlavors ());

        assertEquals ("First inserted is first", 0, res.indexOf (arr[1]));
        assertEquals ("Second inserted is second", 1, res.indexOf (arr[0]));
    }

    private static final class Sin extends ExTransferable.Single {
        public Sin (DataFlavor f) {
            super (f);
        }
        
        protected Object getData () {
            return null;
        }
    } // end of Sin
}
