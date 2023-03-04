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

import java.awt.EventQueue;
import java.util.logging.Level;
import javax.swing.LookAndFeel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MenuBarNotInAWTTest extends NbTestCase {
    private DataFolder df;
    private MenuBar mb;

    static {
        System.setProperty("swing.defaultlaf", MyLaF.class.getName());
    }
    
    public MenuBarNotInAWTTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }
    
    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(
            FileUtil.getConfigRoot(),
            "Folder" + getName()
        );
        df = DataFolder.findFolder(fo);
    }

    @RandomlyFails // NB-Core-Build #3897: Laf created expected:<1> but was:<0>
    public void testCreateAndWait() throws Exception {
        mb = new MenuBar(df);
        mb.waitFinished();

        assertEquals("Laf created", 1, MyLaF.cnt);
    }


    public static final class MyLaF extends LookAndFeel {
        static int cnt;

        public MyLaF() {
            if (!Thread.currentThread().getName().contains("AWT-")) {
                assertTrue("Created only in AWT", EventQueue.isDispatchThread());
            }
            cnt++;
        }

        @Override
        public String getName() {
            return "MyLaf";
        }

        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isNativeLookAndFeel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSupportedLookAndFeel() {
            return true;
        }
    }
}
