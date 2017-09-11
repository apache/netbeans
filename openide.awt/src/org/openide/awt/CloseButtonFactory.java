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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.awt;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

/**
 * Factory class for Close Buttons.
 *
 * @author M. Kristofic
 * @since 7.38
 */
public final class CloseButtonFactory{

    private static Icon closeTabImage;
    private static Icon closeTabPressedImage;
    private static Icon closeTabMouseOverImage;
    private static Icon bigCloseTabImage;
    private static Icon bigCloseTabPressedImage;
    private static Icon bigCloseTabMouseOverImage;

    private CloseButtonFactory() {
    }

    /**
     * Creates a small 'close' JButton with close icon, rollover icon and pressed icon according to Look and Feel
     *
     * @return JButton with close icons.
     */
    public static JButton createCloseButton() {
        JButton closeButton = new JButton();
        int size = 16;
        closeButton.setPreferredSize(new Dimension(size, size));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setBorderPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setIcon(getCloseTabImage());
        closeButton.setRolloverIcon(getCloseTabRolloverImage());
        closeButton.setPressedIcon(getCloseTabPressedImage());
        return closeButton;
    }

    /**
     * Creates a big 'close' JButton with close icon, rollover icon and pressed icon according to Look and Feel
     *
     * @return JButton with close icons.
     */
    public static JButton createBigCloseButton() {
        JButton closeButton = new JButton();
        int size = 19;
        closeButton.setPreferredSize(new Dimension(size, size));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setBorderPainted(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setIcon(getBigCloseTabImage());
        closeButton.setRolloverIcon(getBigCloseTabRolloverImage());
        closeButton.setPressedIcon(getBigCloseTabPressedImage());
        return closeButton;
    }

    private static boolean isWindowsVistaLaF() {
        return isWindowsLaF() && (isWindowsVista() || isWindows7()) && isWindowsXPLaF();
    }

    private static boolean isWindows8LaF() {
        return isWindowsLaF() && isWindows8() && isWindowsXPLaF();
    }
    
    private static boolean isWindows10LaF() {
        return isWindowsLaF() && isWindows10() && isWindowsXPLaF();
    }

    private static boolean isWindowsVista() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Vista") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.0".equals( System.getProperty("os.version") ));
    }
    
    private static boolean isWindows10() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 10") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "10.0".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows8() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 8") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.2".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindows7() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 7") >= 0
            || (osName.equals( "Windows NT (unknown)" ) && "6.1".equals( System.getProperty("os.version") ));
    }

    private static boolean isWindowsXPLaF() {
        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().
                getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isWindowsLaF() && (isXP == null ? false : isXP.booleanValue());
    }

    private static boolean isWindowsLaF() {
        String lfID = UIManager.getLookAndFeel().getID();
        return lfID.endsWith("Windows"); //NOI18N
    }

    private static boolean isAquaLaF() {
        return "Aqua".equals(UIManager.getLookAndFeel().getID());
    }

    private static boolean isGTKLaF () {
        return "GTK".equals( UIManager.getLookAndFeel().getID() ); //NOI18N
    }

    private static Icon getCloseTabImage() {
        if( null == closeTabImage ) {
            String path = UIManager.getString("nb.close.tab.icon.enabled.name" ); //NOI18N
            if( null != path ) {
                closeTabImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_enabled.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_enabled.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_enabled.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_close_enabled.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_close_enabled.png", true); // NOI18N
            } else {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_close_enabled.png", true); // NOI18N
            }
        }
        return closeTabImage;
    }

    private static Icon getCloseTabPressedImage() {
        if( null == closeTabPressedImage ) {
            String path = UIManager.getString("nb.close.tab.icon.pressed.name" ); //NOI18N
            if( null != path ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabPressedImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_pressed.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_pressed.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_pressed.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_close_pressed.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_close_pressed.png", true); // NOI18N
            } else {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_close_pressed.png", true); // NOI18N
            }
        }
        return closeTabPressedImage;
    }

    private static Icon getCloseTabRolloverImage() {
        if( null == closeTabMouseOverImage ) {
            String path = UIManager.getString("nb.close.tab.icon.rollover.name" ); //NOI18N
            if( null != path ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabMouseOverImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_rollover.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_rollover.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_rollover.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_close_rollover.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_close_rollover.png", true); // NOI18N
            } else {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_close_rollover.png", true); // NOI18N
            }
        }
        return closeTabMouseOverImage;
    }


    private static Icon getBigCloseTabImage() {
        if( null == bigCloseTabImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.enabled.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_enabled.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_bigclose_enabled.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_bigclose_enabled.png", true); // NOI18N
            } else {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_bigclose_enabled.png", true); // NOI18N
            }
        }
        return bigCloseTabImage;
    }

    private static  Icon getBigCloseTabPressedImage() {
        if( null == bigCloseTabPressedImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.pressed.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabPressedImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_pressed.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_bigclose_pressed.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_bigclose_pressed.png", true); // NOI18N
            } else {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_bigclose_pressed.png", true); // NOI18N
            }
        }
        return bigCloseTabPressedImage;
    }

    private static Icon getBigCloseTabRolloverImage() {
        if( null == bigCloseTabMouseOverImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.rollover.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabMouseOverImage ) {
            if( isWindows8LaF() || isWindows10LaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win8_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_rollover.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/mac_bigclose_rollover.png", true); // NOI18N
            } else if( isGTKLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_bigclose_rollover.png", true); // NOI18N
            } else {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_bigclose_rollover.png", true); // NOI18N
            }
        }
        return bigCloseTabMouseOverImage;
    }
}
