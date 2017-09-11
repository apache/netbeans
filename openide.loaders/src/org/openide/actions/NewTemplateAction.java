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

package org.openide.actions;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.modules.openide.loaders.DataNodeUtils;
import org.openide.awt.*;
import org.openide.explorer.view.MenuView;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

/** Creates a new object from template in the selected folder.
* @see DataObject#isTemplate
*
* @author Petr Hamernik, Dafe Simonek
*/
public class NewTemplateAction extends NodeAction {

    private static DataObject selectedTemplate;
    private static DataFolder targetFolder;

    /** Maximum count of recent templates. */
    private static int MAX_RECENT_ITEMS = 5;
    
    /** Getter for wizard.
     * @param the node that is currently activated
     * @return the wizard or null if the wizard should not be enabled
    */
    static TemplateWizard getWizard (Node n) {
        if (n == null) {
            Node[] arr = WindowManager.getDefault ().getRegistry ().getActivatedNodes ();
            if (arr.length == 1) {
                n = arr[0];
            }
        }

        // if activated node isn't folder try parent which should be folder
        Node folder = n;
        // bugfix #29661, start finding the target folder with null folder
        targetFolder = null;
        while (targetFolder == null && folder != null) {
            targetFolder = folder.getCookie(DataFolder.class);
            folder = folder.getParentNode ();
        }
        
        NewTemplateAction.Cookie c = n == null ? null : n.getCookie(NewTemplateAction.Cookie.class);
        if (c != null) {
            TemplateWizard t = c.getTemplateWizard ();
            if (t != null) {
                return t;
            }
        }

        return new DefaultTemplateWizard();
    }

    private boolean active = false;

    // This method is called only for the File->New menu item
    // it gets the node selection from the active TC
    protected void performAction (Node[] activatedNodes) {
        if (active)
            return;
        
        active = true;
        
        Node n = activatedNodes.length == 1 ? activatedNodes[0] : null;
        TemplateWizard wizard = getWizard (n);
        if (wizard instanceof DefaultTemplateWizard) {
            if (targetFolder != null && targetFolder.isValid())
                wizard.setTargetFolder(targetFolder);
            if (selectedTemplate != null && selectedTemplate.isValid())
                wizard.setTemplate(selectedTemplate);
        }
        boolean instantiated = false;
        try {
            // clears the name to default
            wizard.setTargetName(null);
            // instantiates
            instantiated = wizard.instantiate() != null;
        } catch (IOException e) {
            Exceptions.attachLocalizedMessage(e,
                                              org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class,
                                                                                   "EXC_TemplateFailed"));
            Exceptions.printStackTrace(e);
        }
        finally {
            if (wizard instanceof DefaultTemplateWizard) {
                try {
                    if (instantiated) {
                        selectedTemplate = wizard.getTemplate();
                        // Put the template in the recent list
                        if (selectedTemplate != null) {
                            recentChanged = addRecent (selectedTemplate);
                        }
                    }
                    // else selectedTemplate might be e.g. Templates folder itself
                    // which would cause an IOException when trying to make a link
                    targetFolder = wizard.getTargetFolder();
                }
                catch (IOException ignore) {
                    selectedTemplate = null;
                    targetFolder = null;
                }
            }
            active = false;
        }
    }
    
    protected boolean asynchronous() {
        return true;
    }

    /* Enables itself only when activates node is DataFolder.
    */
    protected boolean enable (Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1))
            return false;

        NewTemplateAction.Cookie c = activatedNodes[0].getCookie(NewTemplateAction.Cookie.class);
        if (c != null) {
            // if the current node provides its own wizard...
            return c.getTemplateWizard () != null;
        }
        
        DataFolder cookie = activatedNodes[0].getCookie(DataFolder.class);
        if (cookie != null && cookie.getPrimaryFile ().canWrite ()) {
            return true;
        }
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(DataObject.class, "NewTemplate");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (NewTemplateAction.class);
    }
    
    public JMenuItem getMenuPresenter () {
        return new Actions.MenuItem (this, true) {
                   public void setEnabled (boolean e) {
                       super.setEnabled (true);
                   }
               };
    }

    public Component getToolbarPresenter () {
        return new Actions.ToolbarButton (this) {
                   public void setEnabled (boolean e) {
                       super.setEnabled (true);
                   }
               };
    }
    
    /* Creates presenter that displayes submenu with all
    * templates.
    */
    public JMenuItem getPopupPresenter() {
        return getPopupPresenter (null, this);
    }
    
    private JMenuItem getPopupPresenter (final Lookup actionContext, Action action) {
        Node[] nodes = new Node[0];
        if (actionContext != null) {
            nodes = getNodesFromLookup (actionContext);
        }
        final Node n = (nodes.length == 1) ? nodes[0] : null;
        TemplateWizard tw = getWizard (n);
        
        if (tw instanceof DefaultTemplateWizard) {
            return new MenuWithRecent (n, this.isEnabled ());
        } else {
            // The null is correct but depends on the impl of MenuView.Menu
            JMenuItem menu = new MenuView.Menu (null, new TemplateActionListener (actionContext), false) {
                // this is the only place MenuView.Menu needs the node ready
                // so lets prepare it on-time
                public JPopupMenu getPopupMenu () {
                    if (node == null) node = getTemplateRoot (n);
                    return super.getPopupMenu ();
                }
            };
            Actions.connect (menu, action, true);
            return menu;
        }
    }
    

    private class MenuWithRecent extends JMenuPlus {
        private boolean initialized = false;
        private Node node;
        private boolean canWrite;
        
        public MenuWithRecent(Node n, boolean writable) {
            super(); //NewTemplateAction.this.getName());
            Actions.setMenuText(this, NewTemplateAction.this.getName(), false);
            node = n;
            canWrite = writable;
        }
        
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            if (!initialized) {
                popup.add(new Item(null)); // New... item
            
                List privileged = getPrivilegedList();
                // all fixed items
                if (privileged.size() > 0) popup.add(new JSeparator()); // separator
                for (Iterator it = privileged.iterator(); it.hasNext(); ) {
                    DataObject dobj = (DataObject)it.next();
                    if (dobj instanceof DataShadow)
                                dobj = ((DataShadow)dobj).getOriginal();
                    popup.add(new Item(dobj));
                }

                // all recent items
                boolean regenerate = false;
                boolean addSeparator = ! getRecentList ().isEmpty ();
                for (Iterator it = getRecentList ().iterator(); it.hasNext(); ) {
                    DataObject dobj = (DataObject)it.next ();
                    if (isValidTemplate (dobj)) {
                        if (addSeparator) popup.add (new JSeparator ()); // separator
                        addSeparator = false;
                        popup.add (new Item (dobj));
                    } else {
                        // some template was unvalidated => have to regenerate next time
                        regenerate = true;
                    }
                }
                recentChanged = recentChanged || regenerate;
                initialized = true;
            }
            return popup;
        }
        
        private class Item extends JMenuItem implements HelpCtx.Provider, ActionListener {
            DataObject template; // Null means no template -> show the chooser
            public Item(DataObject template) {
                super();
                this.template = template;
                
                setText (template == null ? 
                    NbBundle.getMessage(DataObject.class, "NewTemplateAction") :
                    template.getNodeDelegate().getDisplayName()
                );
                    
                if (template == null) {
                    setIcon (NewTemplateAction.this.getIcon());
                } else {
                    setIcon (new ImageIcon(template.getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)));
                }
                
                addActionListener(this);
                // recommendation from issue 32191, don't enable popup menu items on read-only folders
                setEnabled (canWrite);
            }
            
            /** Get context help for this item.*/
            public HelpCtx getHelpCtx() {
                if (template != null) {
                    return template.getHelpCtx();
                }
                return NewTemplateAction.this.getHelpCtx();
            }
            
            /** Invoked when an action occurs. */
            public void actionPerformed(ActionEvent e) {
                doShowWizard(template, node);
            }
        }
    }
    
    /** Cached content of Templates/Privileged */
    private DataFolder privilegedListFolder;
    
    /** Cached content of Templates/Recent */
    private DataFolder recentListFolder;
    
    private boolean recentChanged = true;
    private List<DataObject> recentList = new ArrayList<DataObject> (0);
    
    private List<DataObject> getPrivilegedList() {
        if (privilegedListFolder == null) {
            FileObject fo = FileUtil.getConfigFile("Templates/Privileged"); // NOI18N
            if (fo != null) privilegedListFolder = DataFolder.findFolder(fo);
        }
        if (privilegedListFolder != null) {
            DataObject[] data = privilegedListFolder.getChildren();
            List<DataObject> l2 = new ArrayList<DataObject>(data.length);
            for (int i=0; i<data.length; i++) {
                DataObject dobj = data[i];
                if (dobj instanceof DataShadow)
                                dobj = ((DataShadow)dobj).getOriginal();
                if (isValidTemplate (dobj)) {
                    l2.add(dobj);
                }
            }
            return l2;
        } else {
            return new ArrayList<DataObject>(0);
        }
    }

    private void doShowWizard(DataObject template, Node node) {
        targetFolder = null;
        TemplateWizard wizard = getWizard (node);

        try {
            wizard.setTargetName (null);
            Set created = wizard.instantiate (template, targetFolder);
            if (created != null && wizard instanceof DefaultTemplateWizard) {
                // put the item in the recent list
                selectedTemplate = wizard.getTemplate();
                if (selectedTemplate != null) {
                    // bugfix #36604; notify that the list recent used templates changed
                    recentChanged = addRecent (selectedTemplate);
                }
            }
        } catch (IOException e) {
            Exceptions.attachLocalizedMessage(e,
                                              org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class,
                                                                                   "EXC_TemplateFailed"));
            Exceptions.printStackTrace(e);
        }
    }
    
    private DataFolder getRecentFolder () {
        if (recentListFolder == null) {
            FileObject fo = FileUtil.getConfigFile("Templates/Recent"); // NOI18N
            if (fo != null) {
                recentListFolder = DataFolder.findFolder(fo);
            }
        }
        
        return recentListFolder;
    }
    
    private List<DataObject> getRecentList () {
        if (!recentChanged) return recentList;
        if (getRecentFolder () != null) {
            DataObject[] data = getRecentFolder ().getChildren ();
            List<DataObject> l2 = new ArrayList<DataObject>(data.length);
            for (int i=0; i<data.length; i++) {
                DataObject dobj = data[i];
                if (dobj instanceof DataShadow)
                                dobj = ((DataShadow)dobj).getOriginal();
                if (isValidTemplate (dobj)) {
                    l2.add(dobj);
                } else {
                    removeRecent (data[i]);
                }
            }
            recentList = l2;
        } else {
            recentList = new ArrayList<DataObject> (0);
        }
        
        recentChanged = false;
        
        return recentList;
    }
    
    private boolean isValidTemplate (DataObject template) {
        return (template != null) && template.isTemplate () && template.isValid ();
    }

    private boolean addRecent (DataObject template) {
        DataFolder folder = getRecentFolder ();
        
        // no recent folder, no recent templates
        if (folder == null) return false;
        
        // check if privileged
        if (getPrivilegedList ().contains (template)) return false;
        
        // check if recent already
        if (isRecent (template)) return false;
        
        DataObject[] templates = folder.getChildren ();
        
        DataObject[] newOrder = new DataObject[templates.length + 1];
        for (int i = 1; i < newOrder.length; i++) {
            newOrder[i] = templates[i - 1];
        }
        
        try {
            newOrder[0] = template.createShadow (folder);
            folder.setOrder (newOrder);
        } catch (IOException ioe) {
            Logger.getLogger(NewTemplateAction.class.getName()).log(Level.WARNING, null, ioe);
            // can't create shadow
            return false;
        }
        
        // reread children
        templates = folder.getChildren ();
        int size = templates.length;
        
        while (size > MAX_RECENT_ITEMS) {
            // remove last
            removeRecent (templates[size - 1]);
            size--;
        }
        
        return true;
    }
    
    private boolean removeRecent (DataObject template) {
        DataFolder folder = getRecentFolder ();
        
        // no recent folder, no recent templates
        if (folder == null) return false;
        
        try {
            template.delete ();
            return true;
        } catch (IOException ioe) {
            Logger.getLogger(NewTemplateAction.class.getName()).log(Level.WARNING, null, ioe);
            // it couldn't be deleted
            return false;
        }
    }
    
    private boolean isRecent (DataObject template) {
        return getRecentList ().contains (template);
    }
    
    /** Create a hierarchy of templates.
    * @return a node representing all possible templates
    */
    public static Node getTemplateRoot () {
        RootChildren ch = new RootChildren (null);
        // create the root
        return ch.getRootFolder ().new FolderNode (ch);
    }
    
    private static Node getTemplateRoot (Node n) {
        RootChildren ch = new RootChildren (n);
        // create the root
        Node help = ch.getRootFolder ().new FolderNode (ch);
        return help;
    }
    
    /** Cookie that can be implemented by a node if it wishes to have a 
     * special templates wizard.
     */
    public static interface Cookie extends Node.Cookie {
        /** Getter for the wizard that should be used for this cookie.
         */
        public TemplateWizard getTemplateWizard ();
    }
    
    /** Checks whether an object is acceptable for display as a container.
     */
    private static boolean acceptObj (DataObject obj) {
        if (obj.isTemplate ()) {
            return true;
        }

        if (obj instanceof DataFolder) {
            Object o = obj.getPrimaryFile ().getAttribute ("simple"); // NOI18N
            return o == null || Boolean.TRUE.equals (o);
        }

        return false;
        
    }


    /** Actions listener which instantiates the template */
    private static class TemplateActionListener implements NodeAcceptor, DataFilter {
        static final long serialVersionUID =1214995994333505784L;
        Lookup actionContext;
        TemplateActionListener(Lookup context) {
            actionContext = context;
        }
        public boolean acceptNodes (Node[] nodes) {
            Node[] nodesInContext = null;
            if (actionContext != null) {
                nodesInContext = getNodesFromLookup (actionContext);
            }
            if ((nodesInContext == null) || (nodesInContext.length != 1)) {
                Logger.getAnonymousLogger().warning("Wrong count of nodes in context lookup."); //NOI18N
                return false;
            }
            if ((nodes == null) || (nodes.length != 1)) {
                Logger.getAnonymousLogger().warning("Wrong count of selected nodes in popup menu."); //NOI18N
                return false;
            }
            Node n = nodes[0];
            DataObject obj = n.getCookie(DataObject.class);
            if (obj == null || !obj.isTemplate ()) {
                Logger.getAnonymousLogger().warning("Selected node in popup menu is not acceptable."); //NOI18N
                // do not accept
                return false;
            }
            
            // bugfix #38421, read node in contextLookup to select the right wizard
            TemplateWizard wizard = getWizard (nodesInContext[0]);
            
            try {
                wizard.setTargetName (null);
                wizard.instantiate (obj, targetFolder);
            } catch (IOException e) {
                Exceptions.attachLocalizedMessage(e,
                                                  org.openide.util.NbBundle.getMessage(org.openide.loaders.DataObject.class,
                                                                                       "EXC_TemplateFailed"));
                Exceptions.printStackTrace(e);
            }

            // ok
            return true;
        }

        /** Data filter impl.
        */
        public boolean acceptDataObject (DataObject obj) {
            return acceptObj (obj);
        }
    }
    
    /** Root template childen.
     */
    private static class RootChildren extends Children.Keys<Node>
    implements NodeListener {
        /** last wizard used with the root */
        private TemplateWizard wizard;
        /** Folder of templates */
        private DataFolder rootFolder;
        /** node to display templates for or null if current selection
         * should be followed
         */
        private WeakReference<Node> current;
        /** weak listener */
        private NodeListener listener = org.openide.nodes.NodeOp.weakNodeListener (this, null);
        
        /** Instance not connected to any node.
         */
        public RootChildren (Node n) {
            TopComponent.Registry reg = WindowManager.getDefault ().getRegistry ();
            reg.addPropertyChangeListener (org.openide.util.WeakListeners.propertyChange (this, reg));
            
            updateWizard (getWizard (n));
        }
        
        public DataFolder getRootFolder () {
            if (rootFolder == null) {
                // if rootFolder is null then initialize folder
                doSetKeys ();
            }
            return rootFolder;
        }
               

        /** Creates nodes for nodes.
         */
        protected Node[] createNodes(Node n) {
            String nodeName = n.getDisplayName();
            
            DataObject obj = null;
            DataShadow shadow = n.getCookie(DataShadow.class);
            if (shadow != null) {
                // I need DataNode here to get localized name of the
                // shadow, but without the ugly "(->)" at the end
                DataNode dn = new DataNode(shadow, Children.LEAF);
                nodeName = dn.getDisplayName();
                obj = shadow.getOriginal();
                n = obj.getNodeDelegate();
            }
            
            if (obj == null)
                obj = n.getCookie(DataObject.class);
            if (obj != null) {
                if (obj.isTemplate ()) {
                    // on normal nodes stop recursion
                    return new Node[] { new DataShadowFilterNode (n, LEAF, nodeName) };
                }
            
                if (acceptObj (obj)) {
                    // on folders use normal filtering
                    return new Node[] { new DataShadowFilterNode (n, new TemplateChildren (n), nodeName) };
                }
            }
            
            return null;
        }
        
        /** Check whether the node has not been updated.
         */
        private void updateNode (Node n) {            
            if (current != null && current.get () == n) {
                return;
            }
            
            Node prev = current != null? current.get(): null;
            if (prev != null) {
                prev.removeNodeListener (listener);
            }
            
            n.addNodeListener (listener);
            current = new WeakReference<Node> (n);
        }
        
        /** Check whether the wizard was not updated.
         */
        private void updateWizard (TemplateWizard w) {
            if (wizard == w) {
                return;
            }
            
            if (wizard != null) {
                Node n = wizard.getTemplatesFolder ().getNodeDelegate ();
                n.removeNodeListener (listener);
            }
            
            Node newNode = w.getTemplatesFolder ().getNodeDelegate ();
            newNode.addNodeListener (listener);
            wizard = w;
            
            updateKeys ();
        }
        
        /** Updates the keys.
         */
        private void updateKeys () {
            // updateKeys can be called while holding Children.MUTEX
            //   --> replan getNodes(true) to a new thread
            DataNodeUtils.reqProcessor().post(new Runnable() {
                @Override
                public void run() {
                    doSetKeys ();
                }
            });
        }
        
        // don't call this while holding Children.MUTEX
        private void doSetKeys () {
            rootFolder = wizard.getTemplatesFolder ();
            setKeys (rootFolder.getNodeDelegate ().getChildren ().getNodes (true));
        }
         
         /** Fired when the order of children is changed.
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         */
        public void childrenReordered(NodeReorderEvent ev) {
            updateKeys ();
        }        
        
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            updateKeys ();
        }
        
        /** Fired when a set of new children is added.
         * @param ev event describing the action
         */
        public void childrenAdded(NodeMemberEvent ev) {
            updateKeys ();
        }
        
        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
        }

        /** Listen on changes of cookies.
         */
        public void propertyChange(java.beans.PropertyChangeEvent ev) {
            String pn = ev.getPropertyName ();
            
            if (current != null && ev.getSource () == current.get ()) {
                // change in current node
                if (Node.PROP_COOKIE.equals (pn)) {
                    final Node node = current.get();
                    Mutex.EVENT.readAccess(new Runnable() {
                        public void run() {
                            updateWizard (getWizard (node));
                        }
                    });
                }
            } else {
                // change in selected nodes
                if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals (pn)) {
                    // change the selected node
                    Node[] arr = WindowManager.getDefault ().getRegistry ().getActivatedNodes ();
                    if (arr.length == 1) {
                        // only if the size is 1
                        updateNode (arr[0]);
                    }
                }
            }
        }
        
    }
    
    /** Filter node children, that stops on data objects (does not go futher)
    */
    private static class TemplateChildren extends FilterNode.Children {
        public TemplateChildren (Node or) {
            super (or);
        }
        
        /** Creates nodes for nodes.
         */
        @Override
        protected Node[] createNodes(Node n) {
            if (EventQueue.isDispatchThread()) {
                return new Node[] { new DataShadowFilterNode(n, LEAF, n.getDisplayName()) };
            }
            
            String nodeName = n.getDisplayName();
            
            DataObject obj = null;
            DataShadow shadow = n.getCookie(DataShadow.class);
            if (shadow != null) {
                // I need DataNode here to get localized name of the
                // shadow, but without the ugly "(->)" at the end
                DataNode dn = new DataNode(shadow, Children.LEAF);
                nodeName = dn.getDisplayName();
                obj = shadow.getOriginal();
                n = obj.getNodeDelegate();
            }
            
            if (obj == null)
                obj = n.getCookie(DataObject.class);
            if (obj != null) {
                if (obj.isTemplate ()) {
                    // on normal nodes stop recursion
                }
            
                if (acceptObj (obj)) {
                    // on folders use normal filtering
                    return new Node[] { new DataShadowFilterNode (n, new TemplateChildren (n), nodeName) };
                }
            }
            return new Node[] { new DataShadowFilterNode(n, LEAF, nodeName) };
        }

        final void refreshKeyImpl(Node n) {
            refreshKey(n);
        }

    }

    private static class DataShadowFilterNode extends FilterNode implements NodeListener {
        
        private String name;
        
        public DataShadowFilterNode (Node or, org.openide.nodes.Children children, String name) {
            super (or, children);
            this.name = name;
            disableDelegation(FilterNode.DELEGATE_SET_DISPLAY_NAME);
            or.addNodeListener(NodeOp.weakNodeListener(this, or));
                    
        }
        
        @Override
        public String getDisplayName() {
            return name;
        }

        private void refresh() {
            Node n = getOriginal();
            String nodeName = n.getDisplayName();

            DataObject obj = null;
            DataShadow shadow = n.getCookie(DataShadow.class);
            if (shadow != null) {
                // I need DataNode here to get localized name of the
                // shadow, but without the ugly "(->)" at the end
                DataNode dn = new DataNode(shadow, Children.LEAF);
                nodeName = dn.getDisplayName();
                obj = shadow.getOriginal();
                n = obj.getNodeDelegate();
            }

            if (obj == null) {
                obj = n.getCookie(DataObject.class);
            }
            if (obj != null) {
                if (obj.isTemplate()) {
                    // on normal nodes stop recursion
                    this.name = nodeName;
                    this.setChildren(Children.LEAF);
                    return;
                }

                if (acceptObj(obj)) {
                    // on folders use normal filtering
                    this.name = nodeName;
                    this.setChildren(new TemplateChildren(n));
                    return;
                }
            }
            Node p = getParentNode();
            if (p != null) {
                TemplateChildren ch = (TemplateChildren)p.getChildren();
                if (ch != null) {
                    ch.refreshKeyImpl(n);
                }
            }
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
            refresh();
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
            refresh();
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            refresh();
        }
    }

    private static class DefaultTemplateWizard extends TemplateWizard {
        DefaultTemplateWizard() {}
    }
    
    // delegate action
    // bugfix 36573, NewTemplateAction provides context aware action
    private static final Node[] EMPTY_NODE_ARRAY = new Node[0];
    
    private class NodeLookupListener implements LookupListener {
        
        public void resultChanged (org.openide.util.LookupEvent ev) {
            updateAction ();
        }
    }
    
    private void updateAction () {}
    
    static private final synchronized Node[] getNodesFromLookup (Lookup lookup) {
        if (lookup != null) {
            return lookup.lookupAll(Node.class).toArray(EMPTY_NODE_ARRAY);
        }
        return EMPTY_NODE_ARRAY;
    }
    
    
    /** Implements <code>ContextAwareAction</code> interface method. */
    public Action createContextAwareInstance (Lookup actionContext) {
        return new DelegateAction (this, actionContext);
    }
    
    private static final class DelegateAction extends Object
    implements Action, Presenter.Popup, /*Presenter.Menu, Presenter.Toolbar,*/ LookupListener {
        
        private NewTemplateAction delegate;
        private Lookup actionContext;
        private Lookup.Result nodesResult;
        
        private PropertyChangeSupport support = new PropertyChangeSupport (this);
        
        public DelegateAction (NewTemplateAction action, Lookup actionContext) {
            this.delegate = action;
            this.actionContext = actionContext;
            this.nodesResult = actionContext.lookupResult(Node.class);
            // if a weak listener is used then NewTemplateActionTest fails
            //LookupListener l = (LookupListener)WeakListeners.create (LookupListener.class, (LookupListener)action, nodesResult);
            //nodesResult.addLookupListener (l);
            //l.resultChanged (null);
            nodesResult.addLookupListener (this);
            resultChanged (null);
        }
        
        /** Overrides superclass method, adds delegate description. */
        public String toString () {
            return super.toString () + "[delegate=" + delegate + "]"; // NOI18N
        }
        
        public void putValue (String key, Object value) { }
        
        public boolean isEnabled () {
            return delegate.enable (getNodesFromLookup (actionContext));
        }
        
        public Object getValue (String key) {
            return delegate.getValue (key);
        }
        
        public void setEnabled (boolean b) {
        }
        
        public void actionPerformed (ActionEvent e) {
        }
        
        public void addPropertyChangeListener (PropertyChangeListener listener) {
            support.addPropertyChangeListener (listener);
        }
        
        public void removePropertyChangeListener (PropertyChangeListener listener) {
            support.removePropertyChangeListener (listener);
        }
        
        public JMenuItem getPopupPresenter() {
            return delegate.getPopupPresenter (actionContext, this);
        }

        public void resultChanged (org.openide.util.LookupEvent ev) {
            getPopupPresenter ();
//            getMenuPresenter ();
//            getToolbarPresenter ();
        }
        
//        public JMenuItem getMenuPresenter () {
//            return delegate.getMenuPresenter ();
//        }
//        
//        public Component getToolbarPresenter () {
//            return delegate.getToolbarPresenter ();
//        }
//        
    }
}
