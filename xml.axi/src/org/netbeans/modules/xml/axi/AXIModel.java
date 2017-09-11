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
package org.netbeans.modules.xml.axi;

import java.util.List;
import org.netbeans.modules.xml.axi.impl.AXIDocumentImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelBuilderQuery;
import org.netbeans.modules.xml.axi.impl.ModelAccessImpl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;

/**
 * Represents an AXI model for a schema.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIModel extends AbstractModel<AXIComponent> {

    /**
     * Creates a new instance AXIModel.
     */
    public AXIModel(ModelSource modelSource) {
        super(modelSource);
        this.factory = new AXIComponentFactory(this);
        this.root = new AXIDocumentImpl(this, getSchemaModel().getSchema());
        this.modelAccess = new ModelAccessImpl(this);
    }
    
    /**
     * Returns other AXIModels this model refers to.
     */
    public abstract List<AXIModel> getReferencedModels();
            
    /**
     * Returns the schema design pattern property.
     */	
    public abstract SchemaGenerator.Pattern getSchemaDesignPattern();
	
    /**
     * Sets the schema design pattern property.
     */	
    public abstract void setSchemaDesignPattern(SchemaGenerator.Pattern p);
    
    /**
     * Returns the corresponding SchemaModel.
     * @return Returns the corresponding SchemaModel.
     */
    public SchemaModel getSchemaModel() {
        return (SchemaModel)getModelSource().getLookup().
                lookup(SchemaModel.class);
    }
        
    /**
     * Returns the root of the AXI model.
     */
    public AXIDocument getRoot() {
        return root;
    }
    
    /**
     * Returns the component factory.
     */
    public AXIComponentFactory getComponentFactory() {
        return factory;
    }
    
    /**
     * Returns true if the underlying document is read-only, false otherwise.
     */
    public boolean isReadOnly() {
        ModelSource ms = getModelSource();
        assert(ms != null);
        if (ms.isEditable()) {
            FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
            assert(fo != null);
            return !fo.canWrite();
        }
        return true;
    }
    
    /**
     * Returns true if there exists a corresponding visible AXIComponent.
     */
    public boolean canView(SchemaComponent component) {
        AXIModelBuilderQuery factory = new AXIModelBuilderQuery((AXIModelImpl)this);
        return factory.canView(component);
    }

    /////////////////////////////////////////////////////////////////////
    ///////////////////////////// XAM methods ///////////////////////////
    /////////////////////////////////////////////////////////////////////    
    public ModelAccess getAccess() {
        return modelAccess;
    }
    
    public void addChildComponent(Component parent, Component child, int index) {
        AXIComponent axiParent = (AXIComponent)parent;
        AXIComponent axiChild = (AXIComponent)child;
        axiParent.addChildAtIndex(axiChild, index);
    }

    public void removeChildComponent(Component child) {
        AXIComponent axiChild = (AXIComponent)child;        
        AXIComponent axiParent = axiChild.getParent();
        axiParent.removeChild(axiChild);
    }
    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * Keeps a component factory.
     */
    private AXIComponentFactory factory;
    
    /**
     * ModelAccess
     */
    private ModelAccess modelAccess;
    
    /**
     * Root of the AXI tree.
     */
    private AXIDocument root;
}
