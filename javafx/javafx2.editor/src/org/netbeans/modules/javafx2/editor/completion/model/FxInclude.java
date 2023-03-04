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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.net.URL;

/**
 * Represents fx:include instruction. Initially, the fx:include may be unresolved,
 * does not contain the included java type etc. Only filename is resolved (or an error
 * is reported).
 *
 * @author sdedic
 */
public class FxInclude extends FxInstance implements HasResource {
    private String  sourcePath;
    private URL  resolvedURL;
    private FxNewInstance target;

    public FxInclude(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public Kind getKind() {
        return Kind.Include;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public URL getResolvedURL() {
        return resolvedURL;
    }

    @Override
    public String getSourceName() {
        return FxXmlSymbols.FX_INCLUDE;
    }
    
    @Override
    public void accept(FxNodeVisitor v) {
        v.visitInclude(this);
    }
    
    void resolveFile(URL targetFile) {
        this.resolvedURL = targetFile;
    }
    
    public FxNewInstance resolve(FxmlParserResult result) {
        if (target != null) {
            return target;
        }
        return target = result.resolveInstance(this);
    }
    
    void resolveTarget(FxNewInstance target) {
        this.target = target;
        resolve(target.getJavaType(), null, null, target.getDefinition());
    }
}
