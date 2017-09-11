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
package org.netbeans.modules.xsl;


import org.openide.filesystems.*;
import org.openide.loaders.*;

import org.netbeans.modules.xml.XMLDataLoader;
import org.openide.util.NbBundle;

/**
 * XSL object loader. It is mime type based.
 *
 * @author Libor Kramolis
 */
public final class XSLDataLoader extends UniFileLoader {

    private static final long serialVersionUID = 6494980346565290872L;

    /** Creates a new instance of SchemaLoader */
    public XSLDataLoader() {
        super ("org.netbeans.modules.xsl.XSLDataObject"); // NOI18N
    }

    /** Does initialization. Initializes display name,
     * extension list and the actions. */
    protected void initialize () {
        super.initialize();
        
        ExtensionList ext = getExtensions();
        ext.addMimeType (XSLDataObject.MIME_TYPE);
        setExtensions (ext);
    }
    
    protected String actionsContext() {
        return "Loaders/application/xslt+xml/Actions/"; // NOI18N
    }
    
    /**
     * Lazy init name.
     */
    protected String defaultDisplayName () {
        return NbBundle.getMessage(XSLDataLoader.class, "NAME_XSLDataLoader");
    }
    
    /** Creates the right primary entry for given primary file.
     *
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new XMLDataLoader.XMLFileEntry (obj, primaryFile);  //adds smart templating
    }

    /** Creates the right data object for given primary file.
     * It is guaranteed that the provided file is realy primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
            throws DataObjectExistsException, java.io.IOException {
        return new XSLDataObject (primaryFile, this);
    }
    
}
