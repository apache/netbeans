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
package org.netbeans.modules.html.editor.hints.css;

import java.util.Collection;
import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marek
 */
public class HintContext {

    private final String pureElementName;
    private final String elementName;
    private final Collection<FileObject> referredFiles;
    private final Collection<FileObject> allStylesheets;
    private final Map<FileObject, Collection<String>> elements;
    private final Map<String, Collection<FileObject>> element2files;

    public HintContext(String pureElementName, String elementName, Collection<FileObject> referredFiles, Collection<FileObject> allStylesheets, Map<FileObject, Collection<String>> elements, Map<String, Collection<FileObject>> element2files) {
        this.pureElementName = pureElementName;
        this.elementName = elementName;
        this.referredFiles = referredFiles;
        this.allStylesheets = allStylesheets;
        this.elements = elements;
        this.element2files = element2files;
    }
    
    /**
     * Gets the name of the element without the type prefix (dot or hash).
     */
    public String getPureElementName() {
        return pureElementName;
    }

    public String getElementName() {
        return elementName;
    }

    public Collection<FileObject> getAllStylesheets() {
        return allStylesheets;
    }

    public Collection<FileObject> getReferredFiles() {
        return referredFiles;
    }

    public Map<FileObject, Collection<String>> getElements() {
        return elements;
    }

    public Map<String, Collection<FileObject>> getElement2files() {
        return element2files;
    }
}
