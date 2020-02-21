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
package org.netbeans.modules.cnd.makeproject;

import java.util.Map;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 */
public class MakeActionTest {

    private static final boolean TRACE = false;

    public MakeActionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFilesystemConsistency() {
        MakeActionProviderImpl impl = new MakeActionProviderImpl();
    }

    private class MakeActionProviderImpl implements ActionProvider {

        // Commands available from Make project
        public static final String COMMAND_BATCH_BUILD = "batch_build"; // NOI18N
        public static final String COMMAND_BUILD_PACKAGE = "build_packages"; // NOI18N
        public static final String COMMAND_CUSTOM_ACTION = "custom.action"; // NOI18N
        private final String[] supportedActions = {
            COMMAND_BUILD,
            COMMAND_CLEAN,
            COMMAND_REBUILD,
            COMMAND_COMPILE_SINGLE,
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_STEP_INTO,
            COMMAND_DEBUG_SINGLE,
            COMMAND_BATCH_BUILD,
            COMMAND_BUILD_PACKAGE,
            COMMAND_DELETE,
            COMMAND_COPY,
            COMMAND_MOVE,
            COMMAND_RENAME,
            COMMAND_CUSTOM_ACTION,};
        /** Map from commands to ant targets */
        private final Map<String, String[]> commands;
        private final Map<String, String[]> commandsNoBuild;
        private static final String SAVE_STEP = "save"; // NOI18N
        private static final String BUILD_STEP = "build"; // NOI18N
        private static final String BUILD_PACKAGE_STEP = "build-package"; // NOI18N
        private static final String CLEAN_STEP = "clean"; // NOI18N
        private static final String RUN_STEP = "run"; // NOI18N
        private static final String DEBUG_STEP = "debug"; // NOI18N
        private static final String DEBUG_STEPINTO_STEP = "debug-stepinto"; // NOI18N
        private static final String RUN_SINGLE_STEP = "run-single"; // NOI18N
        private static final String DEBUG_SINGLE_STEP = "debug-single"; // NOI18N
        private static final String COMPILE_SINGLE_STEP = "compile-single"; // NOI18N
        private static final String CUSTOM_ACTION_STEP = "custom-action"; // NOI18N
        //private static final String REMOVE_INSTRUMENTATION_STEP = "remove-instrumentation"; // NOI18N
        private static final String VALIDATE_TOOLCHAIN = "validate-toolchain"; // NOI18N
        private static final String CONFIGURE_STEP = "configure"; // NOI18N

        private MakeActionProviderImpl() {
            commands = new TreeMap<>();
            commands.put(COMMAND_BUILD, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, BUILD_STEP});
            commands.put(COMMAND_BUILD_PACKAGE, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, BUILD_STEP, BUILD_PACKAGE_STEP});
            commands.put(COMMAND_CLEAN, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, CLEAN_STEP});
            commands.put(COMMAND_REBUILD, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, CLEAN_STEP, BUILD_STEP});
            commands.put(COMMAND_RUN, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, /*REMOVE_INSTRUMENTATION_STEP,*/ BUILD_STEP, RUN_STEP});
            commands.put(COMMAND_DEBUG, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, /*REMOVE_INSTRUMENTATION_STEP,*/ BUILD_STEP, DEBUG_STEP});
            commands.put(COMMAND_DEBUG_STEP_INTO, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, /*REMOVE_INSTRUMENTATION_STEP,*/ BUILD_STEP, DEBUG_STEPINTO_STEP});
            commands.put(COMMAND_RUN_SINGLE, new String[]{RUN_SINGLE_STEP});
            commands.put(COMMAND_DEBUG_SINGLE, new String[]{DEBUG_SINGLE_STEP});
            commands.put(COMMAND_COMPILE_SINGLE, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, COMPILE_SINGLE_STEP});
            commands.put(COMMAND_CUSTOM_ACTION, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, BUILD_STEP, CUSTOM_ACTION_STEP});
            commandsNoBuild = new TreeMap<>();
            commandsNoBuild.put(COMMAND_BUILD, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, BUILD_STEP});
            commandsNoBuild.put(COMMAND_BUILD_PACKAGE, new String[]{SAVE_STEP, BUILD_PACKAGE_STEP});
            commandsNoBuild.put(COMMAND_CLEAN, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, CLEAN_STEP});
            commandsNoBuild.put(COMMAND_REBUILD, new String[]{SAVE_STEP, VALIDATE_TOOLCHAIN, CLEAN_STEP, BUILD_STEP});
            commandsNoBuild.put(COMMAND_RUN, new String[]{/*REMOVE_INSTRUMENTATION_STEP,*/RUN_STEP});
            commandsNoBuild.put(COMMAND_DEBUG, new String[]{/*REMOVE_INSTRUMENTATION_STEP,*/DEBUG_STEP});
            commandsNoBuild.put(COMMAND_DEBUG_STEP_INTO, new String[]{/*REMOVE_INSTRUMENTATION_STEP,*/DEBUG_STEPINTO_STEP});
            commandsNoBuild.put(COMMAND_CUSTOM_ACTION, new String[]{SAVE_STEP, CUSTOM_ACTION_STEP});
            boolean res1 = verifyMaps(commands, loadActionSteps("CND/BuildAction", new String[]{"build-first"}), "CND/BuildAction"); // NOI18N
            boolean res2 = verifyMaps(commandsNoBuild, loadActionSteps("CND/BuildAction", new String[0]), "CND/BuildAction"); // NOI18N
            if (!res1 || !res2) {
                if (TRACE) {
                    dumpXML(commands, "BuildAction");
                    dumpXML(commandsNoBuild, "NoBuildAction");
                }
                assert false;
            }
        }

        private Map<String, String[]> loadActionSteps(String root, String[] flags) {
            if (TRACE) {
                System.err.println("Root " + root); // NOI18N
            }
            Map<String, String[]> res = new TreeMap<>();
            FileObject folder = FileUtil.getConfigFile(root);
            if (folder != null && folder.isFolder()) {
                for (FileObject subFolder : folder.getChildren()) {
                    if (TRACE) {
                        System.err.println("\tCommand " + subFolder.getNameExt()); // NOI18N
                    }
                    if (subFolder.isFolder()) {
                        TreeMap<Integer, String> map = new TreeMap<>();
                        loop:for (FileObject file : subFolder.getChildren()) {
                            Integer position = (Integer) file.getAttribute("position"); // NOI18N
                            String flag = (String) file.getAttribute("flag"); // NOI18N
                            if (flag != null) {
                                for(String s : flag.split(",")) {
                                    boolean found = false;
                                    for(String f : flags) {
                                        if (s.equals(f)) {
                                            found = true;
                                        }
                                    }
                                    if (!found) {
                                        continue loop;
                                    }
                                }
                            }
                            map.put(position, file.getNameExt());
                        }
                        if (TRACE) {
                            map.values().forEach((step) -> {
                                System.err.println("\t\tStep " + step); // NOI18N
                            });
                        }
                        res.put(subFolder.getNameExt(), map.values().toArray(new String[map.size()]));
                    }
                }
            }
            return res;
        }

        private boolean verifyMaps(Map<String, String[]> map1, Map<String, String[]> map2, String root) {
            boolean res = true;
            for (Map.Entry<String, String[]> entry : map1.entrySet()) {
                String[] arr2 = map2.get(entry.getKey());
                if (arr2 == null) {
                    if (TRACE) {
                        System.err.println("No found key " + root + "/" + entry.getKey());
                    }
                    res = false;
                } else {
                    String[] arr1 = entry.getValue();
                    if (arr1.length != arr2.length) {
                        if (TRACE) {
                            System.err.println("No equal size of key " + root + "/" + entry.getKey());
                        }
                        res = false;
                    } else {
                        for (int i = 0; i < arr1.length; i++) {
                            if (!arr1[i].equals(arr2[i])) {
                                if (TRACE) {
                                    System.err.println("No equal value of key " + root + "/" + entry.getKey() + " " + arr1[i] + "!=" + arr2[i]);
                                }
                                res = false;
                            }
                        }
                    }
                }
            }
            return res;
        }

        private String getIndent(int level) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < level * 4; i++) {
                buf.append(' ');
            }
            return buf.toString();
        }

        private void dumpXML(Map<String, String[]> map1, String subSequence) {
            int level = 1;
            System.out.println(getIndent(level++) + "<folder name=\"CND\">");
            System.out.println(getIndent(level++) + "<folder name=\"" + subSequence + "\">");
            for (Map.Entry<String, String[]> entry : map1.entrySet()) {
                System.out.println(getIndent(level++) + "<folder name=\"" + entry.getKey() + "\">");
                String[] arr = entry.getValue();
                for (int i = 0; i < arr.length; i++) {
                    System.out.println(getIndent(level++) + "<file name=\"" + arr[i] + "\">");
                    System.out.println(getIndent(level) + "<attr name=\"position\" intvalue=\"" + ((i + 1) * 100) + "\"/>");
                    System.out.println(getIndent(--level) + "</file>");
                }
                System.out.println(getIndent(--level) + "</folder>");
            }
            System.out.println(getIndent(--level) + "</folder>	<!-- " + subSequence + " -->");
            System.out.println(getIndent(--level) + "</folder>       <!-- CND -->");
        }

        @Override
        public String[] getSupportedActions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
