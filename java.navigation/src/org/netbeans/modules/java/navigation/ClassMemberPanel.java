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
