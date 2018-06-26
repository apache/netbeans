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

package org.netbeans.modules.tomcat5.util;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 * Repository whose getDefaultFileSystem returns a writeable FS containing
 * the layer of the Tomcat, J2eeserver and the Database Explorer module. It is 
 * put in the default lookup, thus it is returned by Repository.getDefault().
 *
 * @author sherold
 */
public class RepositoryImpl extends Repository {
    
    private XMLFileSystem system;
    
    public RepositoryImpl() {
        super(createDefFs());
    }
    
    private static FileSystem createDefFs() {
        try
        {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem layerFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/tomcat5/resources/layer.xml"));
            FileSystem j2eeserverFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/tomcat5/util/fake-j2eeserver-layer.xml"));
            FileSystem dbFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/db/resources/mf-layer.xml"));
            return new MultiFileSystem(new FileSystem[] { writeFs, layerFs, j2eeserverFs, dbFs});
        } catch (SAXException e) {
            return null;
        }
    }
}
