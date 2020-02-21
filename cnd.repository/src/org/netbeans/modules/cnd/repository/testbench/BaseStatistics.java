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

import java.io.PrintStream;
import java.util.*;

/**
 * Base class for collecting simple statistics
 */
public class BaseStatistics<K extends Comparable<?>> {

    protected int min = 0;
    protected int max = 0;
    protected int cnt = 0;
    
    protected int sum;
    
    protected String text;
    
    protected final Map<K, Integer> values = new TreeMap<K, Integer>();
    
    protected int level;
    
    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_MINUMUN = 1;
    public static final int LEVEL_MEDIUM = 2;
    public static final int LEVEL_MAXIMUM = 3;
    
    public BaseStatistics(String text, int level) {
	this.text = text;
	this.level = level;
    }    
    
    public void consume(K key, int value) {
	if( value > max ) {
	    max = value;
	}
	if( value < min ) {
	    min = value;
	}
	cnt++;
	sum += value;
	if( values != null ) {
	    Integer count = values.get(key);
	    values.put(key, Integer.valueOf((count == null) ? 1 : count.intValue() + 1));
	}
    }    

    public void print(PrintStream ps) {
	int avg = (cnt == 0) ? 0 : sum / cnt;
	ps.printf("%s %8d min    %8d max    %8d avg    %8d cnt    %8d sum\n", text, min, max, avg, cnt, sum);	// NOI18N
	if( values != null ) {
	    printDistribution(ps);
	}
    }
    
    protected void printDistributionDetailed(PrintStream ps) {
        int maxKeyLen = 0;
        String alignment = "-"; // NOI18N
	for( Map.Entry<K, Integer> entry : values.entrySet() ) {
            K key = entry.getKey();
            int currKeyLen;
            if( key == null ) {
                currKeyLen = 4; // "null"
            } else if( key instanceof Number ) { // can't use K.class  :(
                maxKeyLen = 8;
                alignment = "";
                break;
            } else {
                currKeyLen = key.toString().length();
            }
            if( currKeyLen > maxKeyLen ) {
                maxKeyLen = currKeyLen;
            }
	}
        String format = "\t%" + alignment + maxKeyLen + "s %8d\n"; // NOI18N
	for( Map.Entry<K, Integer> entry : values.entrySet() ) {
	    ps.printf(format, entry.getKey(), entry.getValue());	// NOI18N
	}
    }
    
    protected void printDistribution(PrintStream ps) {
	ps.printf("\tDistribution:\n");	// NOI18N
	printDistributionDetailed(ps);
    }

    public void clear() {
        min = max = cnt = sum = 0;
        values.clear();
    }
    
}
