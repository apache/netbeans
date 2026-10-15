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
                    if (value instanceof String) {
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
