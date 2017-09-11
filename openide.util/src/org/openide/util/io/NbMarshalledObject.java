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
    private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; // NOI18N

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

        if ((obj != null) && obj instanceof NbMarshalledObject) {
            NbMarshalledObject other = (NbMarshalledObject) obj;

            return Arrays.equals(objBytes, other.objBytes);
        } else {
            return false;
        }
    }
}
