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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.palette.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * A class that listens to changes to the set of opened TopComponents and to the
 * set of activated Nodes to show/hide the palette window when a TopComponent that
 * supports the palette is activated/deactivated.
 *
 * @author S. Aubrecht
 */
final class PaletteVisibility {

    private static final RequestProcessor RP = new RequestProcessor( "PaletteVisibility", 1 ); //NOI18N
    
    public static boolean isVisible( PaletteController pc ) {
        if( null == pc ) {
            return isVisible( null, false);
        }
        return isVisible( pc, true );
    }
    
    private static boolean isVisible( PaletteController pc, boolean defValue ) {
        String paletteId = getPaletteId( pc );

        FileObject fo = findPaletteTopComponentSettings();
        boolean res = defValue;
        Object val = null == fo ? null : fo.getAttribute( "_palette_visible_"+paletteId );
        if( val instanceof Boolean ) {
            res = ((Boolean)val).booleanValue();
        } else if( null != pc ) {
            Node rootNode = pc.getRoot().lookup(Node.class);
            if( null != rootNode ) {
                res = Utils.getBoolean(rootNode, PaletteController.ATTR_PALETTE_DEFAULT_VISIBILITY, defValue);
            }
        }
        return res;
    }
    
    public static void setVisible( PaletteController pc, boolean isVisible ) {
        String paletteId = getPaletteId( pc );

        //don't block AWT
        _setVisible( paletteId, isVisible );
    }

    private static void _setVisible( final String paletteId, final boolean isVisible ) {
        RP.post( new Runnable() {

            @Override
            public void run() {
                FileObject fo = findPaletteTopComponentSettings();
                try {
                    if( null != fo )
                        fo.setAttribute("_palette_visible_" + paletteId, new Boolean(isVisible));
                } catch (IOException ex) {
                    Logger.getLogger(PaletteVisibility.class.getName()).log( Level.INFO, null, ex );
                }
            }
        });
    }

    private static FileObject findPaletteTopComponentSettings() {
        String role = WindowManager.getDefault().getRole();
        String root = "Windows2Local"; //NOI18N
        if( null != role )
            root += "/Roles/" + role;
        FileObject res = FileUtil.getConfigFile(root+"/Modes/commonpalette");
        if( null == res ) {
            try {
                //for unit-testing
                res = FileUtil.getConfigFile(root+"/Modes");
                if( null == res ) {
                    res = FileUtil.getConfigFile(root);
                    if( null == res )
                        res = FileUtil.getConfigRoot().createFolder(root);
                    res = res.createFolder("Modes");
                }
                
                res = res.createFolder("commonpalette");
            } catch (IOException ex) {
                Logger.getLogger(PaletteVisibility.class.getName()).log( Level.INFO, null, ex );
            }
        }
        return res;
    }
    
    private static String getPaletteId( PaletteController pc ) {
        if( null == pc ) {
            return "_empty_";
        }
        DataFolder dof = (DataFolder)pc.getModel().getRoot().lookup( DataFolder.class );
        if( null != dof ) {
            FileObject fo = dof.getPrimaryFile();
            if( null != fo ) {
                return fo.getPath();
            }
        }
        return pc.getModel().getName();
        
    }
}
