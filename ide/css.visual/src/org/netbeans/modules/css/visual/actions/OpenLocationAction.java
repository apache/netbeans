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
package org.netbeans.modules.css.visual.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.css.visual.spi.Location;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that opens a location in the editor.
 *
 * @author mfukala@netbeans.org.
 */
@NbBundle.Messages({
    "OpenLocationAction.displayName=Go to Source"
})
public class OpenLocationAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            final Location location = activatedNodes[i].getLookup().lookup(Location.class);
            FileObject fob = location.getFile();
            if (fob != null) {
                try {
                    DataObject dob = DataObject.find(fob);
                    if (location.getOffset() == -1) {
                        //just open file
                        Openable openable = dob.getLookup().lookup(Openable.class);
                        if (openable != null) {
                            openable.open();
                        }
                    } else {
                        //open and set caret to the location
                        final EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
                        if (ec != null) {
                            Mutex.EVENT.readAccess(new Runnable() {

                                @Override
                                public void run() {
                                    JEditorPane[] openedPanes = ec.getOpenedPanes();
                                    if (openedPanes != null && openedPanes.length > 0) {
                                        //already opened
                                        ec.open(); //give it a focus 
                                        JEditorPane pane = openedPanes[0];
                                        pane.setCaretPosition(location.getOffset());
                                    } else {
                                        //not opened, open it
                                        try {
                                            ec.openDocument();
                                            ec.open();
                                            openedPanes = ec.getOpenedPanes();
                                            if (openedPanes != null && openedPanes.length > 0) {
                                                //now opened
                                                JEditorPane pane = openedPanes[0];
                                                pane.setCaretPosition(location.getOffset());
                                            }
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }
                                
                            });
                        }

                    }
                } catch (DataObjectNotFoundException ex) {
                    Logger.getLogger(OpenLocationAction.class.getName()).log(Level.INFO, null, ex);
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
//        if (activatedNodes.length == 0) {
//            return false;
//        }
//        for (int i = 0; i < activatedNodes.length; i++) {
//            Location resource = activatedNodes[i].getLookup().lookup(Location.class);
//            if ((resource != null) && (resource.getFile() == null)) {
//                return false;
//            }
//        }
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return Bundle.OpenLocationAction_displayName();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
