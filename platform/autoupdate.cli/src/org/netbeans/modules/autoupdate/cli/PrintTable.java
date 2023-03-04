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
package org.netbeans.modules.autoupdate.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Prints table formated for output stream.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class PrintTable {
    private final String[] names;
    private final List<String[]> data;
    private int[] limits;
    
    public PrintTable(String... fieldNames) {
        names = fieldNames;
        data = new ArrayList<String[]>();
    }
    
    public void setLimits(int... limits) {
        assert limits.length == names.length;
        this.limits = limits;
    }
    
    public void addRow(String... fields) {
        data.add(fields);
    }
    
    public void write(Appendable ps) throws IOException {
        int[] lengths = new int[names.length];
        length(names, lengths, limits);
        for (String[] arr : data) {
            length(arr, lengths, limits);
        }
        
        printRow(ps, names, lengths, limits);
        printSeparator(ps, lengths);
        for (String[] arr : data) {
            printRow(ps, arr, lengths, limits);
        }
        printSeparator(ps, lengths);
    }
    
    private static void length(String[] data, int[] lengths, int[] limits) {
        assert data.length == lengths.length;
        for (int i = 0; i < data.length; i++) {
            int l = data[i].length();
            if (limits != null && limits[i] >= 0 && l > limits[i]) {
                l = limits[i];
            }
            if (lengths[i] < l) {
                lengths[i] = l;
            }
        }
    }

    private static void printRow(Appendable ps, String[] data, int[] lengths, int[] limits) throws IOException {
        assert data.length == lengths.length;
        String sep = "";
        for (int i = 0; i < data.length; i++) {
            ps.append(sep);
            String d = data[i];
            if (limits != null && limits[i] >= 0 && d.length() > limits[i]) {
                d = d.substring(0, limits[i]);
            }
            ps.append(d);
            int missing = lengths[i] - d.length();
            while (missing-- > 0) {
                ps.append(' ');
            }
            sep = " ";
        }
        ps.append('\n');
    }
    private static void printSeparator(Appendable ps, int[] lengths) throws IOException {
        String sep = "";
        for (int i = 0; i < lengths.length; i++) {
            ps.append(sep);
            for (int j = 0; j < lengths[i]; j++) {
                ps.append('-');
            }
            sep = " ";
        }
        ps.append('\n');
    }
}
