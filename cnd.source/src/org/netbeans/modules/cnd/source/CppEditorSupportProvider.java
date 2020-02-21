/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
