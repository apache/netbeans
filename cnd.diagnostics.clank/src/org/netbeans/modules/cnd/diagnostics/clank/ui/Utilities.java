/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.cnd.diagnostics.clank.ui;


import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.StyledDocument;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.cnd.diagnostics.clank.ui.codesnippet.CodeSnippet;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class Utilities {
    private static final Map<String, Image> ext2iconCache = new HashMap<String, Image>();
    
    public static Image getIconByFile(final String path, int type) {
        if (path == null) {
            return null;
        }
        final String file = CndPathUtilities.getBaseName(path);
        if (file == null || file.endsWith("(unknown)") || file.endsWith("\\null") || "null".equals(file)) {//NOI18N
            return null;
        }
        int lastDot = file.lastIndexOf(".");//NOI18N
        String ext = "";
        if (lastDot > 0 && lastDot < file.length()) {
            ext = file.substring(lastDot + 1);
        } else if (path.contains("/include/")) { // NOI18N
            ext = "h"; // NOI18N
        }
        Image image = ext2iconCache.get(ext);
        if (image == null && !ext.isEmpty()) {
            try {
                File tempFile = File.createTempFile("analyticsImage", "." + ext);//NOI18N
                tempFile.deleteOnExit();
                FileObject fo = FileUtil.toFileObject(tempFile);
                image = getIconByFileObject(fo, type);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return image;
    }

    /**
     * 
     * @param codeSnippet
     * @param type i.e. java.beans.BeanInfo.ICON_COLOR_16x16
     * @return 
     */
    public static Image getFileIconForCodeSnippet(CodeSnippet codeSnippet, int type) {
        Image icon = null;//codeSnippet.getDescription().getIcon();
        if (icon == null) {
            icon = getIconByFile(codeSnippet.getFilePath(), type);
            if (icon == null) {
                FileObject fileObject = codeSnippet.getFileObject();
                if (fileObject != null) {
                    icon = getIconByFileObject(fileObject, type);
                }
            }
        }
        return icon;
    }
    
 /**
     * 
     * @param fo
     * @param file
     * @param annotations
     * @param scrollToLine 1-based line number to make current after opening file,
     *          -1 means take the first annotation and make it current
     */
    public static void show(final FileObject fo,  int scrollToLine) {
       // LOG.log(Level.INFO, "opening file {0} with fo {1}\n", new Object[] {file, fo});//NOI18N
        if (fo != null) {
            DataObject dob = null;
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (dob != null) {
                Openable openable = dob.getLookup().lookup(Openable.class);
                if (openable != null) {
                    openable.open();
                }
                EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
                if (ec != null) {
                    try {
                        StyledDocument doc = ec.openDocument();
//                        if (doc != null && file != null) {
//                            doc.putProperty(UncoverAnnotationManager.SOURCE_FILE_PROPERTY_NAME, file);
//                        }
                        if (scrollToLine > 0) {
                            LineCookie lineCookie = dob.getCookie(LineCookie.class);
                            if (lineCookie != null) {
                                final Line current = lineCookie.getLineSet().getCurrent(scrollToLine - 1);
                                if (current != null) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            //PopupManager.uninstallAllPopups();
                                            current.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FRONT);
                                        }
                                    });
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
        
    
    /**
     * 
     * @param fo
     * @param type i.e. java.beans.BeanInfo.ICON_COLOR_16x16
     * @return
     */
    public static Image getIconByFileObject(FileObject fo, int type) {
        Image image = null;
        if (fo != null) {
            String ext = fo.getExt();
            DataObject dob = null;
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (dob != null) {
                Node node = dob.getNodeDelegate();
                if (node != null) {
                    image = node.getIcon(type);
                    if (ext.length() > 0 && image != null) {
                        ext2iconCache.put(ext, image);
                    }
                }
            }
        }
        return image;
    }
    
   private static final Color START_GRADIENT_COLOR;
    private static final Color END_GRADIENT_COLOR;
    private static final Color defaultBackgroundColor;
    static {
        Color bg = UIManager.getDefaults().getColor("Panel.background"); // NOI18N
        START_GRADIENT_COLOR = UIManager.getDefaults().getColor("Panel.background"); // NOI18N
        END_GRADIENT_COLOR = bg.brighter();
        defaultBackgroundColor = Color.white;
    }
    
    private static final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    
    
    
    public static Paint createGradient(int height) {
        return new GradientPaint(0, 0, START_GRADIENT_COLOR, 0, height, END_GRADIENT_COLOR);
    }
    
    public static boolean isTransparentTreeViewSupportedByLookAndFeel() {
        return !isGTK;
    }
    
    public static Color getDefaultBackground() {
        return defaultBackgroundColor;
    }
    
    private final static Preferences prefs = NbPreferences.forModule(Utilities.class);
    private static final Logger LOG = Logger.getLogger("SourceUtilities"); // NOI18N
    public static final boolean SHOW_ROOT_CAUSES = Boolean.getBoolean("analytics.show.rootcause");

    public static Preferences getPrefs() {
        return prefs;
    }       
    
    public static Logger getLogger() {
        return LOG;
    }        
}
