/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.execution;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author akrasny
 */
public final class IOTabsController {

    private static final IOTabsController instance = new IOTabsController();
    private static final TabsGroupGroupsComparator comparator = new TabsGroupGroupsComparator();
    private final List<TabsGroup> groups = new ArrayList<>();

    public static IOTabsController getDefault() {
        return instance;
    }

    public TabsGroup openTabsGroup(final String groupName, final boolean reuse) {
        synchronized (groups) {
            List<TabsGroup> toRemove = new ArrayList<>();

            // Cleanup obsolete groups
            for (TabsGroup group : groups) {
                if (group.canForget()) {
                    toRemove.add(group);
                }
            }

            for (TabsGroup group : toRemove) {
                groups.remove(group);
            }

            if (reuse) {
                TabsGroup toReuse = null;
                for (TabsGroup group : groups) {
                    if (group.groupName.equals(groupName)) {
                        if (!group.isLocked()) {
                            toReuse = group;
                            break;
                        }
                    }
                }
                if (toReuse != null) {
                    toReuse.closeAll();
                    groups.remove(toReuse);
                }
            }

            int idx = 1;
            for (TabsGroup group : groups) {
                if (!group.groupName.equals(groupName)) {
                    continue;
                }
                if (idx >= group.seqID) {
                    idx++;
                }
            }
            TabsGroup result = new TabsGroup(groupName, idx);
            groups.add(result);
            Collections.sort(groups, comparator);
            return result;
        }
    }

    public static InputOutput getInputOutput(final InputOutputTab ioTab) {
        return ioTab.inputOutputRef.get();
    }

    public static final class TabsGroup {

        private final List<InputOutputTab> tabs = new ArrayList<>();
        private final AtomicBoolean locked = new AtomicBoolean(false);
        private final String groupName;
        private final int seqID;

        private TabsGroup(final String groupName, final int seqID) {
            this.seqID = seqID;
            this.groupName = groupName;
        }

        private boolean isLocked() {
            return locked.get();
        }

        public InputOutputTab getTab(final String tabName, final IOTabFactory factory) {
            String name = seqID == 1 ? tabName : tabName.concat(" #" + seqID); // NOI18N
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    if (tab.name.equals(name)) {
                        return tab;
                    }
                }
                final InputOutput inputOutput = factory.createNewTab(name);
                // Latent bug! inputOutput is stored in weak reference.
                // So caller should keep get and inputOutput in local variable just after getting tab.
                InputOutputTab newTab = new InputOutputTab(name, inputOutput);
                tabs.add(newTab);
                return newTab;
            }
        }

        private void closeAll() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    tab.closeTab();
                }
                tabs.clear();
            }
        }

        public void lockAndReset() {
            if (!locked.compareAndSet(false, true)) {
                throw new IllegalStateException("Already locked"); // NOI18N
            }

            synchronized (tabs) {
                Mutex.EVENT.writeAccess(new Action<Void>() {

                    @Override
                    public Void run() {
                        for (InputOutputTab tab : tabs) {
                            tab.resetIO();
                            // In case of NbIO this call to closeOutput() will
                            // not change a title to a plain (not bold) font ...
                            // For this we need to write at least one char to 
                            // the stream (and do this from another thread).
                            // But will just ignore this minor problem, as
                            // this method is invoked right before performing
                            // output... 
                            // Still do closeOutput() because this will make 
                            // terminal's title not bold.
                            // Closing output doesn't prevent from further 
                            // writing 
                            tab.closeOutput();
                        }
                        return null;
                    }
                });
            }
        }

        public void unlockAndCloseOutput() {
            synchronized (tabs) {
                if (!locked.get()) {
                    throw new IllegalStateException("Not locked: " + toString()); // NOI18N
                }

                Mutex.EVENT.writeAccess(new Action<Void>() {

                    @Override
                    public Void run() {
                        for (InputOutputTab tab : tabs) {
                            tab.closeOutput();
                        }
                        return null;
                    }
                });

                locked.set(false);
            }
        }

        private boolean canForget() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    if (tab.inputOutputRef.get() != null) {
                        return false;
                    }
                }
                tabs.clear();
                return true;
            }
        }

        @Override
        public String toString() {
            return "Group of IO tabs named " + groupName; // NOI18N
        }
    }

    public static final class InputOutputTab {

        private final String name;
        private final WeakReference<InputOutput> inputOutputRef;

        private InputOutputTab(final String name, final InputOutput inputOutput) {
            this.name = name;
            inputOutputRef = new WeakReference<>(inputOutput);
        }

        public String getName() {
            return name;
        }

        public void select() {
            Mutex.EVENT.postWriteRequest(new Runnable() {

                @Override
                public void run() {
                    InputOutput io = inputOutputRef.get();
                    if (io != null) {
                        io.select();
                    }
                }
            });
        }

        private OutputWriter getOutputWriter() {
            InputOutput io = inputOutputRef.get();
            return (io == null) ? null : io.getOut();
        }

        public void closeOutput() {
            Mutex.EVENT.postWriteRequest(new Runnable() {

                @Override
                public void run() {
                    OutputWriter outputWriter = getOutputWriter();
                    if (outputWriter != null) {
                        outputWriter.close();
                    }
                }
            });
        }

        private void closeTab() {
            final InputOutput io = inputOutputRef.get();
            if (io != null) {
                Mutex.EVENT.postWriteRequest(new Runnable() {

                    @Override
                    public void run() {
                        io.closeInputOutput();
                    }
                });
            }
        }

        private void resetIO() {
            Mutex.EVENT.postWriteRequest(new Runnable() {

                @Override
                public void run() {
                    OutputWriter outputWriter = getOutputWriter();
                    if (outputWriter != null) {
                        try {
                            outputWriter.reset();
                        } catch (IOException ex) {
                        }
                    }
                }
            });
        }
    }

    private static final class TabsGroupGroupsComparator implements Comparator<TabsGroup> {

        @Override
        public int compare(final TabsGroup o1, final TabsGroup o2) {
            return o1.seqID - o2.seqID;
        }
    }

    public interface IOTabFactory {

        InputOutput createNewTab(String tabName);
    }
}
