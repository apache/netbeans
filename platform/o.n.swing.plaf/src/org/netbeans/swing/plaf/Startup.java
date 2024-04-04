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

package org.netbeans.swing.plaf;

import java.awt.Toolkit;
import java.util.logging.Logger;
import org.netbeans.swing.plaf.aqua.AquaLFCustoms;
import org.netbeans.swing.plaf.gtk.GtkLFCustoms;
import org.netbeans.swing.plaf.metal.MetalLFCustoms;
import org.netbeans.swing.plaf.util.NbTheme;
import org.netbeans.swing.plaf.util.RelativeColor;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.swing.plaf.nimbus.NimbusLFCustoms;
import org.netbeans.swing.plaf.winclassic.WindowsLFCustoms;
import org.netbeans.swing.plaf.windows8.Windows8LFCustoms;
import org.netbeans.swing.plaf.winvista.VistaLFCustoms;
import org.netbeans.swing.plaf.winxp.XPLFCustoms;

/** Singleton, manages customizers for various LFs. Installs, uninstalls them on LF change.
 * LF customizers works with Swing UIManager, putting info about various UI elements
 * in it. Other modules then can query UIManager to get UI elements to get needed
 * visual design and behaviour.
 *
 * @author  Dafe Simonek, Tim Boudreau
 */
public final class Startup {
    //originally LFCustomsManager

    /** For debugging purposes, enable forcing the customizations for, i.e.,
     * Windows look and feel on a platform that doesn't support it */
    private static final String FORCED_CUSTOMS = System.getProperty("nb.forceui"); //NOI18N

    /** Provides the ability to disable customizations for applications which, for example, provide their own
     * subclass of MetalLookAndFeel.  See issue XXX
     */
    private static final boolean NO_CUSTOMIZATIONS = Boolean.getBoolean("netbeans.plaf.disable.ui.customizations"); //NOI18N

    /** Constant for Nimbus L&amp;F name */
    private static final String NIMBUS="Nimbus";

    /** Singleton instance */
    private static Startup instance = null;

    /** Currently used LF customizer */
    private LFCustoms curCustoms = null;
    private LFCustoms globalCustoms = null;

    private static URL themeURL = null;
    private static Class uiClass = null;
    private static ResourceBundle bundle;

    private boolean installed = false;

    /** Starts handling of LF customizers. Called only from getInstance. */
    private Startup() {
        initialize();
    }

    /** Initializes defaulf customs for all LFs and fills UIManager with
     * references to LF customizers for supported LFs.
     */
    private void initialize() {
        LFInstanceOrName lfon = getLookAndFeel();
        boolean forceLaf = false;
        if (lfon.lf instanceof MetalLookAndFeel) {
            //Metal theme must be assigned before using the look and feel
            forceLaf = installTheme(lfon.lf);
        }
        // overall defaults for all LFs
        // defaults for supported LFs

        try {
            if (forceLaf ||
                (lfon.lf != null && lfon.lf != UIManager.getLookAndFeel()) ||
                (lfon.lfClassName != null && !lfon.lfClassName.equals(UIManager.getLookAndFeel().getClass().getName()))) {
                if (lfon.lf != null) {
                    UIManager.setLookAndFeel(lfon.lf);
                    uiClass = lfon.lf.getClass();
                } else {
                    boolean success = false;
                    try {
                        UIManager.setLookAndFeel(lfon.lfClassName);
                        success = true;
                        uiClass = UIManager.getLookAndFeel().getClass();
                    } catch (ClassNotFoundException ex) {
                        System.err.println("Custom UI class " + lfon.lfClassName + " not found."); // NOI18N
                    } catch (IllegalAccessException ex) {
                        System.err.println("Custom UI class " + lfon.lfClassName + " not possible to access."); // NOI18N
                    } catch (InstantiationException ex) {
                        System.err.println("Custom UI class " + lfon.lfClassName + " not possible to instantiate."); // NOI18N
                    } catch (UnsupportedLookAndFeelException ex) {
                        System.err.println("Custom UI class " + lfon.lfClassName + " not supported as a look & feel."); // NOI18N
                    }
                    if (!success) {
                        //#144402 - try fallback to Metal L&F
                        LookAndFeel mlf = new MetalLookAndFeel();
                        installTheme(mlf);
                        UIManager.setLookAndFeel(mlf);
                        uiClass = MetalLookAndFeel.class;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println ("Could not install look and feel " + lfon.getClassName());
        }
    }

    private LFInstanceOrName getLookAndFeel() {
      // related to #118534 - log info about Nimbus L&F
      if (uiClass != null && uiClass.getName().contains(NIMBUS)) {
          Logger.getLogger(getClass().getName()).warning(
                  "L&F Warning: Nimbus L&F is not supported L&F yet and system " +
                  "may exhibit various drawing problems. Please use for experimental purposes only.");
      }

      if (uiClass == null) {
          ResourceBundle b = bundle != null ? bundle : ResourceBundle.getBundle("org.netbeans.swing.plaf.Bundle"); // NOI18N
          String uiClassName = b.getString("LookAndFeelClassName"); // NOI18N
          if ("default".equals(uiClassName)) { // NOI18N
              uiClassName = defaultLaF();
          }
          if (uiClassName.equals(MetalLookAndFeel.class.getName())) {
              return new LFInstanceOrName(new MetalLookAndFeel());
          } else {
            return new LFInstanceOrName(uiClassName);
          }
      } else {
          LookAndFeel lf = UIManager.getLookAndFeel();
          if (uiClass != lf.getClass()) {
              try {
                lf = (LookAndFeel) uiClass.getDeclaredConstructor().newInstance();
              } catch (ReflectiveOperationException ex) {
                  return new LFInstanceOrName(uiClass.getName());
              }
          }
          return new LFInstanceOrName(lf);
      }
    }

    /** Default NetBeans logic for finding out the right L&amp;F.
     * @return name of the LaF to instantiate
     */
    private static String defaultLaF() {
        String uiClassName;
        if (isWindows()) {
            uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //NOI18N
        } else if (isMac()) {
            uiClassName = "com.apple.laf.AquaLookAndFeel";
        } else if (shouldUseMetal()) {
            uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel"; //NOI18N
        } else {
            //Should get us metal where it doesn't get us GTK
            uiClassName = UIManager.getSystemLookAndFeelClassName();
            // Enable GTK L&F only for JDK version 1.6.0 update 1 and later.
            // GTK L&F quality unacceptable for earlier versions.
            // Also enable GTK L&F for OpenJDK
            String javaVersion = System.getProperty("java.version");
            if ("1.6.0_01".compareTo(javaVersion) > 0 && System.getProperty("java.vm.name") != null && System.getProperty("java.vm.name").indexOf("OpenJDK") < 0) {
                // IDE runs on 1.5 or 1.6 - useGtk can enabled Gtk
                if (uiClassName.indexOf("gtk") >= 0 && !Boolean.getBoolean("useGtk")) {
                    uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
                }
            } else {
                // IDE runs on 1.6_0_01 or higher - useGtk can disabled Gtk
                if (uiClassName.indexOf("gtk") >= 0 && System.getProperty("useGtk") != null && !Boolean.getBoolean("useGtk")) {
                    uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
                }
            }
            // #118534 - don't allow Nimbus L&F as default system L&F,
            // as we're not ready to support it yet
            if (uiClassName.contains("Nimbus")) {
                uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel";
            }
        }
        return uiClassName;
    }

    private boolean installTheme(LookAndFeel lf) {
        boolean themeInstalled = false;
        //Load the theme
        if (themeURL != null) {
            themeInstalled = true;
            NbTheme nbTheme = new NbTheme(themeURL, lf);
            MetalLookAndFeel.setCurrentTheme(nbTheme);
        }
        return themeInstalled;
    }

    /** Enables, installs LF customization.  */
    private void install () {
        if (installed) {
            return;
        }
        if (globalCustoms == null) {
            globalCustoms = new AllLFCustoms();
            installLFCustoms (globalCustoms);
        }
        installPerLFDefaults();
        installTheme(UIManager.getLookAndFeel());

        runPostInstall();

        attachListener();
    }

    private void installPerLFDefaults() {
        boolean isLFChange = curCustoms != null;

        curCustoms = findCustoms();
        if (curCustoms != null) {
            Integer in = (Integer) UIManager.get(LFCustoms.CUSTOM_FONT_SIZE); //NOI18N
            if (in == null && UIManager.getLookAndFeel().getClass() == MetalLookAndFeel.class) {
                in = new Integer (11);
            }

            //#161761: Do not want to use font size param for GTK L&F because it causes mixed font size
            if ((in != null) && !UIUtils.isGtkLF()) {
                AllLFCustoms.initCustomFontSize (in.intValue());
            }
            installLFCustoms (curCustoms);
            if (isLFChange) {
                //make sure UIBootstrapValue.Lazy instances really get a chance
                //to replace their values
                loadAllLazyValues (curCustoms);
            }
            curCustoms.disposeValues();
        }
        installSmoothScrollPane();
    }

    private void installSmoothScrollPane() {
      UIDefaults defaults = UIManager.getDefaults();
      final String SCROLL_PANE_UI_CLASS_ID = "ScrollPaneUI";
      Object existingValue = defaults.get(SCROLL_PANE_UI_CLASS_ID);
      if ("javax.swing.plaf.basic.BasicScrollPaneUI".equals(existingValue))
        defaults.put(SCROLL_PANE_UI_CLASS_ID, "org.netbeans.swing.plaf.util.SmoothScrollPaneUI");
    }

    private void loadAllLazyValues (LFCustoms customs) {
        if (globalCustoms != null) {
            loadLazy (globalCustoms.getApplicationSpecificKeysAndValues());
            loadLazy (globalCustoms.getGuaranteedKeysAndValues());
            loadLazy (globalCustoms.getLookAndFeelCustomizationKeysAndValues());
        }
        loadLazy (customs.getApplicationSpecificKeysAndValues());
        loadLazy (customs.getGuaranteedKeysAndValues());
        loadLazy (customs.getLookAndFeelCustomizationKeysAndValues());
    }

    private void loadLazy (Object[] o) {
        if (o.length > 0) {
            UIDefaults uidefaults = UIManager.getDefaults();
            for (int i=1; i < o.length; i+=2) {
                if (o[i] instanceof UIBootstrapValue.Lazy) {
                    ((UIBootstrapValue.Lazy) o[i]).createValue(uidefaults);
                }
                if (o[i] instanceof RelativeColor) {
                    ((RelativeColor) o[i]).clear();
                }
            }
        }
    }

    private void uninstallPerLFDefaults() {
        assert globalCustoms != null;

        if (curCustoms != null) {
            Set<Object> keep = new HashSet<Object> (Arrays.asList(globalCustoms.allKeys()));
            Object[] arr = curCustoms.allKeys();

            for (int i=0; i < arr.length; i++) {
                Object key = arr[i];
                if (!keep.contains(key)) {
                    UIManager.put (key, null);
                }
            }
        }
    }

    private void attachListener() {
        assert listener == null;
        listener = new LFListener();
        UIManager.addPropertyChangeListener(listener);
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "win.xpstyle.themeActive", listener); //NOI18N
    }

    private void installLFCustoms (LFCustoms customs) {
        UIDefaults defaults = UIManager.getDefaults();

        // to make sure we always use system classloader
        defaults.put("ClassLoader", new CLValue()); // NOI18N

        //Install values that some look and feels may leave out, which should
        //be included
        defaults.putDefaults (customs.getGuaranteedKeysAndValues());
        //Install entries for custom NetBeans components, such as borders and
        //colors
        defaults.putDefaults (customs.getApplicationSpecificKeysAndValues());

        if (!NO_CUSTOMIZATIONS) {
            //See issue nnn - Nokia uses a custom metal-based look and feel,
            //and do not want fonts or other things customized
            defaults.putDefaults (customs.getLookAndFeelCustomizationKeysAndValues());
        }

        if (defaults.getBoolean("windowDefaultLookAndFeelDecorated")) {
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
    }

    private void runPostInstall() {
        final Object postInit = UIManager.get( "nb.laf.postinstall.callable" ); //NOI18N
        if( postInit instanceof Callable ) {
            SwingUtilities.invokeLater( new Runnable() {

                @Override
                public void run() {
                    try {
                        ((Callable)postInit).call();
                    } catch( Exception ex ) {
                        Logger.getLogger( Startup.class.getName() ).log( Level.INFO, null, ex );
                    }
                }
            });
        }
    }

    private static ClassLoader loader;
    /**
     * Set a class loader to be used in place of {@link Thread#getContextClassLoader}.
     * @param loader a system-wide class loader
     * @since org.netbeans.swing.plaf 1.15
     */
    public static void setClassLoader(ClassLoader loader) {
        Startup.loader = loader;
    }

    /** Gets the value of system class loader and returns it.
     */
    private static final class CLValue implements UIDefaults.ActiveValue {
        public @Override ClassLoader createValue(UIDefaults defs) {
            return loader != null ? loader : Thread.currentThread().getContextClassLoader();
         }
    }

    /** Finds and returns instance of LF customizer which is suitable for
     * current look and feel.
     */
    private LFCustoms findCustoms () {
        ResourceBundle b = bundle != null ? bundle : ResourceBundle.getBundle( "org.netbeans.swing.plaf.Bundle" ); // NOI18N
        String uiClassName = b.getString( "LookAndFeelCustomsClassName" ); // NOI18N
        if( "default".equals( uiClassName ) ) { // NOI18N
            return findDefaultCustoms();
        }
        try {
            Class klazz = UIUtils.classForName( uiClassName );
            Object inst = klazz.getDeclaredConstructor().newInstance();
            if( inst instanceof LFCustoms )
                return ( LFCustoms ) inst;
        } catch( ClassNotFoundException e ) {
            System.err.println( "LF Customs " + uiClassName + " not on classpath." ); // NOI18N
        } catch( Exception e ) {
            System.err.println( "While loading: " + uiClassName ); // NOI18N
            e.printStackTrace();
        }
        return null;
    }

    private LFCustoms findDefaultCustoms() {
        if (FORCED_CUSTOMS != null) {
            System.err.println("Using explicitly set UI customizations: " + //NOI18N
                FORCED_CUSTOMS);
            if ("Windows8".equals(FORCED_CUSTOMS)) { //NOI18N
                return new Windows8LFCustoms();
            } else if ("Vista".equals(FORCED_CUSTOMS)) { //NOI18N
                return new VistaLFCustoms();
            } else if ("XP".equals(FORCED_CUSTOMS)) { //NOI18N
                return new XPLFCustoms();
            } else if ("Aqua".equals(FORCED_CUSTOMS)) { //NOI18N
                return new AquaLFCustoms();
            } else if ("Metal".equals(FORCED_CUSTOMS)) { //NOI18N
                return new MetalLFCustoms();
            } else if ("Windows".equals(FORCED_CUSTOMS)) { //NOI18N
                return new WindowsLFCustoms();
            } else if ("GTK".equals(FORCED_CUSTOMS)) { //NOI18N
                return new GtkLFCustoms();
            } else {
                try {
                    return (LFCustoms) UIUtils.classForName(FORCED_CUSTOMS).getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    System.err.println("UI customizations class not found: " //NOI18N
                        + FORCED_CUSTOMS); //NOI18N
                }
            }
        }

        LFCustoms result = null;
        try {
            // check whether the current look and feel specifies customs in its UI defaults
            result = (LFCustoms)UIManager.getLookAndFeelDefaults().get("Nb.LFCustoms");
        } catch (ClassCastException cce) {
            //do nothing - the look and feel happens to have something matching
            //our generated key there
        }

        if (result == null) {
            StringBuffer buf = new StringBuffer(40);
            buf.append("Nb."); //NOI18N
            buf.append(UIManager.getLookAndFeel().getID());
            if (UIUtils.isXPLF()) {
                if (isWindows8() || isWindows10() || isWindows11()) {
                    buf.append("Windows8LFCustoms"); //NOI18N
                } else if (isWindowsVista() || isWindows7()) {
                    buf.append("VistaLFCustoms"); //NOI18N
                } else {
                    buf.append("XPLFCustoms"); //NOI18N
                }
            } else {
                buf.append("LFCustoms"); //NOI18N
            }

            try {
                result = (LFCustoms)UIManager.get(buf.toString());
            } catch (ClassCastException cce) {
                //do nothing - the look and feel happens to have something matching
                //our generated key there
            }
        }
        if (result == null) {
            String[] knownLFs = new String[] {
                    "Metal", "Windows", "Aqua", "GTK", "Nimbus" //NOI18N
                };
            switch (Arrays.asList(knownLFs).indexOf(UIManager.getLookAndFeel().getID())) {
                case 1 :
                    if (UIUtils.isXPLF()) {
                        if( isWindows8() || isWindows10() || isWindows11() ) {
                            result = new Windows8LFCustoms();
                        } else if (isWindowsVista() || isWindows7()) {
                            result = new VistaLFCustoms();
                        } else {
                            result = new XPLFCustoms();
                        }
                    } else {
                        result = new WindowsLFCustoms();
                    }
                    break;
                case 0 :
                    result = new MetalLFCustoms();
                    break;
                case 2 :
                    result = new AquaLFCustoms();
                    break;
                case 3 :
                    result = new GtkLFCustoms();
                    break;
                case 4 :
                    result = new NimbusLFCustoms();
                    break;
                default :
                    // #79401 check if it's XP style LnF, for example jGoodies
                    if (UIUtils.isXPLF()) {
                        if (isWindows8() || isWindows10() || isWindows11()) {
                            result = new Windows8LFCustoms();
                        } else if (isWindowsVista() || isWindows7()) {
                            result = new VistaLFCustoms();
                        } else {
                            result = new XPLFCustoms();
                        }
                    } else {
                        if( UIManager.getLookAndFeel() instanceof MetalLookAndFeel ) {
                            result = new MetalLFCustoms();
                        } else {
                            result = new WindowsLFCustoms();
                        }
                    }
            }
        }
        return result;
    }

    /**
     * Initialize values in UIDefaults which need to be there for NetBeans' components; apply customizations such
     * as setting up a custom font size and loading a theme. Basically delegates to
     * {@link #run(java.lang.Class, int, java.net.URL, java.util.ResourceBundle)} with null
     * resource bundle.
     * @param uiClass The UI class which should be used for the look and feel
     * @param uiFontSize A custom fontsize, or 0.  This will be retrievable via UIManager.get("customFontSize") after this method has returned
     *          if non 0.  If non zero, all of the standard Swing font keys in UIDefaults will be customized to
     *          provide a font with the requested size.  Results are undefined for values less than 0 or greater
     *          than any hard limit the platform imposes on font size.
     * @param themeURL An optional URL for a theme file, or null. Theme file format documentation can be found
     *        <a href="https://netbeans.apache.org/projects/ui/themes/themes">here</a>.
     */
    public static void run (Class uiClass, int uiFontSize, URL themeURL) {
        run(uiClass, uiFontSize, themeURL, null);
    }

    /**
     * Initialize values in UIDefaults which need to be there for NetBeans' components; apply customizations such
     * as setting up a custom font size and loading a theme.
     *
     * @param uiClass The UI class which should be used for the look and feel
     * @param uiFontSize A custom fontsize, or 0.  This will be retrievable via UIManager.get("customFontSize") after this method has returned
     *          if non 0.  If non zero, all of the standard Swing font keys in UIDefaults will be customized to
     *          provide a font with the requested size.  Results are undefined for values less than 0 or greater
     *          than any hard limit the platform imposes on font size.
     * @param themeURL An optional URL for a theme file, or null. Theme file format documentation can be found
     *        <a href="https://netbeans.apache.org/projects/ui/themes/themes">here</a>.
     * @param rb resource bundle to use for branding or null. Allows NetBeans to provide enhanced version
     *          of bundle that knows how to deal with branding. The bundle shall have the same keys as
     *          <code>org.netbeans.swing.plaf.Bundle</code> bundle has.
     * @since 1.16
     */
    public static void run (Class uiClass, int uiFontSize, URL themeURL, ResourceBundle rb) {
        if (instance == null) {
          // Modify default font size to the font size passed as a command-line parameter
            if(uiFontSize>0) {
                Integer customFontSize = new Integer (uiFontSize);
                UIManager.put (LFCustoms.CUSTOM_FONT_SIZE, customFontSize);
            }
            Startup.uiClass = uiClass;
            Startup.themeURL = themeURL;
            Startup.bundle = rb;
            instance = new Startup();
            instance.install();
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty ("os.name");
        return osName.startsWith("Windows");
    }

    private static boolean isWindowsVista() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Vista") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.0".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows7() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 7") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.1".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows8() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 8") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.2".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows10() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 10") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "10.0".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows11() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 11") >= 0;
    }

    private static boolean isMac() {
        String osName = System.getProperty ("os.name");
        boolean result = osName.startsWith ("Darwin") || "Mac OS X".equals(osName);
        return result;
    }

    private static boolean isSolaris10 () {
        String osName = System.getProperty ("os.name");
        String osVersion = System.getProperty ("os.version");
        boolean result = osName.startsWith ("SunOS") && "5.10".equals(osVersion);
        return result;
    }

    /** If it is solaris or linux, we can use GTK where supported by getting
     * the platform specific look and feel.
     *
     * Also check to make sure under no
     * circumstances do we use Motif look and feel.
     *
     * #97882: Use Metal on Solaris 10 as well, there is bug which crashes JDK with GTK L&F
     *
     * @return If metal L&F should be used
     */
    private static boolean shouldUseMetal() {
        String osName = System.getProperty ("os.name");
        boolean result = !"Solaris".equals (osName) &&
            !osName.startsWith ("SunOS") &&
            !osName.endsWith ("Linux") ||
            UIManager.getSystemLookAndFeelClassName().indexOf("Motif") > -1 ||
            isSolaris10();
        return result;
    }

    private LFListener listener = null;
    private class LFListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pcl) {
            if ("lookAndFeel".equals(pcl.getPropertyName()) || "win.xpstyle.themeActive".equals(pcl.getPropertyName())) { //NOI18N
                uninstallPerLFDefaults();
                installPerLFDefaults();
            }
        }
    }

    private static final class LFInstanceOrName {
        final LookAndFeel lf;
        final String lfClassName;

        public LFInstanceOrName(LookAndFeel lf) {
            this.lf = lf;
            this.lfClassName = null;
        }

        public LFInstanceOrName(String lfClassName) {
            this.lf = null;
            this.lfClassName = lfClassName;
        }

        public String getClassName() {
            if (lf != null) {
                return lf.getClass().getName();
            } else {
                return lfClassName;
            }
        }
    }

}
