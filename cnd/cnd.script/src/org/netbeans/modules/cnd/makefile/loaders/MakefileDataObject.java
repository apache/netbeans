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

package org.netbeans.modules.cnd.makefile.loaders;


import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makefile.parser.MakefileTargetProviderImpl;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *  Represents a Makefile object in the Repository.
 */
public class MakefileDataObject extends MultiDataObject {

    /** Serial version number */
    static final long serialVersionUID = -5853234372530618782L;

    /** Constructor for this class */
    public MakefileDataObject(FileObject pf, MakefileDataLoader loader)
		throws DataObjectExistsException {
	super(pf, loader);

        registerEditor(MIMENames.MAKEFILE_MIME_TYPE, true);
        CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
	cookies.add(new MakeExecSupport(getPrimaryEntry()));
        cookies.add(new MakefileTargetProviderImpl(getPrimaryFile()));
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Messages("Source=&Source")
    @MultiViewElement.Registration(
        displayName="#Source", // NOI18N
        iconBase="org/netbeans/modules/cnd/script/resources/MakefileDataIcon.gif", // NOI18N
        persistenceType=TopComponent.PERSISTENCE_ONLY_OPENED,
        mimeType=MIMENames.MAKEFILE_MIME_TYPE,
        preferredID="makefile.source", // NOI18N
        position=1
    )
    public static MultiViewEditorElement createMultiViewEditorElement(Lookup context) {
        return new MultiViewEditorElement(context);
    }

    /** Create the delegate node */
    @Override
    protected Node createNodeDelegate() {
	return new MakefileDataNode(this);
    }
}
