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
package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import junit.framework.TestCase;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 *
 * @author mmetelka
 */
public class ViewUpdatesTesting {
    
    public static final Class HI_VIEW = HighlightsView.class;
    public static final Class TAB_VIEW = TabView.class;
    public static final Class NL_VIEW = NewlineView.class;
    public static final Class P_VIEW = ParagraphView.class;

    public static final Object NO_OP_TEST_VALUE = new Object();
    
    public static final String[] VIEW_BUILDER_TEST_VALUE_NAMES = {
        "rebuildCause", "createLocalViews",
        "startCreationOffset", "matchOffset", "endCreationOffset",
        "bmReuseOffset", "bmReusePView", "bmReuseLocalIndex",
        "amReuseOffset", "amReusePIndex", "amReusePView", "amReuseLocalIndex"
    };

    public static final AttributeSet[] FONT_ATTRS = {
        AttributesUtilities.createImmutable(StyleConstants.FontSize, 15),
        AttributesUtilities.createImmutable(StyleConstants.FontSize, 16),
        AttributesUtilities.createImmutable(StyleConstants.FontSize, 17)
    };

    private static int lastId;

    public static JEditorPane createPane() {
        return createPane(new PlainDocument());
    }

    public static JEditorPane createPane(final Document doc) {
        final JEditorPane[] retPane = new JEditorPane[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // Possibly init undo manager
                    UndoManager undoManager = (UndoManager) doc.getProperty(UndoManager.class);
                    if (undoManager == null) {
                        undoManager = new UndoManager();
                        doc.addUndoableEditListener(undoManager);
                        doc.putProperty(UndoManager.class, undoManager);
                    }

                    JEditorPane pane = new JEditorPane() {
                        private BufferedImage bufferedImage;

                        @Override
                        public Graphics getGraphics() {
                            return (bufferedImage != null) ? bufferedImage.getGraphics() : super.getGraphics();
                        }

                        @Override
                        public void setSize(int width, int height) {
                            super.setSize(width, height);
                            updatePaintImage(width, height);
                        }
                        
                        private void updatePaintImage(int width, int height) {
                            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        }
                    };
                    pane.putClientProperty("id", lastId++);
                    pane.setEditorKit(new DefaultEditorKit() {
                        @Override
                        public ViewFactory getViewFactory() {
                            return new ViewFactory() {

                                @Override
                                public View create(Element elem) {
                                    String elemName = elem.getName();
                                    if (elemName != null) {
                                        if (AbstractDocument.SectionElementName.equals(elemName) ||
                                            AbstractDocument.ParagraphElementName.equals(elemName)) // PlainDocument.getDefaultRootElement().getName()
                                        {
                                            return new DocumentView(elem);
                                        }
                                    }
                                    return null;
                                }
                            };
                        }
                    });
                    pane.setDocument(doc);
                    pane.setSize(200, 100); // Make BasicTextUI.getVisibleRect() return non-null Rectangle
                    try {
                        pane.modelToView(0);
                        // First modelToView() leads to pane.setFont() call by DocumentViewOp (in EDT)
                        // since currently in settings a defaultColoring.getFont().getSize() == 13
                        // while pane.getFont().getSize() == 12.
                        // This will lead to BasicTextUI.modelChanged() which produces brand new DocumentView

                        // Second modelToView() will find the font updated already
                        // so it will properly init all paragraph views.
                        pane.modelToView(0);
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException("Should not happen");
                    }
                    retPane[0] = pane;
                }
            });
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
        return retPane[0];
    }
    
    public static void setViewBounds(JTextComponent c, int startOffset, int endOffset) {
//        DocumentView docView = DocumentView.get(c);
        Position startPos = ViewUtils.createPosition(c.getDocument(), startOffset);
        Position endPos = ViewUtils.createPosition(c.getDocument(), endOffset);
        c.putClientProperty(DocumentView.START_POSITION_PROPERTY, startPos);
        c.putClientProperty(DocumentView.END_POSITION_PROPERTY, endPos);
    }
    
    public static OffsetsBag getSingleHighlightingLayerOffsets(final JEditorPane pane) {
        return (OffsetsBag) getSingleHighlightingLayerImpl(pane, null, true);
    }

    public static PositionsBag getSingleHighlightingLayer(final JEditorPane pane) {
        return (PositionsBag) getSingleHighlightingLayerImpl(pane, null, false);
    }

    public static HighlightsContainer getSingleHighlightingLayerCustom(final JEditorPane pane, HighlightsContainer container) {
        return getSingleHighlightingLayerImpl(pane, container, false);
    }

    private static HighlightsContainer getSingleHighlightingLayerImpl(
            final JEditorPane pane, HighlightsContainer container, boolean offsets)
    {
        String propName = "test-single-highlighting-container";
        HighlightsContainer tshc = (HighlightsContainer) pane.getClientProperty(propName);
        if (tshc == null) {
            final Document doc = pane.getDocument();
            final HighlightsContainer bag = (container != null)
                    ? container
                    : (offsets ? new OffsetsBag(doc) : new PositionsBag(doc));
            doc.render(new Runnable() {
                @Override
                public void run() {
                    DocumentView docView = DocumentView.get(pane);
                    if (docView != null) {
                        setTestValues(NO_OP_TEST_VALUE);
                    }
                    // Following will trigger rebuild_all in view hierarchy => skip this rebuild
                    HighlightingManager.getInstance(pane).testSetSingleContainer(bag);
                    if (docView != null) {
                        docView.op.viewsRebuildOrMarkInvalid(); // Rebiuld all views
                    }
                }
            });
            tshc = bag;
            pane.putClientProperty(propName, tshc);
        }
        return tshc;
    }
    
    private static String[] testValueNames;
    
    private static boolean testFactoryRegistered;

    public static void setTestValuesNames(String[] testValueNames) {
        ViewUpdatesTesting.testValueNames = testValueNames;
    }

    public static void setTestValues(final Object... expectedValues) {
        if (expectedValues.length == 0) {
            DocumentView.testRun = null;
            return;
        }
        if (expectedValues.length == 1 && expectedValues[0] == NO_OP_TEST_VALUE) {
            DocumentView.testRun = new Runnable() {
                @Override
                public void run() {
                    // Do nothing and do not check repeated execution
                }
            };
            return;
        }

        DocumentView.testRun = new Runnable() {
            private boolean processed;

            @Override
            public void run() {
                if (processed) {
                    throw new IllegalStateException("Test values were already processed. " + 
                            "New call to setTestValues() needed before the code that checks them gets called.");
                }
                processed = true;
                Object[] testValues = DocumentView.testValues;
                TestCase.assertEquals(expectedValues.length, testValues.length);
                for (int i = 0; i < expectedValues.length; i++) {
                    Object expectedValue = expectedValues[i];
                    Object testValue = testValues[i];
                    if ((expectedValue == null && testValue != null) ||
                            expectedValue != null && !expectedValue.equals(testValue))
                    {
                        String msg;
                        if (testValueNames == null) {
                            msg = "Null testValueNames; ";
                        } else if (testValueNames.length != expectedValues.length) {
                            msg = "testValueNames.lengh=" + testValueNames.length +
                                    " != expectedValues.lengh=" + expectedValues.length;
                        } else {
                            StringBuilder sb = new StringBuilder(200);
                            sb.append("\ntestValues[").append(i).append("]: ").
                                    append(testValueNames[i]).
                                    append(": expected: ").append(expectedValue).
                                    append(" tested: ").append(testValue).append("\n\n");
                            int maxNameLength = 0;
                            for (String name : testValueNames) {
                                maxNameLength = Math.max(maxNameLength, name.length());
                            }
                            
                            for (int j = 0; j < testValues.length; j++) {
                                String name = testValueNames[j];
                                sb.append("/*").append(name).append("*/ ");
                                for (int k = maxNameLength - name.length(); k > 0; k--) {
                                    sb.append(' ');
                                }
                                sb.append(testValues[j]);
                                if (j != testValueNames.length - 1) {
                                    sb.append(',');
                                }
                                sb.append('\n');
                            }
                            msg = sb.toString();
                        }
                        TestCase.fail(msg);
                    }
                }
            }
        };
    }
    
    public static void registerTestFactory() {
        if (!testFactoryRegistered) {
            testFactoryRegistered = true;
            EditorViewFactory.registerFactory(new TestHighlightsViewFactory.FactoryImpl());
        }
    }
    
    public static int getId(JEditorPane pane) {
        return (Integer) pane.getClientProperty("id");
    }
    
    public static UndoManager getUndoManager(Document doc) {
        return (UndoManager) doc.getProperty(UndoManager.class);
    }
    
    public static TestHighlightsViewFactory getTestFactory(JTextComponent component) {
        TestHighlightsViewFactory testFactory = TestHighlightsViewFactory.get(component);
        if (testFactory == null) {
            throw new IllegalStateException("testFactory is null. ViewUpdatesTesting.registerTestFactory() call " +
                    "at test's begining is missing likely.\ncomponent:\n" + component);
        }
        return testFactory;
    }
    
    public static List<TestHighlight> getHighlightsCopy(TestHighlightsViewFactory testFactory) {
        return new GapList<TestHighlight>(getValidHighlights(testFactory));
    }

    public static List<TestHighlight> getValidHighlights(TestHighlightsViewFactory testFactory) {
        List<TestHighlight> highlights = testFactory.getHighlights();
        if (highlights == null) {
            testFactory.setHighlights(highlights = new GapList<TestHighlight>());
        }
        return highlights;
    }
    
    public static void checkIntegrity(JTextComponent component) {
        DocumentView docView = DocumentView.get(component);
        String err = docView.findTreeIntegrityError();
        if (err != null) {
            throw new IllegalStateException("Integrity ERROR:\n" +
                    err + "\n" + docView.toStringDetailNeedsLock());
        }
    }

    /**
     * Ensure a part of view hierarchy has local views of certain length and class
     * 
     * @param startOffset
     * @param endOffset
     * @param viewLengthsAndTypes pairs of integer length of local view and its class.
     */
    public static void checkViews(DocumentView docView, int startOffset, int endOffset, Object... viewLengthsAndTypes) {
        int argCount = viewLengthsAndTypes.length;
        if ((argCount & 1) != 0) {
            throw new IllegalArgumentException("viewLengthsAndTypes.length=" + argCount + " % 2 != 0");
        }
        int origStartOffset = startOffset;
        if (startOffset == -1) {
            startOffset = docView.getStartOffset();
        }
        int origEndOffset = endOffset;
        if (endOffset == -1) {
            endOffset = docView.getEndOffset();
        }
        int argIndex = 0;
        int pIndex = docView.getViewIndex(startOffset);
        String errorMsg = null;
        StringBuilder sbCall = new StringBuilder(2048);
        if (pIndex != -1) {
            ParagraphView pView = docView.getParagraphView(pIndex);
            int index = !pView.isChildrenNull() ? pView.getViewIndex(startOffset) : 0;
            int viewCount = pView.getViewCount();
            for (int offset = startOffset; offset < endOffset;) {
                EditorView view = !pView.isChildrenNull() ? pView.getEditorView(index) : pView;
                if (errorMsg == null) {
                    if (argIndex >= argCount) {
                        errorMsg = "Unexpected view found: " + view;
                    } else if (offset != view.getStartOffset()) {
                        errorMsg = "offset=" + offset + " != viewStartOffset=" + view.getStartOffset();
                    } else if (view.getLength() != (Integer) viewLengthsAndTypes[argIndex]) {
                        errorMsg = "view.getLength()=" + view.getLength() + " != arg=" + viewLengthsAndTypes[argIndex];
                    } else if (view.getClass() != (Class) viewLengthsAndTypes[argIndex + 1]) {
                        errorMsg = "view.getClass()=" + view.getClass() + " != arg=" + viewLengthsAndTypes[argIndex + 1];
                    }
                }
                offset += view.getLength();
                index++;
                if (index >= viewCount) {
                    pIndex++;
                    if (pIndex < docView.getViewCount()) {
                        pView = docView.getParagraphView(pIndex);
                        viewCount = pView.getViewCount();
                    }
                    index = 0;
                }
                argIndex += 2;

                String viewType;
                if (view instanceof HighlightsView) {
                    viewType = "HI_VIEW";
                } else if (view instanceof TabView) {
                    viewType = "TAB_VIEW";
                } else if (view instanceof NewlineView) {
                    viewType = "NL_VIEW /* e:" + offset + " */";
                } else if (view instanceof ParagraphView) {
                    viewType = "P_VIEW /* e:" + offset + " */";
                } else {
                    viewType = "CUSTOM_VIEW";
                }
                sbCall.append("            ").append(view.getLength()).append(", ").append(viewType).append(",\n");
            }
            if (argIndex < argCount && errorMsg == null) {
                errorMsg = "checkViews() called with " + argCount + " arguments but only " + argIndex + " used.";
            }
        } else { // No pViews
            if (argCount == 0) { // No pViews => Ok
                return;
            }
            errorMsg = "No pViews but viewLengthsAndTypes.length=" + argCount + " > 0";
        }
        if (errorMsg != null) {
            if (sbCall.length() >= 2) {
                sbCall.setLength(sbCall.length() - 2); // Remove ending ",\n" and just append '\n'
                sbCall.append('\n');
            }
            TestCase.fail(errorMsg + "\n" +
//                    "    private static final Class HI_VIEW = HighlightsView.class;\n" +
//                    "    private static final Class TAB_VIEW = TabView.class;\n" +
//                    "    private static final Class NL_VIEW = NewlineView.class;\n" +
//                    "    private static final Class P_VIEW = ParagraphView.class;\n" +
                    "        ViewUpdatesTesting.checkViews(docView, " + origStartOffset +
                    ", " + origEndOffset + ",\n" + sbCall.toString() + "        );");
        }
    }
            

}
