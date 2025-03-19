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
package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import static com.sun.source.tree.Tree.Kind.RECORD;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.tree.JCTree;
import java.util.LinkedHashSet;
//import java.util.List;
//import com.sun.tools.javac.util.List;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toCollection;
import javax.lang.model.element.Modifier;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Utilities specific for record types.
 *
 * @author homberghp
 */
public class RecordUtils {

    public static boolean isConStructor(Tree m) {

        if (m.getKind() != Kind.METHOD) {
            return false;
        }
        MethodTree met = (MethodTree) m;
        if (met.getModifiers().getFlags().contains(Modifier.STATIC)) {
            return false;
        }
        return met.getReturnType() == null;
    }

    /**
     * Count the record components of a record.
     *
     * @param record to be counted for its components.
     * @return the count
     */
    public static int countComponents(ClassTree record) {
        return (int) record.getMembers()
                .stream()
                .filter(RecordUtils::isNormalField)
                .count();
    }

    public static List<String> componentNames(ClassTree record) {
        List<String> result = components(record).stream()
                .map(m -> ((VariableTree) m).getName().toString())
                .toList();
        return result;
    }

    public static List<Tree> components(ClassTree record) {

        List<Tree> result = record.getMembers()
                .stream()
                .filter(m -> m.getKind() == Kind.VARIABLE)
                .map(VariableTree.class::cast)
                .filter(RecordUtils::isNormalField)
                .map(Tree.class::cast)
                .toList();
        return result;
    }

    public static Set<String> parameterNames(MethodTree method) {
        Set<String> result = method.getParameters()
                .stream()
                .map(m -> ((VariableTree) m).getName().toString())
                .collect(Collectors.toSet());
        return result;
    }

    public static boolean isNormalField(Tree t) {
        return t.getKind() == Kind.VARIABLE && !((VariableTree) t).getModifiers().getFlags().contains(Modifier.STATIC);
    }

    public static boolean emptyMethod(Tree t) {
        if (t.getKind() != Kind.METHOD) {
            return false;
        }
        MethodTree mt = (MethodTree) t;
        long count = mt.getBody().getStatements().stream().filter(s -> !s.toString().contains("super();")).count();
        return count == 0l;//mt.getBody().getStatements().isEmpty();
    }

    /**
     * A member is a normal field when not a class, (or enum ...) not a method
     * and not static. For record that should be a record component then.
     *
     * @param t
     * @return true if fields of the class/record are of same amount, order and
     * types
     */
    public static boolean isRecordComponent(Tree t) {
        if (t.getKind() == Kind.CLASS) {
            return false;
        }
        if (t.getKind() == Kind.METHOD) {
            return false;
        }
        if (t.getKind() == Kind.VARIABLE && t instanceof VariableTree vt) {
            return !vt.getModifiers().getFlags().contains(Modifier.STATIC);
        }
        return false;
    }

    public static boolean hasAllParameterNames(MethodTree method, Set<String> names) {
        Set<String> actualNames = method.getParameters().stream().map(m -> m.getName().toString()).collect(Collectors.toSet());
        return names.size() == actualNames.size() && names.containsAll(actualNames);
    }

    /**
     * Get the canonical parameters of a record.
     *
     * Implementation detail: The method scans the tree and looks for a
     * constructor with a matching signature. returns 0 if not a record or no
     * constructor found.
     *
     * @param record tree presenting the record
     * @return the list of fields as declared in the record header.
     */
    public static List<JCTree.JCVariableDecl> canonicalParameters(JCTree.JCClassDecl record) {
        if (record.getKind()!= RECORD) return List.of();
        Set<String> expectedNames = record.getMembers().stream()
                .filter(m -> RecordUtils.isNormalField(m))
                .map(m -> ((JCTree.JCVariableDecl) m).getName().toString())
                .collect(Collectors.toSet());
        List<JCTree.JCVariableDecl> result = record.getMembers().stream()
                .filter(m -> RecordUtils.isConStructor(m))
                .map(JCTree.JCMethodDecl.class::cast)
                .filter(t -> RecordUtils.hasAllParameterNames(t, expectedNames))
                .flatMap(m -> ((JCTree.JCMethodDecl) m).getParameters().stream()).toList();
        return result;
    }

    /**
     * Get the canonical parameters of a record.Implementation detail: The
     * method scans the tree and looks for a constructor with a matching
     * signature.
     *
     * returns 0 if not a record or no constructor found.
     *
     * @param aRecord tree presenting the record
     * @return the list of fields as declared in the record header.
     */
    public static List<Tree> canonicalParameters(ClassTree aRecord) {
        // get the names of the RecordComponents
        Set<String> expectedNames = aRecord.getMembers().stream()
                .filter(m -> RecordUtils.isNormalField(m))
                .map(VariableTree.class::cast)
                .map(vt -> vt.getName().toString())
                .collect(toCollection(LinkedHashSet::new));

        // get the constructor that has the expected parameters.
        var result = aRecord.getMembers().stream()
                .filter(RecordUtils::isConStructor)
                .map(MethodTree.class::cast)
                .filter(mt -> hasAllParameterNames(mt, expectedNames))
                .flatMap(mt -> mt.getParameters().stream())
                .map(Tree.class::cast)
                .toList();

        if (result.isEmpty()) {
            result = components(aRecord);
        }
        return result;
    }

    /**
     * For the record get the NON component members, such as static fields, and
     * methods.
     *
     * @param aRecord to process
     * @return the list of selected members
     */
    public static @NonNull
    List<Tree> nonComponentMembers(ClassTree aRecord) {
        return aRecord.getMembers().stream()
                .filter(m -> !RecordUtils.isNormalField(m))
                .map(Tree.class::cast)
                .toList();
    }

    public static boolean isCompactConstructor(Tree rec, Tree met) {
        if (rec.getKind() != Kind.RECORD) {
            return false;
        }
        ClassTree recTree = (ClassTree) rec;
        if (met.getKind() != Kind.METHOD) {
            return false;
        }
        MethodTree methodTree= (MethodTree) met;

        var componentNames = Set.copyOf(componentNames(recTree));
        return hasAllParameterNames(methodTree, componentNames);
//        return true;
    }
}
