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

package org.netbeans.modules.xml.multiview;

import org.netbeans.modules.xml.api.EncodingUtil;

import javax.swing.text.StyledDocument;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * @author pfiala
 */
public class EncodingHelper {
    public static final String DEFAULT_ENCODING = "UTF-8"; // NOI18N;

    private String encoding = DEFAULT_ENCODING;

    public boolean isValidEncoding(String encoding) {
        //test encoding on dummy stream
        try {
            new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream(1), encoding);
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public String setDefaultEncoding(String s) {
        // update prolog to new valid encoding
        if (s.startsWith("<?xml")) {
            int i = s.indexOf("?>");
            if (i > 0) {
                s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + s.substring(i + 2);
            }
        }
        return s;
    }

    public String getEncoding() {
        return encoding;
    }

    public void resetEncoding() {
        encoding = DEFAULT_ENCODING;
    }

    public String detectEncoding(StyledDocument document) throws IOException {
        return setEncoding(EncodingUtil.detectEncoding(document));
    }

    public String detectEncoding(InputStream inputStream) throws IOException {
        return setEncoding(EncodingUtil.detectEncoding(inputStream));
    }

    public String detectEncoding(byte[] data) throws IOException {
        return detectEncoding(new ByteArrayInputStream(data));
    }

    public String setEncoding(String encoding) {
        if (encoding == null) {
            return this.encoding;
        }
        if (isValidEncoding(encoding)) {
            this.encoding = encoding;
        }
        return encoding;
    }
}
