/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.csl.editor.overridden;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.AbstractTaskFactory;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.core.SchedulerTaskCancelSupportImpl;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.csl.navigation.ElementScanningTask;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public final class ComputeAnnotations extends ParserResultTask<Result> {

    private static final Logger LOG = Logger.getLogger(ComputeAnnotations.class.getName());
    private final CancelSupportImplementation cancel = SchedulerTaskCancelSupportImpl.create(this);
    
    @Override
    public void run(Result result, SchedulerEvent event) {
        if (!(result instanceof ParserResult)) {
            return;
        }
        SpiSupportAccessor.getInstance().setCancelSupport(cancel);
        try {
            final FileObject file = result.getSnapshot().getSource().getFileObject();

            if (file == null) {
                return;
            }

            final StyledDocument doc = (StyledDocument) result.getSnapshot().getSource().getDocument(false);

            if (doc == null) {
                return;
            }

            final List<IsOverriddenAnnotation> annotations = new LinkedList<IsOverriddenAnnotation>();
            try {
                ParserManager.parse(Collections.singleton(result.getSnapshot().getSource()), new UserTask() {
                    public @Override void run(ResultIterator resultIterator) throws Exception {
                        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(resultIterator.getSnapshot().getMimeType());
                        if(language != null) { //check for non csl results
                            StructureScanner scanner = language.getStructure();
                            OverridingMethods om = language.getOverridingMethods();
                            if (scanner != null && om != null) {
                                Parser.Result r = resultIterator.getParserResult();
                                if (r instanceof ParserResult) {
                                    Map<ElementHandle, Collection<? extends AlternativeLocation>> overriding = new HashMap<ElementHandle, Collection<? extends AlternativeLocation>>();
                                    Map<ElementHandle, Collection<? extends AlternativeLocation>> overridden = new HashMap<ElementHandle, Collection<? extends AlternativeLocation>>();
                                    Set<ElementHandle> seen = new HashSet<ElementHandle>();
                                    Map<ElementHandle, ElementHandle> node2Parent = new HashMap<ElementHandle, ElementHandle>();

                                    List<? extends StructureItem> children = ElementScanningTask.findCachedStructure(resultIterator.getSnapshot(), r);
                                    if (children == null) {
                                        long startTime = System.currentTimeMillis();
                                        children = scanner.scan((ParserResult) r);

                                        long endTime = System.currentTimeMillis();
                                        Logger.getLogger("TIMER").log(Level.FINE, "Structure (" + language.getMimeType() + ")",
                                                new Object[]{file, endTime - startTime});
                                        //Don't cache if cancelled
                                        if (cancel.isCancelled()) {
                                            return;
                                        }
                                        ElementScanningTask.markProcessed(r, children);
                                    }
                                    List<StructureItem> todo = new LinkedList<StructureItem>(children);

                                    while (!todo.isEmpty()) {
                                        StructureItem i = todo.remove(0);
                                        if (i instanceof StructureItem.InheritedItem
                                                && ((StructureItem.InheritedItem) i).isInherited()) {
                                            continue;
                                        }

                                        todo.addAll(i.getNestedItems());

                                        for (StructureItem nested : i.getNestedItems()) {
                                            if (!node2Parent.containsKey(nested.getElementHandle())) {
                                                node2Parent.put(nested.getElementHandle(), i.getElementHandle());
                                            }
                                        }

                                        if (seen.add(i.getElementHandle())) {
                                            if (i.getElementHandle().getKind() != ElementKind.CLASS && i.getElementHandle().getKind() != ElementKind.INTERFACE) {
                                                Collection<? extends AlternativeLocation> ov = om.overrides((ParserResult) r, i.getElementHandle());

                                                if (ov != null && !ov.isEmpty()) {
                                                    overriding.put(i.getElementHandle(), ov);
                                                }
                                            }

                                            if (om.isOverriddenBySupported((ParserResult) r, i.getElementHandle())) {
                                                Collection<? extends AlternativeLocation> on = om.overriddenBy((ParserResult) r, i.getElementHandle());

                                                if (on != null && !on.isEmpty()) {
                                                    overridden.put(i.getElementHandle(), on);
                                                }
                                            }
                                        }
                                    }

                                    createAnnotations((ParserResult) r, doc, overriding, node2Parent, false, annotations);
                                    createAnnotations((ParserResult) r, doc, overridden, node2Parent, true, annotations);
                                }
                            }
                        }

                        for(Embedding e : resultIterator.getEmbeddings()) {
                            if (cancel.isCancelled()) {
                                return;
                            }
                            run(resultIterator.getResultIterator(e));
                        }
                    }
                });
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
            }

            AnnotationsHolder holder = AnnotationsHolder.get(file);

            if (holder != null) {
                holder.setNewAnnotations(annotations);
            }
//          Logger.getLogger("TIMER").log(Level.FINE, "Is Overridden Annotations", new Object[] {info.getFileObject(), end - start});
        } finally {
            SpiSupportAccessor.getInstance().removeCancelSupport(cancel);
        }

    }

    private void createAnnotations(ParserResult r, StyledDocument doc, Map<ElementHandle, Collection<? extends AlternativeLocation>> descriptions, Map<ElementHandle, ElementHandle> node2Parent, boolean overridden, List<IsOverriddenAnnotation> annotations) {
        if (descriptions != null) {
            for (Entry<ElementHandle, Collection<? extends AlternativeLocation>> e : descriptions.entrySet()) {
                OffsetRange range = e.getKey().getOffsetRange(r);

                if (range == null) {
                    //XXX: log
                    continue;
                }

                AnnotationType type;
                String dn;

                if (overridden) {
                    ElementHandle enclosing = node2Parent.get(e.getKey());
                    if ((enclosing != null && enclosing.getKind() == ElementKind.INTERFACE) || (e.getKey().getKind() == ElementKind.INTERFACE)) {
                        type = AnnotationType.HAS_IMPLEMENTATION;
                        dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_HasImplementations");
                    } else {
                        type = AnnotationType.IS_OVERRIDDEN;
                        dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_IsOverridden");
                    }
                } else {
                    StringBuilder tooltip = new StringBuilder();
                    boolean wasOverrides = false;

                    boolean newline = false;

                    for (AlternativeLocation loc : e.getValue()) {
                        if (newline) {
                            tooltip.append("\n"); //NOI18N
                        }

                        newline = true;

                        if (loc.getElement().getModifiers().contains(Modifier.ABSTRACT)) {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Implements", loc.getDisplayHtml(new GsfHtmlFormatter())));
                        } else {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Overrides", loc.getDisplayHtml(new GsfHtmlFormatter())));
                            wasOverrides = true;
                        }
                    }
                    
                    if (wasOverrides) {
                        type = AnnotationType.OVERRIDES;
                    } else {
                        type = AnnotationType.IMPLEMENTS;
                    }

                    dn = tooltip.toString();
                }

                Position pos = getPosition(doc, range.getStart());

                if (pos == null) {
                    //#179304: possibly the position is outside document bounds (i.e. <0 or >doc.getLenght())
                    continue;
                }

                List<OverrideDescription> ods = new LinkedList<OverrideDescription>();

                for (AlternativeLocation l : e.getValue()) {
                    ods.add(new OverrideDescription(l, overridden));
                }
                
                annotations.add(new IsOverriddenAnnotation(doc, pos, type, dn, ods));
            }
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {
            private Position pos;
            public void run() {
                if (offset < 0 || offset >= doc.getLength())
                    return ;

                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    Logger.getLogger(ComputeAnnotations.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        Impl i = new Impl();

        doc.render(i);

        return i.pos;
    }

    public static final class FactoryImpl extends AbstractTaskFactory {

        public FactoryImpl() {
            super(true);
        }

        @Override
        protected Collection<? extends SchedulerTask> createTasks(Language language, Snapshot snapshot) {
            return Collections.singleton(new ComputeAnnotations());
        }
        
    }
}
