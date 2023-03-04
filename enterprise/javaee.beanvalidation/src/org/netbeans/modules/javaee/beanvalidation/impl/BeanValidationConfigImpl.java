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

package org.netbeans.modules.javaee.beanvalidation.impl;

import java.io.IOException;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.javaee.beanvalidation.api.BeanValidationConfig;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author alexey butenko
 */
public class BeanValidationConfigImpl implements BeanValidationConfig{

    private FileObject configFile;
    private BaseDocument document = null;
    private int insertOffset = 0;

    public BeanValidationConfigImpl(FileObject configFile) {
        this.configFile = configFile;
    }

    public String getName() {
        return configFile.getName();
    }
    public HashMap<FileObject, ConstraintMapping> getConstraintMappings() {
        HashMap<FileObject, ConstraintMapping> constraintMap = new HashMap<FileObject, ConstraintMapping>();
        getDocument();
        insertOffset = 0;
        if (document != null) {
            try {
                document.readLock();
                TokenHierarchy hi = TokenHierarchy.get(document);
                TokenSequence ts = hi.tokenSequence();
                boolean started = false;
                boolean ended = false;
                boolean findInsertPoint = false;
                int startOffset = 0;
                int endOffset = 0;
                FileObject constraintFile = null;
                while (ts.moveNext()) {
                    Token t = ts.token();

                    if (t.id() == XMLTokenId.TAG && t.text().equals("<validation-config")) {    //NOI18N
                        findInsertPoint = true;
                    }
                    if (findInsertPoint && t.id() == XMLTokenId.TAG && t.text().equals(">")) {    //NOI18N
                        insertOffset = t.offset(hi) +t.length();
                        findInsertPoint = false;
                    }
                    if (!started && t.id() == XMLTokenId.TAG && t.text().equals("<constraint-mapping")) {    //NOI18N
                        startOffset = t.offset(hi);
                        endOffset = startOffset +t.length();
                        started = true;
                        ended = true;
                    }
                    if (started && t.id() == XMLTokenId.TEXT) {
                        endOffset += t.length();
                        String value = t.text().toString();
                        WebModule wm = WebModule.getWebModule(configFile);
                        constraintFile = wm.getDocumentBase().getFileObject(value);
                    }
                    if (started && t.id() == XMLTokenId.TAG && t.text().equals("</constraint-mapping")) {    //NOI18N
                        endOffset += t.length()+1;
                        ended = true;
                        started = false;
                    }

                    if (ended && t.id() == XMLTokenId.TAG && t.text().equals(">")) {    //NOI18N
                        endOffset +=t.length();
                        ended = false;
                    }
                    
                    if (!started && !ended && constraintFile != null) {
                        ConstraintMapping constraintMapping = new ConstraintMappingImpl(constraintFile, startOffset, endOffset);
                        constraintMap.put(constraintFile, constraintMapping);
                        constraintFile = null;
                    }
                }
            }finally{
                document.readUnlock();
            }
        }
        return constraintMap;
    }

    private BaseDocument getDocument() {
        if (document == null) {
            try {
                DataObject dataObject = DataObject.find(configFile);
                synchronized (dataObject) {
                    EditorCookie editor = dataObject.getLookup().lookup(EditorCookie.class);
                    if (editor != null) {
                        document =  (BaseDocument) editor.getDocument();
                        if (document != null) {
                            return document;
                        }
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return document;
    }

    private int getInsertOffset() {
        return insertOffset;
    }

    public void addConstraintMapping(FileObject fileObject) {
        if (!getConstraintMappings().containsKey(fileObject)) {
            try {
                WebModule wm = WebModule.getWebModule(fileObject);
                if (wm != null) {
                    String str = "\n<constraint-mapping>" + FileUtil.getRelativePath(wm.getDocumentBase(), fileObject) +    //NOI18N
                                    "</constraint-mapping>";  //NOI18N
                    getDocument().insertString(getInsertOffset(), str, null);
                    reformat();
                    saveConfig();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
    }

    private void reformat() {

        final Reformat reformat = Reformat.get(document);
        reformat.lock();
        try {
            document.runAtomic(new Runnable() {

                public void run() {
                    try {
                        reformat.reformat(0, document.getLength());
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } finally {
            reformat.unlock();
        }
    }
    
    private void saveConfig() {
        try {
            DataObject dataObject = DataObject.find(configFile);
            SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
            if (saveCookie != null) {
                saveCookie.save();
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void removeConstraintMapping(FileObject fileObject) {
        ConstraintMapping constraintMapping = getConstraintMappings().get(fileObject);
        if (constraintMapping != null) {
            try {
                getDocument().remove(constraintMapping.getStartOffset(), constraintMapping.getEndOffset() - constraintMapping.getStartOffset());
                reformat();
                saveConfig();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    class ConstraintMappingImpl implements ConstraintMapping {
        FileObject fileObject;
        int startOffset;
        int endOffset;

        public ConstraintMappingImpl(FileObject fileObject, int startOffset, int endOffset) {
            this.fileObject = fileObject;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }
}
