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

package org.netbeans.spi.editor.mimelookup;

import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Provider of the instance of the given class.
 * <br>
 * The provider gets a list of files which it transfers
 * into one instance of the class for which it's declared.
 *
 * <p>
 * For example there can be an instance provider
 * of actions for the editor popup. The file object names
 * of the actions declared in the layer can be of two forms:<ul>
 *   <li><i>MyAction.instance</i> are actions instances declaration files.</li>
 *   <li><i>reformat-code</i> are editor actions names.</li>
 * </ul>
 * <br>
 * The instance provider translates all the file objects to actions
 * which it returns as a collection in some sort of collection-like class
 * e.g.<pre>
 * interface PopupActions {
 *
 *     List&lt;Action> getActions();
 *
 * }</pre>
 *
 * @param <T> type of instance which will be created
 */
public interface InstanceProvider<T> {
    
    /**
     * Create an instance of the class for which this
     * instance provider is declared in {@link Class2LayerFolder}.
     *
     * @param fileObjectList non-null list of the file objects
     *  collected from the particular layer folder and possibly
     *  the inherited folders.
     * @return non-null instance of the class for which
     *  this instance provider is declared. The list of the file objects
     *  should be translated to that instance so typically the instance
     *  contains some kind of the collection.
     */
    public T createInstance(List<FileObject> fileObjectList);

}
