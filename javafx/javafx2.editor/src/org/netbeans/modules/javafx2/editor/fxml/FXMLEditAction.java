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
@NbBundle.Messages("CTL_EditAction=Edit")
@ActionID(category="Edit", id="org.netbeans.modules.javafx2.editor.fxml.FXMLEditAction")
@ActionReference(path="Loaders/text/x-fxml+xml/Actions", position=320)
@ActionRegistration(displayName="#CTL_EditAction", lazy=false)
public class FXMLEditAction extends AbstractAction implements ContextAwareAction, LookupListener {
    private final Lookup context;
    private Lookup.Result<DataObject> lkpInfo;
    
    public FXMLEditAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public FXMLEditAction(Lookup context) {
        this.context = context;
        putValue(AbstractAction.NAME, Bundle.CTL_EditAction());
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
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
        return super.isEnabled() && context.lookupAll(DataObject.class).size() == 1;
    }
 
    @Override
    public void resultChanged(LookupEvent ev) {
        setEnabled(!lkpInfo.allInstances().isEmpty());
    }
 
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new FXMLEditAction(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataObject dobj = context.lookup(DataObject.class);
        OpenCookie oc = dobj.getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }
}
