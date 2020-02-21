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

package org.netbeans.modules.cnd.source;

import java.io.IOException;
import org.netbeans.modules.cnd.spi.CndCookieProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;
import org.openide.util.lookup.InstanceContent.Convertor;

/**
 */
public final class CppEditorSupportProvider extends CndCookieProvider {
    static final CppEditorSupportFactory staticFactory = new CppEditorSupportFactory();
    static final CppSaveAsFactory saveAsStaticFactory = new CppSaveAsFactory();

    @Override
    public void addLookup(InstanceContentOwner icOwner) {
        SourceDataObject sdao = (SourceDataObject) icOwner;
        icOwner.getInstanceContent().add(sdao, staticFactory);
    }

    private static class CppEditorSupportFactory implements Convertor<SourceDataObject, CppEditorSupport> {
        public CppEditorSupportFactory() {
        }

        @Override
        public CppEditorSupport convert(SourceDataObject obj) {
            Node nodeDelegate = null;
            if (obj.isValid()) {
                nodeDelegate = obj.getNodeDelegate();
            }
            return new CppEditorSupport(obj, nodeDelegate);
        }

        @Override
        public Class<? extends CppEditorSupport> type(SourceDataObject obj) {
            return CppEditorSupport.class;
        }

        @Override
        public String id(SourceDataObject obj) {
            return CppEditorSupport.class.getName()+obj.getPrimaryFile().getPath();
        }

        @Override
        public String displayName(SourceDataObject obj) {
            return id(obj);
        }
    }

    private static class CppSaveAsFactory implements Convertor<SourceDataObject, SaveAsCapable> {
        public CppSaveAsFactory() {
        }

        @Override
        public SaveAsCapable convert(final SourceDataObject obj) {
            return new SaveAsCapableImpl(obj);
        }

        @Override
        public Class<? extends SaveAsCapable> type(SourceDataObject obj) {
            return SaveAsCapable.class;
        }

        @Override
        public String id(SourceDataObject obj) {
            return "CndSaveAsCapable"+obj.getPrimaryFile().getPath(); // NOI18N
        }

        @Override
        public String displayName(SourceDataObject obj) {
            return id(obj);
        }

    }

    private static class SaveAsCapableImpl implements SaveAsCapable {

        private final SourceDataObject obj;

        public SaveAsCapableImpl(SourceDataObject obj) {
            this.obj = obj;
        }

        @Override
        public void saveAs( FileObject folder, String fileName ) throws IOException {
            CppEditorSupport ces = obj.getLookup().lookup(CppEditorSupport.class);
            if (ces != null) {
                ces.saveAs(folder, fileName);
            }
        }
    }
}
