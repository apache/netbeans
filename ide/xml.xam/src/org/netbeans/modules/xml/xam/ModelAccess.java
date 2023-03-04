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

package org.netbeans.modules.xml.xam;

import java.io.IOException;
import javax.swing.event.UndoableEditListener;

/**
 * Access to the underlying structure of the model.
 *
 * @author Nam Nguyen
 */

public abstract class ModelAccess {
    
    public abstract void addUndoableEditListener(UndoableEditListener listener);
    public abstract void removeUndoableEditListener(UndoableEditListener listener);
    
    public abstract void prepareForUndoRedo();
    public abstract void finishUndoRedo();
    
    public void prepareSync() { }
    public abstract Model.State sync() throws IOException;
    
    public abstract void flush();


    private boolean autoSync = true;
    public boolean isAutoSync() {
        return autoSync;
    }
    public void setAutoSync(boolean value) {
        autoSync = value;
    }
    
    /**
     * Returns length in milliseconds since last edit if the model source buffer 
     * is dirty, or 0 if the model source is not dirty.  Class of domain model 
     * implementations should provide override.
     */
    public long dirtyIntervalMillis() {
        return 0;
    }
    /**
     * Unset mark for dirty source buffer.
     */
    public void unsetDirty() {
        // subclass need to override
    }
    
}
