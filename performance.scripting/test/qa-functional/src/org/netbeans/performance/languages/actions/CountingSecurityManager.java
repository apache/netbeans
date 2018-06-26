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

package org.netbeans.performance.languages.actions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Permission;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Assert;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public final class CountingSecurityManager extends SecurityManager {
    private static int cnt;
    private static StringWriter msgs;
    private static PrintWriter pw;
    private static String prefix = "NONE";
    
    public static void register() {
        initialize("NONE");
    }
    
    public static void initialize(String prefix) {
        Assert.assertNotNull(prefix);
        
        if (! (System.getSecurityManager() instanceof CountingSecurityManager)) {
            setAllowedReplace(true);
            System.setSecurityManager(new CountingSecurityManager());
            setAllowedReplace(false);
        }
        if (!System.getSecurityManager().getClass().getName().equals(CountingSecurityManager.class.getName())) {
            throw new IllegalStateException("Wrong security manager: " + System.getSecurityManager());
        }
        cnt = 0;
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        Statistics.reset();
        CountingSecurityManager.prefix = prefix;
        try {
            CountingSecurityManager.prefix = new File(prefix).getCanonicalPath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        System.err.println("setting prefix to " + CountingSecurityManager.prefix);
    }
    
    public static void assertCounts(String msg, int expectedCnt, AtomicLong property) {
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        Statistics.getDefault().print(pw);

        property.set(cnt);
        
        if (cnt < expectedCnt / 10) {
            throw new AssertionError("Too small expectations:\n" + msg + "\n" + msgs + " exp: " + expectedCnt + " was: " + cnt);
        }
        if (expectedCnt < cnt) {
            throw new AssertionError(msg + " exp: " + expectedCnt + " was: " + cnt + "\n" + msgs);
        }
        cnt = 0;
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        Statistics.getDefault().print(pw);
    }

    @Override
    public void checkRead(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            Statistics.fileIsDirectory(file);
//            pw.println("checkRead: " + file);
//            new Exception().printStackTrace(pw);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (file.startsWith(prefix)) {
            cnt++;
            Statistics.fileIsDirectory(file);
            pw.println("checkRead2: " + file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        cnt++;
        pw.println("Fd: " + fd);
    }

    @Override
    public void checkWrite(String file) {
        if (file.startsWith(prefix)) {
            cnt++;
            Statistics.fileIsDirectory(file);
            pw.println("checkWrite: " + file);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        if (perm.getName().equals("setSecurityManager")) { // NOI18N - hardcoded in java.lang
            if (!isAllowedReplace()) {
                throw new SecurityException();
            }
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }

    private static boolean isAllowedReplace() {
        return Boolean.getBoolean("CountingSecurityManager.allowReplace");
    }

    private static void setAllowedReplace(boolean aAllowedReplace) {
        System.setProperty("CountingSecurityManager.allowReplace", String.valueOf(aAllowedReplace));
    }

    /**
     * Collects data and print them when JVM shutting down.
     * 
     * @author Pavel Flaška
     */
    private static class Statistics implements Comparator<Map.Entry<String,Integer>> {

        private static final boolean streamLog = false;
        private static final boolean dirLog = true;
        private static final boolean streamCreation = false;
        /** singleton instance */
        private static Statistics INSTANCE;
        private Map<String, Integer> isDirInvoc = Collections.synchronizedMap(new HashMap<String, Integer>());
        private Map<String, Integer> stacks = Collections.synchronizedMap(new HashMap<String, Integer>());

        private Statistics() {
        }

        /**
         * Get the class instance.
         * 
         * @return singleton of Statistics class.
         */
        static synchronized Statistics getDefault() {
            if (INSTANCE == null) {
                INSTANCE = new Statistics();
            }
            return INSTANCE;
        }

        static synchronized void reset() {
            INSTANCE = null;
        }

        /**
         * Counts in isDirectory() call on <tt>file</tt>.
         * 
         * @param file  file name
         */
        public static void fileIsDirectory(String file) {
            if (!dirLog) {
                return;
            }
            Integer i = Statistics.getDefault().isDirInvoc.get(file);
            if (i == null) {
                i = 1;
            } else {
                i++;
            }
            Statistics.getDefault().isDirInvoc.put(file, i);

            ////////////////////
            StringBuilder sb = new StringBuilder(300);
            StackTraceElement[] ste = Thread.currentThread().getStackTrace();
            for (i = 2; i < ste.length; i++) {
                sb.append(ste[i].toString()).append('\n');
            }
            String s = sb.toString();
            i = Statistics.getDefault().stacks.get(s);
            if (i == null) {
                i = 1;
            } else {
                i++;
            }
            Statistics.getDefault().stacks.put(s, i);
        }

        public int compare(Map.Entry<String,Integer> one, Map.Entry<String,Integer> two) {
            int r = one.getValue().compareTo(two.getValue());
            if (r == 0) {
                return one.getKey().compareTo(two.getKey());
            } else {
                return r;
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // private members
        void print(PrintWriter out) {
            synchronized (isDirInvoc) {
                TreeSet<Map.Entry<String,Integer>> sort = new TreeSet<Map.Entry<String,Integer>>(Collections.reverseOrder(this));
                sort.addAll(isDirInvoc.entrySet());
                int cnt = 0;
                for (Map.Entry<String, Integer> e : sort) {
                    if (cnt++ > 100) {
                        break;
                    }
                    String s = e.getKey();
                    out.printf("%4d", isDirInvoc.get(s));
                    out.println("; " + s);
                }
            }
            int absoluteStacks = 0;
            synchronized (stacks) {
                for (String s : stacks.keySet()) {
                    int value = stacks.get(s);
                    absoluteStacks += value;
                }
                int min = absoluteStacks / 50;
                for (String s : stacks.keySet()) {
                    int value = stacks.get(s);
                    if (value > min) {
                        out.printf("count %5d; Stack:\n", value);
                        for (String line : s.split("\n")) {
                            out.printf("    %s\n", line);
                        }
                    }
                }
            }
            out.println("Total stacks recorded: " + absoluteStacks);
        }
    }
    
}
