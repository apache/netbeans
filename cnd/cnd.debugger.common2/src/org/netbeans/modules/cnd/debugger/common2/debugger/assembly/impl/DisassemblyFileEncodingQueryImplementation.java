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

package org.netbeans.modules.cnd.debugger.common2.debugger.assembly.impl;

import java.nio.charset.Charset;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = FileEncodingQueryImplementation.class)
public class DisassemblyFileEncodingQueryImplementation extends FileEncodingQueryImplementation {
    
    public static final Charset CHARSET = Charset.forName("UTF-8"); //NOI18N
    
    @Override
    public Charset getEncoding(FileObject file) {
        if (file == Disassembly.getFileObject()) {
            return CHARSET;
        }
        return null;
    }

}
