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
/*
 * PageFlowController.java
 *
 * Created on March 1, 2007, 1:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public class PageFlowToolbarUtilities {

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    //    private static final int SCOPE_FACESCONFIG = 1;
    //    private static final int SCOPE_PROJECT = 2;

    public static enum Scope {

        SCOPE_FACESCONFIG, SCOPE_PROJECT, SCOPE_ALL_FACESCONFIG
    }
    private static Map<Scope, String> scope2String = new EnumMap<>(Scope.class);
    private static Map<String, Scope> string2Scope = new HashMap<String, Scope>();
    private static final String TT_SCOPE = NbBundle.getMessage(PageFlowToolbarUtilities.class, "TT_Scope_DropDown");
    private static final String LBL_SCOPE_FACESCONFIG = NbBundle.getMessage(PageFlowToolbarUtilities.class, "LBL_Scope_FacesConfig");
    private static final String LBL_SCOPE_PROJECT = NbBundle.getMessage(PageFlowToolbarUtilities.class, "LBL_Scope_Project");
    private static final String LBL_SCOPE_ALL_PROJECT = NbBundle.getMessage(PageFlowToolbarUtilities.class, "LBL_Scope_All_FacesConfig");
    private static final String TT_LAYOUTPAGES = NbBundle.getMessage(PageFlowToolbarUtilities.class, "TT_LayoutPages");
    static {
        /* Loading these for quick access */
        scope2String.put(Scope.SCOPE_FACESCONFIG, LBL_SCOPE_FACESCONFIG);
        scope2String.put(Scope.SCOPE_PROJECT, LBL_SCOPE_PROJECT);
        scope2String.put(Scope.SCOPE_ALL_FACESCONFIG, LBL_SCOPE_ALL_PROJECT);
        string2Scope.put(LBL_SCOPE_FACESCONFIG, Scope.SCOPE_FACESCONFIG);
        string2Scope.put(LBL_SCOPE_PROJECT, Scope.SCOPE_PROJECT);
        string2Scope.put(LBL_SCOPE_ALL_PROJECT, Scope.SCOPE_ALL_FACESCONFIG);
    }
    private Scope currentScope;
    //    private PageFlowController pfc;

    public static final String getScopeLabel(Scope scope) {
        return scope2String.get(scope);
    }

    public static final Scope getScope(String scopeStr) {
        return string2Scope.get(scopeStr);
    }

    /** Creates a new instance of PageFlowController
     * @param scene
     */
    private PageFlowToolbarUtilities(PageFlowView view) {
        currentScope = Scope.SCOPE_PROJECT;
        setPageFlowView(view);
    }
    private static PageFlowToolbarUtilities instance;
    private static final Map<PageFlowView, PageFlowToolbarUtilities> map = new WeakHashMap<PageFlowView, PageFlowToolbarUtilities>();
    
    /**
     * Remove PageFlowView from the map in PageFlowToolbarUtilities
     */  
    protected static boolean removePageFlowView(PageFlowView view) {
        if( map.containsKey(view)){
            map.remove(view);
            return true;
        }
        return false;
    }

    /**
     *Accessor to get the instance. Singleton pattern
     * @return pageFlowUtilities instance
     */
    public static PageFlowToolbarUtilities getInstance(PageFlowView view) {
        PageFlowToolbarUtilities myInstance = map.get(view);
        if (myInstance == null) {
            myInstance = new PageFlowToolbarUtilities(view);
            map.put(view, myInstance);
        }
        return myInstance;
    }
    
    public static Set<PageFlowView> getViews() {
        return map.keySet();
    }

    /**
     * Get the current page flow editor scope
     * @return currentScope (LBL_SCOPE_PROJECT,LBL_SCOPE_FACESCONFIG)
     */
    public Scope getCurrentScope() {
        return currentScope;
    }

    public void setCurrentScope(Scope scope) {
        this.currentScope = scope;
        if (scopeBox != null && !scopeBox.getSelectedItem().equals(getScopeLabel(currentScope))) {
            scopeBox.setSelectedItem(getScopeLabel(currentScope));
        }
    }
    private JComboBox scopeBox;

    /**
     * Creates a JComboBox for the user to select the scope type.
     * @param view
     * @param pfc
     * @return
     */
    public JComboBox createScopeComboBox() {
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(getScopeLabel(Scope.SCOPE_FACESCONFIG));
        comboBox.addItem(getScopeLabel(Scope.SCOPE_PROJECT));
        comboBox.addItem(getScopeLabel(Scope.SCOPE_ALL_FACESCONFIG));

        //Set the appropriate size of the combo box so it doesn't take up the whole page.
        Dimension prefSize = comboBox.getPreferredSize();
        comboBox.setMinimumSize(prefSize);
        comboBox.setMaximumSize(prefSize);

        comboBox.setSelectedItem(getScopeLabel(currentScope));

        comboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent event) {
                PageFlowView view = getPageFlowView();
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    String newScope = (String) event.getItem();
                    /* Save Locations before switching scope */
                    view.saveLocations();

                    LogRecord record = new LogRecord(Level.FINE, "PageFLowEditor Scope Changed To:" + newScope);
                    record.setSourceClassName("PageFlowUtilities.ItemListener");
                    record.setSourceMethodName("itemStateChanged");
                    record.setParameters(new Object[]{newScope, new Date()});
                    LOGGER.log(record);

                    setCurrentScope(getScope(newScope));
                    //As we are setting the current scope, we should update the controller and update the scene.  But what happens with setup?
                    /* We don't want the background process to continue adding pins to the pages */
//                    view.clearBackgroundPinAddingProcess();

                    /* You don't want to override the data you just stored */
                    view.getPageFlowController().setupGraphNoSaveData();
                }
                view.requestMultiViewActive();
            }
        });
        comboBox.setToolTipText(TT_SCOPE);
        scopeBox = comboBox;
        return comboBox;
    }
    private static final Icon LAYOUT_ICON = ImageUtilities.loadIcon("org/netbeans/modules/web/jsf/navigation/resources/navigation.gif"); // NOI18N
    private JButton layoutButton = null;

    /**
     * Creates a JComboBox for the user to select the scope type.
     * @param view
     * @param pfc
     * @return
     */
    public JButton createLayoutButton() {

        if (layoutButton != null) {
            return layoutButton;
        }

        layoutButton = new JButton(LAYOUT_ICON);
        //Set the appropriate size of the combo box so it doesn't take up the whole page.
        //        Dimension prefSize = layoutButton.getPreferredSize();
        //        layoutButton.setMinimumSize(prefSize);
        //        layoutButton.setMaximumSize(prefSize);
        layoutButton.setToolTipText(TT_LAYOUTPAGES);
        layoutButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                getPageFlowView().layoutNodes();
                getPageFlowView().requestMultiViewActive();
            }
        });
        return layoutButton;
    }
    private Reference<PageFlowView> pageFlowViewRef;

    /* Get's the PageFlowView of the current utilities */
    public final PageFlowView getPageFlowView() {
        return pageFlowViewRef.get();
    }

    /* Sets the PageFlowView in a WeakReference */
    public void setPageFlowView(PageFlowView view) {
        pageFlowViewRef = new WeakReference<PageFlowView>(view);
    }
}
