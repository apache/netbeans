/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.core.output2;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Heap based implementation of the Storage interface, over a byte array.
 *
 */
class HeapStorage implements Storage {
    private boolean closed = true;
    private byte[] bytes = new byte[2048];
    private int size = 0;

    public Storage toFileMapStorage() throws IOException {
        FileMapStorage result = new FileMapStorage();
        BufferResource<ByteBuffer> br = getReadBuffer(0, size);
        try {
            result.write(br.getBuffer());
            return result;
        } finally {
            if (br != null) {
                br.releaseBuffer();
            }
        }
    }

    @Override
    public BufferResource<ByteBuffer> getReadBuffer(
            final int start, final int length) throws IOException {

        return new HeapBufferResource(ByteBuffer.wrap(
                bytes, start, Math.min(length, bytes.length - start)));
    }

    public ByteBuffer getWriteBuffer(int length) throws IOException {
        return ByteBuffer.allocate(length);
    }

    public synchronized int write(ByteBuffer buf) throws IOException {
        closed = false;
        int oldSize = size;
        size += buf.limit();
        if (size > bytes.length) {
            byte[] oldBytes = bytes;
            bytes = new byte[Math.max (oldSize * 2, (buf.limit() * 2) + oldSize)]; 
            System.arraycopy (oldBytes, 0, bytes, 0, oldSize);
        }
        buf.flip();
        buf.get(bytes, oldSize, buf.limit());
        return oldSize;
    }

    @Override
    public void removeBytesFromEnd(int length) throws IOException {
        size = size - length;
    }

    public synchronized void dispose() {
        bytes = new byte[0];
        size = 0;
    }

    public synchronized int size() {
        return size;
    }

    public void flush() throws IOException {
        //N/A
    }

    public void close() throws IOException {
        closed = true;
    }

    public boolean isClosed() {
        return closed;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public synchronized void shiftStart(int byteOffset) {
        size -= byteOffset;
        System.arraycopy(bytes, byteOffset, bytes, 0, size);
    }
 
    private class HeapBufferResource implements BufferResource<ByteBuffer> {

        private ByteBuffer buffer;

        public HeapBufferResource(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public ByteBuffer getBuffer() {
            return buffer;
        }

        @Override
        public void releaseBuffer() {
            this.buffer = null;
        }
    }
}
