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
package org.netbeans.conffile.ui;

import org.netbeans.conffile.MemoryValue;
import org.netbeans.conffile.ArgsParser;
import org.netbeans.conffile.LineSwitchContributor;
import org.netbeans.conffile.LineSwitchWriter;
import org.netbeans.conffile.ArgsParser.ArgsResult;
import org.netbeans.conffile.ConfFile;
import static org.netbeans.conffile.Main.ARG_CRT;
import static org.netbeans.conffile.Main.ARG_FILE;
import static org.netbeans.conffile.Main.NETBEANS_DEFAULT_OPTIONS;
import static org.netbeans.conffile.Main.VERSION;
import org.netbeans.conffile.OS;
import org.netbeans.conffile.OS.ChassisType;
import static org.netbeans.conffile.ui.HintEntry.ALL_HINTS;
import org.netbeans.conffile.ui.comp.UIUtils;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.conffile.Main;

/**
 *
 * @author Tim Boudreau
 */
final class ConfFileSettings implements LineSwitchContributor {

    private String font = "SansSerif";
    private int fontSize = DisplayAutoConfigurer.adjustFontSizeForScreenSize(14);
    private HintEntry renderingHint = HintEntry.HINT_DEFAULT;
    private final List<WeakReference<Runnable>> listeners = new CopyOnWriteArrayList<>();
    private Font uiFont = new Font(font, Font.PLAIN, fontSize);
    private Font labelFont = uiFont.deriveFont(Font.BOLD).deriveFont(uiFont.getSize2D() - 1F);
    private Font largeFont = uiFont.deriveFont(Font.BOLD).deriveFont(uiFont.getSize2D() + 4F);
    private Font smallFont = uiFont.deriveFont(uiFont.getSize2D() - 2F);
    private MemorySettings mem;
    private final ArgsParser.ArgsResult result;
    private Path path;
    private final Set<TweakEntry> tweaks = new HashSet<>();
    private final Set<TweakEntry> origTweaks = new HashSet<>();
    private final List<WeakReference<IntConsumer>> fontSizeChangeListeners = new CopyOnWriteArrayList<>();
    private DisplayType displayType = DisplayType.LCD;
    private Boolean useLargestDisplayMode;
    private DisplayKind displayKind;
    private String userName = null;
    private DisplayMode displayMode = DisplayAutoConfigurer.displayModeNonGui(false);
    private boolean hasGcSettings;
    private boolean hasHiDpi;
    private boolean hasDpiAware;
    private GraphicsConfiguration preferredGraphicsConfiguration;

    private Charset charset = Charset.defaultCharset();

    ConfFileSettings(ArgsResult result) {
        this.result = result;
        if (result.isSet(ARG_CRT)) {
            displayType = DisplayType.CRT;
        } else {
            displayType = DisplayAutoConfigurer.isCrtAspectRatio(displayMode)
                    ? DisplayType.CRT : DisplayType.LCD;
        }
        loadExistingValues(true);
        String screen = result.get(Main.ARG_SCREEN);
        if (screen != null) {
            preferredGraphicsConfiguration = DisplayAutoConfigurer
                    .getGraphicsConfigurationById(screen);
        }
    }

    public GraphicsConfiguration preferredGraphicsConfiguration() {
        return preferredGraphicsConfiguration;
    }

    public ConfFileSettings preferredGraphicsConfiguration(GraphicsConfiguration dev) {
        preferredGraphicsConfiguration = dev;
        return change();
    }

    public static String hardwareSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append("sig-").append(VERSION).append('-');
        OS.get().appendSignature(sb);
        DisplayAutoConfigurer.appendSig(sb);
        String sigString = sb.toString().replaceAll("\\s", "").replace('/', '-')
                .replace('\\', '-').replace(':', '-').replace('=', '0');

        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(sigString.getBytes(US_ASCII));
            return Base64.getUrlEncoder().encodeToString(b).replace('=', '0');
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConfFileSettings.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sigString;
    }

    public Charset charset() {
        return charset;
    }

    public String userName() {
        if (userName == null || userName.isEmpty()) {
            return System.getProperty("user.name");
        }
        return userName;
    }

    public ConfFileSettings charset(Charset charset) {
        this.charset = charset;
        return change();
    }

    public Set<String> availableUIFonts() {
        return UIUtils.availableUIFonts(font);
    }

    private void loadExistingValues(boolean replace) {
        Path file = confFilePath();
        if (file != null) {
            ConfFile f = new ConfFile(file);
            try {
                Map<String, List<String>> opts = f.parse();
                if (!opts.containsKey(NETBEANS_DEFAULT_OPTIONS)) {
                    return;
                }
                String loadedUserName = ConfFile.findSystemPropertySetting(
                        "user.name", opts); // NOI18N
                if (loadedUserName != null) {
                    if (!replace) {
                        if (this.userName == null || this.userName.equals(System.getProperty("user.name"))) { // NOI18N
                            this.userName = loadedUserName;
                        }
                    } else {
                        this.userName = loadedUserName;
                    }
                }
                if (replace || "SansSerif".equals(font)) { // NOI18N
                    String uiFontName = ConfFile.findSystemPropertySetting("uiFontName", opts); // NOI18N
                    if (uiFontName != null) {
                        font = uiFontName;
                    }
                }
                String enc = ConfFile.findSystemPropertySetting("file.encoding", opts); // NOI18N
                if (enc != null) {
                    if ("UTF-8".equals(enc)) {
                        charset = UTF_8;
                    } else {
                        try {
                            charset = Charset.forName(enc);
                        } catch (Exception e) {
                            Logger.getLogger(ConfFileSettings.class.getName())
                                    .log(Level.WARNING, "Bad encoding in conf file", e); // NOI18N
                        }
                    }
                }
                String lcdStyle = ConfFile.findSystemPropertySetting("awt.useSystemAAFontSettings", opts); //NOI18N
                if (lcdStyle != null) {
                    HintEntry hints = HintEntry.parse(lcdStyle);
                    if (hints != null) {
                        this.renderingHint = hints;
                    }
                }
                if (replace) {
                    String consoleLogger = ConfFile.findSystemPropertySetting("netbeans.logger.console", opts); //NOI18N
                    boolean useConsoleLogger = "true".equalsIgnoreCase(consoleLogger);
                    if (useConsoleLogger) {
                        this.tweaks.add(TweakEntry.CONSOLE_LOGGER);
                        this.origTweaks.add(TweakEntry.CONSOLE_LOGGER);
                    } else {
                        this.tweaks.remove(TweakEntry.CONSOLE_LOGGER);
                    }
                    String menuStatusLine = ConfFile.findSystemPropertySetting("netbeans.winsys.statusLine.in.menuBar", //NOI18N
                            opts);
                    boolean statusLineInMenuBar = "true".equalsIgnoreCase(menuStatusLine);
                    if (statusLineInMenuBar) {
                        this.tweaks.add(TweakEntry.STATUS_LINE_IN_MENU_BAR);
                        this.origTweaks.add(TweakEntry.STATUS_LINE_IN_MENU_BAR);
                    } else {
                        this.tweaks.remove(TweakEntry.STATUS_LINE_IN_MENU_BAR);
                    }

                    String openGl = ConfFile.findSystemPropertySetting("sun.java2d.opengl", //NOI18N
                            opts);
                    boolean useOpenGl = "true".equalsIgnoreCase(openGl);
                    if (useOpenGl) {
                        this.tweaks.add(TweakEntry.OPENGL_PIPELINE);
                        this.origTweaks.add(TweakEntry.OPENGL_PIPELINE);
                    } else {
                        this.tweaks.remove(TweakEntry.OPENGL_PIPELINE);
                    }
                }

                hasGcSettings = hasGcSettings(opts.get(NETBEANS_DEFAULT_OPTIONS));
                hasHiDpi = ConfFile.findSystemPropertySetting("hidpi", opts) != null; // NOI18N
                hasDpiAware = ConfFile.findSystemPropertySetting("sun.java2d.dpiaware", opts) != null; // NOI18N
                MemoryValue xms = ConfFile.findMemorySetting(MemoryValue.Kind.INITIAL_HEAP_SIZE, opts);
                MemoryValue xmx = ConfFile.findMemorySetting(MemoryValue.Kind.MAXIMUM_HEAP_SIZE, opts);
                if (xms != null || xmx != null) {
                    if (replace || mem == null) {
                        mem = new MemorySettings(xmx, xms, hasGcSettings);
                    }
                }
                if (!replace) {
                    change();
                }
            } catch (IOException ex) {
                Logger.getLogger(ConfFileSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static final String[] GC_ARGS = new String[]{
        "-J-XX:+UseG1GC", // NOI18N
        "-XX:+UseSerialGC", // NOI18N
        "-XX:+UseParallelGC", // NOI18N
        "-XX:+UseParNewGC", // NOI18N
        "-Xincgc", // NOI18N
        "-XX:+UseParallelOldGC", // NOI18N
        "-XX:+UseConcMarkSweepGC", // NOI18N
        "-XX:+UseZGC" // NOI18N
    };

    public static boolean hasGcSettings(List<String> opts) {
        if (opts == null) {
            return false;
        }
        Set<String> all = new HashSet<>(opts);
        all.retainAll(Arrays.asList(GC_ARGS));
        return !opts.isEmpty();
    }

    ConfFileSettings onFontSizeRecomputed(IntConsumer consumer) {
        fontSizeChangeListeners.add(new WeakReference<>(consumer));
        return this;
    }

    public List<HintEntry> availableHints() {
        List<HintEntry> hints = new ArrayList<>();
        for (HintEntry he : ALL_HINTS) {
            switch (this.displayType) {
                case CRT:
                    if (he.isCrtAppropriate()) {
                        hints.add(he);
                    }
                    break;
                case LCD:
                    hints.add(he);
            }
        }
        return hints;
    }

    int recomputeFontSize(GraphicsDevice device) {
        boolean large = this.displayKind == DisplayKind.LARGE_DESKTOP;
        @SuppressWarnings("UnnecessaryUnboxing")
        boolean useLargestMode = this.useLargestDisplayMode != null
                ? this.useLargestDisplayMode.booleanValue() : false;
        return recomputeFontSize(device, large, useLargestMode);
    }

    int recomputeFontSize(GraphicsDevice device, boolean useLargestAvailableMode) {
        boolean large = this.displayKind == DisplayKind.LARGE_DESKTOP;
        return recomputeFontSize(device, large, useLargestAvailableMode);
    }

    int recomputeFontSize(GraphicsDevice device, boolean large, boolean useLargestAvailableMode) {
        displayMode = device.getDisplayMode();
        int old = fontSize;
        fontSize = DisplayAutoConfigurer.adjustFontSizeForScreenSize(fontSize, device,
                useLargestAvailableMode, large);
        if (fontSize != old) {
            uiFont = new Font(uiFont.getName(), Font.PLAIN, fontSize);
            labelFont = uiFont.deriveFont(Font.BOLD).deriveFont(uiFont.getSize2D() - 1F);
            largeFont = uiFont.deriveFont(Font.BOLD).deriveFont(uiFont.getSize2D() + 4F);
            smallFont = uiFont.deriveFont(uiFont.getSize2D() - 2F);
            for (Iterator<WeakReference<IntConsumer>> it = fontSizeChangeListeners.iterator(); it.hasNext();) {
                WeakReference<IntConsumer> item = it.next();
                IntConsumer c = item.get();
                if (c == null) {
                    it.remove();
                } else {
                    c.accept(fontSize);
                }
            }
            change();
        }
        return fontSize;
    }

    void setConfFilePath(Path path) {
        this.path = path;
        if (path != null && Files.exists(path)) {
            loadExistingValues(false);
        }
    }

    Path confFilePath() {
        if (path != null) {
            return path;
        }
        String cmdLinePath = result.get(ARG_FILE); // NOI18N
        Path result = cmdLinePath == null ? null : Paths.get(cmdLinePath);
        if (result != null && Files.exists(result) && !Files.isDirectory(result)) {
            return result;
        }
        return null;
    }

    ArgsParser.ArgsResult args() {
        return result;
    }

    MemorySettings memory() {
        if (mem == null) {
            mem = new MemorySettings(hasGcSettings);
        }
        return mem;
    }

    Font uiFont() {
        return uiFont;
    }

    Font labelFont() {
        return labelFont;
    }

    Font largeFont() {
        return largeFont;
    }

    Font smallFont() {
        return smallFont;
    }

    void listen(Runnable run) {
        listeners.add(new WeakReference<>(run));
    }

    HintEntry selectedHint() {
        return renderingHint;
    }

    ConfFileSettings selectedHint(HintEntry entry) {
        renderingHint = entry;
        return change();
    }

    private ConfFileSettings change() {
        for (Iterator<WeakReference<Runnable>> it = listeners.iterator(); it.hasNext();) {
            WeakReference<Runnable> w = it.next();
            Runnable r = w.get();
            if (r == null) {
                it.remove();
            } else {
                r.run();
            }
        }
        return this;
    }

    Font getFont() {
        return new Font(font, Font.PLAIN, fontSize);
    }

    String font() {
        return font;
    }

    int size() {
        return fontSize;
    }

    Object hint() {
        return renderingHint;
    }

    String hintName() {
        return renderingHint.toString();
    }

    ConfFileSettings fontAndSize(String font, int size) {
        this.fontSize = size;
        this.font = font;
        return this;
    }

    ConfFileSettings size(int size) {
        this.fontSize = size;
        return change();
    }

    ConfFileSettings font(String font) {
        this.font = font;
        return change();
    }

    ConfFileSettings renderingHint(HintEntry hint) {
        this.renderingHint = hint;
        return change();
    }

    private String quoteIfContainsWhitespace(String fontString) {
        for (int i = 0; i < fontString.length(); i++) {
            if (Character.isWhitespace(fontString.charAt(i))) {
                fontString = '\'' + fontString + '\''; // NOI18N
                break;
            }
        }
        return fontString;
    }

    @Override
    public void contribute(LineSwitchWriter writer) {
        writer.appendOrReplaceArguments("--fontsize", Integer.toString(size())); // NOI18N
        String fontString = quoteIfContainsWhitespace(font.trim());
        writer.appendOrReplaceArguments("-J-DuiFontName=" + fontString); // NOI18N
        switch (renderingHint.name) {
            case "none": // NOI18N
            case "default": // NOI18N
                break;
            case "gasp": // NOI18N
                writer.appendOrReplaceArguments("-J-Dawt.useSystemAAFontSettings=gasp"); // NOI18N
                // For traditional antialiasing, these will also help:
                // swing.aatext was abandoned in JDK 6, but a number of pieces
                // of NetBeans code still check it
                writer.appendOrReplaceArguments("-J-Dswing.aatext=true"); // NOI18N
                // Turn on antialiasing for Node trees and lists
                writer.appendOrReplaceArguments("-J-Dnb.cellrenderer.antialiasing=true"); // NOI18N
                // Turn on additional hinting for the editor
                writer.appendOrReplaceArguments("-J-Dorg.netbeans.editor.aa.extra.hints=true"); // NOI18N
                break;
            default:
                // Use LCD antialiasing
                writer.appendOrReplaceArguments("-J-Dawt.useSystemAAFontSettings=lcd_"
                        + renderingHint.name); // NOI18N
        }
        if (!hasHiDpi && displayMode != null && displayMode.getHeight() > 1200) {
            writer.appendOrReplaceArguments("-J-Dhidpi=true"); // NOI18N
        }
        if (!hasDpiAware) {
            writer.appendOrReplaceArguments("-J-Dsun.java2d.dpiaware=true"); // NOI18N
        }
        mem.contribute(writer);
        for (TweakEntry t : tweaks) {
            writer.appendOrReplaceArguments(t.lineSwitch);
        }
        for (TweakEntry t : origTweaks) {
            if (!tweaks.contains(t)) {
                writer.removeArgument(t.lineSwitch);
            }
        }
        if (userName != null && !userName.isEmpty()) {
            String prop = System.getProperty("user.name"); // NOI18N
            if (!Objects.equals(userName, prop)) {
                writer.appendOrReplaceArguments("-J-Duser.name=" + quoteIfContainsWhitespace(userName)); // NOI18N
            }
        }
    }

    public void setUserName(String name) {
        userName = name.trim().replace('\'', ' ').replace('"', ' ');
    }

    boolean hasTweak(TweakEntry entry) {
        return tweaks.contains(entry);
    }

    void addTweak(TweakEntry tweak) {
        tweaks.add(tweak);
    }

    void removeTweak(TweakEntry tweak) {
        tweaks.remove(tweak);
    }

    ConfFileSettings displayKind(DisplayKind kind) {
        this.displayKind = kind;
        return change();
    }

    DisplayType displayType() {
        return displayType;
    }

    ConfFileSettings displayType(DisplayType type) {
        this.displayType = type;
        return change();
    }

    Boolean isLargeMonitor() {
        return useLargestDisplayMode;
    }

    ConfFileSettings isLargeMonitor(boolean val) {
        if (useLargestDisplayMode == null || useLargestDisplayMode.booleanValue() != val) {
            useLargestDisplayMode = val;
        }
        return change();
    }

    DisplayKind displayKind() {
        if (displayKind != null) {
            return displayKind;
        }
        ChassisType chassis = OS.get().systemType();
        switch (chassis) {
            case NOTEBOOK:
                return DisplayKind.LAPTOP;
            case DESKTOP:
                return DisplayKind.LARGE_DESKTOP;
            case UNKNOWN:
                return DisplayKind.SMALL_DESKTOP;
            default:
                return DisplayKind.SMALL_DESKTOP;
        }
    }

    public static enum DisplayKind {
        LARGE_DESKTOP,
        SMALL_DESKTOP,
        LAPTOP
    }

    public static enum DisplayType {
        LCD,
        CRT
    }
}
