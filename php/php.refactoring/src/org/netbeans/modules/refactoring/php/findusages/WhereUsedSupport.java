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
package org.netbeans.modules.refactoring.php.findusages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.FindUsageSupport;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
@ActionReferences({
    @ActionReference(id = @ActionID(category = "Refactoring", id = "org.netbeans.modules.refactoring.api.ui.WhereUsedAction"), path = "Loaders/text/x-php5/Actions", position = 1700)
})
public final class WhereUsedSupport {

    private ASTNode node;
    private FileObject fo;
    private int offset;
    private PhpElementKind kind;
    private final Set<ModelElement> declarations;
    private ModelElement modelElement;
    private Results results;
    private Set<Modifier> modifier;
    private FindUsageSupport usageSupport;
    private ElementQuery.Index idx;

    private WhereUsedSupport(ElementQuery.Index idx, Set<ModelElement> declarations, ASTNode node, FileObject fo) {
        this(idx, declarations, node.getStartOffset(), fo);
        this.node = node;
    }

    private WhereUsedSupport(ElementQuery.Index idx, Set<ModelElement> declarations, int offset, FileObject fo) {
        this.fo = fo;
        this.declarations = declarations;
        this.offset = offset;
        this.idx = idx;
        setModelElement(ModelUtils.getFirst(declarations));
        kind = this.modelElement.getPhpElementKind();
        this.results = new Results();
    }

    void setModelElement(ModelElement modelElement) {
        this.modelElement = modelElement;
        this.usageSupport = FindUsageSupport.getInstance(idx, this.modelElement);
    }

    public String getName() {
        return modelElement.getName();
    }

    public ASTNode getASTNode() {
        return node;
    }

    public FileObject getDeclarationFileObject() {
        return modelElement.getFileObject();
    }

    public int getOffset() {
        return offset;
    }

    public void clearResults() {
        if (results != null) {
            results.clear();
        }
    }

    public PhpElementKind getKind() {
        return kind;
    }

    public ElementKind getElementKind() {
        return modelElement.getPHPElement().getKind();
    }

    public PhpElementKind getPhpElementKind() {
        return modelElement.getPhpElementKind();
    }

    public Set<Modifier> getModifiers() {
        ModelElement attributeElement = getModelElement();
        return getModifiers(attributeElement);
    }

    public WhereUsedSupport.Results getResults() {
        return results;
    }

    void overridingMethods() {
        Collection<MethodElement> methods = usageSupport.overridingMethods();
        for (MethodElement meth : methods) {
            results.addEntry(meth);
        }
    }

    void collectSubclasses() {
        Collection<TypeElement> subclasses = usageSupport.subclasses();
        for (TypeElement typeElement : subclasses) {
            results.addEntry(typeElement);
        }
    }

    void collectDirectSubclasses() {
        Collection<TypeElement> subclasses = usageSupport.directSubclasses();
        for (TypeElement typeElement : subclasses) {
            results.addEntry(typeElement);
        }
    }

    void collectUsages(FileObject fileObject) {
        Collection<Occurence> occurences = usageSupport.occurences(fileObject);
        if (occurences != null) {
            for (Occurence occurence : occurences) {
                results.addEntry(fileObject, occurence);
            }
        }
    }

    private static Occurence findOccurence(final Model model, final int offset) {
        Occurence result = model.getOccurencesSupport(offset).getOccurence();
        if (result == null) {
            result = model.getOccurencesSupport(offset + "$".length()).getOccurence(); //NOI18N
        }
        return result;
    }

    public static WhereUsedSupport getInstance(final PHPParseResult info, final int offset) {
        Model model = ModelFactory.getModel(info);
        final Occurence occurence = findOccurence(model, offset);
        final Set<ModelElement> declarations = new HashSet<>();
        final Collection<? extends PhpElement> allDeclarations = occurence != null ? occurence.getAllDeclarations() : Collections.<PhpElement>emptyList();
        boolean canContinue = occurence != null && allDeclarations.size() > 0 && allDeclarations.size() < 5;
        if (canContinue && occurence != null && EnumSet.of(Occurence.Accuracy.EXACT, Occurence.Accuracy.MORE, Occurence.Accuracy.UNIQUE).contains(occurence.degreeOfAccuracy())) {
            FileObject parserFo = info.getSnapshot().getSource().getFileObject();
            for (final PhpElement declarationElement : allDeclarations) {
                try {
                    final FileObject fileObject = declarationElement.getFileObject();
                    if (fileObject != null && parserFo != fileObject) {
                        ParserManager.parse(Collections.singleton(Source.create(fileObject)), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                Result parserResult = resultIterator.getParserResult();
                                if (parserResult instanceof PHPParseResult) {
                                    Model modelForDeclaration = ModelFactory.getModel((PHPParseResult) parserResult);
                                    declarations.add(modelForDeclaration.findDeclaration(declarationElement));
                                }
                            }
                        });
                    } else {
                        declarations.add(model.findDeclaration(declarationElement));
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            final Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.getDependent(parserFo));
            FileObject fileObject = info.getSnapshot().getSource().getFileObject();
            return getInstance(declarations, indexQuery, fileObject, offset);
        }
        return null;
    }

    public static WhereUsedSupport getInstance(final Set<ModelElement> declarations, final Index indexQuery, final FileObject fileObject, final int offset) {
        final ModelElement declaration = ModelUtils.getFirst(declarations);
        return (declaration != null && declarations.size() > 0) ? new WhereUsedSupport(indexQuery, declarations, offset, fileObject) : null;
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    public List<ModelElement> getModelElements() {
        return new ArrayList<>(declarations);
    }

    Set<FileObject> getRelevantFiles() {
        ModelElement mElement = getModelElement();
        if (mElement instanceof VariableName) {
            VariableName variable = (VariableName) mElement;
            if (!variable.isGloballyVisible()) {
                return Collections.singleton(mElement.getFileObject());
            }
        } else if (mElement != null && mElement.getPhpModifiers().isPrivate() && !(mElement.getInScope() instanceof TraitScope)) {
            // NETBEANS-6087 private members of trait are used in classes
            return Collections.singleton(mElement.getFileObject());
        }

        return usageSupport.inFiles();
    }

    private Set<Modifier> getModifiers(ModelElement mElement) {
        if (modifier == null) {
            Set<Modifier> retval = Collections.emptySet();
            if (mElement != null && mElement.getInScope() instanceof TypeScope) {
                retval = EnumSet.noneOf(Modifier.class);
                if (mElement.getPhpModifiers().isPrivate()) {
                    retval.add(Modifier.PRIVATE);
                } else if (mElement.getPhpModifiers().isProtected()) {
                    retval.add(Modifier.PROTECTED);
                }
                if (mElement.getPhpModifiers().isPublic()) {
                    retval.add(Modifier.PUBLIC);
                }
                if (mElement.getPhpModifiers().isStatic()) {
                    retval.add(Modifier.STATIC);
                }
            }
           modifier = retval;
        }
        return modifier;
    }

    public static boolean isAlreadyInResults(ASTNode node, Set<ASTNode> results) {
        OffsetRange newOne = new OffsetRange(node.getStartOffset(), node.getEndOffset());
        for (Iterator<ASTNode> it = results.iterator(); it.hasNext();) {
            ASTNode aSTNode = it.next();
            OffsetRange oldOne = new OffsetRange(aSTNode.getStartOffset(), aSTNode.getEndOffset());
            if (newOne.containsInclusive(oldOne.getStart()) || oldOne.containsInclusive(newOne.getStart())) {
                return true;
            }
        }
        return false;
    }


    public final class Results {

        Collection<WhereUsedElement> elements = new TreeSet<>(new Comparator<WhereUsedElement>() {

            @Override
            public int compare(WhereUsedElement o1, WhereUsedElement o2) {
                String path1 = o1.getFile() != null ? o1.getFile().getPath() : ""; //NOI18N
                String path2 = o2.getFile() != null ? o2.getFile().getPath() : ""; //NOI18N
                int retval = path1.compareTo(path2);
                if (retval == 0) {
                    int offset1 = o1.getPosition().getBegin().getOffset();
                    int offset2 = o2.getPosition().getBegin().getOffset();
                    retval = offset1 < offset2 ? -1 : 1;
                }
                return retval;
            }
        });

        Map<FileObject, WarningFileElement> warningElements = new HashMap<>();

        private Results() {
        }

        private void clear() {
            elements.clear();
            warningElements.clear();
        }

        private void addEntry(PhpElement decl) {
            Icon icon = UiUtils.getElementIcon(WhereUsedSupport.this.getElementKind(), decl.getModifiers());
            WhereUsedElement whereUsedElement = WhereUsedElement.create(
                    decl.getName(),
                    decl.getFileObject(),
                    new OffsetRange(decl.getOffset(), decl.getOffset() + decl.getName().length()),
                    icon);
            if (whereUsedElement != null) {
                elements.add(whereUsedElement);
            }
        }

        private void addEntry(FileObject fo, Occurence occurence) {
            Collection<? extends PhpElement> allDeclarations = occurence.getAllDeclarations();
            if (allDeclarations.size() > 0) {
                PhpElement decl = allDeclarations.iterator().next();
                Icon icon = UiUtils.getElementIcon(WhereUsedSupport.this.getElementKind(), decl.getModifiers());
                WhereUsedElement wue = WhereUsedElement.create(decl.getName(), fo, occurence.getOccurenceRange(), icon);
                if (wue != null) {
                    elements.add(wue);
                } else if (!warningElements.containsKey(fo)) {
                    warningElements.put(fo, new WarningFileElement(fo));
                }
            }
        }

        public Collection<WhereUsedElement> getResultElements() {
            return Collections.unmodifiableCollection(elements);
        }

        public Collection<WarningFileElement> getWarningElements() {
            return warningElements.values();
        }
    }
}
