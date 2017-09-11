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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.openide.actions;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/** Saves a data object to a folder under in the
* system's templates area.
*
* @author  Ales Novak, Dafe Simonek
*/
public final class SaveAsTemplateAction extends NodeAction {

    public HelpCtx getHelpCtx () {
        return new HelpCtx (SaveAsTemplateAction.class);
    }

    public String getName () {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "SaveAsTemplate");
    }

    /** @deprecated Should never be called publically. */
    @Deprecated @Override
    public String iconResource () {
        return super.iconResource ();
    }

    @Override
    protected boolean surviveFocusChange () {
        return false;
    }

    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0)
            return false;
        // test if all nodes support saving as template
        DataObject curCookie;
        for (int i = 0; i < activatedNodes.length; i++) {
            curCookie = activatedNodes[i].getCookie (DataObject.class);
            if ((curCookie == null) || (!curCookie.isCopyAllowed()))
                // not supported
                return false;
        }
        return true;
    }

    /* Performs the action - launches new file dialog,
    * saves as a template ...
    * Overrides abstract enable(..) from superclass.
    *
    * @param activatedNodes Array of activated nodes
    */
    protected void performAction (Node[] activatedNodes) {
        // prepare variables
        NodeAcceptor acceptor = FolderNodeAcceptor.getInstance();
        String title = NbBundle.getMessage(org.openide.loaders.DataObject.class, "Title_SaveAsTemplate");
        String rootTitle = NbBundle.getMessage(org.openide.loaders.DataObject.class, "CTL_SaveAsTemplate");
        Node templatesNode = NewTemplateAction.getTemplateRoot ();
        templatesNode.setDisplayName(NbBundle.getMessage(org.openide.loaders.DataObject.class, "CTL_SaveAsTemplate_TemplatesRoot"));
        Node[] selected;
        // ask user: where to save the templates?
        try {
            selected = NodeOperation.getDefault().
                       select(title, rootTitle, templatesNode, acceptor, null);
        } catch (UserCancelException ex) {
            // user cancelled the operation
            return;
        }
        // create & save them all
        // we know DataFolder and DataObject cookies must be supported
        // so we needn't check for null values
        DataFolder targetFolder =
            selected[0].getCookie (DataFolder.class);
        for (int i = 0; i < activatedNodes.length; i++ ) {
            createNewTemplate(
                activatedNodes[i].getCookie (DataObject.class),
                targetFolder);
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine"; // NOI18N

    /** Performs the work of creating a new template */
    private void createNewTemplate(DataObject source,
                                   DataFolder targetFolder) {
        try {
            SaveCookie cookie = source.getCookie (SaveCookie.class);
            if (cookie != null) {
                cookie.save ();
            }
            DataObject newTemplate = source.copy(targetFolder);
            DataObject templateSample = null;
            for (DataObject d : targetFolder.getChildren ()) {
                if (d.isTemplate ()) {
                    templateSample = d;
                    break;
                }
            }
            newTemplate.setTemplate(true);
            if (templateSample == null) {
                // a fallback if no template sample found
                newTemplate.getPrimaryFile().setAttribute(SCRIPT_ENGINE_ATTR, "freemarker"); // NOI18N
            } else {
                setTemplateAttributes (newTemplate.getPrimaryFile (), getAttributes (templateSample.getPrimaryFile ()));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** Sets attributes for given FileObject. */ // XXX: copied from org.netbeans.modules.favorites.templates
    private static void setTemplateAttributes (FileObject fo, Map<String, Object> attributes) throws IOException {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            // skip localizing bundle for custom templates
            if ("SystemFileSystem.localizingBundle".equals (entry.getKey ())) { // NOI18N
                continue;
            }
            fo.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /** Returns map of attributes for given FileObject. */ // XXX: copied from org.netbeans.modules.favorites.templates
    private static Map<String, Object> getAttributes (FileObject fo) {
        HashMap<String, Object> attributes = new HashMap<String, Object> ();
        Enumeration<String> attributeNames = fo.getAttributes ();
        while (attributeNames.hasMoreElements ()) {
            String attrName = attributeNames.nextElement ();
            if (attrName == null) {
                continue;
            }
            Object attrValue = fo.getAttribute (attrName);
            if (attrValue != null) {
                attributes.put (attrName, attrValue);
            }
        }
        return attributes;
    }

    /** Inner class functioning like node acceptor for
    * user dialogs when selecting where to save as template.
    * Accepts folders only. Singleton.
    */
    static final class FolderNodeAcceptor implements NodeAcceptor {

        /** an instance */
        private static FolderNodeAcceptor instance;

        private DataFolder rootFolder;

        /** singleton */
        private FolderNodeAcceptor (DataFolder root) {
            this.rootFolder = root;
        }

        /** accepts a selected folder */
        public boolean acceptNodes (Node[] nodes) {
            boolean res = false;
            if (nodes == null || nodes.length != 1) {
                res = false;
            } else {
                Node n = nodes [0];
                DataFolder df = n.getCookie(DataFolder.class);
                if (df != null) {
                    res = ! rootFolder.equals (df);
                }
            }
            return res;
        }

        /** getter for an instance */
        static FolderNodeAcceptor getInstance() {
            DataFolder rootFolder = NewTemplateAction.getTemplateRoot ().getCookie (DataFolder.class);
            if (instance == null) instance = new FolderNodeAcceptor(rootFolder);
            return instance;
        }
    } // end of FolderNodeAcceptor inner class

}
