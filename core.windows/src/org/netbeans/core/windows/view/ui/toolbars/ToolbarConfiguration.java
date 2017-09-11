/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.spi.settings.Saver;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;

/** 
 * Toolbar configuration, it contains toolbar panel with a list of toolbar rows.
 *
 * @author S. Aubrecht
 */
public final class ToolbarConfiguration implements ToolbarPool.Configuration {

    private final JPanel toolbarPanel;

    private static Map<String,ToolbarConfiguration> name2config = new HashMap<String,ToolbarConfiguration>(10);
    
    /** Toolbar menu is global so it is static. It it the same for all toolbar
     configurations. */
    private static JMenu toolbarMenu;
    
    /** Name of configuration. */
    private final String configName;
    /** Display name of configuration. */
    private final String configDisplayName;

    private final List<ToolbarRow> rows;

    private DnDSupport dndSupport;

    private Saver saver;


    /** Creates new empty toolbar configuration for specific name.
     * @param name new configuration name
     */
    ToolbarConfiguration( String name, String displayName, List<ToolbarRow> rows ) {
        configName = name;
        // fix #44537 - just doing the simple thing of hacking the extension out of the display name.. node.getDisplayName is too unpredictable.
        if (displayName.endsWith(".xml")) {
            displayName = displayName.substring(0, displayName.length() - ".xml".length());
        }
        configDisplayName = displayName;
        // asociate name and configuration instance
        name2config.put(name, this);
        toolbarPanel = new JPanel( new GridLayout(0,1) ) {
            @Override
            public boolean isOpaque() {
                if( null != UIManager.get("NbMainWindow.showCustomBackground") ) //NOI18N
                    return !UIManager.getBoolean("NbMainWindow.showCustomBackground"); //NOI18N
                return super.isOpaque();
            }
        };

        this.rows = new ArrayList<ToolbarRow>(rows);
    }

    private synchronized DnDSupport dndSupport() {
        if (dndSupport == null) {
            dndSupport = new DnDSupport(this);
        }
        return dndSupport;
    }

    /** Finds toolbar configuration which has given name.
     * @return toolbar configuration instance which ID is given name or null
     * if no such configuration can be found */
    public static final ToolbarConfiguration findConfiguration (String name) {
        return name2config.get(name);
    }
    
    private static final ToolbarPool getToolbarPool() {
        return ToolbarPool.getDefault ();
    }
    
    public static void rebuildMenu() {
        synchronized( ToolbarConfiguration.class ) {
            if (toolbarMenu != null) {
                toolbarMenu.removeAll();
                fillToolbarsMenu(toolbarMenu, false);
            }
        }
    }

    @NbBundle.Messages({
        "MSG_ToolbarsInitializing=Initializing..."
    })
    private static void fillToolbarsMenu (JComponent menu, boolean isContextMenu) {
        final ToolbarPool pool = getToolbarPool();
        if (!pool.isFinished()) {
            final JMenuItem mi = new JMenuItem();
            mi.setText(Bundle.MSG_ToolbarsInitializing());
            mi.setEnabled(false);
            menu.add(mi);
            return;
        }
        boolean fullScreen = MainWindow.getInstance().isFullScreenMode();

        ToolbarConfiguration conf = findConfiguration(ToolbarPool.getDefault().getConfiguration());
        if (conf == null) {
            return;
        }
        Map<String, ToolbarConstraints> name2constr = conf.collectAllConstraints();
        // generate list of available toolbars
        for( Toolbar tb : pool.getToolbars() ) {
            final Toolbar bar = tb;
            final String tbName = tb.getName();
            ToolbarConstraints tc = name2constr.get(tbName);


            if (tc != null && tb != null) {
                //May be null if a toolbar has been renamed
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem (
                    tb.getDisplayName(), tc.isVisible()
                );
                mi.putClientProperty("ToolbarName", tbName); //NOI18N
                mi.addActionListener (new ActionListener () {
                    public void actionPerformed (ActionEvent ae) {
                        // #39741 fix
                        // for some reason (unknown to me - mkleint) the menu gets recreated repeatedly, which
                        // can cause the formerly final ToolbarConstraints instance to be obsolete.
                        // that's why we each time look up the current instance on the allToolbars map.
                        ToolbarConfiguration conf = findConfiguration(ToolbarPool.getDefault().getConfiguration());
                        if (conf != null) {
                            ToolbarConstraints tc = conf.getConstraints(tbName);
                            conf.setToolbarVisible(bar, !tc.isVisible());
                        }
                    }
                });
                mi.setEnabled( !fullScreen );
                menu.add (mi);
            }
        }
        menu.add (new JPopupMenu.Separator());

        //Bigger toolbar icons
        boolean smallToolbarIcons = (getToolbarPool().getPreferredIconSize() == 16);
        final String stiName = NbBundle.getMessage(ToolbarConfiguration.class, "CTL_SmallIcons");

        if (!stiName.isEmpty()) {
            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem( stiName, smallToolbarIcons );
            cbmi.addActionListener (new ActionListener () {
                @Override
                public void actionPerformed (ActionEvent ev) {
                    if (ev.getSource() instanceof JCheckBoxMenuItem) {
                        JCheckBoxMenuItem cb = (JCheckBoxMenuItem) ev.getSource();
                        // toggle big/small icons
                        boolean state = cb.getState();
                        if (state) {
                            ToolbarPool.getDefault().setPreferredIconSize(16);
                        } else {
                            ToolbarPool.getDefault().setPreferredIconSize(24);
                        }
                        //Rebuild toolbar panel
                        //#43652: Find current toolbar configuration
                        String name = ToolbarPool.getDefault().getConfiguration();
                        ToolbarConfiguration tbConf = findConfiguration(name);
                        if (tbConf != null) {
                            tbConf.refresh();
                        }
                    }
                }
            });
            cbmi.setEnabled( !fullScreen );
            menu.add (cbmi);
            menu.add( new JPopupMenu.Separator() );
        }


        JMenuItem menuItem = new JMenuItem( new ResetToolbarsAction() );
        menuItem.setEnabled( !fullScreen );
        menu.add( menuItem );

        menuItem = new JMenuItem(NbBundle.getMessage(ToolbarConfiguration.class, "CTL_CustomizeToolbars")); //NOI18N
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                ConfigureToolbarPanel.showConfigureDialog();
            }
        });
        menuItem.setEnabled( !fullScreen );
        menu.add( menuItem );

        for( Component c : menu instanceof JPopupMenu
                ? menu.getComponents()
                : ((JMenu)menu).getPopupMenu().getComponents()) {
            if( c instanceof AbstractButton ) {
                AbstractButton b = (AbstractButton)c;

                if( isContextMenu ) {
                    b.setText( Actions.cutAmpersand(b.getText()) );
                } else {
                    Mnemonics.setLocalizedText( b, b.getText() );
                }
            }
        }
    } // getContextMenu
    

    
    /** Rebuild toolbar panel when size of icons is changed.
     * All components are removed and again added using ToolbarPool's list of correct toolbars.
     */
    void refresh() {
        toolbarPanel.removeAll();
        Toolbar tbs[] = getToolbarPool().getToolbars();
        Map<String, Toolbar> bars = new HashMap<String, Toolbar>(tbs.length);
        boolean smallToolbarIcons = getToolbarPool().getPreferredIconSize() == 16;
        for (int i = 0; i < tbs.length; i++) {
            Toolbar tb = tbs[i];
            String name = tb.getName();
            //make sure that toolbar constraints get created if this is a new toolbar
            ToolbarConstraints tc = getConstraints(name);
            Component [] comps = tb.getComponents();
            for (int j = 0; j < comps.length; j++) {
                if (comps[j] instanceof JComponent) {
                    if (smallToolbarIcons) {
                        ((JComponent) comps[j]).putClientProperty("PreferredIconSize",null); //NOI18N
                        tb.putClientProperty("PreferredIconSize",null); //NOI18N
                    } else {
                        ((JComponent) comps[j]).putClientProperty("PreferredIconSize",Integer.valueOf(24)); //NOI18N
                        tb.putClientProperty("PreferredIconSize",Integer.valueOf(24)); //NOI18N
                    }
                }
                //TODO add icon shadow for mac l&f?
            }
            bars.put(name, tb);
        }

        removeEmptyRows();

        for( ToolbarRow row : rows ) {
            row.removeAll();
            if( !row.isVisible() )
                continue;
            for( ToolbarConstraints tc : row.getConstraints() ) {
                if( !tc.isVisible() )
                    continue;
                Toolbar tb = bars.get(tc.getName());
                if( null != tb ) {
                    ToolbarContainer container = new ToolbarContainer( tb, dndSupport(), tc.isDraggable() );
                    row.add( tc.getName(), container );
                }
            }

            toolbarPanel.add(row);
        }

        adjustToolbarPanelBorder();

        rebuildMenu();
        
        repaint();
    }
    
    /**
     * Add a new row if the screen location points 'just below' the toolbar panel.
     * @param screenLocation
     * @return New toolbar row or null if the screen location is too far from toolbar panel.
     */
    ToolbarRow maybeAddEmptyRow(Point screenLocation) {
        if( rows.isEmpty() )
            return null;
        if( rows.size() > 0 && rows.get(rows.size()-1).isEmpty() )
            return null;
        if( !toolbarPanel.isShowing() )
            return null;
        int rowHeight = rows.get(0).getHeight();
        int bottom = toolbarPanel.getLocationOnScreen().y + toolbarPanel.getHeight();
        if( screenLocation.y >= bottom && screenLocation.y <= bottom + rowHeight ) {
            ToolbarRow row = new ToolbarRow();
            rows.add(row);
            toolbarPanel.add(row);
            repaint();
            return row;
        }
        return null;
    }

    /**
     * @param row
     * @return True if the given row is the last one and there is more than one row
     * in toolbar panel.
     */
    boolean isLastRow( ToolbarRow row ) {
        return rows.size() > 1 && rows.get(rows.size()-1) == row;
    }

    /**
     * Remove the last row if it is empty.
     */
    void maybeRemoveLastRow() {
        if( rows.size() > 1 ) {
            ToolbarRow lastRow = rows.get(rows.size()-1);
            if( lastRow.isEmpty() ) {
                rows.remove(lastRow);
                toolbarPanel.remove(lastRow);
                repaint();
            }
        }
    }

    /**
     * Remove all rows that contain no toolbars.
     */
    void removeEmptyRows() {
        ArrayList<ToolbarRow> toRemove = new ArrayList<ToolbarRow>(rows.size());
        for( ToolbarRow r : rows ) {
            if( r.isEmpty() ) {
                toRemove.add( r );
                toolbarPanel.remove(r);
            }
        }
        rows.removeAll(toRemove);
        repaint();
    }

    void repaint() {
        toolbarPanel.invalidate();
        toolbarPanel.revalidate();
        toolbarPanel.repaint();
    }

    /** Activates the configuration and returns right
     * component that can display the configuration.
     * @return representation component
     */
    public Component activate () {
        refresh();
        //#233904
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                rebuildMenu();
            }
        });
        return toolbarPanel;
    }

    /** Name of the configuration.
     * @return the name
     */
    public String getName () {
        return configName;
    }
    
    public String getDisplayName () {
        return configDisplayName;
    }

    /** Popup menu that should be displayed when the users presses
     * right mouse button on the panel. This menu can contain
     * contains list of possible configurations, additional actions, etc.
     *
     * @return popup menu to be displayed
     */
    public JPopupMenu getContextMenu () {
        JPopupMenu menu = new JPopupMenu();
        fillToolbarsMenu(menu, true);
        return menu;
    }

    /** Fills given menu with toolbars and configurations items and returns
     * filled menu. */ 
    public static JMenu getToolbarsMenu (JMenu menu) {
        fillToolbarsMenu(menu, false);
        toolbarMenu = menu;
        return menu;
    }
    
    /** Make toolbar visible/invisible in this configuration
     * @param tb toolbar
     * @param b true to make toolbar visible
     */
    public void setToolbarVisible(Toolbar tb, boolean visible) {
        ToolbarConstraints tc = getConstraints( tb.getName() );
        boolean isBarVisible = tc.isVisible();
        tc.setVisible(visible);
        if( visible != isBarVisible ) {
            refresh();
            save();
        }
    }
    
    /** Returns true if the toolbar is visible in this configuration
     * @param tb toolbar
     * @return true if the toolbar is visible
     */
    public boolean isToolbarVisible(Toolbar tb) {
        ToolbarConstraints tc = getConstraints(tb.getName());
        return tc.isVisible();
    }

    /**
     * Enable or disable drag and drop of toolbar buttons.
     * @param buttonDndAllowed
     */
    void setToolbarButtonDragAndDropAllowed(boolean buttonDndAllowed) {
        dndSupport().setButtonDragAndDropAllowed(buttonDndAllowed);
    }

    /**
     * @return True when toolbar configuration window is showing and toolbar
     * buttons can be rearranged usin drag and drop.
     * @since 2.66
     */
    public boolean isToolbarConfigurationInProgress() {
        return dndSupport.isButtonDragAndDropAllowed();
    }

    /**
     * @param toolbarName
     * @return Constraints for the given toolbar. If the constraints do not exist
     * yet, new ones are created and added to a suitable toolbar row.
     */
    private ToolbarConstraints getConstraints( String toolbarName ) {
        ToolbarConstraints tc = collectAllConstraints().get(toolbarName);
        if( null == tc ) {
            boolean isQuickSearch = "QuickSearch".equals(toolbarName); //NOI18N
            tc = new ToolbarConstraints(toolbarName,
                    isQuickSearch ? ToolbarConstraints.Align.right : ToolbarConstraints.Align.left,
                    true, true);
            ToolbarRow row = null; //TODO find / add row with the best available space
            if( rows.isEmpty() ) {
                row = new ToolbarRow();
                rows.add(row);
            } else {
                if( isQuickSearch )
                    row = rows.get(0);
                else
                    row = rows.get(rows.size()-1);
            }
            row.addConstraint(tc);
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    refresh();
                }
            });
        }
        return tc;
    }

    /**
     * @param screenLocation
     * @return Toolbar row at given screen location or null.
     */
    ToolbarRow getToolbarRowAt( Point screenLocation ) {
        Rectangle bounds = new Rectangle();
        for( ToolbarRow row : rows ) {
            bounds = row.getBounds(bounds);
            if( row.isShowing() ) {
                bounds.setLocation(row.getLocationOnScreen());
                if( bounds.contains(screenLocation) )
                    return row;
            }
        }
        return null;
    }
    
    /**
     * Take a snapshot of current toolbar configuration and ask for saving it to a file.
     * (The actual saving will happen at some later undefined time).
     */
    void save() {
        if( null == saver )
            return;
        try {
            createSnapshot();
            saver.requestSave();
        } catch( IOException ioE ) {
            Logger.getLogger(ToolbarConfiguration.class.getName()).log(Level.INFO, 
                    "Error while saving toolbar configuration", ioE); //NOI18N
        }
    }

    void setSaverCallback( Saver s ) {
        this.saver = s;
    }

    /**
     * A snapshot of toolbar configuration as the save event might come when toolbar
     * dnd is in progress which would lead to wrong data being stored.
     */
    private List<List<ToolbarConstraints>> snapshot;
    private void createSnapshot() {
        snapshot = new ArrayList<List<ToolbarConstraints>>(rows.size());
        for( ToolbarRow r : rows ) {
            ArrayList<ToolbarConstraints> constraints = new ArrayList<ToolbarConstraints>(20);
            for( ToolbarConstraints tc : r.getConstraints() ) {
                constraints.add(tc);
            }
            snapshot.add(constraints);
        }
    }

    List<? extends List<? extends ToolbarConstraints>> getSnapshot() {
        return snapshot;
    }

    /**
     * @return Toolbar name -> Toolbar constraints.
     */
    private Map<String, ToolbarConstraints> collectAllConstraints() {
        Map<String, ToolbarConstraints> res = new HashMap<String, ToolbarConstraints>(20);
        for( ToolbarRow row : rows ) {
            for( ToolbarConstraints tc : row.getConstraints() ) {
                res.put(tc.getName(), tc);
            }
        }
        return res;
    }

    /** Recognizes if XP theme is set.
     *  (copy & paste from org.openide.awt.Toolbar to avoid API changes)
     * @return true if XP theme is set, false otherwise
     */
    private static Boolean isXP = null;
    private static boolean isXPTheme () {
        if (isXP == null) {
            Boolean xp = (Boolean)Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
            isXP = Boolean.TRUE.equals(xp)? Boolean.TRUE : Boolean.FALSE;
        }
        return isXP.booleanValue();
    }

    //-------------------------------------------------------------------------
    // border for the whole toolbar panel
    //-------------------------------------------------------------------------
    private static Color fetchColor (String key, Color fallback) {
        //Fix ExceptionInInitializerError from MainWindow on GTK L&F - use
        //fallback colors
        Color result = (Color) UIManager.get(key);
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    private static Color mid;
    static {
        Color lo = fetchColor("controlShadow", Color.DARK_GRAY); //NOI18N
        Color hi = fetchColor("control", Color.GRAY); //NOI18N

        int r = (lo.getRed() + hi.getRed()) / 2;
        int g = (lo.getGreen() + hi.getGreen()) / 2;
        int b = (lo.getBlue() + hi.getBlue()) / 2;
        mid = new Color(r, g, b);
    }

    private static boolean isWindows8() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 8") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.2".equals( System.getProperty("os.version") ));
    }

    private static final Border lowerBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0,
        fetchColor("controlShadow", Color.DARK_GRAY)),
        BorderFactory.createMatteBorder(0, 0, 1, 0, mid)); //NOI18N

    private static final Border upperBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0,
        fetchColor("controlShadow", Color.DARK_GRAY)),
        BorderFactory.createMatteBorder(1, 0, 0, 0,
        fetchColor("controlLtHighlight", Color.WHITE))); //NOI18N

    private void adjustToolbarPanelBorder() {
        if( toolbarPanel.getComponentCount() > 0 ) {
            Border b = UIManager.getBorder( "Nb.MainWindow.Toolbar.Border");
            if( null != b ) {
                toolbarPanel.setBorder( b );
                return;
            }
            //add border
            if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                if( isXPTheme() ) {
                    if( isWindows8() ) {
                        toolbarPanel.setBorder( BorderFactory.createEmptyBorder() );
                    } else {
                        //Set up custom borders for XP
                        toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
                            upperBorder,
                            BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0,
                                fetchColor("controlShadow", Color.DARK_GRAY)),
                                BorderFactory.createMatteBorder(0, 0, 1, 0, mid))
                        )); //NOI18N
                    }
                } else {
                    toolbarPanel.setBorder( BorderFactory.createEtchedBorder() );
                }
            } else if ("GTK".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                //No border
                toolbarPanel.setBorder(BorderFactory.createEmptyBorder());
            }
        } else {
            if ("GTK".equals(UIManager.getLookAndFeel().getID())) {
                toolbarPanel.setBorder(BorderFactory.createEmptyBorder());
            } else {
                toolbarPanel.setBorder(lowerBorder);
            }
        }
    }

} // end of class Configuration
