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
package org.netbeans.api.java.source.ui;

import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 * @since 1.1
 * @deprecated Use {@link org.netbeans.api.editor.DialogBinding} instead.
 */
@Deprecated
public final class DialogBinding {

    private DialogBinding() {}
    
    /**
     * Bind given component and given file together.
     * @param fileObject to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original file
     * @param component component to bind
     * @return {@link JavaSource} or null
     * @throws IllegalArgumentException if fileObject is null
     * @since 1.1
     * @deprecated Use {@link org.netbeans.api.editor.DialogBinding#bindComponentToFile(FileObject,int,int,JTextComponent)} instead.
     */
    @Deprecated
    public static JavaSource bindComponentToFile(FileObject fileObject, int offset, int length, JTextComponent component) throws IllegalArgumentException {
        org.netbeans.api.editor.DialogBinding.bindComponentToFile(fileObject, offset, length, component);
        return null;
    }
}
