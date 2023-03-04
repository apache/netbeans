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
package org.netbeans.modules.html.editor.gsf;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.fold.FoldTypeProvider;

/**
 * @author sdedic
 */
@MimeRegistrations({
        @MimeRegistration(mimeType = "text/html", service = FoldTypeProvider.class, position = 1400),
        @MimeRegistration(mimeType = "text/xhtml", service = FoldTypeProvider.class, position = 1400)
})
public class HtmlFoldTypeProvider  implements FoldTypeProvider {
    private Collection<FoldType> TYPES = Arrays.asList(new FoldType[] {
        HtmlStructureScanner.TYPE_TAG,
        HtmlStructureScanner.TYPE_COMMENT
    });

    @Override
    public Collection getValues(Class type) {
        return type == FoldType.class ? TYPES : null;
    }

    @Override
    public boolean inheritable() {
        return false;
    }
    
}
