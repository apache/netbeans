/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jshell.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.modules.jshell.support.PersistentSnippets;
import org.netbeans.modules.jshell.support.PersistentSnippetsSupport;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "LBL_RunSavedScript=Execute saved script",
    "DESC_RunSavedScript=Execute or save a script"
})
//@EditorActionRegistration(
//        mimeType = "text/x-repl",
//        toolBarPosition = 20015,
//        name = "jshell-select-run-saved-snippets",
//        iconResource = "org/netbeans/modules/editor/resources/last_edit_location_16.png",
//        shortDescription = "#LBL_RunSavedScript",
//        popupPath = "",
//        popupPosition = 20015
//)
public class RunScriptAction extends TextAction 
        implements ContextAwareAction, Presenter.Toolbar, PopupMenuListener {
    public static final String ID = "jshell-select-run-saved-snippets"; // NOI18N
    
    private static final String RESOURCE_ICON = "org/netbeans/modules/jshell/resources/run-scripts.png"; // NOI18N
    
    /**
     * The file which contains snippet source(s).
     */
    private FileObject          scriptFile;
    private Lookup              context;
    private JTextComponent      editor;
    private PersistentSnippets  storage;
    
    private JPopupMenu      popup;
    private JMenu           subMenu;
    private boolean         updatePopup;
    private L               listen = new L();
    private Object          weakListen;
    
    public RunScriptAction() {
        super(ID);
    }
    
    private RunScriptAction(Lookup context, JTextComponent editor, FileObject scriptOrFolder, 
            String name, String description) {
        this();
        this.context = context;
        this.editor = editor;
        this.storage = PersistentSnippetsSupport.create(context);
        this.scriptFile = scriptOrFolder;
        putValue(NAME, name);
        if (scriptFile == null) {
            putValue(SHORT_DESCRIPTION, Bundle.DESC_RunSavedScript());
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(RESOURCE_ICON, false));
        } else {
            putValue(SHORT_DESCRIPTION, description);
        }
        
        if (storage == null) {
            setEnabled(false);
            return;
        }
        
        boolean enabled = true;
        
        
        if (scriptOrFolder == null || scriptOrFolder.isFolder()) {
            if (scriptOrFolder == null) {
                popup = new JPopupMenu() {
                    @Override
                    public int getComponentCount() {
                        refresh();
                        return super.getComponentCount();
                    }
                };
            } else {
                subMenu = new JMenu() {
                    @Override
                    public int getMenuComponentCount() {
                        refresh();
                        return super.getMenuComponentCount();
                    }
                };
                subMenu.setText(name);
                subMenu.setToolTipText(description);
            }
            if (storage.getSavedClasses(null).isEmpty()) {
                setEnabled(false);
            }
            invalidate();
            if (scriptOrFolder != null) {
                FileChangeListener fcl = WeakListeners.create(FileChangeListener.class, listen, scriptOrFolder);
                scriptOrFolder.addFileChangeListener(fcl);
                weakListen = fcl;
            } else {
                ChangeListener cl = WeakListeners.change(listen, storage);
                storage.addChangeListener(cl);
                weakListen = cl;
            }
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        refresh();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        synchronized (this) {
            updatePopup = true;
        }
    }
    
    
    
    private void invalidate() {
        synchronized (this) {
            updatePopup = true;
        }
        if (scriptFile != null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refresh);
        } else {
            refresh();
        }
    }
    
    class L extends FileChangeAdapter implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            invalidate();
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            invalidate();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            invalidate();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidate();
        }
        
    }
    
    private void refresh() {
        synchronized (this) {
            if ((subMenu == null && popup == null) || !updatePopup) {
                return;
            }
            updatePopup = false;
        }
        if (scriptFile != null && scriptFile.isData()) {
            return;
        }
        Collection<FileObject> scripts = new ArrayList<>();
        if (scriptFile == null) {
            scripts = storage.getSavedClasses(null);
        } else {
            scripts.addAll(Arrays.asList(scriptFile.getChildren()));
        }
        for (Component comp : (popup != null ? popup : subMenu).getComponents()) {
            if (comp instanceof JMenuItem) {
                Action a = ((JMenuItem)comp).getAction();
                if (a instanceof RunScriptAction) {
                    ((RunScriptAction)a).dispose();
                }
            }
            JComponent c = (popup != null ? popup : subMenu);
            c.remove(comp);
        }
        boolean nonEmpty = false;
        for (FileObject f : scripts) {
            String name = f.getName();
            String desc = storage.getDescription(f);
            if (f.isData()) {
                RunScriptAction ra = new RunScriptAction(context, editor, f, name, desc);
                if (popup != null) {
                    popup.add(ra);
                } else {
                    subMenu.add(ra);
                }
                nonEmpty = true;
            } else if (f.getChildren().length > 0 && !f.getPath().endsWith("startup")) { // FIXME: HACK
                RunScriptAction ra = new RunScriptAction(context, editor, f, name, desc);
                JMenu subMenu = ra.subMenu;
                subMenu.setLabel(name);
                subMenu.setToolTipText(desc);
                if (popup != null) {
                    popup.add(subMenu);
                } else {
                    subMenu.add(subMenu);
                }
                nonEmpty = true;
            }
        }
        setEnabled(nonEmpty);
    }
    
    void dispose() {
        if (scriptFile != null) {
            
        }
        if (storage != null && scriptFile == null) {
            // unregister the root
            storage.removeChangeListener((ChangeListener)weakListen);
        } else if (scriptFile != null && scriptFile.isFolder()) {
            scriptFile.removeFileChangeListener((FileChangeListener)weakListen);
        }
    }

    @NbBundle.Messages({
        "# 0 - message from the script execution",
        "ERR_ExecutingScript=Error executing saved snippets: {0}"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        if (context == null || editor == null || scriptFile == null) {
            return;
        }
        FileObject consoleFile = context.lookup(FileObject.class);
        if (consoleFile == null) {
            return;
        }
        ShellSession s = ShellSession.get(editor.getDocument());
        if (s == null) {
            return;
        }
        try {
            runScript(scriptFile, s);
        } catch (IOException ex) {
            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(Bundle.ERR_ExecutingScript(ex.getLocalizedMessage()), NotifyDescriptor.ERROR_MESSAGE);
            org.openide.DialogDisplayer.getDefault().notify(msg);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent c = findComponent(actionContext);
        return new RunScriptAction(actionContext, c, null, null, null);
    }

    @Override
    public Component getToolbarPresenter() {
        if (popup != null) {
            JButton button = DropDownButtonFactory.createDropDownButton(
                (ImageIcon) getValue(SMALL_ICON), 
                popup
            );
            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            button.setAction(this);
            return button;
        } else {
            return new JButton(this);
        }
    }
    
    static JTextComponent findComponent(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null) {
            JEditorPane panes[] = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                return panes[0];
            }
        }
        return lookup.lookup(JTextComponent.class);
    }
    
    @NbBundle.Messages({
        "LBL_SaveSnippets=Save current snippets..."
    })
    static class SaveScriptAction extends TextAction  {
        private final JTextComponent     comp;
        private final Lookup             context;
        private final PersistentSnippets storage;

        public SaveScriptAction(JTextComponent comp, Lookup context, PersistentSnippets storage) {
            super(Bundle.LBL_SaveSnippets());
            this.comp = comp;
            this.context = context;
            this.storage = storage;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    
    @Deprecated
    private void runScript(FileObject scriptFile, ShellSession session) throws IOException {
        PersistentSnippetsSupport.runScript(scriptFile, session, true);
    }
}
