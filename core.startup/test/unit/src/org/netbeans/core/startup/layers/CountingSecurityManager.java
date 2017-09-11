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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.netbeans.core.startup.InstalledFileLocatorImpl;
import org.openide.modules.Places;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class CountingSecurityManager extends SecurityManager implements Callable<Integer> {
    private static int cnt;
    private static StringWriter msgs;
    private static PrintWriter pw;
    private static String prefix;
    private static Map<String,Exception> who = new HashMap<String, Exception>();
    private static Set<String> allowed = Collections.emptySet();
    private static SecurityManager man;
    private static Mode mode;
    static boolean acceptAll;

    public enum Mode {
        CHECK_READ, CHECK_WRITE
    };
    
    public static void initialize(String prefix, Mode mode, Set<String> allowedFiles) {
        System.setProperty("counting.security.disabled", "true");

        if (System.getSecurityManager() instanceof CountingSecurityManager) {
            // ok
        } else {
            System.setSecurityManager(new CountingSecurityManager());
        }
        setCnt(0);
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
        CountingSecurityManager.prefix = prefix;
        CountingSecurityManager.mode = mode;
        allowed = allowedFiles;

        Logger.getLogger("org.netbeans.TopSecurityManager").setLevel(Level.OFF);
        System.setProperty("org.netbeans.TopSecurityManager.level", "3000");
        System.setProperty("counting.security.disabled", "false");
    }

    static void assertReflection(int maxCount, String whitelist) {
        System.setProperty("counting.reflection.whitelist", whitelist);
        System.getSecurityManager().checkMemberAccess(null, maxCount);
        System.getProperties().remove("counting.reflection.whitelist");
    }

    @Override
    public String toString() {
        return msgs.toString();
    }

    public Integer call() throws Exception {
        return cnt;
    }

    public static boolean isEnabled() {
        return System.getSecurityManager() instanceof Callable<?>;
    }
    
    public static void assertCounts(String msg, int expectedCnt) throws Exception {
        int c = (Integer)((Callable<?>)System.getSecurityManager()).call();
        Assert.assertEquals(msg + "\n" + System.getSecurityManager().toString(), expectedCnt, c);
        setCnt(0);
        msgs = new StringWriter();
        pw = new PrintWriter(msgs);
    }

    /**
     * @return the cnt
     */
    public static int getCnt() {
        return cnt;
    }

    /**
     * @param aCnt the cnt to set
     */
    public static void setCnt(int aCnt) {
        cnt = aCnt;
    }

    @Override
    public void checkPermission(Permission p) {
        if (isDisabled()) {
            return;
        }
        if (p instanceof RuntimePermission && "setSecurityManager".equals(p.getName())) {
            try {
                ClassLoader l = Thread.currentThread().getContextClassLoader();
                Class<?> manClass = Class.forName("org.netbeans.TopSecurityManager", false, l);
                man = (SecurityManager) manClass.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            throw new SecurityException();
        }
    }

    @Override
    public final void checkPropertyAccess(String x) {
        if (man != null) {
            man.checkPropertyAccess(x);
        }
    }
    
    @Override
    public void checkRead(String file) {
        if (mode == Mode.CHECK_READ && acceptFileRead(file)) {
            String off = System.getProperty("counting.off");
            if ("true".equals(off)) {
                return;
            }
            
            String dirs = System.getProperty("netbeans.dirs");
            if (dirs == null && !acceptAll) {
                // not initialized yet
                return;
            }

            setCnt(getCnt() + 1);
            pw.println("checkRead: " + file);
            if (who.get(file) == null) {
                Exception now = new Exception("checkRead: " + file);
                who.put(file, now);
                now.printStackTrace(pw);
                pw.flush();
            }
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        /*
        if (file.startsWith(prefix)) {
            cnt++;
            pw.println("checkRead2: " + file);
        }
         */
    }

    private void assertMembers(int cnt) {
        String res = System.getProperty("counting.reflection.whitelist");
        if (res == null) {
            Assert.fail("Please provide whitelist: " + res);
        }
        Properties okAccess = new Properties();
        try {
            okAccess.load(CountingSecurityManager.class.getResourceAsStream(res));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        int myCnt = 0;
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        Set<Who> m;
        synchronized (members) {
            m = new TreeSet<Who>(members.values());
        }
        for (Who wh : m) {
            if (wh.isIgnore()) {
                continue;
            }
            String howMuchIsOK = okAccess.getProperty(wh.clazz.getName());
            if (howMuchIsOK != null && Integer.parseInt(howMuchIsOK) >= wh.count) {
                continue;
            }

                myCnt += wh.count;
            wh.printStackTrace(p);
            wh.count = 0;
        }
        if (myCnt > cnt) {
            Assert.fail("Expected at much " + cnt + " reflection efforts, but was: " + myCnt + "\n" + w);
        }
    }

    private final Map<Class,Who> members = Collections.synchronizedMap(new HashMap<Class, Who>());
    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        if (clazz == null) {
            assertMembers(which);
        }

        Who w = members.get(clazz);
        if (w == null) {
            w = new Who(clazz);
            members.put(clazz, w);
        }
        w.count++;
    }

    private static class Who extends Exception implements Comparable<Who> {
        int hashCode;
        final Class<?> clazz;
        int count;

        public Who(Class<?> who) {
            super("");
            this.clazz = who;
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            s.println(clazz.getName() + "=" + count);
            super.printStackTrace(s);
        }

        @Override
        public int hashCode() {
            if (hashCode != 0) {
                return hashCode;
            }
            hashCode = clazz.hashCode();
            for (StackTraceElement stackTraceElement : getStackTrace()) {
                hashCode = hashCode * 2 + stackTraceElement.hashCode();
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Who other = (Who) obj;
            if (this.clazz != other.clazz) {
                return false;
            }
            if (this.hashCode() != other.hashCode()) {
                return false;
            }
            return Arrays.equals(getStackTrace(), other.getStackTrace());
        }

        public int compareTo(Who o) {
            if (o == this) {
                return 0;
            }
            if (o.count < this.count) {
                return -1;
            }
            if (o.count > this.count) {
                return 1;
            }
            return this.clazz.getName().compareTo(o.clazz.getName());
        }

        private boolean isIgnore() {
            if (clazz.getName().startsWith("sun.reflect.Generated")) {
                return true;
            }
            if (clazz.getName().startsWith("$Proxy")) {
                return true;
            }
            if (clazz.getName().startsWith("org.apache.tools.ant.")) {
                return true;
            }
            if (clazz.getName().startsWith("sun.nio.")) {
                return true;
            }

            for (StackTraceElement stackTraceElement : getStackTrace()) {
                if (stackTraceElement.getClassName().contains("CountingSecurityManager")) {
                    continue;
                }
                if (stackTraceElement.getClassName().equals("java.lang.Class")) {
                    continue;
                }
                if (stackTraceElement.getClassName().startsWith("java.lang.Thread")) {
                    if (stackTraceElement.getMethodName().equals("auditSubclass")) {
                        return true;
                    }
                    continue;
                }
                if (stackTraceElement.getClassName().startsWith("java.security.AccessController")) {
                    continue;
                }
                if (stackTraceElement.getClassName().equals("sun.swing.SwingLazyValue")) {
                    // ignore createValue method
                    return true;
                }
                if (
                        stackTraceElement.getClassName().equals("java.awt.Component") &&
                        stackTraceElement.getMethodName().equals("isCoalesceEventsOverriden")
                ) {
                    return true;
                }
                if (stackTraceElement.getClassName().startsWith("java.util.ResourceBundle")) {
                    // ignore these invocations
                    return true;
                }
                if (stackTraceElement.getClassName().equals("org.netbeans.jellytools")) {
                    // ignore these invocations
                    return true;
                }
                if (stackTraceElement.getClassName().equals("org.openide.util.lookup.MetaInfServicesLookup$P")) {
                    // ignore these invocations
                    return true;
                }
                if (stackTraceElement.getClassName().equals("org.openide.util.WeakListenerImpl$ListenerReference")) {
                    // ignore: removeXYZListener is done using reflection
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        //setCnt(getCnt() + 1);
        //pw.println("Fd: " + fd);
    }

    @Override
    public void checkWrite(String file) {
        if (mode == Mode.CHECK_WRITE && acceptFileWrite(file)) {
            setCnt(getCnt() + 1);
            pw.println("checkWrite: " + file);
            if (who.get(file) == null) {
                Exception now = new Exception("checkWrite: " + file);
                who.put(file, now);
                now.printStackTrace(pw);
            }
        }
    }

    @Override
    public void checkDelete(String file) {
        if (mode == Mode.CHECK_WRITE && acceptFileWrite(file)) {
            setCnt(getCnt() + 1);
            pw.println("checkDelete: " + file);
        }
    }
    
    private boolean acceptFileWrite(String file) {
        String ud = System.getProperty("netbeans.user");
        if (ud == null) {
            // still initializing
            return false;
        }
        if (!file.startsWith(ud)) {
            return false;
        }

        String f = file.substring(ud.length()).replace(File.separatorChar, '/');
        if (f.contains("config/Modules")) {
            return false;
        }
        if (f.contains("config/Windows2Local")) {
            return false;
        }
        if (f.endsWith(".hg")) {
            try {
                Class<?> ref = Class.forName("org.netbeans.modules.versioning.util.Utils", true, Thread.currentThread().getContextClassLoader());
                Field unver = ref.getDeclaredField("unversionedFolders");
                unver.setAccessible(true);
                unver.set(null, new File[]{new File(ud).getParentFile()});
                return false;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        if (file.startsWith(ud)) {
            if (f.startsWith("/")) {
                f = f.substring(1);
            }
            if (allowed.contains(f)) {
                return false;
            }
        }

        return prefix == null || file.startsWith(prefix);
    }

    private boolean acceptFileRead(String file) {
        if (prefix != null && !file.startsWith(prefix)) {
            return false;
        }
        if (acceptAll) {
            return true;
        }
        
        if (file.contains("jna")) {
            return false;
        }
        for (Class c : this.getClassContext()) {
            if (c.getName().equals(InstalledFileLocatorImpl.class.getName())) {
                if (file.startsWith(Places.getCacheDirectory().getPath())) {
                    return false;
                }
                if (file.equals(System.getProperty("netbeans.home"))) {
                    return false;
                }
                if (file.equals(System.getProperty("netbeans.home") + File.separator + "update_tracking")) {
                    return false;
                }
                if (file.equals(System.getProperty("netbeans.user"))) {
                    return false;
                }
                return true;
            }
        }
        
        if (!file.endsWith(".jar")) {
            return false;
        }
        if (file.endsWith("tests.jar")) {
            return false;
        }
        if (file.endsWith("ant-javafx.jar")) {
            return false;
        }
        if (file.endsWith("org-netbeans-modules-nbjunit.jar")) {
            return false;
        }
        if (file.startsWith(System.getProperty("java.home").replaceAll("[/\\\\][^/\\\\]*$", ""))) {
            return false;
        }
        if (file.startsWith("/usr/jdk/packages/javax.help-")) {
            // ignore javahelp location on solaris
            return false;
        }
        if (file.startsWith(System.getProperty("netbeans.home") + File.separator + "lib")) {
            return false;
        }
        if (file.startsWith(System.getProperty("netbeans.home") + File.separator + "core")) {
            return false;
        }
        String dirs = System.getProperty("netbeans.dirs");
        if (dirs != null) {
            for (String dir : dirs.split(File.pathSeparator)) {
                if (file.startsWith(dir + File.separator + "lib")) {
                    return false;
                }
                if (file.startsWith(dir + File.separator + "core")) {
                    return false;
                }
            }
        }
        // mac osx
        dirs = System.getProperty("java.ext.dirs");
        if (dirs != null) {
            for (String dir : dirs.split(File.pathSeparator)) {
                if (file.startsWith(dir)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void checkExec(String cmd) {
        if (cmd.contains("chmod")) {
            return;
        }
        if (cmd.equals("hg")) {
            return;
        }
        if (cmd.endsWith("/hg")) {
            return;
        }
        if (cmd.endsWith("hg.exe")) {
            return;
        }

        super.checkExec(cmd);
        setCnt(getCnt() + 1);
        pw.println("checkExec: " + cmd);
        new Exception().printStackTrace(pw);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
    }

    /**
     * @return the disabled
     */
    private static boolean isDisabled() {
        return Boolean.getBoolean("counting.security.disabled");
    }
}
