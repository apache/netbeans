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
package org.netbeans.modules.javascript2.debug;

import java.beans.PropertyChangeListener;
import java.net.URL;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin
 */
public interface EditorLineHandler {
    
    public static final String PROP_LINE_NUMBER = "lineNumber";
    
    /**
     * Get the associated {@link FileObject}, if any.
     * @return The FileObject or <code>null</code>
     */
    FileObject getFileObject();
    
    /**
     * Get the associated {@link URL}.
     * @return The URL.
     */
    URL getURL();
    
    /**
     * Get a 1-based line number
     * @return the line number
     */
    int getLineNumber();
    
    /**
     * Set a 1-based line number
     * @param lineNumber 
     */
    void setLineNumber(int lineNumber);
    
    void dispose();
    
    void addPropertyChangeListener(PropertyChangeListener pchl);
    
    void removePropertyChangeListener(PropertyChangeListener pchl);
    
}
