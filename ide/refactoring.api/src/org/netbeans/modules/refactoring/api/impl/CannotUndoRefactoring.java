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
package org.netbeans.modules.refactoring.api.impl;

import java.util.Collection;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Jan Becicka
 */
public class CannotUndoRefactoring extends CannotUndoException {
    private Collection<String> files;

    public CannotUndoRefactoring(Collection<String> checkChecksum) {
        super();
        this.files = checkChecksum;
    }

    @Override
    public String getMessage() {
        StringBuilder b = new StringBuilder("Cannot Undo.\nFollowing files were modified:\n");
        for (String f : files) {
            b.append(f);
            b.append('\n');
        }
        return b.toString();
    }

    public Collection<String> getFiles() {
        return files;
    }
    
}
