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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.JMenuItem;
import org.netbeans.modules.xml.util.Util;
import org.openide.awt.JInlineMenu;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.Lookup;

public abstract class CollectSystemAction extends SystemAction implements Presenter.Popup {
    /** Serial Version UID */
    private static final long serialVersionUID = 6517322512481423122L;

    /** All Actions Lookup Result. */
    private Lookup.Result allActionsResult;

    /** empty array of menu items */
    static JMenuItem[] NONE = new JMenuItem[] {};

    protected final List registeredAction = new ArrayList();

    /** Which Class should be used for Lookup? */
    protected abstract Class getActionLookClass ();

    /** @return all instances of <code>getActionLookClass</code>.
     */
    protected synchronized Collection getPossibleActions () {
        if (allActionsResult == null) {
            allActionsResult = Lookup.getDefault().lookup(new Lookup.Template (
                getActionLookClass()));
            addRegisteredAction();
        }
        return registeredAction;
    }

    abstract protected void addRegisteredAction();

    protected void addRegisteredAction(String folderPath) {
        List<String> actionClassNames = new ArrayList<String>();
        for (Object obj : registeredAction) {
            if (obj == null) continue;
            actionClassNames.add(obj.getClass().getName());
        }
        addRegisteredAction(allActionsResult, actionClassNames);

        addActionFromFolder(folderPath, actionClassNames);
    }

    private void addActionFromFolder(String folderPath,
        List<String> registeredActionClassNames) {
        FileObject layerRoot = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject xmlActionsFileObj = layerRoot.getFileObject(folderPath);
        DataFolder xmlActionsFolder = DataFolder.findFolder(xmlActionsFileObj);
        FolderLookup folderLookup = new FolderLookup(xmlActionsFolder);
        Lookup.Result xmlActionsResult = folderLookup.getLookup().lookup(
            new Lookup.Template (getActionLookClass()));

        addRegisteredAction(xmlActionsResult, registeredActionClassNames);
    }

    private void addRegisteredAction(Lookup.Result xmlActionsResult,
        List<String> registeredActionClassNames) {
        if (xmlActionsResult == null) return;
        synchronized (registeredAction) {
            Collection lookupActions = xmlActionsResult.allInstances();
            Iterator it = lookupActions.iterator();
            while (it.hasNext()) {
                Object lookupAction = it.next();
                String lookupActionClassName = lookupAction.getClass().getName();
                if (! registeredActionClassNames.contains(lookupActionClassName)) {
                    registeredAction.add(lookupAction);
                }
            }
        }
    }

    private JMenuItem[] createMenu () {
        JMenuItem[] menu;

        menu = createMenu (getPossibleActions());

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (
            "--- CollectSystemAction.createMenu: menu = " + menu);//, new RuntimeException());

        return menu;
    }

    private JMenuItem[] createMenu (Collection coll) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (
            "\n--> CollectSystemAction.createMenu: ( " + coll + " )");

        ArrayList items = new ArrayList ();

        Iterator it = coll.iterator();
        while (it.hasNext ()) {
            SystemAction a = (SystemAction) it.next();
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (
                "-*- CollectSystemAction.createMenu: next action " + a +
                             " -- " + ( a.isEnabled() ? "<enabled>" : "[disabled]" ) );
            
            if ( a.isEnabled() ) {
                JMenuItem item = null;
                if (a instanceof Presenter.Popup) {
                    item = ((Presenter.Popup)a).getPopupPresenter ();
                }

                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug
                    ("-*- CollectSystemAction.createMenu: menu item = " + item);

                // test if we obtained the item
                if (item != null) {
                    items.add (item);
                }
            }
        }

        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug
            ("<-- CollectSystemAction.createMenu: all items = " + items + "\n");

        JMenuItem[] array = new JMenuItem [items.size ()];
        items.toArray (array);
        return array;
    }


    /* @return popup presenter.
     */
    @Override
    public JMenuItem getPopupPresenter () {
        return new Menu();
    }

    /* Do nothing.
    * This action itself does nothing, it only presents other actions.
    * @param ev ignored
    */
    @Override
    public void actionPerformed (java.awt.event.ActionEvent e) {
    }



    /** Presenter for this action.
    */
    private class Menu extends JInlineMenu {
        private static final long serialVersionUID = -4962039848190160129L;

        /** last registered items */
        private JMenuItem[] last = NONE;
        /** own property change listner */
        private PropL propL = new PropL ();


        /**
         */
        Menu () {
            changeMenuItems (createMenu());

            Registry r = WindowManager.getDefault().getRegistry ();

            r.addPropertyChangeListener (
                org.openide.util.WeakListeners.propertyChange (propL, r)
            );
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
                items[i].addPropertyChangeListener (propL);
            }
        }

        /** Remove all listeners from menu items.
        * @param items the items
        */
        private void removeListeners (JMenuItem[] items) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                items[i].removePropertyChangeListener (propL);
            }
        }
        
        boolean needsChange = false;        

        @Override
        public void addNotify() {
            if (needsChange) {
                changeMenuItems (createMenu());
                needsChange = false;
            }
            super.addNotify();
        }

        @Override
        public void removeNotify() {
            removeListeners (last);
            last = NONE;
        }


        /** Property listnener to watch changes of enable state.
        */
        private class PropL implements PropertyChangeListener {
            @Override
            public void propertyChange (PropertyChangeEvent ev) {
                String name = ev.getPropertyName ();
                if (
                    name == null ||
                    name.equals (SystemAction.PROP_ENABLED) ||
                    name.equals (Registry.PROP_ACTIVATED_NODES)
                    ) {
                    // change items later
                    needsChange = true;
                }
            }
        }
    } // end: class Menu
}