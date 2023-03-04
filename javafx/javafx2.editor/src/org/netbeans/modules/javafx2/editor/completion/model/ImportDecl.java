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

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.modules.javafx2.editor.completion.beans.FxDefinition;

/**
 * Represents a FXML ?import processing instruction
 * 
 * @author sdedic
 */
public final class ImportDecl extends FxNode {
    /**
     * True, if the import is a wildcard (package) one
     */
    private final boolean wildcard;
    
    /**
     * Imported name
     */
    private final String  importedName;
    
    public boolean isWildcard() {
        return wildcard;
    }

    public String getImportedName() {
        return importedName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImportDecl other = (ImportDecl) obj;
        if (this.wildcard != other.wildcard) {
            return false;
        }
        if ((this.importedName == null) ? (other.importedName != null) : !this.importedName.equals(other.importedName)) {
            return false;
        }
        return true;
    }

    ImportDecl(String importedName, boolean wildcard) {
        this.wildcard = wildcard;
        this.importedName = importedName;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitImport(this);
    }

    @Override
    public Kind getKind() {
        return Kind.Import;
    }
    
    public String getSourceName() {
        return FxXmlSymbols.FX_IMPORT;
    }

    @Override
    @SuppressWarnings("rawtypes")
    void resolve(ElementHandle nameHandle, TypeMirrorHandle typeHandle, ElementHandle<TypeElement> sourceTypeHandle, FxDefinition info) {
    }
    
    
}
