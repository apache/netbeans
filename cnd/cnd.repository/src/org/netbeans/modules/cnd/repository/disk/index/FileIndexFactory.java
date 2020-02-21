/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.repository.disk.index;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public class FileIndexFactory extends AbstractObjectFactory {
    private static FileIndexFactory theFactory;
    private static final Object lock = new Object();
    
    /** Creates a new instance of FileIndexFactory */
    protected FileIndexFactory() {
    }
    
    public static FileIndexFactory getDefaultFactory() {
        synchronized (lock) {
            if (theFactory == null) {
                theFactory = new FileIndexFactory();
            }
        }
        return theFactory;
    }
    
   public void writeIndex(final FileIndex anIndex, final RepositoryDataOutput aStream) throws IOException {
        assert anIndex instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)anIndex, aStream);
    }
    
    public FileIndex readIndex(final RepositoryDataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out instanceof FileIndex;
        return (FileIndex)out;
    }    

    @Override
    protected short getHandler(final Object object) {
        final short aHandle;
        
        if (object instanceof SimpleFileIndex) {
            aHandle = FILE_INDEX_SIMPLE;
        } else if (object instanceof CompactFileIndex) {
            aHandle = FILE_INDEX_COMPACT;
        } else {
            throw new IllegalArgumentException("The Index is an instance of the unknown final class " + object.getClass().getName());  // NOI18N
        }
        return aHandle;
    }

    @Override
    protected SelfPersistent createObject(final short handler, final RepositoryDataInput stream) throws IOException {
        final SelfPersistent anIndex;
        
        switch (handler) {
            case FILE_INDEX_SIMPLE:
                anIndex = new SimpleFileIndex(stream);
                break;
            case FILE_INDEX_COMPACT:
                anIndex = new CompactFileIndex(stream);
                break;
            default:
                throw new IllegalArgumentException("Unknown hander for index was provided: " + handler);  // NOI18N
        }
        
        return anIndex;
    }
    
    private static final int FIRST_INDEX        = 0;
    private static final int FILE_INDEX_SIMPLE  = FIRST_INDEX;
    private static final int FILE_INDEX_COMPACT = FILE_INDEX_SIMPLE + 1;
    public static final int LAST_INDEX          = FILE_INDEX_COMPACT;
    
}
