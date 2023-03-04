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

import org.openide.filesystems.FileObject;

/**Allows creation of JavaSource instances for non-Java files.
 * Is expected to produce "virtual" Java source, which is then parsed
 * by the Java parser and used by selected Java features.
 *
 * @author Jan Lahoda, Dusan Balek
 */
public interface JavaSourceProvider {
    
    /**Create {@link PositionTranslatingJavaFileFilterImplementation} for given file.
     * 
     * @param fo file for which the implementation should be created
     * @return PositionTranslatingJavaFileFilterImplementation or null if which provider
     *         cannot create one for this file
     */
    public PositionTranslatingJavaFileFilterImplementation forFileObject(FileObject fo);
    
    /**"Virtual" Java source provider
     * 
     * Currently, only {@link JavaFileFilterImplementation#filterCharSequence},
     * {@link JavaFileFilterImplementation#getOriginalPosition}, 
     * {@link JavaFileFilterImplementation#getJavaSourcePosition} are called.
     */
    public static interface PositionTranslatingJavaFileFilterImplementation extends JavaFileFilterImplementation {
        /**Compute position in the document for given position in the virtual
         * Java source.
         *
         * @param javaSourcePosition position in the virtual Java Source
         * @return position in the document
         */
        public int getOriginalPosition(int javaSourcePosition);
        
        /**Compute position in the virtual Java source for given position
         * in the document.
         * 
         * @param originalPosition position in the document
         * @return position in the virtual Java source
         */
        public int getJavaSourcePosition(int originalPosition);
    }
}
