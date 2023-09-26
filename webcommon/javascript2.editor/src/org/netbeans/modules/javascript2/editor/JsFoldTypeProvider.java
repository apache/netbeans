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
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.fold.FoldTypeProvider;

/**
 *
 * @author Petr Pisl
 */
@MimeRegistration(mimeType = "text/javascript", service = FoldTypeProvider.class, position = 1101)
public class JsFoldTypeProvider implements FoldTypeProvider {

    private final Collection<FoldType>   types = new ArrayList<>(4);

    public JsFoldTypeProvider() {
        types.add(FoldType.COMMENT);
        types.add(FoldType.CODE_BLOCK);
        types.add(FoldType.DOCUMENTATION);
        types.add(FoldType.MEMBER);
    }

    @Override
    public Collection getValues(Class type) {
        return types;
    }

    @Override
    public boolean inheritable() {
        return false;
    }

}
