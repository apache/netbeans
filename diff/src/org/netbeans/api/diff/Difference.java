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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.diff;

import java.io.Serializable;

/**
 * This class represents a single difference between two files.
 *
 * @author  Martin Entlicher
 */
public class Difference extends Object implements Serializable {

    /** Delete type of difference - a portion of a file was removed in the other */
    public static final int DELETE = 0;

    /** Add type of difference - a portion of a file was added in the other */
    public static final int ADD = 1;

    /** Change type of difference - a portion of a file was changed in the other */
    public static final int CHANGE = 2;
    
    private int type = 0;
    private int firstStart = 0;
    private int firstEnd = 0;
    private int secondStart = 0;
    private int secondEnd = 0;
    private Difference.Part[] firstLineDiffs;
    private Difference.Part[] secondLineDiffs;
    
    /** The text of the difference in the first file. */
    private String firstText;
    /** The text of the difference in the second file. */
    private String secondText;
    
    private static final long serialVersionUID = 7638201981188907148L;
    
    /**
     * Creates a new instance of Difference
     * @param type The type of the difference. Must be one of the <a href="#DELETE">DELETE</a>,
     *             <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a>
     * @param firstStart The line number on which the difference starts in the first file.
     * @param firstEnd The line number on which the difference ends in the first file.
     * @param secondStart The line number on which the difference starts in the second file.
     * @param secondEnd The line number on which the difference ends in the second file.
     */
    public Difference(int type, int firstStart, int firstEnd, int secondStart, int secondEnd) {
        this(type, firstStart, firstEnd, secondStart, secondEnd, null, null, null, null);
    }
    
    /**
     * Creates a new instance of Difference
     * @param type The type of the difference. Must be one of the <a href="#DELETE">DELETE</a>,
     *             <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a>
     * @param firstStart The line number on which the difference starts in the first file.
     * @param firstEnd The line number on which the difference ends in the first file.
     * @param secondStart The line number on which the difference starts in the second file.
     * @param secondEnd The line number on which the difference ends in the second file.
     * @param firstText The text content of the difference in the first file.
     * @param secondText The text content of the difference in the second file.
     */
    public Difference(int type, int firstStart, int firstEnd, int secondStart, int secondEnd,
                      String firstText, String secondText) {
        this(type, firstStart, firstEnd, secondStart, secondEnd, firstText, secondText, null, null);
    }
    
    /**
     * Creates a new instance of Difference
     * @param type The type of the difference. Must be one of the <a href="#DELETE">DELETE</a>,
     *             <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a>
     * @param firstStart The line number on which the difference starts in the first file.
     * @param firstEnd The line number on which the difference ends in the first file.
     * @param secondStart The line number on which the difference starts in the second file.
     * @param secondEnd The line number on which the difference ends in the second file.
     * @param firstText The text content of the difference in the first file.
     * @param secondText The text content of the difference in the second file.
     * @param firstLineDiffs The list of differences on lines in the first file.
     *                    The list contains instances of {@link Difference.Part}.
     *                    Can be <code>null</code> when there are no line differences.
     * @param secondLineDiffs The list of differences on lines in the second file.
     *                    The list contains instances of {@link Difference.Part}.
     *                    Can be <code>null</code> when there are no line differences.
     */
    public Difference(int type, int firstStart, int firstEnd, int secondStart, int secondEnd,
                      String firstText, String secondText, Difference.Part[] firstLineDiffs, Difference.Part[] secondLineDiffs) {
        if (type > 2 || type < 0) {
            throw new IllegalArgumentException("Bad Difference type = "+type);
        }
        this.type = type;
        this.firstStart = firstStart;
        this.firstEnd = firstEnd;
        this.secondStart = secondStart;
        this.secondEnd = secondEnd;
        this.firstText = firstText;
        this.secondText = secondText;
        this.firstLineDiffs = firstLineDiffs;
        this.secondLineDiffs = secondLineDiffs;
    }

    /**
     * Get the difference type. It's one of <a href="#DELETE">DELETE</a>,
     * <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a> meaning
     * respective change in second source.
     */
    public int getType() {
        return this.type;
    }
    
    /**
     * Get the line number on which the difference starts in the first file.
     *
     * <p>For ADD changes it returns previous line number e.g. 0 for add
     * file start.
     */
    public int getFirstStart() {
        return this.firstStart;
    }

    /**
     * Get the line number on which the difference ends in the first file.
     * <p>
     * Does not have any meaning for ADD changes.
     */
    public int getFirstEnd() {
        return this.firstEnd;
    }
    
    /**
     * Get the line number on which the difference starts in the second file.
     */
    public int getSecondStart() {
        return this.secondStart;
    }
    
    /**
     * Get the line number on which the difference ends in the second file.
     * <p>
     * Does not have any meaning for DELETE changes.
     */
    public int getSecondEnd() {
        return this.secondEnd;
    }
    
    /**
     * The list of differences on lines in the first file.
     * The list contains instances of {@link Difference.Part}.
     * Can be <code>null</code> when there are no line differences.
     */
    public Difference.Part[] getFirstLineDiffs() {
        return firstLineDiffs;
    }
    
    /**
     * The list of differences on lines in the second file.
     * The list contains instances of {@link Difference.Part}.
     * Can be <code>null</code> when there are no line differences.
     */
    public Difference.Part[] getSecondLineDiffs() {
        return secondLineDiffs;
    }
    
    /**
     * Get the text content of the difference in the first file.
     */
    public String getFirstText() {
        return firstText;
    }
    
    /**
     * Get the text content of the difference in the second file.
     */
    public String getSecondText() {
        return secondText;
    }
    
    public String toString() {
        return "Difference("+((type == ADD) ? "ADD" : (type == DELETE) ? "DELETE" : "CHANGE")+", "+
               firstStart+", "+firstEnd+", "+secondStart+", "+secondEnd+")";
    }
    
    /**
     * This class represents a difference on a single line.
     */
    public static final class Part extends Object implements Serializable {
        
        private int type;
        private int line;
        private int pos1;
        private int pos2;
        private String text;
        
        private static final long serialVersionUID = 7638201981188907149L;
    
        /**
          * Creates a new instance of LineDiff
          * @param type The type of the difference. Must be one of the {<a href="#DELETE">DELETE</a>,
          *             <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a>
          * @param line The line number
          * @param pos1 The position on which the difference starts on this line.
          * @param pos2 The position on which the difference ends on this line.
          */
        public Part(int type, int line, int pos1, int pos2) {
            if (type > 2 || type < 0) {
                throw new IllegalArgumentException("Bad Difference type = "+type);
            }
            this.type = type;
            this.line = line;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }
        
        /**
          * Get the difference type. It's one of <a href="#DELETE">DELETE</a>,
          * <a href="#ADD">ADD</a> or <a href="#CHANGE">CHANGE</a>.
          */
        public int getType() {
            return this.type;
        }
    
        /**
          * Get the line number.
          */
        public int getLine() {
            return this.line;
        }
        
        /**
          * Get the position on which the difference starts on this line.
          */
        public int getStartPosition() {
            return this.pos1;
        }
        
        /**
          * Get the position on which the difference ends on this line.
          */
        public int getEndPosition() {
            return this.pos2;
        }
        
        /**
         * Set the text content of the difference.
         *
        public void setText(String text) {
            this.text = text;
        }
        
        /**
         * Get the text content of the difference.
         *
        public String getText() {
            return text;
        }
         */
        
    }
    
}
