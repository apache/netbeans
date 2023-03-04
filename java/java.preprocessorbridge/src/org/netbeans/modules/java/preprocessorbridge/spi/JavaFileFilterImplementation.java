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

package org.netbeans.modules.java.preprocessorbridge.spi;

import java.io.Reader;
import java.io.Writer;
import javax.swing.event.ChangeListener;

/**
 * This interface in a friend contract among the j2me project and java/source
 * module. The implementation preprocesses the java file content when it's red by the
 * java infrastructure if needed. From the performance reasons there can be just one
 * implementation of this interface for all sources in the project.
 * 
 * @author Tomas Zezula
 */
public interface JavaFileFilterImplementation {
        
    /**
     * Filters an {@link Reader} by the preprocessor.
     * Called when the file is read from the disk before its content is passed to javac.
     * PRE: The input parameter is never null
     * POST: The returned Reader is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param r {@link Reader} to be preprocessed
     * @return an preprocessed {@link Reader}
     */
    public Reader filterReader (Reader r);
    
    /**
     * Filters an input {@link CharSequence} by the preprocessor. From the performance reason
     * it's highly recommended to implement the method using decorator pattern.
     * Called before the content of the editor is passed to the javac.
     * PRE: The input parameter is never null
     * POST: The returned CharSequence is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param charSequence {@link CharSequence} to be preprocessed
     * @return an preprocessed {@link CharSequence}
     */
    public CharSequence filterCharSequence (CharSequence charSequence);
        
    /**
     * Filters an {@link Writer} by the preprocessor.
     * Called before the file is written to the disk.
     * PRE: The input parameter is never null
     * POST: The returned Writer is not null
     * Threading: The implementor is responsible for concurrent safety, tail call hand off protocol is suggested.
     * @param w {@link Writer} to be preprocessed
     * @return an preprocessed {@link Writer}
     */
    public Writer filterWriter (Writer w);
    
    /**
     * Adds an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be added
     */
    public void addChangeListener (ChangeListener listener);
    

    /**
     * Removes an {@link ChangeListener} to the {@link JavaFileFilterImplementation}
     * The implementor should fire a change when the rules for preprocessing has changed
     * and files should be rescanned.
     * @param listener to be removed
     */
    public void removeChangeListener (ChangeListener listener);
}
