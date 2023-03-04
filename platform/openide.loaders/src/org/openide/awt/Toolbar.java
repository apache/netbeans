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

import org.netbeans.modules.openide.loaders.AWTTask;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderInstance;
import org.openide.util.ImageUtilities;
import org.openide.util.Task;
import org.openide.util.actions.Presenter;

/**
 * Toolbar provides a component which is useful for displaying commonly used
 * actions.  It can be dragged inside its <code>ToolbarPanel</code> to
 * customize its location.
 *
 * @author  David Peroutka, Libor Kramolis
 */
public class Toolbar extends ToolbarWithOverflow /*implemented by patchsuperclass MouseInputListener*/ {
    /** Basic toolbar height.
     @deprecated Use getBasicHeight instead. */
    @Deprecated
    public static final int BASIC_HEIGHT = 34;

    static final Logger LOG = Logger.getLogger(Toolbar.class.getName());

    /** display name of the toolbar */
    private String displayName;
    
    /** Used for lazy creation of Folder and DisplayName */
    private DataFolder backingFolder;
    /* FolderInstance that will track all the changes in backingFolder */
    private Folder processor;

    //needed to turn off the painting of toolbar button borders on ocean
    private static final boolean isMetalLaF =
            MetalLookAndFeel.class.isAssignableFrom(UIManager.getLookAndFeel().getClass());

    private static final boolean isFlatLaF =
            UIManager.getLookAndFeel().getID().startsWith("FlatLaf");

    private static final boolean isWindowsLaF =
            UIManager.getLookAndFeel().getID().equals("Windows");
    
    static final long serialVersionUID = 5011742660516204764L;
    static {
        try {
            Class.forName(AcceleratorBinding.class.getName());
        } catch (ClassNotFoundException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    /** Create a new Toolbar with empty name. */
    public Toolbar () {
        this (""); // NOI18N
    }

    /** Create a new not floatable Toolbar with programmatic name.
     * Display name is set to be the same as name */
    public Toolbar (String name) {
        this (name, name, false);
    }

    /** Create a new not floatable Toolbar with specified programmatic name
     * and display name */
    public Toolbar (String name, String displayName) {
        this (name, displayName, false);
    }
    
    /** Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     * Display name of the toolbar is set equal to the name.
     */
    public Toolbar (String name, boolean f) {
        this (name, name, f);
    }

    private JButton label;
    Toolbar(DataFolder folder) {
        super();
        backingFolder = folder;
        initAll(folder.getName(), false);
        putClientProperty("folder", folder); //NOI18N
    }

    @Override
    public boolean isOpaque() {
        if( null != UIManager.get("NbMainWindow.showCustomBackground") ) //NOI18N
            return !UIManager.getBoolean("NbMainWindow.showCustomBackground"); //NOI18N
        return super.isOpaque();
    }
    
    DataFolder getFolder() {
        return backingFolder;
    }

    /** Start tracking content of the underlaying folder if not doing so yet */
    final Folder waitFinished() {
        // check for too early call (from constructor and UI.setUp...)
        if (backingFolder == null) return null;
        
        synchronized (this) {
            if (processor == null && isVisible()) {
                processor = new Folder(); // It will start tracking immediatelly
            }
            return processor;
        }
    }    
    
    @Override
    public void addNotify() {
        super.addNotify();
        waitFinished();
    }
    
    @Override
    public Component[] getComponents () {
        waitFinished ();
        return super.getComponents ();
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        waitFinished();
    }
    
    private static final Insets emptyInsets = new Insets(1,1,1,1);
    /** Overridden to set focusable to false for any AbstractButton
     * subclasses which are added */
    @Override
    protected void addImpl(Component c, Object constraints, int idx) {
        //issue 39896, after opening dialog from toolbar button, focus
        //remains on toolbar button.  Does not create an accessibility issue - 
        //all standard toolbar buttons are also available via the keyboard
        if (c instanceof AbstractButton) {
            c.setFocusable(false);
            ((JComponent) c).setOpaque(false);
            if (isMetalLaF) {
                //metal/ocean resets borders, so fix it this way
                ((AbstractButton) c).setBorderPainted(false);
                ((AbstractButton) c).setOpaque(false);
            }
            if (isWindowsLaF) {
                ((AbstractButton) c).setMargin(new Insets(2,1,0,1));
            } else if (!isMetalLaF && !isFlatLaF) {
                ((AbstractButton) c).setMargin( emptyInsets );
            }
            if( null != label && c != label ) {
                remove( label );
                label = null;
            }
        } else if( c instanceof JToolBar.Separator ) {
            JToolBar.Separator separator = (JToolBar.Separator)c;
            if (getOrientation() == VERTICAL) {
                separator.setOrientation(JSeparator.HORIZONTAL);
            } else {
                separator.setOrientation(JSeparator.VERTICAL);
            }
        }
        
        super.addImpl (c, constraints, idx);
    }


    /**
     * Create a new <code>Toolbar</code>.
     * @param name a <code>String</code> containing the associated name
     * @param f specified if Toolbar is floatable
     */
    public Toolbar (String name, String displayName, boolean f) {
        super();
        setDisplayName (displayName);
        initAll(name, f);
    }
    
    /** Returns basic toolbar height according to preferred icons size. Used by
     * toolbar layout manager.
     * @return basic toolbar height
     * @since 4.15
     * @deprecated Returns preferred icon size.
     */
    @Deprecated
    public static int getBasicHeight () {
        return ToolbarPool.getDefault().getPreferredIconSize();
    }
    
    private void initAll(String name, boolean f) {
        setName (name);
        
        setFloatable (f);

        getAccessibleContext().setAccessibleName(displayName == null ? getName() : displayName);
        getAccessibleContext().setAccessibleDescription(getName());
    }

    @Override
    public String getUIClassID() {
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }

    /** Compute with HEIGHT_TOLERANCE number of rows for specific toolbar height.
     * @param height of some toolbar
     * @return number of rows
     * @deprecated Always returns 1
     */
    @Deprecated
    public static int rowCount (int height) {
        return 1;
    }

    /** Set DnDListener to Toolbar.
     * @param l DndListener for toolbar
     * @deprecated
     */
    @Deprecated
    public void setDnDListener (DnDListener l) {
        //NOOP
    }

    /**
     *
     * @param dx
     * @param dy
     * @param type
     * @deprecated There is no public support for toolbar drag and drop.
     */
    @Deprecated
    protected void fireDragToolbar (int dx, int dy, int type) {
        //NOOP
    }

    /**
     *
     * @param dx
     * @param dy
     * @param type
     * @deprecated There is no public support for toolbar drag and drop.
     */
    @Deprecated
    protected void fireDropToolbar (int dx, int dy, int type) {
        //NOOP
    }

    /** @return Display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public String getDisplayName () {
        if (displayName == null) {
            if (backingFolder.isValid()) {
                try {
                    return backingFolder.getNodeDelegate ().getDisplayName ();
                } catch (IllegalStateException ex) {
                    // OK: #141387
                }
            }
            // #17020
            return backingFolder.getName();
        }
        return displayName;
    }
    
    /** Sets new display name of this toolbar. Display name is localizable,
     * on the contrary to the programmatic name */
    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * This class can be used to produce a <code>Toolbar</code> instance from
     * the given <code>DataFolder</code>.
     */
    final class Folder extends FolderInstance {

        /**
         * Creates a new folder on the specified <code>DataFolder</code>.
         *
         */
        public Folder () {
            super (backingFolder);
            DataObjectAccessor.DEFAULT.precreateInstances(this);
            recreate ();
            acceleratorBindingsWarmUp();
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        @Override
        public String instanceName () {
            return Toolbar.this.getClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        @Override
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return Toolbar.this.getClass();
        }

        private Map<Object,DataObject> cookiesToObjects = new HashMap<Object,DataObject>();
    
        @Override
        protected Object instanceForCookie (DataObject obj, InstanceCookie cookie)
            throws IOException, ClassNotFoundException {
            Object result = super.instanceForCookie(obj, cookie);
            cookiesToObjects.put (result, obj);
            return result;
        }
        

        /**
         * Accepts only cookies that can provide <code>Toolbar</code>.
         * @param cookie an <code>InstanceCookie</code> to test
         * @return true if the cookie can provide accepted instances
         */
        @Override
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
            throws IOException, ClassNotFoundException {
            boolean is;
            boolean action;
            
            if (cookie instanceof InstanceCookie.Of) {
                InstanceCookie.Of of = (InstanceCookie.Of)cookie;
                action = of.instanceOf (Action.class);
                is = of.instanceOf (Component.class) ||
                     of.instanceOf (Presenter.Toolbar.class) ||
                     action;
            } else {
                Class c = cookie.instanceClass();
                action = Action.class.isAssignableFrom (c);
                is = Component.class.isAssignableFrom(c) ||
                     Presenter.Toolbar.class.isAssignableFrom(c) ||
                     action;
            }
            if (action) {
                cookie.instanceCreate();
            }
            
            return is ? cookie : null;
        }

        /**
         * Returns a <code>Toolbar.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Toolbar.Folder</code> for the specified folder
         */
        @Override
        protected InstanceCookie acceptFolder(DataFolder df) {
            return null; // PENDING new Toolbar.Folder(df);
        }

        /**
         * Updates the <code>Toolbar</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance(final InstanceCookie[] cookies)
            throws IOException, ClassNotFoundException {
            // refresh the toolbar's content
            Toolbar.this.removeAll();
            for (int i = 0; i < cookies.length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();
                    DataObject file = cookiesToObjects.get(obj);

                    if (obj instanceof Presenter.Toolbar) {
                        if (obj instanceof Action && file != null) {
                            AcceleratorBinding.setAccelerator((Action)obj, file.getPrimaryFile());
                        }
                        obj = ((Presenter.Toolbar) obj).getToolbarPresenter();
                    }
                    if (obj instanceof Component) {
                        // remove border and grip if requested. "Fixed" toolbar
                        // item has to live alone in toolbar now
                        if ((obj instanceof JComponent) &&
                            "Fixed".equals(((JComponent) obj).getClientProperty("Toolbar"))) {
                            org.openide.awt.Toolbar.this.removeAll();
                            setBorder(null);
                        }
                        if (obj instanceof javax.swing.JComponent) {
                            if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
                                ((JComponent) obj).putClientProperty("PreferredIconSize", new Integer(24));
                            }
                            ((JComponent) obj).putClientProperty("file", file);
                        }
                        Toolbar.this.add((Component) obj);
                        continue;
                    }
                    if (obj instanceof Action) {
                        Action a = (Action) obj;
                        AbstractButton b;
                        if (a.getValue(Actions.ACTION_VALUE_TOGGLE) != null) {
                            b = new DefaultIconToggleButton();
                        } else {
                            b = new DefaultIconButton();
                        }

                        if (ToolbarPool.getDefault().getPreferredIconSize() == 24) {
                            b.putClientProperty("PreferredIconSize", new Integer(24));
                        }
                        if (null == a.getValue(Action.SMALL_ICON)
                            && (null == a.getValue(Action.NAME) || a.getValue(Action.NAME).toString().length() == 0)) {
                            a.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/openide/loaders/unknown.gif", false));
                        }
                        org.openide.awt.Actions.connect(b, a);
                        b.putClientProperty("file", file);
                        org.openide.awt.Toolbar.this.add(b);
                        if (file != null) {
                            AcceleratorBinding.setAccelerator(a, file.getPrimaryFile());
                        }
                        continue;
                    }
                }
                catch (java.io.IOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                catch (java.lang.ClassNotFoundException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
                finally {
                    cookiesToObjects.clear();
                }
            }
            if( cookies.length == 0 ) {
                label = new JButton("<"+Actions.cutAmpersand(getDisplayName())+">");
                Toolbar.this.add(label);
            }

            // invalidate the toolbar, trigger proper relayout
            Toolbar.this.invalidate ();
            return Toolbar.this;
        }

        /** Recreate the instance in AWT thread.
        */
        @Override
        protected Task postCreationTask (Runnable run) {
            return new AWTTask (run, this);
        }
        
    } // end of inner class Folder

    private static Action emptyAction;
    
    private static void acceleratorBindingsWarmUp() {
        if( null == emptyAction ) {
            emptyAction = new Action() {

                @Override
                public Object getValue(String key) {
                    return null;
                }

                @Override
                public void putValue(String key, Object value) {
                }

                @Override
                public void setEnabled(boolean b) {
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
            AcceleratorBinding.setAccelerator(emptyAction, FileUtil.getConfigRoot());
        }
    }

    @Override
    public void setUI(javax.swing.plaf.ToolBarUI ui) {
        super.setUI(ui);
        if( null != backingFolder && null != processor ) {
            //recreate the toolbar buttons as their borders need to be reset
            processor.recreate();
        }
    }
    

    /** DnDListener is Drag and Drop listener for Toolbar motion events.
     * @deprecated There is no public support for toolbar drag and drop.
     */
    @Deprecated
    public interface DnDListener extends EventListener {
        /** Invoced when toolbar is dragged. */
        public void dragToolbar (DnDEvent e);

        /** Invoced when toolbar is dropped. */
        public void dropToolbar (DnDEvent e);
    } // end of interface DnDListener


    /** DnDEvent is Toolbar's drag and drop event. 
     * @deprecated
     */
    @Deprecated
    public static class DnDEvent extends EventObject {
        /** Type of DnDEvent. Dragging with only one Toolbar. */
        public static final int DND_ONE  = 1;
        /** Type of DnDEvent. Only horizontal dragging with Toolbar and it's followers. */
        public static final int DND_END  = 2;
        /** Type of DnDEvent. Only vertical dragging with whole lines. */
        public static final int DND_LINE = 3;

        /** Name of toolbar where event occured. */
        private String name;
        /** distance of horizontal dragging */
        private int dx;
        /** distance of vertical dragging */
        private int dy;
        /** Type of event. */
        private int type;

        static final long serialVersionUID =4389530973297716699L;
        public DnDEvent (Toolbar toolbar, String name, int dx, int dy, int type) {
            super (toolbar);

            this.name = name;
            this.dx = dx;
            this.dy = dy;
            this.type = type;
        }

        /** @return name of toolbar where event occured. */
        public String getName () {
            return name;
        }

        /** @return distance of horizontal dragging */
        public int getDX () {
            return dx;
        }

        /** @return distance of vertical dragging */
        public int getDY () {
            return dy;
        }

        /** @return type of event. */
        public int getType () {
            return type;
        }
    } // end of class DnDEvent

    /**
     * A button that provides a default icon when no text and no custom icon have been set.
     */
    private static class DefaultIconButton extends JButton {
        private Icon unknownIcon;

        @Override
        public Icon getIcon() {
            Icon retValue = super.getIcon();
            if( null == retValue && (null == getText() || getText().length() == 0 ) ) {
                if (unknownIcon == null) {
                    unknownIcon = ImageUtilities.loadImageIcon("org/openide/loaders/unknown.gif", false); //NOI18N
                }
                retValue = unknownIcon;
            }
            return retValue;
        }
    }

    /**
     * A button that provides a default icon when no text and no custom icon have been set.
     */
    private static class DefaultIconToggleButton extends JToggleButton {
        private Icon unknownIcon;

        @Override
        public Icon getIcon() {
            Icon retValue = super.getIcon();
            if( null == retValue && (null == getText() || getText().length() == 0 ) ) {
                if (unknownIcon == null) {
                    unknownIcon = ImageUtilities.loadImageIcon("org/openide/loaders/unknown.gif", false); //NOI18N
                }
                retValue = unknownIcon;
            }
            return retValue;
        }
    }

} // end of class Toolbar
