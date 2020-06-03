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
package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 */
public class QtProjectDataObject extends MultiDataObject {

    public QtProjectDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(MIMENames.QTPROJECT_MIME_TYPE, true);
        getCookieSet().add(new QMakeExecSupport(getPrimaryEntry()));
    }

    @MultiViewElement.Registration(
        displayName="#Source", // NOI18N
        iconBase="org/netbeans/modules/cnd/loaders/QtProjectIcon.png", // NOI18N
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType=MIMENames.QTPROJECT_MIME_TYPE,
        preferredID="qtptoject.source", // NOI18N
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected Node createNodeDelegate() {
        return new QMakeDataNode(this);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    private static class QMakeDataNode extends DataNode {
        /** Construct the DataNode */
        public QMakeDataNode(QtProjectDataObject obj) {
            super(obj, Children.LEAF, obj.getLookup());
        }

        /** Get the support for methods which need it */
        private QMakeExecSupport getSupport() {
            return getCookie(QMakeExecSupport.class);
        }

        /** Create the properties sheet for the node */
        @Override
        protected Sheet createSheet() {
            // Just add properties to default property tab (they used to be in a special 'Building Tab')
            Sheet defaultSheet = super.createSheet();
            Sheet.Set defaultSet = defaultSheet.get(Sheet.PROPERTIES);
            getSupport().addProperties(defaultSet);
            return defaultSheet;
        }
    }
}
