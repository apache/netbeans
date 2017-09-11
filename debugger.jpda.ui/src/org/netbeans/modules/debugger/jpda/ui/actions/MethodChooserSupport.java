/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
