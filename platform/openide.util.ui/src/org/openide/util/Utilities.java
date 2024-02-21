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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/** Otherwise uncategorized useful static methods.
*
* @author Jan Palka, Ian Formanek, Jaroslav Tulach
*/
public final class Utilities {

    private static final Logger LOG = Logger.getLogger(Utilities.class.getName());

    /** Operating system is Windows NT. */
    public static final int OS_WINNT = BaseUtilities.OS_WINNT;

    /** Operating system is Windows 95. */
    public static final int OS_WIN95 = BaseUtilities.OS_WIN95;

    /** Operating system is Windows 98. */
    public static final int OS_WIN98 = BaseUtilities.OS_WIN98;

    /** Operating system is Solaris. */
    public static final int OS_SOLARIS = BaseUtilities.OS_SOLARIS;

    /** Operating system is Linux. */
    public static final int OS_LINUX = BaseUtilities.OS_LINUX;

    /** Operating system is HP-UX. */
    public static final int OS_HP = BaseUtilities.OS_HP;

    /** Operating system is IBM AIX. */
    public static final int OS_AIX = BaseUtilities.OS_AIX;

    /** Operating system is SGI IRIX. */
    public static final int OS_IRIX = BaseUtilities.OS_IRIX;

    /** Operating system is Sun OS. */
    public static final int OS_SUNOS = BaseUtilities.OS_SUNOS;

    /** Operating system is Compaq TRU64 Unix */
    public static final int OS_TRU64 = BaseUtilities.OS_TRU64;

    /** @deprecated please use OS_TRU64 instead */
    @Deprecated
    public static final int OS_DEC = BaseUtilities.OS_DEC;

    /** Operating system is OS/2. */
    public static final int OS_OS2 = BaseUtilities.OS_OS2;

    /** Operating system is Mac. */
    public static final int OS_MAC = BaseUtilities.OS_MAC;

    /** Operating system is Windows 2000. */
    public static final int OS_WIN2000 = BaseUtilities.OS_WIN2000;

    /** Operating system is Compaq OpenVMS */
    public static final int OS_VMS = BaseUtilities.OS_VMS;

    /**
     *Operating system is one of the Windows variants but we don't know which
     *one it is
     */
    public static final int OS_WIN_OTHER =  BaseUtilities.OS_WIN_OTHER;

    /** Operating system is unknown. */
    public static final int OS_OTHER =  BaseUtilities.OS_OTHER;

    /** Operating system is FreeBSD
     * @since 4.50
     */
    public static final int OS_FREEBSD =  BaseUtilities.OS_FREEBSD;
    
    /** Operating system is Windows Vista.
     * @since 7.17
     */
    public static final int OS_WINVISTA =  BaseUtilities.OS_WINVISTA;

    /** Operating system is one of the Unix variants but we don't know which
     * one it is.
     * @since 7.18
     */
    public static final int OS_UNIX_OTHER =  BaseUtilities.OS_UNIX_OTHER;

    /** Operating system is OpenBSD.
     * @since 7.18
     */
    public static final int OS_OPENBSD =  BaseUtilities.OS_OPENBSD;

    /** A mask for Windows platforms.
     * @deprecated Use {@link #isWindows()} instead.
     */
    @Deprecated
    public static final int OS_WINDOWS_MASK =  BaseUtilities.OS_WINDOWS_MASK;

    /** A mask for Unix platforms.
     * @deprecated Use {@link #isUnix()} instead.
     */
    @Deprecated
    public static final int OS_UNIX_MASK =  BaseUtilities.OS_UNIX_MASK;

    /** A height of the windows's taskbar */
    public static final int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27;

    /** A height of the Mac OS X's menu */
    private static final int TYPICAL_MACOSX_MENU_HEIGHT = 24;

    private static Timer clearIntrospector;
    private static ActionListener doClear;
    private static final int CTRL_WILDCARD_MASK = 32768;
    private static final int ALT_WILDCARD_MASK = CTRL_WILDCARD_MASK * 2;

    private static final Map<GraphicsConfiguration, Map<Rectangle, Long>> screenBoundsCache;

    static {
        final boolean cacheEnabled = !GraphicsEnvironment.isHeadless() && isUnix()
                && !isMac() && (System.getProperty( "netbeans.screen.insetsCache", "true" ).equalsIgnoreCase( "true" )); //NOI18N

        if( cacheEnabled ) {
            screenBoundsCache = new WeakHashMap<GraphicsConfiguration, Map<Rectangle, Long>>();
        } else {
            screenBoundsCache = null;
        }
    }

    //
    // Support for work with actions
    //

    /** the found actionsGlobalContext */
    private static Lookup global;

    private Utilities() {
    }

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
     * <PRE>{@code
     * class MyReference extends WeakReference<Thing> implements Runnable {
     *     private final OtherInfo dataToCleanUp;
     *     public MyReference(Thing ref, OtherInfo data) {
     *         super(ref, Utilities.activeReferenceQueue());
     *         dataToCleanUp = data;
     *     }
     *     public void run() {
     *         dataToCleanUp.releaseOrWhateverYouNeed();
     *     }
     * }
     * }</PRE>
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
     * @since 3.11
     */
    public static ReferenceQueue<Object> activeReferenceQueue() {
        return BaseUtilities.activeReferenceQueue();
    }
 
        /** Get the operating system on which NetBeans is running.
    * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
    */
    public static int getOperatingSystem() {
        return BaseUtilities.getOperatingSystem();
    }

    /** Test whether NetBeans is running on some variant of Windows.
    * @return <code>true</code> if Windows, <code>false</code> if some other manner of operating system
    */
    public static boolean isWindows() {
        return BaseUtilities.isWindows();
    }

    /** Test whether NetBeans is running on Mac OS X.
     * @since 7.7
    * @return <code>true</code> if Mac, <code>false</code> if some other manner of operating system
    */
    public static boolean isMac() {
        return BaseUtilities.isMac();
    }

    /** Test whether NetBeans is running on some variant of Unix.
    * Linux is included as well as the commercial vendors and Mac OS X.
    * @return <code>true</code> some sort of Unix, <code>false</code> if some other manner of operating system
    */
    public static boolean isUnix() {
        return BaseUtilities.isUnix();
    }
    
    /** Test whether a given string is a valid Java identifier.
    * @param id string which should be checked
    * @return <code>true</code> if a valid identifier
    * @see javax.lang.model.SourceVersion#isIdentifier
    * @see javax.lang.model.SourceVersion#isKeyword
    */
    public static boolean isJavaIdentifier(String id) {
        return BaseUtilities.isJavaIdentifier(id);
    }

    /** Central method for obtaining <code>BeanInfo</code> for potential JavaBean classes.
    * @param clazz class of the bean to provide the <code>BeanInfo</code> for
    * @return the bean info
    * @throws java.beans.IntrospectionException for the usual reasons
    * @see java.beans.Introspector#getBeanInfo(Class)
    */
    public static java.beans.BeanInfo getBeanInfo(Class<?> clazz)
    throws java.beans.IntrospectionException {
        java.beans.BeanInfo bi;

        try {
            bi = java.beans.Introspector.getBeanInfo(clazz);
        } catch (java.beans.IntrospectionException ie) {
            Exceptions.attachMessage(ie,
                                     "Encountered while introspecting " +
                                     clazz.getName()); // NOI18N
            throw ie;
        } catch (Error e) {
            // Could be a bug in Introspector triggered by NB code.
            Exceptions.attachMessage(e,
                                     "Encountered while introspecting " +
                                     clazz.getName()); // NOI18N
            throw e;
        }

        if (java.awt.Component.class.isAssignableFrom(clazz)) {
            java.beans.PropertyDescriptor[] pds = bi.getPropertyDescriptors();

            for (int i = 0; i < pds.length; i++) {
                if (pds[i].getName().equals("cursor")) { // NOI18N

                    try {
                        Method getter = Component.class.getDeclaredMethod("getCursor"); // NOI18N
                        Method setter = Component.class.getDeclaredMethod("setCursor", Cursor.class); // NOI18N
                        pds[i] = new java.beans.PropertyDescriptor("cursor", getter, setter); // NOI18N
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }

        // clears about 1000 instances of Method
        if (bi != null) {
            if (clearIntrospector == null) {
                doClear = new ActionListener() {
                            @Override public void actionPerformed(ActionEvent ev) {
                                java.beans.Introspector.flushCaches();
                            }
                        };
                clearIntrospector = new Timer(15000, doClear);
                clearIntrospector.setRepeats(false);
            }

            clearIntrospector.restart();
        }

        return bi;
    }

    /** Central method for obtaining <code>BeanInfo</code> for potential JavaBean classes, with a stop class.
    * @param clazz class of the bean to provide the <code>BeanInfo</code> for
    * @param stopClass the stop class
    * @return the bean info
    * @throws java.beans.IntrospectionException for the usual reasons
    * @see java.beans.Introspector#getBeanInfo(Class, Class)
    */
    public static java.beans.BeanInfo getBeanInfo(Class<?> clazz, Class<?> stopClass)
    throws java.beans.IntrospectionException {
        return java.beans.Introspector.getBeanInfo(clazz, stopClass);
    }

    /** Wrap multi-line strings (and get the individual lines).
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param wrapWords if <code>true</code>, the lines are wrapped on word boundaries (if possible);
    *                  if <code>false</code>, character boundaries are used
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the lines after wrapping
    * @deprecated use {@link #wrapStringToArray(String, int, BreakIterator, boolean)} since it is better for I18N
    */
    @Deprecated
    public static String[] wrapStringToArray(String original, int width, boolean wrapWords, boolean removeNewLines) {
        BreakIterator bi = (wrapWords ? BreakIterator.getWordInstance() : BreakIterator.getCharacterInstance());

        return wrapStringToArray(original, width, bi, removeNewLines);
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
        return BaseUtilities.wrapStringToArray(original, width, breakIterator, removeNewLines);
    }

    /** Wrap multi-line strings.
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param breakIterator algorithm for breaking lines
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the whole string with embedded newlines
    */
    public static String wrapString(String original, int width, BreakIterator breakIterator, boolean removeNewLines) {
        return BaseUtilities.wrapString(original, width, breakIterator, removeNewLines);
    }

    /** Wrap multi-line strings.
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param wrapWords if <code>true</code>, the lines are wrapped on word boundaries (if possible);
    *                  if <code>false</code>, character boundaries are used
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the whole string with embedded newlines
    * @deprecated Use {@link #wrapString (String, int, BreakIterator, boolean)} as it is friendlier to I18N.
    */
    @Deprecated
    public static String wrapString(String original, int width, boolean wrapWords, boolean removeNewLines) {
        // substitute original newlines with spaces,
        // remove newlines from head and tail
        if (removeNewLines) {
            while (original.startsWith("\n")) {
                original = original.substring(1);
            }

            while (original.endsWith("\n")) {
                original = original.substring(0, original.length() - 1);
            }

            original = original.replace('\n', ' ');
        }

        if (width < 1) {
            width = 1;
        }

        if (original.length() <= width) {
            return original;
        }

        java.util.Vector<String> lines = new java.util.Vector<String>();
        int lineStart = 0; // the position of start of currently processed line in the original string
        int lastSpacePos = -1;

        for (int i = 0; i < original.length(); i++) {
            if (lineStart >= (original.length() - 1)) {
                break;
            }

            // newline in the original string
            if (original.charAt(i) == '\n') {
                lines.addElement(original.substring(lineStart, i));
                lineStart = i + 1;
                lastSpacePos = -1;

                continue;
            }

            // remember last space position
            if (Character.isSpaceChar(original.charAt(i))) {
                lastSpacePos = i;
            }

            // last position in the original string
            if (i == (original.length() - 1)) {
                lines.addElement(original.substring(lineStart));

                break;
            }

            // reached width
            if ((i - lineStart) == width) {
                if (wrapWords && (lastSpacePos != -1)) {
                    lines.addElement(original.substring(lineStart, lastSpacePos));
                    lineStart = lastSpacePos + 1; // the space is consumed for the newline
                    lastSpacePos = -1;
                } else {
                    lines.addElement(original.substring(lineStart, i));
                    lineStart = i;
                    lastSpacePos = -1;
                }
            }
        }

        StringBuilder retBuf = new StringBuilder();

        for (java.util.Enumeration<String> e = lines.elements(); e.hasMoreElements();) {
            retBuf.append(e.nextElement());
            retBuf.append('\n');
        }

        return retBuf.toString();
    }

    /** Search-and-replace fixed string matches within a string.
    * @param original the original string
    * @param replaceFrom the substring to be find
    * @param replaceTo the substring to replace it with
    * @return a new string with all occurrences replaced
     * @deprecated Use {@link String#replace(CharSequence,CharSequence)} instead
    */
    @Deprecated
    public static String replaceString(String original, String replaceFrom, String replaceTo) {
        int index = 0;

        if ("".equals(replaceFrom)) {
            return original; // NOI18N
        }

        StringBuilder buf = new StringBuilder();

        while (true) {
            int pos = original.indexOf(replaceFrom, index);

            if (pos == -1) {
                buf.append(original.substring(index));

                return buf.toString();
            }

            buf.append(original.substring(index, pos));
            buf.append(replaceTo);
            index = pos + replaceFrom.length();

            if (index == original.length()) {
                return buf.toString();
            }
        }
    }

    /** Turn full name of an inner class into its pure form.
    * @param fullName e.g. <code>some.pkg.SomeClass$Inner</code>
    * @return e.g. <code>Inner</code>
    */
    public static String pureClassName(final String fullName) {
        return BaseUtilities.pureClassName(fullName);
    }

    /** Test whether the operating system supports icons on frames (windows).
    * @return <code>true</code> if it does <em>not</em>
    * @deprecated Obsolete, useless method, no replacement.
    */
    @Deprecated public static boolean isLargeFrameIcons() {
        return (getOperatingSystem() == OS_SOLARIS) || (getOperatingSystem() == OS_HP);
    }

    /** Compute hash code of array.
    * Asks all elements for their own code and composes the
    * values.
    * @param arr array of objects, can contain <code>null</code>s
    * @return the hash code
    * @see Object#hashCode
    * @deprecated Use {@link Arrays#hashCode(Object[])} instead.
    */
    @Deprecated
    public static int arrayHashCode(Object[] arr) {
        int c = 0;
        int len = arr.length;

        for (int i = 0; i < len; i++) {
            Object o = arr[i];
            int v = (o == null) ? 1 : o.hashCode();
            c += (v ^ i);
        }

        return c;
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
        return BaseUtilities.compareObjects(o1, o2);
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
        return BaseUtilities.compareObjectsImpl(o1, o2, checkArraysDepth);
    }

    /** Assemble a human-presentable class name for a specified class.
    * Arrays are represented as e.g. <code>java.lang.String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getClassName(Class<?> clazz) {
        return BaseUtilities.getClassName(clazz);
    }

    /** Assemble a human-presentable class name for a specified class (omitting the package).
    * Arrays are represented as e.g. <code>String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getShortClassName(Class<?> clazz) {
        return BaseUtilities.getShortClassName(clazz);
    }

    /**
    * Convert an array of objects to an array of primitive types.
    * E.g. an <code>Integer[]</code> would be changed to an <code>int[]</code>.
    * @param array the wrapper array
    * @return a primitive array
    * @throws IllegalArgumentException if the array element type is not a primitive wrapper
    */
    public static Object toPrimitiveArray(Object[] array) {
        return BaseUtilities.toPrimitiveArray(array);
    }

    /**
    * Convert an array of primitive types to an array of objects.
    * E.g. an <code>int[]</code> would be turned into an <code>Integer[]</code>.
    * @param array the primitive array
    * @return a wrapper array
    * @throws IllegalArgumentException if the array element type is not primitive
    */
    public static Object[] toObjectArray(Object array) {
        return BaseUtilities.toObjectArray(array);
    }

    /**
    * Get the object type for given primitive type.
    *
    * @param c primitive type (e.g. <code>int</code>)
    * @return object type (e.g. <code>Integer</code>)
    */
    public static Class<?> getObjectType(Class<?> c) {
        return BaseUtilities.getObjectType(c);
    }

    /**
    * Get the primitive type for given object type.
    *
    * @param c object type (e.g. <code>Integer</code>)
    * @return primitive type (e.g. <code>int</code>)
    */
    public static Class<?> getPrimitiveType(Class<?> c) {
        return BaseUtilities.getPrimitiveType(c);
    }

    /** Find a focus-traverable component.
    * @param c the component to look in
    * @return the same component if traversable, else a child component if present, else <code>null</code>
    * @see Component#isFocusTraversable
    */
    public static Component getFocusTraversableComponent(Component c) {
        if (c.isFocusable()) {
            return c;
        }

        if (!(c instanceof Container)) {
            return null;
        }

        int i;
        int k = ((Container) c).getComponentCount();

        for (i = 0; i < k; i++) {
            Component v = ((Container) c).getComponent(i);

            if (v != null) {
                return v;
            }
        }

        return null;
    }
    
    /** Parses parameters from a given string in shell-like manner.
    * Users of the Bourne shell (e.g. on Unix) will already be familiar with the behavior.
    * For example, when using <code>org.openide.execution.NbProcessDescriptor</code> (Execution API)
    * you should be able to:
    * <ul>
    * <li>Include command names with embedded spaces, such as <code>c:\Program Files\jdk\bin\javac</code>.
    * <li>Include extra command arguments, such as <code>-Dname=value</code>.
    * <li>Do anything else which might require unusual characters or processing. For example:
    * <pre>{@code
    * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
    * }</pre>
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
        return BaseUtilities.parseParameters(s);
    }

    /** Complementary method to parseParameters
     * @see #parseParameters
     */
    public static String escapeParameters(String[] params) {
        return BaseUtilities.escapeParameters(params);
    }

    //
    // Key conversions
    //

    private static final class NamesAndValues {
        final Map<Integer,String> keyToString;
        final Map<String,Integer> stringToKey;
        NamesAndValues(Map<Integer,String> keyToString, Map<String,Integer> stringToKey) {
            this.keyToString = keyToString;
            this.stringToKey = stringToKey;
        }
    }

    private static Reference<NamesAndValues> namesAndValues;

    private static synchronized NamesAndValues initNameAndValues() {
        if (namesAndValues != null) {
            NamesAndValues nav = namesAndValues.get();
            if (nav != null) {
                return nav;
            }
        }

        Field[] fields = KeyEvent.class.getDeclaredFields();

        Map<String,Integer> names = new HashMap<String,Integer>(fields.length * 4 / 3 + 5, 0.75f);
        Map<Integer,String> values = new HashMap<Integer,String>(fields.length * 4 / 3 + 5, 0.75f);

        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                String name = f.getName();
                if (name.startsWith("VK_")) { // NOI18N
                    // exclude VK
                    name = name.substring(3);
                    try {
                        int numb = f.getInt(null);
                        names.put(name, numb);
                        values.put(numb, name);
                    } catch (IllegalArgumentException ex) {
                    } catch (IllegalAccessException ex) {
                    }
                }
            }
        }

        if (names.get("CONTEXT_MENU") == null) { // NOI18N
            names.put("CONTEXT_MENU", 0x20C); // NOI18N
            values.put(0x20C, "CONTEXT_MENU"); // NOI18N
            names.put("WINDOWS", 0x20D); // NOI18N
            values.put(0x20D, "WINDOWS"); // NOI18N
        }
        
        names.put("MOUSE_WHEEL_UP", 0x290);
        names.put("MOUSE_WHEEL_DOWN", 0x291);
        values.put(0x290,"MOUSE_WHEEL_UP");
        values.put(0x291,"MOUSE_WHEEL_DOWN");

        for (int button = 4; button < 10; button++) {
            String name = "MOUSE_BUTTON" + button; // NOI18N
            int code = 0x292 + (button - 1);
            names.put(name, code);
            values.put(code, name);
        }

        NamesAndValues nav = new NamesAndValues(values, names);
        namesAndValues = new SoftReference<NamesAndValues>(nav);
        return nav;
    }

    /**
     * Check whether the provided keycode is within the range reserved for mouse
     * event pseudo-keycodes. Note that not all keycodes in the range may be
     * mapped.
     *
     * @param keycode keycode to check
     * @return true if in mouse range
     * @since 9.31
     */
    public static boolean isMouseKeyCode(int keycode) {
        return keycode >= 0x290 && keycode <= 0x29F;
    }

    /**
     * Get the pseudo-keycode used for mouse wheel up events. May return
     * {@link KeyEvent#VK_UNDEFINED} if not available.
     *
     * @return mouse wheel up keycode if defined
     * @since 9.31
     */
    public static int mouseWheelUpKeyCode() {
        return 0x290;
    }

    /**
     * Get the pseudo-keycode used for mouse wheel up events. May return
     * {@link KeyEvent#VK_UNDEFINED} if not available.
     *
     * @return mouse wheel down keycode if defined
     * @since 9.31
     */
    public static int mouseWheelDownKeyCode() {
        return 0x291;
    }

    /**
     * Get the pseudo-keycode used for the provided mouse button. Returns
     * {@link KeyEvent#VK_UNDEFINED} if not available.
     * <p>
     * Implementation note : only extended mouse buttons in the range BUTTON4 to
     * BUTTON9 are currently mapped to keycodes. The caller may pass in values
     * that best reflect the desired mouse button rather than the actual value
     * from the OS or MouseEvent. eg. on Linux, the JDK excludes X button values
     * for vertical scrolling when generating the range of buttons, and the
     * default NetBeans window system further excludes the horizontal scroll
     * button values - button 4 passed in here might be JDK button 6 and X event
     * button 8.
     *
     * @param button mouse button
     * @return keycode if defined
     * @since 9.31
     */
    public static int mouseButtonKeyCode(int button) {
        if (button >= 4 && button < 10) {
            return 0x292 + (button - 1);
        } else {
            return KeyEvent.VK_UNDEFINED;
        }
    }

    /** Converts a Swing key stroke descriptor to a familiar Emacs-like name.
    * @param stroke key description
    * @return name of the key (e.g. <code>CS-F1</code> for control-shift-function key one)
    * @see #stringToKey
    */
    public static String keyToString(KeyStroke stroke) {
        StringBuilder sb = new StringBuilder();

        // add modifiers that must be pressed
        if (addModifiers(sb, stroke.getModifiers())) {
            sb.append('-');
        }

        appendRest(sb, stroke);
        return sb.toString();
    }

    private static void appendRest(StringBuilder sb, KeyStroke stroke) {
        String c = initNameAndValues().keyToString.get(Integer.valueOf(stroke.getKeyCode()));

        if (c == null) {
            sb.append(stroke.getKeyChar());
        } else {
            sb.append(c);
        }
    }

    /**
     * Converts a Swing key stroke descriptor to a familiar Emacs-like name,
     * but in a portable way, ie. <code>Meta-C</code> on Mac => <code>D-C</code>
     * @param stroke key description
     * @return name of the key (e.g. <code>CS-F1</code> for control-shift-function key one)
     * @see #stringToKey
     */
    public static String keyToString(KeyStroke stroke, boolean portable) {
        if (portable) {
            StringBuilder sb = new StringBuilder();

            // add modifiers that must be pressed
            if (addModifiersPortable(sb, stroke.getModifiers())) {
                sb.append('-');
            }

            appendRest(sb, stroke);
            return sb.toString();
        }
        return keyToString(stroke);
    }

    /** Construct a new key description from a given universal string
    * description.
    * Provides mapping between Emacs-like textual key descriptions and the
    * <code>KeyStroke</code> object used in Swing.
    * <P>
    * This format has following form:
    * <P><code>[C][A][S][M]-<em>identifier</em></code>
    * <p>Where:
    * <UL>
    * <LI> <code>C</code> stands for the Control key
    * <LI> <code>A</code> stands for the Alt key
    * <LI> <code>S</code> stands for the Shift key
    * <LI> <code>M</code> stands for the Meta key
    * </UL>
    * The format also supports two wildcard codes, to support differences in
    * platforms.  These are the preferred choices for registering keystrokes,
    * since platform conflicts will automatically be handled:
    * <UL>
    * <LI> <code>D</code> stands for the default menu accelerator - the Control
    *  key on most platforms, the Command (meta) key on Macintosh</LI>
    * <LI> <code>O</code> stands for the alternate accelerator - the Alt key on
    *  most platforms, the Ctrl key on Macintosh (Macintosh uses Alt as a
    *  secondary shift key for composing international characters - if you bind
    *  Alt-8 to an action, a mac user with a French keyboard will not be able
    *  to type the <code>[</code> character, which is a significant handicap</LI>
    * </UL>
    * If you use the wildcard characters, and specify a key which will conflict
    * with keys the operating system consumes, it will be mapped to whichever
    * choice can work - for example, on Macintosh, Command-Q is always consumed
    * by the operating system, so <code>D-Q</code> will always map to Control-Q.
    * <p>
    * Every modifier before the hyphen must be pressed.
    * <em>identifier</EM> can be any text constant from {@link KeyEvent} but
    * without the leading <code>VK_</code> characters. So {@link KeyEvent#VK_ENTER} is described as
    * <code>ENTER</code>.
    *
    * @param s the string with the description of the key
    * @return key description object, or <code>null</code> if the string does not represent any valid key
    */
    public static KeyStroke stringToKey(String s) {
        StringTokenizer st = new StringTokenizer(s.toUpperCase(Locale.ENGLISH), "-", true); // NOI18N

        int needed = 0;

        Map<String,Integer> names = initNameAndValues().stringToKey;

        int lastModif = -1;

        try {
            for (;;) {
                String el = st.nextToken();

                // required key
                if (el.equals("-")) { // NOI18N

                    if (lastModif != -1) {
                        needed |= lastModif;
                        lastModif = -1;
                    }

                    continue;
                }

                // if there is more elements
                if (st.hasMoreElements()) {
                    // the text should describe modifiers
                    lastModif = readModifiers(el);
                } else {
                    // last text must be the key code
                    Integer i = names.get(el);
                    boolean wildcard = (needed & CTRL_WILDCARD_MASK) != 0;

                    //Strip out the explicit mask - KeyStroke won't know
                    //what to do with it
                    needed = needed & ~CTRL_WILDCARD_MASK;

                    boolean macAlt = (needed & ALT_WILDCARD_MASK) != 0;
                    needed = needed & ~ALT_WILDCARD_MASK;

                    if (i != null) {
                        //#26854 - Default accelerator should be Command on mac
                        if (wildcard) {
                            needed |= getMenuShortcutKeyMask();

                            if (isMac()) {
                                if (!usableKeyOnMac(i, macAlt ? needed | KeyEvent.CTRL_MASK : needed)) {
                                    needed &= ~getMenuShortcutKeyMask();
                                    if (macAlt) {
                                        // CTRL will be added by the "if (macAlt) .." branch below
                                        needed |= KeyEvent.ALT_MASK;
                                    } else {
                                        needed |= KeyEvent.CTRL_MASK;
                                    }
                                }
                            }
                        }

                        if (macAlt) {
                            if (getOperatingSystem() == BaseUtilities.OS_MAC) {
                                needed |= KeyEvent.CTRL_MASK;
                            } else {
                                needed |= KeyEvent.ALT_MASK;
                            }
                        }

                        return KeyStroke.getKeyStroke(i, needed);
                    } else {
                        return null;
                    }
                }
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private static boolean usableKeyOnMac(int key, int mask) {
        //All permutations fail for Q except ctrl
        if (key == KeyEvent.VK_Q) {
            return false;
        }

        boolean isMeta = ((mask & KeyEvent.META_MASK) != 0) || ((mask & KeyEvent.CTRL_DOWN_MASK) != 0);

        boolean isAlt = ((mask & KeyEvent.ALT_MASK) != 0) || ((mask & KeyEvent.ALT_DOWN_MASK) != 0);

        boolean isOnlyMeta = isMeta && ((mask & ~(KeyEvent.META_DOWN_MASK | KeyEvent.META_MASK)) == 0);

        //Mac OS consumes keys Command+ these keys - the app will never see
        //them, so CTRL should not be remapped for these
        if (isOnlyMeta) {
            return (key != KeyEvent.VK_H) && (key != KeyEvent.VK_SPACE) && (key != KeyEvent.VK_TAB);
        }
        if ((key == KeyEvent.VK_D) && isMeta && isAlt) {
            return false;
        }
        if (key == KeyEvent.VK_SPACE && isMeta && ((mask & KeyEvent.CTRL_MASK) != 0)) {
            // http://lists.apple.com/archives/java-dev/2010/Aug/msg00002.html
            return false;
        }
        return true;
    }

    private static int getMenuShortcutKeyMask() {
        // #152050 - work in headless environment too
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            }
        } catch (Throwable ex) {
            // OK, just assume we are headless
        }
        return Event.CTRL_MASK;
    }

    /** Convert a space-separated list of user-friendly key binding names to a list of Swing key strokes.
    * @param s the string with keys
    * @return array of key strokes, or <code>null</code> if the string description is not valid
    * @see #stringToKey
    */
    public static KeyStroke[] stringToKeys(String s) {
        StringTokenizer st = new StringTokenizer(s.toUpperCase(Locale.ENGLISH), " "); // NOI18N
        ArrayList<KeyStroke> arr = new ArrayList<KeyStroke>();

        while (st.hasMoreElements()) {
            s = st.nextToken();

            KeyStroke k = stringToKey(s);

            if (k == null) {
                return null;
            }

            arr.add(k);
        }

        return arr.toArray(new KeyStroke[0]);
    }

    /** Adds characters for modifiers to the buffer.
    * @param buf buffer to add to
    * @param modif modifiers to add (KeyEvent.XXX_MASK)
    * @return true if something has been added
    */
    private static boolean addModifiers(StringBuilder buf, int modif) {
        boolean b = false;

        if ((modif & KeyEvent.CTRL_MASK) != 0) {
            buf.append("C"); // NOI18N
            b = true;
        }

        if ((modif & KeyEvent.ALT_MASK) != 0) {
            buf.append("A"); // NOI18N
            b = true;
        }

        if ((modif & KeyEvent.SHIFT_MASK) != 0) {
            buf.append("S"); // NOI18N
            b = true;
        }

        if ((modif & KeyEvent.META_MASK) != 0) {
            buf.append("M"); // NOI18N
            b = true;
        }

        if ((modif & CTRL_WILDCARD_MASK) != 0) {
            buf.append("D");
            b = true;
        }

        if ((modif & ALT_WILDCARD_MASK) != 0) {
            buf.append("O");
            b = true;
        }

        return b;
    }

    private static boolean addModifiersPortable(StringBuilder buf, int modifiers) {
        boolean b = false;

        if ((modifiers & KeyEvent.SHIFT_MASK) != 0) {
            buf.append('S');
            b = true;
        }

        if (Utilities.isMac() && ((modifiers & KeyEvent.META_MASK) != 0) || !Utilities.isMac() && ((modifiers & KeyEvent.CTRL_MASK) != 0)) {
            buf.append('D');
            b = true;
        }

        if (Utilities.isMac() && ((modifiers & KeyEvent.CTRL_MASK) != 0) || !Utilities.isMac() && ((modifiers & KeyEvent.ALT_MASK) != 0)) {
            buf.append('O');
            b = true;
        }
        // mac alt fallback
        if (Utilities.isMac() && ((modifiers & KeyEvent.ALT_MASK) != 0)) {
            buf.append('A');
            b = true;
        }
        // META fallback, see issue #224362
        if (!Utilities.isMac() && ((modifiers & KeyEvent.META_MASK) != 0)) {
            buf.append('M');
            b = true;
        }

        return b;
    }

    /** Reads for modifiers and creates integer with required mask.
    * @param s string with modifiers
    * @return integer with mask
    * @exception NoSuchElementException if some letter is not modifier
    */
    private static int readModifiers(String s) throws NoSuchElementException {
        int m = 0;

        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
            case 'C':
                m |= KeyEvent.CTRL_MASK;

                break;

            case 'A':
                m |= KeyEvent.ALT_MASK;

                break;

            case 'M':
                m |= KeyEvent.META_MASK;

                break;

            case 'S':
                m |= KeyEvent.SHIFT_MASK;

                break;

            case 'D':
                m |= CTRL_WILDCARD_MASK;

                break;

            case 'O':
                m |= ALT_WILDCARD_MASK;

                break;

            default:
                throw new NoSuchElementException(s);
            }
        }

        return m;
    }

    /**
     * Finds out the monitor where the user currently has the input focus.
     * This method is usually used to help the client code to figure out on
     * which monitor it should place newly created windows/frames/dialogs.
     *
     * @return the GraphicsConfiguration of the monitor which currently has the
     * input focus
     */
    private static GraphicsConfiguration getCurrentGraphicsConfiguration() {
	Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner != null) {
            Window w = SwingUtilities.getWindowAncestor(focusOwner);
            if (w != null) {
                return w.getGraphicsConfiguration();
            } else {
                //#217737 - try to find the main window which could be placed in secondary screen
                Frame f = findMainWindow();
                if(f != null)
                    return f.getGraphicsConfiguration();
            }
        }

        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    /**
     * Returns the usable area of the screen where applications can place its
     * windows.  The method subtracts from the screen the area of taskbars,
     * system menus and the like.  The screen this method applies to is the one
     * which is considered current, ussually the one where the current input
     * focus is.
     *
     * @return the rectangle of the screen where one can place windows
     *
     * @since 2.5
     */
    public static Rectangle getUsableScreenBounds() {
        return getUsableScreenBounds(getCurrentGraphicsConfiguration());
    }

    /**
     * Returns the usable area of the screen where applications can place its
     * windows. The method subtracts from the screen the area of taskbars,
     * system menus and the like.
     * On certain platforms this methods uses a cache to avoid performance degradation due to repeated calls.
     * This can be disabled by setting the property "-Dnetbeans.screen.insetsCache=false"
     * See issue https://bz.apache.org/netbeans/show_bug.cgi?id=219507
     *
     * @param gconf the GraphicsConfiguration of the monitor
     * @return the rectangle of the screen where one can place windows
     *
     * @since 2.5
     */
    public static Rectangle getUsableScreenBounds(GraphicsConfiguration gconf) {
        if( gconf == null ) {
            gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        if( screenBoundsCache == null ) {
            return calculateUsableScreenBounds( gconf );
        }

        synchronized( screenBoundsCache ) {
            Map<Rectangle, Long> cacheEntry = screenBoundsCache.get( gconf );
            if( cacheEntry != null ) {
                final long now = System.currentTimeMillis();
                Entry<Rectangle, Long> entry = cacheEntry.entrySet().iterator().next();
                if( entry.getValue() < now + 10000 ) { // cache hit, 10 seconds lifetime
                    return new Rectangle( entry.getKey() ); // return copy
                }
            }

            final Rectangle screenBounds = calculateUsableScreenBounds( gconf );
            cacheEntry = new HashMap<Rectangle, Long>( 1 );
            cacheEntry.put( screenBounds, System.currentTimeMillis() );
            if( screenBoundsCache.size() > 20 ) { //maximum entries
                screenBoundsCache.clear();
            }
            screenBoundsCache.put( gconf, cacheEntry );
            return new Rectangle( screenBounds );
        }
    }
    
    /**
     * Returns the usable area of the screen where applications can place its
     * windows.  The method subtracts from the screen the area of taskbars,
     * system menus and the like.
     *
     * @param gconf the GraphicsConfiguration of the monitor
     * @return the rectangle of the screen where one can place windows
     *
     * @since 2.5
     */
    private static Rectangle calculateUsableScreenBounds(GraphicsConfiguration gconf) {

        Rectangle bounds = new Rectangle(gconf.getBounds());

        String str;

        str = System.getProperty("netbeans.screen.insets"); // NOI18N

        if (str != null) {
            StringTokenizer st = new StringTokenizer(str, ", "); // NOI18N

            if (st.countTokens() == 4) {
                try {
                    bounds.y = Integer.parseInt(st.nextToken());
                    bounds.x = Integer.parseInt(st.nextToken());
                    bounds.height -= (bounds.y + Integer.parseInt(st.nextToken()));
                    bounds.width -= (bounds.x + Integer.parseInt(st.nextToken()));
                } catch (NumberFormatException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }

            return bounds;
        }

        str = System.getProperty("netbeans.taskbar.height"); // NOI18N

        if (str != null) {
            bounds.height -= Integer.getInteger(str, 0).intValue();

            return bounds;
        }

        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Insets insets = toolkit.getScreenInsets(gconf);
            //#218895 - invalid screen insets in dual screen setup on Linux
            if( insets.left > bounds.x && bounds.x > 0 )
                insets.left -= bounds.x;
            if( insets.top > bounds.y && bounds.y > 0 )
                insets.top -= bounds.y;
            bounds.y += insets.top;
            bounds.x += insets.left;
            bounds.height -= (insets.top + insets.bottom);
            bounds.width -= (insets.left + insets.right);

        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        if( bounds.width <= 0 || bounds.height <= 0 ) {
            bounds = new Rectangle(gconf.getBounds());
        }

        return bounds;
    }

    /**
     * Helps client code place components on the center of the screen.  It
     * handles multiple monitor configuration correctly
     *
     * @param componentSize the size of the component
     * @return bounds of the centered component
     *
     * @since 2.5
     */
    public static Rectangle findCenterBounds(Dimension componentSize) {
        return findCenterBounds(getCurrentGraphicsConfiguration(), componentSize);
    }

    /**
     * Helps client code place components on the center of the screen.  It
     * handles multiple monitor configuration correctly
     *
     * @param gconf the GraphicsConfiguration of the monitor
     * @param componentSize the size of the component
     * @return bounds of the centered component
     */
    private static Rectangle findCenterBounds(GraphicsConfiguration gconf, Dimension componentSize) {
        if (gconf == null) {
            gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }

        Rectangle bounds = gconf.getBounds();

        return new Rectangle(
            bounds.x + ((bounds.width - componentSize.width) / 2),
            bounds.y + ((bounds.height - componentSize.height) / 2), componentSize.width, componentSize.height
        );
    }

    /**
     * Find the main NetBeans window; must have width or height.
     * This is used locally to avoid dependency issues. 
     * @return NetBeans' main window
     */
    private static Frame findMainWindow()
    {
        Frame f = null;
        for( Frame f01 : Frame.getFrames() ) {
            if( "NbMainWindow".equals(f01.getName())) { //NOI18N
                if(f01.getWidth() != 0 || f01.getHeight() != 0) {
                    f = f01;
                }
                break;
            }
        }
        return f;
    }

    /**
     * Finds an appropriate component to use for a dialog's parent. This is for
     * use in situations where a standard swing API, such as
     * {@linkplain javax.swing.JOptionPane}.show* or
     * {@linkplain javax.swing.JFileChooser}.show*, is used to display a dialog.
     * {@code null} should never be used as a dialog's parent because it
     * frequently does the wrong thing in a multi-screen setup.
     * <p>
     * The use of the NetBeans API
     * <a href="@org-openide-dialogs@/org/openide/DialogDisplayer.html#getDefault--">DialogDisplayer.getDefault*</a>
     * is encouraged to display a dialog.
     *
     * @return A suitable parent component for swing dialogs
     * @since 9.26
     */
    // PR4739
    public static Component findDialogParent() {
        return findDialogParent(null);
    }

    /**
     * Finds an appropriate component to use for a dialog's parent. Similar to
     * {@link #findDialogParent()} with the ability to specify a suggested
     * parent component. The suggested parent will be returned if it is
     * non-null, and either there is no active modal dialog or it is contained
     * within that dialog.
     *
     * @param suggestedParent the component to return if non-null and valid
     * @return the suggested parent if suitable, otherwise another suitable
     * parent component for swing dialogs
     * @since 9.30
     */
    public static Component findDialogParent(Component suggestedParent) {
        Component parent = suggestedParent;
        if (parent == null) {
            parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        }
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (parent == null) {
            parent = active;
        } else if (active instanceof Dialog && ((Dialog) active).isModal()) {
            Window suggested = parent instanceof Window ? (Window) parent : SwingUtilities.windowForComponent(parent);
            if (suggested != active) {
                return active;
            }
        }
        if (parent == null) {
            parent = findMainWindow();
        }
        return parent;
    }

    /**
     * Check whether a modal dialog is open.
     *
     * @return true if a modal dialog is open, false otherwise
     * @since 9.30
     */
    public static boolean isModalDialogOpen() {
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        return active instanceof Dialog && ((Dialog) active).isModal();
    }

    /** @return size of the screen. The size is modified for Windows OS
     * - some points are subtracted to reflect a presence of the taskbar
     *
     * @deprecated this method is almost useless in multiple monitor configuration
     *
     * @see #getUsableScreenBounds()
     * @see #findCenterBounds(Dimension)
     */
    @Deprecated
    public static Dimension getScreenSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (isWindows() && !Boolean.getBoolean("netbeans.no.taskbar")) {
            screenSize.height -= TYPICAL_WINDOWS_TASKBAR_HEIGHT;
        } else if (isMac()) {
            screenSize.height -= TYPICAL_MACOSX_MENU_HEIGHT;
        }

        return screenSize;
    }

    /** Utility method for avoiding of memory leak in JDK 1.3 / JFileChooser.showDialog(...)
     * @param parent
     * @param approveButtonText
     * @deprecated Not needed in JDK 1.4.
     * @see <a href="@org-openide-filesystems-nb@/org/openide/filesystems/FileChooserBuilder.html"><code>FileChooserBuilder</code></a>
     */
    @Deprecated
    public static int showJFileChooser(
        javax.swing.JFileChooser chooser, java.awt.Component parent, java.lang.String approveButtonText
    ) {
        if (approveButtonText != null) {
            chooser.setApproveButtonText(approveButtonText);
            chooser.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        }

        Frame frame = null;
        Dialog parentDlg = null;

        if (parent instanceof Dialog) {
            parentDlg = (Dialog) parent;
        } else {
            frame = (parent instanceof java.awt.Frame) ? (Frame) parent
                                                       : (Frame) javax.swing.SwingUtilities.getAncestorOfClass(
                    Frame.class, parent
                );
        }

        String title = chooser.getDialogTitle();

        if (title == null) {
            title = chooser.getUI().getDialogTitle(chooser);
        }

        final javax.swing.JDialog dialog;

        if (parentDlg != null) {
            dialog = new javax.swing.JDialog(parentDlg, title, true);
        } else {
            dialog = new javax.swing.JDialog(frame, title, true);
        }

        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooser, BorderLayout.CENTER);

        dialog.pack();
        dialog.setBounds(findCenterBounds(parent.getGraphicsConfiguration(), dialog.getSize()));

        chooser.rescanCurrentDirectory();

        final int[] retValue = {javax.swing.JFileChooser.CANCEL_OPTION};

        java.awt.event.ActionListener l = new java.awt.event.ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent ev) {
                    if (javax.swing.JFileChooser.APPROVE_SELECTION.equals(ev.getActionCommand())) {
                        retValue[0] = javax.swing.JFileChooser.APPROVE_OPTION;
                    }

                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };

        chooser.addActionListener(l);

        dialog.show();

        return retValue[0];
    }

    /** Sort a list according to a specified partial order.
    * Note that in the current implementation, the comparator will be called
    * exactly once for each distinct pair of list elements, ignoring order,
    * so caching its results is a waste of time.
    * @param l the list to sort (will not be modified)
    * @param c a comparator to impose the partial order; "equal" means that the elements
    *          are not ordered with respect to one another, i.e. may be only a partial order
    * @param stable whether to attempt a stable sort, meaning that the position of elements
    *               will be disturbed as little as possible; might be slightly slower
    * @return the partially-sorted list
    * @throws UnorderableException if the specified partial order is inconsistent on this list
    * @deprecated Deprecated in favor of the potentially much faster (and possibly more correct) {@link #topologicalSort}.
    */
    @SuppressWarnings({"unchecked", "rawtypes"}) // do not bother, it is deprecated anyway
    @Deprecated
    public static List partialSort(List l, Comparator c, boolean stable)
    throws UnorderableException {
        // map from objects in the list to null or sets of objects they are greater than
        // (i.e. must appear after):
        Map deps = new HashMap(); // Map<Object,Set<Object>>
        int size = l.size();

        // Create a table of dependencies.
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                int cmp = c.compare(l.get(i), l.get(j));

                if (cmp != 0) {
                    Object earlier = l.get((cmp < 0) ? i : j);
                    Object later = l.get((cmp > 0) ? i : j);
                    Set s = (Set) deps.get(later);

                    if (s == null) {
                        deps.put(later, s = new HashSet());
                    }

                    s.add(earlier);
                }
            }
        }

        // Lists of items to process, and items sorted.
        List left = new LinkedList(l);
        List sorted = new ArrayList(size);

        while (left.size() > 0) {
            boolean stillGoing = false;
            Iterator it = left.iterator();

            while (it.hasNext()) {
                Object elt = it.next();
                Set eltDeps = (Set) deps.get(elt);

                if ((eltDeps == null) || (eltDeps.isEmpty())) {
                    // This one is OK to add to the result now.
                    it.remove();
                    stillGoing = true;
                    sorted.add(elt);

                    // Mark other elements that should be later
                    // than this as having their dep satisfied.
                    Iterator it2 = left.iterator();

                    while (it2.hasNext()) {
                        Object elt2 = it2.next();
                        Set eltDeps2 = (Set) deps.get(elt2);

                        if (eltDeps2 != null) {
                            eltDeps2.remove(elt);
                        }
                    }

                    if (stable) {
                        break;
                    }
                }
            }

            if (!stillGoing) {
                // Clean up deps to only include "interesting" problems.
                it = deps.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry me = (Map.Entry) it.next();

                    if (!left.contains(me.getKey())) {
                        it.remove();
                    } else {
                        Set s = (Set) me.getValue();
                        Iterator it2 = s.iterator();

                        while (it2.hasNext()) {
                            if (!left.contains(it2.next())) {
                                it2.remove();
                            }
                        }

                        if (s.isEmpty()) {
                            it.remove();
                        }
                    }
                }

                throw new UnorderableException(left, deps);
            }
        }

        return sorted;
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
        return BaseUtilities.topologicalSort(c, edges);
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
        return BaseUtilities.translate(className);
    }
        
     /** This method merges two images into the new one. The second image is drawn
     * over the first one with its top-left corner at x, y. Images need not be of the same size.
     * New image will have a size of max(second image size + top-left corner, first image size).
     * Method is used mostly when second image contains transparent pixels (e.g. for badging).
     * @param image1 underlying image
     * @param image2 second image
     * @param x x position of top-left corner
     * @param y y position of top-left corner
     * @return new merged image
     * @deprecated Use {@link ImageUtilities#mergeImages}.
     */
    @Deprecated
    public static Image mergeImages(Image image1, Image image2, int x, int y) {
        return ImageUtilities.mergeImages(image1, image2, x, y);
    }
    
    /**
     * Loads an image from the specified resource ID. The image is loaded using the "system" classloader registered in
     * Lookup.
     * @param resourceID resource path of the icon (no initial slash)
     * @return icon's Image, or null, if the icon cannot be loaded.
     * @deprecated Use {@link ImageUtilities#loadImage(java.lang.String)}.
     */
    @Deprecated
    public static Image loadImage(String resourceID) {
        return ImageUtilities.loadImage(resourceID);
    }

    /**
     * Converts given icon to a {@link java.awt.Image}.
     *
     * @param icon {@link javax.swing.Icon} to be converted.
     * @since 7.3
     * @deprecated Use {@link ImageUtilities#icon2Image}.
     */
    @Deprecated
    public static Image icon2Image(Icon icon) {
        return ImageUtilities.icon2Image(icon);
    }

    /** Builds a popup menu from actions for provided context specified by
     * <code>Lookup</code>.
     * Takes list of actions and for actions whic are instances of
     * <code>ContextAwareAction</code> creates and uses the context aware instance.
     * Then gets the action presenter or simple menu item for the action to the
     * popup menu for each action (or separator for each 'lonely' null array member).
     *
     * @param actions array of actions to build menu for. Can contain null
     *   elements, they will be replaced by separators
     * @param context the context for which the popup is build
     * @return the constructed popup menu
     * @see ContextAwareAction
     * @since 3.29
     */
    public static JPopupMenu actionsToPopup(Action[] actions, Lookup context) {
        // keeps actions for which was menu item created already (do not add them twice)
        Set<Action> counted = new HashSet<Action>();
        // components to be added (separators are null)
        List<Component> components = new ArrayList<Component>();

        for (Action action : actions) {
            if (action != null && counted.add(action)) {
                // switch to replacement action if there is some
                if (action instanceof ContextAwareAction) {
                    Action contextAwareAction = ((ContextAwareAction) action).createContextAwareInstance(context);
                    if (contextAwareAction == null) {
                        Logger.getLogger(Utilities.class.getName()).log(Level.WARNING,"ContextAwareAction.createContextAwareInstance(context) returns null. That is illegal!" + " action={0}, context={1}", new Object[] {action, context});
                    } else {
                        action = contextAwareAction;
                    }                    
                }

                JMenuItem item;
                if (action instanceof Presenter.Popup) {
                    item = ((Presenter.Popup) action).getPopupPresenter();
                    if (item == null) {
                        Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "findContextMenuImpl, getPopupPresenter returning null for {0}", action);
                        continue;
                    }
                } else {
                    // We need to correctly handle mnemonics with '&' etc.
                     item = ActionPresenterProvider.getDefault().createPopupPresenter(action);
                }

                for (Component c : ActionPresenterProvider.getDefault().convertComponents(item)) {
                    if (c instanceof JSeparator) {
                        components.add(null);
                    } else {
                        components.add(c);
                    }
                }
            } else {
                components.add(null);
            }
        }

        // Now create actual menu. Strip adjacent, leading, and trailing separators.
        JPopupMenu menu = ActionPresenterProvider.getDefault().createEmptyPopup();
        boolean nonempty = false; // has anything been added yet?
        boolean pendingSep = false; // should there be a separator before any following item?
        for (Component c : components) {
            try {
                if (c == null) {
                    pendingSep = nonempty;
                } else {
                    nonempty = true;
                    if (pendingSep) {
                        pendingSep = false;
                        menu.addSeparator();
                    }
                    menu.add(c);
                }
            } catch (RuntimeException ex) {
                Exceptions.attachMessage(ex, "Current component: " + c); // NOI18N
                Exceptions.attachMessage(ex, "List of components: " + components); // NOI18N
                Exceptions.attachMessage(ex, "List of actions: " + Arrays.asList(actions)); // NOI18N
                Exceptions.printStackTrace(ex);
            }
        }
        return menu;
    }

    /** Builds a popup menu for provided component. It retrieves context
     * (lookup) from provided component instance or one of its parent
     * (it searches up to the hierarchy for <code>Lookup.Provider</code> instance).
     * If none of the components is <code>Lookup.Provider</code> instance, then
     * it is created context which is fed with composite ActionMap which delegates
     * to all components up to hierarchy started from the specified one.
     * Then <code>actionsToPopup(Action[],&nbsp;Lookup)</code>} is called with
     * the found <code>Lookup</code> instance, which actually creates a popup menu.
     *
     * @param actions array of actions to build menu for. Can contain null
     *   elements, they will be replaced by separators
     * @param component a component in which to search for a context
     * @return the constructed popup menu
     * @see Lookup.Provider
     * @see #actionsToPopup(Action[], Lookup)
     * @since 3.29
     */
    public static javax.swing.JPopupMenu actionsToPopup(Action[] actions, java.awt.Component component) {
        Lookup lookup = null;

        for (Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider) c).getLookup();

                if (lookup != null) {
                    break;
                }
            }
        }

        if (lookup == null) {
            // Fallback to composite action map, even it is questionable,
            // whether we should support component which is not (nor
            // none of its parents) lookup provider.
            UtilitiesCompositeActionMap map = new UtilitiesCompositeActionMap(component);
            lookup = org.openide.util.lookup.Lookups.singleton(map);
        }

        return actionsToPopup(actions, lookup);
    }

    /**
     * Load a menu sequence from a lookup path.
     * Any {@link Action} instances are returned as is;
     * any {@link JSeparator} instances are translated to nulls.
     * Warnings are logged for any other instances.
     * @param path a path as given to {@link Lookups#forPath}, generally a layer folder name
     * @return a list of actions interspersed with null separators
     * @since org.openide.util 7.14
     */
    public static List<? extends Action> actionsForPath(String path) {
        List<Action> actions = new ArrayList<Action>();
        for (Lookup.Item<Object> item : Lookups.forPath(path).lookupResult(Object.class).allItems()) {
            if (Action.class.isAssignableFrom(item.getType())) {
                Object instance = item.getInstance();
                if (instance != null) {
                    actions.add((Action) instance);
                }
            } else if (JSeparator.class.isAssignableFrom(item.getType())) {
                actions.add(null);
            } else {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Unrecognized object of {0} found in actions path {1}", new Object[] {item.getType(), path});
            }
        }
        return actions;
    }

    /**
     * Loads a menu sequence from a path, given a specific context as Lookup. This variant will allow to use action's contextual presence, enablement or selection,
     * based on Lookup contents. If the registered action implements a {@link ContextAwareAction}, an instance bound to the passed `context' will be created - if
     * the context factory returns {@code null}, indicating the action is not appropriate for the context, the registration will be skipped.
     * Use {@link #actionsGlobalContext()} to supply global context.
     * 
     * Any {@link Action} instances are returned as is;
     * any {@link JSeparator} instances are translated to nulls.
     * Warnings are logged for any other instances.
     * @param path a path as given to {@link Lookups#forPath}, generally a layer folder name
     * @param context the context passed to the action(s)
     * @return a list of actions interspersed with null separators
     * @since 7.14
     */
    public static List<? extends Action> actionsForPath(String path, Lookup context) {
        List<Action> actions = new ArrayList<Action>();
        for (Lookup.Item<Object> item : Lookups.forPath(path).lookupResult(Object.class).allItems()) {
            if (Action.class.isAssignableFrom(item.getType())) {
                Object instance = item.getInstance();
                if (instance instanceof ContextAwareAction) {
                    Object contextAwareInstance = ((ContextAwareAction)instance).createContextAwareInstance(context);
                    if (contextAwareInstance == null) {
                        Logger.getLogger(Utilities.class.getName()).log(Level.WARNING,"ContextAwareAction.createContextAwareInstance(context) returns null. That is illegal!" + " action={0}, context={1}", new Object[] {instance, context});
                    } else {
                        instance = contextAwareInstance;
                    }
                }
                actions.add((Action) instance);
            } else if (JSeparator.class.isAssignableFrom(item.getType())) {
                actions.add(null);
            } else {
                Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Unrecognized object of {0} found in actions path {1}", new Object[] {item.getType(), path});
            }
        }
        return actions;
    }
    /**
     * Global context for actions. Toolbar, menu or any other "global"
     * action presenters shall operate in this context.
     * Presenters for context menu items should <em>not</em> use
     * this method; instead see {@link ContextAwareAction}.
     * @see ContextGlobalProvider
     * @see ContextAwareAction
     * @see <a href="https://netbeans.apache.org/wiki/DevFaqActionContextSensitive">NetBeans FAQ</a>
     * @return the context for actions
     * @since 4.10
     */
    public static Lookup actionsGlobalContext() {
        synchronized (ContextGlobalProvider.class) {
            if (global != null) {
                return global;
            }
        }

        ContextGlobalProvider p = Lookup.getDefault().lookup(ContextGlobalProvider.class);
        Lookup l = (p == null) ? Lookup.EMPTY : p.createGlobalContext();

        synchronized (ContextGlobalProvider.class) {
            if (global == null) {
                global = l;
            }

            return global;
        }
    }

    //
    // end of actions stuff
    //

    /**
     * Loads an image based on resource path.
     * Exactly like {@link #loadImage(String)} but may do a localized search.
     * For example, requesting <code>org/netbeans/modules/foo/resources/foo.gif</code>
     * might actually find <code>org/netbeans/modules/foo/resources/foo_ja.gif</code>
     * or <code>org/netbeans/modules/foo/resources/foo_mybranding.gif</code>.
     * 
     * <p>Caching of loaded images can be used internally to improve performance.
     * 
     * @since 3.24
     * @deprecated Use {@link ImageUtilities#loadImage(java.lang.String, boolean)}.
     */
    @Deprecated
    public static Image loadImage(String resource, boolean localized) {
        return ImageUtilities.loadImage(resource, localized);
    }

    /**
     *  Returns a cursor with an arrow and an hourglass (or stop watch) badge,
     *  to be used when a component is busy but the UI is still responding to the user.
     *
     *  Similar to the predefined {@link Cursor#WAIT_CURSOR}, but has an arrow to indicate
     *  a still-responsive UI.
     *
     *  <p>Typically you will set the cursor only temporarily:
     *
     *  <pre>
     *  <font class="comment">// code is running in other then event dispatch thread</font>
     *  currentComponent.setCursor(Utilities.createProgressCursor(currentComponent));
     *  <font class="keyword">try</font> {
     *      <font class="comment">// perform some work in other than event dispatch thread
     *      // (do not block UI)</font>
     *  } <font class="keyword">finally</font> {
     *      currentComponent.setCursor(<font class="constant">null</font>);
     *  }
     *  </pre>
     *
     *  <p>This implementation provides one cursor for all Mac systems, one for all
     *  Unix systems (regardless of window manager), and one for all other systems
     *  including Windows. Note: The cursor does not have to look native in some
     *  cases on some platforms!
     *
     *  @param   component the non-null component that will use the progress cursor
     *  @return  a progress cursor (Unix, Windows or Mac)
     *
     * @since 3.23
     */
    public static Cursor createProgressCursor(Component component) {
        // refuse null component
        if (component == null) {
            throw new NullPointerException("Given component is null"); //NOI18N
        }

        Image image = null;

        // First check for Mac because its part of the Unix_Mask
        if (isMac()) {
            image = ImageUtilities.loadImage("org/openide/util/progress-cursor-mac.gif"); //NOI18N
        } else if (isUnix()) {
            image = ImageUtilities.loadImage("org/openide/util/progress-cursor-motif.gif"); //NOI18N
        }
        // All other OS, including Windows, use Windows cursor
        else {
            image = ImageUtilities.loadImage("org/openide/util/progress-cursor-win.gif"); //NOI18N
        }

        return createCustomCursor(component, image, "PROGRESS_CURSOR"); //NOI18N
    }

    // added to fix issue #30665 (bad size on linux)
    public static Cursor createCustomCursor(Component component, Image icon, String name) {
        Toolkit t = component.getToolkit();
        Dimension d = t.getBestCursorSize(16, 16);
        Image i = icon;

        if (d.width != icon.getWidth(null)) {
            if (((d.width) == 0) && (d.height == 0)) {
                // system doesn't support custom cursors, falling back
                return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            }

            // need to resize the icon
            Image empty = ImageUtilities.createBufferedImage(d.width, d.height);
            i = ImageUtilities.mergeImages(icon, empty, 0, 0);
        }

        return t.createCustomCursor(i, new Point(1, 1), name);
    }

    /** Attaches asynchronous init job to given component.
     * {@link AsyncGUIJob#construct()} will be called after first
     * paint, when paint event arrives. Later, {@link AsyncGUIJob#finished()}
     * will be called according to the rules of the <code>AsyncGUIJob</code> interface.
     *
     * Useful for components that have slower initialization phase, component
     * can benefit from more responsive behaviour during init.
     *
     * @param comp4Init Regular component in its pre-inited state, state in which
     *        component will be shown between first paint and init completion.
     * @param initJob Initialization job to be called asynchronously. Job can
     *            optionally implement {@link Cancellable}
     *            interface for proper cancel logic. Cancel method will be called
     *            when component stops to be showing during job's progress.
     *            See {@link java.awt.Component#isShowing}
     *
     * @since 3.36
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void attachInitJob(Component comp4Init, AsyncGUIJob initJob) {
        new AsyncInitSupport(comp4Init, initJob);
    }

    /**
     * Converts a file to a URI while being safe for UNC paths.
     * Uses {@link File f}.{@link File#toPath() toPath}().{@link Path#toUri() toUri}()
     * which results into {@link URI} that works with {@link URI#normalize()}
     * and {@link URI#resolve(URI)}.
     * @param f a file
     * @return a {@code file}-protocol URI which may use the host field
     * @see java.nio.file.Path#toUri()
     * @since 8.25
     */
    public static URI toURI(File f) {
        return BaseUtilities.toURI(f);
    }

    /**
     * Converts a URI to a file while being safe for UNC paths.
     * Uses {@link Paths#get(java.net.URI) Paths.get}(u).{@link Path#toFile() toFile}()
     * which accepts UNC URIs with a host field.
     * @param u a {@code file}-protocol URI which may use the host field
     * @return a file
     * @see java.nio.file.Paths#get(java.net.URI)
     * @since 8.25
     */
    public static File toFile(URI u) throws IllegalArgumentException {
        return BaseUtilities.toFile(u);
    }

    /**
     * Convert a file to a matching <code>file:</code> URL.
     * @param f a file (absolute only)
     * @return a URL using the <code>file</code> protocol
     * @throws MalformedURLException for no good reason
     * @see #toFile
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=29711">Issue #29711</a>
     * @since 3.26
     * @deprecated Use {@link #toURI} and {@link URI#toURL} instead under JDK 1.4.
     *             ({@link File#toURL} is buggy in JDK 1.3 and the bugs are not fixed in JDK 1.4.)
     */
    @Deprecated
    public static URL toURL(File f) throws MalformedURLException {
        if (f == null) {
            throw new NullPointerException();
        }

        if (!f.isAbsolute()) {
            throw new IllegalArgumentException("Relative path: " + f); // NOI18N
        }

        URI uri = toURI(f);

        return uri.toURL();
    }

    /**
     * Convert a <code>file:</code> URL to a matching file.
     * <p>You may not use a URL generated from a file on a different
     * platform, as file name conventions may make the result meaningless
     * or even unparsable.
     * @param u a URL with the <code>file</code> protocol
     * @return an absolute file it points to, or <code>null</code> if the URL
     *         does not seem to point to a file at all
     * @see #toURL
     * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=29711">Issue #29711</a>
     * @since 3.26
     * @deprecated Use {@link URL#toURI} and {@link #toFile(URI)} instead under JDK 1.4.
     *             (There was no proper equivalent under JDK 1.3.)
     */
    @Deprecated
    public static File toFile(URL u) {
        if (u == null) {
            throw new NullPointerException();
        }

        try {
            URI uri = u.toURI();

            return toFile(uri);
        } catch (URISyntaxException use) {
            // malformed URL
            return null;
        } catch (IllegalArgumentException iae) {
            // not a file: URL
            return null;
        }
    }

    /**
     * On some platform this method makes a short audible beep, use it when user 
     * tries to invoke an action that's disabled.
     * Some platforms, e.g. MS Windows do not emit any sound in such cases.
     * @since 8.39
     */
    public static void disabledActionBeep() {
        if( isWindows() ) {
            //no sound on MS Windows
            return;
        }
        Toolkit.getDefaultToolkit().beep();
    }

   /** Exception indicating that a given list could not be partially-ordered.
    * @see #partialSort
    * @deprecated Used only by the deprecated partialSort
    */
    @Deprecated
    @SuppressWarnings("rawtypes")
    public static class UnorderableException extends RuntimeException {
        static final long serialVersionUID = 6749951134051806661L;
        private Collection unorderable;
        private Map deps;

        /** Create a new unorderable-list exception with no detail message.
        * @param unorderable a collection of list elements which could not be ordered
        *                    (because there was some sort of cycle)
        * @param deps dependencies associated with the list; a map from list elements
        *             to sets of list elements which that element must appear after
        */
        public UnorderableException(Collection unorderable, Map deps) {
            super( /* "Cannot be ordered: " + unorderable */
            ); // NOI18N
            this.unorderable = unorderable;
            this.deps = deps;
        }

        /** Create a new unorderable-list exception with a specified detail message.
        * @param message the detail message
        * @param unorderable a collection of list elements which could not be ordered
        *                    (because there was some sort of cycle)
        * @param deps dependencies associated with the list; a map from list elements
        *             to sets of list elements which that element must appear after
        */
        public UnorderableException(String message, Collection unorderable, Map deps) {
            super(message);
            this.unorderable = unorderable;
            this.deps = deps;
        }

        /** Get the unorderable elements.
        * @return the elements
        * @see UnorderableException#UnorderableException(Collection,Map)
        */
        public Collection getUnorderable() {
            return unorderable;
        }

        /** Get the dependencies.
        * @return the dependencies
        * @see UnorderableException#UnorderableException(Collection,Map)
        */
        public Map getDeps() {
            return deps;
        }
    }
}
