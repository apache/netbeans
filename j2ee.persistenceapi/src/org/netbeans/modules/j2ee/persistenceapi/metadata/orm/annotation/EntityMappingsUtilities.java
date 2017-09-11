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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.JoinColumn;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.PrimaryKeyJoinColumn;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsUtilities {

    /**
     * Contains the JPA ORM annotations (cf. JPA spec, section 9.1). Only contains
     * the ORM annotations that can appear <strong>in</strong> an entity,
     * that is, <code>javax.persistence.Entity</code> is left out intentionally.
     */
    private static final Set<String> ORM_ANNOTATIONS = new HashSet<String>();

    static {
        ORM_ANNOTATIONS.add("javax.persistence.AssociationOverride");      // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.AssociationOverrides");     // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.AttributeOverride");        // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.AttributeOverrides");       // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Basic");                    // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Column");                   // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.DiscriminatorColumn");      // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.DiscriminatorValue");       // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Embeddable");               // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Embedded");                 // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.EmbeddedId");               // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Enumerated");               // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.GeneratedValue");           // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Id");                       // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.IdClass");                  // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Inheritance");              // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.JoinColumn");               // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.JoinColumns");              // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.JoinTable");                // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Lob");                      // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.ManyToMany");               // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.ManyToOne");                // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.MapKey");                   // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.MappedSuperclass");         // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.OneToMany");                // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.OneToOne");                 // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.OrderBy");                  // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.PrimaryKeyJoinColumn");     // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.PrimaryKeyJoinColumns");    // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.SecondaryTable");           // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.SecondaryTables");          // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.SequenceGenerator");        // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Table");                    // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.TableGenerator");           // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Temporal");                 // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Transient");                // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.UniqueConstraint");         // NOI18N
        ORM_ANNOTATIONS.add("javax.persistence.Version");                  // NOI18N
    }

    public static boolean isTransient(Map<String, ? extends AnnotationMirror> annByType, Set<Modifier> modifiers) {
        return annByType.containsKey("javax.persistence.Transient") || modifiers.contains(Modifier.TRANSIENT); // NOI18N
    }

    public static boolean hasFieldAccess(AnnotationModelHelper helper, List<? extends Element> elements) {
        for (Element element : ElementFilter.methodsIn(elements)) {
            for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                String annTypeName = helper.getAnnotationTypeName(annotation.getAnnotationType());
                if (annTypeName != null && ORM_ANNOTATIONS.contains(annTypeName)) {
                    return false;
                }
            }
        }
        // if we got here, no methods were annotated with JPA ORM annotations
        // then either fields are annotated, or there are no annotations in the class
        // (in which case the default -- field access -- applies)
        return true;
    }

    public static String getterNameToPropertyName(String getterName) {
        // a getter name starts with "get" or "is" and
        // is longer than 3 or 2 characters, respectively
        // i.e. "get()" and "is()" are not a property getters
        if (getterName.length() > 3 && getterName.startsWith("get")) { // NOI18N
            return toLowerCaseFirst(getterName.substring(3));
        }
        if (getterName.length() > 2 && getterName.startsWith("is")) { // NOI18N
            return toLowerCaseFirst(getterName.substring(2));
        }
        return null;
    }

    public static String toUpperCase(String value) {
        // XXX locale
        return value.toUpperCase();
    }

    public static String getElementTypeName(Element element) {
        TypeMirror elementType = element.asType();
        if (TypeKind.DECLARED.equals(elementType.getKind())) {
            return ((TypeElement)((DeclaredType)elementType).asElement()).getQualifiedName().toString(); // NOI18N
        }
        return void.class.getName();
    }

    public static String getCollectionArgumentTypeName(AnnotationModelHelper helper, Element element) {
        TypeMirror elementType = element.asType();
        if (EntityMappingsUtilities.isCollectionType(helper, elementType)) {
            TypeElement argTypeElement = EntityMappingsUtilities.getFirstTypeArgument(elementType);
            if (argTypeElement != null) {
                return argTypeElement.getQualifiedName().toString(); // NOI18N
            }
        }
        return void.class.getName();
    }

    public static List<JoinColumn> getJoinColumns(final AnnotationModelHelper helper, Map<String, ? extends AnnotationMirror> annByType) {
        final List<JoinColumn> result = new ArrayList<JoinColumn>();
        AnnotationMirror joinColumnAnn = annByType.get("javax.persistence.JoinColumn"); // NOI18N
        if (joinColumnAnn != null) {
            result.add(new JoinColumnImpl(helper, joinColumnAnn));
        } else {
            AnnotationMirror joinColumnsAnnotation = annByType.get("javax.persistence.JoinColumns"); // NOI18N
            if (joinColumnsAnnotation != null) {
                AnnotationParser jcParser = AnnotationParser.create(helper);
                jcParser.expectAnnotationArray("value", helper.resolveType("javax.persistence.JoinColumn"), new ArrayValueHandler() { // NOI18N
                    public Object handleArray(List<AnnotationValue> arrayMembers) {
                        for (AnnotationValue arrayMember : arrayMembers) {
                            AnnotationMirror joinColumnAnnotation = (AnnotationMirror)arrayMember.getValue();
                            result.add(new JoinColumnImpl(helper, joinColumnAnnotation));
                        }
                        return null;
                    }
                }, null);
                jcParser.parse(joinColumnsAnnotation);
            }
        }
        return result;
    }

    public static List<PrimaryKeyJoinColumn> getPrimaryKeyJoinColumns(final AnnotationModelHelper helper, Map<String, ? extends AnnotationMirror> annByType) {
        final List<PrimaryKeyJoinColumn> result = new ArrayList<PrimaryKeyJoinColumn>();
        AnnotationMirror pkJoinColumnAnn = annByType.get("javax.persistence.PrimaryKeyJoinColumn"); // NOI18N
        if (pkJoinColumnAnn != null) {
            result.add(new PrimaryKeyJoinColumnImpl(helper, pkJoinColumnAnn));
        } else {
            AnnotationMirror pkJoinColumnsAnnotation = annByType.get("javax.persistence.PrimaryKeyJoinColumns"); // NOI18N
            if (pkJoinColumnsAnnotation != null) {
                AnnotationParser pkjcParser = AnnotationParser.create(helper);
                pkjcParser.expectAnnotationArray("value", helper.resolveType("javax.persistence.PrimaryKeyJoinColumn"), new ArrayValueHandler() { // NOI18N
                    public Object handleArray(List<AnnotationValue> arrayMembers) {
                        for (AnnotationValue arrayMember : arrayMembers) {
                            AnnotationMirror joinColumnAnnotation = (AnnotationMirror)arrayMember.getValue();
                            result.add(new PrimaryKeyJoinColumnImpl(helper, joinColumnAnnotation));
                        }
                        return null;
                    }
                }, null);
                pkjcParser.parse(pkJoinColumnsAnnotation);
            }
        }
        return result;
    }

    public static String getTemporalType(AnnotationModelHelper helper, AnnotationMirror temporalAnnotation) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectEnumConstant("value", helper.resolveType("javax.persistence.TemporalType"), null); // NOI18N
        return parser.parse(temporalAnnotation).get("value", String.class);
    }

    public static IdClassImpl getIdClass(AnnotationModelHelper helper, TypeElement typeElement) {
        AnnotationMirror idClassAnn = helper.getAnnotationsByType(typeElement.getAnnotationMirrors()).get("javax.persistence.IdClass"); // NOI18N
        if (idClassAnn == null) {
            return null;
        }
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectClass("value", null); // NOI18N
        String class2 = parser.parse(idClassAnn).get("value", String.class); // NOI18N
        return new IdClassImpl(class2);
    }

    // not private because of unit tests
    static TypeElement getFirstTypeArgument(TypeMirror type) {
        if (TypeKind.DECLARED != type.getKind()) {
            return null;
        }
        List<? extends TypeMirror> typeArgs = ((DeclaredType)type).getTypeArguments();
        if (typeArgs.size() != 1) {
            return null;
        }
        TypeMirror typeArg = typeArgs.iterator().next();
        if (TypeKind.DECLARED != typeArg.getKind()) {
            return null;
        }
        Element typeArgElement = ((DeclaredType)typeArg).asElement();
        if (ElementKind.CLASS != typeArgElement.getKind()) {
            return null;
        }
        return (TypeElement)typeArgElement;
    }

    // not private because of unit tests
    static boolean isCollectionType(AnnotationModelHelper helper, TypeMirror type) {
        return helper.isSameRawType(type, "java.util.Collection") ||
               helper.isSameRawType(type, "java.util.Set") ||
               helper.isSameRawType(type, "java.util.List") ||
               helper.isSameRawType(type, "java.util.Map");
    }

    private static String toLowerCaseFirst(String value) {
        if (value.length() > 0) {
            // XXX incorrect wrt surrogate pairs
            char[] characters = value.toCharArray();
            // XXX locale
            characters[0] = Character.toLowerCase(characters[0]);
            return new String(characters);
        }
        return value;
    }
}
