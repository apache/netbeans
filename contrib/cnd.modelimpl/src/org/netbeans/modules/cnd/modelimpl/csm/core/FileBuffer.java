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


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.openide.filesystems.FileObject;

/**
 * Represents the file state change event.
 * This event occurs when file is changed it's state
 * from saved to edited or vice versa.
 */
public interface FileBuffer extends APTFileBuffer {

    public void addChangeListener(ChangeListener listener);
    public void removeChangeListener(ChangeListener listener);

    public boolean isFileBased();

    public FileObject getFileObject();
    public CharSequence getUrl();
    
    public String getText(int start, int end) throws IOException;
    
    public CharSequence getText() throws IOException;
    
    public long lastModified();

    public long getCRC();

    int[] getLineColumnByOffset(int offset) throws IOException;

    int getLineCount() throws IOException;

    int getOffsetByLineColumn(int line, int column) throws IOException;
}
