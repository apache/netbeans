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

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultDataObjectHasOpenActionTest extends NbTestCase {

    private DataObject obj;

    public DefaultDataObjectHasOpenActionTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("x.test");
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
    }

    public void testOpenActionIsAlwaysFirst() throws Exception {
        Node n = obj.getNodeDelegate();

        Action openAction = Actions.forID("System", "org.openide.actions.OpenAction");

        assertEquals(
                "Open action is the default one",
                openAction,
                n.getPreferredAction()
                );

        Action[] actions = n.getActions(false);
        assertTrue("There are some actions", actions.length > 1);

        assertEquals(
                "First one is open",
                openAction,
                actions[0]
                );
    }

}
