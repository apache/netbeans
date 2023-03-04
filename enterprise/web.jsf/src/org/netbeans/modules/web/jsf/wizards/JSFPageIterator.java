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

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.wizards.FileType;
import org.netbeans.modules.web.wizards.PageIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** A template wizard iterator for new JSF JSP page
 *
 * @author Po-Ting Wu
 */

public class JSFPageIterator extends PageIterator {

    private static final long serialVersionUID = 1L;

    public JSFPageIterator(FileType fileType) {
        super(fileType);
    }

    public static JSFPageIterator createJspIterator() {
        return new JSFPageIterator(FileType.JSP);
    }

    public static JSFPageIterator createJsfIterator() {
        return new JSFPageIterator(FileType.JSF);
    }

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        Set<DataObject> dobj = super.instantiate(wiz);

        // Add JSF framework here
        FileObject fileObject = ((DataObject) dobj.toArray()[0]).getPrimaryFile();
        WebModule webModule = WebModule.getWebModule(fileObject);
        // issue #221464 - do not try to extend project if it's not Web Project
        if (webModule != null) {
            if (!JSFConfigUtilities.hasJsfFramework(fileObject)) {
                JSFConfigUtilities.extendJsfFramework(fileObject, false);
            }
        }

        return dobj;
    }
}
