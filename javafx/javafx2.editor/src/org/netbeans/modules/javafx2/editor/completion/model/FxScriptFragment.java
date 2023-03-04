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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;

/**
 *
 * @author sdedic
 */
public class FxScriptFragment extends FxNode  implements HasContent, HasResource {
    
    /**
     * Source as written in the attribute
     */
    private String  source;
    
    /**
     * Source location resolved against the original location
     */
    @NullAllowed
    private URL     sourceURL;
    
    /**
     * Content of the fragment, null for fragments with source reference
     */
    @NullAllowed
    private Object  content;

    public FxScriptFragment(String source) {
        this.source = source;
    }
    
    void addContent(CharSequence content) {
        this.content = PropertySetter.addCharContent(this.content, content);
    }
    
    void resolveSource(URL resolved) {
        this.sourceURL = resolved;
    }
    
    public CharSequence getContent() {
        CharSequence s = PropertySetter.getValContent(this.content);
        if (s != content) {
            content = s;
        }
        return s;
    }
    
    public boolean hasContent() {
        return content != null;
    }

    @Override
    public String getSourcePath() {
        return source;
    }
    
    @Override
    public URL getResolvedURL() {
        return sourceURL;
    }
    
    @Override
    public Kind getKind() {
        return Kind.Script;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitScript(this);
    }

    @Override
    public String getSourceName() {
        return FxXmlSymbols.FX_SCRIPT;
    }

    @Override
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
        // no op
    }
}
