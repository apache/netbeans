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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;

/*
 * The first byte in ASN.1
 *
 *-------------------------------------------------
 *|Bit 8|Bit 7|Bit 6|Bit 5|Bit 4|Bit 3|Bit 2|Bit 1|
 *-------------------------------------------------
 *|  Class    | CF  |     +      Type             |
 *-------------------------------------------------
 */
public class Asn1Object {

    private static final int FLAG_CONSTRUCTED = 0x20;

    private static final int TYPE_INTEGER = 0x02;

    private static final int TYPE_MASK = 0x1F;

    private final int tag;

    private final int type;

    private final byte[] value;

    public Asn1Object(int tag, byte[] value) {
        this.tag = tag;
        this.type = tag & TYPE_MASK;
        this.value = value;
    }

    public DerParser read() throws IOException {
        if ((tag & FLAG_CONSTRUCTED) == 0) {
            throw new IOException("This object is not a constructed value");
        }
        return new DerParser(new ByteArrayInputStream(value));
    }

    public BigInteger getBigInteger() throws IOException {
        if (type != TYPE_INTEGER) {
            throw new IOException("This object does not represent integer: " + type);
        }
        return new BigInteger(value);
    }
}
