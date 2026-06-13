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
package org.netbeans.modules.php.blade.editor;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import static org.netbeans.lib.editor.hyperlink.spi.HyperlinkType.GO_TO_DECLARATION;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexerUtils;
import org.netbeans.modules.php.blade.editor.path.BladePathUtils;
import org.netbeans.modules.php.blade.project.BladeProjectProperties;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Similar to a declaration finder
 * 
 *
 * @author bhaidu
 */
@MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = HyperlinkProviderExt.class)
public class HyperlinkProviderImpl implements HyperlinkProviderExt {

    private String methodName;
    private String identifiableText;
    private String tooltipText = ""; // NOI18N
    private FileObject goToFile;
    private int goToOffset = 0;
    public static final int MIN_STRING_IDENTIIFER_LENGTH = 5;
    public static final String FILE_TITLE = "Blade Template File"; // NOI18N

    private final String[] viewMethods = new String[]{"view", "render", "make"}; // NOI18N
    private final Set<String> viewMethodSet = new HashSet<>(Arrays.asList(viewMethods));

    public enum DeclarationType {
        VIEW_PATH;
    }

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION, HyperlinkType.ALT_HYPERLINK);
    }

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        if (!nonLaravelDeclFinderEnabled(doc)) {
            return false;
        }
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        if (!nonLaravelDeclFinderEnabled(doc)) {
            return null;
        }
        if (!type.equals(HyperlinkType.GO_TO_DECLARATION)) {
            //not handled by a LSP handler
            return null;
        }

        BaseDocument baseDoc = (BaseDocument) doc;
        int lineStart = LineDocumentUtils.getLineStart(baseDoc, offset);
        TokenSequence<PHPTokenId> tokensq = BladeLexerUtils.getLockedPhpTokenSequence(doc, offset);

        if (tokensq == null) {
            return null;
        }

        Token<PHPTokenId> currentToken = tokensq.token();
        int startOffset = tokensq.offset();

        if (currentToken == null) {
            return null;
        }

        String focusedText = currentToken.text().toString();

        //2 char config are not that relevant
        if (focusedText.length() < MIN_STRING_IDENTIIFER_LENGTH || !EditorStringUtils.isQuotedString(focusedText)) {
            return null;
        }

        identifiableText = focusedText.substring(1, focusedText.length() - 1);
        PHPTokenId prevTokenId = null;

        while (tokensq.movePrevious() && tokensq.offset() >= lineStart) {
            Token<PHPTokenId> token = tokensq.token();
            if (token == null) {
                break;
            }
            String text = token.text().toString();
            PHPTokenId id = token.id();

            if (prevTokenId != null && id.equals(PHPTokenId.PHP_STRING)) {
                methodName = text;
                //tooltip text

                if (viewMethodSet.contains(methodName)) {
                    FileObject currentFile = FileSystemUtils.getFileObjectFromDoc(doc);

                    if (currentFile == null) {
                        return null;
                    }
                    List<FileObject> includedFiles = BladePathUtils.findFileObjectsForBladeViewPath(currentFile, identifiableText);
                    String viewPath = BladePathUtils.toBladeToProjectFilePath(identifiableText);

                    for (FileObject includedFile : includedFiles) {
                        goToFile = includedFile;
                        tooltipText = FILE_TITLE + " File : <b>" + viewPath // NOI18N
                                + "</b><br><br><i style='margin-left:20px;'>" + identifiableText + "</i>"; // NOI18N
                        goToOffset = 0;
                        break;
                    }

                    return new int[]{startOffset, startOffset + currentToken.length()};
                }

            }

            if (id.equals(PHPTokenId.PHP_TOKEN) && text.equals("(")) { // NOI18N
                prevTokenId = id;
            }
        }
        return null;
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        if (!type.equals(GO_TO_DECLARATION)) {
            return;
        }
        if (viewMethodSet.contains(methodName)) {
            if (goToFile != null) {
                openDocument(goToFile, goToOffset);
            }
        }
    }
    
    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        return "<html><body>" + tooltipText + "</body></html>"; // NOI18N
    }

    private void openDocument(FileObject f, int offset) {
        try {
            DataObject dob = DataObject.find(f);
            NbDocument.openDocument(dob, offset, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * compatible relation with laravel framework plugin
     * blade editor is not a Php Extension, so that's the reason for the implementation of HyperlinkProvider.
     * 
     * @param doc
     * @return 
     */
    private boolean nonLaravelDeclFinderEnabled(Document doc) {
        Project projectOwner = FileSystemUtils.getProjectOwner(doc);
        if (projectOwner == null) {
            return false;
        }
        BladeProjectProperties bladeProperties = BladeProjectProperties.getInstance(projectOwner);

        return bladeProperties.getNonLaravelDeclFinderFlag();
    }

}
