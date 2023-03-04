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
package org.netbeans.modules.web.el.completion;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELStreamCompletionItem extends DefaultCompletionProposal {

    public static final String STREAM_METHOD = "stream"; //NOI18N
    private static final String STREAM_CLASS = "Stream"; //NOI18N

    public ELStreamCompletionItem(int anchorOffset) {
        this.setKind(ElementKind.METHOD);
        setAnchorOffset(anchorOffset);
    }

    @Override
    public ElementHandle getElement() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    @Override
    public String getName() {
        return STREAM_METHOD + "()"; //NOI18N
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.singleton(Modifier.PUBLIC);
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return STREAM_CLASS;
    }

    @Override
    public String getCustomInsertTemplate() {
        return STREAM_METHOD + "()"; //NOI18N
    }

}
