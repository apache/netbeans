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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.List;
import java.lang.reflect.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.net.*;
import java.text.BreakIterator;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;

import org.openide.ErrorManager;

/** Otherwise uncategorized useful static methods.
*
* @author Jan Palka, Ian Formanek, Jaroslav Tulach
*/
public final class Utilities {
    private Utilities() {}

    /** Operating system is Windows NT. */
    public static final int OS_WINNT = 1 << 0;
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

    /** A mask for Windows platforms. */
    public static final int OS_WINDOWS_MASK = OS_WINNT | OS_WIN95 | OS_WIN98 | OS_WIN2000 | OS_WIN_OTHER;
    /** A mask for Unix platforms. */
    public static final int OS_UNIX_MASK = OS_SOLARIS | OS_LINUX | OS_HP | OS_AIX | OS_IRIX | OS_SUNOS | OS_TRU64 | OS_MAC | OS_FREEBSD;

    /** A height of the windows's taskbar */
    public static final int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27;

    /** A height of the Mac OS X's menu */
    private static final int TYPICAL_MACOSX_MENU_HEIGHT = 24;
    
    /** Get the operating system on which the IDE is running.
    * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
    */
    public static final int getOperatingSystem () {
        if (operatingSystem == -1) {
            String osName = System.getProperty ("os.name");
            if ("Windows NT".equals (osName)) // NOI18N
                operatingSystem = OS_WINNT;
            else if ("Windows 95".equals (osName)) // NOI18N
                operatingSystem = OS_WIN95;
            else if ("Windows 98".equals (osName)) // NOI18N
                operatingSystem = OS_WIN98;
            else if ("Windows 2000".equals (osName)) // NOI18N
                operatingSystem = OS_WIN2000;
            else if (osName.startsWith("Windows ")) // NOI18N
                operatingSystem = OS_WIN_OTHER;
            else if ("Solaris".equals (osName)) // NOI18N
                operatingSystem = OS_SOLARIS;
            // JDK 1.4 b2 defines os.name for me as "Redhat Linux" -jglick
            else if (osName.endsWith ("Linux")) // NOI18N
                operatingSystem = OS_LINUX;
            else if ("HP-UX".equals (osName)) // NOI18N
                operatingSystem = OS_HP;
            else if ("AIX".equals (osName)) // NOI18N
                operatingSystem = OS_AIX;
            else if ("Irix".equals (osName)) // NOI18N
                operatingSystem = OS_IRIX;
            else if ("Digital UNIX".equals (osName)) // NOI18N
                operatingSystem = OS_TRU64;
            else if ("OS/2".equals (osName)) // NOI18N
                operatingSystem = OS_OS2;
            else if ("OpenVMS".equals (osName)) // NOI18N
                operatingSystem = OS_VMS;
            else if (osName.equals ("Mac OS X")) // NOI18N
                operatingSystem = OS_MAC;
            else if (osName.startsWith ("Darwin")) // NOI18N
                operatingSystem = OS_MAC;
            else if (osName.toLowerCase (Locale.US).startsWith ("freebsd")) { // NOI18N 
                operatingSystem = OS_FREEBSD;
            }
            else
                operatingSystem = OS_OTHER;
        }
        return operatingSystem;
    }

    /** Test whether the IDE is running on some variant of Windows.
    * @return <code>true</code> if Windows, <code>false</code> if some other manner of operating system
    */
    public static final boolean isWindows () {
        return (getOperatingSystem () & OS_WINDOWS_MASK) != 0;
    }

    /** Test whether the IDE is running on some variant of Unix.
    * Linux is included as well as the commercial vendors.
    * @return <code>true</code> some sort of Unix, <code>false</code> if some other manner of operating system
    */
    public static final boolean isUnix () {
        return (getOperatingSystem () & OS_UNIX_MASK) != 0;
    }

    /** The operating system on which NetBeans runs*/
    private static int operatingSystem = -1;
    
    // only for UtilitiesTest purposes
    static final void resetOperatingSystem () {
        operatingSystem = -1;
    }

    /**
    * Convert an array of objects to an array of primitive types.
    * E.g. an <code>Integer[]</code> would be changed to an <code>int[]</code>.
    * @param array the wrapper array
    * @return a primitive array
    * @throws IllegalArgumentException if the array element type is not a primitive wrapper
    */
    public static Object toPrimitiveArray (Object[] array) {
        if (array instanceof Integer[]) {
            int[] r = new int [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Integer)array[i]) == null) ? 0 : ((Integer)array[i]).intValue ();
            return r;
        }
        if (array instanceof Boolean[]) {
            boolean[] r = new boolean [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Boolean)array[i]) == null) ? false : ((Boolean)array[i]).booleanValue ();
            return r;
        }
        if (array instanceof Byte[]) {
            byte[] r = new byte [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Byte)array[i]) == null) ? 0 : ((Byte)array[i]).byteValue ();
            return r;
        }
        if (array instanceof Character[]) {
            char[] r = new char [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Character)array[i]) == null) ? 0 : ((Character)array[i]).charValue ();
            return r;
        }
        if (array instanceof Double[]) {
            double[] r = new double [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Double)array[i]) == null) ? 0 : ((Double)array[i]).doubleValue ();
            return r;
        }
        if (array instanceof Float[]) {
            float[] r = new float [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Float)array[i]) == null) ? 0 : ((Float)array[i]).floatValue ();
            return r;
        }
        if (array instanceof Long[]) {
            long[] r = new long [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Long)array[i]) == null) ? 0 : ((Long)array[i]).longValue ();
            return r;
        }
        if (array instanceof Short[]) {
            short[] r = new short [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Short)array[i]) == null) ? 0 : ((Short)array[i]).shortValue ();
            return r;
        }
        throw new IllegalArgumentException ();
    }

    /**
    * Convert an array of primitive types to an array of objects.
    * E.g. an <code>int[]</code> would be turned into an <code>Integer[]</code>.
    * @param array the primitive array
    * @return a wrapper array
    * @throws IllegalArgumentException if the array element type is not primitive
    */
    public static Object[] toObjectArray (Object array) {
        if (array instanceof Object[]) return (Object[]) array;
        if (array instanceof int[]) {
            int i, k = ((int[])array).length;
            Integer[] r = new Integer [k];
            for (i = 0; i < k; i++) r [i] = new Integer (((int[]) array)[i]);
            return r;
        }
        if (array instanceof boolean[]) {
            int i, k = ((boolean[])array).length;
            Boolean[] r = new Boolean [k];
            for (i = 0; i < k; i++) r [i] = ((boolean[]) array)[i] ? Boolean.TRUE : Boolean.FALSE;
            return r;
        }
        if (array instanceof byte[]) {
            int i, k = ((byte[])array).length;
            Byte[] r = new Byte [k];
            for (i = 0; i < k; i++) r [i] = new Byte (((byte[]) array)[i]);
            return r;
        }
        if (array instanceof char[]) {
            int i, k = ((char[])array).length;
            Character[] r = new Character [k];
            for (i = 0; i < k; i++) r [i] = new Character (((char[]) array)[i]);
            return r;
        }
        if (array instanceof double[]) {
            int i, k = ((double[])array).length;
            Double[] r = new Double [k];
            for (i = 0; i < k; i++) r [i] = new Double (((double[]) array)[i]);
            return r;
        }
        if (array instanceof float[]) {
            int i, k = ((float[])array).length;
            Float[] r = new Float [k];
            for (i = 0; i < k; i++) r [i] = new Float (((float[]) array)[i]);
            return r;
        }
        if (array instanceof long[]) {
            int i, k = ((long[])array).length;
            Long[] r = new Long [k];
            for (i = 0; i < k; i++) r [i] = new Long (((long[]) array)[i]);
            return r;
        }
        if (array instanceof short[]) {
            int i, k = ((short[])array).length;
            Short[] r = new Short [k];
            for (i = 0; i < k; i++) r [i] = new Short (((short[]) array)[i]);
            return r;
        }
        throw new IllegalArgumentException ();
    }

    /**
    * Get the object type for given primitive type.
    *
    * @param c primitive type (e.g. <code>int</code>)
    * @return object type (e.g. <code>Integer</code>)
    */
    public static Class getObjectType (Class c) {
        if (!c.isPrimitive ()) return c;
        if (c == Integer.TYPE) return Integer.class;
        if (c == Boolean.TYPE) return Boolean.class;
        if (c == Byte.TYPE) return Byte.class;
        if (c == Character.TYPE) return Character.class;
        if (c == Double.TYPE) return Double.class;
        if (c == Float.TYPE) return Float.class;
        if (c == Long.TYPE) return Long.class;
        if (c == Short.TYPE) return Short.class;
        throw new IllegalArgumentException ();
    }

    /**
    * Get the primitive type for given object type.
    *
    * @param c object type (e.g. <code>Integer</code>)
    * @return primitive type (e.g. <code>int</code>)
    */
    public static Class getPrimitiveType (Class c) {
        if (!c.isPrimitive ()) return c;
        if (c == Integer.class) return Integer.TYPE;
        if (c == Boolean.class) return Boolean.TYPE;
        if (c == Byte.class) return Byte.TYPE;
        if (c == Character.class) return Character.TYPE;
        if (c == Double.class) return Double.TYPE;
        if (c == Float.class) return Float.TYPE;
        if (c == Long.class) return Long.TYPE;
        if (c == Short.class) return Short.TYPE;
        throw new IllegalArgumentException ();
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
        Frame[] frames = Frame.getFrames();

        for (int i = 0; i < frames.length; i++) {
            if (javax.swing.SwingUtilities.findFocusOwner(frames[i]) != null) {
                return frames[i].getGraphicsConfiguration();
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
     * windows.  The method subtracts from the screen the area of taskbars,
     * system menus and the like.
     * 
     * @param gconf the GraphicsConfiguration of the monitor
     * @return the rectangle of the screen where one can place windows
     * 
     * @since 2.5
     */
    public static Rectangle getUsableScreenBounds(GraphicsConfiguration gconf) {
        if (gconf == null)
            gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        Rectangle bounds = new Rectangle(gconf.getBounds());
        
        String str;

        str = System.getProperty("netbeans.screen.insets"); // NOI18N
        if (str != null) {
            StringTokenizer st = new StringTokenizer(str, ", "); // NOI18N
            if (st.countTokens() == 4) {
                try {
                    bounds.y = Integer.parseInt(st.nextToken());
                    bounds.x = Integer.parseInt(st.nextToken());
                    bounds.height -= bounds.y + Integer.parseInt(st.nextToken());
                    bounds.width -= bounds.x + Integer.parseInt(st.nextToken());
                }
                catch (NumberFormatException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
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
            Insets insets = toolkit.getScreenInsets (gconf);
                bounds.y += insets.top;
                bounds.x += insets.left;
                bounds.height -= insets.top + insets.bottom;
                bounds.width -= insets.left + insets.right;
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
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
        return findCenterBounds(getCurrentGraphicsConfiguration(),
                                componentSize);
    }

    /**
     * Helps client code place components on the center of the screen.  It
     * handles multiple monitor configuration correctly
     *
     * @param gconf the GraphicsConfiguration of the monitor
     * @param componentSize the size of the component
     * @return bounds of the centered component
     */
    private static Rectangle findCenterBounds(GraphicsConfiguration gconf,
                                              Dimension componentSize) {
        if (gconf == null)
            gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        Rectangle bounds = gconf.getBounds();
        return new Rectangle(bounds.x + (bounds.width - componentSize.width) / 2,
                             bounds.y + (bounds.height - componentSize.height) / 2,
                             componentSize.width,
                             componentSize.height);
    }
}
