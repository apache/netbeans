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

package org.openide.util;

import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Dafe Simonek
 */
public class UtilitiesProgressCursorTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(UtilitiesProgressCursorTest.class);
    }

    /** Creates a new instance of UtilProgressCursorTest */
    public UtilitiesProgressCursorTest(String testName) {
        super(testName);
    }

    public void testProgressCursor () {
        JComponent testTc = new ProgressCursorComp();
        Cursor progressCursor = Utilities.createProgressCursor(testTc);
        testTc.setCursor(progressCursor);
        //testTc.open();
        Cursor compCursor = testTc.getCursor();
        if (!progressCursor.equals(compCursor)) {
            fail("Setting of progress cursor don't work: \n" +
                 "Comp cursor: " + compCursor + "\n" +
                 "Progress cursor: " + progressCursor);
        }
    }
    
    /** testing component for setting cursor
     */
    private static class ProgressCursorComp extends JComponent {

        @Override
        public String getName () {
            return "TestProgressCursorComp";
        }
        
    }

}
