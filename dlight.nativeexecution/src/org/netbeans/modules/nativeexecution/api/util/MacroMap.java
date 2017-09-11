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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Utilities;

/**
 * This map is a wrapper of Map&lt;String, String&gt; that expands macros on
 * insertion...
 */
public final class MacroMap implements Cloneable {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final ExecutionEnvironment execEnv;
    private final MacroExpander macroExpander;
    private final TreeMap<String, String> hostEnv;
    private final TreeMap<String, String> map;
    private final Set<String> varsForExport = new HashSet<>();
    private final boolean isWindows;
    private final Object lock = new Object();

    private MacroMap(final ExecutionEnvironment execEnv, final MacroExpander macroExpander, boolean init) {
        this.execEnv = execEnv;
        this.macroExpander = macroExpander;
        this.isWindows = execEnv.isLocal() && Utilities.isWindows();

        if (isWindows) {
            map = new TreeMap<>(new CaseInsensitiveComparator());
            hostEnv = new TreeMap<>(new CaseInsensitiveComparator());
        } else {
            map = new TreeMap<>();
            hostEnv = new TreeMap<>();
        }

        if (init && HostInfoUtils.isHostInfoAvailable(execEnv)) {
            // This always should be true
            try {
                hostEnv.putAll(HostInfoUtils.getHostInfo(execEnv).getEnvironment());
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        varsForExport.addAll(EnvWriter.wellKnownVars);
    }

    /**
     * Create a MacroMap populated with macros defined in specified
     * ExecutionEnvironment
     */
    public static MacroMap forExecEnv(final ExecutionEnvironment execEnv) {
        return new MacroMap(execEnv, MacroExpanderFactory.getExpander(execEnv), true);
    }

    /**
     * Create empty MacroMap for specified ExecutionEnvironment
     */
    public static MacroMap createEmpty(final ExecutionEnvironment execEnv) {
        return new MacroMap(execEnv, MacroExpanderFactory.getExpander(execEnv), false);
    }

    public final void putAll(final MacroMap envVariables) {
        if (envVariables == null) {
            return;
        }

        putAll(envVariables.map);
    }

    public final void putAll(Map<String, String> map) {
        if (map == null) {
            return;
        }

        for (Entry<String, String> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public final void putAll(String[] env) {
        if (env == null) {
            return;
        }

        for (String envString : env) {
            put(EnvUtils.getKey(envString), EnvUtils.getValue(envString));
        }
    }

    public String put(String key, String value) {
        if (key == null) {
            throw new NullPointerException();
        }

        if (value == null) {
            log.log(Level.INFO, "Attempt to set env variable {0} with null value", key); // NOI18N
        }

        String result = value;

        TreeMap<String, String> oneElementMap = isWindows ? new TreeMap<String, String>(new CaseInsensitiveComparator()) : new TreeMap<String, String>();

        synchronized (lock) {
            if (!map.containsKey(key) && hostEnv.containsKey(key)) {
                oneElementMap.put(key, hostEnv.remove(key));
            } else if (map.containsKey(key)) {
                oneElementMap.put(key, map.get(key));
            }

            try {
                result = macroExpander.expandMacros(value, oneElementMap);
            } catch (ParseException ex) {
            }

            varsForExport.add(key);

            return map.put(key, result);
        }
    }

    public String get(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        synchronized (lock) {
            if (map.containsKey(key)) {
                return map.get(key);
            }

            return hostEnv.get(key);
        }
    }

    public Map<String, String> getUserDefinedMap() {
        TreeMap<String, String> result;
        synchronized (lock) {
            result = new TreeMap<>(map.comparator());
            result.putAll(map);
        }
        return result;
    }

    public Set<String> getExportVariablesSet() {
        return Collections.unmodifiableSet(varsForExport);
    }

    @Override
    public final String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{"); // NOI18N

        synchronized (lock) {
            for (Entry<String, String> entry : map.entrySet()) {
                buf.append(entry.getKey());
                buf.append(" = "); // NOI18N
                buf.append(entry.getValue());
                buf.append(", "); // NOI18N
            }
            for (Entry<String, String> entry : hostEnv.entrySet()) {
                buf.append(entry.getKey());
                buf.append(" = "); // NOI18N
                buf.append(entry.getValue());
                buf.append(", "); // NOI18N
            }
        }

        buf.append("}"); // NOI18N
        return buf.toString();
    }

    public final boolean isEmpty() {
        synchronized (lock) {
            return map.isEmpty() && hostEnv.isEmpty();
        }
    }

    public final Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> joint = new HashSet<>();
        synchronized (lock) {
            joint.addAll(map.entrySet());
            joint.addAll(hostEnv.entrySet());
        }
        return joint;
    }

    @Override
    public MacroMap clone() {
        MacroMap clone;
        synchronized (lock) {
            clone = new MacroMap(execEnv, macroExpander, false);
            clone.map.putAll(map);
            clone.hostEnv.putAll(hostEnv);
            clone.varsForExport.addAll(varsForExport);
        }
        return clone;
    }

    public void prependPathVariable(String name, String path) {
        if (path == null) {
            return;
        }

        String oldpath = get(name);
        String newPath = path + (oldpath == null ? "" : (isWindows ? ';' : ':') + oldpath); // NOI18N
        newPath = newPath.replaceAll("::", ":"); // NOI18N
        newPath = newPath.replaceAll("^:", ""); // NOI18N
        newPath = newPath.replaceAll(":$", ""); // NOI18N
        put(name, newPath);
    }

    public void appendPathVariable(String name, String path) {
        if (path == null) {
            return;
        }

        String oldpath = get(name);
        String newPath = (oldpath == null ? "" : oldpath + (isWindows ? ';' : ':')) + path; // NOI18N
        put(name, newPath);
    }

    public void dump(PrintStream out) {
        for (Entry<String, String> entry : entrySet()) {
            out.printf("Environment: %s=%s\n", entry.getKey(), entry.getValue()); // NOI18N
        }
    }

    public String remove(String name) {
        synchronized (lock) {
            if (map.containsKey(name)) {
                return map.remove(name);
            }
            return hostEnv.remove(name);
        }
    }

    public Map<String, String> toMap() {
        TreeMap<String, String> result;
        synchronized (lock) {
            result = new TreeMap<>(map.comparator());
            result.putAll(hostEnv);
            result.putAll(map);
        }
        return result;
    }

    public void clear() {
        synchronized (lock) {
            hostEnv.clear();
            map.clear();
        }
    }

    private static class CaseInsensitiveComparator implements Comparator<String>, Serializable {

        public CaseInsensitiveComparator() {
        }

        @Override
        public int compare(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return 0;
            }

            if (s1 == null) {
                return 1;
            }

            if (s2 == null) {
                return -1;
            }

            return s1.toUpperCase().compareTo(s2.toUpperCase());
        }
    }
}
