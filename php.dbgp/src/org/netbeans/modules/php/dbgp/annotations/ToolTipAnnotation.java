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
package org.netbeans.modules.php.dbgp.annotations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.EvalCommand;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.modules.php.dbgp.packets.PropertyGetCommand;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

/**
 * @author ads
 *
 */
public class ToolTipAnnotation extends Annotation implements PropertyChangeListener {
    private static final RequestProcessor RP = new RequestProcessor("Tool Tip Annotation"); //NOI18N

    @Override
    public String getAnnotationType() {
        return null; // Currently return null annotation type
    }

    @Override
    public String getShortDescription() {
        final Line.Part part = (Line.Part) getAttachedAnnotatable();
        if (part != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    evaluate(part);
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ((event.getSource() instanceof EvalCommand) || (event.getSource() instanceof PropertyGetCommand)) {
            Object newValue = event.getNewValue();
            if (newValue instanceof Property) {
                Property value = (Property) event.getNewValue();
                firePropertyChange(PROP_SHORT_DESCRIPTION, null, getFormattedValue(value));
            }
        }
    }

    private String getFormattedValue(Property value) {
        return getFormattedValue(value, 0);
    }

    private String getFormattedValue(Property value, int spaces) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(600);
        CommonTooltip.build(value, builder);
        return builder.toString();
    }

    private void evaluate(Line.Part part) {
        Line line = part.getLine();
        if (line == null) {
            return;
        }
        DataObject dataObject = DataEditorSupport.findDataObject(line);
        if (!isPhpDataObject(dataObject)) {
            return;
        }
        EditorCookie editorCookie = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            return;
        }
        final int offset = NbDocument.findLineOffset(document, part.getLine().getLineNumber()) + part.getColumn();
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        String selectedText = getSelectedText(ep, offset);
        if (selectedText != null) {
            if (isPHPIdentifier(selectedText)) {
                sendPropertyGetCommand(selectedText);
            } else if (PhpOptions.getInstance().isDebuggerWatchesAndEval()) {
                sendEvalCommand(selectedText);
            }
        } else {
            final String identifier = ep != null ? getIdentifier(document, ep, offset) : null;
            if (identifier != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        sendPropertyGetCommand(identifier);
                    }
                };
                RP.post(runnable);
            }
        }
        //TODO: review, replace the code depending on lexer.model - part I
    }

    private static String getIdentifier(final StyledDocument doc, final JEditorPane ep, final int offset) {
        String t = null;
        if (ep.getCaret() != null) { // #255228
            if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
                t = ep.getSelectedText();
            }
            if (t != null) {
                return t;
            }
        }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        try {
            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            if (col + 1 >= lineLen) {
                // do not evaluate when mouse hover behind the end of line (112662)
                return null;
            }
            t = doc.getText(lineStartOffset, lineLen);
            return getExpressionToEvaluate(t, col);
        } catch (BadLocationException e) {
            return null;
        }
    }

    static String getExpressionToEvaluate(String text, int col) {
        int identStart = col;
        boolean isInFieldDeclaration = false;
        while (identStart > 0
                && (
                    (text.charAt(identStart - 1) == ' ')
                    || isPHPIdentifier(text.charAt(identStart - 1))
                    || (text.charAt(identStart - 1) == '.')
                    || (text.charAt(identStart - 1) == '>'))) {
            identStart--;
            if (identStart > 0 && text.charAt(identStart) == '>') { // NOI18N
                if (text.charAt(identStart - 1) == '-') { // NOI18N
                    identStart--; // matched object operator ->
                } else {
                    identStart++;
                    break;
                }
            }
            if (text.charAt(identStart) == ' ') {
                String possibleAccessModifier = text.substring(0, identStart).trim();
                if (endsWithAccessModifier(possibleAccessModifier)) {
                    isInFieldDeclaration = true;
                }
                break;
            }
        }
        int identEnd = col;
        while (identEnd < text.length() && Character.isJavaIdentifierPart(text.charAt(identEnd))) {
            identEnd++;
        }
        if (identStart == identEnd) {
            return null;
        }
        String simpleExpression = text.substring(identStart, identEnd).trim();
        String result = simpleExpression;
        if (isInFieldDeclaration && simpleExpression.length() > 1) {
            result = "$this->" + simpleExpression.substring(1); //NOI18N
        }
        return result;
    }

    private static boolean endsWithAccessModifier(final String possibleAccessModifier) {
        String lowerCased = possibleAccessModifier.toLowerCase(Locale.US);
        return lowerCased.endsWith("private") || lowerCased.endsWith("protected") || lowerCased.endsWith("public") || lowerCased.endsWith("var"); //NOI18N
    }

    static boolean isPHPIdentifier(char ch) {
        return isDollarMark(ch) || Character.isJavaIdentifierPart(ch);
    }

    static boolean isPHPIdentifier(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!isPHPIdentifier(text.charAt(i))) {
                return false;
            }
        }
        return text.length() > 0;
    }

    private static boolean isDollarMark(char ch) {
        return ch == '$'; //NOI18N
    }

    private boolean isPhpDataObject(DataObject dataObject) {
        return Utils.isPhpFile(dataObject.getPrimaryFile());
    }

    private void sendEvalCommand(String str) {
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        EvalCommand command = new EvalCommand(session.getTransactionId());
        command.setData(str);
        command.addPropertyChangeListener(this);
        session.sendCommandLater(command);
    }

    private void sendPropertyGetCommand(String str) {
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        PropertyGetCommand command = new PropertyGetCommand(session.getTransactionId());
        command.setName(str);
        command.addPropertyChangeListener(this);
        session.sendCommandLater(command);
    }

    private String getSelectedText(JEditorPane pane, int offset) {
        if (pane != null
                && pane.getCaret() != null
                && pane.getSelectionStart() <= offset
                && offset <= pane.getSelectionEnd()) {
            return pane.getSelectedText();
        }
        return null;
    }

    private DebugSession getSession() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        SessionId id = (SessionId) currentEngine.lookupFirst(null, SessionId.class);
        if (id == null) {
            return null;
        }
        DebugSession session = SessionManager.getInstance().getSession(id);
        return session;
    }

    private static class CommonTooltip {
        protected static final String NEW_LINE = "\n"; // NOI18N
        protected final Property property;
        protected final String type;
        protected final String result;
        protected final int spaces;

        protected CommonTooltip(Property property, String type, String result, int spaces) {
            this.property = property;
            this.type = type;
            this.result = result;
            this.spaces = spaces;
        }

        public static void build(Property property, StringBuilder builder) {
            build(property, builder, 0);
        }

        public static void build(Property property, StringBuilder builder, int spaces) {
            String result = null;
            String type = null;
            try {
                type = property.getType();
                result = property.getStringValue();
            } catch (UnsufficientValueException e) {
                /*
                 *  Result of eval command should contain all data because we are
                 *  not able to retrieve value via property_value command.
                 *  So this should never happened. Otherwise this is a bug in XDebug.
                 */
                Logger.getLogger(ToolTipAnnotation.class.getName()).log(Level.INFO, null, e);
            }
            if ("object".equalsIgnoreCase(type)) { // NOI18N
                new ObjectTooltip(property, type, result, spaces).build(builder);
            } else if ("array".equalsIgnoreCase(type)) { // NOI18N
                new ArrayTooltip(property, type, result, spaces).build(builder);
            } else if ("null".equalsIgnoreCase(type)) { // NOI18N
                new NullTooltip(property, type, result, spaces).build(builder);
            } else if ("bool".equalsIgnoreCase(type)) { // NOI18N
                new BooleanTooltip(property, type, result, spaces).build(builder);
            } else {
                new CommonTooltip(property, type, result, spaces).build(builder);
            }
        }

        public final void build(StringBuilder builder) {
            buildType(builder);
            buildResult(builder);
            buildChildren(builder);
        }

        protected void buildType(StringBuilder builder) {
            builder.append("("); // NOI18N
            builder.append(type);
            builder.append(")"); // NOI18N
        }

        protected void buildResult(StringBuilder builder) {
            if (StringUtils.hasText(result)) {
                if (builder.length() > 0) {
                    appendSpaces(1, builder);
                }
                builder.append(result);
            }
        }

        protected void buildChildren(StringBuilder builder) {
            if (property.hasChildren()) {
                builder.append(getChildrenLeft());
                for (Property child : property.getChildren()) {
                    builder.append(NEW_LINE);
                    appendSpaces(spaces + 2, builder);
                    builder.append(getChildLeft());
                    builder.append(child.getName());
                    builder.append(getChildRight());
                    builder.append(" => "); // NOI18N
                    CommonTooltip.build(child, builder, spaces + 2);
                }
                builder.append(NEW_LINE);
                appendSpaces(spaces, builder);
                builder.append(getChildrenRight());
            }
        }

        public String getChildrenLeft() {
            return getEmptyString();
        }

        public String getChildrenRight() {
            return getEmptyString();
        }

        public String getChildLeft() {
            return getEmptyString();
        }

        public String getChildRight() {
            return getEmptyString();
        }

        private String getEmptyString() {
            assert false : "Unknown type: " + type;
            return ""; // NOI18N
        }

        protected void appendSpaces(int count, StringBuilder builder) {
            for (int i = 0; i < count; i++) {
                builder.append(" "); //NOI18N
            }
        }

    }

    private static final class NullTooltip extends CommonTooltip {

        public NullTooltip(Property property, String type, String result, int spaces) {
            super(property, type, result, spaces);
        }

        @Override
        protected void buildType(StringBuilder builder) {
            builder.append(type);
        }

    }

    private static final class BooleanTooltip extends CommonTooltip {
        public BooleanTooltip(Property property, String type, String result, int spaces) {
            super(property, type, result, spaces);
        }

        @Override
        protected void buildResult(StringBuilder builder) {
            appendSpaces(1, builder);
            if ("1".equals(result)) { // NOI18N
                builder.append("true"); // NOI18N
            } else {
                builder.append("false"); // NOI18N
            }
        }

    }

    private static final class ObjectTooltip extends CommonTooltip {

        public ObjectTooltip(Property property, String type, String result, int spaces) {
            super(property, type, result, spaces);
        }

        @Override
        protected void buildType(StringBuilder builder) {
            builder.append(property.getClassName());
            builder.append(" "); // NOI18N
            builder.append(type);
        }

        @Override
        public String getChildrenLeft() {
            return " {"; // NOI18N
        }

        @Override
        public String getChildrenRight() {
            return "}"; // NOI18N
        }

        @Override
        public String getChildLeft() {
            return ""; // NOI18N
        }

        @Override
        public String getChildRight() {
            return ""; // NOI18N
        }

    }

    private static final class ArrayTooltip extends CommonTooltip {

        public ArrayTooltip(Property property, String type, String result, int spaces) {
            super(property, type, result, spaces);
        }

        @Override
        protected void buildType(StringBuilder builder) {
            builder.append(type); // NOI18N
            builder.append("("); // NOI18N
            builder.append(property.getChildrenSize());
            builder.append(")"); // NOI18N
        }

        @Override
        public String getChildrenLeft() {
            return " ("; // NOI18N
        }

        @Override
        public String getChildrenRight() {
            return ")"; // NOI18N
        }

        @Override
        public String getChildLeft() {
            return "["; // NOI18N
        }

        @Override
        public String getChildRight() {
            return "]"; // NOI18N
        }

    }

}
