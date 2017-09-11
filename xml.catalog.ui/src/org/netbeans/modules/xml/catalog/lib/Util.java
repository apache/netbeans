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
package org.netbeans.modules.xml.catalog.lib;

import java.beans.*;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;

/**
 * Utility methods.
 *
 * @author  Petr Kuzel
 * @author  Libor Kramolis
 * @version 0.2
 */
public class Util {


    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
    }

         

    /**
     * Should be rewritten for fallback Properties customizer
     * @return customizer of given Class     
     */
    public static Customizer getProviderCustomizer(Class clazz) {
        try {
            Class customizer =
                Introspector.getBeanInfo(clazz).getBeanDescriptor().getCustomizerClass();
            
            return (Customizer) customizer.newInstance();
            
        } catch (InstantiationException ex) {
            return null;
        } catch (IntrospectionException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }
    

    /**
     * Create new instance of given provider.
     */
    public static Object createProvider(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    // last catalog directory
    private static File lastDirectory;
    
    /**
     * Prompts user for a catalog file.
     * @param extensions takes a list of file extensions
     * @return filename or null if operation was cancelled.
     */
    public static File selectCatalogFile(final String extensions) {
        return selectFile(extensions,
                NbBundle.getMessage(Util.class, "TITLE_select_catalog"),
                NbBundle.getMessage(Util.class, "PROP_catalog_mask"));
    }
    
    /**
     * Prompts user for a file.
     * @param extensions takes a list of file extensions
     * @param dialogTitle dialog title
     * @param maskTitle title for filter mask
     * @return filename or null if operation was cancelled
     */
    public static File selectFile(final String extensions, String dialogTitle, final String maskTitle) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return maskTitle; // NOI18N
            }
        });

        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setDialogTitle(dialogTitle);
        while (chooser.showDialog(WindowManager.getDefault().getMainWindow(),
                               NbBundle.getMessage(Util.class, "PROP_select_button"))
               == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile()) {
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return f;
                }
            }

            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(Util.class, "MSG_inValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    } 
    
}
