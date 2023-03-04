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

package org.netbeans.modules.web.core.jsploader;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
* Edit an object.
* @see EditCookie
*
* @author Jaroslav Tulach
*/
public class EditServletAction extends CookieAction {

    /** serialVersionUID */
    private static final long serialVersionUID = 183706095337315796L;

    /** File types extensions which cannot be run separately so they do not have their 'own' servlet.*/
    private static final String[] UNSUPPORTED_EXTENSIONS = new String[]{"jspf", "tagf"};
    
    /* Returns false - action should be disabled when a window with no
    * activated nodes is selected.
    *
    * @return false do not survive the change of focus
    */
    @Override
    protected boolean surviveFocusChange () {
        return false;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(EditServletAction.class).getString("EditServlet");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (EditServletAction.class);
    }
    
    /*
     * We always enable View Servlet action, but show an error message 
     * in case when JSP has not been compiled yet.
     */ 
    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            JspDataObject jspdo = (JspDataObject)activatedNodes[i].getCookie(JspDataObject.class);
            if(jspdo != null) {
                FileObject jspfo = jspdo.getPrimaryFile();
                if(jspfo != null) {
                    String fileExt = jspfo.getExt();
                    if(fileExt != null && isUnsupportedExtension(fileExt)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isUnsupportedExtension(String ext) {
        for(String uext : UNSUPPORTED_EXTENSIONS) {
            if(uext.equals(ext)) return true;
        }
        return false;
    }
    
    /* @return the mode of action. */
    protected int mode() {
        return MODE_ANY;
    }

    /* Creates a set of classes that are tested by this cookie.
    * Here only HtmlDataObject class is tested.
    *
    * @return list of classes the that this cookie tests
    */
    protected Class[] cookieClasses () {
        return new Class[] { JspDataObject.class };
    }

    /* Actually performs the action.
    * Calls edit on all activated nodes which supports
    * HtmlDataObject cookie.
    */
    protected void performAction (final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            JspDataObject jspdo = (JspDataObject)activatedNodes[i].getCookie(JspDataObject.class);
            if (jspdo != null) {
                jspdo.refreshPlugin(true);
                EditorCookie cook = jspdo.getServletEditor();
                if (cook != null)
                    cook.open ();
                else {
                    //show error dialog
                    String msg = NbBundle.getMessage(EditServletAction.class, "ERR_CantEditServlet");
                    String title = NbBundle.getMessage(EditServletAction.class, "EditServlet");
                    NotifyDescriptor descriptor = new NotifyDescriptor(msg, title,
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE,
                            new Object[]{NotifyDescriptor.OK_OPTION}, null);
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
}
