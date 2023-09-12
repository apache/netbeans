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

package org.netbeans.installer.utils;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.UiMode;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import org.netbeans.installer.utils.helper.ErrorLevel;


/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public final class UiUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static boolean lookAndFeelInitialized = false;
    private static LookAndFeelType lookAndFeelType = null;
    private static boolean uiUnavailable;
    
    public static boolean showMessageDialog(
            final String message,
            final String title,
            final MessageType messageType) {
        initLAF();
        
        boolean exitInstaller = false;
        
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                int intMessageType = JOptionPane.INFORMATION_MESSAGE;                               
                
                LogManager.logIndent("... show message dialog");
                LogManager.log("title: "+ title);
                LogManager.log("message: " + message);
                
                if (messageType == MessageType.WARNING) {
                    intMessageType = JOptionPane.WARNING_MESSAGE;                
                } else if (messageType == MessageType.CRITICAL) {
                    intMessageType = JOptionPane.ERROR_MESSAGE;
                    exitInstaller = true;
                }
                
                if (messageType == MessageType.ERROR) {                    
                    int result = JOptionPane.showOptionDialog(null,
                                        message,
                                        title,
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.ERROR_MESSAGE,
                                        null,
                                        null,
                                        JOptionPane.YES_OPTION);
                    if (result == JOptionPane.NO_OPTION) {
                        exitInstaller = true;
                        LogManager.logUnindent("... user selected: NO");                        
                    } else {
                        LogManager.logUnindent("... user selected: YES");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                            message, 
                            title, 
                            intMessageType);
                }
                
                LogManager.logUnindent("... dialog closed");
                break;
            case SILENT:
                LogManager.log(message);
                System.err.println(message);
                break;
        }
        
        return exitInstaller;
    }
    /**
     * 
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @return true if user click YES option. 
     * If installer is running silently then false is returned.
     */
    public static boolean showYesNoDialog(
            final String title,
            final String message) {
        return showYesNoDialog(title, message, false);
    }
    /**
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param silentDefault The default return value if installer is running silently
     * @return true if user click YES option. In silent mode return <code>silentDefault</code>
     */
    public static boolean showYesNoDialog(
            final String title,
            final String message,
            final boolean silentDefault) {
        initLAF();
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                LogManager.logIndent("... showing Yes/No dialog");
                LogManager.log("title: " + title);
                LogManager.log("message: " + message);
                final int result = JOptionPane.showConfirmDialog(
                        null,
                        message,
                        title,
                        YES_NO_OPTION);
                LogManager.logUnindent("... dialog closed, choice : " + (result == YES_OPTION ? "yes" : (result == NO_OPTION ? "no" : "closed")));
                return result == YES_OPTION;
                
            case SILENT:
                LogManager.log(message);                
                final String option = StringUtils.format(
                        ResourceUtils.getString(UiUtils.class,
                        silentDefault ? 
                            RESOURCE_SILENT_DEFAULT_YES : 
                            RESOURCE_SILENT_DEFAULT_NO));
                System.err.println(message);
                System.err.println(option);
                LogManager.log(message);
                LogManager.log(option);
                return silentDefault;
        }
        //never get this line...
        return true;
    }
    /**
     * @param title The title of the dialog
     * @param message The message of the dialog
     * @param silentDefault The dafault return value if installer is running silently
     * @return true if user click YES option. In silent mode return <code>silentDefault</code>
     */
    public static int showYesNoCancelDialog(
            final String title,
            final String message,
            final int silentDefault) {
        initLAF();
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                LogManager.logIndent("... show Yes/No/Cancel dialog");
                LogManager.log("title: " + title);
                LogManager.log("message: " + message);
                int result = JOptionPane.showConfirmDialog(
                        null,
                        message,
                        title,
                        YES_NO_CANCEL_OPTION);
                LogManager.logUnindent("... dialog closed, choice : " + (result == YES_OPTION ? "yes" : (result == NO_OPTION ? "no" : (result==CANCEL_OPTION ? "cancel" : "closed"))));
                return result;
                
            case SILENT:
                LogManager.log(message);               
                String resource;
                switch(silentDefault) {
                    case YES_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_YES; 
                        break;
                    case NO_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_NO; 
                        break;
                    case CANCEL_OPTION : 
                        resource = RESOURCE_SILENT_DEFAULT_CANCEL; 
                        break;
                    default:
                        resource = StringUtils.EMPTY_STRING;
                        break;
                }
                        
                final String option = StringUtils.format(
                        ResourceUtils.getString(UiUtils.class,resource));
                System.err.println(message);
                System.err.println(option);
                LogManager.log(message);
                LogManager.log(option);
                return silentDefault;
        }
        //never get this line...
        return silentDefault;
    }
    
    public static void initializeLookAndFeel() throws InitializationException {
        if (lookAndFeelInitialized) {
            return;
        }
        
        try {
            LogManager.log("... initializing look and feel");
            LogManager.indent();
            switch (UiMode.getCurrentUiMode()) {
                case SWING:
                    uiUnavailable = true;
                    String className = System.getProperty(LAF_CLASS_NAME_PROPERTY);
                    if (className == null) {
                        LogManager.log("... custom look and feel class name was not specified, using system default");
                        className = UiUtils.getDefaultLookAndFeelClassName();
                    } else if(!className.contains(StringUtils.DOT)) {//short name of the L&F class
                        className = UiUtils.getLookAndFeelClassNameByShortName(className);
                    }
                    
                    LogManager.log("... class name: " + className);
                    
                    if (Boolean.getBoolean(LAF_DECORATED_WINDOWS_PROPERTY)) {
                        JFrame.setDefaultLookAndFeelDecorated(true);
                    }
                    
                    try {
                        Thread jdkFileChooserWarningLogThread = null;
                        try {
                            // this helps to avoid some GTK L&F bugs for some locales
                            LogManager.log("... get installed L&Fs");
                            UIManager.getInstalledLookAndFeels();
                            LogManager.log("... set specified L&F");
                            UIManager.setLookAndFeel(className);
                            LogManager.log("... check headless");                            
                            if (GraphicsEnvironment.isHeadless()) {
                                HeadlessException e = new HeadlessException(ResourceUtils.getString(UiUtils.class, RESOURCE_FAILED_TO_INIT_UI_HEADLESS));
                                System.err.println(ResourceUtils.getString(UiUtils.class, RESOURCE_FAILED_TO_INIT_UI));
                                System.err.println(e.getMessage());
                                throw new InitializationException(
                                        ResourceUtils.getString(UiUtils.class, 
                                        RESOURCE_FAILED_TO_INIT_UI), e);
                            }

                            if (System.getProperty("os.name").startsWith("Windows")) {
                                // workaround for the issue with further using JFileChooser
                                // in case of missing system icons
                                // Java Issue :
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6210674
                                // NBI Issue :
                                // http://www.netbeans.org/issues/show_bug.cgi?id=105065
                                // it also a workaround for two more bugs
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6449933
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6489447
                                LogManager.log("... creating JFileChooser object to check possible issues with UI");
                                
                                if (System.getProperty("java.version").startsWith("1.6")) {
                                    File desktop = new File(SystemUtils.getUserHomeDirectory(), "Desktop");
                                    File[] zips = null;
                                    final List<String> names = new ArrayList<String>();
                                    if (FileUtils.exists(desktop)) {
                                        zips = desktop.listFiles(new FileFilter() {
                                            public boolean accept(File pathname) {
                                                boolean result = pathname.getName().endsWith(".zip") && pathname.length() > 1000000L;
                                                if (result) {
                                                    names.add(pathname.getName());
                                                }
                                                return result;
                                            }
                                        });
                                    }
                                    if (zips != null && zips.length > 0) {
                                        jdkFileChooserWarningLogThread = new NbiThread() {
                                            @Override
                                            public void run() {
                                                try {
                                                    sleep(8000); //8 seconds
                                                } catch (InterruptedException e) {
                                                    return;
                                                }
                                                final File lock = new File(Installer.getInstance().getLocalDirectory(), Installer.LOCK_FILE_NAME);
                                                LogManager.log("\n... !!! WARNING !!!");
                                                LogManager.log("... There are some big zip files on your desktop: " + StringUtils.asString(names));
                                                LogManager.log("... In case installer UI does not appear for a long time:");
                                                LogManager.log("...    1) kill the installer process");
                                                LogManager.log("...    2) move those zip files somewhere from the desktop");
                                                LogManager.log("...    3) delete " + lock);
                                                LogManager.log("...    4) run installer again");                                                
                                                LogManager.log("... For more details see the following bugs descriptions: ");
                                                LogManager.log("... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6372808");
                                                LogManager.log("... http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5050516");
                                            }
                                        };
                                        jdkFileChooserWarningLogThread.start();
                                    }
                                }
                                
                                                                
                                
                                if (SwingUtilities.isEventDispatchThread()) {
                                    new JFileChooser();
                                } else {
                                    try {
                                        SwingUtilities.invokeAndWait(new Runnable() {
                                            public void run() {
                                                new JFileChooser();
                                            }
                                        });
                                    } catch (InvocationTargetException e) {
                                        throw (e.getCause() != null) ? e.getCause() : e;
                                    }
                                }
                                
                                
                                if(jdkFileChooserWarningLogThread!=null) {
                                    jdkFileChooserWarningLogThread.interrupt();
                                    jdkFileChooserWarningLogThread = null;
                                }
                                
                                LogManager.log("... getting default Toolkit to check possible issues with UI");
                                Toolkit.getDefaultToolkit();
                                
                                // workaround for JDK issue with JProgressBar using StyleXP
                                // http://www.netbeans.org/issues/show_bug.cgi?id=106876
                                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337517
                                LogManager.log("... creating JProgressBar object to check possible issues with UI");
                                new JProgressBar().getMaximumSize();
                                
                                LogManager.log("... all UI checks done");
                            }
                            LogManager.log("... L&F is set");
                        } catch (Throwable e) {
                            if(jdkFileChooserWarningLogThread!=null) {
                                jdkFileChooserWarningLogThread.interrupt();
                                jdkFileChooserWarningLogThread = null;
                            }
                            // we're catching Throwable here as pretty much anything can happen
                            // while setting the look and feel and we have no control over it
                            // if something wrong happens we should fall back to the default
                            // cross-platform look and feel which is assumed to be working
                            // correctly
                            LogManager.log("... could not activate defined L&F, initializing cross-platfrom one", e);
                            if (e instanceof InternalError) {
                                System.err.println(e.getMessage());
                            } else if (e instanceof ExceptionInInitializerError) {
                                final Throwable cause = e.getCause();
                                
                                if (cause instanceof HeadlessException) {
                                    System.err.println(cause.getMessage());
                                }
                            }                            
                            
                            className = UIManager.getCrossPlatformLookAndFeelClassName();
                            LogManager.log("... cross-platform L&F class-name : " + className);
                            
                            UIManager.setLookAndFeel(className);
                            
                            if(System.getProperty(LAF_CLASS_NAME_PROPERTY)!=null) {
                                // Throw exception only if user specified custom L&F, 
                                // otherwise just go to initialization of cross-platfrom L&F 
                                //     (Exception e is already logged above)
                                // See also http://www.netbeans.org/issues/show_bug.cgi?id=122557                                
                                // This exception would be thrown only if cross-platform LAF is successfully installed
                                throw new InitializationException(
                                    ResourceUtils.getString(UiUtils.class, 
                                    RESOURCE_FAILED_TO_ACTIVATE_DEFINED_LAF), e);
                            }                            
                        }
                    } catch (NoClassDefFoundError e) {
                        throw new InitializationException(
                                ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (ClassNotFoundException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (InstantiationException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (IllegalAccessException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    } catch (UnsupportedLookAndFeelException e) {
                        throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF), e);
                    }
                    break;
            }
        } catch (LinkageError err) {
            uiUnavailable = true;
            LogManager.log(ErrorLevel.ERROR, err);
            throw new InitializationException(ResourceUtils.getString(UiUtils.class,
                    RESOURCE_FAILED_TO_INITIALIZE_UI_LINKERR, err.getLocalizedMessage()), err);
        } finally {
            LogManager.unindent();
            LogManager.log("... initializing L&F finished");
            
            if (Boolean.getBoolean("nbi.look.and.feel.dump.defaults")) {
                try {
                    LogManager.logIndent("... dumping UIManger L&F defaults: ");
                    Hashtable hash = (Hashtable) UIManager.getLookAndFeelDefaults();
                    Enumeration keys = hash.keys();
                    while (keys.hasMoreElements()) {
                        Object key = keys.nextElement();
                        LogManager.log("" + key + " = " + hash.get(key));
                    }
                } catch (Exception e) {
                    LogManager.log(e);
                } finally {
                    LogManager.unindent();
                }
            }
            
            lookAndFeelInitialized = true;
            lookAndFeelType = getLAF();
        }
    }
    
    public static String getDefaultLookAndFeelClassName(
            ) {
        switch (UiMode.getCurrentUiMode()) {
            case SWING:
                String className = UIManager.getSystemLookAndFeelClassName();
                
                // if the default look and feel is the cross-platform one, we might
                // need to correct this choice. E.g. - KDE, where GTK look and feel
                // would be much more appropriate
                if (className.equals(UIManager.getCrossPlatformLookAndFeelClassName())) {
                    
                    // if the current platform is Linux and the desktop manager is
                    // KDE, then we should try to use the GTK look and feel
                    try {
                        if (System.getProperty("os.name").contains("Linux") &&
                                (System.getenv("KDE_FULL_SESSION") != null)) {
                            // check whether the GTK look and feel class is
                            // available -- we'll get CNFE if it is not and it will
                            // not be set
                            Class.forName(LookAndFeelType.GTK.getClassName());
                            
                            className = LookAndFeelType.GTK.getClassName();
                        }
                    } catch (ClassNotFoundException e) {
                        ErrorManager.notifyDebug(ResourceUtils.getString(UiUtils.class,
                                RESOURCE_FAILED_TO_FORCE_GTK), e);
                    }
                }
                
                return className;
            default:
                return null;
        }
    }
    
    public static final String getLookAndFeelClassNameByShortName(String name) {
        if(name == null) {
            return null;
        }
        for(LookAndFeelType lafType : LookAndFeelType.values()) {
            if(lafType.getId()!=null && lafType.getClassName()!=null) {
                if(name.toLowerCase(Locale.ENGLISH).equals(lafType.getId().toLowerCase(Locale.ENGLISH))) {
                    return lafType.getClassName();
                }
            }
        }
        return name;
    }
    
    public static final LookAndFeelType getLAF() {
        if(lookAndFeelType==null) {
            try {
                initializeLookAndFeel();
            } catch (InitializationException e) {
                LogManager.log(e);
            }
            lookAndFeelType = LookAndFeelType.DEFAULT;

            if (!uiUnavailable && UiMode.getCurrentUiMode() == UiMode.SWING) {
                LookAndFeel laf = UIManager.getLookAndFeel();
                if (laf != null) {
                    String id = laf.getID();
                    for (LookAndFeelType type : LookAndFeelType.values()) {
                        if (id.equals(LookAndFeelType.WINDOWS_XP.getId()) ||
                                id.equals(LookAndFeelType.WINDOWS_CLASSIC.getId())) {
                            final Object object = Toolkit.getDefaultToolkit().
                                    getDesktopProperty(WINDOWS_XP_THEME_MARKER_PROPERTY);
                            boolean xpThemeActive = false;
                            if (object != null) {
                                xpThemeActive = (Boolean) object;
                            }
                            lookAndFeelType = (xpThemeActive) ? LookAndFeelType.WINDOWS_XP : LookAndFeelType.WINDOWS_CLASSIC;
                            break;
                        } else if (id.equals(type.getId())) {
                            lookAndFeelType = type;
                            break;
                        }
                    }
                }
            }
        }
        return lookAndFeelType;
    }

    private static void initLAF() {
        try {
            initializeLookAndFeel();
        } catch (InitializationException  e) {
            ErrorManager.notifyWarning(e.getMessage(), e.getCause());
        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private UiUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    
    public static enum MessageType {
        INFORMATION,
        WARNING,
        ERROR,
        CRITICAL
    }
    
    public enum LookAndFeelType {
        WINDOWS_XP("win.xp", "Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
        WINDOWS_CLASSIC("win.classic", "Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
        MOTIF("motif", "Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
        GTK("gtk", "GTK", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"),
        METAL("metal", "Metal", "javax.swing.plaf.metal.MetalLookAndFeel"),
        AQUA("aqua", "Aqua", "com.apple.laf.AquaLookAndFeel"),
        NIMBUS("nimbus", "Nimbus", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"),
        DEFAULT("default", null, null);
        
        private String name;
        private String className;
        private String id;
        
        @Override
        public String toString() {
            return name;
        }
        private LookAndFeelType(String name, String id, String className) {
            this.name = name;
            this.id = id;
            this.className = className;
        }
        public String getId() {
            return id;
        }
        public String getClassName() {
            return className;
        }
    };
    
    public static int getDimension(Properties props, final String defaultPropertyName, final int defaultValue) {
        int dimension = defaultValue;
        String propertyName = defaultPropertyName;
        if (props.getProperty(propertyName + "." + UiUtils.getLAF()) != null) {
            propertyName = propertyName + "." + UiUtils.getLAF();
        }
        
        if (props.getProperty(propertyName) != null) {
            try {
                dimension = Integer.parseInt(
                        props.getProperty(propertyName).trim());
            } catch (NumberFormatException e) {
                final String warning = ResourceUtils.getString(
                        UiUtils.class,
                        RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY,
                        propertyName,
                        props.getProperty(propertyName));
                
                ErrorManager.notifyWarning(warning, e);
            }
        } 
        return dimension;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    /**
     * Name of the system property, which contains the look and feel class name that
     * should be used by the wizard.
     */
    public static final String LAF_CLASS_NAME_PROPERTY =
            "nbi.look.and.feel"; // NOI18N
    
    /**
     * Name of the system property, which tells the UiUtils whether the wizard
     * windows should be decorated by the current look and feel or the system
     * window manager.
     */
    public static final String LAF_DECORATED_WINDOWS_PROPERTY =
            "nbi.look.and.feel.decorate.windows"; // NOI18N
    
    public static final String WINDOWS_XP_THEME_MARKER_PROPERTY =
            "win.xpstyle.themeActive"; // NOI18N
     /**
     * Name of a resource bundle entry.
     */
    private static final String RESOURCE_FAILED_TO_PARSE_SYSTEM_PROPERTY =
            "UI.error.failed.to.parse.property"; // NOI18N
    private static final String RESOURCE_FAILED_TO_ACTIVATE_CROSSPLATFORM_LAF =
            "UI.error.failed.to.activate.crossplatform.laf"; // NOI18N
    private static final String RESOURCE_FAILED_TO_ACTIVATE_DEFINED_LAF =
            "UI.error.failed.to.activate.defined.laf";//NOI18N
    private static final String RESOURCE_FAILED_TO_INIT_UI = 
            "UI.error.failed.to.init.ui";//NOI18N
    private static final String RESOURCE_FAILED_TO_INIT_UI_HEADLESS = 
            "UI.error.failed.to.init.ui.headless";//NOI18N
    private static final String RESOURCE_FAILED_TO_FORCE_GTK =
            "UI.error.failed.to.force.gtk";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_YES = 
            "UI.silent.default.yes";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_NO = 
            "UI.silent.default.no";//NOI18N
    private static final String RESOURCE_SILENT_DEFAULT_CANCEL = 
            "UI.silent.default.cancel";//NOI18N
    private static final String RESOURCE_FAILED_TO_INITIALIZE_UI_LINKERR =
            "UI.error.failed.to.initialize.ui.linkerr"; // NOI18N
}
