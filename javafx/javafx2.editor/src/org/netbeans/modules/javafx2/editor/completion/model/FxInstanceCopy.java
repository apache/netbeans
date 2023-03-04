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

/**
 *
 * @author sdedic
 */
public final class FxInstanceCopy extends FxInstance {
    /**
     * ID of the blueprint
     */
    private String  blueprintId;
    
    private FxInstance blueprint;

    FxInstanceCopy(String blueprintId) {
        this.blueprintId = blueprintId;
    }

    public String getBlueprintId() {
        return blueprintId;
    }

    @Override
    public Kind getKind() {
        return Kind.Instance;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitCopy(this);
    }

    public FxInstance getBlueprint() {
        return blueprint;
    }
    
    void resolveBlueprint(FxInstance target) {
        this.blueprint = target;
        if (blueprint != null) {
            setJavaType(target.getJavaType());
        }
    }

    public String getSourceName() {
        return FxXmlSymbols.FX_COPY;
    }
    
}
