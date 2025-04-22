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
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import java.util.LinkedHashSet;
//import java.util.List;
//import com.sun.tools.javac.util.List;
import static com.sun.tools.javac.code.Flags.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.util.Name;
import java.util.ArrayList;
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

    public static boolean isConStructor(JCTree m) {

        if (m.getKind() != Kind.METHOD) {
            return false;
        }
        MethodTree met = (MethodTree) m;
        if (met.getModifiers().getFlags().contains(Modifier.STATIC)) {
            return false;
        }
        return met.getReturnType() == null;
    }

    public static boolean isCanonConStructor(JCTree m) {
        if (!isConStructor(m)) {
            return false;
        }
        JCTree.JCMethodDecl met = (JCTree.JCMethodDecl) m;
        if ((met.mods.flags & com.sun.tools.javac.code.Flags.RECORD) == 0) {
            return false;
        }
        return true;
    }

    /**
     * Count the record components of a record.
     *
     * @param record to be counted for its components.
     * @return the count
     */
    public static int countComponents(ClassTree record) {
        return RecordUtils.components(record).size();
    }
        

    public static List<String> componentNames(ClassTree record) {
        List<String> result = components(record).stream()
                .map(m -> ((VariableTree) m).getName().toString())
                .toList();
        return result;
    }

    public static List<JCTree.JCVariableDecl> components(ClassTree record) {

        List<JCTree.JCVariableDecl> result = record.getMembers()
                .stream()
                .filter(m -> m.getKind() == Kind.VARIABLE)
                .map(JCTree.JCVariableDecl.class::cast)
                .filter(v -> (v.mods.flags & Flags.RECORD)!=0)
//                .map(Tree.class::cast)
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
    public static boolean isRecordComponent(JCTree t) {
        return (t instanceof JCTree.JCVariableDecl vt && ((vt.mods.flags & com.sun.tools.javac.code.Flags.RECORD) != 0));
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
        if (record.getKind() != RECORD) {
            System.err.println("RU not a record");
            return List.of();
        }

        List<JCTree.JCVariableDecl> result = record.getMembers().stream()
                .filter(m -> m.getKind() == Kind.METHOD)
                .map(JCTree.JCMethodDecl.class::cast)
                .filter(met -> met.getReturnType() == null)
                .peek(ctor->System.err.println("RU flags "+FlagsMap.toString(ctor.mods.flags)+ " <ctor>"+ctor+"</ctor>"))
                .filter(ctor -> (ctor.mods.flags & com.sun.tools.javac.code.Flags.RECORD) != 0
                        || (ctor.mods.flags & com.sun.tools.javac.code.Flags.COMPACT_RECORD_CONSTRUCTOR) != 0
                        || (ctor.params.size() == RecordUtils.countComponents(record)))
                .findFirst().stream()
                .flatMap(mt -> ((JCTree.JCMethodDecl) mt).getParameters().stream())
                .collect(toCollection(ArrayList::new));
        return result;
    }

    /**
     * Get the canonical parameters of a record.Implementation detail: The
     * method scans the tree and looks for a constructor with a matching
     * signature. returns 0 if not a record or no constructor found.
     *
     * @param aRecord tree presenting the record
     * @param make to fix names
     * @return the list of fields as declared in the record header.
     */
    public static List<JCTree.JCVariableDecl> canonicalParameters(JCTree.JCClassDecl aRecord, com.sun.tools.javac.tree.TreeMaker make) {
        // get the names of the RecordComponents
        LinkedHashSet<Name> expectedNames = aRecord.getMembers().stream()
                .filter(m -> RecordUtils.isRecordComponent(m))
                .map(JCTree.JCVariableDecl.class::cast)
                .map(vt -> vt.name)
                .collect(toCollection(LinkedHashSet::new));

        // get the constructor that has the expected parameters.
        List<JCTree.JCVariableDecl> result = RecordUtils.canonicalParameters(aRecord);

        // apply possible rename, since we pick the canonical parameters from a most likely
        // synthetic (canoniocal) constructor;
        var itr = expectedNames.iterator();
        for (int i = 0; i < result.size(); i++) {
            JCTree.JCVariableDecl vt = result.get(i);
            JCTree.JCExpression initializer = vt.init;
            JCModifiers mods = vt.mods;
            JCTree.JCExpression type = vt.vartype;
            Name name = itr.next();
            if (!name.toString().equals(vt.getName().toString())) { // only replace if needed
                JCTree.JCVariableDecl newVar = make.VarDef(mods, name, type, initializer);
                result.set(i, newVar);
            }
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

    /**
     * Detect potential compact constructor. A compact constructor is a method
     * that has return-type null, and has a parameter list with all names equal
     * to the components as defined for the record rec. If the method mods has
     * the COMPACT_RECORD_CONSTRUCTOR flag set, return true always.
     *
     * @param rec the tree to inspect
     * @param met the method to consider
     * @return true if this is a candidate compact constructor, false otherwise.
     */
    public static boolean isCompactConstructor(Tree rec, Tree met) {

        if (met instanceof JCTree.JCMethodDecl m2 && 0 != (m2.mods.flags & COMPACT_RECORD_CONSTRUCTOR)) {
            return true;
        }
        if (rec.getKind() != Kind.RECORD) {
            return false;
        }
        ClassTree recTree = (ClassTree) rec;
        if (met.getKind() != Kind.METHOD) {
            return false;
        }
        MethodTree methodTree = (MethodTree) met;
        if (null != methodTree.getReturnType()) { // not a constructor
            return false;
        }
        var componentNames = Set.copyOf(componentNames(recTree));
        return hasAllParameterNames(methodTree, componentNames);
//        return true;
    }

}
