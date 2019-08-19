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
package org.netbeans.conffile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Writes line switches back to the netbeans.conf file.
 *
 * @author Tim Boudreau
 */
public class LineSwitchWriter {

    private final List<String> switches = new LinkedList<>();
    private final List<String> originals;
    private final ReplacementChecker checker;

    public LineSwitchWriter(List<String> originalSwitches, ReplacementChecker checker) {
        switches.addAll(originalSwitches);
        originals = new LinkedList<>(originalSwitches);
        this.checker = checker;
    }

    public LineSwitchWriter(List<String> originalSwitches) {
        this(originalSwitches, new DefaultOptionsReplacementChecker());
    }

    /**
     * Get the current list of switches.
     *
     * @return
     */
    public List<String> switches() {
        return switches;
    }

    /**
     * Append one or more JVM arguments to a variable defined in the
     * configuration file; the replacement checker eliminates conflicting ones.
     *
     * @param s An array of line switches
     * @return this
     */
    public LineSwitchWriter appendOrReplaceArguments(String... s) {
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].trim();
        }
        String[] test = new String[s.length];
        boolean replaced = false;
        Set<Integer> duplicates = new TreeSet<>();
        for (int i = 0; i < switches.size(); i++) {
            if (i + test.length > switches.size()) {
                break;
            }
            for (int j = 0; j < test.length; j++) {
                test[j] = switches.get(i + j);
            }
            if (checker.isReplacement(s, test)) {
                boolean didReplace = false;
                for (int j = 0; j < test.length; j++) {
                    // It is possible to have duplicate entries
                    // in netbeans.conf variables, so don't assume
                    // if we replaced one we can break the loop
                    if (replaced) {
                        duplicates.add(i);
                    } else {
                        didReplace = true;
                        switches.set(i + j, s[j]);
                    }
                }
                if (didReplace) {
                    replaced = true;
                }
            }
        }
        List<Integer> toRemove = new ArrayList<>(duplicates);
        Collections.reverse(toRemove);
        for (int rem : toRemove) {
            switches.remove(rem);
        }
        if (!replaced) {
            for (String s1 : s) {
                switches.add(s1.trim());
            }
        }
        return this;
    }

    boolean hasChanges() {
        return !originals.equals(switches);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256).append('"');
        List<String> all = new ArrayList<>(switches);
        for (String sw : all) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(sw);
        }
        return sb.append("\"\n").toString();
    }

    public void removeArgument(String lineSwitch) {
        switches.remove(lineSwitch);
    }

    interface ReplacementChecker {

        boolean isReplacement(String[] nue, String[] old);
    }
}
