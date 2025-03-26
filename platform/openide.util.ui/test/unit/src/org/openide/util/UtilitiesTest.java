/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
//import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
/*
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
*/
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import org.netbeans.junit.NbTestCase;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.implspi.NamedServicesProvider;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.test.MockLookup;

/**
 * @author Jiri Rechtacek et al.
 */
public class UtilitiesTest extends NbTestCase {

    public UtilitiesTest (String testName) {
        super (testName);
    }
    
    public void testCustomCursorNotSupported() {
        NoCustomCursorToolkit toolkit = new NoCustomCursorToolkit();
        CustomToolkitComponent c = new CustomToolkitComponent( toolkit );
        Image icon = new BufferedImage( 16, 16, BufferedImage.TYPE_BYTE_BINARY );
        Cursor cursor = Utilities.createCustomCursor( c, icon, "junittest" );
        assertTrue( "fallback to wait cursor", Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ).equals( cursor ) );
        assertTrue( "getBestCursorSize was called", toolkit.getBestCursorSizeCalled );
        assertFalse( "no custom cursor created", toolkit.createCustomCursorCalled );
    }

    public void testKeyConversions() throws Exception {
        assertEquals("CS-F1", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)));
        assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK), Utilities.stringToKey("A-EQUALS"));
        // XXX stringToKeys, Mac support, various more exotic conditions...
    }

    public void testKeyConversionsPortable() throws Exception {
        if (Utilities.isMac()) {
            assertEquals("SD-D", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_MASK | KeyEvent.META_MASK), true));
            assertEquals("SO-D", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK), true));
            assertEquals("A-RIGHT", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), true));
        } else {
            assertEquals("SD-D", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK), true));
            assertEquals("O-RIGHT", Utilities.keyToString(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), true));
        }
    }

    public void testSpecialKeyworksOn14AsWell15 () throws Exception {
        KeyStroke ks = Utilities.stringToKey("C-CONTEXT_MENU");
        assertNotNull ("key stroke created", ks);
        KeyStroke alt = KeyStroke.getKeyStroke(ks.getKeyCode(), KeyEvent.ALT_MASK);
        String s = Utilities.keyToString(alt);
        assertEquals ("Correctly converted", "A-CONTEXT_MENU", s);    
    }
    
    public void testSpecialKeyworksOn14AsWell15WithoutModificators () throws Exception {
        KeyStroke ks = Utilities.stringToKey("CONTEXT_MENU");
        assertNotNull ("key stroke created", ks);
        String s = Utilities.keyToString(ks);
        assertEquals ("Correctly converted", "CONTEXT_MENU", s);
    }

    public void testActionsToPopupWithLookup() throws Exception {
        MockLookup.setInstances(new AwtBridgeImpl());
        final List<String> commands = new ArrayList<String>();
        class BasicAction extends AbstractAction {
            public BasicAction(String name) {
                super(name);
            }
            public void actionPerformed(ActionEvent e) {
                commands.add((String) getValue(Action.NAME));
            }
        }
        class ContextAction extends BasicAction implements ContextAwareAction {
            public ContextAction(String name) {
                super(name);
            }
            public Action createContextAwareInstance(final Lookup actionContext) {
                return new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        commands.add(ContextAction.this.getValue(Action.NAME) + "/" + actionContext.lookup(String.class));
                    }
                };
            }
        }
        class SpecialMenuAction extends BasicAction implements Presenter.Popup {
            public SpecialMenuAction(String name) {
                super(name);
            }
            public JMenuItem getPopupPresenter() {
                JMenuItem item = new JMenuItem((String) getValue(Action.NAME));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commands.add(((String) getValue(Action.NAME)) + "/popup");
                    }
                });
                return item;
            }
        }
        Action duplicated = new BasicAction("duplicated");
        Action[] actions = new Action[] {
            null,
            null,
            new BasicAction("first"),
            duplicated,
            null,
            null,
            new BasicAction("second"),
            duplicated,
            null,
            new ContextAction("context"),
            new SpecialMenuAction("presenter"),
            null,
            new BasicAction("top"),
            new BasicAction("HIDDEN"),
            null,
            new BasicAction("bottom"),
            null,
            null,
        };
        Lookup l = Lookups.singleton("thing");
        JPopupMenu menu = Utilities.actionsToPopup(actions, l);
        for (Component element : menu.getComponents()) { // including separators
            if (element instanceof AbstractButton) {
                ((AbstractButton) element).doClick();
            } else {
                commands.add(null);
            }
        }
        String[] expectedCommands = new String[] {
            // leading separators must be stripped
            "first",
            "duplicated",
            null, // adjacent separators must be collapsed
            "second",
            // do not add the same action twice
            null,
            "context/thing", // ContextAwareAction was checked for
            "presenter/popup", // Presenter.Popup was checked for
            null,
            "top",
            // exclude HIDDEN because of AwtBridgeImpl.convertComponents
            // separator should however remain
            null,
            "bottom",
            // trailing separators must be stripped
        };
        assertEquals("correct generated menu", Arrays.asList(expectedCommands), commands);
    }

    public void testActionsForPath() throws Exception {
        MockLookup.setInstances(new NamedServicesProviderImpl());
        // #156829: ensure that no tree lock is acquired.
        final Semaphore ready = new Semaphore(0);
        final Semaphore done = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (new JSeparator().getTreeLock()) {
                    ready.release();
                    try {
                        done.acquire();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        ready.acquire();
        try {
            assertEquals("[hello, null, there]", Utilities.actionsForPath("stuff").toString());
        } finally {
            done.release();
        }
    }
    
    public static class ContextData {
        final String suffix;

        public ContextData(String suffix) {
            this.suffix = suffix;
        }
    }

    public void testActionsForPathWithLookup() throws Exception {
        MockLookup.setInstances(new NamedServicesProviderImpl());
        // #156829: ensure that no tree lock is acquired.
        final Semaphore ready = new Semaphore(0);
        final Semaphore done = new Semaphore(0);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                synchronized (new JSeparator().getTreeLock()) {
                    ready.release();
                    try {
                        done.acquire();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        ready.acquire();
        try {
            assertEquals("[hello-ehlo, null, there-ehlo]", Utilities.actionsForPath("stuff", Lookups.fixed(new ContextData("ehlo"))).toString());
        } finally {
            done.release();
        }
    }

    private static class CustomToolkitComponent extends Component {
        private Toolkit customToolkit;
        
        public CustomToolkitComponent( Toolkit t ) {
            this.customToolkit = t;
        }

        @Override
        public Toolkit getToolkit() {
            return customToolkit;
        }
    }

    private static class NoCustomCursorToolkit extends Toolkit {
        public FontMetrics getFontMetrics(Font font) {
            return Toolkit.getDefaultToolkit().getFontMetrics( font );
        }

        public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
            return Toolkit.getDefaultToolkit().prepareImage( image, width, height, observer );
        }

        public int checkImage(Image image, int width, int height, ImageObserver observer) {
            return Toolkit.getDefaultToolkit().checkImage( image, width, height, observer );
        }

        public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
            return Toolkit.getDefaultToolkit().getPrintJob( frame, jobtitle, props );
        }

        public Image createImage(ImageProducer producer) {
            return Toolkit.getDefaultToolkit().createImage( producer );
        }

        public Image getImage(String filename) {
            return Toolkit.getDefaultToolkit().getImage( filename );
        }

        public Image createImage(String filename) {
            return Toolkit.getDefaultToolkit().createImage( filename );
        }

        public Map mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException {
            return Toolkit.getDefaultToolkit().mapInputMethodHighlight( highlight );
        }

        public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
            return Toolkit.getDefaultToolkit().createImage( imagedata, imageoffset, imagelength );
        }

        public Image getImage(URL url) {
            return Toolkit.getDefaultToolkit().getImage( url );
        }

        public Image createImage(URL url) {
            return Toolkit.getDefaultToolkit().createImage( url );
        }

        public void sync() {
            Toolkit.getDefaultToolkit().sync();
        }

        protected EventQueue getSystemEventQueueImpl() {
            return Toolkit.getDefaultToolkit().getSystemEventQueue();
        }

        public Clipboard getSystemClipboard() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        public Dimension getScreenSize() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getScreenSize();
        }

        public int getScreenResolution() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getScreenResolution();
        }

        public String[] getFontList() {
            return Toolkit.getDefaultToolkit().getFontList();
        }

        public ColorModel getColorModel() throws HeadlessException {
            return Toolkit.getDefaultToolkit().getColorModel();
        }

        public void beep() {
            Toolkit.getDefaultToolkit().beep();
        }

        boolean createCustomCursorCalled = false;

        @Override
        public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {

            createCustomCursorCalled = true;
            return Toolkit.getDefaultToolkit().createCustomCursor(cursor, hotSpot, name);
        }

        boolean getBestCursorSizeCalled = false;

        @Override
        public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
            getBestCursorSizeCalled = true;
            return new Dimension(0,0);
        }

        @Override
        public boolean isModalityTypeSupported(ModalityType modalityType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isModalExclusionTypeSupported(ModalExclusionType modalExclusionType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static final class AwtBridgeImpl extends ActionPresenterProvider {
        public JPopupMenu createEmptyPopup() {
            return new JPopupMenu();
        }
        public JMenuItem createMenuPresenter(Action action) {
            return new JMenuItem(action);
        }
        public JMenuItem createPopupPresenter(Action action) {
            return new JMenuItem(action);
        }
        public Component createToolbarPresenter(Action action) {
            return new JButton(action);
        }
        public Component[] convertComponents(Component comp) {
            if (comp instanceof JMenuItem && "HIDDEN".equals(((JMenuItem) comp).getText())) {
                return new Component[0];
            } else {
                return new Component[] {comp};
            }
        }
    }

    private class NamedServicesProviderImpl extends NamedServicesProvider {

        public NamedServicesProviderImpl() {
        }

        public Lookup create(String path) {
            if (!path.equals("stuff/")) {
                return Lookup.EMPTY;
            }
            InstanceContent content = new InstanceContent();

            class ContextAction extends AbstractAction implements ContextAwareAction {
                final String obj;

                public ContextAction(String obj) {
                    this.obj = obj;
                }
                
                public void actionPerformed(ActionEvent e) {
                }

                @Override
                public String toString() {
                    return obj;
                }

                @Override
                public Action createContextAwareInstance(Lookup actionContext) {
                    ContextData cd = actionContext.lookup(ContextData.class);
                    return new ContextAction(obj + (cd == null ? "-ctx" : "-" + cd.suffix));
                }
            }
            
            InstanceContent.Convertor<String, Action> actionConvertor = new InstanceContent.Convertor<String, Action>() {
                public Action convert(final String obj) {
                    return new ContextAction(obj);
                }
                
                public Class<? extends Action> type(String obj) {
                    return AbstractAction.class;
                }

                public String id(String obj) {
                    return obj;
                }

                public String displayName(String obj) {
                    return id(obj);
                }
            };
            InstanceContent.Convertor<Boolean, JSeparator> separatorConvertor = new InstanceContent.Convertor<Boolean, JSeparator>() {

                public JSeparator convert(Boolean obj) {
                    fail("should not be creating the JSeparator yet");
                    return new JSeparator();
                }

                public Class<? extends JSeparator> type(Boolean obj) {
                    return JSeparator.class;
                }

                public String id(Boolean obj) {
                    return "sep";
                }

                public String displayName(Boolean obj) {
                    return id(obj);
                }
            };
            content.add("hello", actionConvertor);
            content.add(true, separatorConvertor);
            content.add("there", actionConvertor);
            return new AbstractLookup(content);
        }
    }
    
}
