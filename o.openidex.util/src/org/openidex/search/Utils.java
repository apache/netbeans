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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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

package org.openidex.search;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

 /**
  * Search API utility class.
  *
  * @since org.openidex.util/3 3.20
  * @author  Marian Petras
  * @author  kaktus
  */
public final class Utils {

    private Utils() {
    }

    /**
     */
    static SearchInfo getSearchInfo(Node node) {
        /* 1st try - is the SearchInfo object in the node's lookup? */
        SearchInfo info = node.getLookup().lookup(SearchInfo.class);
        if (info != null) {
            return info;
        }

        /* 2nd try - does the node represent a DataObject.Container? */
        DataFolder container = node.getLookup().lookup(DataFolder.class);
        if (container == null) {
            return null;
        } else {
            return SearchInfoFactory.createSearchInfo(
                    container.getPrimaryFile(),
                    true,                       //recursive
                    new FileObjectFilter[] {
                            SearchInfoFactory.VISIBILITY_FILTER });
        }
    }

    /**
     * Returns <code>Iterator</code> of <code>FileObject</code>'s for the provided <code>SearchInfo</code>.
     * If provided <code>SearchInfo</code> object is implementation of <code>SearchInfo.Files</code> interface
     * then the result of method <code>SearchInfo.Files.filesToSearch</code> is returned. Otherwise the objects
     * are getting from the <code>SearchInfo.objectsToSearch</code> method.
     *
     * @param si <code>SearchInfo</code> object to return the iterator for
     * @return iterator which iterates over <code>FileObject</code>s
     * @since org.openidex.util/3 3.20
     */
    public static Iterator<FileObject> getFileObjectsIterator(SearchInfo si){
        if (si instanceof SearchInfo.Files){
            return ((SearchInfo.Files)si).filesToSearch();
        }else{
            Set<FileObject> set = new HashSet<FileObject>();
            for(Iterator<DataObject> iter = si.objectsToSearch(); iter.hasNext();){
                set.add(iter.next().getPrimaryFile());
            }
            return set.iterator();
        }
    }

    static Iterator<DataObject> toDataObjectIterator(Iterator<FileObject> itFO){
        Set<DataObject> set = new HashSet<DataObject>();
        while(itFO.hasNext()){
            try {
                set.add(DataObject.find(itFO.next()));
            } catch (DataObjectNotFoundException ex){}
        }
        return set.iterator();
    }
    
}
