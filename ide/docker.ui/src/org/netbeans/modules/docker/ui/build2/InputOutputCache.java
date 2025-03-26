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

package org.netbeans.modules.docker.ui.build2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public final class InputOutputCache {

    private static final Logger LOGGER = Logger.getLogger(InputOutputCache.class.getName());

    private static final Map<InputOutput, CachedInputOutput> AVAILABLE = new WeakHashMap<>();

    private static final Set<String> ACTIVE_DISPLAY_NAMES = new HashSet<String>();

    private InputOutputCache() {
        super();
    }

    public static CachedInputOutput get(String name, List<Action> actions) {
        synchronized (CachedInputOutput.class) {
            CachedInputOutput result = getInputOutput(name, actions);
            if (result == null) {
                result = createInputOutput(name, actions);
            }
            return result;
        }
    }

    public static CachedInputOutput get(InputOutput inputOutput) {
        CachedInputOutput result = null;

        synchronized (InputOutputCache.class) {
            for (Iterator<Entry<InputOutput, CachedInputOutput>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, CachedInputOutput> entry = it.next();

                final InputOutput free = entry.getKey();
                final CachedInputOutput data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (free.equals(inputOutput)) {
                    result = data;
                    ACTIVE_DISPLAY_NAMES.add(result.getDisplayName());
                    it.remove();
                }
                LOGGER.log(Level.FINEST, "Pooled: {0}", data.getDisplayName());
            }
        }
        return result;
    }

    public static void release(CachedInputOutput data) {
        synchronized (InputOutputCache.class) {
            InputOutput io = data.getInputOutput();
            if (io != null) {
                AVAILABLE.put(io, data);
                ACTIVE_DISPLAY_NAMES.remove(data.getDisplayName());
            }
        }
    }

    private static CachedInputOutput getInputOutput(String name, List<Action> actions) {
        CachedInputOutput result = null;

        TreeSet<CachedInputOutput> candidates = new TreeSet<>(CachedInputOutput.DISPLAY_NAME_COMPARATOR);

        synchronized (InputOutputCache.class) {
            for (Iterator<Entry<InputOutput, CachedInputOutput>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, CachedInputOutput> entry = it.next();

                final InputOutput free = entry.getKey();
                final CachedInputOutput data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (isAppropriateName(name, data.getDisplayName())) {
                    List<Action> candidateActions = data.getActions();
                    if (candidateActions.isEmpty() && (actions == null || actions.isEmpty())) {
                        candidates.add(data);
                    } else if (actions != null && candidateActions.size() == actions.size()) {
                        boolean differs = false;
                        for (int i = 0; i < candidateActions.size(); i++) {
                            if (candidateActions.get(i).getClass() != actions.get(i).getClass()) {
                                differs = true;
                                break;
                            }
                        }
                        if (!differs) {
                            candidates.add(data);
                        }
                    } // continue to remove all closed tabs
                }

                LOGGER.log(Level.FINEST, "Pooled: {0}", data.getDisplayName());
            }

            if (!candidates.isEmpty()) {
                result = candidates.first();
                AVAILABLE.remove(result.getInputOutput());
                ACTIVE_DISPLAY_NAMES.add(result.getDisplayName());
            }
        }
        return result;
    }

    private static CachedInputOutput createInputOutput(String originalDisplayName,
            List<Action> actions) {

        synchronized (InputOutputCache.class) {
            String displayName = getNonActiveDisplayName(originalDisplayName);

            InputOutput io;

            if (actions != null && !actions.isEmpty()) {
                io = IOProvider.getDefault().getIO(displayName,
                        actions.toArray(new Action[0]));

                //rerunAction.setParent(io);
            } else {
                io = IOProvider.getDefault().getIO(displayName, true);

            }

            ACTIVE_DISPLAY_NAMES.add(displayName);
            return new CachedInputOutput(io, displayName, actions);
        }
    }

    private static boolean isAppropriateName(String base, String toMatch) {
        if (!toMatch.startsWith(base)) {
            return false;
        }
        return toMatch.substring(base.length()).matches("^(\\ #[0-9]+)?$"); // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - tab name",
        "# {1} - tab number",
        "LBL_Uniquified={0} #{1}"
    })
    private static String getNonActiveDisplayName(String displayNameBase) {
        String nonActiveDN = displayNameBase;
        if (ACTIVE_DISPLAY_NAMES.contains(nonActiveDN)) {
            // Uniquify: "prj (targ) #2", "prj (targ) #3", etc.
            int i = 2;
            String testdn;

            do {
                testdn = Bundle.LBL_Uniquified(nonActiveDN, i++);
            } while (ACTIVE_DISPLAY_NAMES.contains(testdn));

            nonActiveDN = testdn;
        }
        assert !ACTIVE_DISPLAY_NAMES.contains(nonActiveDN);
        return nonActiveDN;
    }

    public static final class CachedInputOutput {

        private static final Comparator<CachedInputOutput> DISPLAY_NAME_COMPARATOR = new Comparator<CachedInputOutput>() {

            @Override
            public int compare(CachedInputOutput o1, CachedInputOutput o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        };

        private final WeakReference<InputOutput> inputOutput;

        private final String displayName;

        private final List<Action> actions;

        public CachedInputOutput(InputOutput inputOutput, String displayName, List<Action> actions) {
            this.inputOutput = new WeakReference<>(inputOutput);
            this.displayName = displayName;
            this.actions = new ArrayList<>(actions);
        }

        @CheckForNull
        public InputOutput getInputOutput() {
            return inputOutput.get();
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<Action> getActions() {
            return Collections.unmodifiableList(actions);
        }
    }
}
