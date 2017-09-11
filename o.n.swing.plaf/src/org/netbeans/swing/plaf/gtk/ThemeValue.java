/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.swing.plaf.gtk;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthUI;
import org.netbeans.swing.plaf.util.UIUtils;

/**
 * Value which will look something up via reflection from the GTK theme.
 *
 * @author  Tim Boudreau
 */
final class ThemeValue implements UIDefaults.ActiveValue {
    private final Object fallback;
    private final Region aRegion;
    private ColorType aColorType = null;
    private boolean darken = false;
    
    private Object value = null;
    
    private static Boolean functioning = null;
    
    /** Creates a new instance of GTKColor */
    public ThemeValue(Region region, ColorType colorType, Object fallback) {
        this.fallback = fallback;
        this.aRegion = region;
        this.aColorType = colorType;
        register(this);
    }

    /** Creates a new instance of GTKColor */
    public ThemeValue(Region region, ColorType colorType, Object fallback, boolean darken) {
        this.fallback = fallback;
        this.aRegion = region;
        this.aColorType = colorType;
        this.darken = darken;
        register(this);
    }
    
    public ThemeValue (Region region, Font fallback) {
        this.fallback = fallback;
        this.aRegion = region;
        register(this);
    }
    
    public Object createValue(UIDefaults table) {
        if (value == null) {
            if (!functioning()) {
                value = fallback;
            } else {
                if (fallback instanceof Font) {
                    Object val = getFont();
                    if (ct++ < 4) {
                        //Wrong values returned if GTK not yet initialized
                        return val;
                    }
                    value = val;
                } else {
                    value = getColor();
                }
            }
        }
        return value != null ? value : fallback;
    }
    
    private int ct = 0;
    
    void clear() {
        value = null;
    }
    
    public Font getFont() {
        SynthStyle style = getSynthStyle (aRegion);
        if (Boolean.TRUE.equals(functioning)) {
            try {
                Font result = (Font) synthStyle_getFontForState.invoke (style,
                    new Object [] {
                        getSynthContext ()
                    });
                if (result == null) {
                    result = (Font) fallback;
                }
                return result;
            } catch (Exception e) {
                functioning = Boolean.FALSE;
                if (log) {
                    e.printStackTrace();
                }
            }
        }
        //This will only happen once, after which functioning will be false
        return null; 
    }
    
    private static boolean log = Boolean.getBoolean ("themeValue.log");

    public Color getColor () {
        SynthStyle style = getSynthStyle (aRegion);
        if (Boolean.TRUE.equals(functioning)) {
            try {
                Color result = (Color) synthStyle_getColorForState.invoke (style,
                    new Object [] {
                        getSynthContext (),
                        aColorType
                    });
                if (result == null) {
                    result = (Color) fallback;
                }
                if (darken) {
                    result = result.darker();
                }
                return result;
            } catch (Exception e) {
                functioning = Boolean.FALSE;
                if (log) {
                    e.printStackTrace();
                }
            }
        }
        //This will only happen once, after which functioning will be false
        return null;
    }    
    
    public static boolean functioning() {
        if (functioning == null) {
            checkFunctioning();
        }
        return functioning.booleanValue();
    }

    private static void checkFunctioning() {
        functioning = Boolean.FALSE;
        try {
            gtkColorType = UIUtils.classForName ("com.sun.java.swing.plaf.gtk.GTKColorType"); //NOI18N

            synthStyle_getColorForState = SynthStyle.class.getDeclaredMethod ("getColorForState",  //NOI18N
                 SynthContext.class, ColorType.class );
                 
            synthStyle_getColorForState.setAccessible(true);
            
            synthStyle_getFontForState = SynthStyle.class.getDeclaredMethod ("getFontForState", //NOI18N
                SynthContext.class );
                
            synthStyle_getFontForState.setAccessible(true);
            

            LIGHT = (ColorType) valueOfField (gtkColorType, "LIGHT"); //NOI18N
            DARK = (ColorType) valueOfField (gtkColorType, "DARK"); //NOI18N
            MID = (ColorType) valueOfField (gtkColorType, "MID"); //NOI18N
            BLACK = (ColorType) valueOfField (gtkColorType, "BLACK"); //NOI18N
            WHITE = (ColorType) valueOfField (gtkColorType, "WHITE"); //NOI18N

            functioning = Boolean.TRUE;
        } catch (Exception e) {
            System.err.println ("Cannot initialize GTK colors - using hardcoded defaults: " + e); //NOI18N
            if (log) {
                e.printStackTrace();
            }
            return;
        }
    }

    private static JButton getDummyButton() {
        if (dummyButton == null) {
            dummyButton = new JButton();
            CellRendererPane crp = new CellRendererPane();
            crp.add (dummyButton);
        }
        ButtonModel mdl = dummyButton.getModel();
        return dummyButton;
    }
    
    private static JButton dummyButton = null;
    
    private static SynthContext getSynthContext () {
        try {
            JButton dummyButton = getDummyButton();
            
            ButtonUI bui = dummyButton.getUI();
            if (bui instanceof SynthUI) {
                return ((SynthUI) bui).getContext(dummyButton);
            } else {
               throw new IllegalStateException ("I don't have a SynthButtonUI to play with"); //NOI18N
            }
        } catch (Exception e) {
            functioning = Boolean.FALSE;
            if (log) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static SynthStyle getSynthStyle (Region region) {
        return SynthLookAndFeel.getStyle(getDummyButton(), region);
    }

    private static Object valueOfField (Class clazz, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(null);
    }

    private static Class<?> gtkColorType = null;

    private static Method synthStyle_getColorForState = null;
    private static Method synthStyle_getFontForState = null;

    static ColorType /* <GTKColorType> */ LIGHT = null;
    static ColorType /* <GTKColorType> */ DARK = null;
    static ColorType /* <GTKColorType> */ BLACK = null;
    static ColorType /* <GTKColorType> */ WHITE = null;
    static ColorType /* <GTKColorType> */ MID = null;
    
    
    private static HashSet<ThemeValue> instances = null;
    /** 
     * Unbeautiful caching - the reflection lookup has serious performance
     * issues - we will cache values instead.  */
    private static synchronized void register (ThemeValue value) {
        if (instances == null) {
            instances = new HashSet<ThemeValue>();
            registerPcl();
        }
        instances.add (value);
    }
    
    private static void registerPcl() {
        PropertyChangeListener l = new Listener();
        UIManager.addPropertyChangeListener(l);
        
        //Thanks to Scott Violet for how to do this.  See also
        //com.sun.java.swing.plaf.gtk.GtkLookAndFeel.WeakPCL
        
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Gtk/FontName", l); //NOI18N
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Xft/DPI", l); //NOI18N
        Toolkit.getDefaultToolkit().addPropertyChangeListener(
            "gnome.Net/ThemeName", l); //NOI18N
        
    }
    
    private static class Listener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent pce) {
            if (pce.getSource() instanceof UIManager && "lookAndFeel".equals( //NOI18N
                pce.getPropertyName())) { 
                    
                String s = UIManager.getLookAndFeel().getClass().getName();
                if (s.indexOf("gtk") < 0) { //NOI18N
                    //We have changed look and feels somehow.  Unregister.
                    UIManager.removePropertyChangeListener(this);
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Gtk/FontName", this); //NOI18N
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Xft/DPI", this); //NOI18N
                    Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "gnome.Net/ThemeName", this); //NOI18N
                }
            } else {
                for (ThemeValue tv: instances) {
                    tv.clear();
                }
            }
        }
    }
    
    static {
        //This must be called to initialize the fields before anyone tries
        //to construct a ThemeValue passing, say, ThemeValue.LIGHT.  These are
        //populated with values from GTKLookAndFeel by reflection
        functioning();
    }
}
