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
package org.netbeans.tax.io;

import java.io.*;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.io.TreeStreamResult;
import org.netbeans.tax.io.TreeWriter;

/**
 * Access tree content using input stream. The tree MUST NOT change until the
 * stream is closed. It is responsibility of caller.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public class TreeInputStream extends InputStream {

    private final ByteArrayInputStream input;

    /**
     * Creates new TreeInputStream that is immediatelly ready for reading.
     */
    public TreeInputStream (TreeDocumentRoot doc) throws IOException {
        input = new ByteArrayInputStream (Convertors.treeToByteArray (doc));
    }
    
    public void close () throws IOException {
        if (input == null) throw new IOException (Util.THIS.getString ("EXC_null_input"));
        input.close ();
    }
    
    public int read () throws IOException {
        if (input == null) throw new IOException (Util.THIS.getString ("EXC_null_input"));
        int ch = input.read ();
        return ch;
    }
}
