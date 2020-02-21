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
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 */
public class QtResourceDataObject extends MultiDataObject {

    public QtResourceDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(MIMENames.QT_RESOURCE_MIME_TYPE, true);
    }

    @MultiViewElement.Registration(
        displayName="#Source", // NOI18N
        iconBase="org/netbeans/modules/cnd/loaders/QtResourceIcon.png", // NOI18N
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType=MIMENames.QT_RESOURCE_MIME_TYPE,
        preferredID="qtresource.source", // NOI18N
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

}
