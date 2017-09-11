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

package org.netbeans.modules.html.api;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.actions.Viewable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.html.HtmlDataObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Node that represents HTML data object.
 *
 * @author  Radim Kubacki
 */
public final class HtmlDataNode extends org.openide.loaders.DataNode {
    
    private static final String PROP_FILE_ENCODING = "encoding"; //NOI18N
    private static final String SHEETNAME_TEXT_PROPERTIES = "textProperties"; // NOI18N
    private Sheet sheet = null;
    
    private Node.PropertySet[] customPropertySet;
    
    private static final String VIEWABLE_CLASS_NAME = Viewable.class.getName();
    
    /** Creates new HtmlDataNode */
    public HtmlDataNode(DataObject dobj, Children ch) {
        super(dobj, ch);
        setShortDescription(NbBundle.getMessage(HtmlDataObject.class, "LBL_htmlNodeShortDesc"));
    }
    
    private boolean isHtmlProject() {
        Project current = FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
        return current != null && current.getClass().getName().equals("org.netbeans.modules.web.clientproject.ClientSideProject"); //NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions(context);
        if (isHtmlProject()) {

            //filter out view action in html project
            List<Action> filtered = new ArrayList<Action>();
            for (int i = 0; i < actions.length; i++) {
                Action a = actions[i];
                if (a != null) {
                    Object value = a.getValue("type"); //NOI18N
                    if (value != null && (value instanceof String)) {
                        String type = (String) value;
                        if (VIEWABLE_CLASS_NAME.equals(type)) {
                            continue;
                        }
                    }
                }
                filtered.add(a);
            }
            return filtered.toArray(new Action[0]);
        }


        return actions;

    }
    
    @Override
    public Node.PropertySet[] getPropertySets() {
        if(customPropertySet != null) {
            return customPropertySet;
        }
        
        if(sheet == null) {
            sheet = new Sheet();
            
            Node.PropertySet[] tmp = super.getPropertySets();
            Sheet.Set set;
            for(int i = 0; i < tmp.length; i++) {
                set = new Sheet.Set();
                set.setName(tmp[i].getName());
                set.setShortDescription(tmp[i].getShortDescription());
                set.setDisplayName(tmp[i].getDisplayName());
                set.setValue("helpID", HtmlDataNode.class.getName() + ".PropertySheet");// NOI18N
                set.put(tmp[i].getProperties());
                sheet.put(set);
            }
            // add encoding property
            set = new Sheet.Set();
            set.setName(SHEETNAME_TEXT_PROPERTIES);
            set.setDisplayName(NbBundle.getMessage(HtmlDataObject.class, "PROP_textfileSetName")); // NOI18N
            set.setShortDescription(NbBundle.getMessage(HtmlDataObject.class, "HINT_textfileSetName")); // NOI18N
            set.put(new PropertySupport.ReadOnly(
                    PROP_FILE_ENCODING,
                    String.class,
                    NbBundle.getMessage(HtmlDataObject.class, "PROP_fileEncoding"), //NOI18N
                    NbBundle.getMessage(HtmlDataObject.class, "HINT_fileEncoding") //NOI18N
                    ) {
                        @Override
                public Object getValue() {
                    return FileEncodingQuery.getEncoding(getDataObject().getPrimaryFile()).name();
                }
            });
            sheet.put(set);
        }
        return sheet.toArray();
    }

    /**
     * Sets custom Node.PropertySet[] to this node.
     * 
     * {@link #firePropertySetsChange(org.openide.nodes.Node.PropertySet[], org.openide.nodes.Node.PropertySet[])} is called afterwards.
     * 
     * @since 1.46
     * @param sets the custom property sets or null if the default property sets should be used.
     */
    public void setPropertySets(Node.PropertySet[] sets) {
        Node.PropertySet[] old = customPropertySet != null ? customPropertySet : getPropertySets();
        Node.PropertySet[] neww = sets != null ? sets : getPropertySets();
        
        customPropertySet = sets;
        
        firePropertySetsChange(old, neww);
    }
    
}
