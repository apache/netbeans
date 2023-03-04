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

package org.netbeans.spi.java.project.support.ui;

import static org.netbeans.spi.java.project.support.ui.Bundle.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * Factory for creating a dialog for editing jar/folder-based classpath dependencies 
 * for Ant based projects. Allows to add/modify/remove source and javadoc jars.
 * @author mkleint
 * 
 * @since org.netbeans.modules.java.project 1.14
 */
public final class EditJarSupport {
    
    /**
     * Show dialog that allows to edit source and javadoc jar/folder locations
     * for a given jar/folder on the classpath. Returns null when dialog was cancelled,
     * otherwise returns EditJarSupport.Item bean with new values.
     * 
     * @param helper
     * @param item - bean with currently used values for classpath jar, source and javadoc jars location
     * @return null if dialog cancelled, or the original item with modified values
     */
    @Messages("TIT_Edit_jar_reference=Edit Jar Reference")
    public static Item showEditDialog(AntProjectHelper helper, Item item) {
        EditJarPanel panel = new EditJarPanel(item, helper);
        DialogDescriptor dd = new DialogDescriptor(panel, TIT_Edit_jar_reference());
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION == ret) {
            return panel.assignValues();
        }
        return null;
    }
    
    /**
     * Simple object holding information used for passing information in and out of the 
     * <code>EditJarSupport.showEditDialog</code> method.
     * 
     */
    public static final class Item {
        private String jarFile;
        private String sourceFile;
        private String javadocFile;

        /**
         * classpath file location, can be relative or absolute path (relative to project basedir)
         */
        public String getJarFile() {
            return jarFile;
        }

        public void setJarFile(String jarFile) {
            this.jarFile = jarFile;
        }

        /**
         * source file location, can be relative or absolute path (relative to project basedir)
         */
        public String getSourceFile() {
            return sourceFile;
        }

        public void setSourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
        }

        /**
         * javadoc file location, can be relative or absolute path (relative to project basedir)
         */
        public String getJavadocFile() {
            return javadocFile;
        }

        public void setJavadocFile(String javadocFile) {
            this.javadocFile = javadocFile;
        }
    }

}
