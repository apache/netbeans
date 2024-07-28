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

import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileObject;

/**
 *
 * @author bogdan
 */
public class PhpIndexFunctionResult extends PhpIndexResult {

    protected final List<String> params;
    protected final String classNamespace;

    public PhpIndexFunctionResult(String name, FileObject fo,
            PhpIndexFunctionResult.Type type,
            OffsetRange range, String classNamespace, List<String> params) {
        super(name, fo, type, range);
        this.params = params;
        this.classNamespace = classNamespace;
    }

    public String getParamsAsString() {
        if (params == null || params.isEmpty()){
            return "()";
        }
        return "(" + String.join(", ", params) + ")";
    }

    public List<String> getParams(){
        return params;
    }
    
    public String getClassNamespace(){
        return classNamespace;
    }
}
