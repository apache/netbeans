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
package org.netbeans.modules.php.editor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public final class FindUsageSupport {
    private final Set<FileObject> files;
    private final ModelElement element;
    private final ElementQuery.Index index;

    public static FindUsageSupport getInstance(ElementQuery.Index index, ModelElement element) {
        return new FindUsageSupport(index, element);
    }

    private FindUsageSupport(ElementQuery.Index index, ModelElement element) {
        this.element = element;
        this.files = new LinkedHashSet<>();
        this.index = index;
    }

    public Collection<MethodElement> overridingMethods() {
        if (element instanceof MethodElement) {
            MethodElement method = (MethodElement) element;
            TypeElement type = method.getType();
            HashSet inheritedByMethods = new HashSet<>();
            for (TypeElement nextType : index.getInheritedByTypes(type)) {
                inheritedByMethods.addAll(index.getDeclaredMethods(nextType));
            }
            return ElementFilter.forName(NameKind.exact(method.getName())).filter(inheritedByMethods);
        } else if (element instanceof MethodScope) {
            MethodScope method = (MethodScope) element;
            TypeScope type = (TypeScope) method.getInScope();
            HashSet inheritedByMethods = new HashSet<>();
            for (TypeElement nextType : index.getInheritedByTypes(type)) {
                inheritedByMethods.addAll(index.getDeclaredMethods(nextType));
            }
            return ElementFilter.forName(NameKind.exact(method.getName())).filter(inheritedByMethods);
        }

        return Collections.emptyList();
    }

    public Collection<TypeElement> subclasses() {
        if (element instanceof TypeElement) {
            return index.getInheritedByTypes((TypeElement) element);
        }
        return Collections.emptySet();
    }

    public Collection<TypeElement> directSubclasses() {
        if (element instanceof TypeElement) {
            return index.getDirectInheritedByTypes((TypeElement) element);
        }
        return Collections.emptySet();
    }

    @CheckForNull
    public Collection<Occurence> occurences(FileObject fileObject) {
        final Set<Occurence> retval = new TreeSet<>(new Comparator<Occurence>() {

            @Override
            public int compare(Occurence o1, Occurence o2) {
                return o1.getOccurenceRange().compareTo(o2.getOccurenceRange());
            }
        });
        if (fileObject != null && fileObject.isValid()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(fileObject)), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Result parameter = resultIterator.getParserResult();
                        if (parameter instanceof PHPParseResult) {
                            Model model = ModelFactory.getModel((PHPParseResult) parameter);
                            ModelVisitor modelVisitor = model.getModelVisitor();
                            retval.addAll(modelVisitor.getOccurence(element));
                        }
                    }
                });
            } catch (org.netbeans.modules.parsing.spi.ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return retval;
    }

    /**
     * @return the files
     */
    public Set<FileObject> inFiles() {
        synchronized (this) {
            if (this.files.isEmpty()) {
                addFile(element.getFileObject());
                String name = element.getName();
                final PhpElementKind kind = element.getPhpElementKind();
                if (kind.equals(PhpElementKind.VARIABLE) || kind.equals(PhpElementKind.FIELD)) {
                    name = name.startsWith("$") ? name.substring(1) : name;
                } else if (kind.equals(PhpElementKind.METHOD) && MethodElement.CONSTRUCTOR_NAME.equalsIgnoreCase(name)) {
                    name = element.getInScope().getName();
                }
                for (FileObject fo : index.getLocationsForIdentifiers(name)) {
                    addFile(fo);
                }
            }
        }
        return files;
    }

    private synchronized void addFile(FileObject fileObject) {
        FileType fileType = PhpSourcePath.getFileType(fileObject);
        if (fileType != FileType.INCLUDE && fileType != FileType.INTERNAL) {
            this.files.add(fileObject);
        }
    }

    /**
     * @return the element
     */
    public ModelElement elementToFind() {
        return element;
    }
}
