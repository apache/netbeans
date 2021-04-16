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

package org.netbeans.core.startup;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.imageio.ImageIO;
import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.LEFT;
import javax.swing.*;
import org.netbeans.Stamps;
import org.netbeans.Util;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** A class that encapsulates all the splash screen things.
*/
public final class Splash implements Stamps.Updater {

    private static Splash splash;
    
    /** is there progress bar in splash or not */
    private static final boolean noBar = Boolean.getBoolean("netbeans.splash.nobar") ||
            !Boolean.parseBoolean(NbBundle.getMessage(Splash.class, "SplashShowProgressBar"));

    public static Splash getInstance() {
        if (splash == null) {
            splash = new Splash();
        }
        return splash;
    }
    
    public static void showAboutDialog (java.awt.Frame parent, javax.swing.JComponent info) {
        createAboutDialog (parent, info).setVisible(true);
    }
    
    private static JDialog createAboutDialog (java.awt.Frame parent, javax.swing.JComponent info) {
        SplashDialog splashDialog = new SplashDialog (parent, info);
        return splashDialog;
    }

    // Copied from MainWindow:
    private static final String ICON_16 = "org/netbeans/core/startup/frame.gif"; // NOI18N
    private static final String ICON_32 = "org/netbeans/core/startup/frame32.gif"; // NOI18N
    private static final String ICON_48 = "org/netbeans/core/startup/frame48.gif"; // NOI18N
    private static final String ICON_256 = "org/netbeans/core/startup/frame256.png"; // NOI18N
    private static final String ICON_512 = "org/netbeans/core/startup/frame512.png"; // NOI18N
    private static final String ICON_1024 = "org/netbeans/core/startup/frame1024.png"; // NOI18N
    private void initFrameIcons (Frame f) {
        f.setIconImages(Arrays.asList(
                ImageUtilities.loadImage(ICON_16, true),
                ImageUtilities.loadImage(ICON_32, true),
                ImageUtilities.loadImage(ICON_48, true),
                ImageUtilities.loadImage(ICON_256, true),
                ImageUtilities.loadImage(ICON_512, true),
                ImageUtilities.loadImage(ICON_1024, true)));
    }
    
    private Frame frame;
    private SplashPainter painter;
    private SplashComponent comp;
    private SplashScreen splashScreen;
    /**
     * Indicate if we should try to take advantage of java's "-splash" parameter, which allows
     * the splash screen to be displayed at an earlier stage in the app startup sequence. See the
     * original Buzilla RFE at https://netbeans.org/bugzilla/show_bug.cgi?id=60142 . This requires
     * splash screen image(s) to be written to the cache directory the first time NetBeans starts,
     * to be available during subsequent NetBeans startups. Despite
     * https://bugs.openjdk.java.net/browse/JDK-8145173 and
     * https://bugs.openjdk.java.net/browse/JDK-8151787 , as of OpenJDK 10.0.2 and OpenJDK 12.0.1
     * I have found no way to make this work properly with HiDPI screens on Windows. HiDPI filenames
     * attempted include "splash@2x.png", "splash@200pct.png", "splash.scale-200.png", and
     * "splash.java-scale200.png". In all of these cases, the regular "splash.png" file is used
     * instead of one of the 2x-scaled ones (for a system DPI scaling of 200%), and the splash
     * screen becomes half the expected size. Thus, to we disable this feature for now, in favor of
     * a slightly delayed splash screen that appears with the correct size and resolution on HiDPI
     * screens.
     *
     * <p>See also https://issues.apache.org/jira/browse/NETBEANS-67 .
     */
    private static final boolean USE_LAUNCHER_SPLASH = false;
    
    private Splash() {
        Stamps s = Stamps.getModulesJARs();
        if (!CLIOptions.isNoSplash() && !GraphicsEnvironment.isHeadless()) {
            if (USE_LAUNCHER_SPLASH && !s.exists("splash.png")) {
                s.scheduleSave(this, "splash.png", false);
            }
            try {
                splashScreen = SplashScreen.getSplashScreen();
                if (splashScreen != null) {
                    Graphics2D graphics = splashScreen.createGraphics();
                    painter = new SplashPainter(graphics, null, false);
                }
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
            if (painter == null) {
                comp = new SplashComponent(false);
                painter = comp.painter;
            }
        }
    }
    
    final int getMaxSteps() {
        return painter.maxSteps;
    }
    
    final int getProgress() {
        return painter.progress;
    }
    
    /** Enables or disables splash component and its progress
     * animation
     */
    public void setRunning(boolean running) {
        if (CLIOptions.isNoSplash()) {
            return;
        }
        if (comp == null) {
            // ignore all manipulations
            return;
        }

        if (running) {
            if (frame == null) {
                frame = new Frame(NbBundle.getMessage(Splash.class, "LBL_splash_window_title")); // e.g. for window tray display
                //#215320
                frame.setType(Window.Type.POPUP);
                initFrameIcons(frame); // again, only for possible window tray display
                frame.setUndecorated(true);
                // add splash component
                frame.setLayout(new BorderLayout());
                frame.add(comp, BorderLayout.CENTER);
                frame.setResizable(false);

                int width = Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_WIDTH"));
                int height = Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_HEIGHT"));
                frame.setPreferredSize(new Dimension(width, height));

                SwingUtilities.invokeLater(new SplashRunner(frame, true));
            }
        } else {
            SwingUtilities.invokeLater(new SplashRunner(frame, false));
        }
    }
    
    public void dispose() {
        setRunning(false);
        splash = null;
    }

    public void increment(int steps) {
        if (noBar || CLIOptions.isNoSplash()) {
            return;
        }

//System.out.println("Splash.increment ("+steps+"), "+comp);
        if (painter != null) {
            painter.increment(steps);
        }
    }
    
    public Component getComponent() {
        return comp;
    }
    
    /** Updates text in splash window
     */
    public void print(String s) {
        if (CLIOptions.isNoSplash() || painter == null) {
            return;
        }

        painter.setText(s);
    }

    /** Adds specified numbers of steps to a progress
     */
    public void addToMaxSteps(int steps) {
        if (noBar || CLIOptions.isNoSplash()) {
            return;
        }

        if (painter != null) {
            painter.addToMaxSteps(steps);
        }
    }
    
//****************************************************************************    
    /**
     * Standard way how to place the window to the center of the screen.
     */
    static void center(Window c) {
        c.pack();
        c.setBounds(Utilities.findCenterBounds(c.getSize()));
    }

    /**
     * Loads a splash image from its source. For high-resolution rendering on HiDPI displays, the
     * returned image should be converted to a HiDPI-aware Icon instance via
     * {@link ImageUtilities#image2Icon} prior to painting.
     *
     *  @param about if true then about image is loaded, if false splash image is loaded
     */
    public static Image loadContent(boolean about) {
        return ImageUtilities.icon2Image(loadContentIcon(about));
    }

    private static Icon loadContentIcon(boolean about) {
        Image ret = null;
        if (about) {
            ret = ImageUtilities.loadImage("org/netbeans/core/startup/about.png", true);
        }
        if (ret == null) {
            ret = ImageUtilities.loadImage("org/netbeans/core/startup/splash.gif", true);
        }
        if (ret == null) {
            return null;
        }
        return new ScaledBitmapIcon(ret,
                Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_WIDTH")),
                Integer.parseInt(NbBundle.getMessage(Splash.class, "SPLASH_HEIGHT")));
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        ImageIO.write((BufferedImage)loadContent(false), "png", os);
    }

    @Override
    public void cacheReady() {
    }

    /**
     * This class implements double-buffered splash screen component.
     */
    private static class SplashComponent extends JComponent implements Accessible {
        final SplashPainter painter;

        public SplashComponent(boolean about) {
            painter = new SplashPainter(
                (Graphics2D)getGraphics(), this, about
            );
        }

        /**
         * Override update to *not* erase the background before painting.
         */
        @Override
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Renders this component to the given graphics.
         */
        @Override
        public void paint(Graphics graphics) {
            painter.graphics = (Graphics2D) graphics;
            painter.paint();
        }
        @Override
        public boolean isOpaque() {
            return true;
        }

        @Override
        public String toString() {
            return "SplashComponent - "
                    + "progress: " + painter.progress + "/" + painter.maxSteps
                    + " text: " + painter.text;
        }
    }

    private static final class TextBox {
        final Rectangle bounds;
        final Color color;
        final int textSize;
        final Font font;
        final FontMetrics fm;
        final int horizontalAlignment;
        // Will be set by SwingUtilities.layoutCompoundLabel.
        final Rectangle effectiveBounds = new Rectangle();

        private TextBox(
                Rectangle bounds, Color color, int textSize, Font font, FontMetrics fontMetrics,
                int horizontalAlignment)
        {
            this.bounds = bounds;
            this.color = color;
            this.textSize = textSize;
            this.font = font;
            this.fm = fontMetrics;
            this.horizontalAlignment = horizontalAlignment;
        }

        /**
         * Compute the text layout and, if graphics is not null, paint the text string.
         */
        public void layout(String text, Graphics graphics) {
            if (fm == null) {
                // XXX(-ttran) this happened on Japanese Windows NT, don't
                // fully understand why
                return;
            }
            SwingUtilities.layoutCompoundLabel(fm, text, null,
                    BOTTOM, horizontalAlignment, BOTTOM, horizontalAlignment,
                    bounds, new Rectangle(), effectiveBounds, 0);
            if (graphics != null) {
                graphics.setColor(color);
                graphics.setFont(font);
                graphics.drawString(text, effectiveBounds.x, effectiveBounds.y + fm.getAscent());
            }
        }

        public static TextBox parse(Graphics graphics, JComponent comp, ResourceBundle bundle,
            String prefix, boolean optional)
        {
            if (optional && !bundle.containsKey(prefix + "Bounds"))
                return null;
            StringTokenizer st = new StringTokenizer(
                    bundle.getString(prefix + "Bounds"), " ,"); // NOI18N
            Rectangle bounds = new Rectangle(Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()),
                    Integer.parseInt(st.nextToken()));
            Color color = Color.BLACK;
            try {
                Integer rgb = Integer.decode(bundle.getString(prefix + "Color")); // NOI18N
                color = new Color(rgb.intValue());
            } catch (NumberFormatException nfe) {
                //IZ 37515 - NbBundle.DEBUG causes startup to fail; use default value
                Util.err.warning("Number format exception " + //NOI18N
                        "loading splash screen parameters."); //NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, nfe);
            }
            int size = 12;
            try {
                String sizeStr = bundle.getString(prefix + "FontSize");
                size = Integer.parseInt(sizeStr);
            } catch (MissingResourceException e) {
                //ignore - use default size
            } catch (NumberFormatException nfe) {
                //ignore - use default size
            }
            int horizontalAlignment = LEFT;
            try {
                switch (bundle.getString(prefix + "HorizontalAlignment").toLowerCase(Locale.US)) {
                  case "left":
                      horizontalAlignment = SwingConstants.LEFT;
                      break;
                  case "center":
                      horizontalAlignment = SwingConstants.CENTER;
                      break;
                  case "right":
                      horizontalAlignment = SwingConstants.RIGHT;
                      break;
                  default:
                      // Ignore; use default
                      Util.err.warning(
                          "Invalid horizontal alignment for splash screen text box"); //NOI18N
                }
            } catch (MissingResourceException e) {
              // Ignore; use default
            }
            Font font = new Font(bundle.getString(prefix + "FontType"), Font.PLAIN, size); // NOI18N
            FontMetrics fontMetrics;
            if (comp != null) {
                fontMetrics = comp.getFontMetrics(font);
            } else {
                fontMetrics = graphics.getFontMetrics(font);
            }
            return new TextBox(bounds, color, size, font, fontMetrics, horizontalAlignment);
        }
    }

    private static class SplashPainter {
        TextBox statusBox;
        // May be null.
        TextBox versionBox;
        Color color_bar;
        Color color_edge;
        Color color_corner;
        private Rectangle dirty = new Rectangle();
        private Rectangle bar = new Rectangle();
        private Rectangle bar_inc = new Rectangle();
        private int progress = 0;
        private int maxSteps = 0;
        private int barStart = 0;
        private int barLength = 0;
        private Icon image;
        private String text;
        private Graphics2D graphics;
        private final JComponent comp;
        private final boolean about;

        /**
         * Creates a new splash screen component.
         * param about true is this component will be used in about dialog
         */
        public SplashPainter(Graphics graphics, JComponent comp, boolean about) {
            this.graphics = (Graphics2D) graphics;
            this.comp = comp;
            this.about = about;
        }

        final void init() throws MissingResourceException, NumberFormatException {
            assert SwingUtilities.isEventDispatchThread();
            // check if init has already been called
            if (statusBox != null) {
                return;
            }
            // 100 is allocated for module system that will adjust this when number
            // of existing modules is known
            maxSteps = 140;

            ResourceBundle bundle = NbBundle.getBundle(Splash.class);
            statusBox = TextBox.parse(graphics, comp, bundle, "SplashRunningText", false);
            versionBox = TextBox.parse(graphics, comp, bundle, "SplashVersionText", true);
            StringTokenizer st = new StringTokenizer(
                    bundle.getString("SplashProgressBarBounds"), " ,"); // NOI18N
            try {
                bar = new Rectangle(Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken()),
                        Integer.parseInt(st.nextToken()));
                Integer rgb = Integer.decode(bundle.getString("SplashProgressBarColor")); // NOI18N
                color_bar = new Color(rgb.intValue());
                rgb = Integer.decode(bundle.getString("SplashProgressBarEdgeColor")); // NOI18N
                color_edge = new Color(rgb.intValue());
                rgb = Integer.decode(bundle.getString("SplashProgressBarCornerColor")); // NOI18N
                color_corner = new Color(rgb.intValue());
            } catch (NumberFormatException nfe) {
                //IZ 37515 - NbBundle.DEBUG causes startup to fail - provide some useless values
                Util.err.warning("Number format exception " + //NOI18N
                        "loading splash screen parameters."); //NOI18N
                Logger.getLogger("global").log(Level.WARNING, null, nfe);
                color_bar = Color.ORANGE;
                color_edge = Color.BLUE;
                color_corner = Color.GREEN;
                bar = new Rectangle(0, 0, 80, 10);
            }

            image = loadContentIcon(about);

            if (comp != null)
              comp.setFont(statusBox.font);
        }

        long next;
        @SuppressWarnings("CallToThreadDumpStack")
        final void repaint(Rectangle r) {
            if (comp != null) {
                comp.repaint(r);
            } else {
                if (next < System.currentTimeMillis()) {
                    paint();
                    try {
                        Splash s = splash;
                        if (s != null) {
                            s.splashScreen.update();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    next = System.currentTimeMillis() + 200;
                }
            }
        }

        /**
         * Defines the single line of text this component will display.
         */
        public void setText(final String text) {
            // trying to set again the same text?
            if (text != null && text.equals(this.text)) {
                return;
            }

            // run in AWT, there were problems with accessing font metrics
            // from now AWT thread
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    init();
                    if (text == null) {
                        repaint(dirty);
                        return;
                    }

                    if (statusBox.fm == null) {
                        return;
                    }

                    adjustText(text);

                    statusBox.layout(text, null);
                    dirty = dirty.union(statusBox.effectiveBounds);
                    // update screen (assume repaint manager optimizes unions;)
                    repaint(dirty);
                    dirty = new Rectangle(statusBox.effectiveBounds);
                }
            });
        }
        
        /**
         * Creates new text with the ellipsis at the end when text width is
         * bigger than allowed space
         */
        private void adjustText(String text) {
            String newText = null;
            String newString;
            
            if (text == null) {
                return ;
            }

            if (statusBox.fm == null) {           
                return;
            }
            
            int width = statusBox.fm.stringWidth(text);
            
            if (width > statusBox.bounds.width) {
                StringTokenizer st = new StringTokenizer(text);
                while (st.hasMoreTokens()) {
                    String element = st.nextToken();                                    
                    if (newText == null)
                        newString = element;
                    else
                        newString = newText + " " + element; // NOI18N
                    if (statusBox.fm.stringWidth(newString + "...") > statusBox.bounds.width) { // NOI18N
                        this.text = newText + "..."; // NOI18N
                        break;
                    } else                        
                        newText = newString;
                        
                }
                // #71064 - cut the text and put the ellipsis correctly when 
                // very loong text without spaces that exceeds available space is used
                // it can happen in multibyte environment (such as japanese) 
                if (newText == null) {
                    this.text = "";
                    newString = "";
                    newText = "";
                    for (int i = 0; i < text.length(); i++) {
                        newString += text.charAt(i);
                        if (statusBox.fm.stringWidth(newString + "...") > statusBox.bounds.width) { // NOI18N
                            this.text = newText + "..."; // NOI18N
                            break;
                        } else {
                            newText = newString;
                        }
                    }
                }
            } else
                this.text = text;
        }
    
        public void increment(int steps) {
            if (steps <= 0) {
                return;
            }
            progress += steps;
            if (progress > maxSteps) {
                progress = maxSteps;
            } else if (maxSteps > 0) {
                int bl = bar.width * progress / maxSteps - barStart;
                if (bl > 1 || barStart % 2 == 0) {
                    barLength = bl;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            init();
                            /* Don't try to be smart about which section of the bar to repaint.
                            There can be tricky rounding issues on HiDPI screens with non-integral
                            scaling factors (e.g. 150%). */
                            repaint(bar);
                        }
                    });
                }
            }
        }
	

        /** Adds space for given number of steps.
         * It also alters progress to preserve ratio between completed and total
         * number of steps.
         */
        final void addToMaxSteps(int steps) {
            if (steps == 0) {
                return;
            }
            if (maxSteps == 0) {
                int prog = progress / steps;
                maxSteps = steps;
                progress = prog;
            } else {
                int max = maxSteps + steps;
                int prog = progress * max / maxSteps;
                maxSteps = max;
                progress = prog;
            }
            // do repaint on next increment
        }
	
        void paint() {
            // loadContentIcon may return a null image
            if (image != null) {
                image.paintIcon(comp, graphics, 0, 0);
            }
            // turn anti-aliasing on for the splash text
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (versionBox != null) {
                String buildNumber = System.getProperty("netbeans.buildnumber");
                versionBox.layout(NbBundle.getMessage(TopLogging.class, "currentVersion", buildNumber), graphics);
            }
            if (text != null) {
                statusBox.layout(text, graphics);
            }

            // Draw progress bar if applicable
            if (!noBar && maxSteps > 0/* && barLength > 0*/) {
                graphics.setColor(color_bar);
                graphics.fillRect(bar.x, bar.y, barStart + barLength, bar.height);
                /* To discourage visual artifacts on HiDPI displays, only paint the distinct
                corner/edge colors if the branding actually calls for them. */
                if (!color_bar.equals(color_corner)) {
                  graphics.setColor(color_corner);
                  graphics.drawLine(bar.x, bar.y, bar.x, bar.y + bar.height);
                  graphics.drawLine(bar.x + barStart + barLength, bar.y, bar.x + barStart + barLength, bar.y + bar.height);
                }
                if (!color_bar.equals(color_edge)) {
                  graphics.setColor(color_edge);
                  graphics.drawLine(bar.x, bar.y + bar.height / 2, bar.x, bar.y + bar.height / 2);
                  graphics.drawLine(bar.x + barStart + barLength, bar.y + bar.height / 2, bar.x + barStart + barLength, bar.y + bar.height / 2);
                }
                barStart += barLength;
                barLength = 0;
            }
        }
    }

    private static class SplashDialog extends JDialog implements ActionListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5185644855500178404L;

        private final SplashComponent splashComponent = new SplashComponent(true);
        
        /** Creates a new SplashDialog */
        public SplashDialog (java.awt.Frame parent, javax.swing.JComponent infoPanel) {
            super (parent, true);
    
            JPanel splashPanel = new JPanel();
            JTabbedPane tabbedPane = new JTabbedPane();
            setTitle (NbBundle.getMessage(Splash.class, "CTL_About_Title"));
            // add splash component
            splashPanel.add (splashComponent);
            tabbedPane.addTab(NbBundle.getMessage(Splash.class, "CTL_About_Title"), splashPanel);
            tabbedPane.addTab(NbBundle.getMessage(Splash.class, "CTL_About_Detail"), infoPanel);
            getContentPane().add(tabbedPane, BorderLayout.CENTER);

            getRootPane().registerKeyboardAction(
                this,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
            );
            
            tabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Splash.class, "ACSN_AboutTabs"));
            tabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Splash.class, "ACSD_AboutTabs"));
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Splash.class, "ACSD_AboutDialog"));
            
            Splash.center(this);
            
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible (false);
            dispose();
        }
    }

    private static class SplashRunner implements Runnable {

        private Window splashWindow;
        private boolean visible;

        public SplashRunner(Window splashWindow, boolean visible) {
            this.splashWindow = splashWindow;
            this.visible = visible;
        }

        @Override
        public void run() {
            if (visible) {
                Splash.center(splashWindow);
                splashWindow.setVisible(true);
                splashWindow.toFront ();
            }
            else {
                splashWindow.setVisible (false);
                splashWindow.dispose ();
            }
        }
    }

}
