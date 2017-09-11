/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.util;

import java.awt.Color;
import javax.swing.UIManager;
import org.jdesktop.swingx.plaf.UIManagerExt;

/**
 * Helper to deal with different color schemes.
 * 
 * <p>This allows  handling of dark color themes without needing explicit 
 * configuration per LAF/color scheme</p>
 * 
 * <p>Primary motivation are LAFs that are not tied to their color scheme.
 * For nimbus and metal netbeans has special dark LAFs that also customized the
 * color schemes for DB support. But the Linux GTK and Windows themes follow the
 * settings from the environment. With these color settings can't be deduced from
 * LAF-name/class.
 * </p>
 * 
 * <p>See isDarkTheme for an explantion how a dark theme is detected.</p>
 */
public class ColorHelper {

    private static Boolean isDark = null;
    
    private ColorHelper() {
    }
    
    private static boolean isDarkTheme() {
        if(isDark != null) {
            return isDark;
        }
        // Based on tests with different LAFs and color combinations, a light
        // theme can be reliably detected by observing the brightness value of
        // the HSB Values of Table.background and Table.foreground
        //
        // Results from the test (Theme / Foreground / Background)
        // Gtk - Numix (light) / 0.2 / 0.97
        // Gtk - BlackMATE (dark) / 1.0 / 0.24
        // Gtk - Clearlooks (light) / 0.1 / 1.0
        // Gtk - ContrastHighInverse (dark) / 1.0 / 0.0
        // Gtk - DustSand (light) / 0.19 / 1.0
        // Gtk - TraditionalOkTest (light) / 0.0 / 0.74
        // Gtk - Menta (light) / 0.17 / 0.96
        // DarkNimbus (dark) / 0.9 / 0.19
        // DarkMetal (dark) / 0.87 / 0.19
        // CDE (light) / 0.0 / 0.76
        // Nimbus (light) / 0.0 / 1.0
        // Metall (light) / 0.2 / 1.0
        // Windows (light) / 0.0 / 1.0
        // Windows Classic (light) / 0.0 / 1.0
        // Windows HighContrast Black (dark) / 1.0 / 0
        Color foreground = UIManager.getColor("Table.foreground");
        Color background = UIManager.getColor("Table.background");
        float foreground_brightness = Color.RGBtoHSB(
                foreground.getRed(),
                foreground.getGreen(), 
                foreground.getBlue(), 
                null)[2];
        float background_brightness = Color.RGBtoHSB(
                background.getRed(),
                background.getGreen(), 
                background.getBlue(), 
                null)[2];
        isDark = background_brightness < foreground_brightness;
        return isDark;
    }

    public static Color getTableGrid() {
        Color managerColor = UIManager.getColor("nb.dataview.table.grid");
        if(managerColor == null) {
            UIManager.put("nb.dataview.table.grid", new Color(14277081));
            return UIManager.getColor("nb.dataview.table.grid");
        } else {
            return managerColor;
        }
    }
    
    public static Color getTableBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.table.background");
        if(managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.table.background", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.table.background", Color.WHITE); //NOI18N
            }
            return UIManager.getColor("nb.dataview.table.background"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTableAltbackground() {
        Color managerColor = UIManager.getColor("nb.dataview.table.altbackground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.table.altbackground", new RelativeColor(new Color(0, 0, 0), new Color(30, 30, 30), "Table.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.table.altbackground", new Color(0.92F, 0.95F, 0.99F));
            }
            return UIManager.getColor("nb.dataview.table.altbackground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTableSqlconstantForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.table.sqlconstant.foreground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.table.sqlconstant.foreground", new Color(220, 220, 220)); //NOI18N
            } else {
                UIManager.put("nb.dataview.table.sqlconstant.foreground", Color.DARK_GRAY);
            }
            return UIManager.getColor("nb.dataview.table.sqlconstant.foreground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTablecellFocused() {
        Color managerColor = UIManager.getColor("nb.dataview.tablecell.focused");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.tablecell.focused", new RelativeColor(new Color(0, 0, 0), new Color(10,10,30), "Table.selectionBackground")); //NOI18N
            } else {
                UIManager.put("nb.dataview.tablecell.focused", new Color(204,204,255));
            }
            return UIManager.getColor("nb.dataview.tablecell.focused"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTableRollOverRowBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.table.rollOverRowBackground");
        if (managerColor == null) {
            if (isDarkTheme()) {
                if (UIManager.getColor("Table[Enabled+Selected].textBackground") != null) {
                    UIManager.put("nb.dataview.table.rollOverRowBackground", new RelativeColor(new Color(0, 0, 0), new Color(30, 30, 30), "Table[Enabled+Selected].textBackground")); //NOI18N
                } else {
                    UIManager.put("nb.dataview.table.rollOverRowBackground", new RelativeColor(new Color(0, 0, 0), new Color(30, 30, 30), "Table.selectionBackground")); //NOI18N
                }
            } else {
                UIManager.put("nb.dataview.table.rollOverRowBackground", new Color(0.94F, 0.96F, 0.96F));
            }
            return UIManager.getColor("nb.dataview.table.rollOverRowBackground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTablecellEditedSelectedForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.tablecell.edited.selected.foreground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.tablecell.edited.selected.foreground", new Color(255, 248, 60)); //NOI18N
            } else {
                UIManager.put("nb.dataview.tablecell.edited.selected.foreground", new Color(229, 148, 0));
            }
            return UIManager.getColor("nb.dataview.tablecell.edited.selected.foreground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getTablecellEditedUnselectedForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.tablecell.edited.unselected.foreground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.tablecell.edited.unselected.foreground", new Color(0, 255, 16)); //NOI18N
            } else {
                UIManager.put("nb.dataview.tablecell.edited.unselected.foreground", new Color(0, 128, 0)); // green color
            }
            return UIManager.getColor("nb.dataview.tablecell.edited.unselected.foreground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.background");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.background", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.background", UIManagerExt.getColor("JXMonthView.background"));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.background"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.foreground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.foreground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.foreground")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.foreground", UIManagerExt.getColor("JXMonthView.foreground"));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.foreground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerSelectedBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.selectedBackground");
        if (managerColor == null) {
            if (isDarkTheme()) {
                if (UIManager.getColor("Table[Enabled+Selected].textBackground") != null) {
                    UIManager.put("nb.dataview.jxdatetimepicker.selectedBackground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table[Enabled+Selected].textBackground")); //NOI18N
                } else {
                    UIManager.put("nb.dataview.jxdatetimepicker.selectedBackground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.selectionBackground")); //NOI18N
                }
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.selectedBackground", UIManagerExt.getColor("JXMonthView.selectedBackground"));
            }

            return UIManager.getColor("nb.dataview.jxdatetimepicker.selectedBackground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerSelectedForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.selectedForeground");
        if (managerColor == null) {
            if (isDarkTheme()) {
                if (UIManager.getColor("Table[Enabled+Selected].textForeground") != null) {
                    UIManager.put("nb.dataview.jxdatetimepicker.selectedForeground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table[Enabled+Selected].textForeground")); //NOI18N
                } else {
                    UIManager.put("nb.dataview.jxdatetimepicker.selectedForeground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.selectionForeground")); //NOI18N
                }
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.selectedForeground", UIManagerExt.getColor("JXMonthView.selectedForeground"));
            }

            return UIManager.getColor("nb.dataview.jxdatetimepicker.selectedForeground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerDaysOfTheWeekForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.daysOfTheWeekForeground");
        if (managerColor == null) {
            if (isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.daysOfTheWeekForeground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "Table.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.daysOfTheWeekForeground", UIManagerExt.getColor("JXMonthView.daysOfTheWeekForeground"));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.daysOfTheWeekForeground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerTodayBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.todayBackground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.todayBackground", new RelativeColor(new Color(0, 0, 0), new Color(20, 20, 20), "Table.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.todayBackground", UIManagerExt.getColor("JXMonthView.foreground"));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.todayBackground"); //NOI18N
        } else {
            return managerColor;
        }
    }
    
    public static Color getJxdatetimepickerTodayPanelBackgroundGradientStart() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.start");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.start", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "TableHeader.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.start", new Color(238, 238, 238));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.start"); //NOI18N
        } else {
            return managerColor;
        }
    }
    
    public static Color getJxdatetimepickerTodayPanelBackgroundGradientEnd() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.end");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.end", new RelativeColor(new Color(0, 0, 0), new Color(10, 10, 10), "TableHeader.background")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.end", Color.WHITE);
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.background.gradient.end"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerTodayPanelLinkForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.linkForeground");
        if (managerColor == null) {
            if(isDarkTheme()) {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.linkForeground", new RelativeColor(new Color(0, 0, 0), new Color(0, 0, 0), "TableHeader.foreground")); //NOI18N
            } else {
                UIManager.put("nb.dataview.jxdatetimepicker.todayPanel.linkForeground", new Color(16, 66, 104));
            }
            return UIManager.getColor("nb.dataview.jxdatetimepicker.todayPanel.linkForeground"); //NOI18N
        } else {
            return managerColor;
        }
    }
    
    public static Color getJxdatetimepickerMonthStringBackground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.monthStringBackground");
        if (managerColor == null) {
            UIManager.put("nb.dataview.jxdatetimepicker.monthStringBackground", UIManagerExt.getColor("JXMonthView.monthStringBackground")); //NOI18N
            return UIManager.getColor("nb.dataview.jxdatetimepicker.monthStringBackground"); //NOI18N
        } else {
            return managerColor;
        }
    }

    public static Color getJxdatetimepickerMonthStringForeground() {
        Color managerColor = UIManager.getColor("nb.dataview.jxdatetimepicker.monthStringForeground");
        if (managerColor == null) {
            UIManager.put("nb.dataview.jxdatetimepicker.monthStringForeground", UIManagerExt.getColor("JXMonthView.monthStringForeground")); //NOI18N
            return UIManager.getColor("nb.dataview.jxdatetimepicker.monthStringForeground"); //NOI18N
        } else {
            return managerColor;
        }
    }
}
