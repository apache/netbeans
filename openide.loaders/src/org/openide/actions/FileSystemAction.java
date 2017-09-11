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


import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import org.openide.awt.JInlineMenu;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.lookup.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Action that presents standard file system-related actions.
* Listens until a node representing a {@link DataObject}
* is selected and then retrieves {@link SystemAction}s from its
* {@link FileSystem}.
*
* @author  Jaroslav Tulach
*/
public class FileSystemAction extends SystemAction
implements ContextAwareAction, Presenter.Menu, Presenter.Popup {
    /** empty array of menu items */
    static JMenuItem[] NONE = new JMenuItem[] {};

    /** computes the nodes.
     */
    private static Node[] nodes (Lookup lookup) {
        Collection<? extends Node> c;

        if (lookup != null) {
            c = lookup.lookupAll(Node.class);
        } else {
            c = Collections.emptyList();
        }
        return c.toArray(new Node[c.size()]);
    }

    /** Creates menu for currently selected nodes.
    * @param popUp create popup or normal menu
    * @param n nodes to work with or null
    */
    static JMenuItem[] createMenu (boolean popUp, Lookup lookup) {
        Node[] n = nodes (lookup);
        
        if (n == null) {
            n = WindowManager.getDefault ().getRegistry ().getActivatedNodes ();
        }
        
        Map<FileSystem,Set<FileObject>> fsSet = new HashMap<FileSystem,Set<FileObject>>();
        List<DataObject> l = new LinkedList<DataObject>();
        if(n == null || n.length == 0) {
            l.addAll(lookup.lookupAll(DataObject.class));
        } else {
            for(Node node : n) {
                DataObject obj = node.getCookie(DataObject.class);
                if(obj != null) {
                    l.add(obj);
                }
            }
        }

        if (!l.isEmpty()) {
            for (DataObject obj : l) {
                 while (obj instanceof DataShadow)
                     obj = ((DataShadow) obj).getOriginal();
                 if (obj != null) {
                     try {
                         FileSystem fs = obj.getPrimaryFile ().getFileSystem ();
                         Set<FileObject> foSet = fsSet.get(fs);
                         if (foSet == null ) {
                             fsSet.put(fs, foSet = new LinkedHashSet<FileObject>());
                         }
                         foSet.addAll(obj.files ());
                     } catch (FileStateInvalidException ex) {continue;}
                 }  
            }
            /* At present not allowed to construct actions for selected nodes on more filesystems - its safe behaviour
             * If this restriction will be considered as right solution, then code of this method can be simplified
             */
            if (fsSet.size () == 0 || fsSet.size() > 1) {
                return createMenu(Enumerations.<Action>empty(), popUp, lookup);
            }
            
            List<Action> result = new LinkedList<Action>();
            Set<FileObject> backSet = new LinkedHashSet<FileObject>();
            for (Map.Entry<FileSystem,Set<FileObject>> entry : fsSet.entrySet()) {

                FileSystem fs = entry.getKey();
                Set<FileObject> foSet = entry.getValue();
                List<FileObject> backupList = new LinkedList<FileObject>(foSet);
                Iterator<FileObject> it = backupList.iterator ();
                while (it.hasNext ()) {
                    FileObject fo = it.next ();
                    try {
                        if (fo.getFileSystem () != fs) {
                            it.remove ();
                        }
                    } catch (FileStateInvalidException ex) {
                        it.remove ();
                    }
                }                
                backSet.addAll(backupList);
                result.addAll(fs.findExtrasFor(backSet).lookupAll(Action.class));
            }

            if (isManualRefresh()) {
                result.add(FileSystemRefreshAction.get(FileSystemRefreshAction.class));
            }
            
            return createMenu (Collections.enumeration (result), popUp, createProxyLookup(lookup, backSet)/*lookup*/);
        }
        return NONE;
    }
    private static boolean isManualRefresh() {
        return NbPreferences.root().node("org/openide/actions/FileSystemRefreshAction").getBoolean("manual", false); // NOI18N
    }

    private static ProxyLookup createProxyLookup(final Lookup lookup, final Set<FileObject> backSet) {
        return new ProxyLookup(lookup, Lookups.fixed((Object[])backSet.toArray(new FileObject [backSet.size()])));
    }

    /** Creates list of menu items that should be used for given
    * data object.
    * @param en enumeration of SystemAction that should be added
    *   into the menu if enabled and if not duplicated
    */
    static JMenuItem[] createMenu(Enumeration<? extends Action> en, boolean popUp, Lookup lookup) {
        en = Enumerations.removeDuplicates (en);

        List<JMenuItem> items = new ArrayList<JMenuItem>();
        while (en.hasMoreElements ()) {
            Action a = en.nextElement();
            
            // Retrieve context sensitive action instance if possible.
            if(lookup != null && a instanceof ContextAwareAction) {                
                a = ((ContextAwareAction)a).createContextAwareInstance(lookup);
            }
            
            boolean enabled = false;
            try {
                enabled = a.isEnabled();
            } catch (RuntimeException e) {
                Exceptions.attachMessage(e,
                                         "Guilty action: " +
                                         a.getClass().getName()); // NOI18N
                Exceptions.printStackTrace(e);
            }
            if (enabled) {
                JMenuItem item = null;
                if (popUp) {
                    if (a instanceof Presenter.Popup) {
                        item = ((Presenter.Popup)a).getPopupPresenter ();
                    }
                } else {
                    if (a instanceof Presenter.Menu) {
                        item = ((Presenter.Menu)a).getMenuPresenter ();
                    }
                }
                // test if we obtained the item
                if (item != null) {
                    items.add (item);
                }
            }
        }
        JMenuItem[] array = new JMenuItem [items.size ()];
        items.toArray (array);
        return array;
    }

    public JMenuItem getMenuPresenter () {
        return new Menu (false, null);
    }

    public JMenuItem getPopupPresenter () {
        return new Menu (true, null);
    }

    public String getName () {
        return NbBundle.getMessage(DataObject.class, "ACT_FileSystemAction");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (FileSystemAction.class);
    }

    /* Do nothing.
    * This action itself does nothing, it only presents other actions.
    * @param ev ignored
    */
    public void actionPerformed(ActionEvent e) {
        assert false : "ActionEvt: " + e;
    }
    
    /** Implements <code>ContextAwareAction</code> interface method. */
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(actionContext);
    }
    

    /** Presenter for this action.
    */
    private static class Menu extends JInlineMenu implements PropertyChangeListener {
        /** menu presenter (true) or popup presenter (false) */
        private boolean popup;
        /** last registered items */
        private JMenuItem[] last = NONE;
        /** context for actions or null */
        private Lookup lookup;

        static final long serialVersionUID =2650151487189209766L;

        /** Creates new instance for menu/popup presenter.
        * @param popup true if this should represent popup
        * @param arr nodes to work with or null if global one should be used
        */
        Menu (boolean popup, Lookup lookup) {
            this.popup = popup;
            this.lookup = lookup;
            
            changeMenuItems (createMenu (popup, lookup));

            if (lookup == null) {
                // listen only when nodes not provided
                TopComponent.Registry r = WindowManager.getDefault ().getRegistry ();

                r.addPropertyChangeListener (
                    WeakListeners.propertyChange (this, r)
                );
            }
        }

        /** Changes the selection to new items.
        * @param items the new items
        */
        synchronized void changeMenuItems (JMenuItem[] items) {
            removeListeners (last);
            addListeners (items);
            last = items;
            setMenuItems (items);
        }


        /** Add listeners to menu items.
        * @param items the items
        */
        private void addListeners (JMenuItem[] items) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                items[i].addPropertyChangeListener (this);
            }
        }

        /** Remove all listeners from menu items.
        * @param items the items
        */
        private void removeListeners (JMenuItem[] items) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                items[i].removePropertyChangeListener (this);
            }
        }
        
        boolean needsChange = false;
        
        public void addNotify() {
            if (needsChange) {
                changeMenuItems (createMenu (popup, lookup));
                needsChange = false;
            }
            super.addNotify();
        }

        public void removeNotify() {
            removeListeners (last);
            last = NONE;
        }

        public void propertyChange (PropertyChangeEvent ev) {
            String name = ev.getPropertyName ();
            if (
                name == null ||
                name.equals (SystemAction.PROP_ENABLED) ||
                name.equals (TopComponent.Registry.PROP_ACTIVATED_NODES)
            ) {
                // change items later
                needsChange = true;
            }
        }
    }
    
    /** Context aware action implementation. */
    private static final class DelegateAction extends AbstractAction 
    implements Presenter.Menu, Presenter.Popup {
        /** lookup to work with */
        private Lookup lookup;

        public DelegateAction(Lookup lookup) {
            this.lookup = lookup;
        }


        /** @return menu presenter.  */
        public JMenuItem getMenuPresenter () {
            return new FileSystemAction.Menu (false, lookup);
        }

        /** @return popup presenter.  */
        public JMenuItem getPopupPresenter () {
            return new FileSystemAction.Menu (true, lookup);
        }
        
        public void actionPerformed(ActionEvent e) {
            assert false : e;
        }
        
    } // end of DelegateAction
    
}
