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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.util;

import org.netbeans.modules.versioning.spi.VersioningSupport;

import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Collection of Files that has special contracts for add, remove and contains methods, see below.
 * 
 * @author Maros Sandor
 */
public class FileCollection {
    
    private static final char FLAT_FOLDER_MARKER = '*';
    
    private final Set<File> storage = new HashSet<File>(1);
    
    public synchronized void load(Preferences prefs, String key) {
        List<String> paths = Utils.getStringList(prefs, key);
        storage.clear();        
        for (String path : paths) {
            if (path.charAt(0) == FLAT_FOLDER_MARKER) {
                storage.add(VersioningSupport.getFlat(path.substring(1)));        
            } else {
                storage.add(new File(path));        
            }
        }
    }

    public synchronized void save(Preferences prefs, String key) {
        List<String> paths = new ArrayList<String>(storage.size());
        for (File file : storage) {
            if (VersioningSupport.isFlat(file)) {
                paths.add(FLAT_FOLDER_MARKER + file.getAbsolutePath());        
            } else {
                paths.add(file.getAbsolutePath());        
            }
        }
        Utils.put(prefs, key, paths);
    }

    /**
     * A file is contained in the collection either if it is in the colelction itself or there is any of its parents. 
     * 
     * @param file a file to query
     * @return true if the file is contained in the collection, false otherwise
     */
    public synchronized boolean contains(File file) {
        for (File element : storage) {
            if (Utils.isAncestorOrEqual(element, file)) return true;
        }
        return false;
    }

    /**
     * Adds a file to the collection. If any of its parent files is already in the collection, the file is NOT added.
     * All children of the supplied file are removed from the collection.
     * 
     * @param file a file to add
     */
    public synchronized void add(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isAncestorOrEqual(element, file)) return;
            if (Utils.isAncestorOrEqual(file, element)) {
                i.remove();
            }
        }
        storage.add(file);
    }

    /**
     * Removes a file from the collection. This method also removes all its parents and also all its children.
     * 
     * @param file a file to remove
     */
    public synchronized void remove(File file) {
        for (Iterator<File> i = storage.iterator(); i.hasNext(); ) {
            File element = i.next();
            if (Utils.isAncestorOrEqual(element, file) || Utils.isAncestorOrEqual(file, element)) {
                i.remove();
            }
        }
    }
}
