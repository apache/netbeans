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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.reveng;

/**
 * Constants for Hibernate Reverse Engineering file tags and attribute names
 * 
 * @author gowri
 */
public class HibernateRevengXmlConstants {
    
    public static final String REVENG_TAG = "hibernate-reverse-engineering";
    public static final String SCHEMA_SELECTION_TAG = "schema-selection";
    public static final String TYPE_MAPPING_TAG = "type-mapping";
    public static final String TABLE_TAG = "table";
    public static final String TABLE_FILTER_TAG = "table-filter";   
    public static final String GENERATOR_TAG = "generator";    
    public static final String COLUMN_TAG = "column";   
    public static final String SQL_TYPE_TAG = "sql-type";
    
    
    public static final String MATCH_NAME_ATTRIB = "match-name"; // table name
    public static final String PACKAGE_ATTRIB = "package";
    public static final String CLASS_ATTRIB = "class";
    public static final String NAME_ATTRIB = "name";    
    public static final String COLUMN_ATTRIB = "column";  
    public static final String HIBERNATE_TYPE_ATTRIB = "hibernate-type";
}
