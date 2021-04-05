#Signature file v4.1
#Version 1.0

CLSS public abstract interface com.formdev.flatlaf.FlatClientProperties
fld public final static java.lang.String BUTTON_TYPE = "JButton.buttonType"
fld public final static java.lang.String BUTTON_TYPE_HELP = "help"
fld public final static java.lang.String BUTTON_TYPE_SQUARE = "square"
fld public final static java.lang.String BUTTON_TYPE_TAB = "tab"
fld public final static java.lang.String MINIMUM_HEIGHT = "JComponent.minimumHeight"
fld public final static java.lang.String MINIMUM_WIDTH = "JComponent.minimumWidth"
fld public final static java.lang.String PLACEHOLDER_TEXT = "JTextField.placeholderText"
fld public final static java.lang.String PROGRESS_BAR_LARGE_HEIGHT = "JProgressBar.largeHeight"
fld public final static java.lang.String PROGRESS_BAR_SQUARE = "JProgressBar.square"
fld public final static java.lang.String SCROLL_BAR_SHOW_BUTTONS = "JScrollBar.showButtons"
fld public final static java.lang.String SELECTED_STATE = "JButton.selectedState"
fld public final static java.lang.String SELECTED_STATE_INDETERMINATE = "indeterminate"
fld public final static java.lang.String SELECT_ALL_ON_FOCUS_POLICY = "JTextField.selectAllOnFocusPolicy"
fld public final static java.lang.String SELECT_ALL_ON_FOCUS_POLICY_ALWAYS = "always"
fld public final static java.lang.String SELECT_ALL_ON_FOCUS_POLICY_NEVER = "never"
fld public final static java.lang.String SELECT_ALL_ON_FOCUS_POLICY_ONCE = "once"
fld public final static java.lang.String TABBED_PANE_HAS_FULL_BORDER = "JTabbedPane.hasFullBorder"
fld public final static java.lang.String TABBED_PANE_SHOW_TAB_SEPARATORS = "JTabbedPane.showTabSeparators"
fld public final static java.lang.String TABBED_PANE_TAB_HEIGHT = "JTabbedPane.tabHeight"
fld public final static java.lang.String TAB_BUTTON_SELECTED_BACKGROUND = "JToggleButton.tab.selectedBackground"
fld public final static java.lang.String TAB_BUTTON_UNDERLINE_COLOR = "JToggleButton.tab.underlineColor"
fld public final static java.lang.String TAB_BUTTON_UNDERLINE_HEIGHT = "JToggleButton.tab.underlineHeight"
meth public static boolean clientPropertyBoolean(javax.swing.JComponent,java.lang.String,boolean)
meth public static boolean clientPropertyEquals(javax.swing.JComponent,java.lang.String,java.lang.Object)
meth public static int clientPropertyInt(javax.swing.JComponent,java.lang.String,int)
meth public static java.awt.Color clientPropertyColor(javax.swing.JComponent,java.lang.String,java.awt.Color)

CLSS public com.formdev.flatlaf.FlatDarculaLaf
cons public init()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public static boolean install()
supr com.formdev.flatlaf.FlatDarkLaf

CLSS public com.formdev.flatlaf.FlatDarkLaf
cons public init()
meth public boolean isDark()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public static boolean install()
supr com.formdev.flatlaf.FlatLaf

CLSS public abstract com.formdev.flatlaf.FlatDefaultsAddon
cons public init()
meth public int getPriority()
meth public java.io.InputStream getDefaults(java.lang.Class<?>)
supr java.lang.Object

CLSS public com.formdev.flatlaf.FlatIntelliJLaf
cons public init()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public static boolean install()
supr com.formdev.flatlaf.FlatLightLaf

CLSS public abstract com.formdev.flatlaf.FlatLaf
cons public init()
meth public abstract boolean isDark()
meth public boolean isNativeLookAndFeel()
meth public boolean isSupportedLookAndFeel()
meth public java.lang.String getID()
meth public javax.swing.UIDefaults getDefaults()
meth public static boolean install(javax.swing.LookAndFeel)
meth public static boolean isShowMnemonics()
meth public static void initIconColors(javax.swing.UIDefaults,boolean)
meth public static void updateUI()
meth public void initialize()
meth public void uninitialize()
supr javax.swing.plaf.basic.BasicLookAndFeel
hfds LOG,base,desktopPropertyListener,desktopPropertyName,lastShowMnemonicWindow,mnemonicListener,postInitialization,showMnemonics
hcls LazyModifyInputMap

CLSS public com.formdev.flatlaf.FlatLightLaf
cons public init()
meth public boolean isDark()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public static boolean install()
supr com.formdev.flatlaf.FlatLaf

CLSS public com.formdev.flatlaf.IntelliJTheme
cons public init(java.io.InputStream) throws java.io.IOException
fld public final boolean dark
fld public final java.lang.String author
fld public final java.lang.String name
innr public static ThemeLaf
meth public static boolean install(java.io.InputStream)
meth public static com.formdev.flatlaf.FlatLaf createLaf(com.formdev.flatlaf.IntelliJTheme)
meth public static com.formdev.flatlaf.FlatLaf createLaf(java.io.InputStream) throws java.io.IOException
supr java.lang.Object
hfds checkboxDuplicateColors,checkboxKeyMapping,colors,icons,namedColors,noWildcardReplace,ui,uiKeyCopying,uiKeyInverseMapping,uiKeyMapping

CLSS public static com.formdev.flatlaf.IntelliJTheme$ThemeLaf
 outer com.formdev.flatlaf.IntelliJTheme
cons public init(com.formdev.flatlaf.IntelliJTheme)
meth public boolean isDark()
meth public com.formdev.flatlaf.IntelliJTheme getTheme()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public javax.swing.UIDefaults getDefaults()
supr com.formdev.flatlaf.FlatLaf
hfds theme

CLSS public com.formdev.flatlaf.util.ColorFunctions
cons public init()
innr public abstract interface static ColorFunction
innr public static Darken
innr public static Lighten
meth public !varargs static java.awt.Color applyFunctions(java.awt.Color,com.formdev.flatlaf.util.ColorFunctions$ColorFunction[])
supr java.lang.Object

CLSS public abstract interface static com.formdev.flatlaf.util.ColorFunctions$ColorFunction
 outer com.formdev.flatlaf.util.ColorFunctions
meth public abstract void apply(float[])

CLSS public static com.formdev.flatlaf.util.ColorFunctions$Darken
 outer com.formdev.flatlaf.util.ColorFunctions
cons public init(float,boolean,boolean)
meth protected boolean shouldInverse(float[])
supr com.formdev.flatlaf.util.ColorFunctions$Lighten

CLSS public static com.formdev.flatlaf.util.ColorFunctions$Lighten
 outer com.formdev.flatlaf.util.ColorFunctions
cons public init(float,boolean,boolean)
intf com.formdev.flatlaf.util.ColorFunctions$ColorFunction
meth protected boolean shouldInverse(float[])
meth public void apply(float[])
supr java.lang.Object
hfds amount,autoInverse,relative

CLSS public com.formdev.flatlaf.util.DerivedColor
cons public !varargs init(com.formdev.flatlaf.util.ColorFunctions$ColorFunction[])
meth public java.awt.Color derive(java.awt.Color)
supr javax.swing.plaf.ColorUIResource
hfds functions

CLSS public com.formdev.flatlaf.util.HSLColor
cons public init(float,float,float)
cons public init(float,float,float,float)
cons public init(float[])
cons public init(float[],float)
cons public init(java.awt.Color)
meth public float getAlpha()
meth public float getHue()
meth public float getLuminance()
meth public float getSaturation()
meth public float[] getHSL()
meth public java.awt.Color adjustHue(float)
meth public java.awt.Color adjustLuminance(float)
meth public java.awt.Color adjustSaturation(float)
meth public java.awt.Color adjustShade(float)
meth public java.awt.Color adjustTone(float)
meth public java.awt.Color getComplementary()
meth public java.awt.Color getRGB()
meth public java.lang.String toString()
meth public static float[] fromRGB(java.awt.Color)
meth public static java.awt.Color toRGB(float,float,float)
meth public static java.awt.Color toRGB(float,float,float,float)
meth public static java.awt.Color toRGB(float[])
meth public static java.awt.Color toRGB(float[],float)
supr java.lang.Object
hfds alpha,hsl,rgb

CLSS public com.formdev.flatlaf.util.HiDPIUtils
cons public init()
innr public abstract interface static Painter
meth public static void paintAtScale1x(java.awt.Graphics2D,int,int,int,int,com.formdev.flatlaf.util.HiDPIUtils$Painter)
meth public static void paintAtScale1x(java.awt.Graphics2D,int,int,int,int,double,com.formdev.flatlaf.util.HiDPIUtils$Painter)
meth public static void paintAtScale1x(java.awt.Graphics2D,javax.swing.JComponent,com.formdev.flatlaf.util.HiDPIUtils$Painter)
supr java.lang.Object

CLSS public abstract interface static com.formdev.flatlaf.util.HiDPIUtils$Painter
 outer com.formdev.flatlaf.util.HiDPIUtils
meth public abstract void paint(java.awt.Graphics2D,int,int,int,int,double)

CLSS public com.formdev.flatlaf.util.JavaCompatibility
cons public init()
meth public static void drawStringUnderlineCharAt(javax.swing.JComponent,java.awt.Graphics,java.lang.String,int,int,int)
supr java.lang.Object
hfds drawStringUnderlineCharAtMethod

CLSS public com.formdev.flatlaf.util.StringUtils
cons public init()
meth public static java.lang.String removeLeading(java.lang.String,java.lang.String)
meth public static java.lang.String removeTrailing(java.lang.String,java.lang.String)
meth public static java.util.List<java.lang.String> split(java.lang.String,char)
supr java.lang.Object

CLSS public com.formdev.flatlaf.util.SystemInfo
cons public init()
fld public final static boolean IS_JAVA_9_OR_LATER
fld public final static boolean IS_JETBRAINS_JVM
fld public final static boolean IS_KDE
fld public final static boolean IS_LINUX
fld public final static boolean IS_MAC
fld public final static boolean IS_MAC_OS_10_11_EL_CAPITAN_OR_LATER
fld public final static boolean IS_WINDOWS
supr java.lang.Object

CLSS public com.formdev.flatlaf.util.UIScale
cons public init()
meth public static boolean isSystemScalingEnabled()
meth public static double getSystemScaleFactor(java.awt.Graphics2D)
meth public static double getSystemScaleFactor(java.awt.GraphicsConfiguration)
meth public static float getUserScaleFactor()
meth public static float scale(float)
meth public static float unscale(float)
meth public static int scale(int)
meth public static int scale2(int)
meth public static int unscale(int)
meth public static java.awt.Dimension scale(java.awt.Dimension)
meth public static java.awt.Insets scale(java.awt.Insets)
meth public static javax.swing.plaf.FontUIResource applyCustomScaleFactor(javax.swing.plaf.FontUIResource)
meth public static javax.swing.plaf.FontUIResource scaleFont(javax.swing.plaf.FontUIResource,float)
meth public static void scaleGraphics(java.awt.Graphics2D)
supr java.lang.Object
hfds DEBUG,initialized,jreHiDPI,scaleFactor

CLSS public java.awt.Color
cons public init(float,float,float)
cons public init(float,float,float,float)
cons public init(int)
cons public init(int,boolean)
cons public init(int,int,int)
cons public init(int,int,int,int)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["red", "green", "blue", "alpha"])
cons public init(java.awt.color.ColorSpace,float[],float)
fld public final static java.awt.Color BLACK
fld public final static java.awt.Color BLUE
fld public final static java.awt.Color CYAN
fld public final static java.awt.Color DARK_GRAY
fld public final static java.awt.Color GRAY
fld public final static java.awt.Color GREEN
fld public final static java.awt.Color LIGHT_GRAY
fld public final static java.awt.Color MAGENTA
fld public final static java.awt.Color ORANGE
fld public final static java.awt.Color PINK
fld public final static java.awt.Color RED
fld public final static java.awt.Color WHITE
fld public final static java.awt.Color YELLOW
fld public final static java.awt.Color black
fld public final static java.awt.Color blue
fld public final static java.awt.Color cyan
fld public final static java.awt.Color darkGray
fld public final static java.awt.Color gray
fld public final static java.awt.Color green
fld public final static java.awt.Color lightGray
fld public final static java.awt.Color magenta
fld public final static java.awt.Color orange
fld public final static java.awt.Color pink
fld public final static java.awt.Color red
fld public final static java.awt.Color white
fld public final static java.awt.Color yellow
intf java.awt.Paint
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public float[] getColorComponents(float[])
meth public float[] getColorComponents(java.awt.color.ColorSpace,float[])
meth public float[] getComponents(float[])
meth public float[] getComponents(java.awt.color.ColorSpace,float[])
meth public float[] getRGBColorComponents(float[])
meth public float[] getRGBComponents(float[])
meth public int getAlpha()
meth public int getBlue()
meth public int getGreen()
meth public int getRGB()
meth public int getRed()
meth public int getTransparency()
meth public int hashCode()
meth public java.awt.Color brighter()
meth public java.awt.Color darker()
meth public java.awt.PaintContext createContext(java.awt.image.ColorModel,java.awt.Rectangle,java.awt.geom.Rectangle2D,java.awt.geom.AffineTransform,java.awt.RenderingHints)
meth public java.awt.color.ColorSpace getColorSpace()
meth public java.lang.String toString()
meth public static float[] RGBtoHSB(int,int,int,float[])
meth public static int HSBtoRGB(float,float,float)
meth public static java.awt.Color decode(java.lang.String)
meth public static java.awt.Color getColor(java.lang.String)
meth public static java.awt.Color getColor(java.lang.String,int)
meth public static java.awt.Color getColor(java.lang.String,java.awt.Color)
meth public static java.awt.Color getHSBColor(float,float,float)
supr java.lang.Object
hfds FACTOR,cs,falpha,frgbvalue,fvalue,serialVersionUID,value

CLSS public abstract interface java.awt.Paint
intf java.awt.Transparency
meth public abstract java.awt.PaintContext createContext(java.awt.image.ColorModel,java.awt.Rectangle,java.awt.geom.Rectangle2D,java.awt.geom.AffineTransform,java.awt.RenderingHints)

CLSS public abstract interface java.awt.Transparency
fld public final static int BITMASK = 2
fld public final static int OPAQUE = 1
fld public final static int TRANSLUCENT = 3
meth public abstract int getTransparency()

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract javax.swing.LookAndFeel
cons public init()
meth public abstract boolean isNativeLookAndFeel()
meth public abstract boolean isSupportedLookAndFeel()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getID()
meth public abstract java.lang.String getName()
meth public boolean getSupportsWindowDecorations()
meth public java.lang.String toString()
meth public javax.swing.Icon getDisabledIcon(javax.swing.JComponent,javax.swing.Icon)
meth public javax.swing.Icon getDisabledSelectedIcon(javax.swing.JComponent,javax.swing.Icon)
meth public javax.swing.LayoutStyle getLayoutStyle()
meth public javax.swing.UIDefaults getDefaults()
meth public static java.lang.Object getDesktopPropertyValue(java.lang.String,java.lang.Object)
meth public static java.lang.Object makeIcon(java.lang.Class<?>,java.lang.String)
meth public static javax.swing.ComponentInputMap makeComponentInputMap(javax.swing.JComponent,java.lang.Object[])
meth public static javax.swing.InputMap makeInputMap(java.lang.Object[])
meth public static javax.swing.text.JTextComponent$KeyBinding[] makeKeyBindings(java.lang.Object[])
meth public static void installBorder(javax.swing.JComponent,java.lang.String)
meth public static void installColors(javax.swing.JComponent,java.lang.String,java.lang.String)
meth public static void installColorsAndFont(javax.swing.JComponent,java.lang.String,java.lang.String,java.lang.String)
meth public static void installProperty(javax.swing.JComponent,java.lang.String,java.lang.Object)
meth public static void loadKeyBindings(javax.swing.InputMap,java.lang.Object[])
meth public static void uninstallBorder(javax.swing.JComponent)
meth public void initialize()
meth public void provideErrorFeedback(java.awt.Component)
meth public void uninitialize()
supr java.lang.Object

CLSS public javax.swing.plaf.ColorUIResource
cons public init(float,float,float)
cons public init(int)
cons public init(int,int,int)
 anno 0 java.beans.ConstructorProperties(java.lang.String[] value=["red", "green", "blue"])
cons public init(java.awt.Color)
intf javax.swing.plaf.UIResource
supr java.awt.Color

CLSS public abstract interface javax.swing.plaf.UIResource

CLSS public abstract javax.swing.plaf.basic.BasicLookAndFeel
cons public init()
intf java.io.Serializable
meth protected javax.swing.Action createAudioAction(java.lang.Object)
meth protected javax.swing.ActionMap getAudioActionMap()
meth protected void initClassDefaults(javax.swing.UIDefaults)
meth protected void initComponentDefaults(javax.swing.UIDefaults)
meth protected void initSystemColorDefaults(javax.swing.UIDefaults)
meth protected void loadSystemColors(javax.swing.UIDefaults,java.lang.String[],boolean)
meth protected void playSound(javax.swing.Action)
meth public javax.swing.UIDefaults getDefaults()
meth public void initialize()
meth public void uninitialize()
supr javax.swing.LookAndFeel
hfds audioLock,clipPlaying,disposer,invocator,needsEventHelper
hcls AWTEventHelper,AudioAction

