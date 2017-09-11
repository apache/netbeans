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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Manages extensible (freeform) metadata in an Ant-based project.
 * @author Jesse Glick
 */
final class ExtensibleMetadataProviderImpl implements AuxiliaryConfiguration, CacheDirectoryProvider {

    /**
     * Relative path from project directory to the required private cache directory.
     */
    private static final String CACHE_PATH = "nbproject/private"; // NOI18N
    
    private final AntProjectHelper helper;
    
    ExtensibleMetadataProviderImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public FileObject getCacheDirectory() throws IOException {
        return FileUtil.createFolder(helper.getProjectDirectory(), CACHE_PATH);
    }
    
    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        return helper.getConfigurationFragment(elementName, namespace, shared);
    }
    
    public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
        if (fragment.getNamespaceURI() == null || fragment.getNamespaceURI().length() == 0) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (fragment.getLocalName().equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                fragment.getNamespaceURI().equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        helper.putConfigurationFragment(fragment, shared);
    }
    
    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (elementName.equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                namespace.equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        return helper.removeConfigurationFragment(elementName, namespace, shared);
    }
    
}
