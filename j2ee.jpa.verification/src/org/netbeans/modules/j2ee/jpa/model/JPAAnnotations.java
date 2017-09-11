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

package org.netbeans.modules.j2ee.jpa.model;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Constants for annotation classes defined by the Java Persistestence API
 *
 * @author Tomasz Slota
 */
public class JPAAnnotations {
    
    //class level annotations...
    public static final String ENTITY = "javax.persistence.Entity"; //NOI18N
    public static final String MAPPED_SUPERCLASS = "javax.persistence.MappedSuperclass"; //NOI18N
    public static final String EMBEDDABLE = "javax.persistence.Embeddable"; // NOI18N
    public static final String ID_CLASS = "javax.persistence.IdClass"; //NOI18N
    public static final String SEQUENCE_GENERATOR = "javax.persistence.SequenceGenerator"; // NOI18N
    public static final String TABLE_GENERATOR = "javax.persistence.TableGenerator"; // NOI18N
    public static final String INHERITANCE = "javax.persistence.Inheritance"; // NOI18N
    public static final String DISCRIMINATOR_COLUMN = "javax.persistence.DiscriminatorColumn"; // NOI18N
    public static final String DISCRIMINATOR_VALUE = "javax.persistence.DiscriminatorValue"; // NOI18N
    public static final String TABLE = "javax.persistence.Table"; // NOI18N
    public static final String SECONDARY_TABLE = "javax.persistence.SecondaryTable"; // NOI18N
    public static final String SECONDARY_TABLES = "javax.persistence.SecondaryTables"; // NOI18N
    public static final String PK_JOIN_COLUMN = "javax.persistence.PrimaryKeyJoinColumn"; // NOI18N
    public static final String PK_JOIN_COLUMNS = "javax.persistence.PrimaryKeyJoinColumns"; // NOI18N
    public static final String ATTRIBUTE_OVERRIDE = "javax.persistence.AttributeOverride"; // NOI18N
    public static final String ATTRIBUTE_OVERRIDES = "javax.persistence.AttributeOverrides"; // NOI18N
    public static final String ASSOCIATION_OVERRIDE = "javax.persistence.AssociationOverride"; // NOI18N
    public static final String ASSOCIATION_OVERRIDES = "javax.persistence.AssociationOverrides"; // NOI18N
    public static final String NAMED_QUERY = "javax.persistence.NamedQuery"; // NOI18N
    public static final String NAMED_NATIVE_QUERY = "javax.persistence.NamedNativeQuery"; // NOI18N
    public static final String NAMED_QUERIES = "javax.persistence.NamedQueries"; // NOI18N
    public static final String NAMED_NATIVE_QUERIES = "javax.persistence.NamedNativeQueries"; // NOI18N
    public static final String ACCESS_TYPE = "javax.persistence.Access";//NOI18N
    public static final String ACCESS_TYPE_FIELD = "javax.persistence.AccessType.FIELD";//NOI18N
    public static final String ACCESS_TYPE_PROPERTY = "javax.persistence.AccessType.PROPERTY";//NOI18N
    
    // member level annotations...
    public static final String ID = "javax.persistence.Id"; //NOI18N
    public static final String EMBEDDED_ID = "javax.persistence.EmbeddedId"; //NOI18N
    public static final String EMBEDDED = "javax.persistence.Embedded"; // NOI18N
    public static final String TRANSIENT = "javax.persistence.Transient"; //NOI18N
    public static final String VERSION = "javax.persistence.Version"; //NOI18N
    public static final String BASIC = "javax.persistence.Basic"; //NOI18N
    public static final String ENUMERATED = "javax.persistence.Enumerated"; //NOI18N
    public static final String GENERATED_VALUE = "javax.persistence.GeneratedValue"; // NOI18N
    public static final String ONE_TO_ONE = "javax.persistence.OneToOne"; // NOI18N
    public static final String ONE_TO_MANY = "javax.persistence.OneToMany"; // NOI18N
    public static final String MANY_TO_ONE = "javax.persistence.ManyToOne"; // NOI18N
    public static final String MANY_TO_MANY = "javax.persistence.ManyToMany"; // NOI18N
    public static final String MAP_KEY = "javax.persistence.MapKey"; //NOI18N
    public static final String LOB = "javax.persistence.Lob"; //NOI18N
    public static final String TEMPORAL = "javax.persistence.Temporal"; //NOI18N
    public static final String COLUMN = "javax.persistence.Column"; // NOI18N
    public static final String JOIN_COLUMN = "javax.persistence.JoinColumn"; // NOI18N
    public static final String JOIN_COLUMNS = "javax.persistence.JoinColumns"; // NOI18N
    public static final String JOIN_TABLE = "javax.persistence.JoinTable"; //NOI18N
    public static final String ELEMENT_COLLECTION = "javax.persistence.ElementCollection"; //NOI18N
    
    public static final Set<String> MEMBER_LEVEL = new TreeSet<String>(Arrays.asList(
            ID, EMBEDDED_ID, EMBEDDED, TRANSIENT, VERSION, BASIC, ENUMERATED,
            GENERATED_VALUE,ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY,
            MAP_KEY, LOB, TEMPORAL, COLUMN, JOIN_COLUMN, JOIN_COLUMNS,JOIN_TABLE, ELEMENT_COLLECTION));
    
    // annotation methods...
    //nullable attribute in annotations like Column, JoinColumn etc.
    public static final String NULLABLE_ATTR = "nullable"; // NOI18N
    
    //value attribute in annotations with single attribute
    public static final String VALUE_ATTR = "value"; //NOI18N
    
    public static final String NAME_ATTR = "name"; //NOI18N
    
    
    // TODO: Add other constants here
}
