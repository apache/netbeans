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

package org.openide.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Object that holds serialized reference to another object.
 * Inspired by java.rmi.MarshalledObject but modified to
 * work with NetBeans and its modules. So no annotations are
 * stored with the bytestream and when the object
 * is deserialized it is assumed to be produced by
 * some installed module.
 */
public final class NbMarshalledObject implements Serializable {
    /** serial version UID */
    private static final long serialVersionUID = 7842398740921434354L;
    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; // NOI18N

    /**
     * @serial Bytes of serialized representation.  If <code>objBytes</code> is
     * <code>null</code> then the object marshalled was a <code>null</code>
     * reference.
     */
    private byte[] objBytes = null;

    /**
     * @serial Stored hash code of contained object.
     *
     * @see #hashCode
     */
    private int hash;

    /**
    * Creates a new <code>NbMarshalledObject</code> that contains the
    * serialized representation of the provided object.
    *
    * @param obj the object to be serialized (must be serializable)
    * @exception IOException the object is not serializable
    */
    public NbMarshalledObject(Object obj) throws IOException {
        if (obj == null) {
            hash = 17;

            return;
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new NbObjectOutputStream(bout);
        out.writeObject(obj);
        out.flush();
        objBytes = bout.toByteArray();

        int h = 0;

        for (int i = 0; i < objBytes.length; i++) {
            h = (37 * h) + objBytes[i];
        }

        hash = h;
    }

    /**
    * Returns a new copy of the contained marshalledobject.
    * The object is deserialized by NbObjectInputStream, so it
    * is assumed that it can be located in some module.
    *
    * @return a copy of the contained object
    * @exception IOException on any I/O problem
    * @exception ClassNotFoundException if the class of the object cannot be found
    */
    public Object get() throws IOException, ClassNotFoundException {
        if (objBytes == null) { // must have been a null object

            return null;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois = new NbObjectInputStream(bin);

        try {
            return ois.readObject();
        } catch (RuntimeException weird) {
            // Probably need to know what the bad ser actually was.
            StringBuffer buf = new StringBuffer((objBytes.length * 2) + 20);
            buf.append("Bad ser data: "); // NOI18N

            for (int i = 0; i < objBytes.length; i++) {
                int b = objBytes[i];

                if (b < 0) {
                    b += 256;
                }

                buf.append(HEX[b / 16]);
                buf.append(HEX[b % 16]);
            }

            IOException ioe = new IOException(weird.toString() + ": " + buf); // NOI18N
            ioe.initCause(weird);
            throw ioe;
        } finally {
            ois.close();
        }
    }

    /**
    * @return a hash code
    */
    public int hashCode() {
        return hash;
    }

    /** Two objects are equal if the hold the same serialized
    * representation.
    *
    * @param obj the object to compare with this <code>MarshalledObject</code>
    * @return <code>true</code> if the objects are serialized into the same bytestreams
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof NbMarshalledObject) {
            NbMarshalledObject other = (NbMarshalledObject) obj;

            return Arrays.equals(objBytes, other.objBytes);
        } else {
            return false;
        }
    }
}
