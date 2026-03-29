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
package org.netbeans.modules.java.editor.semantic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.editor.options.InlineHintsSettings;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.java.api.ui.JavaWhereUsedSupport;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

public final class ReferenceCountHintsTask extends JavaParserResultTask<Parser.Result> {

    static final String KEY_VIRTUAL_TEXT_BLOCK = "virtual-text-block"; // NOI18N
    static final String KEY_VIRTUAL_TEXT_BLOCK_ANCHOR_OFFSET = "virtual-text-block-anchor-offset"; // NOI18N
    static final String KEY_VIRTUAL_TEXT_BLOCK_TOOLTIP = "virtual-text-block-tooltip"; // NOI18N

    private static final Object KEY_BAG = new Object();
    private static final Object KEY_MANAGER = new Object();
    private static final Object KEY_INTERACTION = new Object();
    private static final int BLOCK_HINT_BOTTOM_GAP = 2;
    private static final int BLOCK_HINT_FONT_REDUCTION = 2;
    private static final Logger LOG = Logger.getLogger(ReferenceCountHintsTask.class.getName());
    private static final Set<Tree.Kind> DECLARATION_PARENTS = EnumSet.of(
            Tree.Kind.CLASS,
            Tree.Kind.INTERFACE,
            Tree.Kind.ENUM,
            Tree.Kind.RECORD,
            Tree.Kind.ANNOTATION_TYPE,
            Tree.Kind.COMPILATION_UNIT);

    ReferenceCountHintsTask() {
        super(JavaSource.Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Parser.Result result, SchedulerEvent event) {
        CompilationInfo info = CompilationInfo.get(result);
        if (info == null) {
            return;
        }
        Document doc = result.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return;
        }
        Manager manager = getManager(doc);
        if (!InlineHintsSettings.isReferenceCountEnabled()) {
            manager.clear();
            return;
        }
        if (SourceUtils.isScanInProgress()) {
            manager.defer(info);
            return;
        }
        List<Declaration> declarations = collectDeclarations(info, doc);
        manager.update(info, declarations);
    }

    @Override
    public int getPriority() {
        return 110;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

    public static OffsetsBag getBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_BAG);
        if (bag == null) {
            bag = new OffsetsBag(doc);
            doc.putProperty(KEY_BAG, bag);
        }
        return bag;
    }

    public static void install(JEditorPane pane) {
        if (pane.getClientProperty(KEY_INTERACTION) == null) {
            HintInteraction interaction = new HintInteraction(pane);
            pane.putClientProperty(KEY_INTERACTION, interaction);
            pane.addMouseListener(interaction);
            pane.addMouseMotionListener(interaction);
        }
    }

    private static Manager getManager(Document doc) {
        Manager manager = (Manager) doc.getProperty(KEY_MANAGER);
        if (manager == null) {
            manager = new Manager(doc);
            doc.putProperty(KEY_MANAGER, manager);
        }
        return manager;
    }

    private static List<Declaration> collectDeclarations(CompilationInfo info, Document doc) {
        List<Declaration> declarations = new ArrayList<>();
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitMethod(MethodTree tree, Void p) {
                Element element = info.getTrees().getElement(getCurrentPath());
                if (element != null
                        && element.getKind() == ElementKind.METHOD
                        && InlineHintsSettings.isReferenceCountMethodsEnabled()) {
                    int[] nameSpan = info.getTreeUtilities().findNameSpan(tree);
                    if (nameSpan != null) {
                        declarations.add(createDeclaration(doc, info, getCurrentPath(), nameSpan[0], element.getKind()));
                    }
                }
                return super.visitMethod(tree, p);
            }

            @Override
            public Void visitClass(ClassTree tree, Void p) {
                Element element = info.getTrees().getElement(getCurrentPath());
                TreePath parentPath = getCurrentPath().getParentPath();
                if (element != null
                        && InlineHintsSettings.isReferenceCountTypesEnabled()
                        && isSupportedType(element.getKind())
                        && tree.getSimpleName().length() > 0
                        && parentPath != null
                        && DECLARATION_PARENTS.contains(parentPath.getLeaf().getKind())) {
                    int[] nameSpan = info.getTreeUtilities().findNameSpan(tree);
                    if (nameSpan != null) {
                        declarations.add(createDeclaration(doc, info, getCurrentPath(), nameSpan[0], element.getKind()));
                    }
                }
                return super.visitClass(tree, p);
            }
        }.scan(info.getCompilationUnit(), null);
        return declarations;
    }

    private static boolean isSupportedType(ElementKind kind) {
        return kind == ElementKind.CLASS
                || kind == ElementKind.INTERFACE
                || kind == ElementKind.ENUM
                || kind == ElementKind.RECORD
                || kind == ElementKind.ANNOTATION_TYPE;
    }

    private static Declaration createDeclaration(Document doc, CompilationInfo info, TreePath path, int anchorOffset, ElementKind kind) {
        try {
            int paragraphOffset = LineDocumentUtils.getLineStart(LineDocumentUtils.asRequired(doc, LineDocument.class), anchorOffset);
            return new Declaration(
                    TreePathHandle.create(path, info),
                    doc.createPosition(paragraphOffset),
                    doc.createPosition(anchorOffset),
                    kind);
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static AttributeSet getHintAttributes(Document doc, int offset) {
        OffsetsBag bag = getBag(doc);
        int endOffset = Math.min(doc.getLength(), offset + 1);
        if (endOffset <= offset) {
            return null;
        }
        HighlightsSequence sequence = bag.getHighlights(offset, endOffset);
        while (sequence.moveNext()) {
            AttributeSet attrs = sequence.getAttributes();
            if (attrs != null && attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK) != null) {
                return attrs;
            }
        }
        return null;
    }

    private static Rectangle2D getHintBounds(JTextComponent component, int paragraphOffset, AttributeSet attrs) throws BadLocationException {
        if (attrs == null) {
            return null;
        }
        Integer anchorOffset = (Integer) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK_ANCHOR_OFFSET);
        String text = (String) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK);
        if (anchorOffset == null || text == null) {
            return null;
        }
        Shape paragraphShape = component.modelToView2D(paragraphOffset);
        Shape anchorShape = component.modelToView2D(anchorOffset);
        if (paragraphShape == null || anchorShape == null) {
            return null;
        }
        Rectangle2D paragraphBounds = paragraphShape.getBounds2D();
        Rectangle2D anchorBounds = anchorShape.getBounds2D();
        FontMetrics metrics = getHintMetrics(component);
        int height = metrics.getHeight() + BLOCK_HINT_BOTTOM_GAP;
        return new Rectangle2D.Double(anchorBounds.getX(), paragraphBounds.getY() - height, metrics.stringWidth(text), height);
    }

    private static Font getHintFont(JTextComponent component) {
        Font font = component.getFont();
        return font.deriveFont(Math.max(1f, font.getSize2D() - BLOCK_HINT_FONT_REDUCTION));
    }

    private static FontMetrics getHintMetrics(JTextComponent component) {
        return component.getFontMetrics(getHintFont(component));
    }

    private static String formatCount(int count) {
        return count == 1 ? "1 reference" : count + " references"; // NOI18N
    }

    @MimeRegistration(mimeType = "text/x-java", service = TaskFactory.class)
    public static final class Factory extends TaskFactory {
        @Override
        public java.util.Collection<? extends SchedulerTask> create(org.netbeans.modules.parsing.api.Snapshot snapshot) {
            return Collections.singletonList(new ReferenceCountHintsTask());
        }
    }

    private static final class Manager implements ClassIndexListener, Runnable {

        private static final RequestProcessor WORKER = new RequestProcessor(Manager.class);

        private final Document doc;
        private final RequestProcessor.Task task;
        private final Map<TreePathHandle, Integer> cache = new ConcurrentHashMap<>();
        private volatile List<Declaration> declarations = Collections.emptyList();
        private volatile List<HintActionData> hintActions = Collections.emptyList();
        private volatile FileObject file;
        private volatile Scope scope;
        private volatile long serial;
        private volatile ClassIndex classIndex;
        private volatile AtomicBoolean activeCancel = new AtomicBoolean();
        private volatile Future<Void> scanRetry;

        Manager(Document doc) {
            this.doc = doc;
            this.task = WORKER.create(this);
        }

        void update(CompilationInfo info, List<Declaration> declarations) {
            FileObject file = info.getFileObject();
            if (file == null) {
                clear();
                return;
            }
            registerIndexListener(info.getClasspathInfo().getClassIndex());
            Scope newScope = createScope(file);
            boolean clearCache = !sameDeclarations(this.declarations, declarations) || !sameScope(this.scope, newScope);
            this.file = file;
            this.scope = newScope;
            this.declarations = declarations;
            schedule(clearCache);
        }

        void defer(CompilationInfo info) {
            FileObject file = info.getFileObject();
            if (file == null) {
                clear();
                return;
            }
            registerIndexListener(info.getClasspathInfo().getClassIndex());
            this.file = file;
            this.scope = createScope(file);
            this.declarations = Collections.emptyList();
            this.hintActions = Collections.emptyList();
            publish(Collections.emptyList(), new OffsetsBag(doc));
            LOG.log(Level.INFO, "Reference count hints deferred for {0} while scan is in progress", file);
            scheduleAfterScan(file);
        }

        void clear() {
            serial++;
            activeCancel.set(true);
            activeCancel = new AtomicBoolean();
            Future<Void> pendingRetry = scanRetry;
            if (pendingRetry != null) {
                pendingRetry.cancel(false);
                scanRetry = null;
            }
            cache.clear();
            declarations = Collections.emptyList();
            hintActions = Collections.emptyList();
            publish(Collections.emptyList(), new OffsetsBag(doc));
        }

        private void registerIndexListener(ClassIndex newClassIndex) {
            if (classIndex == newClassIndex) {
                return;
            }
            classIndex = newClassIndex;
            newClassIndex.addClassIndexListener(WeakListeners.create(ClassIndexListener.class, this, newClassIndex));
            cache.clear();
        }

        private Scope createScope(FileObject file) {
            ClassPath sourcePath = ClassPath.getClassPath(file, ClassPath.SOURCE);
            if (sourcePath != null && sourcePath.getRoots().length > 0) {
                return Scope.create(List.of(sourcePath.getRoots()), null, null);
            }
            return Scope.create(null, null, List.of(file));
        }

        private boolean sameDeclarations(List<Declaration> previous, List<Declaration> current) {
            if (previous.size() != current.size()) {
                return false;
            }
            for (int i = 0; i < previous.size(); i++) {
                if (!previous.get(i).handle.equals(current.get(i).handle)) {
                    return false;
                }
            }
            return true;
        }

        private boolean sameScope(Scope previous, Scope current) {
            return previous != null
                    && current != null
                    && previous.getFiles().equals(current.getFiles())
                    && previous.getSourceRoots().equals(current.getSourceRoots())
                    && previous.isDependencies() == current.isDependencies();
        }

        private void schedule(boolean clearCache) {
            if (clearCache) {
                cache.clear();
            }
            serial++;
            activeCancel.set(true);
            activeCancel = new AtomicBoolean();
            Future<Void> pendingRetry = scanRetry;
            if (pendingRetry != null) {
                pendingRetry.cancel(false);
                scanRetry = null;
            }
            task.schedule(150);
        }

        private void scheduleAfterScan(FileObject file) {
            Future<Void> pendingRetry = scanRetry;
            if (pendingRetry != null && !pendingRetry.isDone()) {
                return;
            }
            JavaSource source = JavaSource.forFileObject(file);
            if (source == null) {
                schedule(false);
                return;
            }
            try {
                scanRetry = source.runWhenScanFinished(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        scanRetry = null;
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        List<Declaration> freshDeclarations = collectDeclarations(controller, doc);
                        update(controller, freshDeclarations);
                    }
                }, true);
            } catch (IOException ex) {
                scanRetry = null;
                LOG.log(Level.FINE, "Could not schedule reference count refresh after scan for {0}", file);
                LOG.log(Level.FINER, null, ex);
                schedule(false);
            }
        }

        @Override
        public void run() {
            long currentSerial = serial;
            Scope currentScope = scope;
            List<Declaration> currentDeclarations = declarations;
            if (currentScope == null || currentDeclarations.isEmpty()) {
                publish(Collections.emptyList(), new OffsetsBag(doc));
                return;
            }
            AtomicBoolean currentCancel = activeCancel;
            OffsetsBag newBag = new OffsetsBag(doc);
            List<HintActionData> actions = new ArrayList<>();
            List<String> debugCounts = new ArrayList<>();
            for (Declaration declaration : currentDeclarations) {
                if (currentCancel.get() || currentSerial != serial) {
                    return;
                }
                Integer cachedCount = cache.get(declaration.handle);
                int count;
                if (cachedCount != null) {
                    count = cachedCount;
                } else {
                    boolean cacheCount = true;
                    try {
                        count = JavaWhereUsedSupport.getDirectReferenceCount(declaration.handle, currentScope, currentCancel);
                    } catch (Exception ex) {
                        count = 0;
                        cacheCount = false;
                        if (!currentCancel.get()) {
                            LOG.log(Level.FINE, "Could not compute reference count for {0}", declaration.handle);
                            LOG.log(Level.FINER, null, ex);
                        }
                    }
                    if (currentCancel.get() || currentSerial != serial) {
                        return;
                    }
                    if (cacheCount) {
                        cache.put(declaration.handle, count);
                    }
                }
                if (count <= 0) {
                    if (debugCounts.size() < 12) {
                        debugCounts.add(declaration.handle + "=0"); // NOI18N
                    }
                    continue;
                }
                int paragraphOffset = declaration.paragraphPosition.getOffset();
                if (paragraphOffset >= doc.getLength()) {
                    continue;
                }
                int anchorOffset = declaration.anchorPosition.getOffset();
                String label = formatCount(count);
                newBag.addHighlight(paragraphOffset, Math.min(doc.getLength(), paragraphOffset + 1),
                        AttributesUtilities.createImmutable(
                                KEY_VIRTUAL_TEXT_BLOCK, label,
                                KEY_VIRTUAL_TEXT_BLOCK_ANCHOR_OFFSET, anchorOffset,
                                KEY_VIRTUAL_TEXT_BLOCK_TOOLTIP, label));
                actions.add(new HintActionData(declaration.paragraphPosition, declaration.anchorPosition, declaration.handle, currentScope));
                if (debugCounts.size() < 12) {
                    debugCounts.add(declaration.handle + "=" + count); // NOI18N
                }
            }
            if (currentSerial == serial) {
                LOG.log(Level.INFO, "Reference count hints processed for {0}: declarations={1}, published={2}, counts={3}",
                        new Object[]{file, currentDeclarations.size(), actions.size(), debugCounts});
                publish(actions, newBag);
            }
        }

        private void publish(List<HintActionData> actions, OffsetsBag newBag) {
            SwingUtilities.invokeLater(() -> {
                hintActions = actions;
                getBag(doc).setHighlights(newBag);
            });
        }

        private HintActionData findAction(int paragraphOffset) {
            for (HintActionData data : hintActions) {
                if (data.paragraphPosition.getOffset() == paragraphOffset) {
                    return data;
                }
            }
            return null;
        }

        @Override
        public void typesAdded(org.netbeans.api.java.source.TypesEvent event) {
            schedule(true);
        }

        @Override
        public void typesRemoved(org.netbeans.api.java.source.TypesEvent event) {
            schedule(true);
        }

        @Override
        public void typesChanged(org.netbeans.api.java.source.TypesEvent event) {
            schedule(true);
        }

        @Override
        public void rootsAdded(org.netbeans.api.java.source.RootsEvent event) {
            schedule(true);
        }

        @Override
        public void rootsRemoved(org.netbeans.api.java.source.RootsEvent event) {
            schedule(true);
        }
    }

    private static final class HintInteraction extends MouseAdapter {

        private final JEditorPane pane;

        HintInteraction(JEditorPane pane) {
            this.pane = pane;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            pane.setCursor(findMatch(e) != null ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR) : null);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            pane.setCursor(null);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 1 || e.isPopupTrigger()) {
                return;
            }
            HintMatch match = findMatch(e);
            if (match == null) {
                return;
            }
            e.consume();
            ReferenceUsagesPopup.show(pane, match.bounds, match.data.handle, match.data.scope, match.label);
        }

        private HintMatch findMatch(MouseEvent event) {
            Document doc = pane.getDocument();
            int offset = pane.viewToModel2D(event.getPoint());
            if (offset < 0) {
                return null;
            }
            AttributeSet attrs = getHintAttributes(doc, offset);
            if (attrs == null) {
                return null;
            }
            try {
                Rectangle2D bounds = getHintBounds(pane, offset, attrs);
                if (bounds == null || !bounds.contains(event.getPoint())) {
                    return null;
                }
                HintActionData data = getManager(doc).findAction(offset);
                if (data == null) {
                    return null;
                }
                return new HintMatch(data, bounds, (String) attrs.getAttribute(KEY_VIRTUAL_TEXT_BLOCK));
            } catch (BadLocationException ex) {
                return null;
            }
        }
    }

    private record Declaration(TreePathHandle handle, Position paragraphPosition, Position anchorPosition, ElementKind kind) {
    }

    private record HintActionData(Position paragraphPosition, Position anchorPosition, TreePathHandle handle, Scope scope) {
    }

    private record HintMatch(HintActionData data, Rectangle2D bounds, String label) {
    }
}
