/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 */
abstract class BaseMakeViewChildren extends Children.Keys<Object>
        implements ChangeListener, RefreshableItemsContainer {

    private final RequestProcessor.Task refreshKeysTask;
    private final KeyUpdater keyUpdater;
    static final int WAIT_DELAY = 50;

    private Folder folder;
    protected final MakeLogicalViewProvider provider;
    private final Object lock = new Object();

    public BaseMakeViewChildren(Folder folder, MakeLogicalViewProvider provider) {
        this.folder = folder;
        this.provider = provider;
        keyUpdater = new KeyUpdater();
        this.refreshKeysTask = provider.getAnnotationRP().create(keyUpdater, true);
    }

    protected final Project getProject() {
        return provider.getProject();
    }

    protected boolean isRoot() {
        return false;
    }

    protected void onFolderChange(Folder folder) {
    }

    @Override
    protected void addNotify() {
        if (this.provider.isFindPathMode()) {
            //System.err.println("BaseMakeViewChildren: FindPathMode " + (SwingUtilities.isEventDispatchThread() ? "UI":"regular") + " thread");
            // no wait node for direct search
            super.addNotify();
            getAddNotifyRunnable().run();
        } else {
            //System.err.println("BaseMakeViewChildren: create wait node " + (SwingUtilities.isEventDispatchThread() ? "UI":"regular") + " thread");
            if (SwingUtilities.isEventDispatchThread()) {
                super.addNotify();
                resetKeys(Collections.singleton(getWaitNode()));
                provider.getAnnotationRP().post(getAddNotifyRunnable(), WAIT_DELAY);
            } else {
                SwingUtilities.invokeLater(() -> {
                    addNotify();
                });
            }
        }
    }

    private void resetKeys(Collection<?> keysSet) {
        synchronized(lock) {
            setKeys(keysSet);
        }
    }
    
    private Runnable getAddNotifyRunnable() {
        return () -> {
            if (isRoot() && folder == null) {
                MakeConfigurationDescriptor mcd = provider.getMakeConfigurationDescriptor();
                if (mcd != null) {
                    folder = mcd.getLogicalFolders();
                    onFolderChange(folder);
                }
            }
            if (folder != null) { // normally it shouldn't happen, but might happen if the project metadata is broken
                folder.addChangeListener(BaseMakeViewChildren.this);
                if (getProject().getProjectDirectory() != null && getProject().getProjectDirectory().isValid()) {
                    resetKeys(getKeys(null));
                }
            }
        };
    }

    private Node getWaitNode() {
        return new LoadingNode();
    }


    @Override
    public void refreshItem(Item item) {
        refreshKey(item);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void removeNotify() {
        refreshKeysTask.cancel();
        keyUpdater.cancel();
        resetKeys(Collections.EMPTY_SET);
        if (folder != null) {
            folder.removeChangeListener(this);
        }
        super.removeNotify();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof Item) {
            // update single item (it may be broken)
            Item[] items = getFolder().getItemsAsArray();
            for (final Item item : items) {
                if (e.getSource() == item) {
                    // refreshItem() acquires Children.MUTEX; make sure
                    // it's not under ProjectManager.mutex() (IZ#175996)
                    Runnable todo = () -> {
                        refreshItem(item);
                    };
                    provider.getAnnotationRP().post(todo);
                    break;
                }
            }
        } else {
            // update folder. Items may have been added or deleted
            refreshKeysTask.schedule(WAIT_DELAY);
        }
    }

    abstract protected Collection<Object> getKeys(AtomicBoolean canceled);

    public Folder getFolder() {
        return folder;
    }

    private class KeyUpdater implements Runnable {
        private AtomicBoolean canceled = new AtomicBoolean(false);
        
        private KeyUpdater() {
        }

        private synchronized void cancel() {
            canceled.set(true);
        }
        
        @Override
        public void run() {
            synchronized(this) {
                canceled = new AtomicBoolean(false);
            }
            //System.err.println("resetKeys on " + getFolder());
            Collection<Object> keys = getKeys(canceled);
            if (!canceled.get()) {
                resetKeys(keys);
            }
        }
    }
}
