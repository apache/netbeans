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
            lines.add(new ServiceLoaderLine(impl, position, supersedes.toArray(new String[0])));
        }
    }

}
