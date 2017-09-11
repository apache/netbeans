/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.api;

import org.netbeans.modules.web.common.spi.DependentFileQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * An API entry point to query dependency relationships between files. 
 * None SPI is really implemented at this stage - see comments below.
 */
public class DependentFileQuery {

    private static Lookup.Result<DependentFileQueryImplementation> lookup = 
            Lookup.getDefault().lookupResult(DependentFileQueryImplementation.class);
    
    /**
     * Does "master" FileObject depends on "dependent" FileObject? Typical usage will
     * be to answer questions like "does foo.html depends on style.css?"
     */
    public static boolean isDependent(FileObject master, FileObject dependent) {
        if (dependent.equals(master)) {
            return true;
        }
        for (DependentFileQueryImplementation impl : lookup.allInstances()) {
            if (impl.isDependent(master, dependent) == DependentFileQueryImplementation.Dependency.YES) {
                return true;
            }
        }
        
        return false;
    }
    
    /*
     * Find collection of files whose change affects changes in the view of <code>fileObject</code>.
     * F.e. if <code>fileObject</code> is html file then its view is affected 
     * by changes in included JS or CSS files. Related to {@link #isDependent} .
     * @param fileObject
     * @return collection of files from which <code>fileObject</code> depends on
     *
    public static Set<FileObject> getDependent(FileObject fileObject) {
        Collection<? extends DependentFileQueryImplementation> impls = lookup.allInstances();
        if ( impls.isEmpty() ){
            return Collections.emptySet();
        }
        if ( impls.size() == 1){
            return impls.iterator().next().getDependent(fileObject);
        }
        HashSet<FileObject> result = new HashSet<FileObject>();
        for( DependentFileQueryImplementation impl : lookup.allInstances()){
            Set<FileObject> set = impl.getDependent(fileObject);
            result.addAll(set);
        }
        return result;
    }*/
}
