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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.node;

import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.SetDefaultDatabase;
import org.netbeans.lib.ddl.impl.SetDefaultSchema;
import org.netbeans.lib.ddl.impl.Specification;

/**
 * This class factors out interaction with the DDL package.  This allows
 * us to unit test this interaction
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class DDLHelper {
    public static void deleteTable(Specification spec, 
            String schema, String tablename) throws Exception {
        AbstractCommand cmd = spec.createCommandDropTable(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();
    }

    public static void setDefaultDatabase(Specification spec, String dbname) throws CommandNotSupportedException, DDLException {
        SetDefaultDatabase cmd = spec.createSetDefaultDatabase(dbname);
        cmd.execute();
    }

    public static void setDefaultSchema(Specification spec, String schemaName) throws CommandNotSupportedException, DDLException {
        SetDefaultSchema cmd = spec.createSetDefaultSchema(schemaName);
        cmd.execute();
    }

    public static void deleteIndex(Specification spec,
            String schema, String tablename, String indexname) throws CommandNotSupportedException, DDLException
    {
        DropIndex cmd = spec.createCommandDropIndex(indexname);
        cmd.setTableName(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
    
    public static void deleteView(Specification spec,
            String schema, String viewname) throws CommandNotSupportedException, DDLException {
        AbstractCommand cmd = spec.createCommandDropView(viewname);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
}
