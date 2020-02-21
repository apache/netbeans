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
