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

package org.netbeans.modules.j2ee.ddloaders.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;

/** A node to represent this ejb-jar.xml object.
 *
 * @author  Ludovic Champenois
 * @version 1.0
 */
public class ClientDataNode extends DataNode {
    
    private static final String DEPLOYMENT = "deployment"; // NOI18N
    private static final java.awt.Image ERROR_BADGE = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/client/error-badge.gif" ); //NOI18N
    private static final java.awt.Image CLIENT_XML = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/client/DDDataIcon.gif" ); //NOI18N
 
    private ClientDataObject dataObject;
   
    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /** Listener on dataobject */
    private PropertyChangeListener ddListener;
    
    public ClientDataNode (ClientDataObject obj) {
        this (obj, Children.LEAF);
    }

    public ClientDataNode (ClientDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        initListeners();
    }

    /** Initialize listening on adding/removing server so it is 
     * possible to add/remove property sheets
     */
    private void initListeners(){
        ddListener = new PropertyChangeListener () {
            
            public void propertyChange (PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName ();
                Object newValue = evt.getNewValue ();
                Object oldValue = evt.getOldValue ();
                if (ClientDataObject.PROP_DOCUMENT_DTD.equals (propertyName)) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                }
                if (ClientDataObject.PROP_VALID.equals (propertyName)
                &&  Boolean.TRUE.equals (newValue)) {
                    removePropertyChangeListener (ClientDataNode.this.ddListener);
                }
                if (Node.PROP_PROPERTY_SETS.equals (propertyName)) {
                    firePropertySetsChange(null,null);
                }
                if (XmlMultiViewDataObject.PROP_SAX_ERROR.equals(propertyName)) {
                    fireShortDescriptionChange((String) oldValue, (String) newValue);
                }
            }
            
        };
        getDataObject ().addPropertyChangeListener (ddListener);
    } 
    
    private ClientDataObject getDDDataObject () {
        return (ClientDataObject) getDataObject ();
    }
   
    protected Sheet createSheet () {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(ClientDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(ClientDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getAppClient().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("TBD application-client.xml file");//NOI18N
    }

    void iconChanged() {
        fireIconChange();
    }

    public java.awt.Image getIcon(int type) {
        if (dataObject.getSaxError()==null)
            return CLIENT_XML;
        else 
            return ImageUtilities.mergeImages(CLIENT_XML, ERROR_BADGE, 6, 6);
    }
    
    public String getShortDescription() {
        org.xml.sax.SAXException saxError = dataObject.getSaxError();
        if (saxError==null) {
            return NbBundle.getBundle(ClientDataNode.class).getString("HINT_web_dd");
        } else {
            return saxError.getMessage();
        }
    }

}
