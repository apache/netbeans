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
