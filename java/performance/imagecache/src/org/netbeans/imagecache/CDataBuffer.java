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
/*
 * CDataBuffer.java
 *
 * Created on February 17, 2004, 2:21 AM
 */

package org.netbeans.imagecache;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

/** An imaging DataBufferInt implementation backed by an IntBuffer over a
 * memory mapped file.
 *
 * @author  Tim Boudreau
 */
class CDataBuffer extends DataBuffer {
    IntBuffer buf;

    /** Creates a new instance of CDataBuffer */
    public CDataBuffer(IntBuffer buf, int width, int height) {
        super (TYPE_INT, width * height);
        this.buf = buf;
    }

    public int getOffset() {
        return 0;
    }

    public int getElem(int bank, int i) {
        return this.buf.get(i);
    }

    public void setElem(int bank, int i, int val) {
//        throw new UnsupportedOperationException();
        //do nothing
    }

}
