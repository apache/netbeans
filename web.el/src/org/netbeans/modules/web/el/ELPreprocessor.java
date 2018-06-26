/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

import com.sun.el.parser.ELParserConstants;
import com.sun.el.parser.ELParserTokenManager;
import com.sun.el.parser.SimpleCharStream;
import com.sun.el.parser.Token;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Converts expression language expressions with respect to the given conversion table.
 * Allows to convert offset between the original and converted expressions.
 * Typical usage is conversion of the xml entity references inside the facelets expressions.
 *
 * XXX: only shortening patterns (XX->Y) are supported so not rule like (A->BB) will work.
 * 
 * @todo Make the class pluggable so the xml - jsf entities are not hardcoded.
 * 
 * @author marekfukala
 */
public class ELPreprocessor {

    private static final Logger LOG = Logger.getLogger(ELPreprocessor.class.getName());

    /** 
     * Html entity references conversion table.
     */
    public static final String[][] XML_ENTITY_REFS_CONVERSION_TABLE = new String[][]{ //NOI18N
        {"&amp;", "&"}, 
        {"&gt;", ">"}, 
        {"&lt;", "<"},
        {"&quot;", "\""},
        {"&apos;", "'"}        
    };
    
    //escaped chars conversion for attribute values
    public static final String[][] ESCAPED_CHARACTERS = new String[][]{
        {"\\\\", "\\"}, /* \\ -> \ */
        {"\\<", "<"},   /* \< -> < */
        {"\\>", ">"},   /* \> -> > */
        {"\\\"", "\""}, /* \" -> " */
        {"\\'", "'"},   /* \' -> ' */
        {"\\&", "&"},   /* \& -> & */
    };
    
    private final String originalExpression;
    private final String[][][] conversionTables;
    
    private String preprocessedExpression;
    private final boolean[] diffs;

    private List<OffsetRange> stringLiterals = new LinkedList<>();
    
    public ELPreprocessor(String expression, String[][]... conversionTables) {
        this.originalExpression = expression;
        this.conversionTables = conversionTables;
        this.diffs = new boolean[originalExpression.length()];
        init();
    }
    
    public String getOriginalExpression() {
        return originalExpression;
    }
    
    public String getPreprocessedExpression() {
        return preprocessedExpression;
    }
    
    public int getOriginalOffset(int preprocessedELoffset) {
        int diff = 0;
        for(int i = 0; i < originalExpression.length(); i++) {
            int pointer = i + diff;            
            if(pointer == preprocessedELoffset) {
                return i;
            }
            diff += diffs[i] ? -1 : 0;
        }
        //if we got here the offset points at the very end of the expression
        assert preprocessedELoffset == preprocessedExpression.length(); //or there's a bug
        return originalExpression.length(); //last offset handled here
    }
    
    public int getPreprocessedOffset(int originalOffset) {
        int diff = 0;
        for(int i = 0; i < originalOffset; i++) {
            diff += diffs[i] ? -1 : 0;
        }
        return originalOffset + diff;
    }

    //the algorithm is far from the most effective one, but for relatively small
    //set of the patterns the complexity is acceptable
    private void init() {
        long start = System.nanoTime();
        boolean[] localDiffs = new boolean[diffs.length];
        String result = originalExpression;
        preprocessStringLiterals();
        LOG.log(Level.FINEST, "StringLiteral preprocessing took: {0} ns", (System.nanoTime() - start));
        for(String[][] table : conversionTables) {
            for(String[] patternPair : table) {
                //create local diffs copy - needs to be used to properly convert positions during the processing
                System.arraycopy(diffs, 0, localDiffs, 0, diffs.length);
                
                StringBuilder resolved = new StringBuilder();
                String source = patternPair[0];
                String dest = patternPair[1];

                assert source.length() >= dest.length() : "no expanding rules supported!"; //NOI18N
                
                int match;
                int lastMatchEnd = 0;
                while((match = result.indexOf(source, lastMatchEnd)) != -1) {
                    if (isInsideStringLiteral(match)) {
                        resolved.append(result.substring(lastMatchEnd, match));
                        resolved.append(source);
                        lastMatchEnd = match + source.length();
                        continue;
                    }
                    resolved.append(result.substring(lastMatchEnd, match));
                    resolved.append(dest);
                    int originalSourceMatch = getOriginalOffset(match); //we operate on the already modified source text
                    int patternsLenDiff = source.length() - dest.length();
                    for(int i = originalSourceMatch; i < originalSourceMatch + patternsLenDiff; i++) {
                        localDiffs[i] = true;
                    }

                    lastMatchEnd = match + source.length();
                }
                resolved.append(result.substring(lastMatchEnd));

                result = resolved.toString();
                
                //set the locally modified diffs to the original diff array
                //this copying is necessary so the getOriginalOffset() conversion works properly *during* the 
                //actual source conversion where
                System.arraycopy(localDiffs, 0, diffs, 0, diffs.length);
            }
            
        }
        this.preprocessedExpression = result;
        LOG.log(Level.FINEST, "All preprocessing took: {0} ns", (System.nanoTime() - start));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ELPreprocessor.class.getSimpleName());
        sb.append('(');
        sb.append(System.identityHashCode(this));
        sb.append(')');
        sb.append('\n');
        for(String[][] table : conversionTables) {
            sb.append("table:");
            for(String[] pattern : table) {
                sb.append('(');
                sb.append(pattern[0]);
                sb.append("->");
                sb.append(pattern[1]);
                sb.append(')');
            }
            sb.append(' ');
        }
        sb.append('\n');
        sb.append("source:");
        sb.append(getOriginalExpression());
        sb.append("\n");
        sb.append("diffs :");
        for(int i = 0; i < diffs.length; i++) {
            sb.append(diffs[i] ? "-" : "0");
        }
        sb.append("\n");
        sb.append("result:");
        sb.append(getPreprocessedExpression());
        sb.append("\n");
        
        return sb.toString();
    }

    private void preprocessStringLiterals() {
        // optimalization - epressions w/out any string mustn't be preprocessed
        if (!originalExpression.contains("'") && !originalExpression.contains("\"")) { //NOI18N
            return;
        }
        SimpleCharStream simpleCharStream = new SimpleCharStream(new StringReader(originalExpression));
        ELParserTokenManager tokenManager = new ELParserTokenManager(simpleCharStream);
        while (true) {
            Token t = tokenManager.getNextToken();
            if (t.kind == ELParserConstants.EOF) {
                break;
            } else if (t.kind == ELParserConstants.STRING_LITERAL) {
                stringLiterals.add(new OffsetRange(t.beginColumn, t.beginColumn + t.image.length() - 1));
            }
        }
    }

    private boolean isInsideStringLiteral(int offset) {
        for (OffsetRange offsetRange : stringLiterals) {
            if (offsetRange.getStart() <= offset && offsetRange.getEnd() > offset) {
                return true;
            }
        }
        return false;
    }

}
