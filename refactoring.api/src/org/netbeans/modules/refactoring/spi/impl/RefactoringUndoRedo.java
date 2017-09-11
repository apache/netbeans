/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.spi.impl;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=UndoRedo.class, path="org/netbeans/modules/refactoring")
public class RefactoringUndoRedo implements UndoRedo {
    
    private UndoManager manager = UndoManager.getDefault();
    
    private ChangeSupport pcs = new ChangeSupport(this);
    
    public RefactoringUndoRedo() {
        manager.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                pcs.fireChange();
            }

        });
    }

    @Override
    public boolean canUndo() {
        return manager.isUndoAvailable();
    }

    @Override
    public boolean canRedo() {
        return manager.isRedoAvailable();
    }

    @Override
    public void undo() throws CannotUndoException {
        manager.undo(null);
    }

    @Override
    public void redo() throws CannotRedoException {
        manager.redo(null);
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        pcs.addChangeListener(l);
        
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        pcs.removeChangeListener(l);
    }

    @Override
    public String getUndoPresentationName() {
        return manager.getUndoDescription(null);
    }

    @Override
    public String getRedoPresentationName() {
        return manager.getRedoDescription(null);
    }
    
}
