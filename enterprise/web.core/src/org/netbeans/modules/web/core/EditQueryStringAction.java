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

package org.netbeans.modules.web.core;

import java.io.IOException;
import org.netbeans.modules.web.core.jsploader.JspLoader;
import org.netbeans.modules.web.core.jsploader.JspDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;

/**
* EditQueryStringAction.
*
* @author   Petr Jiricka
*/
@ActionID(id = "org.netbeans.modules.web.core.EditQueryStringAction", category = "Build")
@ActionRegistration(lazy = false, displayName = "#LBL_EditQueryString")
@ActionReference(path = "Shortcuts", name = "C-Q")
public class EditQueryStringAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8487176709444303658L;

    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        DataObject dObj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
        QueryStringCookie qsc = (QueryStringCookie)activatedNodes[0].getCookie(QueryStringCookie.class);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringLabel"),
                                             NbBundle.getBundle(EditQueryStringAction.class).getString("CTL_QueryStringTitle"));

        dlg.setInputText(WebExecSupport.getQueryString(dObj.getPrimaryFile()));

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            try {
                // WebExecSupport.setQueryString(dObj.getPrimaryFile(), dlg.getInputText());
                qsc.setQueryString (dlg.getInputText());
            }
            catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected boolean enable (Node[] activatedNodes){
        if (activatedNodes.length == 0) {
            return false;
        }
        for (int i = 0; i < activatedNodes.length; i++){
            DataObject dObj = (DataObject)(activatedNodes[i]).getCookie(DataObject.class);
            QueryStringCookie qsc = (QueryStringCookie)activatedNodes[i].getCookie(QueryStringCookie.class);

            if (qsc == null || dObj == null)
                return false;
            
            if (dObj instanceof JspDataObject){
                String ext = dObj.getPrimaryFile().getExt();
                if (ext.equals(JspLoader.TAGF_FILE_EXTENSION) 
                    || ext.equals(JspLoader.TAGX_FILE_EXTENSION)
                    || ext.equals(JspLoader.TAG_FILE_EXTENSION))
                        return false;
            }
        }
        return true;
    }
    /**
    * Returns QueryStringCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   // ExecCookie.class, DataObject.class
                   QueryStringCookie.class 
               };
    }

    /** @return the action's icon */
    public String getName() {
        return NbBundle.getBundle (EditQueryStringAction.class).getString ("LBL_EditQueryString");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (EditQueryStringAction.class);
    }
}

