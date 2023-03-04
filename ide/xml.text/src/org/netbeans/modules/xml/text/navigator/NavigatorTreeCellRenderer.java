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

package org.netbeans.modules.xml.text.navigator;
import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import static org.netbeans.modules.xml.text.structure.XMLConstants.*;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;


/** TreeCellRenderer implementatin for the XML Navigator.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final String TAG_16 = "org/netbeans/modules/xml/text/navigator/resources/tag.png";
    private static final String PI_16 = "org/netbeans/modules/xml/text/navigator/resources/xml_declaration.png";
    private static final String DOCTYPE_16 = "org/netbeans/modules/xml/text/navigator/resources/doc_type.png";
    private static final String CDATA_16 = "org/netbeans/modules/xml/text/navigator/resources/cdata.png";
    
    private static final String ERROR_16 = "org/netbeans/modules/xml/text/navigator/resources/badge_error.png";
    
    private final Image ERROR_IMAGE = ImageUtilities.loadImage(ERROR_16, true);
   
    private final Icon[] TAG_ICON = new Icon[]{getImageIcon(TAG_16, false), getImageIcon(TAG_16, true)};
    private final Icon[] PI_ICON = new Icon[]{getImageIcon(PI_16, false), getImageIcon(PI_16, true)};
    private final Icon[] DOCTYPE_ICON = new Icon[]{getImageIcon(DOCTYPE_16, false), getImageIcon(DOCTYPE_16, true)};
    private final Icon[] CDATA_ICON = new Icon[]{getImageIcon(CDATA_16, false), getImageIcon(CDATA_16, true)};
     
    private HtmlRenderer.Renderer renderer;
    
    public NavigatorTreeCellRenderer() {
        super();
        renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(true);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        TreeNodeAdapter tna = (TreeNodeAdapter)value;
        DocumentElement de = (DocumentElement)tna.getDocumentElement();
        
        String htmlText = tna.getText(true);
        Component comp = renderer.getTreeCellRendererComponent(tree, htmlText, sel, expanded, leaf, row, hasFocus);
        comp.setEnabled(tree.isEnabled());
        ((JLabel)comp).setToolTipText(tna.getToolTipText().trim().length() > 0 ? tna.getToolTipText() : null);
        
        boolean containsError = tna.getChildrenErrorCount() > 0;
        //normal icons
        if(de.getType().equals(XML_TAG)
        || de.getType().equals(XML_EMPTY_TAG)) {
            setIcon(TAG_ICON, containsError);
        } else if(de.getType().equals(XML_PI)) {
            setIcon(PI_ICON, containsError);
        } else if(de.getType().equals(XML_DOCTYPE)) {
            setIcon(DOCTYPE_ICON, containsError);
        } else if(de.getType().equals(XML_CDATA)) {
            setIcon(CDATA_ICON, containsError);
        }
        
        return comp;
    }
    
    public void setIcon(Icon[] icons, boolean containsError) {
        renderer.setIcon(icons[containsError ? 1 : 0]);
    }
    
    private ImageIcon getImageIcon(String name, boolean error){
        ImageIcon icon = ImageUtilities.loadImageIcon(name, false);
        if(error)
            return new ImageIcon(ImageUtilities.mergeImages( icon.getImage(), ERROR_IMAGE, 15, 7 ));
        else
            return icon;
    }
    
}
