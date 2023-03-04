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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author vita
 */
public final class InstanceProviderLookup extends AbstractLookup {

    private InstanceContent content;
    private InstanceProvider<?> instanceProvider;
    
    private CompoundFolderChildren children;
    private PCL listener = new PCL();

    /** Creates a new instance of InstanceProviderLookup */
    public InstanceProviderLookup(String [] paths, InstanceProvider instanceProvider) {
        this(paths, instanceProvider, new InstanceContent());
    }
    
    private InstanceProviderLookup(String [] paths, InstanceProvider instanceProvider, InstanceContent content) {
        super(content);
        
        this.content = content;
        this.instanceProvider = instanceProvider;
        
        this.children = new CompoundFolderChildren(paths, true);
        this.children.addPropertyChangeListener(listener);
    }

    @Override
    protected void initialize() {
        rebuild();
    }
    
    private void rebuild() {
        List<FileObject> files = children.getChildren();
        Object instance = instanceProvider.createInstance(files);
        content.set(Collections.singleton(instance), null);
    }
    
    private class PCL implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
}
