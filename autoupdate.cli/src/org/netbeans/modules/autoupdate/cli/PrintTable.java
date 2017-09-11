/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.autoupdate.cli;

import java.io.PrintStream;
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
    
    public void write(PrintStream ps) {
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

    private static void printRow(PrintStream ps, String[] data, int[] lengths, int[] limits) {
        assert data.length == lengths.length;
        String sep = "";
        for (int i = 0; i < data.length; i++) {
            ps.print(sep);
            String d = data[i];
            if (limits != null && limits[i] >= 0 && d.length() > limits[i]) {
                d = d.substring(0, limits[i]);
            }
            ps.print(d);
            int missing = lengths[i] - d.length();
            while (missing-- > 0) {
                ps.print(' ');
            }
            sep = " ";
        }
        ps.println();
    }
    private static void printSeparator(PrintStream ps, int[] lengths) {
        String sep = "";
        for (int i = 0; i < lengths.length; i++) {
            ps.print(sep);
            for (int j = 0; j < lengths[i]; j++) {
                ps.print('-');
            }
            sep = " ";
        }
        ps.println();
    }
}
