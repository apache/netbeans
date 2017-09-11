    /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.updater;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;

/**
 *
 * @author  phrebejk, akemr, Jiri Rechtacek
 * @version
 */
@SuppressWarnings("CallToThreadDumpStack")
public class UpdaterFrame extends javax.swing.JPanel 
implements UpdatingContext {

    private static final boolean enabledConsole = Boolean.getBoolean("netbeans.logger.console");
    
    static {
        FileHandler fh = null;
        try {
            boolean append = true;
            fh = new FileHandler(UpdateTracking.getUserDir().getPath() + File.separator +
                    "var" + File.separator + "log" + File.separator + "updater.log", 1000000, 3, append);
            fh.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        XMLUtil.LOG.setUseParentHandlers(enabledConsole);
        
        if (fh != null) {
            XMLUtil.LOG.addHandler(fh);
        }
        XMLUtil.LOG.info("Entering updater.jar .................................................................... ");
    }
    

    /** Operating system is Windows x */
    public static final int OS_WIN = 1;
    /** Operating system is Solaris. */
    public static final int OS_SOLARIS = 8;
    /** Operating system is Linux. */
    public static final int OS_LINUX = 16;
    /** Operating system is OS/2. */
    public static final int OS_OS2 = 1024;
    /** Operating system is unknown. */
    public static final int OS_OTHER = 65536;
    
    private static final String SPLASH_PATH = "org/netbeans/updater/resources/updatersplash"; // NOI18N
    private static final String[] ICONS_PATHS = {"org/netbeans/updater/resources/frame",
        "org/netbeans/updater/resources/frame32",
        "org/netbeans/updater/resources/frame48"}; // NOI18N

    private boolean bigBounds = false;

    private JFrame splashFrame;
    
    /** For external running Updater without GUI */
    private boolean noSplash = false; 
    
    /** Creates new form UpdaterFrame */
    public UpdaterFrame() {
        this(null);
    }
    
    private UpdaterFrame(String[] args) {
        
        if (args!=null && args.length>0)
            cli(args);
        
        initComponents ();
        
        if ( addBorder() )
            setBorder(new LineBorder(stringToColor ("UpdaterFrame.LineBorder.Color", new Color(0, 0, 0))));
        
        loadSplash();
    }
    
    static void center(Window c) {
        c.pack();

        GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Rectangle bounds = gconf.getBounds();
        Dimension dialogSize = c.getSize();
        
        c.setLocation(bounds.x + (bounds.width - dialogSize.width) / 2,
                    bounds.y + (bounds.height - dialogSize.height) / 2);
    }

    static String getMainWindowTitle() {
        return Localization.getBrandedString("UpdaterFrame.Form.title");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        textLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setBackground(stringToColor("UpdaterFrame.Background", new Color(6, 4, 100)));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(jLabel3, gridBagConstraints);

        jTextArea1.setBackground(stringToColor("UpdaterFrame.TextBackground", new Color (213, 204, 187)));
        jTextArea1.setEditable(false);
        jTextArea1.setForeground(stringToColor("UpdaterFrame.TextForeground", java.awt.Color.white));
        jTextArea1.setText(Localization.getBrandedString( "UpdaterFrame.jTextArea1.text" ));
        jTextArea1.setDisabledTextColor(stringToColor("UpdaterFrame.DisabledTextColor", java.awt.Color.white));
        jTextArea1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 14, 0, 14);
        add(jTextArea1, gridBagConstraints);

        textLabel.setFont(new java.awt.Font(Localization.getBrandedString("UpdaterFrame.textLabel.fontName"), 1, 11));
        textLabel.setForeground(stringToColor("UpdaterFrame.TextForeground", java.awt.Color.white));
        textLabel.setText(Localization.getBrandedString("UpdaterFrame.textLabel.text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 14, 8, 14);
        add(textLabel, gridBagConstraints);

        progressBar.setMinimumSize(stringToDimension("UpdaterFrame.ProgressBar.PreferredSize", new Dimension (300, 20)));
        progressBar.setPreferredSize(stringToDimension("UpdaterFrame.ProgressBar.PreferredSize", new Dimension (300, 20)));
        progressBar.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 14, 10, 14);
        add(progressBar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void paintComponent (Graphics g) {
        if (isGradient ()) {
            Color outerColor = stringToColor ("UpdaterFrame.outerColor", new Color(230, 242, 234));
            Color centerColor = stringToColor ("UpdaterFrame.centerColor", Color.WHITE);
            int w = getWidth();
            int h = getHeight();

            // paint the background color.
            Graphics2D g2d = (Graphics2D) g;
            if (isGradientVertical ()) {
                Icon splashIcon = jLabel3.getIcon();
                int splashH = splashIcon!=null ? splashIcon.getIconHeight() : 0;
                g2d.setPaint(new GradientPaint(0, splashH, centerColor, 0, h, outerColor));
                g2d.fillRect(0, splashH, w, h - splashH);
            } else {
                g2d.setPaint(new GradientPaint(0, 0, outerColor, w/2, 0, centerColor, true));
                g2d.fillRect(0, 0, w, h);
            }
        } else {
            super.paintComponent (g);
        }
    }
    
    private void showSplash () {             
        splashFrame = new SplashFrame(this);

        // show splash
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                splashFrame.setVisible(true);
                splashFrame.toFront ();
            }
        });
    }
    
    @Override
    public void disposeSplash () {
        if (splashFrame != null) splashFrame.dispose ();
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main (String... args) {
        UpdaterFrame panel = new UpdaterFrame (args);
        if (!panel.noSplash) {
            panel.showSplash ();
        }
        new UpdaterDispatcher (panel).run ();
        XMLUtil.LOG.info("-------------------------- exiting updater.jar");
    }

    @Override
    public Collection<File> forInstall() {
        return null;
    }
    
    @Override
    public void unpackingIsRunning () {
    }
    
    @Override
    public void unpackingFinished() {
        runningFinished();
    }

    @Override
    public void runningFinished() {
        System.exit(0);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel textLabel;
    // End of variables declaration//GEN-END:variables


    @Override
    public void setLabel( final String text ) {
        
        if (noSplash) return;

        final javax.swing.JLabel label = textLabel;

        EventQueue.invokeLater( new Runnable() {
            @Override
            public void run() {
                label.setText( text );
            }
        });
    }


    @Override
    public void setProgressRange( final long min, final long max ) {
        
        if (noSplash) return;

        bigBounds = max > 0xFFFF;

        final javax.swing.JProgressBar pb = progressBar;
        final boolean bb = bigBounds;

        EventQueue.invokeLater( new Runnable() {
            @Override
            public void run() {
                pb.setMinimum( bb ? (int)(min / 1024) : (int)min );
                pb.setMaximum( bb ? (int)(max / 1024) : (int)max );
            }
        });

    }

    @Override
    public void setProgressValue( final long value ) {
        
        if (noSplash) return;

        final boolean bb = bigBounds;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue( bb ? (int)(value / 1024) : (int)value );
            }
        });
    }
    
    /** Get the operating system on which the IDE is running.
    * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
    */
    private static int getOperatingSystem () {
        int operatingSystem;
        String osName = System.getProperty ("os.name");
        if ( osName != null && osName.startsWith("Windows")) // NOI18N
            operatingSystem = OS_WIN;
        else if ("Solaris".equals (osName)) // NOI18N
            operatingSystem = OS_SOLARIS;
        else if (osName.startsWith ("SunOS")) // NOI18N
            operatingSystem = OS_SOLARIS;
        else if ("Linux".equals (osName)) // NOI18N
            operatingSystem = OS_LINUX;
        else if ("OS/2".equals (osName)) // NOI18N
            operatingSystem = OS_OS2;
        else
            operatingSystem = OS_OTHER;
        return operatingSystem;
    }
    
    @Override
    public boolean isFromIDE() {
        return false;
    }

    static private Color stringToColor( String key, Color defcolor ) {
        try {
            String str = Localization.getBrandedString( key + "_R" );  // NOI18N
            int re = Integer.parseInt( str );
            
            str = Localization.getBrandedString( key + "_G" );  // NOI18N
            int gr = Integer.parseInt( str );
            
            str = Localization.getBrandedString( key + "_B" );  // NOI18N
            int bl = Integer.parseInt( str );
            
            return new Color ( re, gr, bl );
        } catch ( Exception e ) {
            return defcolor;
        }
    }
    
    static private Dimension stringToDimension (String key, Dimension defaultSize) {
        try {
            String str = Localization.getBrandedString( key + "_X" );  // NOI18N
            int x = Integer.parseInt( str );
            
            str = Localization.getBrandedString( key + "_Y" );  // NOI18N
            int y = Integer.parseInt( str );
            
            return new Dimension (x, y);
        } catch ( Exception e ) {
            return defaultSize;
        }
    }
    
    static private boolean addBorder() {
        return "true".equals( Localization.getBrandedString( "UpdaterFrame.hasBorder" ) ); // NOI18N
    }
    
    static private boolean isGradient () {
        return "true".equals (Localization.getBrandedString ("UpdaterFrame.isGradient")); // NOI18N
    }

    static private boolean isGradientVertical () {
        return "true".equals (Localization.getBrandedString ("UpdaterFrame.isGradientVertical")); // NOI18N
    }    
    
    private void loadSplash() {
        URL lookup = Localization.getBrandedResource( SPLASH_PATH, ".gif" ); // NOI18N
        if ( lookup != null )
            jLabel3.setIcon( new ImageIcon( lookup ) );
    }
    
    private static List<Image> loadIcons() {
        List<Image> icons = new ArrayList<Image>();
        
        for (String iconPath : ICONS_PATHS) {
            Image icon = loadIcon(iconPath);
            if (icon != null) {
                icons.add(icon);
            }
        }
        
        return icons;
    }
    
    private static Image loadIcon(String iconPath) {
        URL lookup = Localization.getBrandedResource(iconPath, ".gif"); // NOI18N        
        if (lookup != null) {
            try {
                return ImageIO.read(lookup);                
            } catch (IOException ex) {
                XMLUtil.LOG.log(Level.WARNING, "Cannot load icon (" + iconPath + ".gif)", ex);
            }
        }
        return null;
    }

    @Override
    public OutputStream createOS(File bckFile) throws FileNotFoundException {
        return new FileOutputStream(bckFile);
    }
        
    static class SplashFrame extends JFrame {
        
        /** Creates a new SplashFrame */
        @SuppressWarnings("LeakingThisInConstructor")
        public SplashFrame (UpdaterFrame panel) {
            super (getMainWindowTitle());
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setIconImages(loadIcons());            
            setUndecorated(true);
            setResizable(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            // add splash component
            getContentPane().add(panel);
            center(this);
        }
        
        @Override
        public java.awt.Dimension getPreferredSize () {
            return stringToDimension("UpdaterFrame.Splash.PreferredSize", new Dimension (400, 280));
        }
    }
    
    // copied from core/CLIOptions
    
    private static boolean isOption (String value, String optionName) {
        if (value == null) return false;
        
        if (value.startsWith ("--")) {
            return value.substring (2).equals (optionName);
        } else if (value.startsWith ("-")) {
            return value.substring (1).equals (optionName);
        }
        return false;
    }
    
    private int cli(String[] args) {
        // let's go through the command line
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            if (isOption (args[i], "noexit")) { // NOI18N
                throw new IllegalStateException();
            } else if (isOption (args[i], "nosplash")) { // NOI18N
                noSplash = true;
            } else if (isOption (args[i], "locale")) { // NOI18N
                args[i] = null;
                String localeParam = args[++i];
                String language;
                String country = ""; // NOI18N
                String variant = ""; // NOI18N
                int index1 = localeParam.indexOf(":"); // NOI18N
                if (index1 == -1)
                    language = localeParam;
                else {
                    language = localeParam.substring(0, index1);
                    int index2 = localeParam.indexOf(":", index1+1); // NOI18N
                    if (index2 != -1) {
                        country = localeParam.substring(index1+1, index2);
                        variant = localeParam.substring(index2+1);
                    }
                    else
                        country = localeParam.substring(index1+1);
                }
                Locale.setDefault(new Locale(language, country, variant));
            } else if (isOption (args[i], "branding")) { // NOI18N
                args[i] = null;
                if (++i == args.length) {
                    System.err.println("Option --branding requires one argument.");
                    return 2;
                }
                String branding = args[i];
                if (branding.equals("-")) branding = null; // NOI18N
                try {
                    Localization.setBranding(branding);
                } catch (IllegalArgumentException iae) {
                    XMLUtil.LOG.log(Level.WARNING, "Cannot change branding", iae);
                    return 1;
                }
            }
        }
        
        return 0;
    }
    
}
