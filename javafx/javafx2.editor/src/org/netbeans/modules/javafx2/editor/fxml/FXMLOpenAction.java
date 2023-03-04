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
package org.netbeans.modules.javafx2.editor.fxml;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.javafx2.editor.spi.FXMLOpener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.*;

/**
 * Custom open action. The opening itself is delgated to an instance of
 * {@linkplain FXMLOpener} if registered. If the provided opener fails
 * a fallback strategy of opening the XML file in editor is taken.
 * 
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages("CTL_OpenAction=Open")
@ActionID(category="Edit", id="org.netbeans.modules.javafx2.editor.fxml.FXMLOpenAction")
@ActionReference(path="Loaders/text/x-fxml+xml/Actions", position=300)
@ActionRegistration(displayName="#CTL_OpenAction", lazy=false)
public class FXMLOpenAction extends AbstractAction implements ContextAwareAction, LookupListener {
    private final Lookup context;
    private Lookup.Result<DataObject> lkpInfo;
    
    private FXMLOpener opener;
    private FXMLOpener defaultOpener = new FXMLOpener() {
        @Override
        public boolean isEnabled(Lookup context) {
            return context.lookupAll(DataObject.class).size() == 1;
        }

        @Override
        public boolean open(Lookup context) {
            DataObject dobj = context.lookup(DataObject.class);
            OpenCookie oc = dobj.getCookie(OpenCookie.class);
            if (oc != null) {
                oc.open();
                return true;
            }
            return false;
        }
    };
    public FXMLOpenAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public FXMLOpenAction(Lookup context) {
        this.context = context;
        putValue(AbstractAction.NAME, Bundle.CTL_OpenAction());
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        
        setupOpener();
    }
    
    private void setupOpener() {
        opener = Lookup.getDefault().lookup(FXMLOpener.class);
    }
    
    void init() {
        if (lkpInfo != null) {
            return;
        }
 
        //The thing we want to listen for the presence or absence of
        //on the global selection
        lkpInfo = context.lookupResult(DataObject.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }
    
    @Override
    public boolean isEnabled() {
        init();
        return opener != null && opener.isEnabled(context) && super.isEnabled();
    }
 
    @Override
    public void resultChanged(LookupEvent ev) {
        setEnabled(!lkpInfo.allInstances().isEmpty());
    }
 
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new FXMLOpenAction(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isNewFile = Thread.currentThread().getStackTrace()[2].getFileName().equals("ProjectUtilities.java"); // NOI18N
        if (opener != null && !isNewFile) {
            opener.open(context);
        } else if (defaultOpener.isEnabled(context)) {
            defaultOpener.open(context);
        }
    }
}
