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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
