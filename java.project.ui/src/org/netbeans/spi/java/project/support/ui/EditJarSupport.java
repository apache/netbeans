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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
