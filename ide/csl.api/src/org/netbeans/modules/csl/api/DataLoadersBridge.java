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

package org.netbeans.modules.csl.api;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.spi.DefaultDataLoadersBridge;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Provides the FileObject from a Document. Normally this means getting the info
 * from the Document's stream, but other implementations might exist.
 * 
 * @author Emilian Bold
 */
public abstract class DataLoadersBridge {

    public abstract Object createInstance(FileObject file);

    public abstract <T> T getCookie(FileObject fo, Class<T> aClass) throws IOException;
    
    public <T> T getCookie(JTextComponent comp, Class<T> aClass) throws IOException {
        return getCookie(getFileObject(comp), aClass);
    }

    public abstract Node getNodeDelegate(JTextComponent target);

    public abstract <T> T getSafeCookie(FileObject fo, Class<T> aClass);

    public abstract StyledDocument getDocument(FileObject fo);

    /**
     * @return Text of the given line in the document
     */
    public abstract String getLine(Document doc, int lineNumber);

    public abstract JEditorPane[] getOpenedPanes(FileObject fo);

    public abstract FileObject getFileObject(Document doc);

    public abstract FileObject getPrimaryFile(FileObject fileObject);

    public abstract EditorCookie isModified(FileObject file);

    public FileObject getFileObject(JTextComponent text) {
        return getFileObject(text.getDocument());
    }

    /**
     * Make a weak-referenced listener on the DataObject which represents the given FileObject
     * @param fo
     * @param fcl Will be called if the DataObject is invalidate
     * @return The listner, which needs to get a hard-reference otherwise will be garbage collected.
     * @throws java.io.IOException
     */
    public abstract PropertyChangeListener getDataObjectListener(FileObject fo, FileChangeListener fcl) throws IOException;
    //---
    private static DataLoadersBridge instance = null;

    public static synchronized DataLoadersBridge getDefault() {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(DataLoadersBridge.class);
            //TODO: listen on the lookup ? Seems too much
            if (instance == null) {
                instance = new DefaultDataLoadersBridge();
            }
        }
        return instance;
    }
}
