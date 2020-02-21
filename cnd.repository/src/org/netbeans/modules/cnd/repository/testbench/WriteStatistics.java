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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class for gathering recent write statistics
 */
public final class WriteStatistics {
    
    private static final WriteStatistics instance = new WriteStatistics();
    
    /** Private constructor to prevent external creation */
    private WriteStatistics() {
    }
    
    public static WriteStatistics instance() {
	return instance;
    }
    
    private long writeStatIntervalStart = 0;
    private int writeStatInterval = 1000;
    private int writeCount = 0;
    //private int[] writesPerInterval = new int[10];
    private long totalWriteCount = 0;

    private final Object utfLock = new Object();
    private boolean utfHookSet = false;
    private Map<String, Integer> utfStacks = new HashMap<String, Integer>();
    private Map<String, Integer> utfPaths = new HashMap<String, Integer>();

    public void updateOnWriteUTF(CharSequence str) {
        if (isFile(str)) {
            updateUTFStatistics(str);
            synchronized (utfLock) {
                if (!utfHookSet) {
                    utfHookSet = true;
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            printUTFStatistics();
                        }
                    });
                }
            }
        }
    }

    private void updateUTFStatistics(CharSequence str) {
        synchronized (utfLock) {
            // by path
            {
                String key = str.toString();
                Integer cnt = utfPaths.get(key);
                utfPaths.put(key, (cnt == null) ? 1 : cnt.intValue() + 1);
            }
            // by stack
            {
                StringBuilder stackText = new StringBuilder();
                for (StackTraceElement stackEl : filterUTFStack(Thread.currentThread().getStackTrace())) {
                    stackText.append(stackEl.getClassName()).append('.').append(stackEl.getMethodName()); //NOI18N
                    stackText.append('(').append(stackEl.getFileName()).append(':').append(stackEl.getLineNumber()).append(")\n"); //NOI18N
                }
                String key = stackText.toString();
                Integer cnt = utfStacks.get(key);
                utfStacks.put(key, (cnt == null) ? 1 : cnt.intValue() + 1);
            }
        }
    }

    private List<StackTraceElement> filterUTFStack(StackTraceElement[] stack) {
        List<StackTraceElement> result = new ArrayList<StackTraceElement>();
        boolean wasFound = false;
        for (int i = 3; i < stack.length; i++) {
            boolean isFound = stack[i].getClassName().contains(".cnd.modelimpl."); // NOI18N
            if (wasFound) {
                if (isFound) {
                    result.add(stack[i]);
                } else {
                    break;
                }
            } else {
                result.add(stack[i]);
                wasFound = isFound;
            }
        }
        return result;
    }

    private void printUTFStatistics() {
        synchronized (utfLock) {
            // By path
            {
                List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(utfPaths.size());
                entries.addAll(utfPaths.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });
                System.out.printf("\n\nUTF/path start ===\n"); //NOI18N
                int total = 0;
                for (Map.Entry<String, Integer> entry : entries) {
                    int cnt = entry.getValue().intValue();
                    System.out.printf("UTF/path %8d %s\n", cnt, entry.getKey()); //NOI18N
                    total += cnt;
                }
                System.out.printf("UTF/path %8d %s\n", total, "TOTAL"); //NOI18N
                System.out.printf("\nUTF/path end ===\n\n"); //NOI18N
            }
            // By stack
            {
                List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(utfStacks.size());
                entries.addAll(utfStacks.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                });
                System.out.printf("\n\nUTF/stack start ===\n"); //NOI18N
                int total = 0;
                for (Map.Entry<String, Integer> entry : entries) {
                    int cnt = entry.getValue().intValue();
                    String stack = entry.getKey();
                    System.out.printf("\nUTF/stack %8d\n%s\n", cnt, stack); //NOI18N
                    total += cnt;
                }
                System.out.printf("UTF/stack %8d\n%s\n", total, "TOTAL"); //NOI18N
                System.out.printf("\nUTF/stack end ===\n\n"); //NOI18N
            }
        }
    }

    private static boolean isFile(CharSequence str) {
        if (str != null && str.length() > 0) {
            if (str.charAt(0) == '/') { // Unix only so far
                return true;
            }
        }
        return false;
    }

    public void update(int increment) {
	totalWriteCount += increment;
	long currTime = System.currentTimeMillis();
	if( this.writeStatIntervalStart == 0 ) {	// called first time
	    writeCount = increment;
	    writeStatIntervalStart = System.currentTimeMillis();
	} 
	else if( currTime - writeStatIntervalStart < writeStatInterval ) {
	    writeCount += increment;
	} 
	else {
	    int currentWPS = (int) (1000L * writeCount / (currTime - writeStatIntervalStart));
	    writeStatIntervalStart = currTime;
//	    for( int i = 1; i < writesPerInterval.length; i++ ) {
//		writesPerInterval[i-1] = writesPerInterval[i];
//	    }
//	    writesPerInterval[writesPerInterval.length-1] = currentWPS;
	    if( Stats.writeStatistics ) {
//		System.err.printf("Write statistics\n");
//		for (int i = 0; i < writesPerInterval.length; i++) {
//		    System.err.printf("\t%s %d WPS\n", baseFile.getName(), writesPerInterval[i]);
//		}
		System.err.printf("\tcurrent writes: %4d current WPS: %4d  total writes: %8d \n", 
			writeCount, currentWPS, totalWriteCount);
	    }
	    writeCount = increment;
	}
    }
}
