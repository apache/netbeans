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

package  org.netbeans.modules.web.taglib;

/**
 *
 * @author  Milan Kuchtiak
 * @version 1.0
 */

import java.io.IOException;

import org.openide.loaders.UniFileLoader;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** Data loader which recognizes .tld files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
*/
public final class TLDLoader extends UniFileLoader {
    
    public static final String tldExt = "tld"; //NOI18N
    public static final String TLD_MIMETYPE = "text/x-tld"; //NOI18N
    
    private static final long serialVersionUID = -7367746798495347598L;
    
    /** Constructor */
    public TLDLoader() {
	super("org.netbeans.modules.web.taglib.TLDDataObject"); // NOI18N
    }
    
     /** Does initialization. Initializes display name,
     * extension list and the actions. */
    @Override
    protected void initialize () {
    	super.initialize();
	ExtensionList ext = new ExtensionList();
	ext.addExtension(tldExt);
	setExtensions(ext);
        getExtensions().addMimeType(TLD_MIMETYPE);
    }

    protected MultiDataObject createMultiObject(final FileObject fo)
	throws IOException {
	MultiDataObject obj = new TLDDataObject(fo, this);
	return obj;
    }

    @Override
    protected String defaultDisplayName () {
	return NbBundle.getMessage (TLDLoader.class, "TLD_loaderName");
    }
    
    @Override
    protected String actionsContext() {
        return "Loaders/text/x-tld/Actions/"; // NOI18N
    }
    
}
