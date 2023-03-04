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

package org.openidex.search;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Marian Petras
 */
class CompoundSearchIterator implements Iterator<FileObject> {

    /** */
    private final SearchInfo[] elements;
    /** */
    private int elementIndex;
    /** */
    private Iterator<FileObject> elementIterator;
    /** */
    private FileObject nextObject;
    /** */
    private boolean upToDate;

    /**
     * Creates a new instance of <code>CompoundSearchIterator</code>.
     *
     * @param  elements  elements of the compound iterator
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is <code>null</code>
     */
    CompoundSearchIterator(SearchInfo[] elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        if (elements.length == 0) {
            this.elements = null;
            elementIndex = 0;
            upToDate = true;                //hasNext() returns always false
        } else {
            this.elements = elements;
            elementIterator = Utils.getFileObjectsIterator(elements[elementIndex = 0]);
            upToDate = false;
        }
    }

    /**
     */
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return (elements != null) && (elementIndex < elements.length);
    }

    /**
     */
    public FileObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        upToDate = false;
        return nextObject;
    }
    
    /**
     */
    private void update() {
        assert upToDate == false;
        
        while (!elementIterator.hasNext()) {
            elements[elementIndex] = null;
            
            if (++elementIndex == elements.length) {
                break;
            }
            
            elementIterator = Utils.getFileObjectsIterator(elements[elementIndex]);
        }
        
        if (elementIndex < elements.length) {
            nextObject = elementIterator.next();
        } else {
            elementIterator = null;
            nextObject = null;
        }
        
        upToDate = true;
    }

    /**
     * @exception  java.lang.UnsupportedOperationException
     *             always - this operation is not supported
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
