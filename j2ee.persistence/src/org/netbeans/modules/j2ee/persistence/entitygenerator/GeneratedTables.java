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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType;
import org.openide.filesystems.FileObject;

/**
 * This interface describes the tables used to generate
 * classes and these classes. It contains a set of tables
 * and the locations of the classes generated for these tables
 * (the root folder, the package name and the class name).
 *
 * @author Andrei Badea
 */
public interface GeneratedTables {

    /**
     * Returns the catalog of the tables
     */
    public String getCatalog();
    
    /**
     * Returns the schema of the tables
     */
    public String getSchema();
    
    /**
     * Returns the names of the tables which should be used to generate classes.
     */
    public Set<String> getTableNames();

    /**
     * Returns the root folder of the class which will be generated for
     * the specified table.
     */
    public FileObject getRootFolder(String tableName);

    /**
     * Returns the package of the class which will be generated for
     * the specified table.
     */
    public String getPackageName(String tableName);

    /**
     * Returns the name of the class to be generated for the specified table.
     */
    public String getClassName(String tableName);

    /**
     * Returns the type of the update the class to be generated for the specified table.
     */
    public UpdateType getUpdateType(String tableName);

    /**
     * Returns the unique constraints defined on the table
     */
    public Set<List<String>> getUniqueConstraints(String tableName);
}
