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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.api.ui.JavaWhereUsedSupport;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

@NbBundle.Messages({
    "LBL_ReferencePopup_Loading=Searching references...",
    "LBL_ReferencePopup_NoReferences=No references found",
    "LBL_ReferencePopup_ShowAll=Show All References...",
    "LBL_ReferencePopup_Error=Unable to load references"
})
final class ReferenceUsagesPopup extends JPanel implements FocusListener {

    private static final RequestProcessor WORKER = new RequestProcessor(ReferenceUsagesPopup.class);
    private static final int MAX_VISIBLE_ROWS = 12;
    private static final int DEFAULT_VISIBLE_ROWS = 8;
    private static final int POPUP_WIDTH = 560;

    private final ReferenceLoader loader;
    private final AtomicBoolean cancel = new AtomicBoolean();
    private final DefaultListModel<PopupEntry> model = new DefaultListModel<>();
    private final JList<PopupEntry> list = new JList<>(model) {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
    };

    private ReferenceUsagesPopup(TreePathHandle handle, Scope scope, String accessibleName) {
        this.loader = new ReferenceLoader(handle, scope);

        setLayout(new BorderLayout());
        setOpaque(true);
        setFocusCycleRoot(true);
        if (accessibleName != null) {
            getAccessibleContext().setAccessibleName(accessibleName);
        }

        configureList();
        setBackground(list.getBackground());
        add(createScrollPane(), BorderLayout.CENTER);
        setEntries(List.of(new MessageEntry(Bundle.LBL_ReferencePopup_Loading())));
        addFocusListener(this);
    }

    static void show(JTextComponent component, Rectangle2D hintBounds, TreePathHandle handle, Scope scope, String label) {
        PopupUtil.hidePopup();
        ReferenceUsagesPopup popup = new ReferenceUsagesPopup(handle, scope, label);
        Point location = popupLocation(component, hintBounds);
        PopupUtil.showPopup(popup, null, location.x, location.y + 1, true, (int) Math.ceil(hintBounds.getHeight()));
        popup.loadReferences();
    }

    @Override
    public void focusGained(FocusEvent e) {
        list.requestFocusInWindow();
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    private void loadReferences() {
        WORKER.post(() -> {
            List<PopupEntry> entries = loader.load(cancel);
            if (entries == null || cancel.get()) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                if (!cancel.get()) {
                    setEntries(entries);
                }
            });
        });
    }

    private void setEntries(List<PopupEntry> entries) {
        model.clear();
        for (PopupEntry entry : entries) {
            model.addElement(entry);
        }
        list.setVisibleRowCount(Math.min(Math.max(entries.size(), 1), MAX_VISIBLE_ROWS));
        selectFirstSelectable();
    }

    private void selectFirstSelectable() {
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).selectable()) {
                list.setSelectedIndex(i);
                return;
            }
        }
        list.clearSelection();
    }

    private void activateSelection() {
        PopupEntry entry = list.getSelectedValue();
        if (entry == null || !entry.selectable()) {
            return;
        }
        cancel.set(true);
        PopupUtil.hidePopup();
        entry.activate();
    }

    private void configureList() {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        list.setCellRenderer(new Renderer());
        list.setFixedCellHeight(Math.max(list.getFontMetrics(list.getFont()).getHeight() + 8, 20));
        list.setVisibleRowCount(DEFAULT_VISIBLE_ROWS);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    activateSelection();
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiersEx() == 0) {
                    activateSelection();
                    e.consume();
                }
            }
        });
    }

    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(null);
        scrollPane.setBackground(list.getBackground());
        scrollPane.getViewport().setBackground(list.getBackground());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(POPUP_WIDTH, list.getFixedCellHeight() * DEFAULT_VISIBLE_ROWS));
        return scrollPane;
    }

    private static Point popupLocation(JTextComponent component, Rectangle2D hintBounds) {
        Point location = new Point((int) Math.round(hintBounds.getX()), (int) Math.round(hintBounds.getMaxY()));
        SwingUtilities.convertPointToScreen(location, component);
        return location;
    }

    private sealed interface PopupEntry permits ReferenceEntry, ActionEntry, MessageEntry {

        String leftHtml();

        String rightText();

        String tooltip();

        boolean selectable();

        void activate();
    }

    private record ReferenceEntry(RefactoringElement element) implements PopupEntry {

        @Override
        public String leftHtml() {
            return ReferencePresentation.displayText(element);
        }

        @Override
        public String rightText() {
            return ReferencePresentation.locationText(element);
        }

        @Override
        public String tooltip() {
            return ReferencePresentation.tooltipText(element);
        }

        @Override
        public boolean selectable() {
            return true;
        }

        @Override
        public void activate() {
            element.openInEditor();
        }
    }

    private record ActionEntry(String text, Runnable action) implements PopupEntry {

        @Override
        public String leftHtml() {
            return text;
        }

        @Override
        public String rightText() {
            return null;
        }

        @Override
        public String tooltip() {
            return text;
        }

        @Override
        public boolean selectable() {
            return true;
        }

        @Override
        public void activate() {
            action.run();
        }
    }

    private record MessageEntry(String text) implements PopupEntry {

        @Override
        public String leftHtml() {
            return text;
        }

        @Override
        public String rightText() {
            return null;
        }

        @Override
        public String tooltip() {
            return text;
        }

        @Override
        public boolean selectable() {
            return false;
        }

        @Override
        public void activate() {
        }
    }

    private static final class Renderer extends DefaultListCellRenderer {

        private static final int ALTERNATE_ROW_DELTA = 6;
        private static final double MIN_CONTRAST_RATIO = 3.0d;
        private PopupEntry entry;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            entry = (PopupEntry) value;
            setToolTipText(entry.tooltip());
            Color background = isSelected ? list.getSelectionBackground() : backgroundForRow(list, index);
            Color foreground = isSelected
                    ? list.getSelectionForeground()
                    : foregroundForEntry(entry, list.getForeground(), background);
            setBackground(background);
            setForeground(foreground);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color bgColor = getBackground();
            Color fgColor = getForeground();

            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            CompletionUtilities.renderHtml(null, entry.leftHtml(), entry.rightText(), g, getFont(), fgColor, getWidth(), getHeight(), false);
        }

        private Color foregroundForEntry(PopupEntry entry, Color defaultColor, Color background) {
            if (entry instanceof MessageEntry) {
                Color disabled = UIManager.getColor("Label.disabledForeground"); // NOI18N
                return readableColor(disabled, background, defaultColor);
            }
            if (entry instanceof ActionEntry) {
                Color link = UIManager.getColor("nb.html.link.foreground"); // NOI18N
                return readableColor(link, background, defaultColor);
            }
            return defaultColor;
        }

        private Color backgroundForRow(JList<?> list, int index) {
            if ((index % 2) != 0) {
                return list.getBackground();
            }
            Color alternate = UIManager.getColor("Table.alternateRowColor"); // NOI18N
            if (alternate != null && !alternate.equals(list.getBackground())) {
                return alternate;
            }
            return shiftBrightness(list.getBackground(), isDark(list.getBackground()) ? ALTERNATE_ROW_DELTA : -ALTERNATE_ROW_DELTA);
        }

        private Color readableColor(Color candidate, Color background, Color fallback) {
            Color resolved = candidate != null ? candidate : fallback;
            if (contrastRatio(resolved, background) >= MIN_CONTRAST_RATIO) {
                return resolved;
            }
            return fallback;
        }

        private static Color shiftBrightness(Color color, int delta) {
            return new Color(
                    clamp(color.getRed() + delta),
                    clamp(color.getGreen() + delta),
                    clamp(color.getBlue() + delta));
        }

        private static int clamp(int value) {
            return Math.max(0, Math.min(255, value));
        }

        private static boolean isDark(Color color) {
            return relativeLuminance(color) < 0.5d;
        }

        private static double contrastRatio(Color first, Color second) {
            double firstLuminance = relativeLuminance(first) + 0.05d;
            double secondLuminance = relativeLuminance(second) + 0.05d;
            return Math.max(firstLuminance, secondLuminance) / Math.min(firstLuminance, secondLuminance);
        }

        private static double relativeLuminance(Color color) {
            return 0.2126d * linearize(color.getRed() / 255d)
                    + 0.7152d * linearize(color.getGreen() / 255d)
                    + 0.0722d * linearize(color.getBlue() / 255d);
        }

        private static double linearize(double component) {
            return component <= 0.03928d
                    ? component / 12.92d
                    : Math.pow((component + 0.055d) / 1.055d, 2.4d);
        }
    }

    private static final class ReferenceLoader {

        private static final Set<ElementKind> SUPPORTED_TYPE_KINDS = EnumSet.of(
                ElementKind.CLASS,
                ElementKind.INTERFACE,
                ElementKind.ENUM,
                ElementKind.RECORD,
                ElementKind.ANNOTATION_TYPE);

        private final TreePathHandle handle;
        private final Scope scope;

        ReferenceLoader(TreePathHandle handle, Scope scope) {
            this.handle = handle;
            this.scope = scope;
        }

        List<PopupEntry> load(AtomicBoolean cancel) {
            try {
                List<RefactoringElement> references = findDirectReferences(cancel);
                checkCancelled(cancel);
                return buildEntries(references);
            } catch (InterruptedIOException ex) {
                return null;
            } catch (IOException ex) {
                return List.of(
                        new MessageEntry(Bundle.LBL_ReferencePopup_Error()),
                        new ActionEntry(Bundle.LBL_ReferencePopup_ShowAll(), this::openAllReferences));
            }
        }

        private List<PopupEntry> buildEntries(List<RefactoringElement> references) {
            if (references.isEmpty()) {
                return List.of(new MessageEntry(Bundle.LBL_ReferencePopup_NoReferences()));
            }
            List<PopupEntry> entries = new ArrayList<>(references.size() + 1);
            for (RefactoringElement reference : references) {
                entries.add(new ReferenceEntry(reference));
            }
            entries.add(new ActionEntry(Bundle.LBL_ReferencePopup_ShowAll(), this::openAllReferences));
            return entries;
        }

        private void openAllReferences() {
            JavaWhereUsedSupport.openDirectReferenceResults(handle, scope);
        }

        private List<RefactoringElement> findDirectReferences(AtomicBoolean cancel) throws IOException {
            ResolvedElement resolved = resolve();
            if (resolved == null) {
                return Collections.emptyList();
            }
            WhereUsedQuery query = createQuery(resolved.kind);
            RefactoringSession session = RefactoringSession.create(resolved.displayName);
            Problem problem = query.preCheck();
            if (isFatal(problem)) {
                return Collections.emptyList();
            }
            problem = query.fastCheckParameters();
            if (isFatal(problem)) {
                return Collections.emptyList();
            }
            problem = query.checkParameters();
            if (isFatal(problem)) {
                return Collections.emptyList();
            }
            checkCancelled(cancel);
            problem = query.prepare(session);
            if (isFatal(problem)) {
                return Collections.emptyList();
            }
            checkCancelled(cancel);
            List<RefactoringElement> references = new ArrayList<>(session.getRefactoringElements());
            references.sort(ReferencePresentation.REFERENCE_ORDER);
            return references;
        }

        private WhereUsedQuery createQuery(ElementKind kind) {
            WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(handle));
            query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, false);
            if (scope != null) {
                query.getContext().add(scope);
            }
            if (kind == ElementKind.METHOD) {
                query.getContext().add(handle);
                query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, false);
                query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, false);
                query.putValue(WhereUsedQueryConstants.SEARCH_OVERLOADED, false);
                query.putValue(WhereUsedQueryConstants.FIND_DIRECT_REFERENCES, true);
                query.putValue(WhereUsedQuery.FIND_REFERENCES, true);
            } else if (SUPPORTED_TYPE_KINDS.contains(kind)) {
                query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, false);
                query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, false);
                query.putValue(WhereUsedQuery.FIND_REFERENCES, true);
            } else {
                throw new IllegalArgumentException("Unsupported where-used element kind: " + kind); // NOI18N
            }
            return query;
        }

        private ResolvedElement resolve() throws IOException {
            JavaSource source = JavaSource.forFileObject(handle.getFileObject());
            if (source == null) {
                return null;
            }
            final ResolvedElement[] resolved = new ResolvedElement[1];
            source.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    Element element = handle.resolveElement(controller);
                    if (element == null) {
                        return;
                    }
                    ElementKind kind = element.getKind();
                    if (kind == ElementKind.CONSTRUCTOR || !(kind == ElementKind.METHOD || SUPPORTED_TYPE_KINDS.contains(kind))) {
                        return;
                    }
                    String displayName = kind == ElementKind.METHOD
                            ? element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName() // NOI18N
                            : element.getSimpleName().toString();
                    resolved[0] = new ResolvedElement(kind, displayName);
                }
            }, true);
            return resolved[0];
        }

        private static boolean isFatal(Problem problem) {
            return problem != null && problem.isFatal();
        }

        private static void checkCancelled(AtomicBoolean cancel) throws InterruptedIOException {
            if (cancel != null && cancel.get()) {
                throw new InterruptedIOException("Cancelled");
            }
        }
    }

    private static final class ReferencePresentation {

        private static final Comparator<RefactoringElement> REFERENCE_ORDER = Comparator
                .comparing(ReferencePresentation::sortPath)
                .thenComparingInt(ReferencePresentation::sortLine)
                .thenComparing(RefactoringElement::getText, Comparator.nullsLast(Comparator.naturalOrder()));

        private ReferencePresentation() {
        }

        static String displayText(RefactoringElement element) {
            String text = element.getDisplayText();
            if (text == null || text.isEmpty()) {
                text = element.getText();
            }
            return stripHtmlEnvelope(text);
        }

        static String locationText(RefactoringElement element) {
            FileObject file = element.getParentFile();
            if (file == null) {
                return null;
            }
            try {
                PositionBounds bounds = element.getPosition();
                if (bounds == null) {
                    return file.getNameExt();
                }
                return file.getNameExt() + ':' + (bounds.getBegin().getLine() + 1);
            } catch (IOException ex) {
                return file.getNameExt();
            }
        }

        static String tooltipText(RefactoringElement element) {
            FileObject file = element.getParentFile();
            if (file == null) {
                return displayText(element);
            }
            String location = locationText(element);
            return location != null ? file.getPath() + " - " + location : file.getPath(); // NOI18N
        }

        private static String sortPath(RefactoringElement element) {
            FileObject file = element.getParentFile();
            return file != null ? file.getPath() : ""; // NOI18N
        }

        private static int sortLine(RefactoringElement element) {
            try {
                PositionBounds bounds = element.getPosition();
                return bounds != null ? bounds.getBegin().getLine() : Integer.MAX_VALUE;
            } catch (IOException ex) {
                return Integer.MAX_VALUE;
            }
        }

        private static String stripHtmlEnvelope(String text) {
            if (text == null) {
                return ""; // NOI18N
            }
            String result = text;
            if (result.regionMatches(true, 0, "<html>", 0, 6)) { // NOI18N
                result = result.substring(6);
            }
            if (result.regionMatches(true, Math.max(0, result.length() - 7), "</html>", 0, 7)) { // NOI18N
                result = result.substring(0, result.length() - 7);
            }
            return result;
        }
    }

    private record ResolvedElement(ElementKind kind, String displayName) {
    }
}
