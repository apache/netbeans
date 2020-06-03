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

package org.netbeans.modules.cnd.highlight.error;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;

/**
 * An ErrorMaker that searches for and deletes a string
 * (for example, ";" or "{")
 */
public abstract class SimpleDeletingErrorMaker extends BaseErrorMaker {

    private final String textToDelete;
    private int searchFrom;

    public SimpleDeletingErrorMaker(String textToDelete) {
        this.textToDelete = textToDelete;
    }

    @Override
    public void init(BaseDocument document, CsmFile csmFile) {
        super.init(document, csmFile);
        searchFrom = 0;
    }
    
    @Override
    public boolean change() throws BadLocationException {
        BaseDocument doc = getDocument();
        String text = doc.getText(searchFrom,  doc.getLength() - searchFrom);
        int pos = text.indexOf(textToDelete);
        if( pos < 0 ) {
            return false;
        } else {
            pos += searchFrom;
            searchFrom = pos + 1;
            remove(pos, 1);
            return true;
        }
    }

    /**
     * A simple class for gathering statistics
     */
    private static class Stat {
        
        private final String file;
        public int triesCount;
        public int hitCount;
        public int inducedCount;

        public Stat(String file) {
            this.file = file;
        }
        
        public void consume(Stat other) {
            triesCount += other.triesCount;
            hitCount += other.hitCount;
            inducedCount += other.inducedCount;
        }
        
        public void print() {
            System.err.printf("%8d %8d %8d %s\n", triesCount, hitCount, inducedCount, file);
        }
        
        public static void printHeader() {
            System.err.printf("%8s %8s %8s %s\n", "tries", "hits", "induced", "file");
        }
    }
    
    private final Map<String, Stat> stats = new HashMap<>();
    
    private Stat getCurrentStat() {
        String absPath = getCsmFile().getAbsolutePath().toString();
        Stat stat = stats.get(absPath);
        if (stat == null) {
            stat = new Stat(absPath);
            stats.put(absPath, stat);
        }
        return stat;
    }
    
    @Override
    public void analyze(Collection<CsmErrorInfo> errors) {
        Stat stat = getCurrentStat();
        stat.triesCount++;
        if (!errors.isEmpty()) {
            stat.hitCount++;
            stat.inducedCount += errors.size() - 1;
        }
    }
    
    public void printStatistics() {
        System.err.printf("\nStatistics:\n");
        Stat.printHeader();
        Stat total = new Stat("TOTAL");
        for (Stat stat : stats.values()) {
            stat.print();
            total.consume(stat);
        }
        total.print();
    }
}
