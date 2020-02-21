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
