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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.Position;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.textsync.TextRegion;

/**
 * Implementation of the code template parameter.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateParameterImpl {

    private static final String NULL_PARAMETER_NAME = "<null>"; // NOI18N

    private static final String NULL_HINT_NAME = "<null>"; // NOI18N
    
    private static final String TRUE_HINT_VALUE = "true"; // NOI18N
    
    /**
     * Get parameter implementation from parameter instance.
     */
    public static CodeTemplateParameterImpl get(CodeTemplateParameter parameter) {
        return CodeTemplateSpiPackageAccessor.get().getImpl(parameter);
    }
    
    /**
     * Insert handler - may be null e.g. when parsing for completion item rendering.
     */
    private final CodeTemplateInsertHandler handler;
    
    private final CodeTemplateParameter parameter;
    
    private String value;
    
    private int parametrizedTextStartOffset;
    
    private int parametrizedTextEndOffset;
    
    private CodeTemplateParameter master;
    
    private Collection<CodeTemplateParameter> slaves;
    
    private String name;
    
    private Map<String, String> hints;
    
    private Map<String, String> hintsUnmodifiable;
    
    private TextRegion<CodeTemplateParameterImpl> textRegion;
    
    private boolean editable;
    
    private boolean userModified;

    private boolean completionInvoke;

    CodeTemplateParameterImpl(CodeTemplateInsertHandler handler,
    String parametrizedText, int parametrizedTextOffset) {
        this.handler = handler; // handler may be null for completion item parsing
        this.parametrizedTextStartOffset = parametrizedTextOffset;
        this.parameter = CodeTemplateSpiPackageAccessor.get().createParameter(this);
        textRegion = new TextRegion<CodeTemplateParameterImpl>(); // zero bounds for now
        textRegion.setClientInfo(this);
        parseParameterContent(parametrizedText);
    }
    
    public CodeTemplateParameter getParameter() {
        return parameter;
    }

    public CodeTemplateInsertHandler getHandler() {
        return handler;
    }
    
    /**
     * Get name of this parameter as parsed from the code template description's text.
     */
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return isSlave()
                ? master.getValue()
                : ((handler != null && handler.isInserted()) ? handler.getDocParameterValue(this) : value);
    }

    /**
     * Set value from parameter's API.
     * 
     * @param newValue new value of the parameter.
     */
    public void setValue(String newValue) {
        if (isSlave()) {
            throw new IllegalStateException("Cannot set value for slave parameter"); // NOI18N
        }
        if (newValue == null) {
            throw new NullPointerException("newValue cannot be null"); // NOI18N
        }

        // handler assumed to be non-null
        if (!handler.isReleased()) {
            if (handler.isInserted()) {
                // Includes parameter update notification
                handler.setDocMasterParameterValue(this, newValue);
                return;
            } else { // not inserted yet
                this.value = newValue;
                handler.resetCachedInsertText();
                // Do not notify parameter change when not inserted in document
            }
        }
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public boolean isUserModified() {
        return userModified;
    }
    
    boolean isCompletionInvoke() {
        return completionInvoke;
    }
    
    void markUserModified() {
        this.userModified = true;
    }
    
    /**
     * @return &gt;=0 index of the '${' in the parametrized text.
     */
    public int getParametrizedTextStartOffset() {
        return parametrizedTextStartOffset;
    }
    
    /**
     * If the parameter is unclosed the offset will point past the end
     * of the parametrized text.
     *
     * @return &gt;=0 end offset of the parameter in the parametrized text
     *  pointing right after the closing '}' of the parameter.
     */
    public int getParametrizedTextEndOffset() {
        return parametrizedTextEndOffset;
    }

    public int getInsertTextOffset() {
        if (handler != null) {
            if (!handler.isInserted()) {
                handler.checkInsertTextBuilt();
            }
            return textRegion.startOffset() - handler.getInsertOffset();
        }
        return textRegion.startOffset();
    }

    void resetPositions(Position startPosition, Position endPosition) {
        textRegion.updateBounds(startPosition, endPosition);
    }
    
    public TextRegion textRegion() {
        return textRegion;
    }

    public Map<String, String> getHints() {
        return (hintsUnmodifiable != null) ? hintsUnmodifiable : Collections.<String, String>emptyMap();
    }
    
    public CodeTemplateParameter getMaster() {
        return master;
    }
    
    public Collection<? extends CodeTemplateParameter> getSlaves() {
        return (slaves != null) ? slaves : Collections.<CodeTemplateParameter>emptyList();
    }
    
    public boolean isSlave() {
        return (master != null);
    }
    
    /**
     * Mark that this parameter will be slave of the given master parameter.
     */
    void markSlave(CodeTemplateParameter master) {
        CodeTemplateParameterImpl masterImpl = paramImpl(master);
        if (getMaster() != null) {
            throw new IllegalStateException(toString() + " already slave of " + master); // NOI18N
        }
        setMaster(master);
        masterImpl.addSlave(getParameter());
        
        // reparent slaves as well
        if (slaves != null) {
            for (Iterator<CodeTemplateParameter> it = slaves.iterator(); it.hasNext();) {
                CodeTemplateParameterImpl paramImpl = paramImpl(it.next());
                paramImpl.setMaster(master);
                masterImpl.addSlave(paramImpl.getParameter());
            }
            slaves.clear();
        }
    }
    
    private static CodeTemplateParameterImpl paramImpl(CodeTemplateParameter param) {
        return CodeTemplateSpiPackageAccessor.get().getImpl(param);
    }
    
    /**
     * Initialize the hints of this parameter by parsing
     * parameter's text from the given parametrized text
     * at the offset given in the constructor.
     * 
     * @param parametrizedText text to parse at the offset given in the constructor.
     * @return index of the '}' where the parameter ends
     *  or <code>parametrizedText.length()</code> if the parameter is unclosed.
     */
    private void parseParameterContent(String parametrizedText) {
        int index = parametrizedTextStartOffset + 2;
        String hintName = null;
        boolean afterEquals = false;
        int nameStartIndex = -1;
        boolean insideStringLiteral = false;
        StringBuffer stringLiteralText = new StringBuffer();

        while (true) {
            // Search for names or "..." values separated by whitespace
            String completedString = null;
            if (index >= parametrizedText.length()) {
                break;
            }
            char ch = parametrizedText.charAt(index);

            if (insideStringLiteral) { // inside string constant "..."
                if (ch == '"') { // string ends
                    insideStringLiteral = false;
                    completedString = stringLiteralText.toString();
                    stringLiteralText.setLength(0); // clear the string buffer

                } else if (ch == '\\') {
                    index = escapedChar(parametrizedText,
                            index + 1, stringLiteralText);
                } else { // regular char
                    stringLiteralText.append(ch);
                }

            } else { // not string hint
                if (Character.isWhitespace(ch) || ch == '=' || ch == '}') {
                    if (nameStartIndex != -1) { // name found
                        completedString = parametrizedText.substring(
                                nameStartIndex, index);
                        nameStartIndex = -1;
                    } else {
                        // No name was accounted
                    }

                } else if (ch == '"') { // starting string literal
                    insideStringLiteral = true;

                } else { // starting or inside name
                    if (nameStartIndex == -1) {
                        nameStartIndex = index;
                    }
                }
            }

            if (completedString != null) {
                if (name == null) { // First string will be parameter's name
                    name = completedString;
                } else { // hints
                    if (hints == null) { // Create hints
                        hints = new LinkedHashMap<String, String>(4);
                        hintsUnmodifiable = Collections.unmodifiableMap(hints);
                    }
                    
                    if (hintName == null) { // no current hint's name
                        if (afterEquals) { // hint's value
                            // Hint name was not filled in
                            hints.put(NULL_HINT_NAME, completedString);
                            afterEquals = false;
                            // hintName stays null
                            
                        } else { // will be hint name
                            hintName = completedString;
                        }
                        
                    } else { // hint's name is non-null
                        if (afterEquals) { // hint's value
                            hints.put(hintName, completedString);
                            afterEquals = false;
                            hintName = null;
                            
                        } else { // next hint
                            hints.put(hintName, TRUE_HINT_VALUE);
                            hintName = completedString;
                        }
                    }
                }
            }
            
            if (!insideStringLiteral) {
                if (ch == '=') {
                    afterEquals = true;
                } else if (ch == '}') { // end of the parameter
                    if (hintName != null) { // true-value hint
                        hints.put(hintName, TRUE_HINT_VALUE);
                        hintName = null;
                    }
                    break;
                }
            }
            
            index++; // move to next char
        }
        
        if (name == null) {
            name = NULL_PARAMETER_NAME;
        }
        
        // Determine default parameter's value
        String defaultValue = getHints().get(CodeTemplateParameter.DEFAULT_VALUE_HINT_NAME);
        if (defaultValue == null) { // implicit value will be name of the parameter
            defaultValue = name;
        }
        value = defaultValue;
        
        String completionInvokeHint = getHints().get(CodeTemplateParameter.COMPLETION_INVOKE_HINT_NAME);
        completionInvoke = Boolean.parseBoolean(completionInvokeHint);
        
        if (name.equals(CodeTemplateParameter.CURSOR_PARAMETER_NAME)
                || name.equals(CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME)
                || name.equals(CodeTemplateParameter.NO_INDENT_PARAMETER_NAME)) {
            editable = false;
            value = "";
        } else if (name.equals(CodeTemplateParameter.SELECTION_PARAMETER_NAME)) {
            editable = false;            
            if (handler != null) {
                value = handler.getComponent().getSelectedText();
                if (value == null)
                    value = ""; //NOI18N
                if (value.endsWith("\n"))
                    value = value.substring(0, value.length() - 1);
                if (getHints().get(CodeTemplateParameter.LINE_HINT_NAME) != null && !value.endsWith("\n")) //NOI18N
                    value += "\n"; //NOI18N
            }
        } else {
            editable = !isHintValueFalse(CodeTemplateParameter.EDITABLE_HINT_NAME);
        }
        
        parametrizedTextEndOffset = index + 1;
    }
    
    private boolean isHintValueFalse(String hintName) {
        String hintValue = getHints().get(hintName);
        return (hintValue != null) && "false".equalsIgnoreCase(hintValue); // NOI18N
    }
    
    /**
     * Called after '\' was found in the text to complete the escaped
     * character and append it to the given output.
     *
     * @param text non-null text to be scanned.
     * @param index index after '\' in the text to be used for finding
     *  the target character.
     * @param output non-null output to which the resulting character should
     *  be appended.
     * @return index of the next character to read.
     */
    private int escapedChar(CharSequence text, int index, StringBuffer output) {
        if (index == text.length()) {
            output.append('\\');
        } else {
            switch (text.charAt(index++)) {
                case '\\':
                    output.append('\\');
                    break;
                case 'n':
                    output.append('\n');
                    break;
                case 'r':
                    output.append('\r');
                    break;
                case '"':
                    output.append('"');
                    break;
                case '\'':
                    output.append('\'');
                    break;
                    
                case 'u': // Unicode sequence
                    int val = 0;
                    for (int i = 0; i < 4; i++) {
                        if (index < text.length()) {
                            char ch = text.charAt(index);
                            if (ch >= '0' && ch <= '9') {
                                val = (val << 4) + (ch - '0');
                            } else if (ch >= 'a' && ch <= 'f') {
                                val = (val << 4) + 10 + (ch - 'a');
                            } else if (ch >= 'A' && ch <= 'F') {
                                val = (val << 4) + 10 + (ch - 'F');
                            } else { // invalid char
                                break;
                            }
                        }
                        index++;
                    }
                    output.append(val);
                    break;
                    
                default: // not known char => append '\'
                    index--;
                    output.append('\\');
                    break;
            }
        }

        return index; // index of the next read
    }
    
    private void addSlave(CodeTemplateParameter slave) {
        if (slaves == null) {
            slaves = new ArrayList<CodeTemplateParameter>(2);
        }
        slaves.add(slave);
    }
    
    private void setMaster(CodeTemplateParameter master) {
        this.master = master;
    }
    
    @Override
    public String toString() {
        return "name=" + getName() + ", slave=" + isSlave() // NOI18N
            + ", value=\"" + getValue() + '"'; // NOI18N
    }

}
