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
package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ExpressionPool;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.JPDAMethodChooserUtils;
import org.netbeans.modules.debugger.jpda.actions.JPDAMethodChooserUtils.ReleaseUIListener;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.ui.MethodChooser;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;

public class MethodChooserSupport {

    private final JPDADebuggerImpl debugger;
    private final JPDAThread currentThread;
    private final String url;
    private final ReferenceType clazzRef;
    private MethodChooser chooser;
    private PropertyChangeListener chooserPropertyChangeListener;

    ArrayList<Annotation> annotations;
    private int startLine;
    private int endLine;
    private int selectedIndex = -1;
    private Operation[] operations;
    private Location[] locations;
    private ExpressionPool.Interval expressionLines;
    private boolean[] isCertainlyReachable;
    
    MethodChooserSupport(JPDADebuggerImpl debugger, String url, ReferenceType clazz, int methodLine) {
        this.debugger = debugger;
        this.currentThread = debugger.getCurrentThread();
        this.url = url;
        this.clazzRef = clazz;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public MethodChooser.Segment[] getSegments() {
        MethodChooser.Segment[] segments = new MethodChooser.Segment[operations.length];
        for (int x = 0; x < segments.length; x++) {
            int start = operations[x].getMethodStartPosition().getOffset();
            int end = operations[x].getMethodEndPosition().getOffset();
            if (isCertainlyReachable[x]) {
                segments[x] = new MethodChooser.Segment(start, end);
            } else {
                segments[x] = new UncertainSegment(start, end);
            }
        }
        return segments;
    }

    public int getSegmentsCount() {
        return operations.length;
    }

    public String getHint() {
        return NbBundle.getMessage(MethodChooserSupport.class, "MSG_RunIntoMethod_Status_Line_Help");
    }

    public KeyStroke[] getStopEvents() {
        return new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK)
        };
    }

    public KeyStroke[] getConfirmEvents() {
        return new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
        };
    }

    public MethodChooser createChooser() {
        return new MethodChooser(
                    url, getSegments(), selectedIndex,
                    getHint(), getStopEvents(), getConfirmEvents()
                );
    }

    public void tearUp(MethodChooser selector) {
        // hack - disable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.setProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips", "true"); // NOI18N
        this.chooser = selector;
        debugger.addPropertyChangeListener(chooserPropertyChangeListener);
        debugger.getThreadsCollector().addPropertyChangeListener(chooserPropertyChangeListener);
        annotateLines();
    }

    public void tearDown() {
        // hack - enable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.clearProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips"); // NOI18N
        debugger.removePropertyChangeListener(chooserPropertyChangeListener);
        debugger.getThreadsCollector().removePropertyChangeListener(chooserPropertyChangeListener);
        clearAnnotations();
    }

    public void doStepInto() {
        final int index = chooser.getSelectedIndex();
        JPDAMethodChooserUtils.doStepInto(debugger, operations[index], locations[index], expressionLines);
    }

    public boolean init() {
        JPDAMethodChooserUtils.Params params =
                JPDAMethodChooserUtils.init(debugger, currentThread, url,
                                            new ReleaseUIListener() {
                                                @Override
                                                public void releaseUI() {
                                                    chooser.releaseUI(false);
                                                }
        });
        operations = params.getOperations();
        expressionLines = params.getExpressionLines();
        locations = params.getLocations();
        startLine = params.getStartLine();
        endLine = params.getEndLine();
        isCertainlyReachable = params.getIsCertainlyReachable();
        selectedIndex = params.getSelectedIndex();
        chooserPropertyChangeListener = params.getChoosertPropertyChangeListener();
        return params.isContinuedDirectly();
    }

    private void annotateLines() {
        annotations = new ArrayList<Annotation>();
        EditorContext context = EditorContextBridge.getContext();
        JPDAThread thread = debugger.getCurrentThread();
        Operation currOp = thread.getCurrentOperation();
        int currentLine = currOp != null ? currOp.getStartPosition().getLine() : thread.getLineNumber(null);
        String annoType = currOp != null ?
            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE :
            EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
        for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
            if (lineNum != currentLine) {
                Object anno = context.annotate(url, lineNum, annoType, null);
                if (anno instanceof Annotation) {
                    annotations.add((Annotation)anno);
                }
            } // if
        } // for
    }

    private void clearAnnotations() {
        if (annotations != null) {
            for (Annotation anno : annotations) {
                anno.detach();
            }
        }
    }

    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private static class UncertainSegment extends MethodChooser.Segment {

        public UncertainSegment(int start, int end) {
            super(start, end);
        }

    }
    
}
