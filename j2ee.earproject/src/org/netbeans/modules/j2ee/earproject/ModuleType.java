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

package org.netbeans.modules.j2ee.earproject;

import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public enum ModuleType {
    
    WEB(NbBundle.getMessage(ModuleType.class, "CTL_WebModule")),
    EJB(NbBundle.getMessage(ModuleType.class, "CTL_EjbModule")),
    CLIENT(NbBundle.getMessage(ModuleType.class, "CTL_ClientModule"));
    
    private final String description;
    
    ModuleType(final String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /** Maps relative deployment descriptor's path to {@link ModuleType}. */
    private static final Map<String, ModuleType> DEFAULT_DD = new HashMap<String, ModuleType>();
    
    static {
        DEFAULT_DD.put("web/WEB-INF/web.xml", ModuleType.WEB); // NOI18N
        DEFAULT_DD.put("src/conf/ejb-jar.xml", ModuleType.EJB); // NOI18N
        DEFAULT_DD.put("src/conf/application-client.xml", ModuleType.CLIENT); // NOI18N
    }
    
    /**
     * Detects Enterprise Application modules in the <code>appRoot</code>'s
     * subfolders recursively.
     *
     * @param folder root folder - typically enterprise application folder
     * @return map of FileObject to ModuleType entries
     */
    public static Map<FileObject, ModuleType> detectModules(final FileObject appRoot) {
        Map<FileObject, ModuleType> descriptors =
                new HashMap<FileObject, ModuleType>();
        // do detection for each subdirectory
        for (FileObject subprojectRoot : appRoot.getChildren()) {
            if (subprojectRoot.isFolder()) {
                ModuleType type = ModuleType.detectModuleType(subprojectRoot);
                if (type != null) {
                    descriptors.put(subprojectRoot, type);
                }
            }
        }
        return descriptors;
    }
    
    /**
     * Tries to detect Enterprise Application module's type in the given folder.
     *
     * @param folder folder which possibly containing module
     * @return <code>null</code> if no module were detected; instance otherwise
     */
    public static ModuleType detectModuleType(final FileObject moduleRoot) {
        ModuleType result = null;
        for (Map.Entry<String, ModuleType> entry : DEFAULT_DD.entrySet()) {
            FileObject ddFO = moduleRoot.getFileObject(entry.getKey());
            if (ddFO != null && ddFO.isData()) { // deployment descriptor detected
                result = entry.getValue();
            }
        }
        return result;
    }
    
}
