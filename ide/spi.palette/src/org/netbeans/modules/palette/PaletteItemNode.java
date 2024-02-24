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

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;




/**
 *
 * @author Libor Kotouc
 */
public final class PaletteItemNode extends FilterNode implements Node.Cookie {
    
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private String name;
    private String bundleName;
    private String displayNameKey;
    private String className;
    private String tooltipKey; 
    private String icon16URL;
    private String icon32URL; 
    
    private String displayName;
    private String description;
    private Image icon16;
    private Image icon32;
    
    private DataObject originalDO;
    
    PaletteItemNode(DataNode original, 
                    String name, 
                    String bundleName, 
                    String displayNameKey, 
                    String className, 
                    String tooltipKey, 
                    String icon16URL, 
                    String icon32URL, 
                    InstanceContent content) 
    {
        super(original, Children.LEAF, new ProxyLookup(( new Lookup[] {new AbstractLookup(content), original.getLookup()})));
        
        content.add( this );
        this.name = name;
        this.bundleName = bundleName; 
        this.displayNameKey = displayNameKey;
        this.className = className;
        this.tooltipKey = tooltipKey;
        this.icon16URL = icon16URL;
        this.icon32URL = icon32URL;
        
        this.originalDO = original.getLookup().lookup(DataObject.class);
    }
 
    PaletteItemNode(DataNode original, 
                    String name, 
                    String displayName, 
                    String tooltip, 
                    String icon16URL, 
                    String icon32URL, 
                    InstanceContent content) 
    {
        super(original, Children.LEAF, new ProxyLookup(( new Lookup[] {new AbstractLookup(content), original.getLookup()})));
        
        content.add( this );
        this.name = name;
        assert null != displayName;
        this.displayName = displayName;
        this.description = tooltip;
        if( null == this.description )
            description = displayName;
        this.icon16URL = icon16URL;
        this.icon32URL = icon32URL;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        if (displayName == null)
            displayName = _getDisplayName(bundleName, displayNameKey, className);
        
        return displayName;
    }

    @Override
    public String getShortDescription() {
        if (description == null)
            description = _getShortDescription(bundleName, tooltipKey, className, displayNameKey);
        
        return description;
    }

    @Override
    public Image getIcon(int type) {

        Image icon = null;
        
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon16 == null) {
                icon16 = _getIcon(type, icon16URL);
                if (icon16 == null)
                    icon16 = ImageUtilities.loadImage("org/netbeans/modules/palette/resources/unknown16.gif"); // NOI18N
            }
            icon = icon16;
        }
        else if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            if (icon32 == null) {
                icon32 = _getIcon(type, icon32URL);
                if (icon32 == null)
                    icon32 = ImageUtilities.loadImage("org/netbeans/modules/palette/resources/unknown32.gif"); // NOI18N
            }
            icon = icon32;
        }
        
        return icon;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }

    // TODO properties
    @Override
    public Node.PropertySet[] getPropertySets() {
        return NO_PROPERTIES;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        
        Lookup lookup = getLookup();
        ActiveEditorDrop drop = (ActiveEditorDrop) lookup.lookup(ActiveEditorDrop.class);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(drop);
        t.put(s);
        
        //do not allow external DnD flavors otherwise some items may get interpreted
        //as an external file dropped into the editor window
        return new NoExternalDndTransferable( t );
    }

    @Override
    public Transferable drag() throws IOException {
        return clipboardCopy();
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
    
    public String _getDisplayName(
            String bundleName, 
            String displayNameKey, 
            String instanceName) 
    {

        String dName = null;
        try {
            dName = NbBundle.getBundle(bundleName).getString(displayNameKey);

            if (dName == null && displayNameKey != null)
                dName = displayNameKey;

            if (dName == null) {//derive name from the instance name
                if (instanceName != null && instanceName.trim().length() > 0) {
                    int dotIndex = instanceName.lastIndexOf('.'); // NOI18N
                    dName = instanceName.substring(dotIndex);
                }
            }

            if (dName == null) // no name derived from the item
                dName = name;

        }
        catch (Exception ex) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ex );
            // fall back to the original:
            dName = getOriginal().getDisplayName();
        }

        return (dName == null ? "" : dName);
    }

    public String _getShortDescription(
            String bundleName, 
            String tooltipKey, 
            String instanceName, 
            String displayNameKey) 
    {

        String tooltip = null;
        try {
            tooltip = NbBundle.getBundle(bundleName).getString(tooltipKey);

            if (tooltip == null && tooltipKey != null)
                tooltip = tooltipKey;

            if (tooltip == null) {//derive name from instance name
                if (instanceName != null && instanceName.trim().length() > 0) {
                    int dotIndex = instanceName.indexOf('.'); // NOI18N
                    tooltip = instanceName.substring(0, dotIndex).replace('-', '.'); // NOI18N
                }
            }

            if (tooltip == null) // no tooltip derived from the item
                tooltip = _getDisplayName(bundleName, displayNameKey, instanceName);

        }
        catch (Exception ex) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ex );
            // fall back to the original:
            tooltip = getOriginal().getShortDescription();
        }

        return (tooltip == null ? "" :  tooltip);
    }

    public Image _getIcon(int iconType, String iconURL) {

        Image icon = null;
        try {
            icon = ImageUtilities.loadImage(iconURL);
        }
        catch (Exception ex) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ex );
        }
        if( null == icon ) {
            try {
                //the URL may point to an external file
                icon = ImageIO.read( new URL(iconURL) );
            } catch( IOException ex ) {
                Logger.getLogger( getClass().getName() ).log( Level.INFO, null, ex );
                // fall back to the original:
                if (null != originalDO 
                        && !FileUtil.isParentOf( FileUtil.getConfigRoot(), originalDO.getPrimaryFile())) {
                    icon = getOriginal().getIcon(BeanInfo.ICON_COLOR_16x16);
                }
            }
        }

        return icon;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        DataNode dn = (DataNode) getOriginal();
        Object helpId = dn.getDataObject().getPrimaryFile().getAttribute("helpId"); //NOI18N
        return (helpId == null ? super.getHelpCtx() : new HelpCtx(helpId.toString()));
    }
    
    /**
     * Transferable wrapper that does not allow DataFlavors for external drag and drop
     * (FileListFlavor and URI list flavors)
     */
    private static class NoExternalDndTransferable implements Transferable {
        private Transferable t;
        private DataFlavor uriListFlavor;
        public NoExternalDndTransferable( Transferable t ) {
            this.t = t;
        }
    
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) 
                || t.isDataFlavorSupported( getUriListFlavor() ) ) {
                ArrayList<DataFlavor> tmp = new ArrayList<DataFlavor>( flavors.length );
                for( int i=0; i<flavors.length; i++ ) {
                    if( isDataFlavorSupported( flavors[i] ) )
                        tmp.add( flavors[i] );
                }
                flavors = tmp.toArray(new DataFlavor[0]);
            }
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            if( DataFlavor.javaFileListFlavor.equals( flavor ) || getUriListFlavor().equals( flavor ) )
                return false;
            return t.isDataFlavorSupported(flavor);
        }

        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported(flavor) )
                throw new UnsupportedFlavorException( flavor );
            return t.getTransferData( flavor );
        }
        
        private DataFlavor getUriListFlavor () {
            if( null == uriListFlavor ) {
                try {
                    uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                } catch (ClassNotFoundException ex) {
                    //cannot happen
                    throw new AssertionError(ex);
                }
            }
            return uriListFlavor;
        }
    }
}
