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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.netbeans.modules.dbschema.DBElementProvider;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.dbschema.nodes.SchemaRootChildren;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;

@MIMEResolver.ExtensionRegistration(
    displayName="org.netbeans.modules.dbschema.resources.Bundle#DBSchemaResolver",
    extension="dbschema",
    mimeType="text/x-dbschema+xml",
    position=1010
)
public class DBschemaDataObject extends MultiDataObject {
  
    protected transient SchemaElement schemaElement;
    transient SchemaElementImpl schemaElementImpl;

    public DBschemaDataObject (FileObject pf, DBschemaDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init ();
    }
  
    private void init () {
        CookieSet cookies = getCookieSet ();
        
		cookies.add(new DBElementProvider());
        
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("valid")) //NOI18N
                    if (! isValid())
                        if (schemaElement == null) {
                            schemaElement = SchemaElementUtil.forName(getPrimaryFile());
                            if (schemaElement != null) {
                                SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                                try {
                                    SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().toURL().toString()); //NOI18N
                                } catch (Exception exc) {
                                    if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                        exc.printStackTrace();
                                }
                                schemaElement = null;
                            }
                            return;
                        } else {
                            SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                            try {
                                SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().toURL().toString()); //NOI18N
                            } catch (Exception exc) {
                                if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                    exc.printStackTrace();
                            }
                            schemaElement = null;
                            return;
                        }

                if (event.getPropertyName().equals("primaryFile")) //NOI18N
                    if (schemaElement == null)
                        return;
                    else {
                        SchemaElement.removeFromCache(schemaElement.getName().getFullName());
                        try {
                            SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().toURL().toString()); //NOI18N
                        } catch (Exception exc) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                                exc.printStackTrace();
                        }
                        schemaElement = null;
                        getSchema();
                        return;
                    }
            }
        };

        addPropertyChangeListener(listener);
    }

    public SchemaElement getSchema() {
        if (schemaElement == null)
            setSchema(SchemaElementUtil.forName(getPrimaryFile()));

        return schemaElement;
    }
  
      public void setSchema(SchemaElement schema) {
        schemaElement = schema;
        if (isValid()) {
            Node n = getNodeDelegate();
            Children ch = n.getChildren();
            ((SchemaRootChildren) ch).setElement(schemaElement);
        }
      }

    public SchemaElementImpl getSchemaElementImpl() {
        return schemaElementImpl;
    }

    public void setSchemaElementImpl(SchemaElementImpl schemaImpl) {
        schemaElementImpl = schemaImpl;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }
  
    protected Node createNodeDelegate () {
    	Node nodeDelegate = new DBschemaDataNode(this);
        
        return nodeDelegate;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
