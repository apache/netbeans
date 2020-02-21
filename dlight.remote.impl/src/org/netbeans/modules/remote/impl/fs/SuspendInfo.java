/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.remote.impl.fs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.util.NotImplementedException;

/**
 * Stores information concerning suspended files and subdirectories
 * inside a suspended directory and its children.
 * When you suspend a directory, all child directories become suspended as well,
 * but SuspendInfo corresponds exactly to the directory suspend was called on.
 */
public class SuspendInfo {
    
    private final RemoteDirectory owner;
    private final Object lock = new Object();
    
    private final Map<RemoteDirectory, List<RemoteFileObjectBase>> dummyChildren;
    private final Set<RemoteFileObjectBase> suspended;

    public SuspendInfo(RemoteDirectory owner) {
        this.owner = owner;
        this.dummyChildren = new HashMap<>();
        this.suspended = new HashSet<>();
    }

    /** 
     * Gets *dummy* children for the given directory.
     * Note that there might be non-dummy children as well
     */
    public RemoteFileObject[] getDirectDummyChildren(RemoteDirectory dir) {
        synchronized(lock) {
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            RemoteFileObject[] result = new RemoteFileObject[children.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = children.get(i).getOwnerFileObject();
            }
            return result;
        }
    }
    
    /** 
     * Gets *dummy* child of the given directory with the given name.
     * Note that there might be non-dummy children as well
     */
    public RemoteFileObjectBase getDirectDummyChild(RemoteDirectory dir, String nameExt) {
        synchronized(lock) {
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            if (children != null) {
                for (RemoteFileObjectBase fo : children) {
                    if (fo.getNameExt().equals(nameExt)) {
                        return fo;
                    }
                }
            }
        }
        return null;
    }
    
    private void addDummyChildImpl(RemoteDirectory dir, RemoteFileObjectBase fo) {
        synchronized (lock) {
            suspended.add(fo);            
            List<RemoteFileObjectBase> children = dummyChildren.get(dir);
            if (children == null) {
                children = new ArrayList<>();
                dummyChildren.put(dir, children);
            }
            children.add(fo);
        }
    }
    
    /** 
     * Adds a dummy child to its parent.
     * automatically calls addSuspendsd() as well 
     */
    public void addDummyChild(RemotePlainFile fo) {
        addDummyChildImpl(fo.getParentImpl(), fo);
    }
    
    /** 
     * Adds a dummy child to its parent.
     * automatically calls addSuspendsd() as well 
     */
    public void addDummyChild(RemoteDirectory fo) {        
        addDummyChildImpl(fo.getParentImpl(), fo);
    }
    
    /** 
     * Gets all suspedned files.
     */
    public List<RemoteFileObjectBase> getAllSuspended() {
        synchronized (lock) {
            return new ArrayList<>(suspended);
        }
    }
    
    /** 
     * Adds a file to suspended list.
     */
    public void addSuspended(RemotePlainFile fo) {
        synchronized (lock) {
            suspended.add(fo);
        }
    }    
    
    public void dispose() {
        
    }
}
