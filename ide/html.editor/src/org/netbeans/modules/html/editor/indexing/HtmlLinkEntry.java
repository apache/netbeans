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
package org.netbeans.modules.html.editor.indexing;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 * Describes an external content referenced from HTML document. 
 * @author sdedic
 */
public final class HtmlLinkEntry extends Entry {

    private final String tagName;
    private final String attributeName;
    private final FileReference ref;

    public HtmlLinkEntry(FileObject baseFile, String link, OffsetRange astRange, OffsetRange documentRange, String tagName, String attributeName) {
        super(link, astRange, documentRange);
        this.tagName = tagName;
        this.attributeName = attributeName;
        this.ref = WebUtils.resolveToReference(baseFile, getName());
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getTagName() {
        return tagName;
    }

    public FileReference getFileReference() {
        return ref;
    }

    @Override
    public String toString() {
        return super.toString() + ", tag=" + getTagName() + ", attr=" + getAttributeName() + ", reference=" + getFileReference();
    }

}
