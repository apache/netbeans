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
package org.netbeans.modules.web.el.completion;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultCompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.el.ELElement;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Erno Mononen
 */
final class ELResourceBundleKeyCompletionItem extends DefaultCompletionProposal {

    private static final String ICON_PATH = "org/netbeans/modules/web/el/completion/resources/propertiesKey.gif";//NOI18N

    private final String key;
    private final String value;
    private final ELElement element;
    private final FileObject bundleFile;

    public ELResourceBundleKeyCompletionItem(String key, String value, ELElement element, FileObject bundleFile) {
        this.key = key;
        this.value = value;
        this.element = element;
        this.bundleFile = bundleFile;
    }

    @Override
    public String getName() {
        return key;
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.appendHtml("<font color='#ce7b00'>" + value + "</font>"); //NOI18N
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    @Override
    public int getSortPrioOverride() {
        return 20;
    }

    @Override
    public ElementHandle getElement() {
        return new ResourceBundleItemElementHandle();
    }

    @Override
    public ImageIcon getIcon() {
        return ImageUtilities.loadImageIcon(ICON_PATH, false);
    }

    private class ResourceBundleItemElementHandle extends ELElementHandle {

        @Override
        Documentation document(ParserResult info, Callable<Boolean> cancel) {
            return Documentation.create(key + "=" + "<font color='#ce7b00'>" + value + "</font>"); //NOI18N
        }

        @Override
        public FileObject getFileObject() {
            if (bundleFile != null) {
                return bundleFile;
            }
            return element.getSnapshot().getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return key;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

    }
}
