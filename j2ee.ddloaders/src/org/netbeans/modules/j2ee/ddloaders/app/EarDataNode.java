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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A node to represent this ejb-jar.xml object.
 *
 * @author  Ludovic Champenois
 * @version 1.0
 */
public final class EarDataNode extends DataNode {
    
    private static final String DEPLOYMENT="deployment"; // NOI18N
 
    private EarDataObject dataObject;
   
    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /** Listener on dataobject */
    private PropertyChangeListener ddListener;

    private Action[] filteredActions;
    
    public EarDataNode (EarDataObject obj) {
        this (obj, Children.LEAF);
    }

    public EarDataNode (EarDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        setIconBaseWithExtension(dataObject.getIconBaseForValidDocument());
        initListeners();
    }

    /**
     * <strong>Note:</strong> Temporary fix for #77261. Remove when MultiView
     * editor is implemented for Enterprise Application deployment descriptor.
     */
    public Action[] getActions(boolean context) {
        if (filteredActions == null) {
            Action[] origActions = super.getActions(context);
            List<Action> actions = new ArrayList<Action>();
            // Fix for IZ#172674
            Action openAction = org.openide.util.actions.SystemAction.get(
                    OpenAction.class);
            for (int i = 0; i < origActions.length; i++) {
                if (origActions[i] instanceof OpenAction) {
                    continue;
                }
                /*
                 * Fix for IZ#172558 - Cannot open application.xml
                 */
                if ( openAction != null && origActions[i] != null && openAction.
                        getValue(Action.NAME).equals(origActions[i].getValue(
                                Action.NAME)))
                {
                    continue;
                }
                actions.add(origActions[i]);
            }
            filteredActions = (Action[]) actions.toArray(new Action[actions.size()]);
        }
        return filteredActions;
    }
    
    /** Initialize listening on adding/removing server so it is
     * possible to add/remove property sheets
     */
    private void initListeners(){
        ddListener = new PropertyChangeListener () {
            
            public void propertyChange (PropertyChangeEvent evt) {
                if (EarDataObject.PROP_DOCUMENT_DTD.equals (evt.getPropertyName ())) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, evt.getOldValue (), evt.getNewValue ());
                }
                if (DataObject.PROP_VALID.equals (evt.getPropertyName ())
                &&  Boolean.TRUE.equals (evt.getNewValue ())) {
                    removePropertyChangeListener (EarDataNode.this.ddListener);
                }
                if (EarDataObject.PROP_DOC_VALID.equals (evt.getPropertyName ())) {
                    if (Boolean.TRUE.equals (evt.getNewValue ()))
                        setIconBaseWithExtension(dataObject.getIconBaseForValidDocument());
                    else
                        setIconBaseWithExtension(dataObject.getIconBaseForInvalidDocument());
                }
                if (Node.PROP_PROPERTY_SETS.equals (evt.getPropertyName ())) {
                    firePropertySetsChange(null,null);
                }                
            }
            
        };
        getDataObject ().addPropertyChangeListener (ddListener);
    } 
    
    protected Sheet createSheet () {
        Sheet s = new Sheet ();
        Sheet.Set ss = new Sheet.Set ();
        ss.setName (DEPLOYMENT);
        ss.setDisplayName (NbBundle.getMessage (EarDataNode.class, "PROP_deploymentSet"));
        ss.setShortDescription (NbBundle.getMessage (EarDataNode.class, "HINT_deploymentSet"));  
        ss.setValue ("helpID", "TBD---Ludo ejbjar node");   // NOI18N
        
        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(EarDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(EarDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getApplication().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("TBD ejbjar file");//NOI18N
    }
    
}
