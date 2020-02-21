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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.io.IOException;

import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;

import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataNode;

import org.openide.cookies.ViewCookie;

import org.openide.nodes.*;

public class RtcDataObject extends MultiDataObject implements ViewCookie {

    private RtcModel model;

    // I initially tried ".rtc.xml", but it was too much trouble considering
    // that most of NB datasystems works best with a single suffix.

    public static final String EXTENSION = "rtc";	// NOI18N

    public RtcDataObject(FileObject fo, RtcDataLoader loader)
	throws DataObjectExistsException, IOException {

	super(fo, loader);
	if (Log.Rtc.debug)
	    System.out.printf("RtcDataObject.<init>()\n"); // NOI18N

	CookieSet cookies = getCookieSet();
	cookies.add(this);
    }

    private final static String ICON_RESOURCE =
	"org/netbeans/modules/cnd/debugger/common2/icons/Refresh.gif"; //NOI18N

    // override DataObject
    protected Node createNodeDelegate() {
	DataNode node = new DataNode(this, Children.LEAF);
	node.setIconBaseWithExtension(ICON_RESOURCE);
	node.setShortDescription(Catalog.get("RTCExperiment"));
	return node;
    }

    RtcModel getModel() {
	return model;
    }

    // interface ViewCookie
    public void view() {
	if (Log.Rtc.debug)
	    System.out.printf("RtcDataObject.view()\n"); // NOI18N

	FileObject fo = getPrimaryEntry().getFile();

	model = new RtcModel(fo.getPath());

	try {
	    model.read(fo);
	} catch (IOException e) {
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
	    return;
	}

	RtcViewer viewer = new RtcViewer();
	RtcView view = new RtcView(viewer, null, null);
	view.setModel(model);
	view.refresh();

	// cross-connect view and TopComponent
	viewer.setView(view);

	viewer.open();
	viewer.requestActive();
    }
}
