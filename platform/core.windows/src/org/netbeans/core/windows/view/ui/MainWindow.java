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

package org.netbeans.core.windows.view.ui;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.border.*;
import javax.swing.event.*;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.openide.LifecycleManager;
import org.openide.awt.*;
import org.openide.awt.MenuBar;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** The MainWindow of IDE. Holds toolbars, main menu and also entire desktop
 * if in MDI user interface. Singleton.
 * This class is final only for performance reasons, can be unfinaled
 * if desired.
 *
 * @author Ian Formanek, Petr Hamernik
 */
public final class MainWindow {
   /** generated Serialized Version UID */
   static final long serialVersionUID = -1160791973145645501L;

   private final JFrame frame;
   private final AutoHidingMenuBar autoHidingMenuBar;

   private static JMenuBar mainMenuBar;

   /** Desktop. */
   private Component desktop;

   /** Inner panel which contains desktop component */
   private JPanel desktopPanel;

   private static JPanel innerIconsPanel;

   /** Flag indicating main window is initialized. */
   private boolean inited;

   private Lookup.Result <SaveCookie> saveResult;
   private Lookup.Result <DataObject> dobResult;
   private LookupListener saveListener;

   private static MainWindow theInstance;

   private JPanel statusBarContainer = null;
   private JComponent statusBar;

   private static final Logger LOGGER = Logger.getLogger(MainWindow.class.getName());

   //update main window title bar when current document is modified (Mac OS X only)
   private final RequestProcessor RP = new RequestProcessor( "MainWndMac", 1 ); //NOI18N

   /** Constructs main window. */
   private MainWindow(JFrame frame) {
       this.frame = frame;
       this.autoHidingMenuBar = new AutoHidingMenuBar(frame);
   }

   public static MainWindow install( JFrame frame ) {
       synchronized( MainWindow.class ) {
           if( null != theInstance ) {
               LOGGER.log(Level.INFO, "Installing MainWindow again, existing frame is: " + theInstance.frame); //NOI18N
           }
           theInstance = new MainWindow(frame);
           return theInstance;
       }
   }

   public static MainWindow getInstance() {
       synchronized( MainWindow.class ) {
           if( null == theInstance ) {
               LOGGER.log(Level.INFO, "Accessing uninitialized MainWindow, using dummy JFrame instead." ); //NOI18N
               theInstance = new MainWindow(new JFrame());
           }
           return theInstance;
       }
   }

   public static void init() {
       if (mainMenuBar == null) {
           mainMenuBar = createMenuBar();
           ToolbarPool.getDefault().waitFinished();
           Toolkit toolkit = Toolkit.getDefaultToolkit();
           Class<?> xtoolkit = toolkit.getClass();
           //#183739 - provide proper app name on Linux
           if (xtoolkit.getName().equals("sun.awt.X11.XToolkit")) { //NOI18N
               try {
                    final Field awtAppClassName = xtoolkit.getDeclaredField("awtAppClassName"); //NOI18N
                    awtAppClassName.setAccessible(true);
                    awtAppClassName.set(null, NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title_No_Project", "").trim()); //NOI18N
               } catch (Exception x) {
                   LOGGER.log(Level.FINE, null, x);
               }
           }
           //#198639 - workaround for main menu & mouse issues in Gnome 3
           String session = System.getenv("DESKTOP_SESSION"); //NOI18N
           if ("gnome-shell".equals(session) || "gnome".equals(session) || "mate".equals(session)) { //NOI18N
               try {
                   Class<?> xwm = Class.forName("sun.awt.X11.XWM"); //NOI18N
                   Field awt_wmgr = xwm.getDeclaredField("awt_wmgr"); //NOI18N
                   awt_wmgr.setAccessible(true);
                   Field other_wm = xwm.getDeclaredField("OTHER_WM"); //NOI18N
                   other_wm.setAccessible(true);
                   if (awt_wmgr.get(null).equals(other_wm.get(null))) {
                       Field metacity_wm = xwm.getDeclaredField("METACITY_WM"); //NOI18N
                       metacity_wm.setAccessible(true);
                       awt_wmgr.set(null, metacity_wm.get(null));
                       LOGGER.info("installed #198639 workaround"); //NOI18N
                   }
               } catch (Exception x) {
                   LOGGER.log(Level.FINE, null, x);
               }
           }
       }

       logLookAndFeelUsage();
   }

   /** Initializes main window. */
   public void initializeComponents() {
       if(inited) {
           return;
       }
       inited = true;

       JPanel contentPane = new JPanel(new BorderLayout()) {
           @Override
           public void paint(Graphics g) {
               super.paint(g);
               LOGGER.log(Level.FINE,
                       "Paint method of main window invoked normally."); //NOI18N
               // XXX is this only needed by obsolete #24291 hack, or now needed independently?
               WindowManagerImpl.getInstance().mainWindowPainted();
           }

       };
       if( isShowCustomBackground() )
           contentPane.setOpaque( false );
       frame.setContentPane(contentPane);

       init();

       initRootPane();

       // initialize frame
       initFrameIcons(frame);

       initListeners();

       frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

       frame.getAccessibleContext().setAccessibleDescription(
               NbBundle.getBundle(MainWindow.class).getString("ACSD_MainWindow"));

       frame.setJMenuBar(mainMenuBar);

       if (!Constants.NO_TOOLBARS) {
           JComponent tb = getToolbarComponent();
           frame.getContentPane().add(tb, BorderLayout.NORTH);
       }

       if(!Constants.SWITCH_STATUSLINE_IN_MENUBAR) {
           if (Constants.CUSTOM_STATUS_LINE_PATH == null) {
               final boolean separateStatusLine = null == statusBarContainer;
               final JPanel statusLinePanel = new JPanel(new BorderLayout());
               if( isShowCustomBackground() )
                   statusLinePanel.setOpaque( false);
               int magicConstant = 0;
               if (Utilities.isMac()) {
                   // on mac there is window resize component in the right most bottom area.
                   // it paints over our icons..
                   magicConstant = 12;

                   if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                       if( separateStatusLine ) {
                       statusLinePanel.setBorder( BorderFactory.createCompoundBorder(
                               BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.darkShadow")), //NOI18N
                               BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.lightShadow") ) ) ); //NOI18N
                       } else {
                           statusLinePanel.setBorder( BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.darkShadow")) );
                       }
                   }
               }

               // status line should add some pixels on the left side
               statusLinePanel.setBorder(BorderFactory.createCompoundBorder(
                       statusLinePanel.getBorder(),
                       BorderFactory.createEmptyBorder (0, 0, 0, magicConstant)));

               if( !"Aqua".equals(UIManager.getLookAndFeel().getID())
                       && !UIManager.getBoolean( "NbMainWindow.StatusBar.HideSeparator" )
                       && separateStatusLine ) { //NOI18N
                   statusLinePanel.add(new JSeparator(), BorderLayout.NORTH);
               }
               if( separateStatusLine ) {
                    JLabel status = new StatusLine();
                    // XXX #19910 Not to squeeze status line.
                    status.setText(" "); // NOI18N
                    status.setPreferredSize(new Dimension(0, status.getPreferredSize().height));
                    // text in line should be shifted for 4pix.
                    status.setBorder (BorderFactory.createEmptyBorder (0, 15, 0, 0));

                    statusLinePanel.add(status, BorderLayout.CENTER);
               }

               WindowManager.getDefault().invokeWhenUIReady( new Runnable() {
                   @Override
                   public void run() {
                        decoratePanel (statusLinePanel, false);
                   }
               });
               statusLinePanel.setName("statusLine"); //NOI18N
               statusBar = statusLinePanel;
               if( separateStatusLine ) {
                    frame.getContentPane().add (statusLinePanel, BorderLayout.SOUTH);
               } else {
                   statusBarContainer.add( statusLinePanel, BorderLayout.CENTER );
                   AutoHideStatusText.install( frame, statusBarContainer );
               }
           } else { // custom status line provided
               JComponent status = getCustomStatusLine();
               if (status != null) {
                   frame.getContentPane().add(status, BorderLayout.SOUTH);
               }
           }
       }

       frame.getContentPane().add(getDesktopPanel(), BorderLayout.CENTER);

       //#38810 start - focusing the main window in case it's not active and the menu is
       // selected..
       MenuSelectionManager.defaultManager().addChangeListener(new ChangeListener(){
           @Override
           public void stateChanged(ChangeEvent e) {
               MenuElement[] elems = MenuSelectionManager.defaultManager().getSelectedPath();
               if (elems != null && elems.length > 0) {
                   if (elems[0] == frame.getJMenuBar()) {
                       if (!frame.isActive()) {
                           frame.toFront();
                       }
                   }
               }
           }
       });
       //#38810 end
       String title = NbBundle.getMessage(MainWindow.class, "CTL_MainWindow_Title_No_Project", System.getProperty("netbeans.buildnumber")); //NOI18N
       if( !title.isEmpty() )
           frame.setTitle(title);
       if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
           //Show a "save dot" in the close button if a modified file is
           //being edited
           //Show the icon of the edited file in the window titlebar like
           //other mac apps
           saveResult = Utilities.actionsGlobalContext().lookupResult (SaveCookie.class);
           dobResult = Utilities.actionsGlobalContext().lookupResult (DataObject.class);
           if( null != saveResult && null != dobResult ) {
               saveListener = new LookupListener() {

                   private final Object lock = new Object();
                   private LookupEvent lastEvent = null;

                   private final RequestProcessor.Task updateTask = RP.create(
                           new Runnable() {

                       @Override
                       public void run() {
                           LookupEvent ev;
                           synchronized (lock) {
                               ev = lastEvent;
                           }
                           if (ev != null) {
                               updateMacDocumentProperties(ev);
                           }
                           synchronized (lock) {
                               if (lastEvent == ev) {
                                   lastEvent = null;
                               }
                           }
                       }
                   });

                   @Override
                   public void resultChanged(final LookupEvent ev) {

                       synchronized (lock) {
                           lastEvent = ev;
                       }
                       updateTask.schedule(250);
                   }
               };
               saveResult.addLookupListener(saveListener);
               dobResult.addLookupListener(saveListener);
           }
           dobResult.allItems();
       }
   }

   private void updateMacDocumentProperties( LookupEvent ev ) {
        if (ev.getSource() == saveResult) {
            final boolean modified = saveResult.allItems().size() > 0;
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    frame.getRootPane().putClientProperty ("Window.documentModified", //NOI18N
                            modified ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        } else if (ev.getSource() == dobResult) {
            final File[] documentFile = new File[1];
            Collection<? extends Lookup.Item<DataObject>> allItems = dobResult.allItems();
            if( 1 == allItems.size() ) {
                DataObject dob = allItems.iterator().next().getInstance();
                if( null != dob ) {
                    FileObject file = dob.getPrimaryFile();
                    documentFile[0] = FileUtil.toFile( file );
                }
            }
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    frame.getRootPane().putClientProperty("Window.documentFile", documentFile[0]); //NOI18N
                }
            });
        }
   }

   private static void decoratePanel (JPanel panel, boolean safeAccess) {
       assert safeAccess || SwingUtilities.isEventDispatchThread () : "Must run in AWT queue.";
       if (innerIconsPanel != null) {
           panel.remove (innerIconsPanel);
       }
       innerIconsPanel = getStatusLineElements (panel);
       if (innerIconsPanel != null) {
           panel.add (innerIconsPanel, BorderLayout.EAST);
       }
       if( isShowCustomBackground() )
           panel.setOpaque( false );
   }

   private static Lookup.Result<StatusLineElementProvider> result;

   // package-private because StatusLineElementProviderTest
   static JPanel getStatusLineElements (JPanel panel) {
       // bugfix #56375, don't duplicate the listeners
       if (result == null) {
           result = Lookup.getDefault ().lookup (
                   new Lookup.Template<StatusLineElementProvider> (StatusLineElementProvider.class));
           result.addLookupListener (new StatusLineElementsListener (panel));
       }
       Collection<? extends StatusLineElementProvider> c = result.allInstances ();
       if (c == null || c.isEmpty ()) {
           return null;
       }
       final Iterator<? extends StatusLineElementProvider> it = c.iterator ();
       final JPanel icons = new JPanel (new FlowLayout (FlowLayout.RIGHT, 0, 0));
       if( isShowCustomBackground() )
           icons.setOpaque( false );
       icons.setBorder (BorderFactory.createEmptyBorder (1, 0, 0, 2));
       final boolean[] some = new boolean[1];
       some[0] = false;
       Runnable r = new Runnable() {
            @Override
            public void run() {
               while (it.hasNext ()) {
                   StatusLineElementProvider o = it.next ();
                   Component comp = o.getStatusLineElement ();
                   if (comp != null) {
                       some[0] = true;
                       icons.add (comp);
                   }
               }
            }
       };
       if( !SwingUtilities.isEventDispatchThread() ) {
           SwingUtilities.invokeLater( r );
           return icons;
       } else {
           r.run();
       }
       return some[0] ? icons : null;
   }

   protected void initRootPane() {
       JRootPane root = frame.getRootPane();
       if( null == root )
           return;
       HelpCtx.setHelpIDString(
               root, new HelpCtx(MainWindow.class).getHelpID());
       if (Utilities.isWindows()) {
           // use glass pane that will not cause repaint/revalidate of parent when set visible
           // is called (when setting wait cursor in ModuleActions) #40689
           JComponent c = new JPanel() {
               @Override
               public void setVisible(boolean flag) {
                   if (flag != isVisible ()) {
                       super.setVisible(flag);
                   }
               }
           };
           c.setName(root.getName()+".nbGlassPane");  // NOI18N
           c.setVisible(false);
           ((JPanel)c).setOpaque(false);
           root.setGlassPane(c);
       }
   }


   //delegate some JFrame methods for convenience

   public void setBounds(Rectangle bounds) {
       frame.setBounds(bounds);
   }

   public void setExtendedState(int extendedState) {
       frame.setExtendedState(extendedState);
   }

   public void setVisible(boolean visible) {
        if ("false".equals(System.getProperty("org.netbeans.core.WindowSystem.show"))) { // NOI18N
            return;
        }
        frame.setVisible(visible);
   }

   public int getExtendedState() {
       return frame.getExtendedState();
   }

   public JMenuBar getJMenuBar() {
       return frame.getJMenuBar();
   }

    void setStatusBarContainer( JPanel panel ) {
        assert null != panel;
        assert panel.getLayout() instanceof BorderLayout;
        this.statusBarContainer = panel;
        if( null != statusBar ) {
            statusBarContainer.add( statusBar, BorderLayout.CENTER );
        }
    }

   private static class StatusLineElementsListener implements LookupListener {
       private JPanel decoratingPanel;
       StatusLineElementsListener (JPanel decoratingPanel) {
           this.decoratingPanel = decoratingPanel;
       }
       @Override
       public void resultChanged (LookupEvent ev) {
           SwingUtilities.invokeLater (new Runnable () {
               @Override
               public void run () {
                   decoratePanel (decoratingPanel, false);
               }
           });
       }
   }

   /** Creates and returns border for desktop which is visually aligned
    * with currently active LF */
   private static Border getDesktopBorder () {
       Border b = (Border) UIManager.get ("nb.desktop.splitpane.border");
       if (b != null) {
           return b;
       } else {
           return new EmptyBorder(1, 1, 1, 1);
       }
   }

   private static final String ICON_16 = "org/netbeans/core/startup/frame.gif"; // NOI18N
   private static final String ICON_32 = "org/netbeans/core/startup/frame32.gif"; // NOI18N
   private static final String ICON_48 = "org/netbeans/core/startup/frame48.gif"; // NOI18N
   private static final String ICON_256 = "org/netbeans/core/startup/frame256.png"; // NOI18N
   private static final String ICON_512 = "org/netbeans/core/startup/frame512.png"; // NOI18N
   private static final String ICON_1024 = "org/netbeans/core/startup/frame1024.png"; // NOI18N
   static void initFrameIcons(Frame f) {
       List<Image> currentIcons = f.getIconImages();
       if( !currentIcons.isEmpty() || Utilities.isMac())
           return; //do not override icons if they have been already provided elsewhere (JDev / macOS uses Dock icon)
       f.setIconImages(Arrays.asList(
               ImageUtilities.loadImage(ICON_16, true),
               ImageUtilities.loadImage(ICON_32, true),
               ImageUtilities.loadImage(ICON_48, true),
               ImageUtilities.loadImage(ICON_256, true),
               ImageUtilities.loadImage(ICON_512, true),
               ImageUtilities.loadImage(ICON_1024, true)));
   }

   private void initListeners() {
       frame.addWindowListener (new WindowAdapter() {
               @Override
               public void windowClosing(WindowEvent evt) {
                   LifecycleManager.getDefault().exit();
               }
           }
       );
   }

   static void preInitMenuAndToolbar() {
       createMenuBar();
       ToolbarPool.getDefault();
   }

   /** Creates menu bar. */
   private static JMenuBar createMenuBar() {
       JMenuBar menu = getCustomMenuBar();
       if (menu == null) {
            menu = new MenuBar (null);
       }
       menu.setBorderPainted(false);
       if (menu instanceof MenuBar) {
           ((MenuBar)menu).waitFinished();
       }

       if(Constants.SWITCH_STATUSLINE_IN_MENUBAR) {
           if (Constants.CUSTOM_STATUS_LINE_PATH == null) {
               JLabel status = new StatusLine();
               JSeparator sep = new JSeparator(JSeparator.VERTICAL);
               Dimension d = sep.getPreferredSize();
               d.width += 6; // need a bit more padding...
               sep.setPreferredSize(d);
               JPanel statusLinePanel = new JPanel(new BorderLayout());
               statusLinePanel.add(sep, BorderLayout.WEST);
               statusLinePanel.add(status, BorderLayout.CENTER);

               decoratePanel (statusLinePanel, true);
               statusLinePanel.setName("statusLine"); //NOI18N
               menu.add(statusLinePanel);
           } else {
               JComponent status = getCustomStatusLine();
               if (status != null) {
                   menu.add(status);
               }
           }
       }

       return menu;
   }

    /**
     * Tries to find custom menu bar component on system file system.
     * @return menu bar component or <code>null</code> if no menu bar
     *         component is found on system file system.
     */
    private static JMenuBar getCustomMenuBar() {
        try {
            String fileName = Constants.CUSTOM_MENU_BAR_PATH;
            if (fileName == null) {
                return null;
            }
            FileObject fo = FileUtil.getConfigFile(fileName);
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
                if (ic != null) {
                    return (JMenuBar)ic.instanceCreate();
                }
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    /**
     * Tries to find custom status line component on system file system.
     * @return status line component or <code>null</code> if no status line
     *         component is found on system file system.
     */
    private static JComponent getCustomStatusLine() {
        try {
            String fileName = Constants.CUSTOM_STATUS_LINE_PATH;
            if (fileName == null) {
                return null;
            }
            FileObject fo = FileUtil.getConfigFile(fileName);
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                InstanceCookie ic = (InstanceCookie)dobj.getCookie(InstanceCookie.class);
                if (ic != null) {
                    return (JComponent)ic.instanceCreate();
                }
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

   /** Creates toolbar component. */
   private static JComponent getToolbarComponent() {
       ToolbarPool tp = ToolbarPool.getDefault();
       tp.waitFinished();
//        ErrorManager.getDefault().getInstance(MainWindow.class.getName()).log("toolbar config name=" + WindowManagerImpl.getInstance().getToolbarConfigName());
//        tp.setConfiguration(WindowManagerImpl.getInstance().getToolbarConfigName()); // NOI18N

       return tp;
   }

   private Rectangle forcedBounds = null;
   /** Packs main window, to set its border */
   private void initializeBounds() {
       Rectangle bounds;
       if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
           bounds = WindowManagerImpl.getInstance().getMainWindowBoundsJoined();
       } else {
           bounds = WindowManagerImpl.getInstance().getMainWindowBoundsSeparated();
       }
       if( null != forcedBounds ) {
           bounds = new Rectangle( forcedBounds );
           frame.setPreferredSize( bounds.getSize() );
           forcedBounds = null;
       }

       if(!bounds.isEmpty()) {
           frame.setBounds(bounds);
       }
   }

   /** Prepares main window, has to be called after {@link initializeComponents()}. */
   public void prepareWindow() {
       initializeBounds();
   }

   /** Sets desktop component. */
   public void setDesktop(Component comp) {
       if(desktop == comp) {
           // XXX PENDING revise how to better manipulate with components
           // so there don't happen unneeded removals.
           if(desktop != null
           && !Arrays.asList(getDesktopPanel().getComponents()).contains(desktop)) {
               getDesktopPanel().add(desktop, BorderLayout.CENTER);
           }
           return;
       }

       if(desktop != null) {
           getDesktopPanel().remove(desktop);
       }

       desktop = comp;

       if(desktop != null) {
           getDesktopPanel().add(desktop, BorderLayout.CENTER);
       }
       frame.invalidate();
       frame.validate();

       frame.repaint();
   }

   // XXX PENDING used in DnD only.
   public Component getDesktop() {
       return desktop;
   }

   public boolean hasDesktop() {
       return desktop != null;
   }

   /** #112408: Single access point for desktopPanel to ensure it's never null */
   private JPanel getDesktopPanel () {
       if (desktopPanel == null) {
           // initialize desktop panel
           desktopPanel = new JPanel();
           desktopPanel.setBorder(getDesktopBorder());
           desktopPanel.setLayout(new BorderLayout());
           if( isShowCustomBackground() )
               desktopPanel.setOpaque( false );
       }
       return desktopPanel;
   }

   // XXX
   /** Gets bounds of main window without the dektop component. */
   public Rectangle getPureMainWindowBounds() {
       Rectangle bounds = frame.getBounds();

       // XXX Substract the desktop height, we know the pure main window
       // is always at the top, the width is same.
       if(desktop != null) {
           Dimension desktopSize = desktop.getSize();
           bounds.height -= desktopSize.height;
       }

       return bounds;
   }

   // Full Screen Mode
   private boolean isFullScreenMode = false;
   private Rectangle restoreBounds;
   private int restoreExtendedState = JFrame.NORMAL;
   private boolean isSwitchingFullScreenMode = false;
   private boolean isUndecorated = true;
   private int windowDecorationStyle = JRootPane.FRAME;


   public void setFullScreenMode( boolean fullScreenMode ) {
       if( isFullScreenMode == fullScreenMode || isSwitchingFullScreenMode
               || Utilities.isMac()) { //Mac OS X has its own built-in full screen support, see applemenu module
           return;
       }
       isSwitchingFullScreenMode = true;
       if( !isFullScreenMode ) {
           restoreExtendedState = frame.getExtendedState();
           restoreBounds = frame.getBounds();
           isUndecorated = frame.isUndecorated();
           windowDecorationStyle = frame.getRootPane().getWindowDecorationStyle();
       }

       final TopComponent activeTc = TopComponent.getRegistry().getActivated();

       GraphicsDevice device = null;
       GraphicsConfiguration conf = frame.getGraphicsConfiguration();
       if( null != conf ) {
           device = conf.getDevice();
           if( isFullScreenMode && device.isFullScreenSupported() && !(Utilities.isMac() || Utilities.isWindows()) ) {
               //#195927 - attempting to prevent NPE on sunray solaris
               try {
                    device.setFullScreenWindow( null );
               }catch( IllegalArgumentException iaE ) {
                   //#206310 - sometimes this make problems on Linux
                   LOGGER.log( Level.FINE, null, iaE );
               }
           }
       }

       isFullScreenMode = fullScreenMode;
       if( Utilities.isWindows() )
           frame.setVisible( false );
       else
           WindowManagerImpl.getInstance().setVisible(false);

       frame.dispose();

       autoHidingMenuBar.setAutoHideEnabled(isFullScreenMode);

       frame.setUndecorated( isFullScreenMode || isUndecorated );
       // Added to support Custom Look and Feel with Decorations
       frame.getRootPane().setWindowDecorationStyle( isFullScreenMode ? JRootPane.NONE : windowDecorationStyle );

       final String toolbarConfigName = ToolbarPool.getDefault().getConfiguration();
       if( null != toolbarConfigName ) {
           ToolbarConfiguration tc = ToolbarConfiguration.findConfiguration( toolbarConfigName );
           if( null != tc )
               tc.rebuildMenu();
       }
       getToolbarComponent().setVisible( !isFullScreenMode );
       final boolean updateBounds = ( !isFullScreenMode );//&& restoreExtendedState != JFrame.MAXIMIZED_BOTH );

       if( updateBounds || (isFullScreenMode() && !Utilities.isWindows()) ) {
           if( updateBounds ) {
               forcedBounds = restoreBounds;
           } else {
               if( null != conf ) {
                   forcedBounds = conf.getBounds();
               } else {
                   GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                   forcedBounds = ge.getMaximumWindowBounds();
               }
           }
       }

       if( null != device && device.isFullScreenSupported() && !Utilities.isWindows()) {
           device.setFullScreenWindow( isFullScreenMode ? frame : null );
       } else {
           frame.setExtendedState( isFullScreenMode ? JFrame.MAXIMIZED_BOTH : restoreExtendedState );
       }

       if( Utilities.isWindows() ) {
           frame.setVisible( true );
           SwingUtilities.invokeLater( new Runnable() {
               @Override
               public void run() {
                   frame.invalidate();
                   frame.validate();
                   frame.repaint();
                   if( updateBounds ) {
                       frame.setPreferredSize( restoreBounds.getSize() );
                       frame.setBounds( restoreBounds );
                   }
                   ToolbarPool.getDefault().setConfiguration( toolbarConfigName );
                   isSwitchingFullScreenMode = false;
                   SwingUtilities.invokeLater(new Runnable() {
                       @Override
                       public void run() {
                            if( null != activeTc ) {
                                activeTc.requestFocusInWindow();
                            }
                       }
                   });
               }
           });
       } else {
           WindowManagerImpl.getInstance().setVisible(true);
           SwingUtilities.invokeLater( new Runnable() {
               @Override
               public void run() {
                   frame.invalidate();
                   frame.validate();
                   frame.repaint();
                   ToolbarPool.getDefault().setConfiguration( toolbarConfigName );
                   isSwitchingFullScreenMode = false;
                   SwingUtilities.invokeLater(new Runnable() {
                       @Override
                       public void run() {
                            if( null != activeTc ) {
                                activeTc.requestFocusInWindow();
                            }
                       }
                   });
                }
           });
       }
   }

   public boolean isFullScreenMode() {
       return isFullScreenMode;
   }

   public JFrame getFrame() {
       return frame;
   }

   private static class HeavyWeightPopupFactory extends PopupFactory {

       @Override
       public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
           return new HeavyWeightPopup(owner, contents, x, y);
       }
   }

   private static class HeavyWeightPopup extends Popup {
       public HeavyWeightPopup(Component owner, Component contents, int x, int y) {
           super( owner, contents, x, y);
       }
   }

   private static boolean isShowCustomBackground() {
       return UIManager.getBoolean("NbMainWindow.showCustomBackground"); //NOI18N
   }

   private static boolean lafLogged = false;
    private static void logLookAndFeelUsage() {
        if( lafLogged )
            return;
        lafLogged = true;
        LookAndFeel laf = UIManager.getLookAndFeel();
        Logger logger = Logger.getLogger( "org.netbeans.ui.metrics.laf" );   // NOI18N
        LogRecord rec = new LogRecord( Level.INFO, "USG_LOOK_AND_FEEL" ); //NOI18N
        String lafId = laf.getID();
        if( UIManager.getLookAndFeelDefaults().getBoolean( "nb.dark.theme" ) ) //NOI18N
        {
            lafId = "DARK " + lafId; //NOI18N
        }
        rec.setParameters( new Object[]{ lafId, laf.getName() } );
        rec.setLoggerName( logger.getName() );
        logger.log( rec );
    }
}

