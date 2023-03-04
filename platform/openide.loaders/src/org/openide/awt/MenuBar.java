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
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderInstance;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** An extended version of swing's JMenuBar. This menubar can
 * load its content from the folder where its "disk image" is stored.<P>
 * Moreover, menu is <code>Externalizable</code> to restore its persistent
 * state with minimal storage expensiveness.
 *
 * The MenuBar recognizes following objects in the folder: <UL>
 * <LI>subfolders - they're turned into top-level JMenu instances
 * <LI>instances of <CODE>Component</CODE> - they're added directly
 *  to the menubar.
 * <LI>instances of <CODE>Presenter.Toolbar</CODE> - their toolbar presenter
 *  is added to the menubar.
 * </UL>
 * before OpenAPI version 3.2, only subfolders were recognized.
 *
 * <P>In subfolders the following objects are recognized and added to submenus:<UL>
 * <LI>nested subfolders - they're turned into submenus
 * <LI>instances of <CODE>Presenter.Menu</CODE>
 * <LI>instances of <CODE>JMenuItem</CODE>
 * <LI>instances of <CODE>JSeparator</CODE>
 * <LI>instances of <CODE>Action</CODE>
 * <LI>executable <CODE>DataObject</CODE>s
 * </UL>
 *
 * @author  David Peroutka, Dafe Simonek, Petr Nejedly
 */
public class MenuBar extends JMenuBar implements Externalizable {

    /** the folder which represents and loads content of the menubar */
    private MenuBarFolder menuBarFolder;

    /*
    private static final Icon BLANK_ICON = new ImageIcon(
        Utilities.loadImage("org/openide/loaders/empty.gif")); // NOI18N            
     */

    static final long serialVersionUID =-4721949937356581268L;
    static {
        try {
            Class.forName(AcceleratorBinding.class.getName());
        } catch (ClassNotFoundException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    // -J-Dorg.openide.awt.MenuBar.level=FINE
    private static final Logger LOG = Logger.getLogger(MenuBar.class.getName());

    /** Don't call this constructor or this class will not get
     * initialized properly. This constructor is only for externalization.
     */
    public MenuBar() {
        super();
    }

    /** Creates a new <code>MenuBar</code> from given folder.
     * @param folder The folder from which to create the content of the menubar.
     * If the parameter is null, default menu folder is obtained.
     */
    public MenuBar(DataFolder folder) {
        this();
        DataFolder theFolder = folder;
        if (theFolder == null) {
            FileObject root = FileUtil.getConfigRoot();
            FileObject fo = null;
            try {
                fo = FileUtil.createFolder(root, "Menu"); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (fo == null) throw new IllegalStateException("No Menu/"); // NOI18N
            theFolder = DataFolder.findFolder(fo);
        }
        startLoading(theFolder);
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    updateUI();
                }
            });
        }

        if(folder != null) {
            getAccessibleContext().setAccessibleDescription(folder.getName());
        }
    }

    @Override
    public boolean isOpaque() {
        if( null != UIManager.get("NbMainWindow.showCustomBackground") ) //NOI18N
            return !UIManager.getBoolean("NbMainWindow.showCustomBackground"); //NOI18N
        return super.isOpaque();
    }

    @Override
    public void updateUI() {
        if (EventQueue.isDispatchThread()) {
            super.updateUI();
            String laf = UIManager.getLookAndFeel().getID();
            // Let GTK supply some border, or mnemonic underlines.
            boolean gtk = laf.equals("GTK");
            boolean windows = laf.equals("Windows");
            if (!(gtk || windows)) {
                setBorder(BorderFactory.createEmptyBorder());
            }
            if (windows) {
                // Ensure that Windows8LFCustoms can provide a custom border here.
                setBorderPainted(true);
            }
        }
    }

    @Override
    public int getMenuCount() {
        if (menuBarFolder != null && !Thread.holdsLock(getTreeLock())) {
            menuBarFolder.waitFinished();
        }
        return super.getMenuCount();
    }
    
    public @Override void addImpl (Component c, Object constraint, int idx) {
        //Issue 17559, Apple's screen menu bar implementation blindly casts
        //added components as instances of JMenu.  Silently ignore any non-menu
        //items on Mac if the screen menu flag is true.
        if (Utilities.isMac() && 
                Boolean.getBoolean ("apple.laf.useScreenMenuBar")) { //NOI18N
            if (!(c instanceof JMenu)) {
                return;
            }
        }
        super.addImpl (c, constraint, idx);
    }
    
    /** Blocks until the menubar is completely created. */
    public void waitFinished () {
        menuBarFolder.instanceFinished();
    }
    
    /** Saves the contents of this object to the specified stream.
     *
     * @exception IOException Includes any I/O exceptions that may occur
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(menuBarFolder.getFolder());
    }

    /**
     * Restores contents of this object from the specified stream.
     *
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        startLoading((DataFolder)in.readObject());
    }
    
    /** Starts loading of this menu from menu folder */
    void startLoading (final DataFolder folder) {
        menuBarFolder = new MenuBarFolder(folder);
    }
    
    /** Convert an array of instance cookies to instances, adds them
     * to given list.
     * @param arr array of instance cookies
     * @param list list to add created objects to
     */
    static void allInstances (InstanceCookie[] arr, java.util.List<Object> list) {
        Exception ex = null;
        
        for (int i = 0; i < arr.length; i++) {
            
            Exception newEx = null;
            try {
                Object o = arr[i].instanceCreate();
                if (o == LazyMenu.SEPARATOR) {
                    o = new JSeparator();
                }
                list.add (o);
            } catch (ClassNotFoundException e) {
                newEx = e;
            } catch (IOException e) {
                newEx = e;
            }
            
            if (newEx != null) {
                Throwable t = newEx;
                while (true) {
                    if (t.getCause() == null) {
                        if (t instanceof ClassNotFoundException) {
                            newEx = new ClassNotFoundException(t.getMessage(), ex);
                            newEx.setStackTrace(t.getStackTrace());
                        } else {
                            t.initCause(ex);
                        }
                        break;
                    }
                    t = t.getCause();
                }
                ex = newEx;
            }
        }
     
        // if there was an exception => notify it
        if (ex != null) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This class can be used to fill the content of given
     * <code>MenuBar</code> from the given <code>DataFolder</code>.
     */
    private final class MenuBarFolder extends FolderInstance {
        /** List of the components this FolderInstance manages. */
        private ArrayList<Component> managed;
        private List<Object> instances;

        /** Creates a new menubar folder on the specified <code>DataFolder</code>.
         * @param folder a <code>DataFolder</code> to work with
         */
        public MenuBarFolder (final DataFolder folder) {
            super(folder);
            DataObjectAccessor.DEFAULT.precreateInstances(this);
            // preinitialize outside of AWT
            new DynaMenuModel();
            recreate ();
        }

        /** Removes the components added by this FolderInstance from the MenuBar.
         * Called when menu is refreshed. */
        private void cleanUp() {
            synchronized (getTreeLock()) {
                for (Iterator<Component> it = getManaged().iterator(); it.hasNext(); ) {
                    MenuBar.this.remove(it.next());
                }
                getManaged().clear();
            }
        }

        /** Adds the component to the MenuBar after the last added one */
        private void addComponent (Component c) {
            if (c == null) {
                c = createNoComponent();
            }
            synchronized (getTreeLock()) {
                MenuBar.this.add(c, getManaged().size());
                getManaged().add(c);
            }
        }

        private void addComponent (Component c, int index) {
            if (c == null) {
                c = createNoComponent();
            }
            synchronized (getTreeLock()) {
                MenuBar.this.add(c, index);
                getManaged().add(index, c);
            }
        }

        private void removeComponent (int index) {
            synchronized (getTreeLock()) {
                getManaged().remove(index);
                MenuBar.this.remove(index);
            }
        }

        private Component createNoComponent() {
            JComponent noComponent = new JComponent() {};
            noComponent.setSize(0, 0);
            noComponent.setVisible(false);
            return noComponent;
        }

        /** Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        public @Override String instanceName () {
            return MenuBar.class.getName();
        }

        /** Returns the root class of all objects.
         * @return MenuBar.class
         */
        public @Override Class instanceClass () {
            return MenuBar.class;
        }

        private Map<Object,DataObject> cookiesToObjects = new HashMap<Object,DataObject>();

        @Override
        protected Object instanceForCookie (DataObject obj, InstanceCookie cookie)
            throws IOException, ClassNotFoundException {
            Object result = super.instanceForCookie(obj, cookie);
            cookiesToObjects.put (result, obj);
            return result;
        }

        /** Accepts only cookies that can provide a <code>Component</code>
         * or a <code>Presenter.Toolbar</code>.
         * @param cookie the instance cookie to test
         * @return true if the cookie is accepted.
         */
        protected @Override InstanceCookie acceptCookie(InstanceCookie cookie)
                throws IOException, ClassNotFoundException {
            Class cls = cookie.instanceClass();
            boolean is =
                    Component.class.isAssignableFrom(cls) ||
                    Presenter.Toolbar.class.isAssignableFrom(cls) ||
                    Action.class.isAssignableFrom(cls);
            return is ? cookie : null;
        }

        /** Returns an <code>InstanceCookie</code> of a JMenu
	 * for the specified <code>DataFolder</code>.
	 *
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return an <code>InstanceCookie</code> for the specified folder
         */
        protected @Override InstanceCookie acceptFolder (DataFolder df) {
            return new LazyMenu(df, false).slave;
        }

        /** Updates the <code>MenuBar</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>MenuBar</code> representee
         */
        protected Object createInstance(InstanceCookie[] cookies)
                throws IOException, ClassNotFoundException {
            final List<Object> ll = new ArrayList<Object>(cookies.length);
            allInstances(cookies, ll);

            final MenuBar mb = MenuBar.this;
            
            List<Object> lo = instances;
            if (lo == null) {
                lo = Collections.EMPTY_LIST;
            }
            boolean modified = false;
            int i = 0, j = 0;
            try {
                for ( ; i < lo.size() && j < ll.size(); i++, j++) {
                    if (Objects.equals(lo.get(i), ll.get(j))) {
                        continue;
                    }
                    Object instance = ll.get(j);
                    // has lo instance?
                    int k;
                    for (k = i+1; k < lo.size(); k++) {
                        if (Objects.equals(lo.get(k), instance)) {
                            break;
                        }
                    }
                    if (k < lo.size()) {
                        // Remove all components <i, k)
                        while (i < k) {
                            removeComponent(j);
                            i++;
                        }
                    } else {
                        // a new instance
                        Component component = convertToComponent(instance);
                        addComponent(component, j);
                        i--;
                    }
                    modified = true;
                }
                while (i < lo.size()) {
                    removeComponent(j);
                    i++;
                    modified = true;
                }
                while (j < ll.size()) {
                    Component component = convertToComponent(ll.get(j));
                    addComponent(component);
                    j++;
                    modified = true;
                }
            } finally {
                cookiesToObjects.clear();
            }
            instances = ll;
            if (modified) {
                mb.validate();
                mb.repaint();
            }
            return mb;
        }

        private Component convertToComponent(final Object obj) {
            Component retVal = null;
            if (obj instanceof Component) {
                retVal = (Component)obj;                
            } else {
                if (obj instanceof Presenter.Toolbar) {
                    DataObject file = cookiesToObjects.get(obj);
                    if (obj instanceof Action && file != null) {
                        AcceleratorBinding.setAccelerator((Action)obj, file.getPrimaryFile());
                    }
                    retVal = ((Presenter.Toolbar)obj).getToolbarPresenter();
                } else if (obj instanceof Action) {
                    Action a = (Action) obj;
                    JButton button = new JButton();
                    Actions.connect(button, a);
                    retVal = button;                    
                }                
            }
            if (retVal instanceof JButton) { // tune the presenter a bit
                ((JButton)retVal).setBorderPainted(false);
                ((JButton)retVal).setMargin(new java.awt.Insets(0, 2, 0, 2));
            }
            return retVal;
        }
        
        /** For outer class access to the data folder */
        DataFolder getFolder () {
            return folder;
        }

        /** Recreate the instance in AWT thread. */
        protected @Override Task postCreationTask (Runnable run) {
            return new AWTTask (run, this);
        }

        /**
         * @return the managed
         */
        private ArrayList<Component> getManaged() {
            assert Thread.holdsLock(getTreeLock());
            if (managed == null) {
                 managed = new ArrayList<Component>();
            }
            return managed;
        }

    }
    
    /**
     * A marker class to allow different processing of remapped key events
     * on mac - allows them to be recognized by LazyMenu. 
     */
    private static final class MarkedKeyEvent extends KeyEvent {
        MarkedKeyEvent (Component c, int id, 
                    long when, int mods, int code, char kchar, 
                    int loc) {
            super(c, id, when, mods, code, kchar, loc);
        }
    }

    /** Menu based on the folder content whith lazy items creation. */
    private static class LazyMenu extends JMenu implements NodeListener, Runnable, ChangeListener {
        static final JSeparator SEPARATOR = new LazySeparator();
        static {
            // preinitialize outside of AWT
            new DynaMenuModel();
        }
        final DataFolder master;
        final boolean icon;
        final MenuFolder slave;
        final DynaMenuModel dynaModel;
	
        /** Constructor. */
        public LazyMenu(final DataFolder df, boolean icon) {
            this.master = df;
            this.icon = icon;
            this.dynaModel = new DynaMenuModel();
            this.slave = new MenuFolder();
            
            setName(df.getName());
            final FileObject pf = df.getPrimaryFile();
            Object prefix = pf.getAttribute("property-prefix"); // NOI18N
            if (prefix instanceof String) {
                Enumeration<String> en = pf.getAttributes();
                while (en.hasMoreElements()) {
                    String attrName = en.nextElement();
                    if (attrName.startsWith((String)prefix)) {
                        putClientProperty(
                            attrName.substring(((String)prefix).length()), 
                            pf.getAttribute(attrName)
                        );
                    }
                }
            }

            // Listen for changes in Node's DisplayName/Icon
            Node n = master.getNodeDelegate ();
            n.addNodeListener (org.openide.nodes.NodeOp.weakNodeListener (this, n));
            Mutex.EVENT.readAccess(this);
            getModel().addChangeListener(this);
        }

        @Override
        public void updateUI() {
            if (EventQueue.isDispatchThread()) {
                super.updateUI();
            } else {
                Mutex.EVENT.readAccess(this);
            }
        }
        @Override
        public int getItemCount() {
            conditionalInitialize();
            return super.getItemCount();
        }

        @Override
        public int getMenuComponentCount() {
            conditionalInitialize();
            return super.getMenuComponentCount();
        }

        @Override
        public Component[] getMenuComponents() {
            conditionalInitialize();
            return super.getMenuComponents();
        }
        
        private void conditionalInitialize() {
            if (Thread.holdsLock(getTreeLock())) {
                return;
            }
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                //#211284 - don't initialize menu items when the component tree is being refreshed on main wnd (de)activation
                for( StackTraceElement ste : Thread.currentThread().getStackTrace() ) {
                    if( "com.apple.laf.AquaRootPaneUI".equals(ste.getClassName()) ) { //NOI18N
                        if( "windowDeactivated".equals(ste.getMethodName()) ) { //NOI18N
                            return;
                        }
                        if( "windowActivated".equals(ste.getMethodName()) ) { //NOI18N
                            return;
                        }
                    }
                }
            }
            doInitialize();
        }
        
        protected @Override boolean processKeyBinding(KeyStroke ks,
                                        KeyEvent e,
                                        int condition,
                                        boolean pressed) {
            if (Utilities.isMac()) {
                int mods = e.getModifiers();
                boolean isCtrl = (mods & KeyEvent.CTRL_MASK) != 0;
                boolean isAlt = (mods & KeyEvent.ALT_MASK) != 0;
                if (isAlt && (e instanceof MarkedKeyEvent)) {
                    mods = mods & ~ KeyEvent.CTRL_MASK;
                    mods = mods & ~ KeyEvent.CTRL_DOWN_MASK;
                    mods |= KeyEvent.ALT_MASK;
                    mods |= KeyEvent.ALT_DOWN_MASK;
                    
                    KeyEvent newEvent = new MarkedKeyEvent (
                        (Component) e.getSource(), e.getID(), 
                        e.getWhen(), mods, e.getKeyCode(), e.getKeyChar(), 
                        e.getKeyLocation());
                    
                    KeyStroke newStroke = null;
                    if( null != ks ) {
                        newStroke = e.getID() == KeyEvent.KEY_TYPED ?
                            KeyStroke.getKeyStroke (ks.getKeyChar(), mods) :
                            KeyStroke.getKeyStroke (ks.getKeyCode(), mods,
                            !ks.isOnKeyRelease());
                    }
                    
                    boolean result = super.processKeyBinding (newStroke, 
                        newEvent, condition, pressed);
                    
                    if (newEvent.isConsumed()) {
                        e.consume();
                    }
                    return result;
                } else if (!isAlt) {
                    return super.processKeyBinding (ks, e, condition, pressed);
                } else {
                    return false;
                }
            } else {
                return super.processKeyBinding (ks, e, condition, pressed);
            }                     
        }            

        private void updateProps() {
            assert EventQueue.isDispatchThread();
            getModel().removeChangeListener(this);
            if (master.isValid()) {
                // set the text and be aware of mnemonics
                Node n = master.getNodeDelegate ();
                Mnemonics.setLocalizedText(this, n.getDisplayName());
                if (icon) setIcon (new ImageIcon (
                n.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16)));
            } else {
                setText(master.getName());
                setIcon(null);
            }
            getModel().addChangeListener(this);
        }

        /** Update the properties. Exported via Runnable interface so it
         * can be rescheduled. */
        @Override
        public void run() {
            if (master == null) {
                return;
            }
            updateUI();
            updateProps();
        }

        /** If the display name changes, than change the name of the menu.*/
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            if (
                Node.PROP_DISPLAY_NAME.equals (ev.getPropertyName ()) ||
                Node.PROP_NAME.equals (ev.getPropertyName ()) ||
                Node.PROP_ICON.equals (ev.getPropertyName ())
            ) {
                Mutex.EVENT.readAccess(this);
            }
        }

        // The rest of the NodeListener implementation
        public void childrenAdded (NodeMemberEvent ev) {}
        public void childrenRemoved (NodeMemberEvent ev) {}
        public void childrenReordered(NodeReorderEvent ev) {}
        public void nodeDestroyed (NodeEvent ev) {}
            
        private boolean selected = false;
    
        /* Used on Mac only where setPopupMenuVisible does not work */
        public void stateChanged(ChangeEvent event) {
            if (Utilities.isMac()) {
                if (selected) {
                    selected = false;
                } else {
                    selected = true;
                    doInitialize();
                    dynaModel.checkSubmenu(this);
                }
            }
        }
        

// mkleint: overriding setPopupMenuVisible doesn't work on mac, replaced by listening on changes of Button model.
        
        
        /** Overriden to provide better strategy for placing the JMenu on the screen on non Mac platforms.
        * @param b a boolean value -- true to make the menu visible, false to hide it
        */
        @Override
        public void setPopupMenuVisible(boolean b) {
            if (!Utilities.isMac()) {
                boolean isVisible = isPopupMenuVisible();
                
                if (b != isVisible) {
                    if (b) {
                        doInitialize();
                        dynaModel.checkSubmenu(this);
                    }
                }
            }
            super.setPopupMenuVisible(b);
        }
        
	private void doInitialize() {
        if (slave != null) {
            slave.waitFinishedSuper();
        }
	}
	    
	/** This class can be used to update a <code>JMenu</code> instance
	 * from the given <code>DataFolder</code>.
	 */
	private class MenuFolder extends FolderInstance {
            
    	    /**
             * Start tracking the content of the master folder.
             * It will cause initial update of the Menu
             */
    	    public MenuFolder () {
                super(master);
                DataObjectAccessor.DEFAULT.precreateInstances(this);
    	    }


    	    /** The name of the menu
             * @return the name
             */
    	    public @Override String instanceName () {
                return LazyMenu.class.getName();
    	    }

    	    /** Returns the class of represented menu.
             * @return JMenu.class
             */
    	    public @Override Class instanceClass () {
                return JMenu.class;
    	    }
            
            
            public @Override Object instanceCreate() throws IOException, ClassNotFoundException {
                return LazyMenu.this;
            }

            public @Override void waitFinished() {
//                super.waitFinished();
            }
            
            void waitFinishedSuper() {
                super.waitFinished();
            }
            
        private Map<Object,FileObject> cookiesToFiles = new HashMap<Object,FileObject>();

        @Override
        protected Object instanceForCookie(DataObject obj, InstanceCookie cookie) throws IOException, ClassNotFoundException {
            Object result;
            if (cookie.instanceClass().equals(JSeparator.class)) {
                result = SEPARATOR;
            } else {
                result = super.instanceForCookie(obj, cookie);
            }
            cookiesToFiles.put(result, obj.getPrimaryFile());
            return result;
        }

    	    /**
             * Accepts only cookies that can provide <code>Menu</code>.
             * @param cookie an <code>InstanceCookie</code> to test
             * @return true if the cookie can provide accepted instances
             */
    	    protected @Override InstanceCookie acceptCookie(InstanceCookie cookie)
    	    throws IOException, ClassNotFoundException {
		// [pnejedly] Don't try to optimize this by InstanceCookie.Of
		// It will load the classes few ms later from instanceCreate
		// anyway and more instanceOf calls take longer
            	Class c = cookie.instanceClass();
                boolean action = Action.class.isAssignableFrom (c);
                if (action) {
                    cookie.instanceCreate();
                }
            	boolean is =
                	Presenter.Menu.class.isAssignableFrom (c) ||
                	JMenuItem.class.isAssignableFrom (c) ||
                	JSeparator.class.isAssignableFrom (c) ||
                    action;
            	return is ? cookie : null;
    	    }

    	    /**
    	     * Returns a <code>Menu.Folder</code> cookie for the specified
    	     * <code>DataFolder</code>.
             * @param df a <code>DataFolder</code> to create the cookie for
    	     * @return a <code>Menu.Folder</code> for the specified folder
    	     */
    	    protected @Override InstanceCookie acceptFolder(DataFolder df) {
                boolean hasIcon = df.getPrimaryFile().getAttribute("SystemFileSystem.icon") != null;
            	return new LazyMenu(df, hasIcon).slave;
    	    }

    	    /** Updates the <code>JMenu</code> represented by this folder.
    	     * @param cookies array of instance cookies for the folder
    	     * @return the updated <code>JMenu</code> representee
    	     */
            protected Object createInstance(InstanceCookie[] cookies)
            throws IOException, ClassNotFoundException {
                LazyMenu m = LazyMenu.this;
                assert EventQueue.isDispatchThread() : Thread.currentThread().getName();

                //synchronized (this) { // see #15917 - attachment from 2001/09/27
                LinkedList<Object> cInstances = new LinkedList<Object>();
                allInstances(cookies, cInstances);

                // #11848, #13013. Enablement should be set immediatelly,
                // popup will be created on-demand.
                // m.setEnabled(!cInstances.isEmpty());
                // TODO: fill it with empty sign instead
                if (cInstances.isEmpty()) {
                    JMenuItem item = new JMenuItem(
                            NbBundle.getMessage(DataObject.class, "CTL_EmptyMenu"));

                    item.setEnabled(false);
                    m.add(item);
                }

                m.dynaModel.loadSubmenu(cInstances, m, true, cookiesToFiles);
                return m;
            }
            
            /** Removes icons from all direct menu items of this menu.
             * Not recursive, * /
            private List alignVertically (List menuItems) {
                List result = new ArrayList(menuItems.size());
                JMenuItem curItem = null;
                for (Iterator iter = menuItems.iterator(); iter.hasNext(); ) {
                    curItem = (JMenuItem)iter.next();
                    if (curItem != null && curItem.getIcon() == null) {
                        curItem.setIcon(BLANK_ICON);
                    }
                    result.add(curItem);
                }
                return result;
            }
             */

    	    /** Recreate the instance in AWT thread.
    	     */
    	    protected @Override Task postCreationTask(Runnable run) {
            	return new AWTTask (run, this);
    	    }
	}
    } // end of LazyMenu

    private static final class LazySeparator extends JSeparator
    implements Runnable {
        public LazySeparator() {
        }

        @Override
        public void updateUI() {
            if (EventQueue.isDispatchThread()) {
                super.updateUI();
            } else {
                Mutex.EVENT.readAccess(this);
            }
        }

        @Override
        public void run() {
            updateUI();
        }
    } // end of LazySeparator
}
