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

import org.netbeans.conffile.LineSwitchWriter.ReplacementChecker;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Boudreau
 */
public class DefaultOptionsReplacementChecker implements ReplacementChecker {

    private static final String[] TWO_ARG_PREFIXES
            = new String[]{"--laf", "--fontsize", "--extra-uc", "--locale"};

    private static final Set<String> TWO_ARG = new HashSet<>(Arrays.asList(TWO_ARG_PREFIXES));

    @Override
    public boolean isReplacement(String[] nue, String[] old) {
        if (old.length == 0) {
            return false;
        }
        assert nue.length == old.length;
        if (nue.length == 2 && TWO_ARG.contains(nue[0]) && old[0].equals(nue[0])) {
            return true;
        }
        if (nue[0].equals(old[0])) {
            return true;
        }
        if (nue.length == 1) {
            Matcher ma = XX_PLUS.matcher(old[0]);
            Matcher mb = XX_PLUS.matcher(nue[0]);
            if (ma.find() && mb.find()) {
                if (ma.group(1).equals(mb.group(1))) {
                    return true;
                }
            }
        }
        String sp = sharedPrefix(nue[0], old[0]);
        if (sp.endsWith("=")) {
            return true;
        }
        return sp.length() >= 6 && (sp.startsWith("-J-Xmx") || sp.startsWith("-J-Xms") || sp.startsWith("-J-Xss"));
    }

    private static final Pattern XX_PLUS
            = Pattern.compile("^-J-XX:[\\-\\+](.*)$");

    private String sharedPrefix(String a, String b) {
        char[] ac = a.toCharArray();
        char[] bc = b.toCharArray();
        int max = Math.min(ac.length, bc.length);
        StringBuilder sb = new StringBuilder(max);
        for (int i = 0; i < max; i++) {
            if (ac[i] == bc[i]) {
                sb.append(ac[i]);
                if (ac[i] == '=') {
                    break;
                }
            } else {
                break;
            }
        }
        return sb.toString();
    }
}
