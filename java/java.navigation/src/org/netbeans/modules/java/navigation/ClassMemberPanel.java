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

package org.netbeans.modules.java.navigation;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.lang.model.element.Element;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithToolbar;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType="text/x-java", position=100, displayName="#LBL_members"),
    @NavigatorPanel.Registration(mimeType="application/x-class-file", displayName="#LBL_members")
})
public class ClassMemberPanel implements NavigatorPanelWithUndo, NavigatorPanelWithToolbar {

    private static volatile ClassMemberPanel INSTANCE;   //Apparently not accessed in event dispatch thread in CaretListeningTask    
    private static final RequestProcessor RP = new RequestProcessor(ClassMemberPanel.class.getName(),1);
    //@GuardedBy("ClassMemberPanel.class")
    private static Reference<FileObject> lastFileRef = null;

    //@GuardedBy("this")
    private ClassMemberPanelUI component;
    
    public ClassMemberPanel() {
    }

    @Override
    public void panelActivated(final Lookup context) {
        assert context != null;
        INSTANCE = this;
        final ClassMemberPanelUI panel = getClassMemberPanelUI();
        RP.post( new Runnable () {
            @Override
            public void run () {
                ClassMemberNavigatorJavaSourceFactory f = ClassMemberNavigatorJavaSourceFactory.getInstance();
                if (f != null) {
                    f.setLookup(context, panel);
                    CaretListeningFactory.runAgain();
                }
            }
        });
    }

    @Override
    public void panelDeactivated() {
        final FileObject luf = getLastUsedFile();        
        getClassMemberPanelUI().clearNodes(false);
        if (JavadocTopComponent.exists()) {
            JavadocTopComponent.getDefault().clearContent(luf);
        }
        INSTANCE = null;
        //Even the setLookup(EMPTY) is fast, has to be called in RP to keep ordering
        RP.post( new Runnable () {
            @Override
            public void run () {
                ClassMemberNavigatorJavaSourceFactory f = ClassMemberNavigatorJavaSourceFactory.getInstance();
                if (f != null)
                    f.setLookup(Lookup.EMPTY, null);
            }
        });
    }

    @Override
    public Lookup getLookup() {
        return this.getClassMemberPanelUI().getLookup();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ClassMemberPanel.class,"LBL_members");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(ClassMemberPanel.class,"HINT_members");
    }

    @Override
    public JComponent getComponent() {
        return getClassMemberPanelUI();
    }

    void selectElement(ElementHandle<Element> eh) {
        getClassMemberPanelUI().selectNode(Pair.of(eh,null));
    }

    void selectTreePath(TreePathHandle tph) {
        getClassMemberPanelUI().selectNode(Pair.of(null,tph));
    }
    
    synchronized ClassMemberPanelUI getClassMemberPanelUI() {
        if (this.component == null) {
            this.component = new ClassMemberPanelUI();
        }
        return this.component;
    }
    
    public static ClassMemberPanel getInstance() {
        return INSTANCE;
    }

    @Override
    public UndoRedo getUndoRedo() {
        final UndoRedo undoRedo = Lookups.forPath("org/netbeans/modules/refactoring").lookup(UndoRedo.class);
        return undoRedo==null?UndoRedo.NONE:undoRedo;
    }

    @Override
    public synchronized JComponent getToolbarComponent() {
        return getClassMemberPanelUI().getToolbar();
    }

    static synchronized FileObject getLastUsedFile() {
        return lastFileRef == null ? null : lastFileRef.get();
    }

    static synchronized boolean compareAndSetLastUsedFile(@NullAllowed final FileObject file) {
        final FileObject lastFile = lastFileRef == null ? null : lastFileRef.get();
        final boolean res = file == null ? lastFile == null : file.equals(lastFile);
        lastFileRef = file == null ? null : new WeakReference<FileObject>(file);
        return res;
    }
}
