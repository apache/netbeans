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
package org.netbeans.modules.docker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
//@NotThreadSafe
public class Demuxer implements StreamItem.Fetcher {

    private static final Logger LOGGER = Logger.getLogger(Demuxer.class.getName());

    private final InputStream is;

    private byte[] buffer = new byte[8];

    private byte[] content = new byte[256];

    public Demuxer(InputStream is) {
        this.is = is;
    }

    @Override
    public StreamItem fetch() {
        try {
            int sum = 0;
            do {
                int read = is.read(buffer, sum, buffer.length - sum);
                if (read < 0) {
                    return null;
                }
                sum += read;
            } while (sum < 8);
            // now we have 8 bytes
            assert buffer.length == 8;

            boolean error;
            int size = ByteBuffer.wrap(buffer).getInt(4);
            if (buffer[0] == 0 || buffer[0] == 1) {
                error = false;
            } else if (buffer[0] == 2) {
                error = true;
            } else {
                throw new IOException("Unparsable stream " + buffer[0]);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
            sum = 0;
            do {
                int read = is.read(content, 0, Math.min(size - sum, content.length));
                if (read < 0) {
                    return null;
                }
                bos.write(content, 0, read);
                sum += read;
            } while (sum < size);
            return new StreamItem(ByteBuffer.wrap(bos.toByteArray()), error);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }
}
