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
package org.netbeans.modules.csl.core;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

public class CslCamelCaseInterceptor implements CamelCaseInterceptor {

    @Override
    public boolean beforeChange(MutableContext context) throws BadLocationException {
        return false;
    }

    @Override
    public void change(MutableContext context) throws BadLocationException {
        int offset = context.getOffset();
        Document doc = context.getDocument();

        KeystrokeHandler bc = UiUtils.getBracketCompletion(doc, offset);
        if (bc != null) {
            int nextOffset = bc.getNextWordOffset(doc, offset, context.isBackward());
            context.setNextWordOffset(nextOffset);
        }
    }

    @Override
    public void afterChange(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(MutableContext context) {
    }

    @MimeRegistrations(value = {
        @MimeRegistration(mimeType = "", service = CamelCaseInterceptor.Factory.class)})
    public static class Factory implements CamelCaseInterceptor.Factory {

        @Override
        public CamelCaseInterceptor createCamelCaseInterceptor(MimePath mimePath) {
            return new CslCamelCaseInterceptor();
        }
    }
}
