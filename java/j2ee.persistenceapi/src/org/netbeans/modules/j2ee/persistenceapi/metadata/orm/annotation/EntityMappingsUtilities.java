/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        ORM_ANNOTATIONS.add("jakarta.persistence.AssociationOverride");      // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.AssociationOverrides");     // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.AttributeOverride");        // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.AttributeOverrides");       // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Basic");                    // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Column");                   // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.DiscriminatorColumn");      // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.DiscriminatorValue");       // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Embeddable");               // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Embedded");                 // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.EmbeddedId");               // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Enumerated");               // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.GeneratedValue");           // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Id");                       // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.IdClass");                  // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Inheritance");              // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.JoinColumn");               // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.JoinColumns");              // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.JoinTable");                // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Lob");                      // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.ManyToMany");               // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.ManyToOne");                // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.MapKey");                   // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.MappedSuperclass");         // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.OneToMany");                // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.OneToOne");                 // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.OrderBy");                  // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.PrimaryKeyJoinColumn");     // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.PrimaryKeyJoinColumns");    // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.SecondaryTable");           // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.SecondaryTables");          // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.SequenceGenerator");        // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Table");                    // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.TableGenerator");           // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Temporal");                 // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Transient");                // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.UniqueConstraint");         // NOI18N
        ORM_ANNOTATIONS.add("jakarta.persistence.Version");                  // NOI18N
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
        return annByType.containsKey("jakarta.persistence.Transient")
                || annByType.containsKey("javax.persistence.Transient")
                || modifiers.contains(Modifier.TRANSIENT); // NOI18N
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
        if (TypeKind.DECLARED == elementType.getKind()) {
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
            joinColumnAnn = annByType.get("jakarta.persistence.JoinColumn"); // NOI18N
        }
        if (joinColumnAnn != null) {
            result.add(new JoinColumnImpl(helper, joinColumnAnn));
        } else {
            AnnotationMirror joinColumnsAnnotation = annByType.get("jakarta.persistence.JoinColumns"); // NOI18N
            String type = "jakarta.persistence.JoinColumn"; // NOI18N
            if (joinColumnsAnnotation == null) {
                joinColumnsAnnotation = annByType.get("javax.persistence.JoinColumns"); // NOI18N
                type = "javax.persistence.JoinColumn"; // NOI18N
            }
            if (joinColumnsAnnotation != null) {
                AnnotationParser jcParser = AnnotationParser.create(helper);
                jcParser.expectAnnotationArray("value", helper.resolveType(type), new ArrayValueHandler() { // NOI18N
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
        AnnotationMirror pkJoinColumnAnn = annByType.get("jakarta.persistence.PrimaryKeyJoinColumn"); // NOI18N
        if (pkJoinColumnAnn == null) {
            pkJoinColumnAnn = annByType.get("javax.persistence.PrimaryKeyJoinColumn"); // NOI18N
        }
        if (pkJoinColumnAnn != null) {
            result.add(new PrimaryKeyJoinColumnImpl(helper, pkJoinColumnAnn));
        } else {
            AnnotationMirror pkJoinColumnsAnnotation = annByType.get("javax.persistence.PrimaryKeyJoinColumns"); // NOI18N
            String type = "javax.persistence.PrimaryKeyJoinColumn";
            if (pkJoinColumnsAnnotation == null) {
                pkJoinColumnsAnnotation = annByType.get("jakarta.persistence.PrimaryKeyJoinColumns"); // NOI18N
                type = "jakarta.persistence.PrimaryKeyJoinColumn";
            }
            if (pkJoinColumnsAnnotation != null) {
                AnnotationParser pkjcParser = AnnotationParser.create(helper);
                pkjcParser.expectAnnotationArray("value", helper.resolveType(type), new ArrayValueHandler() { // NOI18N
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
        if(((TypeElement) temporalAnnotation.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("jakarta.")) {
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectEnumConstant("value", helper.resolveType("jakarta.persistence.TemporalType"), null); // NOI18N
            return parser.parse(temporalAnnotation).get("value", String.class);
        } else {
            AnnotationParser parser = AnnotationParser.create(helper);
            parser.expectEnumConstant("value", helper.resolveType("javax.persistence.TemporalType"), null); // NOI18N
            return parser.parse(temporalAnnotation).get("value", String.class);
        }
    }

    public static IdClassImpl getIdClass(AnnotationModelHelper helper, TypeElement typeElement) {
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror idClassAnn = annByType.get("jakarta.persistence.IdClass"); // NOI18N
        if (idClassAnn == null) {
            idClassAnn = annByType.get("javax.persistence.IdClass"); // NOI18N
        }
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
