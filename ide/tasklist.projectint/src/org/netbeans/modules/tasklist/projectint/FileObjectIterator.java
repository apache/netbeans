/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.projectint;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Iterates all files and sub-folders under the given root folders.
 * 
 * @author S. Aubrecht
 */
class FileObjectIterator implements Iterator<FileObject> {
    
    private Collection<FileObject> roots;
    
    private Iterator<FileObject> rootsIterator;
    private Enumeration<? extends FileObject> rootChildrenEnum;
    
    /** Creates a new instance of FileObjectIterator */
    public FileObjectIterator( Collection<FileObject> roots ) {
        this.roots = roots;
    }
    
    public boolean hasNext() {
        if( null == rootsIterator ) {
            rootsIterator = roots.iterator();
            return rootsIterator.hasNext();
        }
        return (null != rootChildrenEnum && rootChildrenEnum.hasMoreElements()) 
                || rootsIterator.hasNext();
    }
    
    public FileObject next() {
        FileObject result = null;
        if( null == rootChildrenEnum || !rootChildrenEnum.hasMoreElements() ) {
            if( rootsIterator.hasNext() ) {
                result = rootsIterator.next();
                rootChildrenEnum = result.getChildren( true );
            } else {
                throw new NoSuchElementException();
            }
        } else {
            result = rootChildrenEnum.nextElement();
        }
        return result;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private boolean isUnderRoots( FileObject fo ) {
        for( FileObject root : roots ) {
            if( FileUtil.isParentOf( root, fo ) )
                return true;
        }
        return false;
    }
}
