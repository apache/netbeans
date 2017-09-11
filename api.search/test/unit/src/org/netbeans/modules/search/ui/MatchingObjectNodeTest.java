/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.ui;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.SearchTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

/**
 *
 * @author jhavlin
 */
public class MatchingObjectNodeTest extends NbTestCase {

    public MatchingObjectNodeTest(String name) {
        super(name);
    }

    /**
     * Test that bug 213441 is fixed.
     */
    @RandomlyFails
    public void testBug213441() throws IOException, InterruptedException,
            InvocationTargetException {

        Node n = new AbstractNode(Children.LEAF);
        Semaphore s = new Semaphore(0);
        ResultModel rm = SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        MatchingObjectNode mon = new MatchingObjectNode(n, Children.LEAF, mo,
                false);
        mon.addNodeListener(new DisplayNameChangeListener(s));
        mon.getDisplayName();
        mo.getFileObject().delete();
        rm.close();
        assertTrue("Display name change event has not been fired!", //NOI18N
                s.tryAcquire(10, TimeUnit.SECONDS));
    }

    /**
     * Test for bug 217984.
     */
    public void testCreateNodeForInvalidDataObject() throws IOException {
        ResultModel rm = SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        DataObject dob = mo.getDataObject();
        FileObject fob = mo.getFileObject();
        Node original = dob.getNodeDelegate();
        fob.delete();
        // No exception should be thrown from the constructor.
        Node n = new MatchingObjectNode(original, Children.LEAF, mo, false);
        assertEquals("test.txt", n.getDisplayName());
    }

    /**
     * Listener that releases a semaphore when display name is changed.
     */
    private class DisplayNameChangeListener implements NodeListener {

        private Semaphore semaphore;

        public DisplayNameChangeListener(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Node.PROP_DISPLAY_NAME)) {
                semaphore.release();
            }
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }
    }
}
