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
package org.netbeans.modules.profiler.api;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.filesystems.FileObject;

/**
 * Editor context encapsulating document, project and (Swing) text component.
 * 
 * @author Jiri Sedlacek
 */
public final class EditorContext {
    
    private Document document;
    private FileObject fileObject;
    private JTextComponent textComponent;

    
    public EditorContext(JTextComponent textComponent, Document document, FileObject fileObject) {
        this.textComponent = textComponent;
        this.document = document;
        this.fileObject = fileObject;
    }


    public Document getDocument() {
        return document;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

    public JTextComponent getTextComponent() {
        return textComponent;
    }
    
}
