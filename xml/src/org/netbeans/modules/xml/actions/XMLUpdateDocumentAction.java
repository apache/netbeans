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
