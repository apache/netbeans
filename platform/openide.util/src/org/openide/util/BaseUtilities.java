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

package org.openide.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import org.openide.util.lookup.implspi.ActiveQueue;

/**
 * General purpose utility methods.
 * 
 * @author Jan Palka, Ian Formanek, Jaroslav Tulach
 */
public abstract class BaseUtilities {
    private static final Logger LOG = Logger.getLogger(BaseUtilities.class.getName());

    /** Operating system is Windows NT. */
    public static final int OS_WINNT = 1/* << 0*/;

    /** Operating system is Windows 95. */
    public static final int OS_WIN95 = OS_WINNT << 1;

    /** Operating system is Windows 98. */
    public static final int OS_WIN98 = OS_WIN95 << 1;

    /** Operating system is Solaris. */
    public static final int OS_SOLARIS = OS_WIN98 << 1;

    /** Operating system is Linux. */
    public static final int OS_LINUX = OS_SOLARIS << 1;

    /** Operating system is HP-UX. */
    public static final int OS_HP = OS_LINUX << 1;

    /** Operating system is IBM AIX. */
    public static final int OS_AIX = OS_HP << 1;

    /** Operating system is SGI IRIX. */
    public static final int OS_IRIX = OS_AIX << 1;

    /** Operating system is Sun OS. */
    public static final int OS_SUNOS = OS_IRIX << 1;

    /** Operating system is Compaq TRU64 Unix */
    public static final int OS_TRU64 = OS_SUNOS << 1;

    /** @deprecated please use OS_TRU64 instead */
    @Deprecated
    public static final int OS_DEC = OS_TRU64 << 1;

    /** Operating system is OS/2. */
    public static final int OS_OS2 = OS_DEC << 1;

    /** Operating system is Mac. */
    public static final int OS_MAC = OS_OS2 << 1;

    /** Operating system is Windows 2000. */
    public static final int OS_WIN2000 = OS_MAC << 1;

    /** Operating system is Compaq OpenVMS */
    public static final int OS_VMS = OS_WIN2000 << 1;

    /**
     *Operating system is one of the Windows variants but we don't know which
     *one it is
     */
    public static final int OS_WIN_OTHER = OS_VMS << 1;

    /** Operating system is unknown. */
    public static final int OS_OTHER = OS_WIN_OTHER << 1;

    /** Operating system is FreeBSD
     * @since 4.50
     */
    public static final int OS_FREEBSD = OS_OTHER << 1;
    
    /** Operating system is Windows Vista.
     * @since 7.17
     */
    public static final int OS_WINVISTA = OS_FREEBSD << 1;

    /** Operating system is one of the Unix variants but we don't know which
     * one it is.
     * @since 7.18
     */
    public static final int OS_UNIX_OTHER = OS_WINVISTA << 1;

    /** Operating system is OpenBSD.
     * @since 7.18
     */
    public static final int OS_OPENBSD = OS_UNIX_OTHER << 1;

    /** A mask for Windows platforms.
     * @deprecated Use {@link #isWindows()} instead.
     */
    @Deprecated
    public static final int OS_WINDOWS_MASK = OS_WINNT | OS_WIN95 | OS_WIN98 | OS_WIN2000 | OS_WINVISTA | OS_WIN_OTHER;

    /** A mask for Unix platforms.
     * @deprecated Use {@link #isUnix()} instead.
     */
    @Deprecated
    public static final int OS_UNIX_MASK = OS_SOLARIS | OS_LINUX | OS_HP | OS_AIX | OS_IRIX | OS_SUNOS | OS_TRU64 |
        OS_MAC | OS_FREEBSD | OS_OPENBSD | OS_UNIX_OTHER;

    /** The operating system on which NetBeans runs*/
    private static int operatingSystem = -1;

    // Package retranslation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private static final Object TRANS_LOCK = /* FB complains about both "TRANS_LOCK" and new String("TRANS_LOCK") */new Object();

    /** last used classloader or if run in test mode the TRANS_LOCK */
    private static Object transLoader;

    /** regular expression to with all changes */
    private static RE transExp;
    
    /**
     * Prevent subclassing outside package
     */
    private BaseUtilities() {}
    
    /**
     * Useful queue for all parts of system that use <code>java.lang.ref.Reference</code>s
     * together with some <code>ReferenceQueue</code> and need to do some clean up
     * when the reference is enqueued. Usually, in order to be notified about that, one
     * needs to either create a dedicated thread that blocks on the queue and is
     * <code>Object.notify</code>-ed, which is the right approach but consumes
     * valuable system resources (threads) or one can periodically check the content
     * of the queue by <code>RequestProcessor.Task.schedule</code> which is
     * completely wrong, because it wakes up the system every (say) 15 seconds.
     * In order to provide useful support for this problem, this queue has been
     * provided.
     * <P>
     * If you have a reference that needs cleanup, make it implement {@link Runnable}
     * and register it with the queue:
     * <PRE>
     * class MyReference extends WeakReference&lt;Thing&gt; implements Runnable {
     *     private final OtherInfo dataToCleanUp;
     *     public MyReference(Thing ref, OtherInfo data) {
     *         super(ref, Utilities.queue());
     *         dataToCleanUp = data;
     *     }
     *     public void run() {
     *         dataToCleanUp.releaseOrWhateverYouNeed();
     *     }
     * }
     * </PRE>
     * When the <code>ref</code> object is garbage collected, your run method
     * will be invoked by calling
     * <code>((Runnable) reference).run()</code>
     * and you can perform whatever cleanup is necessary. Be sure not to block
     * in such cleanup for a long time as this prevents other waiting references
     * from cleaning themselves up.
     * <P>
     * Do not call any <code>ReferenceQueue</code> methods. They
     * will throw exceptions. You may only enqueue a reference.
     * <p>
     * Be sure to call this method anew for each reference.
     * Do not attempt to cache the return value.
     * @return reference queue
     * @since 3.11
     */
    public static ReferenceQueue<Object> activeReferenceQueue() {
        return ActiveQueue.queue();
    }

    /** Get the operating system on which NetBeans is running.
    * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
    */
    public static int getOperatingSystem() {
        if (operatingSystem == -1) {
            String osName = System.getProperty("os.name");

            if ("Windows NT".equals(osName)) { // NOI18N
                operatingSystem = OS_WINNT;
            } else if ("Windows 95".equals(osName)) { // NOI18N
                operatingSystem = OS_WIN95;
            } else if ("Windows 98".equals(osName)) { // NOI18N
                operatingSystem = OS_WIN98;
            } else if ("Windows 2000".equals(osName)) { // NOI18N
                operatingSystem = OS_WIN2000;
            } else if ("Windows Vista".equals(osName)) { // NOI18N
                operatingSystem = OS_WINVISTA;
            } else if (osName.startsWith("Windows ")) { // NOI18N
                operatingSystem = OS_WIN_OTHER;
            } else if ("Solaris".equals(osName)) { // NOI18N
                operatingSystem = OS_SOLARIS;
            } else if (osName.startsWith("SunOS")) { // NOI18N
                operatingSystem = OS_SOLARIS;
            }
            // JDK 1.4 b2 defines os.name for me as "Redhat Linux" -jglick
            else if (osName.endsWith("Linux")) { // NOI18N
                operatingSystem = OS_LINUX;
            } else if ("HP-UX".equals(osName)) { // NOI18N
                operatingSystem = OS_HP;
            } else if ("AIX".equals(osName)) { // NOI18N
                operatingSystem = OS_AIX;
            } else if ("Irix".equals(osName)) { // NOI18N
                operatingSystem = OS_IRIX;
            } else if ("SunOS".equals(osName)) { // NOI18N
                operatingSystem = OS_SUNOS;
            } else if ("Digital UNIX".equals(osName)) { // NOI18N
                operatingSystem = OS_TRU64;
            } else if ("OS/2".equals(osName)) { // NOI18N
                operatingSystem = OS_OS2;
            } else if ("OpenVMS".equals(osName)) { // NOI18N
                operatingSystem = OS_VMS;
            } else if (osName.equals("Mac OS X")) { // NOI18N
                operatingSystem = OS_MAC;
            } else if (osName.startsWith("Darwin")) { // NOI18N
                operatingSystem = OS_MAC;
            } else if (osName.toLowerCase(Locale.US).startsWith("freebsd")) { // NOI18N 
                operatingSystem = OS_FREEBSD;
            } else if ("OpenBSD".equals(osName)) { // NOI18N
                operatingSystem = OS_OPENBSD;
            } else if (File.pathSeparatorChar == ':') { // NOI18N
                operatingSystem = OS_UNIX_OTHER;
            } else {
                operatingSystem = OS_OTHER;
            }
        }

        return operatingSystem;
    }

    /** Test whether NetBeans is running on some variant of Windows.
    * @return <code>true</code> if Windows, <code>false</code> if some other manner of operating system
    */
    public static boolean isWindows() {
        return (getOperatingSystem() & OS_WINDOWS_MASK) != 0;
    }

    /** Test whether NetBeans is running on Mac OS X.
     * @since 7.7
    * @return <code>true</code> if Mac, <code>false</code> if some other manner of operating system
    */
    public static boolean isMac() {
        return (getOperatingSystem() & OS_MAC) != 0;
    }

    /** Test whether NetBeans is running on some variant of Unix.
    * Linux is included as well as the commercial vendors and Mac OS X.
    * @return <code>true</code> some sort of Unix, <code>false</code> if some other manner of operating system
    */
    public static boolean isUnix() {
        return (getOperatingSystem() & OS_UNIX_MASK) != 0;
    }

    // only for UtilitiesTest purposes
    static void resetOperatingSystem() {
        operatingSystem = -1;
    }

    /** Test whether a given string is a valid Java identifier.
    * @param id string which should be checked
    * @return <code>true</code> if a valid identifier
    * @see SourceVersion#isIdentifier
    * @see SourceVersion#isKeyword
    */
    public static boolean isJavaIdentifier(String id) {
        if (id == null) {
            return false;
        }
        return SourceVersion.isIdentifier(id) && !SourceVersion.isKeyword(id);
    }

    /** Wrap multi-line strings (and get the individual lines).
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param breakIterator breaks original to chars, words, sentences, depending on what instance you provide.
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the lines after wrapping
    */
    public static String[] wrapStringToArray(
        String original, int width, BreakIterator breakIterator, boolean removeNewLines
    ) {
        if (original.length() == 0) {
            return new String[] { original };
        }

        String[] workingSet;

        // substitute original newlines with spaces,
        // remove newlines from head and tail
        if (removeNewLines) {
            original = trimString(original);
            original = original.replace('\n', ' ');
            workingSet = new String[] { original };
        } else {
            StringTokenizer tokens = new StringTokenizer(original, "\n"); // NOI18N
            int len = tokens.countTokens();
            workingSet = new String[len];

            for (int i = 0; i < len; i++) {
                workingSet[i] = tokens.nextToken();
            }
        }

        if (width < 1) {
            width = 1;
        }

        if (original.length() <= width) {
            return workingSet;
        }

widthcheck:  {
            boolean ok = true;

            for (int i = 0; i < workingSet.length; i++) {
                ok = ok && (workingSet[i].length() < width);

                if (!ok) {
                    break widthcheck;
                }
            }

            return workingSet;
        }

        java.util.ArrayList<String> lines = new java.util.ArrayList<String>();

        int lineStart = 0; // the position of start of currently processed line in the original string

        for (int i = 0; i < workingSet.length; i++) {
            if (workingSet[i].length() < width) {
                lines.add(workingSet[i]);
            } else {
                breakIterator.setText(workingSet[i]);

                int nextStart = breakIterator.next();
                int prevStart = 0;

                do {
                    while (((nextStart - lineStart) < width) && (nextStart != BreakIterator.DONE)) {
                        prevStart = nextStart;
                        nextStart = breakIterator.next();
                    }

                    if (nextStart == BreakIterator.DONE) {
                        nextStart = prevStart = workingSet[i].length();
                    }

                    if (prevStart == 0) {
                        prevStart = nextStart;
                    }

                    lines.add(workingSet[i].substring(lineStart, prevStart));

                    lineStart = prevStart;
                    prevStart = 0;
                } while (lineStart < workingSet[i].length());

                lineStart = 0;
            }
        }

        String[] s = new String[lines.size()];

        return lines.toArray(s);
    }

    /** trims String
    * @param s a String to trim
    * @return trimmed String
    */
    private static String trimString(String s) {
        int idx = 0;
        char c;
        final int slen = s.length();

        if (slen == 0) {
            return s;
        }

        do {
            c = s.charAt(idx++);
        } while (((c == '\n') || (c == '\r')) && (idx < slen));

        s = s.substring(--idx);
        idx = s.length() - 1;

        if (idx < 0) {
            return s;
        }

        do {
            c = s.charAt(idx--);
        } while (((c == '\n') || (c == '\r')) && (idx >= 0));

        return s.substring(0, idx + 2);
    }

    /** Wrap multi-line strings.
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param breakIterator algorithm for breaking lines
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the whole string with embedded newlines
    */
    public static String wrapString(String original, int width, BreakIterator breakIterator, boolean removeNewLines) {
        String[] sarray = wrapStringToArray(original, width, breakIterator, removeNewLines);
        StringBuilder retBuf = new StringBuilder();

        for (int i = 0; i < sarray.length; i++) {
            retBuf.append(sarray[i]);
            retBuf.append('\n');
        }

        return retBuf.toString();
    }

    /** Turn full name of an inner class into its pure form.
    * @param fullName e.g. <code>some.pkg.SomeClass$Inner</code>
    * @return e.g. <code>Inner</code>
    */
    public static String pureClassName(final String fullName) {
        final int index = fullName.indexOf('$');

        if ((index >= 0) && (index < fullName.length())) {
            return fullName.substring(index + 1);
        }

        return fullName;
    }

    /** Safe equality check.
    * The supplied objects are equal if: <UL>
    * <LI> both are <code>null</code>
    * <LI> both are arrays with same length and equal items (if the items are arrays,
    *      they are <em>not</em> checked the same way again)
    * <LI> the two objects are {@link Object#equals}
    * </UL>
    * This method is <code>null</code>-safe, so if one of the parameters is true and the second not,
    * it returns <code>false</code>.
    * <p>Use {@code java.util.Objects.deepEquals} in JDK 7.
    * @param  o1 the first object to compare
    * @param  o2 the second object to compare
    * @return <code>true</code> if the objects are equal
    */
    public static boolean compareObjects(Object o1, Object o2) {
        return compareObjectsImpl(o1, o2, 1);
    }

    /** Safe equality check with array recursion.
    * <p>Use {@code java.util.Objects.deepEquals} in JDK 7.
    * @param  o1 the first object to compare
    * @param  o2 the second object to compare
    * @param  checkArraysDepth the depth to which arrays should be compared for equality (negative for infinite depth, zero for no comparison of elements, one for shallow, etc.)
    * @return <code>true</code> if the objects are equal
    * @see #compareObjects(Object, Object)
    */
    public static boolean compareObjectsImpl(Object o1, Object o2, int checkArraysDepth) {
        // handle null values
        if (o1 == null) {
            return (o2 == null);
        } else if (o2 == null) {
            return false;
        }

        // handle arrays
        if (checkArraysDepth > 0) {
            if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
                // Note: also handles multidimensional arrays of primitive types correctly.
                // I.e. new int[0][] instanceof Object[]
                Object[] o1a = (Object[]) o1;
                Object[] o2a = (Object[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (!compareObjectsImpl(o1a[i], o2a[i], checkArraysDepth - 1)) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof byte[]) && (o2 instanceof byte[])) {
                byte[] o1a = (byte[]) o1;
                byte[] o2a = (byte[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof short[]) && (o2 instanceof short[])) {
                short[] o1a = (short[]) o1;
                short[] o2a = (short[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof int[]) && (o2 instanceof int[])) {
                int[] o1a = (int[]) o1;
                int[] o2a = (int[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof long[]) && (o2 instanceof long[])) {
                long[] o1a = (long[]) o1;
                long[] o2a = (long[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
                float[] o1a = (float[]) o1;
                float[] o2a = (float[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
                double[] o1a = (double[]) o1;
                double[] o2a = (double[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof char[]) && (o2 instanceof char[])) {
                char[] o1a = (char[]) o1;
                char[] o2a = (char[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            } else if ((o1 instanceof boolean[]) && (o2 instanceof boolean[])) {
                boolean[] o1a = (boolean[]) o1;
                boolean[] o2a = (boolean[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;

                if (l1 != l2) {
                    return false;
                }

                for (int i = 0; i < l1; i++) {
                    if (o1a[i] != o2a[i]) {
                        return false;
                    }
                }

                return true;
            }

            // else not array type
        }

        // handle common objects--non-arrays, or arrays when depth == 0
        return o1.equals(o2);
    }

    /** Assemble a human-presentable class name for a specified class.
    * Arrays are represented as e.g. <code>java.lang.String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getClassName(Class<?> clazz) {
        // if it is an array, get short name of element type and append []
        if (clazz.isArray()) {
            return getClassName(clazz.getComponentType()) + "[]"; // NOI18N
        } else {
            return clazz.getName();
        }
    }

    /** Assemble a human-presentable class name for a specified class (omitting the package).
    * Arrays are represented as e.g. <code>String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getShortClassName(Class<?> clazz) {
        // if it is an array, get short name of element type and append []
        if (clazz.isArray()) {
            return getShortClassName(clazz.getComponentType()) + "[]"; // NOI18N
        }

        String name = clazz.getName().replace('$', '.');

        return name.substring(name.lastIndexOf('.') + 1, name.length()); // NOI18N
    }

    /**
    * Convert an array of objects to an array of primitive types.
    * E.g. an <code>Integer[]</code> would be changed to an <code>int[]</code>.
    * @param array the wrapper array
    * @return a primitive array
    * @throws IllegalArgumentException if the array element type is not a primitive wrapper
    */
    public static Object toPrimitiveArray(Object[] array) {
        if (array instanceof Integer[]) {
            int[] r = new int[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Integer) array[i]) == null) ? 0 : (Integer) array[i];
            }

            return r;
        }

        if (array instanceof Boolean[]) {
            boolean[] r = new boolean[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Boolean) array[i]) == null) ? false : (Boolean) array[i];
            }

            return r;
        }

        if (array instanceof Byte[]) {
            byte[] r = new byte[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Byte) array[i]) == null) ? 0 : (Byte) array[i];
            }

            return r;
        }

        if (array instanceof Character[]) {
            char[] r = new char[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Character) array[i]) == null) ? 0 : (Character) array[i];
            }

            return r;
        }

        if (array instanceof Double[]) {
            double[] r = new double[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Double) array[i]) == null) ? 0 : (Double) array[i];
            }

            return r;
        }

        if (array instanceof Float[]) {
            float[] r = new float[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Float) array[i]) == null) ? 0 : (Float) array[i];
            }

            return r;
        }

        if (array instanceof Long[]) {
            long[] r = new long[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Long) array[i]) == null) ? 0 : (Long) array[i];
            }

            return r;
        }

        if (array instanceof Short[]) {
            short[] r = new short[array.length];
            int i;
            int k = array.length;

            for (i = 0; i < k; i++) {
                r[i] = (((Short) array[i]) == null) ? 0 : (Short) array[i];
            }

            return r;
        }

        throw new IllegalArgumentException();
    }

    /**
    * Convert an array of primitive types to an array of objects.
    * E.g. an <code>int[]</code> would be turned into an <code>Integer[]</code>.
    * @param array the primitive array
    * @return a wrapper array
    * @throws IllegalArgumentException if the array element type is not primitive
    */
    public static Object[] toObjectArray(Object array) {
        if (array instanceof Object[]) {
            return (Object[]) array;
        }

        if (array instanceof int[]) {
            int i;
            int k = ((int[]) array).length;
            Integer[] r = new Integer[k];

            for (i = 0; i < k; i++) {
                r[i] = ((int[]) array)[i];
            }

            return r;
        }

        if (array instanceof boolean[]) {
            int i;
            int k = ((boolean[]) array).length;
            Boolean[] r = new Boolean[k];

            for (i = 0; i < k; i++) {
                r[i] = ((boolean[]) array)[i];
            }

            return r;
        }

        if (array instanceof byte[]) {
            int i;
            int k = ((byte[]) array).length;
            Byte[] r = new Byte[k];

            for (i = 0; i < k; i++) {
                r[i] = ((byte[]) array)[i];
            }

            return r;
        }

        if (array instanceof char[]) {
            int i;
            int k = ((char[]) array).length;
            Character[] r = new Character[k];

            for (i = 0; i < k; i++) {
                r[i] = ((char[]) array)[i];
            }

            return r;
        }

        if (array instanceof double[]) {
            int i;
            int k = ((double[]) array).length;
            Double[] r = new Double[k];

            for (i = 0; i < k; i++) {
                r[i] = ((double[]) array)[i];
            }

            return r;
        }

        if (array instanceof float[]) {
            int i;
            int k = ((float[]) array).length;
            Float[] r = new Float[k];

            for (i = 0; i < k; i++) {
                r[i] = ((float[]) array)[i];
            }

            return r;
        }

        if (array instanceof long[]) {
            int i;
            int k = ((long[]) array).length;
            Long[] r = new Long[k];

            for (i = 0; i < k; i++) {
                r[i] = ((long[]) array)[i];
            }

            return r;
        }

        if (array instanceof short[]) {
            int i;
            int k = ((short[]) array).length;
            Short[] r = new Short[k];

            for (i = 0; i < k; i++) {
                r[i] = ((short[]) array)[i];
            }

            return r;
        }

        throw new IllegalArgumentException();
    }

    /**
    * Get the object type for given primitive type.
    *
    * @param c primitive type (e.g. <code>int</code>)
    * @return object type (e.g. <code>Integer</code>)
    */
    public static Class<?> getObjectType(Class<?> c) {
        if (!c.isPrimitive()) {
            return c;
        }

        if (c == Integer.TYPE) {
            return Integer.class;
        }

        if (c == Boolean.TYPE) {
            return Boolean.class;
        }

        if (c == Byte.TYPE) {
            return Byte.class;
        }

        if (c == Character.TYPE) {
            return Character.class;
        }

        if (c == Double.TYPE) {
            return Double.class;
        }

        if (c == Float.TYPE) {
            return Float.class;
        }

        if (c == Long.TYPE) {
            return Long.class;
        }

        if (c == Short.TYPE) {
            return Short.class;
        }

        throw new IllegalArgumentException();
    }

    /**
    * Get the primitive type for given object type.
    *
    * @param c object type (e.g. <code>Integer</code>)
    * @return primitive type (e.g. <code>int</code>)
    */
    public static Class<?> getPrimitiveType(Class<?> c) {
        if (!c.isPrimitive()) {
            return c;
        }

        if (c == Integer.class) {
            return Integer.TYPE;
        }

        if (c == Boolean.class) {
            return Boolean.TYPE;
        }

        if (c == Byte.class) {
            return Byte.TYPE;
        }

        if (c == Character.class) {
            return Character.TYPE;
        }

        if (c == Double.class) {
            return Double.TYPE;
        }

        if (c == Float.class) {
            return Float.TYPE;
        }

        if (c == Long.class) {
            return Long.TYPE;
        }

        if (c == Short.class) {
            return Short.TYPE;
        }

        throw new IllegalArgumentException();
    }

    /** Parses parameters from a given string in shell-like manner.
    * Users of the Bourne shell (e.g. on Unix) will already be familiar with the behavior.
    * For example, when using <code>org.openide.execution.NbProcessDescriptor</code> (Execution API)
    * you should be able to:
    * <ul>
    * <li>Include command names with embedded spaces, such as <code>c:\Program Files\jdk\bin\javac</code>.
    * <li>Include extra command arguments, such as <code>-Dname=value</code>.
    * <li>Do anything else which might require unusual characters or processing. For example:
    * <p><code>
    * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
    * </code>
    * <p>This example would create the following executable name and arguments:
    * <ol>
    * <li> <code>c:\program files\jdk\bin\java</code>
    * <li> <code>-Dmessage=Hello /\/\ there!</code>
    * <li> <code>-Xmx128m</code>
    * </ol>
    * Note that the command string does not escape its backslashes--under the assumption
    * that Windows users will not think to do this, meaningless escapes are just left
    * as backslashes plus following character.
    * </ul>
    * <em>Caveat</em>: even after parsing, Windows programs (such as the Java launcher)
    * may not fully honor certain
    * characters, such as quotes, in command names or arguments. This is because programs
    * under Windows frequently perform their own parsing and unescaping (since the shell
    * cannot be relied on to do this). On Unix, this problem should not occur.
    * @param s a string to parse
    * @return an array of parameters
    */
    public static String[] parseParameters(String s) {
        final int NULL = 0x0;
        final int IN_PARAM = 0x1;
        final int IN_DOUBLE_QUOTE = 0x2;
        final int IN_SINGLE_QUOTE = 0x3;
        ArrayList<String> params = new ArrayList<>(5);
        char c;

        int state = NULL;
        StringBuilder buff = new StringBuilder(20);
        int slength = s.length();

        for (int i = 0; i < slength; i++) {
            c = s.charAt(i);
            switch (state) {
                case NULL:
                    switch (c) {
                        case '\'':
                            state = IN_SINGLE_QUOTE;
                            break;
                        case '"':
                            state = IN_DOUBLE_QUOTE;
                            break;
                        default:
                            if (!Character.isWhitespace(c)) {
                                buff.append(c);
                                state = IN_PARAM;
                            }
                    }
                    break;
                case IN_SINGLE_QUOTE:
                    if (c != '\'') {
                        buff.append(c);
                    } else {
                        state = IN_PARAM;
                    }
                    break;
                case IN_DOUBLE_QUOTE:
                    switch (c) {
                        case '\\':
                            char peek = (i < slength - 1) ? s.charAt(i+1) : Character.MIN_VALUE;
                            if (peek == '"' || peek =='\\') {
                                buff.append(peek);
                                i++;
                            } else {
                                buff.append(c);
                            }
                            break;
                        case '"':
                            state = IN_PARAM;
                            break;
                        default:
                            buff.append(c);
                    }
                    break;
                case IN_PARAM:
                    switch (c) {
                        case '\'':
                            state = IN_SINGLE_QUOTE;
                            break;
                        case '"':
                            state = IN_DOUBLE_QUOTE;
                            break;
                        default:
                          if (Character.isWhitespace(c)) {
                              params.add(buff.toString());
                              buff.setLength(0);
                              state = NULL;
                          } else {
                              buff.append(c);
                          }
                    }
                    break;
            }
        }
        if (buff.length() > 0) {
            params.add(buff.toString());
        }

        return params.toArray(new String[0]);
    }

    /** Complementary method to parseParameters
     * @param params set of parameters
     * @return escaped parameters
     * @see #parseParameters
     */
    public static String escapeParameters(String[] params) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < params.length; i++) {
            escapeString(params[i], sb);
            sb.append(' ');
        }

        final int len = sb.length();

        if (len > 0) {
            sb.setLength(len - 1);
        }

        return sb.toString().trim();
    }

    /** Escapes one string
     * @see #escapeParameters
     */
    private static void escapeString(String s, StringBuffer sb) {
        if (s.length() == 0) {
            sb.append("\"\"");

            return;
        }

        boolean hasSpace = false;
        final int sz = sb.length();
        final int slen = s.length();
        char c;

        for (int i = 0; i < slen; i++) {
            c = s.charAt(i);

            if (Character.isWhitespace(c)) {
                hasSpace = true;
                sb.append(c);

                continue;
            }

            if (c == '\\') {
                sb.append('\\').append('\\');

                continue;
            }

            if (c == '"') {
                sb.append('\\').append('"');

                continue;
            }

            sb.append(c);
        }

        if (hasSpace) {
            sb.insert(sz, '"');
            sb.append('"');
        }
    }

    /**
     * Topologically sort some objects.
     * <p>There may not be any nulls among the objects, nor duplicates
     * (as per hash/equals), nor duplicates among the edge lists.
     * The edge map need not contain an entry for every object, only if it
     * has some outgoing edges (empty but not null map values are permitted).
     * The edge map shall not contain neither keys nor value entries for objects not
     * in the collection to be sorted, if that happens they will be ignored (since version 7.9).
     * <p>The incoming parameters will not be modified; they must not be changed
     * during the call and possible calls to TopologicalSortException methods.
     * The returned list will support modifications.
     * <p>There is a <em>weak</em> stability guarantee: if there are no edges
     * which contradict the incoming order, the resulting list will be in the same
     * order as the incoming elements. However if some elements need to be rearranged,
     * it is <em>not</em> guaranteed that others will not also be rearranged, even
     * if they did not strictly speaking need to be.
     * @param <T> tyupe of element
     * @param c a collection of objects to be topologically sorted
     * @param edges constraints among those objects, of type <code>Map&lt;Object,Collection&gt;</code>;
     *              if an object is a key in this map, the resulting order will
     *              have that object before any objects listed in the value
     * @return a partial ordering of the objects in the collection,
     * @exception TopologicalSortException if the sort cannot succeed due to cycles in the graph, the
     *   exception contains additional information to describe and possibly recover from the error
     * @since 3.30
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=27286">Issue #27286</a>
     */
    public static <T> List<T> topologicalSort(Collection<? extends T> c, Map<? super T, ? extends Collection<? extends T>> edges)
    throws TopologicalSortException {
        Map<T,Boolean> finished = new HashMap<T,Boolean>();
        List<T> r = new ArrayList<T>(Math.max(c.size(), 1));
        List<T> cRev = new ArrayList<T>(c);
        Collections.reverse(cRev);

        Iterator<T> it = cRev.iterator();

        while (it.hasNext()) {
            List<T> cycle = visit(it.next(), edges, finished, r);

            if (cycle != null) {
                throw new TopologicalSortException(cRev, edges);
            }
        }

        Collections.reverse(r);
        if (r.size() != c.size()) {
            r.retainAll(c);
        }

        return r;
    }

    /**
     * Visit one node in the DAG.
     * @param node node to visit
     * @param edges edges in the DAG
     * @param finished which nodes are finished; a node has no entry if it has not yet
     *                 been visited, else it is set to false while recurring and true
     *                 when it has finished
     * @param r the order in progress
     * @return list with detected cycle
     */
    static <T> List<T> visit(
        T node,
        Map<? super T, ? extends Collection<? extends T>> edges,
        Map<T,Boolean> finished,
        List<T> r
    ) {
        Boolean b = finished.get(node);

        //System.err.println("node=" + node + " color=" + b);
        if (b != null) {
            if (b.booleanValue()) {
                return null;
            }

            ArrayList<T> cycle = new ArrayList<T>();
            cycle.add(node);
            finished.put(node, null);

            return cycle;
        }

        Collection<? extends T> e = edges.get(node);

        if (e != null) {
            finished.put(node, Boolean.FALSE);

            Iterator<? extends T> it = e.iterator();

            while (it.hasNext()) {
                List<T> cycle = visit(it.next(), edges, finished, r);

                if (cycle != null) {
                    if (cycle instanceof ArrayList) {
                        // if cycle instanceof ArrayList we are still in the
                        // cycle and we want to collect new members
                        if (Boolean.FALSE == finished.get(node)) {
                            // another member in the cycle
                            cycle.add(node);
                        } else {
                            // we have reached the head of the cycle
                            // do not add additional cycles anymore
                            Collections.reverse(cycle);

                            // changing cycle to not be ArrayList
                            cycle = Collections.unmodifiableList(cycle);
                        }
                    }

                    // mark this node as tested
                    finished.put(node, Boolean.TRUE);

                    // and report an error
                    return cycle;
                }
            }
        }

        finished.put(node, Boolean.TRUE);
        r.add(node);

        return null;
    }

    /** Provides support for parts of the system that deal with classnames
     * (use <code>Class.forName</code>, <code>NbObjectInputStream</code>, etc.) or filenames
     * in layers.
     * <P>
     * Often class names (especially package names) changes during lifecycle
     * of a module. When some piece of the system stores the name of a class
     * in certain point of a time and wants to find the correct <code>Class</code>
     * later it needs to count with the possibility of rename.
     * <P>
     * For such purposes this method has been created. It allows modules to
     * register their classes that changed names and other parts of system that
     * deal with class names to find the correct names.
     * <P>
     * To register a mapping from old class names to new ones create a file
     * <code>META-INF/netbeans/translate.names</code> in your module and fill it
     * with your mapping:
     * <PRE>
     * #
     * # Mapping of legacy classes to new ones
     * #
     *
     * org.oldpackage.MyClass=org.newpackage.MyClass # rename of package for one class
     * org.mypackage.OldClass=org.mypackage.NewClass # rename of class in a package
     *
     * # rename of class and package
     * org.oldpackage.OldClass=org.newpackage.NewClass
     *
     * # rename of whole package
     * org.someoldpackage=org.my.new.package.structure
     *
     * # class was removed without replacement
     * org.mypackage.OldClass=
     *
     * </PRE>
     * Btw. one can use spaces instead of <code>=</code> sign.
     * For a real world example
     * check the
     * <a href="https://github.com/apache/netbeans/tree/master/ide/xml">
     * xml module</a>.
     *
     * <P>
     * For purposes of {@link org.openide.util.io.NbObjectInputStream} there is
     * a following special convention:
     * If the
     * className is not listed as one that is to be renamed, the returned
     * string == className, if the className is registered to be renamed
     * than the className != returned value, even in a case when className.equals (retValue)
     * <p>
     * Similar behaviour applies to <b>filenames</b> provided by layers (system filesystem). Filenames
     * can be also translated to adapt to location changes e.g. in action registrations. Note that 
     * <b>no spaces or special characters</b> are allowed in both translated filenames or translation 
     * results. Filenames must conform to regexp {@code ^[/a-zA-Z0-9$_.+-]+$}. Keys and values are treated
     * as paths from fs root.
     * 
     * <p>
     * Example of file path translation (action registration file has moved):
     * <pre>
     * # registration ID has changed
     * Actions/Refactoring/RefactoringWhereUsed.instance=Actions/Refactoring/org-netbeans-modules-refactoring-api-ui-WhereUsedAction.instance
     * </pre>
     *
     * @param className fully qualified name of a class, or file path to translate
     * @return new name of the class according to renaming rules.
     */
    public static String translate(final String className) {
        checkMapping();

        RE exp;

        synchronized (TRANS_LOCK) {
            exp = transExp;
        }

        if (exp == null) {
            // no transition table found
            return className;
        }

        synchronized (exp) {
            // refusing convertions as fast as possible
            return exp.convert(className);
        }
    }

    /** Loads all resources that contain renaming information.
     */
    private static void checkMapping() {
        // test if we run in test mode
        if (transLoader == TRANS_LOCK) {
            // no check
            return;
        }

        ClassLoader current = Lookup.getDefault().lookup(ClassLoader.class);

        if (current == null) {
            current = ClassLoader.getSystemClassLoader();
        }

        if (transLoader == current) {
            // no change, no rescan
            return;
        }

        initForLoader(current, current);
    }

    /* Initializes the content of transition table from a classloader.
     * @param loader loader to read data from
     * @param set loader to set as the transLoader or null if we run in test mode
     */
    static void initForLoader(ClassLoader current, Object set) {
        if (set == null) {
            set = TRANS_LOCK;
        }

        Enumeration<URL> en;

        try {
            en = current.getResources("META-INF/netbeans/translate.names");
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            en = null;
        }

        if ((en == null) || !en.hasMoreElements()) {
            synchronized (TRANS_LOCK) {
                transLoader = set;
                transExp = null;
            }

            return;
        }

        // format of line in the meta files
        //
        // # comments are allowed
        // a.name.in.a.Package=another.Name # with comment is allowed
        // for.compatibility.one.can.use.Space instead.of.Equal
        //
        RE re = null;

        // [pnejedly:perf] commented out. The RegExp based translation was way slower
        // than the hand-written RE13
        //        if (Dependency.JAVA_SPEC.compareTo(new SpecificationVersion("1.4")) >= 0) { // NOI18N
        //            try {
        //                re = (RE)Class.forName ("org.openide.util.RE14").newInstance ();
        //            } catch (ThreadDeath t) {
        //                throw t;
        //            } catch (Throwable t) {
        //            }
        //        }
        //        if (re == null) {
        re = new RE13();

        //        }
        TreeSet<String[]> list = new TreeSet<String[]>(
                new Comparator<String[]>() {
                    @Override public int compare(String[] o1, String[] o2) {
                        String s1 = o1[0];
                        String s2 = o2[0];

                        int i1 = s1.length();
                        int i2 = s2.length();

                        if (i1 != i2) {
                            return i2 - i1;
                        }

                        return s2.compareTo(s1);
                    }
                }
            );

        while (en.hasMoreElements()) {
            URL u = en.nextElement();

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(u.openStream(), "UTF8") // use explicit encoding  //NOI18N
                    );
                loadTranslationFile(re, reader, list);
                reader.close();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Problematic file: {0}", u);
                LOG.log(Level.WARNING, null, ex);
            }
        }

        // construct a regular expression of following form. Let "1", "2", "3", "4"
        // be the keys:
        // "^
        // thus if 4 is matched five groups will be created
        String[] arr = new String[list.size()];
        String[] pattern = new String[arr.length];

        int i = 0;
        for (String[] pair : list) {
            arr[i] = pair[1].intern(); // name of the track
            pattern[i] = pair[0]; // original object
            i++;
        }

        synchronized (TRANS_LOCK) {
            // last check
            if (arr.length == 0) {
                transExp = null;
            } else {
                transExp = re;
                transExp.init(pattern, arr);
            }

            transLoader = set;
        }
    }

    /**
     * Load single translation file.
     * @param re interface for communication between Utilities.translate and regular expression impl.
     * @param reader source of data
     * @param results will be filled with String[2]
     */
    private static void loadTranslationFile(RE re, BufferedReader reader, Set<String[]> results)
    throws IOException {
        for (;;) {
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            if ((line.length() == 0) || line.startsWith("#")) { // NOI18N

                continue;
            }

            String[] pair = re.readPair(line);

            if (pair == null) {
                throw new java.io.InvalidObjectException("Line is invalid: " + line);
            }

            results.add(pair);
        }
    }

    /**
     * Converts a file to a URI while being safe for UNC paths.
     * Uses {@link File f}.{@link File#toPath() toPath}().{@link java.nio.file.Path#toUri() toUri}()
     * which results into {@link URI} that works with {@link URI#normalize()}
     * and {@link URI#resolve(URI)}.
     * @param f a file
     * @return a {@code file}-protocol URI which may use the host field
     * @see java.nio.file.Path#toUri()
     * @since 8.25
     */
    public static URI toURI(File f) {
        URI u;
        if (pathToURISupported()) {
            try {
                u = f.toPath().toUri();
            } catch (java.nio.file.InvalidPathException ex) {
                u = f.toURI();
                LOG.log(Level.FINE, "can't convert " + f + " falling back to " + u, ex);
            }
        } else {
            u = f.toURI();
        }
        if (u.toString().startsWith("file:///")) { 
            try {
                // #214131 workaround
                return new URI(
                    /* "file" */u.getScheme(), /* null */u.getUserInfo(), 
                    /* null (!) */u.getHost(), /* -1 */u.getPort(), 
                    /* "/..." */u.getPath(), /* null */u.getQuery(), 
                    /* null */u.getFragment()
                );
            } catch (URISyntaxException ex) {
                LOG.log(Level.FINE, "could not convert " + f + " to URI", ex);
            }
        }
        return u;
    }

    /**
     * Converts a URI to a file while being safe for UNC paths.
     * Uses {@link Paths#get(java.net.URI) Paths.get}(u).{@link java.nio.file.Path#toFile() toFile}()
     * which accepts UNC URIs with a host field.
     * @param u a {@code file}-protocol URI which may use the host field
     * @return a file
     * @see java.nio.file.Paths#get(java.net.URI)
     * @since 8.25
     */
    public static File toFile(URI u) throws IllegalArgumentException {
        try {
            return Paths.get(u).toFile();
        } catch (Exception x) {
            LOG.log(Level.FINE, "could not convert " + u + " to File", x);
        }
        String host = u.getHost();
        if (host != null && !host.isEmpty() && "file".equals(u.getScheme())) {
            return new File("\\\\" + host + u.getPath().replace('/', '\\'));
        }
        return new File(u);    
    }

    /**
     * Normalizes the given {@code URI}. Like {@link URI#normalize()}, but does
     * not break certain special {@code URI}s, so should be preferred over
     * {@linkplain URI#normalize()}.
     *
     * @param uri the {@code URI} to normalize
     * @return the normalized URI
     */
    public static URI normalizeURI(URI uri) {
        @SuppressWarnings("URI.normalize")
        URI normalized = uri.normalize();

        if (uri.getAuthority() == null && "file".equals(uri.getScheme()) && uri.getPath().startsWith("//")) {
            try {
                normalized = new URI(normalized.getScheme(), null, "///" + normalized.getPath(), normalized.getQuery(), normalized.getFragment());
            } catch (URISyntaxException ex) {
                throw new IllegalStateException(ex); //unexpected
            }
        }

        return normalized;
    }

    /** Interfaces for communication between Utilities.translate and regular
     * expression impl.
     *
     * Order of methods is:
     * readPair few times
     * init once
     * convert many times
     */
    @SuppressWarnings("PackageVisibleInnerClass")
    static interface RE {
        public void init(String[] original, String[] newversion);

        public String convert(String pattern);

        /** Parses line of text to two parts: the key and the rest
         */
        public String[] readPair(String line);
    }

    private static volatile Boolean pathURIConsistent;

    private static boolean pathToURISupported() {
        Boolean res = pathURIConsistent;
        if (res == null) {
            boolean c;
            try {
                final File f = new File("küñ"); //NOI18N
                c = f.toPath().toUri().equals(f.toURI());
            } catch (InvalidPathException e) {
                c = false;
            }
            if (!c) {
                LOG.fine("The java.nio.file.Path.toUri is inconsistent with java.io.File.toURI");   //NOI18N
            }
            res = pathURIConsistent = c;
        }
        return res;
    }
}
