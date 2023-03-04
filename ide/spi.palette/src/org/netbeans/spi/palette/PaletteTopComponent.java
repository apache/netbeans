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


package org.netbeans.spi.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.palette.Utils;
import org.netbeans.modules.palette.ui.PalettePanel;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;




/**
 * <p>Top component which displays component palette.</p>
 *
 *
 * @author S. Aubrecht
 */

final class PaletteTopComponent extends TopComponent implements PropertyChangeListener {

    static final long serialVersionUID = 4248268998485315735L;

    private static PaletteTopComponent instance;

    /** Creates new PaletteTopComponent */
    private PaletteTopComponent() {
        setName(Utils.getBundleString("CTL_Component_palette"));  // NOI18N
        setToolTipText(Utils.getBundleString("HINT_PaletteComponent"));
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/palette/resources/palette.png")); // NOI18N
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(505, 88));
        add( PalettePanel.getDefault().getScrollPane(), BorderLayout.CENTER );
        
        putClientProperty( "keepPreferredSizeWhenSlideIn", Boolean.TRUE ); // NOI18N
    }
    
    @Override
    public void requestActive() {
        super.requestActive();
        PalettePanel.getDefault().requestFocusInWindow();
    }

    /** Gets default instance. Don't use directly, it reserved for '.settings' file only,
     * i.e. deserialization routines, otherwise you can get non-deserialized instance. */
    public static synchronized PaletteTopComponent getDefault() {
        if(instance == null) {
            instance = new PaletteTopComponent();
        }
        return instance;
    }
    
    /** Overriden to explicitely set persistence type of PaletteTopComponent
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    @Override
    public void componentOpened() {
        PaletteSwitch switcher = PaletteSwitch.getDefault();
        
        switcher.addPropertyChangeListener( this );
        PaletteController pc = switcher.getCurrentPalette();
        setPaletteController( pc );
        if( Utils.isOpenedByUser(this) ) {
            //only change the flag when the Palette window was opened from ShowPaletteAction
            //i.e. user clicked the menu item or used keyboard shortcut - ignore window system load & restore
            PaletteVisibility.setVisible( pc, true );
        }
    }
    
    @Override
    public void componentClosed() {
        // palette is closed so reset its contents
        PaletteSwitch switcher = PaletteSwitch.getDefault();
        
        switcher.removePropertyChangeListener( this );
        PaletteController pc = switcher.getCurrentPalette();
        PaletteVisibility.setVisible( pc, false );
//        if( null != pc )
//            PaletteVisibility.setVisible( null, false );
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    @Override
    protected String preferredID() {
        return "CommonPalette"; //NOI18N
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if( PaletteSwitch.PROP_PALETTE_CONTENTS.equals( e.getPropertyName() ) ) {
            PaletteController pc = (PaletteController)e.getNewValue();
            
            setPaletteController( pc );
        }
    }
    
    private void setPaletteController( PaletteController pc ) {
        if( null != pc ) {
            PalettePanel.getDefault().setContent( pc, pc.getModel(), pc.getSettings() );
        } else {
            PalettePanel.getDefault().setContent( null, null, null );
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return PalettePanel.getDefault().getHelpCtx();
    }

    static void showPalette() {
        WindowManager wm = WindowManager.getDefault();
        TopComponent palette = wm.findTopComponent("CommonPalette"); // NOI18N
        if (null == palette) {
            Logger.getLogger(PaletteSwitch.class.getName()).log(Level.INFO, "Cannot find CommonPalette component."); // NOI18N

            //for unit-testing
            palette = getDefault();
        }
        if (!palette.isOpened()) {
            palette.open();
        }
    }
    
    static void hidePalette() {
        TopComponent palette = instance;
        if (palette != null && palette.isOpened()) {
            palette.close();
        }
    }
    
    static final class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457788L;
        public Object readResolve() {
            return PaletteTopComponent.getDefault();
        }
    }
}
