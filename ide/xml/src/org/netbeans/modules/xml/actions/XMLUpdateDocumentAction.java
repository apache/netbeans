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

package org.netbeans.modules.xml.actions;

import java.util.Iterator;
import org.netbeans.modules.xml.cookies.UpdateDocumentCookie;
import org.netbeans.modules.xml.util.Util;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 * Reinitialize internal structures. User has strong feeling that he changed
 * external state that is not authomatically monitored by given data object.
 *
 */
public final class XMLUpdateDocumentAction extends CookieAction {
    
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -235822666875674523L;

    /* @return the mode of action. */
    protected int mode() {
        return MODE_ALL;
    }

    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName () {
        return Util.THIS.getString (XMLUpdateDocumentAction.class, "PROP_UpdateDocument");
    }

    protected Class[] cookieClasses () {
        return new Class[] { UpdateDocumentCookie.class };
    }

    /* Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (XMLUpdateDocumentAction.class);
    }

    // is called from a ModuleActions thread
    protected void performAction (final Node[] activatedNodes) {
        
        Lookup lookup = Lookup.getDefault();
        Lookup.Template<Class> template = new Lookup.Template(Performer.class);
        final Lookup.Result result = lookup.lookup(template);
        
        RequestProcessor.getDefault().postRequest(new Runnable() {
            public void run() {        
                for (int i = 0; i < activatedNodes.length; i++) {
                    UpdateDocumentCookie rc = activatedNodes[i].getCookie
                                                   (UpdateDocumentCookie.class);
                    if (rc != null) {
                        rc.updateDocumentRoot();
                    }

                    //??? unfortunatelly there can be only one cookie per node
                    // use lookup to emulate N-cookies - performers

                    // delegate to all registered performers
                    Iterator it = result.allInstances().iterator();
                    while (it.hasNext()) {
                        Performer next = (Performer) it.next();
                        next.perform(activatedNodes[i]);
                    }
                }
            }
        });
    }
    
    protected boolean asynchronous() {
        return false;
    }

    /**
     * To be somehow exposed via API, now XML module specifics.
     * It is registered at module layer. All registered performes are invoked.
     */
    public static interface Performer {
        
        /**
         * Update internal state after possible change of external resources
         * (flush caches etc.).
         */
        void perform(Node node);
    }
}
