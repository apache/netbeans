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

package org.netbeans.modules.nbform.palette;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.netbeans.spi.palette.PaletteActions;

/**
 *
 * @author sa154850
 */
public class FormPaletteActions extends PaletteActions {

    /** Creates a new instance of FormPaletteProvider */
    public FormPaletteActions() {
    }

    @Override
    public Action[] getImportActions() {

        Action[] res = new Action[3];

        res[0] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddJAR_Button") ) { // NOI18N
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSourceResolver.JarEntry.class);
                    }
                 };
        res[0].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddJAR_Button") ); // NOI18N
        
        res[1] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddLibrary_Button") ) { // NOI18N
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSourceResolver.LibraryEntry.class);
                    }
                };
        res[1].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddLibrary_Button") ); // NOI18N
        
        res[2] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddProject_Button") ) { // NOI18N
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSourceResolver.ProjectEntry.class);
                    }
                };
        res[2].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddProject_Button") ); // NOI18N
        
        return res;
    }

    @Override
    public Action[] getCustomCategoryActions(org.openide.util.Lookup category) {
        return new Action[0]; //TODO implement this
    }

    @Override
    public Action[] getCustomItemActions(org.openide.util.Lookup item) {
        return new Action[0]; //TODO implement this
    }

    @Override
    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    @Override
    public Action getPreferredAction(org.openide.util.Lookup item) {
        return null; //TODO implement this
    }
}
