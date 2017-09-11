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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.axi.impl;

import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.SchemaGeneratorFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelAccess;

/**
 * ModelAccess implementation.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ModelAccessImpl extends ModelAccess {
    
    /**
     * Creates a new instance of ModelAccessImpl
     */
    public ModelAccessImpl(AXIModel model) {
        this.model = (AXIModelImpl)model;
    }
        
    public void addUndoableEditListener(UndoableEditListener listener) {
        model.addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
        model.removeUndoableEditListener(listener);
    }
    
    public void prepareForUndoRedo() {        
    }
    
    public void finishUndoRedo() {
    }
    
    private SchemaModel getSchemaModel() {
	return model.getSchemaModel();
    }
    
    public Model.State sync() throws IOException {
        //build the ref cache and listens to referenced AXIModels
        model.buildReferenceableCache();
        
        //run the validator
        if(!model.validate()) {
            setAutoSync(true);
            return Model.State.NOT_WELL_FORMED;
        }
        
        //initialize the AXIDocument for the first time.
        //and sets auto-sync to true.
        if(!model.isAXIDocumentInitialized()) {
            model.initializeAXIDocument();
            setAutoSync(true);
            return Model.State.VALID;
        }
        
        if(!model.doSync()) {
            return Model.State.NOT_SYNCED;
        }
        
        //if everythings goes well, return a valid state.
	return Model.State.VALID;
    }   
        
    public void flush() {
        try {
            SchemaGeneratorFactory sgf = SchemaGeneratorFactory.getDefault();			
            sgf.updateSchema(model.getSchemaModel(), model.getSchemaDesignPattern());
        } catch (Exception ex) {
            throw new IllegalStateException("Exception during flush: ",ex); //NOI18N
        } finally {
            model.getPropertyChangeListener().clearEvents();
        }
    }
    
    /**
     * Returns length in milliseconds since last edit if the model source buffer 
     * is dirty, or 0 if the model source is not dirty.
     */
    private long dirtyTimeMillis = 0;
    public long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }
    
    public void setDirty() {
        dirtyTimeMillis = System.currentTimeMillis();
    }
    
    public void unsetDirty() {
        dirtyTimeMillis = 0;
    }
    
    private AXIModelImpl model;        
}
