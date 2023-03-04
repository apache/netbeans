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
package org.netbeans.modules.autoupdate.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import static org.netbeans.modules.autoupdate.services.Utilities.hexDecode;

public class ExternalFile {

    private static final Logger LOG = Logger.getLogger(ExternalFile.class.getName());

    /**
     * Parse an ".external" file from supplied input stream.
     *
     * <p>The expected format is a UTF-8 encoded text file. The format is line
     * oriented and contain the following items:</p>
     *
     * <ul>
     * <li>lines with the format {@code # <characters>} are treated as a
     * comments</li>
     * <li>lines with the format {@code CRC:<integer>} represent a CRC32
     * checksum</li>
     * <li>lines with the format {@code URL:<url>} represents one possible URL
     * for download</li>
     * <li>lines with the format {@code SIZE:<integer>} represent the
     * size of the target file (currently unused)</li>
     * <li>lines with the format {@code URL:<url>} represents one possible URL
     * for download</li>
     * <li>lines with the format
     * {@code MessageDigest: <algorithm> <hexvalue>} represents one message
     * digest for the data. {@code <algorithm>} is the message digest algorithm
     * used and {@code <hexvalue>} the result of the digest algorithm applied
     * to the data referenced by the URL(s) and hex encoded.</li>
     * </ul>
     *
     * <p>If multiple {@code CRC} lines are found, the last value will be used,
     * if multiple {@code URL} lines are found, all URLs are considered as
     * possible download source. Multiple {@code MessageDigest} lines with
     * different {@code algorithm} values will all be considered, if the same
     * {@code algorithm} is found twice, only the last entry is considered.</p>
     *
     * <p>This method will not close the inputStream.</p>
     *
     * @param is
     * @return
     */
    public static ExternalFile fromStream(String name, InputStream is) throws IOException {
        ExternalFile ext = new ExternalFile();
        ext.setName(name);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        for(String line = br.readLine(); line != null; line = br.readLine()) {
            if(line.startsWith("#")) {
                // Comment
            } else if (line.startsWith("SIZE:")) {
                ext.setSize(Integer.parseInt(line.substring(5).trim()));
            } else if (line.startsWith("CRC:")) {
                ext.setCrc32(Long.parseLong(line.substring(4).trim()));
            } else if (line.startsWith("URL:")) {
                String url = line.substring(4).trim();
                for (;;) {
                    int index = url.indexOf("${");
                    if (index == -1) {
                        break;
                    }
                    int end = url.indexOf("}", index);
                    String propName = url.substring(index + 2, end);
                    final String propVal = System.getProperty(propName);
                    if (propVal == null) {
                        throw new IOException("Can't find property " + propName);
                    }
                    url = url.substring(0, index) + propVal + url.substring(end + 1);
                }
                ext.getModifiableUrls().add(url);
            } else if (line.startsWith("MessageDigest:")) {
                // Assume format: <JSSE_Implementation_Name> HexEncodedHashValue>
                String[] parts = line.substring(14).trim().split("\\s+");
                if(parts.length == 2) {
                    try {
                        ext.getModifiableMessageDigests().put(
                            parts[0],
                            hexDecode(parts[1]));
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.INFO, MessageFormat.format(
                            "Invalidly formatted MessageDigest line found in {1}: {0}",
                            new Object[]{line, name}), ex);
                    }
                } else {
                    LOG.log(Level.INFO,
                        "Invalidly formatted MessageDigest line found in {1}: {0}",
                        new Object[]{line, name});
                }
            } else if (! line.trim().isEmpty()) {
                LOG.log(Level.INFO, "Invalid content found in {1}: {0}",
                    new Object[]{line, name});
            }
        }
        return ext;
    }

    private String name;
    private final List<String> urls = new ArrayList<>();
    private Long crc32 = null;
    private Integer size = null;
    private final Map<String,byte[]> messageDigest = new HashMap<>();

    private ExternalFile() {
    }

    public List<String> getUrls() {
        return Collections.unmodifiableList(urls);
    }

    public Map<String,byte[]> getMessageDigests() {
        return Collections.unmodifiableMap(this.messageDigest);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private List<String> getModifiableUrls() {
        return urls;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private Map<String,byte[]> getModifiableMessageDigests() {
        return this.messageDigest;
    }

    public Long getCrc32() {
        return crc32;
    }

    private void setCrc32(Long crc32) {
        this.crc32 = crc32;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return validator, that checks the CRC32 value and all message digest
     *         values, that can be verified by the runtime JRE. Unsupported
     *         message digest values will be ignored
     */
    public MessageMultiValidator getValidator() {
        List<MessageValidator> validators = new ArrayList<>(2);
        validators.add(new MessageChecksumValidator(new CRC32(), getCrc32()));
        for(Entry<String,byte[]> entry: getMessageDigests().entrySet()) {
            try {
                validators.add(new MessageDigestValidator(
                    MessageDigest.getInstance(entry.getKey()), entry.getValue()));
            } catch (NoSuchAlgorithmException ex) {
                LOG.log(Level.INFO,
                    "Requested message digest {0} not found for {1}",
                    new Object[] {entry.getKey(), getName()});
            }
        }
        return new MessageMultiValidator(validators);
    }
}
