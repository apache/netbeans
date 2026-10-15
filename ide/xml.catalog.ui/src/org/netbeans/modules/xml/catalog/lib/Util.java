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
            return (Customizer) customizer.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | IntrospectionException ex) {
            return null;
        }
    }
    

    /**
     * Create new instance of given provider.
     */
    public static Object createProvider(Class clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
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
