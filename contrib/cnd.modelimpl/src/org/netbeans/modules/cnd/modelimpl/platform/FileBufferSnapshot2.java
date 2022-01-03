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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 */
public class FileBufferSnapshot2 extends AbstractFileBuffer {

    private final Snapshot snapshot;
    private final long timestamp;

    public FileBufferSnapshot2(Snapshot snapshot, long timestamp) {
        super(snapshot.getSource().getFileObject());
        this.snapshot = snapshot;
        this.timestamp = timestamp;
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        CharSequence text = snapshot.getText();
        char[] res = new char[text.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = text.charAt(i);
        }
        return res;
    }

    @Override
    public boolean isFileBased() {
        return false; // TODO: ???
    }

    @Override
    public String getText(int start, int end) throws IOException {
        return snapshot.getText().subSequence(start, end).toString();
    }

    @Override
    public CharSequence getText() throws IOException {
         return snapshot.getText();
    }

    @Override
    public long lastModified() {
        //return snapshot.getSource().getFileObject().lastModified().getTime();
        return timestamp;
    }
}
