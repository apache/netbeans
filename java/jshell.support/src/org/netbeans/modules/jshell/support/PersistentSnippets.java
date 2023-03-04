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
package org.netbeans.modules.jshell.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/**
 * Connects the editor with persistent snippets store. The store may reside 
 * in the project, or in the IDE's configuration, or both.
 * <p/>
 * An instance of PersistentSnippets can be obtained from 
 * @author sdedic
 */
public interface PersistentSnippets {
    /**
     * Returns a collection of FileObjects which represent snippets saved
     * in a form of a java class source file. The class name will give name of the 
     * snippet. The javadoc description will be presented as a short description.
     * 
     * @return FileObjects which store sources for saved stuff
     */
    public Collection<FileObject>   getSavedClasses(String folderName);
    
    /**
     * Creates a folder to store the class(es). If name exists, the existing name
     * will be returned. Pass {@code null} for the root folder
     * 
     * @param name 
     * @return folder to save classes into.
     */
    public FileObject               savedClassFolder(String name);
    
    /**
     * Saves the class into the storage under the given name
     * @param name
     * @param description
     * @param contents
     * @return 
     */
    public FileObject               saveClass(
                                        String name, 
                                        String description, 
                                        InputStream contents) throws IOException;
    
    public String                   getDescription(FileObject saved);
    public void                     setDescription(FileObject saved, String desc);
    
    public void addChangeListener(ChangeListener l);
    public void removeChangeListener(ChangeListener l);
    
    public boolean                  isValid();
    
    public Collection<FileObject>   startupSnippets(String runAction);
}
