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

package org.netbeans.modules.hibernate.mapping;

/**
 * Constants for Hibernate mapping file tags and attribute names
 * 
 * @author Dongmei
 */
public class HibernateMappingXmlConstants {
    
    public static final String MAPPING_TAG = "hibernate-mapping";
    public static final String CLASS_TAG = "class";
    public static final String ID_TAG = "id";
    public static final String GENERATOR_TAG = "generator";
    public static final String PROPERTY_TAG = "property";
    public static final String SET_TAG = "set";
    public static final String KEY_TAG = "key";
    public static final String ONE_TO_MANY_TAG = "one-to-many";
    public static final String DISCRIMINATOR_TAG = "discriminator";
    public static final String COMPOSITE_ID_TAG = "composite-id";
    public static final String KEY_PROPERTY_TAG = "key-property";
    public static final String KEY_MANY_TO_ONE_TAG = "key-many-to-one";
    public static final String VERSION_TAG = "version";
    public static final String TIMESTAMP_TAG = "timestamp";
    public static final String MANY_TO_ONE_TAG = "many-to-one";
    public static final String ONE_TO_ONE_TAG = "one-to-one";
    public static final String COMPONENT_TAG = "component";
    public static final String SUBCLASS_TAG = "subclass";
    public static final String JOINED_SUBCLASS_TAG = "joined-subclass";
    public static final String UNION_SUBCLASS_TAG = "union-subclass";
    public static final String JOIN_TAG = "join";
    public static final String COLUMN_TAG = "column";
    public static final String IMPORT_TAG = "import";
    public static final String ANY_TAG = "any";
    public static final String MAP_TAG = "map";
    public static final String LIST_TAG = "list";
    public static final String LIST_INDEX_TAG = "list-index";
    public static final String INDEX_TAG = "index";
    public static final String MAP_KEY_TAG = "map-key";
    public static final String ELEMENT_TAG = "element";
    public static final String MANY_TO_MANY_TAG = "many-to-many";
    
    public static final String TABLE_ATTRIB = "table"; // table name
    public static final String PACKAGE_ATTRIB = "package";
    public static final String CLASS_ATTRIB = "class";
    public static final String NAME_ATTRIB = "name";
    public static final String TYPE_ATTRIB = "type";
    public static final String COLUMN_ATTRIB = "column";
    public static final String EXTENDS_ATTRIB = "extends";
    public static final String PERSISTER_ATTRIB = "persister";
    public static final String CASCADE_ATTRIB = "cascade";
    public static final String ID_TYPE_ATTRIB = "id-type";
}
