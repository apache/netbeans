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
