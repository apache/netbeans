/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.tls;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 *
 * @author Petr Hejl
 */
public class DerParser {

    private final InputStream is;

    public DerParser(InputStream is) {
        this.is = is;
    }

    public Asn1Object read() throws IOException {
        int tag = is.read();

        if (tag < 0) {
            throw new IOException("No more data to read");
        }
        int length = readLength();
        return new Asn1Object(tag, readValue(length));
    }

    private int readLength() throws IOException {
        int read = is.read();
        if (read < 0) {
            throw new IOException("No more data to read");
        }
        // sigle byte
        if ((read & ~0x7F) == 0) {
            return read;
        }

        // multibyte
        int num = read & 0x7F;
        if (read >= 0xFF || num > 4) {
            throw new IOException("Length too big to be used");
        }
        return new BigInteger(1, readValue(num)).intValue();
    }

    private byte[] readValue(int length) throws IOException {
        byte[] value = new byte[length];
        int count = 0;
        while (count < length) {
            int real = is.read(value, count, length - count);
            if (real < 0) {
                throw new IOException("Can't read the requested value");
            }
            count += real;
        }
        return value;
    }

}
