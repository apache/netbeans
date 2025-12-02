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
package org.netbeans.modules.j2ee.persistence.editor.hyperlink;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;
import org.netbeans.modules.j2ee.persistence.editor.completion.CCParser;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class NamedQueryHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        return getIdentifierSpan(doc, offset, null);
    }

    @Override
    public void performClickAction(final Document doc, final int offset, HyperlinkType type) {
        final AtomicBoolean cancel = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread( 
                () -> goToNQ(doc, offset), 
                NbBundle.getMessage(NamedQueryHyperlinkProvider.class, "LBL_GoToNamedQuery"), 
                cancel, 
                false);
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);

        if (ts == null) {
            return null;
        }

        ts.move(offset);
        if (!ts.moveNext()) {
            return null;
        }

        Token<JavaTokenId> t = ts.token();
        FileObject fo = getFileObject(doc);
        String name = t.text().toString();
        name = name.substring(name.startsWith("\"") ? 1 : 0, name.endsWith("\"") ? name.length() - 1 : name.length());
        String query = findNq(fo, name);
        if (query != null) {
            return query;
        }
        return null;
    }

    private void goToNQ(Document doc, int offset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);

        if (ts == null) {
            return;
        }

        ts.move(offset);
        if (!ts.moveNext()) {
            return;
        }

        Token<JavaTokenId> t = ts.token();
        final FileObject fo = getFileObject(doc);
        String name = t.text().toString();
        name = name.substring(name.startsWith("\"") ? 1 : 0, name.endsWith("\"") ? name.length() - 1 : name.length());
        final String nam = name;
        Project project = FileOwnerQuery.getOwner(getFileObject(doc));
        if (project == null) {
            return;
        }
        Object[] entInfo = findEntity(fo, nam);
        if (entInfo == null) {
            return;
        }
        final FileObject ent = (FileObject) entInfo[1];
        final String entClasst = (String) entInfo[0];
        
        if(ent == null) {
            return;
        }
        
        JavaSource js = JavaSource.forFileObject(ent);

        if (ent != null) {
            try {
                js.runUserActionTask( (CompilationController parameter) -> {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    AnnotationMirror foundAm = null;
                    AnnotationValue get = null;
                    Trees trees = parameter.getTrees();
                    TypeElement entityElement = parameter.getElements().getTypeElement(entClasst);
                    
                    List<? extends AnnotationMirror> annotationMirrors = entityElement.getAnnotationMirrors();
                    if (annotationMirrors != null) {
                        Iterator<? extends AnnotationMirror> iterator = annotationMirrors.iterator();
                        while (iterator.hasNext() && foundAm == null) {
                            AnnotationMirror next = iterator.next();
                            if (next.getAnnotationType().toString().equals("javax.persistence.NamedQueries")) {//NOI18N
                                
                                Map<? extends ExecutableElement, ? extends AnnotationValue> maps = next.getElementValues();
                                
                                for (AnnotationValue vl : maps.values()) {
                                    List lst = (List) vl.getValue();
                                    for (Object val : lst) {
                                        if (val instanceof AnnotationMirror) {
                                            AnnotationMirror am = (AnnotationMirror) val;
                                            if ("javax.persistence.NamedQuery".equals(am.getAnnotationType().toString())) {//NOI18N
                                                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = am.getElementValues();
                                                for (ExecutableElement el : elementValues.keySet()) {
                                                    if (el.getSimpleName().contentEquals("name")) { //NOI18N
                                                        get = elementValues.get(el);
                                                        if (get.getValue().toString().equals(nam)) {
                                                            foundAm = am;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if(foundAm != null) {
                                            break;
                                        }
                                    }
                                    if(foundAm != null) {
                                        break;
                                    }
                                }
                                
                            } else if (next.getAnnotationType().toString().equals("javax.persistence.NamedQuery")) {//NOI18N
                                if (!next.getElementValues().isEmpty()) {
                                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = next.getElementValues();
                                    for (ExecutableElement el : elementValues.keySet()) {
                                        if (el.getSimpleName().contentEquals("name")) { //NOI18N
                                            get = elementValues.get(el);
                                            if (get.getValue().toString().equals(nam)) {
                                                foundAm = next;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(foundAm != null) {
                        TreePath tree = trees.getPath(entityElement, foundAm, get);
                        int startOffset = (int) trees.getSourcePositions().getStartPosition(parameter.getCompilationUnit(), tree.getLeaf());
                        UiUtils.open(ent, startOffset );
                    }
                }, true);
                //parameter.getClasspathInfo()
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * TODO: find entities in other projects
     * @param javaFile
     * @param nqName
     * @return 
     */
    private Object[] findEntity(FileObject javaFile, String nqName) {
        Project prj = FileOwnerQuery.getOwner(javaFile);
        if (prj == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(javaFile);
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction( (EntityMappingsMetadata metadata) -> metadata.getRoot().getEntity() );
            } catch (MetadataModelException ex) {
            } catch (IOException ex) {
            }
        }
        if (entities != null) {
            for (Entity entity : entities) {
                for (NamedQuery nq : entity.getNamedQuery()) {
                    if (nqName.equals(nq.getName())) {
                        return new Object[]{entity.getClass2(), cp.findResource(entity.getClass2().replace('.', '/') + ".java")};
                    }
                }
            }
        }
        return null;
    }

    private String findNq(FileObject javaFile, String nqName) {
        Project prj = FileOwnerQuery.getOwner(javaFile);
        if (prj == null) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(javaFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        EntityClassScopeProvider provider = (EntityClassScopeProvider) prj.getLookup().lookup(EntityClassScopeProvider.class);
        EntityClassScope ecs = null;
        Entity[] entities = null;
        if (provider != null) {
            ecs = provider.findEntityClassScope(javaFile);
        }
        if (ecs != null) {
            try {
                entities = ecs.getEntityMappingsModel(false).runReadAction( (EntityMappingsMetadata metadata) -> metadata.getRoot().getEntity() );
            } catch (MetadataModelException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (entities != null) {
            for (Entity entity : entities) {
                for (NamedQuery nq : entity.getNamedQuery()) {
                    if (nqName.equals(nq.getName())) {
                        return nq.getQuery();
                    }
                }
            }
        }
        return null;
    }

    private static final Set<JavaTokenId> USABLE_TOKEN_IDS = EnumSet.of(JavaTokenId.STRING_LITERAL);

    public static int[] getIdentifierSpan(final Document doc, final int offset, Token<JavaTokenId>[] token) {
        FileObject fo = getFileObject(doc);
        if (fo == null) {
            //do nothing if FO is not attached to the document - the goto would not work anyway:
            return null;
        }
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return null;
        }

        EntityClassScopeProvider eCS = prj.getLookup().lookup(EntityClassScopeProvider.class);
        if (eCS == null) {
            return null;//no jpa support
        }

        final int[] ret = new int[]{-1, -1};
        doc.render( () -> {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(th, offset);
            
            if (ts == null) {
                return;
            }
            
            ts.move(offset);
            if (!ts.moveNext()) {
                return;
            }
            
            Token<JavaTokenId> t = ts.token();
            boolean hasMessage = false;
            if (USABLE_TOKEN_IDS.contains(t.id())) {
                for (int i = 0; i < 5; i++) {
                    if (!ts.movePrevious()) {
                        break;
                    }
                    Token<JavaTokenId> tk = ts.token();
                    if (TokenUtilities.equals(CCParser.CREATE_NAMEDQUERY, tk.text())) {//NOI18N
                        hasMessage = true;
                    }
                }
                if (hasMessage) {
                    ts.move(offset);
                    ts.moveNext();
                    ret[0] = ts.offset();
                    ret[1] = ts.offset() + t.length();
                    return;
                }
            }
        });
        return (ret[0] == -1) ? null : ret;

    }

    private static FileObject getFileObject(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);

        return od != null ? od.getPrimaryFile() : null;
    }

}
