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

