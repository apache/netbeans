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
