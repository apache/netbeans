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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.versioning.core;

import java.net.MalformedURLException;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.spi.queries.CollocationQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Delegates the work to the owner of files in query.
 * 
 * @author Maros Sandor
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.CollocationQueryImplementation2.class, position=50)
public class VcsCollocationQueryImplementation implements CollocationQueryImplementation2 {

    @Override
    public boolean areCollocated(URI file1, URI file2) {
        VCSFileProxy proxy1 = Utils.toFileProxy(file1);
        VCSFileProxy proxy2 = Utils.toFileProxy(file2);
        
        if(proxy1 == null || proxy2 == null) return false;
        VersioningSystem vsa = VersioningManager.getInstance().getOwner(proxy1);
        VersioningSystem vsb = VersioningManager.getInstance().getOwner(proxy2);
        if (vsa == null || vsa != vsb) return false;
        
        CollocationQueryImplementation2 cqi = vsa.getCollocationQueryImplementation();
        return cqi != null && cqi.areCollocated(file1, file2);
    }

    @Override
    public URI findRoot(URI file) {
        VCSFileProxy proxy = Utils.toFileProxy(file);
        if(proxy != null) {
            VersioningSystem system = VersioningManager.getInstance().getOwner(proxy);
            CollocationQueryImplementation2 cqi = system != null ? system.getCollocationQueryImplementation() : null;
            return cqi != null ? cqi.findRoot(file) : null;
        }
        return null;
    }
    
}        
