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
package org.openide.explorer.view;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineViewTest.TestNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Holy
 */
public class TableViewTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TableViewTest.class);
    }

    private static final Logger LOG = Logger.getLogger(TableViewTest.class.getName());

    public TableViewTest(String name) {
        super(name);
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    public static final class AWTExceptionHandler {

        public AWTExceptionHandler() {
        }

        public static void handle(Throwable tt) {
            exceptionInEDT = true;
        }
    }

    static {
        System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
    }
    TableViewPanel tvp;
    Dialog dlg;
    static boolean exceptionInEDT;

    @RandomlyFails /* NB-Core-Build #8129:
java.awt.IllegalComponentStateException: component must be showing on the screen to determine its location
	at java.awt.Component.getLocationOnScreen_NoTreeLock(Component.java:1964)
	at java.awt.Component.getLocationOnScreen(Component.java:1938)
	at javax.swing.JPopupMenu.show(JPopupMenu.java:887)
	at org.openide.explorer.view.TableView.showPopup(TableView.java:339)
	at org.openide.explorer.view.TableView.access$500(TableView.java:85)
	at org.openide.explorer.view.TableView$PopupAdapter.showPopup(TableView.java:439)
	at ...
	at java.awt.Component.dispatchEvent(Component.java:4481)
	at org.openide.explorer.view.TableViewTest.test170578NPE(TableViewTest.java:128)
    */
    public void test170578NPE() throws InterruptedException, InvocationTargetException, AWTException {
        System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
        final Keys ch = new Keys(false, "1", "2", "3");
        final TestNode root = new TestNode(ch, "root");

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                tvp = new TableViewPanel(root);
                DialogDescriptor dd = new DialogDescriptor(tvp, "", false, null);
                dlg = DialogDisplayer.getDefault().createDialog(dd);
            }
        });

        dlg.setVisible(true);
        while (!dlg.isShowing()) {
            LOG.log(Level.INFO, "Not showing yet {0}", dlg);
            Thread.sleep(1000);
        }

        Point p = tvp.view.getLocationOnScreen();
        long now = System.currentTimeMillis();
        tvp.view.dispatchEvent(new MouseEvent(tvp.view, MouseEvent.MOUSE_PRESSED, now, InputEvent.BUTTON3_MASK, p.x + 5, p.y + 5, 1, true));
        tvp.view.dispatchEvent(new MouseEvent(tvp.view, MouseEvent.MOUSE_RELEASED, now, InputEvent.BUTTON3_MASK, p.x + 5, p.y + 5, 1, true));
        Thread.sleep(1000);

        if (exceptionInEDT) {
            fail("Exception in AWT thread");
        }
        dlg.setVisible(false);
    }

    public static class Keys extends Children.Keys {

        public Keys(boolean lazy, String... args) {
            super(lazy);
            if (args != null && args.length > 0) {
                setKeys(args);
            }
        }

        public void keys(String... args) {
            super.setKeys(args);
        }

        public void keys(Collection args) {
            super.setKeys(args);
        }

        @Override
        protected Node[] createNodes(Object key) {
            return new Node[]{new TestNode(key.toString())};
        }
    }

    private class TableViewPanel extends JPanel implements ExplorerManager.Provider {

        ExplorerManager manager = new ExplorerManager();
        TableView view;

        private TableViewPanel(Node rootNode) {
            setLayout(new BorderLayout());
            manager.setRootContext(rootNode);
            view = new TableView();
            Node.Property[] props = rootNode.getPropertySets()[0].getProperties();
            ((NodeTableModel) view.getTable().getModel()).setProperties(props);
            add(view, BorderLayout.CENTER);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }
}
