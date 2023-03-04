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

package org.netbeans.modules.j2ee.ddloaders.web;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import java.beans.*;

import java.net.URL;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;

/** A node to represent this object.
 *
 * @author  mkuchtiak
 * @version 1.0
 */
public class DDDataNode extends DataNode {

    private DDDataObject dataObject;

    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N

    /** Listener on dataobject */
    private PropertyChangeListener ddListener;
    
    public DDDataNode (DDDataObject obj) {
        this (obj, Children.LEAF);
    }

    public DDDataNode (DDDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        initListeners();
    }
    
    private static final java.awt.Image ERROR_BADGE = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/error-badge.gif" ); //NOI18N
    private static final java.awt.Image WEB_XML = 
        ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif" ); //NOI18N
    
    @Override
    public java.awt.Image getIcon(int type) {
        if (dataObject.getSaxError()==null)
            return WEB_XML;
        else 
            return ImageUtilities.mergeImages(WEB_XML, ERROR_BADGE, 6, 6);
    }
    
    @Override
    public String getShortDescription() {
        org.xml.sax.SAXException saxError = dataObject.getSaxError();
        if (saxError == null) {
            if (dataObject instanceof DDFragmentDataObject) {
                URL url = dataObject.getPrimaryFile().toURL();
                return NbBundle.getBundle(DDDataNode.class).getString("HINT_web_fragment_dd") + (url != null ? " ["+url+"]" : "");
            }
            else
                return NbBundle.getBundle(DDDataNode.class).getString("HINT_web_dd");
        } else {
            return saxError.getMessage();
        }
    }

    void iconChanged() {
        fireIconChange();
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
                if (DDDataObject.PROP_DOCUMENT_DTD.equals (propertyName)) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, oldValue, newValue);
                }
                if (DataObject.PROP_VALID.equals (propertyName)
                &&  Boolean.TRUE.equals (newValue)) {
                    removePropertyChangeListener (DDDataNode.this.ddListener);
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

    @Override
    protected Sheet createSheet () {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);

        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(DDDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(DDDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getWebApp().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
}
