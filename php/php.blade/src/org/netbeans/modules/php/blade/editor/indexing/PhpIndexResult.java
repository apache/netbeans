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
package org.netbeans.modules.php.blade.editor.indexing;

import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class PhpIndexResult {
    public static enum Type{
        CLASS,
        FUNCTION,
        NAMESPACE,
        CONSTANT
    };

    public String name;
    public String namespace;
    public FileObject declarationFile;
    public PhpIndexResult.Type type;
    public OffsetRange range;
    
    public PhpIndexResult(String name, FileObject fo,
            PhpIndexResult.Type type,
            OffsetRange range){
        this.name = name;
        this.declarationFile = fo;
        this.type = type;
        this.range = range;
    }
    
    public PhpIndexResult(String name, String qualifiedName, FileObject fo,
            PhpIndexResult.Type type,
            OffsetRange range){
        this.name = name;
        this.namespace = qualifiedName;
        this.declarationFile = fo;
        this.type = type;
        this.range = range;
    }
    
    public int getStartOffset(){
        return this.range.getStart();
    }
}
