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
package org.netbeans.modules.xml.xdm.diff;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;

/**
 * Support for two-phased sync protocol.
 *
 * @author Nam Nguyen
 */
public class SyncPreparation {
    private Document newDoc;
    private Document oldDoc;
    private List<Difference> diffs;
    private IOException error;
    
    /** Creates a new instance of SyncPreparation */
    public SyncPreparation(Document newDoc) {
        assert newDoc != null : "Argument newDoc is null";
        this.newDoc = newDoc;
    }
    
    public SyncPreparation(Document oldDoc, List<Difference> diffs) {
        assert oldDoc != null : "Argument oldDoc is null.";
        this.oldDoc = oldDoc;
        this.diffs = diffs;
    }
    
    public SyncPreparation(Exception err) {
        assert err != null : "Argument err is null.";
        if (err instanceof IOException) {
            error = (IOException) err;
        } else {
            error = new IOException();
            error.initCause(err);
        }
    }
    
    public Document getNewDocument() {
        return newDoc;
    }
    
    public Document getOldDocument() {
        return oldDoc;
    }
    
    public List<Difference> getDifferences() {
        return diffs;
    }
    
    public boolean hasErrors() {
        return error != null;
    }
    
    public IOException getError() {
        return error;
    }
    
}
