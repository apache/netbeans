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
package org.netbeans.modules.versioning.annotate;

import java.util.*;

/**
 * Encapsulates versioning annotation of one line of text.
 * 
 * @author Maros Sandor
 */
public final class VcsAnnotation {

    private int     lineNumber;
    private String  author;
    private String  revision;
    private Date    date;
    private String  documentText;
    private String  description;

    public VcsAnnotation(int lineNumber, String author, String revision, Date date, String documentText, String description) {
        this.lineNumber = lineNumber;
        this.author = author;
        this.revision = revision;
        this.date = date;
        this.documentText = documentText;
        this.description = description;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getAuthor() {
        return author;
    }

    public String getRevision() {
        return revision;
    }

    public Date getDate() {
        return date;
    }

    public String getDocumentText() {
        return documentText;
    }

    public String getDescription() {
        return description;
    }
}
