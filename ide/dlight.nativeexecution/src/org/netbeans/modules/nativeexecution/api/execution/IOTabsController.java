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
package org.netbeans.modules.nativeexecution.api.execution;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
            groups.sort(comparator);
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
