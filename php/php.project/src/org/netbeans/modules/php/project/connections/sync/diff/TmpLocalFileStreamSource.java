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
package org.netbeans.modules.php.project.connections.sync.diff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.netbeans.modules.php.project.connections.TmpLocalFile;

/**
 * Stream source for temporary local files.
 */
public class TmpLocalFileStreamSource extends BaseStreamSource {

    private final TmpLocalFile tmpFile;
    private final String charsetName;


    public TmpLocalFileStreamSource(String name, TmpLocalFile tmpFile, String mimeType, String charsetName, boolean remote) {
        super(name, mimeType, remote);
        this.tmpFile = tmpFile;
        this.charsetName = charsetName;
    }

    @Override
    protected Reader createReaderInternal() throws IOException {
        return createReader(tmpFile);
    }

    private Reader createReader(TmpLocalFile file) throws FileNotFoundException {
        try {
            return new BufferedReader(new InputStreamReader(file.getInputStream(), charsetName));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
