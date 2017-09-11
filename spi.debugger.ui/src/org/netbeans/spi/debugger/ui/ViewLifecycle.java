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
package org.netbeans.spi.debugger.ui;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.debugger.ui.views.ViewModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;

/**
 * Support class for a custom view based on registered view models.
 * 
 * @author Martin Entlicher
 * @since 2.34
 */
public final class ViewLifecycle {
    
    private ViewModelListener vml;
    private final CompoundModelUpdateListener cmul;

    ViewLifecycle(ViewModelListener vml, CompoundModelUpdateListener cmul) {
        this.vml = vml;
        this.cmul = cmul;
    }
    
    /**
     * Get the current compound model, that can be used to construct the custom
     * view.
     * @return The current compound model
     */
    public Models.CompoundModel getModel() {
        return cmul.getCurrentModel();
    }
    
    /**
     * Add a listener, which is called with the updated compound model.
     * @param mul The model update listener
     */
    public void addModelUpdateListener(ModelUpdateListener mul) {
        cmul.addModelUpdateListener(mul);
    }
    
    /**
     * Remove a model update listener
     * @param mul The model update listener
     */
    public void removeModelUpdateListener(ModelUpdateListener mul) {
        cmul.removeModelUpdateListener(mul);
    }

    /**
     * Destroy the underlying data, call this method when the view is closed.
     * Model updates will no longer be received after this method is called.
     */
    public void destroy() {
        vml.destroy();
    }


    /**
     * Model update listener, notified with updated compound model.
     */
    public static interface ModelUpdateListener {
        
        /**
         * Called when compound model is updated.
         * 
         * @param compoundModel The new compound model
         * @param de The associated debugger engine, whose models were used to create the
         * compound model. Can be <code>null</code>, when no active debugger engine
         * was found.
         */
        public void modelUpdated(Models.CompoundModel compoundModel, DebuggerEngine de);
        
    }
    
    static class CompoundModelUpdateListener implements ModelUpdateListener {
        
        private final List<ModelUpdateListener> muls = new LinkedList<ModelUpdateListener>();
        private CompoundModel currentCompoundModel;

        public void addModelUpdateListener(ModelUpdateListener mul) {
            synchronized(muls) {
                muls.add(mul);
            }
        }

        public void removeModelUpdateListener(ModelUpdateListener mul) {
            synchronized(muls) {
                muls.remove(mul);
            }
        }
        
        public CompoundModel getCurrentModel() {
            return currentCompoundModel;
        }

        @Override
        public void modelUpdated(CompoundModel compoundModel, DebuggerEngine de) {
            List<ModelUpdateListener> muls2;
            synchronized(muls) {
                currentCompoundModel = compoundModel;
                muls2 = new LinkedList<ModelUpdateListener>(muls);
            }
            for (ModelUpdateListener mul : muls2) {
                mul.modelUpdated(compoundModel, de);
            }
        }
        
    }
    
}
