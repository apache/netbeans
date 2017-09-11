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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
