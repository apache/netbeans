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

package org.netbeans.modules.cnd.makeproject;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class MakeProjectEncodingQueryImpl extends FileEncodingQueryImplementation {
    
    private final MakeProject project;
    private String nameCache = null;
    private Charset cache = null;
    
    /** Creates a new instance of J2SEProjectEncodingQueryImpl */
    public MakeProjectEncodingQueryImpl(MakeProject project) {
        this.project = project;
    }
    
    @Override
    public Charset getEncoding(FileObject file) {
        assert file != null;
        
        synchronized (this) {
            String enc = project.getSourceEncoding();
            
            if (!enc.equals(nameCache)) {
                cache = null;
                nameCache = enc;
            }
        
            if (cache != null) {
                return cache;
            }
        }
        
        synchronized (this) {
            if (cache == null) {
                try {
                    //From discussion with K. Frank the project returns Charset.defaultCharset ()
                    //for older projects (no encoding property). The old project used system encoding => Charset.defaultCharset ()
                    //should work for most users.
                    cache = nameCache == null ? Charset.defaultCharset() : Charset.forName(nameCache);
                } catch (IllegalCharsetNameException exception) {
                    return null;
                }
            }
            return cache;
        }
    }
}
