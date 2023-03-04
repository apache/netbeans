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
package org.netbeans.core.output2;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author jhavlin
 */
public class NbIOProviderTest {

    public NbIOProviderTest() {
    }

    @Test
    public void testGetIO_4args() {

        IOContainer def = IOContainer.getDefault();
        IOContainer cus = IOContainer.create(new MyContainerProvider());

        InputOutput ioDef1 = IOProvider.getDefault().getIO("test1", true, null, def);
        InputOutput ioCus1 = IOProvider.getDefault().getIO("test1", true, null, cus);
        InputOutput ioDef2 = IOProvider.getDefault().getIO("test2", true, null, def);
        InputOutput ioCus2 = IOProvider.getDefault().getIO("test2", true, null, cus);

        assertNotSame(ioDef1, ioCus1);
        assertNotSame(ioDef2, ioCus2);
        assertNotSame(ioDef1, ioDef2);
        assertNotSame(ioCus1, ioCus2);

        InputOutput ioDef1b = IOProvider.getDefault().getIO("test1", false, null, def);
        InputOutput ioCus1b = IOProvider.getDefault().getIO("test1", false, null, cus);

        assertSame(ioDef1, ioDef1b);
        assertSame(ioCus1, ioCus1b);

        ioDef1.closeInputOutput();
        ioDef2.closeInputOutput();
        ioCus1.closeInputOutput();
        ioCus2.closeInputOutput();
    }

    public static class MyContainerProvider implements IOContainer.Provider {

        @Override
        public void open() {
        }

        @Override
        public void requestActive() {
        }

        @Override
        public void requestVisible() {
        }

        @Override
        public boolean isActivated() {
            return true;
        }

        @Override
        public void add(JComponent comp, CallBacks cb) {
        }

        @Override
        public void remove(JComponent comp) {
        }

        @Override
        public void select(JComponent comp) {
        }

        @Override
        public JComponent getSelected() {
            return null;
        }

        @Override
        public void setTitle(JComponent comp, String name) {
        }

        @Override
        public void setToolTipText(JComponent comp, String text) {
        }

        @Override
        public void setIcon(JComponent comp, Icon icon) {
        }

        @Override
        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        }

        @Override
        public boolean isCloseable(JComponent comp) {
            return true;
        }
    }
}
