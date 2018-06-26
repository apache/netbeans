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

package org.netbeans.modules.j2ee.deployment.common.api;

import org.openide.filesystems.FileObject;

/**
 * This interface provides an abstraction for the original relational database
 * information used to generate an Entity container managed persistence bean.
 * Changes to cmp or cmr fields made after generation will not be reflected in
 * this interface and may invalidation the original mappings. Thus, this
 * information is intended to serve only as a hint for application server
 * mapping.
 * @author  Chris Webster
 */
public interface OriginalCMPMapping {

    /**
     * @return file reference to dbschema where table and column references
     * refer. 
     */
    FileObject getSchema();
    
    /**
     * @return ejb name. 
     */
    String getEjbName();
    
    /**
     * @return table name containing all column references.
     */
    String getTableName();
    
    /**
     * @param cmpFieldName to use for locating column mapping.
     * @return name of column which represents the cmp field in #getTable() or
     * null if this cmp field does not have a mapping (this could happen because
     * of a change to the set of cmp fields or changes to the specific cmp field).
     */
    String getFieldColumn(String cmpFieldName);
    
    /**
     * Obtain the foreign key columns used to express a 1:N relationship. This
     * method will return the mapping only from the dependant EJB. Consider a
     * an Order to LineItem relationship (Order contains many line items)
     * where Order has a cmr field called lineItems and LineItem has a cmr field
     * named order. Invoking <code>getRelationshipColumn("lineItems")</code> on
     * Order would return null; however, the same invocation on LineItem would
     * return the foreign keys which reference Order. Mapping information for
     * M:N relationships is obtain via the #getRelationshipTable(java.lang.String)
     * method.
     * @param cmrFieldName to use for locating column mapping.
     * @return name of columns representing the cmr field in #getTable() or null
     * if the field was not present during creation or is participating in a
     * many to many relationship. 
     */
    String[] getRelationshipColumn(String cmrFieldName);
    
    /** 
     * Obtain the name of the join table a cmr field is based on. Both sides
     * of the relationship will return the same table name.
     * @param cmrFieldName to use in decision.
     * @return name of join table if cmrFieldName is based on a join table, 
     * null otherwise. 
     */
    String getRelationshipJoinTable(String cmrFieldName);
}
