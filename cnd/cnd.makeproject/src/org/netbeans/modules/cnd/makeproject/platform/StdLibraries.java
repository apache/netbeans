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

package org.netbeans.modules.cnd.makeproject.platform;

import java.util.HashMap;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.openide.util.NbBundle;

/**
 *
 */
public class StdLibraries {
    private static final HashMap<String, LibraryItem.StdLibItem> libraries = new HashMap<>();

    static {
        addLibrary("Motif", new String[] {"Xm", "Xt", "Xext", "X11"}); // NOI18N
        addLibrary("Mathematics", new String[] {"m"}); // NOI18N
        addLibrary("Yacc", new String[] {"y"}); // NOI18N
        addLibrary("Lex", new String[] {"l"}); // NOI18N
        addLibrary("SocketsNetworkServices", new String[] {"socket", "nsl"}); // NOI18N
        addLibrary("SolarisThreads", new String[] {"thread"}); // NOI18N
        addLibrary("DataCompression", new String[] {"z"}); // NOI18N
        addLibrary("PosixThreads", new String[] {"pthread"}); // NOI18N
        addLibrary("Posix4", new String[] {"posix4"}); // NOI18N
        addLibrary("Internationalization", new String[] {"intl"}); // NOI18N
        addLibrary("PatternMatching", new String[] {"gen"}); // NOI18N
        addLibrary("Curses", new String[] {"curses"}); // NOI18N
        addLibrary("DynamicLinking", new String[] {"dl"}); // NOI18N
        addLibrary("CUnit", new String[] {"cunit"}); // NOI18N
        addLibrary("CppUnit", new String[] {"cppunit"}); // NOI18N
        addLibrary("OracleInstantClient", new String[]{"clntsh", "nnz11"}); // NOI18N
    }

    public static LibraryItem.StdLibItem getStandardLibary(String id) {
        return libraries.get(id);
    }

    private static void addLibrary(String id, String[] libs) {
        LibraryItem.StdLibItem item = new LibraryItem.StdLibItem(id, NbBundle.getBundle(StdLibraries.class).getString("StdLib."+id), libs); // NOI18N
        libraries.put(item.getName(), item);
    }

    private StdLibraries() {
    }
}
