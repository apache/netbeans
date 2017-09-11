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
