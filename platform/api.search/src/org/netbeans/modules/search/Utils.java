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

package org.netbeans.modules.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 *
 * @author  Marian Petras
 * @author  kaktus
 */
final class Utils {

    private Utils() { }

    /**
     * Returns a border for explorer views.
     *
     * @return  border to be used around explorer views
     *          (<code>BeanTreeView</code>, <code>TreeTableView</code>,
     *          <code>ListView</code>).
     */
    static Border getExplorerViewBorder() {
        Border border;
        border = (Border) UIManager.get("Nb.ScrollPane.border");        //NOI18N
        if (border == null) {
            border = BorderFactory.createEtchedBorder();
        }
        return border;
    }    
    
    /**
     * Converts an input file stream into a char sequence.
     *
     * @throws IOException
     */
    static CharBuffer getCharSequence(final FileInputStream stream, Charset encoding) throws IOException {
        FileChannel channel = stream.getChannel();
        ByteBuffer bbuf = ByteBuffer.allocate((int) channel.size());
        try {
            channel.read(bbuf, 0);
        } catch (ClosedByInterruptException cbie) {
            return null;        //this is actually okay
        } finally {
            channel.close();
        }
        bbuf.rewind();
        CharBuffer cbuf = encoding.decode(bbuf);

        return cbuf;
 }
}
