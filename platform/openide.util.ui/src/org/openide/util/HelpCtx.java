/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.openide.util.lookup.ServiceProvider;

/** Provides help for any window or other feature in the system.
* It is designed to be JavaHelp-compatible and to use the same tactics when
* assigning help to {@link JComponent} instances.
* @see <a href="@org-netbeans-modules-javahelp@/">JavaHelp Integration API</a>
* @author Petr Hamernik, Jaroslav Tulach, Jesse Glick
*/
public final class HelpCtx extends Object {
    private static final Logger err = Logger.getLogger("org.openide.util.HelpCtx"); // NOI18N

    /** Default help page.
    * This (hopefully) points to a note explaining to the user that no help is available.
    * Precisely, the Help ID is set to <code>org.openide.util.HelpCtx.DEFAULT_HELP</code>.
    */
    public static final HelpCtx DEFAULT_HELP = new HelpCtx(HelpCtx.class.getName() + ".DEFAULT_HELP"); // NOI18N

    /** URL of the help page */
    private final URL helpCtx;

    /** JavaHelp ID for the help */
    private final String helpID;

    /** Create a help context by URL.
     * @deprecated Does not work nicely with JavaHelp.
    * @param helpCtx URL to point help to
    */
    @Deprecated
    public HelpCtx(URL helpCtx) {
        this.helpCtx = helpCtx;
        this.helpID = null;
    }

    /** Create a help context by tag.
    * You must provide an ID of the
    * desired help for the item. The ID should refer to an
    * already installed help; this can be easily installed by specifying
    * a JavaHelp help set for the module (see the JavaHelp API for details).
    *
    * @param helpID the JavaHelp ID of the help
    */
    public HelpCtx(String helpID) {
        this.helpID = helpID;
        this.helpCtx = null;
    }

    /** Create a help context by class.
    * Assigns the name of a class as
    * the ID.
    *
    * @param clazz the class to take the name from
    * @deprecated Too easily breaks IDs after code refactoring. Rather use {@link #HelpCtx(String)} with a constant value known to be in the JavaHelp map.
    */
    @Deprecated
    public HelpCtx(Class<?> clazz) {
        this(clazz.getName());
    }

    /** Get a URL to the help page, if applicable.
    * @return a URL to the page, or <code>null</code> if the target was specified by ID
    */
    public URL getHelp() {
        return helpCtx;
    }

    /** Get the ID of the help page, if applicable.
    * @return the JavaHelp ID string, or <code>null</code> if specified by URL
    */
    public String getHelpID() {
        return helpID;
    }

    /**
     * Displays the help page in a supported viewer, if any.
     * @return true if this help was displayed successfully
     * @since 8.21
     * @see org.openide.util.HelpCtx.Displayer#display
     */
    public boolean display() {
        for (Displayer d : Lookup.getDefault().lookupAll(Displayer.class)) {
            if (d.display(this)) {
                return true;
            }
        }
        return false;
    }

    // object identity
    @Override
    public int hashCode() {
        int base = HelpCtx.class.hashCode();

        if (helpCtx != null) {
            base ^= helpCtx.hashCode();
        }

        if (helpID != null) {
            base ^= helpID.hashCode();
        }

        return base;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HelpCtx other = (HelpCtx) obj;
        if (this.helpCtx != other.helpCtx && (this.helpCtx == null || !this.helpCtx.equals(other.helpCtx))) {
            return false;
        }
        if ((this.helpID == null) ? (other.helpID != null) : !this.helpID.equals(other.helpID)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        if (helpID != null) {
            return "HelpCtx[" + helpID + "]"; // NOI18N
        } else {
            return "HelpCtx[" + helpCtx + "]"; // NOI18N
        }
    }

    /** Set the help ID for a component.
    * @param comp the visual component to associate help to
    * @param helpID help ID, or <code>null</code> if the help ID should be removed
    */
    public static void setHelpIDString(JComponent comp, String helpID) {
        err.log(Level.FINE, "setHelpIDString: {0} on {1}", new Object[]{helpID, comp});

        comp.putClientProperty("HelpID", helpID); // NOI18N
    }

    /** Find the help ID for a component.
     * If the component implements {@link org.openide.util.HelpCtx.Provider},
     * its method {@link org.openide.util.HelpCtx.Provider#getHelpCtx} is called.
     * If the component has help attached by {@link #setHelpIDString}, it returns that.
     * Otherwise it checks the parent component recursively.
     *
     * @param comp the component to find help for
     * @return the help for that component (never <code>null</code>)
     */
    public static HelpCtx findHelp(java.awt.Component comp) {
        if (err.isLoggable(Level.FINEST)) {
            err.log(Level.FINEST, "findHelp on " + comp, new Exception());
        } else {
            err.log(Level.FINE, "findHelp on {0}", comp);
        }

        while (comp != null) {
            if (comp instanceof HelpCtx.Provider) {
                HelpCtx h = ((HelpCtx.Provider) comp).getHelpCtx();

                err.log(Level.FINE, "found help {0} through HelpCtx.Provider interface", h);

                return h;
            }

            if (comp instanceof JComponent) {
                JComponent jc = (JComponent) comp;
                String hid = (String) jc.getClientProperty("HelpID"); // NOI18N

                if (hid != null) {
                    err.log(Level.FINE, "found help {0} by client property", hid);

                    return new HelpCtx(hid);
                }
            }

            comp = comp.getParent();

            err.log(Level.FINE, "no luck, trying parent {0}", comp);
        }

        err.fine("nothing found");

        return DEFAULT_HELP;
    }

    /** Finds help context for a generic object. Right now checks
     * for HelpCtx.Provider and handles java.awt.Component in a
     * special way compatible with JavaHelp.
     * Also {@link BeanDescriptor}'s are checked for a string-valued attribute
     * <code>helpID</code>, as per the JavaHelp specification (but no help sets
     * will be loaded).
     *
     * @param instance to search help for
     * @return the help for the object or <code>HelpCtx.DEFAULT_HELP</code> if HelpCtx cannot be found
     *
     * @since 4.3
     */
    public static HelpCtx findHelp(Object instance) {
        if (instance instanceof java.awt.Component) {
            return findHelp((java.awt.Component) instance);
        }

        if (instance instanceof HelpCtx.Provider) {
            return ((HelpCtx.Provider) instance).getHelpCtx();
        }

        try {
            BeanDescriptor d = Introspector.getBeanInfo(instance.getClass()).getBeanDescriptor();
            String v = (String) d.getValue("helpID"); // NOI18N

            if (v != null) {
                return new HelpCtx(v);
            }
        } catch (IntrospectionException e) {
            err.log(Level.FINE, "findHelp on {0}: {1}", new Object[]{instance, e});
        }

        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * An object implementing this interface is willing to answer
     * the HelpCtx.findHelp() query itself.
     *
     * @since 3.20
     */
    public static interface Provider {
        /**
         * Get the {@link HelpCtx} associated with implementing object.
         * @return assigned <code>HelpCtx</code> or
         *         {@link #DEFAULT_HELP}, never <code>null</code>.
         */
        public HelpCtx getHelpCtx();
    }

    /**
     * Service to display a {@link HelpCtx} in a help viewer.
     * Permits modules with minimal API dependencies to display JavaHelp where supported.
     * @see ServiceProvider
     * @see #display()
     * @since 8.21
     */
    public interface Displayer {

        /**
         * Displays a help page.
         * @param help a help ID to display
         * @return true if it was displayed successfully
         */
        boolean display(HelpCtx help);

    }

}
