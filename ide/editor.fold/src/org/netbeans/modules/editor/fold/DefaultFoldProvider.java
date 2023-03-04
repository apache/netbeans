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
package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldTypeProvider;

/**
 * Default folds, provided by the infrastructure.
 * These fold types do not propagate to individual languages, only serve as
 * a common base for "all languages" configuration.
 * 
 * @author sdedic
 */
@MimeRegistration(mimeType = "", service = FoldTypeProvider.class, position = 950)
public class DefaultFoldProvider implements FoldTypeProvider {
    private final Collection<FoldType>  defaultTypes;
    
    public DefaultFoldProvider() {
        defaultTypes = new ArrayList<FoldType>(7);
        defaultTypes.add(FoldType.CODE_BLOCK);
        defaultTypes.add(FoldType.COMMENT);
        defaultTypes.add(FoldType.DOCUMENTATION);
        defaultTypes.add(FoldType.INITIAL_COMMENT);
        defaultTypes.add(FoldType.MEMBER);
        defaultTypes.add(FoldType.NESTED);
        defaultTypes.add(FoldType.TAG);
        defaultTypes.add(FoldType.USER);
    }
    
    @Override
    public Collection getValues(Class type) {
        return type == FoldType.class ? defaultTypes : null;
    }
    
    @Override
    public boolean inheritable() {
        return false;
    }
    
}
