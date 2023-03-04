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

package org.netbeans.modules.extexecution;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.spi.extexecution.open.OptionOpenHandler;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public final class InputOutputManager {

    private static final Logger LOGGER = Logger.getLogger(InputOutputManager.class.getName());

    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     * @see "#43001"
     */
    private static final Map<InputOutput, InputOutputData> AVAILABLE =
            new WeakHashMap<InputOutput, InputOutputData>();

    private static final Set<String> ACTIVE_DISPLAY_NAMES = new HashSet<String>();

    private InputOutputManager() {
        super();
    }

    public static void addInputOutput(InputOutputData data) {
        synchronized (InputOutputManager.class) {
            AVAILABLE.put(data.inputOutput, data);
            ACTIVE_DISPLAY_NAMES.remove(data.displayName);
        }
    }

    /**
     * Tries to find free Output Window tab for the given name.
     *
     * @param name the name of the free tab. Other free tabs are ignored.
     * @return free tab and its current display name or <tt>null</tt>
     */
    public static InputOutputData getInputOutput(String name, boolean actions, String optionsPath) {
        InputOutputData result = null;

        TreeSet<InputOutputData> candidates = new TreeSet<InputOutputData>(InputOutputData.DISPLAY_NAME_COMPARATOR);

        synchronized (InputOutputManager.class) {
            for (Iterator<Entry<InputOutput, InputOutputData>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, InputOutputData> entry = it.next();

                final InputOutput free = entry.getKey();
                final InputOutputData data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (isAppropriateName(name, data.displayName)) {
                    if ((actions && data.rerunAction != null && data.stopAction != null)
                            || !actions && data.rerunAction == null && data.stopAction == null) {
                        if (optionsPath != null && data.optionsAction != null && data.optionsAction.getOptionsPath().equals(optionsPath)
                                || optionsPath == null && data.optionsAction == null) {
                            // Reuse it.
                            candidates.add(data);
                        }
                    } // continue to remove all closed tabs
                }

                LOGGER.log(Level.FINEST, "InputOutputManager pool: {0}", data.getDisplayName());
            }

            if (!candidates.isEmpty()) {
                result = candidates.first();
                AVAILABLE.remove(result.inputOutput);
                ACTIVE_DISPLAY_NAMES.add(result.displayName);
            }
        }
        return result;
    }

    public static InputOutputData getInputOutput(InputOutput inputOutput) {
        InputOutputData result = null;

        synchronized (InputOutputManager.class) {
            for (Iterator<Entry<InputOutput, InputOutputData>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, InputOutputData> entry = it.next();

                final InputOutput free = entry.getKey();
                final InputOutputData data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (free.equals(inputOutput)) {
                    result = data;
                    ACTIVE_DISPLAY_NAMES.add(result.displayName);
                    it.remove();
                }
                LOGGER.log(Level.FINEST, "InputOutputManager pool: {0}", data.getDisplayName());
            }
        }
        return result;
    }

    public static InputOutputData createInputOutput(String originalDisplayName,
            boolean controlActions, String optionsPath) {

        synchronized (InputOutputManager.class) {
            String displayName = getNonActiveDisplayName(originalDisplayName);

            InputOutput io;
            StopAction stopAction = null;
            RerunAction rerunAction = null;
            OptionsAction optionsAction = null;

            if (controlActions) {
                stopAction = new StopAction();
                rerunAction = new RerunAction();
                if (optionsPath != null) {
                    OptionOpenHandler handler = Lookup.getDefault().lookup(OptionOpenHandler.class);
                    if (handler != null) {
                        optionsAction = new OptionsAction(handler, optionsPath);
                        io = IOProvider.getDefault().getIO(displayName,
                                new Action[] {rerunAction, stopAction, optionsAction});
                    } else {
                        LOGGER.log(Level.WARNING, "No available OptionsOpenHandler so no Options button");
                        io = IOProvider.getDefault().getIO(displayName,
                            new Action[] {rerunAction, stopAction});
                    }
                } else {
                    io = IOProvider.getDefault().getIO(displayName,
                            new Action[] {rerunAction, stopAction});
                }
                rerunAction.setParent(io);
            } else {
                if (optionsPath != null) {
                    OptionOpenHandler handler = Lookup.getDefault().lookup(OptionOpenHandler.class);
                    if (handler != null) {
                        optionsAction = new OptionsAction(handler, optionsPath);
                        io = IOProvider.getDefault().getIO(displayName,
                                new Action[] {optionsAction});
                    } else {
                        LOGGER.log(Level.WARNING, "No available OptionsOpenHandler so no Options button");
                        io = IOProvider.getDefault().getIO(displayName, true);
                    }
                } else {
                    io = IOProvider.getDefault().getIO(displayName, true);
                }
            }

            ACTIVE_DISPLAY_NAMES.add(displayName);
            return new InputOutputData(io, displayName, stopAction, rerunAction, optionsAction);
        }
    }

    // unit test only
    public static void clear() {
        synchronized (InputOutputManager.class) {
            AVAILABLE.clear();
            ACTIVE_DISPLAY_NAMES.clear();
        }
    }

    private static boolean isAppropriateName(String base, String toMatch) {
        if (!toMatch.startsWith(base)) {
            return false;
        }
        return toMatch.substring(base.length()).matches("^(\\ #[0-9]+)?$"); // NOI18N
    }

    private static String getNonActiveDisplayName(String displayNameBase) {
        String nonActiveDN = displayNameBase;
        if (ACTIVE_DISPLAY_NAMES.contains(nonActiveDN)) {
            // Uniquify: "prj (targ) #2", "prj (targ) #3", etc.
            int i = 2;
            String testdn;

            do {
                testdn = NbBundle.getMessage(InputOutputManager.class, "Uniquified", nonActiveDN, i++);
            } while (ACTIVE_DISPLAY_NAMES.contains(testdn));

            nonActiveDN = testdn;
        }
        assert !ACTIVE_DISPLAY_NAMES.contains(nonActiveDN);
        return nonActiveDN;
    }

    public static final class InputOutputData {

        private static final Comparator<InputOutputData> DISPLAY_NAME_COMPARATOR = new Comparator<InputOutputData>() {

            @Override
            public int compare(InputOutputData o1, InputOutputData o2) {
                return o1.displayName.compareTo(o2.displayName);
            }
        };

        private final InputOutput inputOutput;

        private final String displayName;

        private final StopAction stopAction;

        private final RerunAction rerunAction;

        private final OptionsAction optionsAction;

        public InputOutputData(InputOutput inputOutput, String displayName,
                StopAction stopAction, RerunAction rerunAction, OptionsAction optionsAction) {
            this.displayName = displayName;
            this.stopAction = stopAction;
            this.rerunAction = rerunAction;
            this.inputOutput = inputOutput;
            this.optionsAction = optionsAction;
        }

        public InputOutput getInputOutput() {
            return inputOutput;
        }

        public String getDisplayName() {
            return displayName;
        }

        public RerunAction getRerunAction() {
            return rerunAction;
        }

        public StopAction getStopAction() {
            return stopAction;
        }

        public OptionsAction getOptionsAction() {
            return optionsAction;
        }
    }
}
