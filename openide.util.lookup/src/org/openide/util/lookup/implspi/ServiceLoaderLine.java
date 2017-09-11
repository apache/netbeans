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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.util.lookup.implspi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * One entry in a {@code META-INF/services/*} file.
 * For purposes of collections, all lines with the same impl class are equal,
 * and lines with lower position sort first (else by class name).
 */
final class ServiceLoaderLine implements Comparable<ServiceLoaderLine> {

    private static final String POSITION = "#position="; // NOI18N
    private static final String SUPERSEDE = "#-"; // NOI18N

    private final String impl;
    private final int position;
    private final String[] supersedes;

    public ServiceLoaderLine(String impl, int position, String[] supersedes) {
        this.impl = impl;
        this.position = position;
        this.supersedes = supersedes;
    }

    public @Override int compareTo(ServiceLoaderLine o) {
        if (impl.equals(o.impl)) {
            return 0;
        }
        int r = position - o.position;
        return r != 0 ? r : impl.compareTo(o.impl);
    }

    public @Override boolean equals(Object o) {
        return o instanceof ServiceLoaderLine && impl.equals(((ServiceLoaderLine) o).impl);
    }

    public @Override int hashCode() {
        return impl.hashCode();
    }

    public void write(PrintWriter w) {
        w.println(impl);
        if (position != Integer.MAX_VALUE) {
            w.println(POSITION + position);
        }
        for (String exclude : supersedes) {
            w.println(SUPERSEDE + exclude);
        }
    }

    public static void parse(Reader r, SortedSet<ServiceLoaderLine> lines) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        String impl = null;
        int position = Integer.MAX_VALUE;
        List<String> supersedes = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith(POSITION)) {
                position = Integer.parseInt(line.substring(POSITION.length()));
            } else if (line.startsWith(SUPERSEDE)) {
                supersedes.add(line.substring(SUPERSEDE.length()));
            } else {
                finalize(lines, impl, position, supersedes);
                impl = line;
                position = Integer.MAX_VALUE;
                supersedes.clear();
            }
        }
        finalize(lines, impl, position, supersedes);
    }
    private static void finalize(Set<ServiceLoaderLine> lines, String impl, int position, List<String> supersedes) {
        if (impl != null) {
            lines.add(new ServiceLoaderLine(impl, position, supersedes.toArray(new String[supersedes.size()])));
        }
    }

}
