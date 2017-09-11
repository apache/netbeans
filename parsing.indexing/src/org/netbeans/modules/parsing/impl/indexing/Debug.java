/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
final class Debug {
    private Debug() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    @NonNull
    static StringBuilder printMap(
            @NonNull final Map<URL, List<URL>> deps,
            @NonNull final StringBuilder sb) {
        Set<URL> sortedRoots = new TreeSet<>(C);
        sortedRoots.addAll(deps.keySet());
        for(URL url : sortedRoots) {
            sb.append("  ").append(url).append(":\n"); //NOI18N
//            for(URL depUrl : deps.get(url)) {
//                sb.append("  -> ").append(depUrl).append("\n"); //NOI18N
//            }
        }
        return sb;
    }

    static StringBuilder printCollection(Collection<? extends URL> collection, StringBuilder sb) {
        Set<URL> sortedRoots = new TreeSet<>(C);
        sortedRoots.addAll(collection);
        for(URL url : sortedRoots) {
            sb.append("  ").append(url).append("\n"); //NOI18N
        }
        return sb;
    }

    static StringBuilder printMimeTypes(Collection<? extends String> collection, StringBuilder sb) {
        for(Iterator<? extends String> i = collection.iterator(); i.hasNext(); ) {
            String mimeType = i.next();
            sb.append("'").append(mimeType).append("'"); //NOI18N
            if (i.hasNext()) {
                sb.append(", "); //NOI18N
            }
        }
        return sb;
    }

    private static final Comparator<URL> C = new Comparator<URL>() {
        @Override
        public int compare(URL o1, URL o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };
}
