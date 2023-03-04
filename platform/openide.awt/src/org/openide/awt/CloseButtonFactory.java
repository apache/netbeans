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

    private static boolean isWindows8OrAboveLaF() {
        return isWindowsLaF() && (isWindows8() || isWindows10() || isWindows11()) && isWindowsXPLaF();
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

    private static boolean isWindows11() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Windows 11") >= 0;
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
            closeTabImage = UIManager.getIcon("nb.close.tab.icon.enabled.icon"); //NOI18N
        }
        if( null == closeTabImage ) {
            String path = UIManager.getString("nb.close.tab.icon.enabled.name" ); //NOI18N
            if( null != path ) {
                closeTabImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabImage ) {
            if( isWindows8OrAboveLaF() ) {
                closeTabImage = Windows8VectorCloseButton.DEFAULT;
            } else if( isWindowsVistaLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_enabled.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_enabled.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_enabled.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabImage = AquaVectorCloseButton.DEFAULT;
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
            closeTabPressedImage = UIManager.getIcon("nb.close.tab.icon.pressed.icon"); //NOI18N
        }
        if( null == closeTabPressedImage ) {
            String path = UIManager.getString("nb.close.tab.icon.pressed.name" ); //NOI18N
            if( null != path ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabPressedImage ) {
            if( isWindows8OrAboveLaF() ) {
                closeTabPressedImage = Windows8VectorCloseButton.PRESSED;
            } else if( isWindowsVistaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_pressed.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_pressed.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_pressed.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabPressedImage = AquaVectorCloseButton.PRESSED;
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
            closeTabMouseOverImage = UIManager.getIcon("nb.close.tab.icon.rollover.icon"); //NOI18N
        }
        if( null == closeTabMouseOverImage ) {
            String path = UIManager.getString("nb.close.tab.icon.rollover.name" ); //NOI18N
            if( null != path ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == closeTabMouseOverImage ) {
            if( isWindows8OrAboveLaF() ) {
                closeTabMouseOverImage = Windows8VectorCloseButton.PRESSED;
            } else if( isWindowsVistaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_close_rollover.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_close_rollover.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_close_rollover.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabMouseOverImage = AquaVectorCloseButton.ROLLOVER;
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
            bigCloseTabImage = UIManager.getIcon("nb.bigclose.tab.icon.enabled.icon"); //NOI18N
        }
        if( null == bigCloseTabImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.enabled.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabImage ) {
            if( isWindows8OrAboveLaF() ) {
                bigCloseTabImage = Windows8VectorCloseButton.DEFAULT;
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_enabled.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_enabled.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabImage = AquaVectorCloseButton.DEFAULT;
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
            bigCloseTabPressedImage = UIManager.getIcon("nb.bigclose.tab.icon.pressed.icon"); //NOI18N
        }
        if( null == bigCloseTabPressedImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.pressed.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabPressedImage ) {
            if( isWindows8OrAboveLaF() ) {
                bigCloseTabPressedImage = Windows8VectorCloseButton.PRESSED;
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_pressed.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabPressedImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_pressed.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabPressedImage = AquaVectorCloseButton.PRESSED;
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
            bigCloseTabMouseOverImage = UIManager.getIcon("nb.bigclose.tab.icon.rollover.icon"); //NOI18N
        }
        if( null == bigCloseTabMouseOverImage ) {
            String path = UIManager.getString("nb.bigclose.tab.icon.rollover.name" ); //NOI18N
            if( null != path ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon(path, true); // NOI18N
            }
        }
        if( null == bigCloseTabMouseOverImage ) {
            if( isWindows8OrAboveLaF() ) {
                bigCloseTabMouseOverImage = Windows8VectorCloseButton.PRESSED;
            } else if( isWindowsVistaLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/vista_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsXPLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/xp_bigclose_rollover.png", true); // NOI18N
            } else if( isWindowsLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/win_bigclose_rollover.png", true); // NOI18N
            } else if( isAquaLaF() ) {
                bigCloseTabMouseOverImage = AquaVectorCloseButton.ROLLOVER;
            } else if( isGTKLaF() ) {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/gtk_bigclose_rollover.png", true); // NOI18N
            } else {
                bigCloseTabMouseOverImage = ImageUtilities.loadImageIcon("org/openide/awt/resources/metal_bigclose_rollover.png", true); // NOI18N
            }
        }
        return bigCloseTabMouseOverImage;
    }
}
