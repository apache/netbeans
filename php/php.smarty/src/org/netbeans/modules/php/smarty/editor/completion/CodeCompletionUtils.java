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
package org.netbeans.modules.php.smarty.editor.completion;

import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.netbeans.modules.php.smarty.editor.completion.entries.SmartyCodeCompletionOffer;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.utlis.LexerUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Fousek
 */
public class CodeCompletionUtils {
    private static final Logger LOG = Logger.getLogger(CodeCompletionUtils.class.getName());
    private static final int COMPLETION_MAX_FILTER_LENGHT = 20;
    private static final int SCANNING_MAX_FILTER_LENGHT = 100;

    public static String getTextPrefix(Document doc, int offset) {
        int readLength = (COMPLETION_MAX_FILTER_LENGHT > offset) ? offset : COMPLETION_MAX_FILTER_LENGHT;
        try {
            int lastWS = getLastWS(doc.getText(offset - readLength, readLength), getDocumentOpenDelimiter(doc));
            return doc.getText(offset - lastWS, lastWS);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "";
    }

    public static int getSubstitutionLenght(Document doc, int offset) {
        return getTextPrefix(doc, offset).length();
    }

    public static int getLastWS(String area, String openDelimiter) {
        for (int i = area.length() - 1; i >= 0; i--) {
            if (LexerUtils.isWS(area.charAt(i)) || area.charAt(i) == '|' || area.charAt(i) == '/') {
                return area.length() - i - 1;
            } else if (area.substring(i).startsWith(openDelimiter)) {
                return area.length() - i - openDelimiter.length();
            }
        }
        return area.length();
    }

    public static boolean startsWithIgnoreCase(String text, String prefix) {
        return text.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }

    public static boolean insideSmartyCode(Document doc, int offset) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

        tokenSequence.move(offset);
        if (tokenSequence.moveNext() || tokenSequence.movePrevious()) {
            Object tokenID = tokenSequence.token().id();

            if (tokenID == TplTopTokenId.T_HTML && isDefaultOpenDelimOnPreviousPosition(doc, offset)) {
                return true;
            }

            if (tokenID == TplTopTokenId.T_HTML
                    || tokenID == TplTopTokenId.T_PHP
                    || tokenID == TplTopTokenId.T_COMMENT
                    || tokenID == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                return false;
            }
        }
        return true;
    }

    static boolean inVariableModifiers(Document doc, int caretOffset) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();

        tokenSequence.move(caretOffset);
        tokenSequence.movePrevious(); tokenSequence.moveNext();
        while (!tokenSequence.isEmpty()) {
            if (tokenSequence.token().id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER
                    || tokenSequence.token().id() == TplTopTokenId.T_HTML
                    && isDefaultOpenDelimOnPreviousPosition(doc, caretOffset)) {
                return false;
            } else if (tokenSequence.token().id() == TplTopTokenId.T_SMARTY) {
                if (tokenSequence.token().text().toString().contains("|")) {
                    return true;
                }
            }
            tokenSequence.movePrevious();
        }
        return false;
    }

    private static boolean isDefaultOpenDelimOnPreviousPosition(Document doc, int currentOffset) {
        if (currentOffset == 0 || doc.getLength() < 1) {
            return false;
        }
        try {
            String text = doc.getText(currentOffset - 1, 1);
            return SmartyFramework.OPEN_DELIMITER.equals(text);
        } catch (BadLocationException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
        return false;
    }

    static ArrayList<String> afterSmartyCommand(Document doc, int offset) {
        String openDelimiter = getDocumentOpenDelimiter(doc);
        // search for command one position back - waits at least for space after command
        int updatedOffset = offset;
        try {
            if (updatedOffset > 0 &&
                    !doc.getText(offset - openDelimiter.length(), openDelimiter.length()).equals(openDelimiter)) {
                updatedOffset--;
            }
            int readLength = (SCANNING_MAX_FILTER_LENGHT > updatedOffset) ? updatedOffset : SCANNING_MAX_FILTER_LENGHT;
            return getLastKeywords(doc.getText(updatedOffset - readLength, readLength), openDelimiter);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new ArrayList<String>();
    }

    public static ArrayList<String> getLastKeywords(String area, String openDelimiter) {
        int delimiterPosition = area.lastIndexOf(openDelimiter);
        String searchingContent = (delimiterPosition > -1) ? area.substring(delimiterPosition + openDelimiter.length()) : area;
        String[] keywords = searchingContent.split("[ =]");
        ArrayList<String> availableItems = new ArrayList<String>();
        for (String string : keywords) {
            if (SmartyCodeCompletionOffer.getFunctionParameters().get(string) != null) {
                if (availableItems.isEmpty()) {
                    availableItems.add(string);
                }
                else {
                    availableItems.set(0, string);
                }
            }
            if (!availableItems.isEmpty()) {
                for (TplCompletionItem completionItem : SmartyCodeCompletionOffer.getFunctionParameters().get(availableItems.get(0))) {
                    if (completionItem.getItemText().equals(string)) {
                        availableItems.add(string);
                    }
                }
            }
        }
        return availableItems;
    }

    private static String getDocumentOpenDelimiter(Document doc) {
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        if (fileObject == null) {
            return SmartyFramework.getDelimiterDefaultOpen();
        } else {
            return SmartyFramework.getOpenDelimiter(fileObject);
        }
    }
}
