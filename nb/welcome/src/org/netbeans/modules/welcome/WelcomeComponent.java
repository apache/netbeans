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

package org.netbeans.modules.welcome;

import java.lang.ref.WeakReference;
import org.openide.util.NbBundle;
import org.openide.windows.*;
import java.awt.*;
import javax.swing.*;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.ui.StartPageContent;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;

/**
 * The welcome screen.
 * @author  Richard Gregor, S. Aubrecht
 */
public class WelcomeComponent extends TopComponent {
    static final long serialVersionUID=6021472310161712674L;
    private static WeakReference<WelcomeComponent> component =
                new WeakReference<WelcomeComponent>(null); 
    private JComponent content;

    private boolean initialized = false;
    
    private WelcomeComponent(){
        setLayout(new BorderLayout());
        setName(NbBundle.getMessage(WelcomeComponent.class, "LBL_Tab_Title"));   //NOI18N
        content = null;
        initialized = false;
        putClientProperty( "activateAtStartup", Boolean.TRUE ); //NOI18N
        putClientProperty( "KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE ); //NOI18N
    }
    
    @Override protected String preferredID(){
        return "WelcomeComponent";    //NOI18N
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */    
    
    private void doInitialize() {
        initAccessibility();
        
        if( null == content ) {
            WelcomeOptions.getDefault().incrementStartCounter();

            JScrollPane scroll = new JScrollPane(new StartPageContent());
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setOpaque(false);
            scroll.setOpaque(false);
            scroll.getViewport().setPreferredSize(new Dimension(Constants.START_PAGE_MIN_WIDTH,100));
            JScrollBar vertical = scroll.getVerticalScrollBar();
            if( null != vertical ) {
                vertical.setBlockIncrement(30*Constants.FONT_SIZE);
                vertical.setUnitIncrement(Constants.FONT_SIZE);
            }
            content = scroll;
            add( content, BorderLayout.CENTER );
            setFocusable( false );
        }
    }
        
    /* Singleton accessor. As WelcomeComponent is persistent singleton this
     * accessor makes sure that WelcomeComponent is deserialized by window system.
     * Uses known unique TopComponent ID "Welcome" to get WelcomeComponent instance
     * from window system. "Welcome" is name of settings file defined in module layer.
     */
    public static WelcomeComponent findComp() {
        WelcomeComponent wc = component.get();
        if (wc == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("Welcome"); // NOI18N
            if (tc != null) {
                if (tc instanceof WelcomeComponent) {
                    wc = (WelcomeComponent)tc;
                    component = new WeakReference<WelcomeComponent>(wc); 
                } else {
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + WelcomeComponent.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    wc = WelcomeComponent.createComp();
                }
            } else {
                //WelcomeComponent cannot be deserialized
                //Fallback to accessor reserved for window system.
                wc = WelcomeComponent.createComp();
            }
        }       
        return wc;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * WelcomeComponent instance from settings file when method is given. Use <code>findComp</code>
     * to get correctly deserialized instance of WelcomeComponent. */
    public static WelcomeComponent createComp() {
        WelcomeComponent wc = component.get();
        if(wc == null) {
            wc = new WelcomeComponent();
            component = new WeakReference<WelcomeComponent>(wc);
        }
        return wc;
    }
    
    /** Overriden to explicitely set persistence type of WelcomeComponent
     * to PERSISTENCE_ALWAYS */
    @Override public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WelcomeComponent.class, "ACS_Welcome_DESC")); // NOI18N
    }

    /**
     * Called when <code>TopComponent</code> is about to be shown.
     * Shown here means the component is selected or resides in it own cell
     * in container in its <code>Mode</code>. The container is visible and not minimized.
     * <p><em>Note:</em> component
     * is considered to be shown, even its container window
     * is overlapped by another window.</p>
     * @since 2.18
     *
     * #38900 - lazy addition of GUI components
     *
     */
    @Override protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        if( null != content && getComponentCount() == 0 ) {
            //notify components down the hierarchy tree that they should 
            //refresh their content (e.g. RSS feeds)
            add( content, BorderLayout.CENTER );
        }
        super.componentShowing();
        setActivatedNodes( new Node[] {} );
    }

    private static boolean firstTimeOpen = true;
    @Override 
    protected void componentOpened() {
        super.componentOpened();
        if( firstTimeOpen ) {
            firstTimeOpen = false;
            if( !WelcomeOptions.getDefault().isShowOnStartup() ) {
                close();
            }
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("InitialLayout"); //NOI18N
        if( null != group ) {
            group.open();
            boolean firstTimeClose = NbPreferences.forModule(WelcomeComponent.class).getBoolean("firstTimeClose", true); //NOI18N
            NbPreferences.forModule(WelcomeComponent.class).putBoolean("firstTimeClose", false); //NOI18N
            if( firstTimeClose ) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("projectTabLogical_tc"); //NOI18N
                if( null != tc && tc.isOpened() )
                    tc.requestActive();
            }
        }
    }
    
    @Override protected void componentHidden() {
        super.componentHidden();
        if( null != content ) {
            //notify components down the hierarchy tree that they no long 
            //need to periodically refresh their content (e.g. RSS feeds)
            remove( content );
        }
    }

    @Override
    public void requestFocus() {
        if( null != content )
            content.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        if( null != content )
            return content.requestFocusInWindow();
        return super.requestFocusInWindow();
    }
}

