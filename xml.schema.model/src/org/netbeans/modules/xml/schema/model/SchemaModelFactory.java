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

package org.netbeans.modules.xml.schema.model;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.impl.EmbeddedSchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelFactory extends AbstractModelFactory<SchemaModel> {
    
    private static final SchemaModelFactory schemaModelFactory =
        new SchemaModelFactory();
    
    private SchemaModel primitiveTypesSchema;
    
    /**
     * Hidden constructor to create singleton SchemaModelFactory
     */
    private SchemaModelFactory() {
    }
    
    public static SchemaModelFactory getDefault() {
        return schemaModelFactory;
    }
     
    public SchemaModel createEmbeddedSchemaModel(DocumentModel embeddingModel, Element schemaElement){
        return new EmbeddedSchemaModelImpl(embeddingModel, schemaElement);
    }
    
    public synchronized SchemaModel getPrimitiveTypesModel() {
        if (primitiveTypesSchema == null) {
            primitiveTypesSchema = createPrimitiveSchemaModel();
        }
        return primitiveTypesSchema;
    }
    
    private SchemaModel createPrimitiveSchemaModel() {
        javax.swing.text.Document d;
        SchemaModel m;
        try {
            InputStream in = getClass().getResourceAsStream("primitiveTypesSchema.xsd"); //NOI18N
            try {
                d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(in);
                ModelSource ms = 
                    new ModelSource(Lookups.singleton(d), false);
                m = new SchemaModelImpl(ms);
                m.sync();
            } finally {
                in.close();
            }
        } catch (BadLocationException ex) {
            throw new RuntimeException("writing into empty document failed",ex); //NOI18N
        } catch (IOException ex) {
            throw new RuntimeException("schema should be correct",ex); //NOI18N
        } 
        return m;
    } 

    /**
     * Get model from given model source.  Model source should at very least 
     * provide lookup for:
     * 1. FileObject of the model source
     * 2. DataObject represent the model
     * 3. Swing Document buffer for in-memory text of the model source
     */
    public SchemaModel getModel(ModelSource modelSource) {
        Lookup lookup = modelSource.getLookup();
        assert lookup.lookup(Document.class) != null;
        return super.getModel(modelSource);
    }
    
    protected SchemaModel createModel(ModelSource modelSource) {
        return new SchemaModelImpl(modelSource);
    }
}
