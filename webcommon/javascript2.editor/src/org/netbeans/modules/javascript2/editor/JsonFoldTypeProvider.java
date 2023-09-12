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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldTypeProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
@MimeRegistration(mimeType = "text/x-json", service = FoldTypeProvider.class, position = 1102)
public class JsonFoldTypeProvider implements FoldTypeProvider {

    private final Collection<FoldType>   types = new ArrayList<>(4);

    @NbBundle.Messages({
        "FT_Label_Object-block=Objects",
        "FT_display_Object-block={...}",
        "FT_display_default=..."
    })
    public static final FoldType OBJECT = FoldType.create("code-object", Bundle.FT_Label_Object_block(),
            new FoldTemplate(1, 1, Bundle.FT_display_Object_block()));

    @NbBundle.Messages({
        "FT_Label_Array-block=Arrays",
        "FT_display_Array-block=[...]"
    })
    public static final FoldType ARRAY = FoldType.create("code-array", Bundle.FT_Label_Array_block(),
            new FoldTemplate(1, 1, Bundle.FT_display_Array_block()));

    public JsonFoldTypeProvider() {
        types.add(OBJECT);
        types.add(ARRAY);
    }

    @Override
    public Collection getValues(Class type) {
        return types;
    }

    @Override
    public boolean inheritable() {
        return true;
    }

}
