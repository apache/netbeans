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
package org.netbeans.modules.nativeexecution.api.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A helper utility class for reading from Windows registry.
 *
 * This class uses reg.exe windows binary for querying system registry. The
 * functionality is limited to getting results of execution of
 * <code>reg &<lt>key&<gt> /s</code> and
 * <code>reg &<lt>key&<gt> /v varname</code>.
 *
 * This class is not thread-safe.
 *
 * Iteration goes through several root keys as specified by rootKeys param. By
 * default the order and keys are: RootKey.HKLM, RootKey.HKCU, RootKey.NHKLM,
 * RootKey.NHKCU.
 *
 * @See {@link RootKey}
 *
 * Specifying RootKey.N... keys as one of rootKeys param means that on 64-bit
 * Windows 64-bit view of the registry will be examined even from 32-bit jvm.
 *
 * No any parsing of any output is performed by this class.
 *
 * Usage:  <code>
 *      WindowsRegistryIterator registryIterator = WindowsRegistryIterator.get("SOFTWARE\\MyApp", "MyValue");
 *      while (registryIterator.hasNext()) {
 *          String[] output = registryIterator.next();
 *          if (output != null) {
 *              if (output_matches_needed_criteria(output)) {
 *                  return parse_output_as_you_want(output);
 *              }
 *          }
 *      }
 *      return null;
 * </code>
 *
 * Note: if a subKey starts (case-insensitively) with "HKLM\\" or "HKCU\\", it
 * is modified not to contain this root prefix before proceeding with a search.
 *
 * @author akrasny
 * @since 1.30.2
 */
public final class WindowsRegistryIterator implements Iterator<String[]> {

    private final RootKey[] rootKeys;
    private final String subKey;
    private final String valueName;
    private final boolean recursively;
    private int idx;

    private WindowsRegistryIterator(String subKey, RootKey[] rootKeys, String valueName, boolean recursively) {
        if (subKey.toLowerCase().startsWith("hklm\\") || subKey.toLowerCase().startsWith("hkcu\\")) { // NOI18N
            subKey = subKey.substring(5);
        }
        this.subKey = subKey;
        this.rootKeys = rootKeys;
        this.valueName = valueName;
        this.recursively = recursively;
        this.idx = 0;
    }

    /**
     * Returns an instance of the iterator configured to iterate over a default
     * sequence of rootKeys looking for a valueName by subKey path without
     * recursion.
     *
     * @param subKey - subkey to search in
     * @param valueName - a name of key value to query
     * @return a ready-to-use iterator
     */
    public static WindowsRegistryIterator get(String subKey, String valueName) {
        return get(subKey, new RootKey[]{RootKey.HKLM, RootKey.HKCU, RootKey.NHKLM, RootKey.NHKCU}, valueName, false);
    }

    /**
     * Returns an instance of the iterator configured to iterate over a default
     * sequence of rootKeys looking for a valueName by subKey path possibly
     * looking into subkeys recursively.
     *
     * @param subKey - subkey to search in
     * @param valueName - a name of key value to query
     * @param recursively - <code>true</code> means recursive query
     * @return a ready-to-use iterator
     */
    public static WindowsRegistryIterator get(String subKey, String valueName, boolean recursively) {
        return get(subKey, new RootKey[]{RootKey.HKLM, RootKey.HKCU, RootKey.NHKLM, RootKey.NHKCU}, valueName, recursively);
    }

    /**
     * Returns an instance of the iterator configured to iterate over a
     * specified sequence of rootKeys looking for a valueName by subKey path
     * possibly looking into subkeys recursively.
     *
     * @param subKey - subkey to search in
     * @param rootKeys - rootKeys to iterate over
     * @param valueName - a name of key value to query
     * @param recursively - <code>true</code> means recursive query
     * @return a ready-to-use iterator
     */
    public static WindowsRegistryIterator get(String subKey, RootKey[] rootKeys, String valueName, boolean recursively) {
        return new WindowsRegistryIterator(subKey, rootKeys, valueName, recursively);
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words,
     * returns {@code true} if {@link #next} would return an element rather than
     * throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return idx < rootKeys.length;
    }

    /**
     * Iterates over remaining rootKeys in attempt to get some output from
     * reg.exe.
     *
     * If reg.exe exited with exit code 0, it's output is split by '\n' and
     * returned.
     *
     * Note that this method is not really interrupt-able because it uses
     * ProcessUtils.execute() that doesn't handle thread interruption. By the
     * same reason this method could lead to OOM because all output from reg.exe
     * is accumulated before processing.
     *
     * @return - <code>null</code> if query with the current parameters fails,
     * reg.exe output otherwise.
     */
    @Override
    public String[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        while (idx < rootKeys.length) {
            RootKey key = rootKeys[idx++];
            String fullKey;
            String reg_exe;
            switch (key) {
                case HKLM:
                    fullKey = "HKLM\\" + subKey; // NOI18N
                    reg_exe = "%SystemRoot%\\system32\\reg.exe"; // NOI18N
                    break;
                case NHKLM:
                    fullKey = "HKLM\\" + subKey; // NOI18N
                    reg_exe = "%SystemRoot%\\sysnative\\reg.exe"; // NOI18N
                    break;
                case HKCU:
                    fullKey = "HKCU\\" + subKey; // NOI18N
                    reg_exe = "%SystemRoot%\\system32\\reg.exe"; // NOI18N
                    break;
                case NHKCU:
                    fullKey = "HKCU\\" + subKey; // NOI18N
                    reg_exe = "%SystemRoot%\\sysnative\\reg.exe"; // NOI18N
                    break;
                default:
                    throw new InternalError("Unhandled/Unknown/Unimplemented RootKey"); // NOI18N
            }
            StringBuilder suffix = new StringBuilder();
            if (recursively) {
                suffix.append(" /s "); // NOI18N
            }
            if (valueName != null) {
                suffix.append(" /v "); // NOI18N
                if (valueName.indexOf(' ')>0) { // NOI18N
                    suffix.append('"').append(valueName).append('"'); // NOI18N
                } else {
                    suffix.append(valueName);
                }
            }
            if (fullKey.indexOf(' ')>0) {
                fullKey = "\""+fullKey+"\""; // NOI18N
            }
            ProcessBuilder pb = new ProcessBuilder("cmd", "/C", reg_exe + " query " + fullKey + suffix.toString()); // NOI18N
            ProcessUtils.ExitStatus result = ProcessUtils.execute(pb);
            if (result.isOK()) {
                return result.getOutputString().split("\n"); // NOI18N
            }
        }
        return null;
    }

    /**
     * throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove operation is not supported by this iterator"); // NOI18N
    }

    /**
     * Root keys that could be iterated.
     *
     * NHKLM and NHKCU denotes that reg.exe should be started in a special way
     * so that 64-bit registry is queried from 32-bit jdk.
     */
    public enum RootKey {

        HKLM,
        NHKLM,
        HKCU,
        NHKCU
    }
}
