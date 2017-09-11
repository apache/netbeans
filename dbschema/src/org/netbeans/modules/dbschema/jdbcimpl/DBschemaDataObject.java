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
  
    transient protected SchemaElement schemaElement;
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
                                    SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
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
                                SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
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
                            SchemaElement.removeFromCache(schemaElement.getName().getFullName() + "#" + getPrimaryFile().getURL().toString()); //NOI18N
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
