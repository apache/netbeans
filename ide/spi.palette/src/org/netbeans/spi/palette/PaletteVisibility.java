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
