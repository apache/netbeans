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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    final static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457788L;
        public Object readResolve() {
            return PaletteTopComponent.getDefault();
        }
    }
}
