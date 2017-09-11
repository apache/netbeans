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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author abadea
 */
public class Catalog {
    
    private final DBMetaDataProvider provider;
    private final String name;
    
    private Map/*<String, Schema>*/ schemas;
    
    Catalog(DBMetaDataProvider provider, String name) {
        this.provider = provider;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized Schema[] getSchemas() throws SQLException {
        if (schemas == null) {
            schemas = new TreeMap();
            ResultSet rs = null;
            
            if (name == null) {
                // assuming the current catalog when the catalog name is null
                rs = provider.getMetaData().getSchemas();
            } else {
                // DatabaseMetaData.getSchemas() can not be used to retrieved the
                // list of schemas in a given catalog, since it (e.g. for the JTDS
                // driver) only returns the schemas in the current catalog. The 
                // workaround is to retrieve all tables from all schemas in the given
                // catalog and obtain a schema list from that. This is not perfect, 
                // since it will not return the schemas containig neither tables nor views.
                rs = provider.getMetaData().getTables(name, "%", "%", new String[] { "TABLE", "VIEW" }); // NOI18N
            }

            try {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM"); // NOI18N
                    if(schemaName == null) schemaName = "";//handle null as empty name
                    Schema schema = new Schema(provider, this, schemaName);
                    schemas.put(schemaName, schema);
                }
            } finally {
                rs.close();
            }
        }
        
        return (Schema[])schemas.values().toArray(new Schema[schemas.size()]);
    }
    
    public synchronized Schema getSchema(String name) throws SQLException {
        if (schemas == null) {
            getSchemas();
        }
        
        return (Schema)schemas.get(name);
    }
    
    public String toString() {
        return "Catalog[name='" + name + "']"; // NOI18N
    }
}
