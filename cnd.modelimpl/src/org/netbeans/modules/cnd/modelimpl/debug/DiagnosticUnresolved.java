/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 *
 */
public class DiagnosticUnresolved {


    private static class IntArray {

        private int[] data;
        private int size;
        
        public IntArray(int capacity) {
            data = new int[capacity];
            size = 0;
        }
        
        public IntArray() {
            this(64);
        }
        
        public int get(int index) {
            return data[index];
        }
        
        public int size() {
            return size;
        }
        
        public void add(int value) {
            if( ! contains(value) ) {
                if( size >= data.length ) {
                    int[] old = data;
                    data = new int[old.length + 128];
                }
                data[size++] = value;
            }
        }
        
        protected boolean contains(int value) {
            for (int i = 0; i < size; i++) {
                if( data[i] == value ) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    private static class UnresolvedInfoBase {
        
        private final String name;
        private int count;
        
        public UnresolvedInfoBase(String name) {
            this.name = name;
        }
        
        public void registerOccurence(CsmFile file, int offset) {
            count++;
        }
        
        public int getCount() {
            return count;
        }
        
        public String getName() {
            return name;
        }

        public void dumpStatistics(PrintStream out) {
            out.println(getName() + ' ' + getCount());
        }
    }

    
    private static class UnresolvedInfoEx extends UnresolvedInfoBase {

        private final Map<CsmFile, IntArray> files = new HashMap<>();
        
        public UnresolvedInfoEx(String name) {
            super(name);
        }
        
        @Override
        public void registerOccurence(CsmFile file, int offset) {
            super.registerOccurence(file, offset);
            IntArray ia = files.get(file);
            if( ia == null ) {
                ia = new IntArray();
                files.put(file, ia);
            }
            ia.add(offset);
        }

        @Override
        public void dumpStatistics(PrintStream out) {
            
            out.println(getName() + ' ' + getCount());
            out.println(" By files:"); // NOI18N
            
            Comparator<CsmFile> comp = new Comparator<CsmFile>() {
                @Override
                public int compare(CsmFile o1, CsmFile o2) {
                    if( o1 == o2 ) {
                        return 0;
                    }
                    IntArray ia1 = files.get(o1);
                    IntArray ia2 = files.get(o2);
                    return (ia1.size() > ia2.size()) ? -1 : 1;
                }
            };
            
            List<CsmFile> list = new ArrayList<>(files.keySet());
            Collections.sort(list, comp);
            for (Iterator it = list.iterator(); it.hasNext();) {
                CsmFile file = (CsmFile) it.next();
                IntArray ia = files.get(file);
                int cnt = (ia == null) ? -1 : ia.size();
                out.println("    " +  file.getAbsolutePath() + ' ' + cnt); // NOI18N
            }

        }
        
    }
    
    private Map<String, UnresolvedInfoBase> map = new HashMap<>();
    private int level;
    
    public DiagnosticUnresolved(int level) {
        this.level = level;
    }
    
    private static String glueName(CharSequence[] nameTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameTokens.length; i++) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
    
    public void onUnresolved(CharSequence[] nameTokens, CsmFile file, int offset) {
        if( level < 1 ) {
            return;
        }
        String name = glueName(nameTokens);
        UnresolvedInfoBase u = map.get(name);
        if( u == null ) {
            u = (level == 1) ? new UnresolvedInfoBase(name) : new UnresolvedInfoEx(name);
            map.put(name, u);
        }
        u.registerOccurence(file, offset);
    }
        
    public void dumpStatictics(String fileName, boolean append) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(fileName, append), true);
        try {
            dumpStatictics(out);
        }
        finally {
            out.close();
        }
    }
    
    protected void dumpStatictics(PrintStream out) {
            
        out.println("\n**** Unresolved names statistics\n"); // NOI18N
        
        Comparator<UnresolvedInfoBase> comp = new Comparator<UnresolvedInfoBase>() {
            @Override
            public int compare(UnresolvedInfoBase ui1, UnresolvedInfoBase ui2) {
                if( ui1 == ui2 ) {
                    return 0;
                }
                return (ui1.getCount() > ui2.getCount()) ? -1 : 1;
            }
        };
        
        List<UnresolvedInfoBase> infos = new ArrayList<>(map.values());
        int total = 0;
        Collections.sort(infos, comp);
        for (Iterator it = infos.iterator(); it.hasNext();) {
            UnresolvedInfoBase ui = (UnresolvedInfoBase) it.next();
            ui.dumpStatistics(out);
            total += ui.getCount();
        }
        
        out.println("Totally " + total + " unresolved"); // NOI18N
    }
    
}
