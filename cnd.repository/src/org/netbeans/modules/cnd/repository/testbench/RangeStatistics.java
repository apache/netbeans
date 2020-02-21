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

package org.netbeans.modules.cnd.repository.testbench;

import java.io.*;
import java.util.*;

/**
 * Collects a simple statistics
 */
public class RangeStatistics extends BaseStatistics<Integer> {


    protected int rangeCount;
    
    public RangeStatistics(String text, int level) {
	this(text, level, 10);
    }
    
    public RangeStatistics(String text, int level, int rangeCount) {
	super(text, level);
	this.rangeCount = rangeCount;
    }

    public void consume(int value) {
	consume(value, value);
    }
    
    @Override
    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	//ps.printf("%s %8d min    %8d max    %8d avg\n", text, min, max, avg);	// NOI18N
        ps.printf("%s %8d min    %8d max    %8d avg    %8d cnt    %8d sum\n", text, min, max, avg, cnt, sum);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    @Override
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	if( level > LEVEL_MEDIUM || values.size() <= rangeCount ) {
	    printDistributionDetailed(ps);
	}
	else {
	    printDistributionGrouped(ps);
	}
    }
    

    static private class Range { 

	/** range start (inclusive) */
	public int from;

	/** range start (inclusive) */
	public int to;

	/** count of values that are in range */
	public int cnt;

	public Range(int from, int to) {
	    this.from = from;
	    this.to = to;
	}
	
        @Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(from);
	    sb.append('-');
	    sb.append(to);
	    sb.append(": "); // NOI18N
	    sb.append(cnt);
	    return sb.toString();
	}
    }
    
    private Range[] ranges;
    
    private void createRanges() {
	
	int[] valuesArray = new int[values.size()];
	int pos = 0;
	for( Map.Entry<Integer, Integer> entry : values.entrySet() ) {
	    valuesArray[pos++] = entry.getKey();
	}

	ranges = new Range[rangeCount];
	int rangeSize = values.size() / rangeCount + ( values.size() % rangeCount == 0 ? 0 : 1 );
	
	for( int i = 0; i < ranges.length; i++ ) {
	    int from = Math.min(rangeSize*i, valuesArray.length-1);
	    int to = Math.min(from + rangeSize - 1, valuesArray.length-1);
	    try {
		ranges[i] = new Range(valuesArray[from], valuesArray[to]);
	    }
	    catch( ArrayIndexOutOfBoundsException e ) {
		System.err.printf("i=%d from=%d to=%d valuesArray.length=%d\n", i, from, to, valuesArray.length);
		e.printStackTrace(System.err);
	    }
	    catch( Exception e ) {
		e.printStackTrace(System.err);
	    }
	}
    }
    
    private Range getRange(int value) {
	if( ranges == null ) {
	    createRanges();
	}
	for (int i = 0; i < ranges.length; i++) {
	    if( ranges[i].from <= value && value <= ranges[i].to ) {
		return ranges[i];
	    }
	}
	throw new IllegalArgumentException("Value " + value + " are out of range " + min + '-' + max); // NOI18N
    }
    
    private void printDistributionGrouped(PrintStream ps) {
	
	for( Map.Entry<Integer, Integer> entry : values.entrySet() ) {
	    Range range = getRange(entry.getKey());
	    range.cnt += entry.getValue();
	}

	int maxFrom = 0, maxTo = 0, maxCnt = 0;
	for (int i = 0; i < ranges.length; i++) {
	    maxFrom = Math.max(maxCnt, ranges[i].from);
	    maxTo = Math.max(maxCnt, ranges[i].to);
	    maxCnt = Math.max(maxCnt, ranges[i].cnt);
	}
	maxFrom = (int) Math.log10(maxFrom) + 1;
	maxTo = (int) Math.log10(maxTo) + 1;
	maxCnt = (int) Math.log10(maxCnt) + 1;
	
	StringBuilder format = new StringBuilder("\t%"); // NOI18N
	format.append(maxFrom);
	format.append("d - %"); // NOI18N
	format.append(maxTo);
	format.append("d   %"); // NOI18N
	format.append(maxCnt);
	format.append("d   %2d%%\n"); // NOI18N

	
	for (int i = 0; i < ranges.length; i++) {
	    if( ranges[i].cnt > 0 ) {
		//ps.printf("\t%8d - %8d %8d\n", ranges[i].from, ranges[i].to, ranges[i].cnt);	// NOI18N
		int percent =  ranges[i].cnt*100/this.cnt;
		ps.printf(format.toString(), ranges[i].from, ranges[i].to, ranges[i].cnt, percent);	// NOI18N
	    }
	}
    }
}
