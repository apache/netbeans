/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
