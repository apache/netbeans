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
package org.netbeans.modules.cnd.repository.disk;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * The most simple interface for random file access. The purpose is to hide all
 * details that concerns file access and have several pluggable implementations
 *
 */
public interface FileRWAccess {

    public ByteBuffer readData(long offset, int size) throws IOException;

    /**
     * @param data
     * @return offset in the file the data was written from
     *
     * @throws IOException
     */
    public long appendData(ByteBuffer data) throws IOException;

//    public void move(long offset, int size, long newOffset) throws IOException;
//
    public long move(FileRWAccess from, long offset, int size) throws IOException;

    public void truncate(long size) throws IOException;

    public void close() throws IOException;

    public long size() throws IOException;

    public boolean isValid() throws IOException;
}
