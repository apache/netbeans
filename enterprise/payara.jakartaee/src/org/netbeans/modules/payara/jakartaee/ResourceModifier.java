/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.payara.jakartaee;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Nitya Doraisamy
 */
public class ResourceModifier {
    public static void appendAttr(StringBuilder builder, String name, String value, boolean force) {
        if(force || (name != null && name.length() > 0)) {
            builder.append(name);
            builder.append("=\"");
            builder.append(value);
            builder.append("\" ");
        }
    }

    public static void appendProperty(StringBuilder builder, String name, String value, boolean force) {
        if(force || (value != null && value.length() > 0)) {
            builder.append("        <property name=\"");
            builder.append(name);
            builder.append("\" value=\"");
            builder.append(value);
            builder.append("\"/>\n");
        }
    }

    public static void appendResource(File sunResourcesXml, String fragment) throws IOException {
        String sunResourcesBuf = readResourceFile(sunResourcesXml);
        if (sunResourcesXml.getAbsolutePath().contains("sun-resources.xml")) {
            sunResourcesBuf = insertFragment(SUN_RESOURCES_XML_HEADER,sunResourcesBuf, fragment);
        } else {
            sunResourcesBuf = insertFragment(GF_RESOURCES_XML_HEADER,sunResourcesBuf, fragment);
        }
        writeResourceFile(sunResourcesXml, sunResourcesBuf);
    }

    private static final String SUN_RESOURCES_XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE resources PUBLIC " +
            "\"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" " +
            "\"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">\n" +
        "<resources>\n";
    private static final String GF_RESOURCES_XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!DOCTYPE resources PUBLIC " +"\"-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN\" " +
            "\"http://glassfish.org/dtds/glassfish-resources_1_5.dtd\">\n" +
//            "\"-//Sun Microsystems, Inc.//DTD Application Server 9.0 Resource Definitions //EN\" " +
//            "\"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">\n" +
        "<resources>\n";
    private static final String SUN_RESOURCES_XML_FOOTER =
        "</resources>\n";

    private static String insertFragment(String header, String sunResourcesBuf, String fragment) throws IOException {
        //String header = SUN_RESOURCES_XML_HEADER;
        String footer = SUN_RESOURCES_XML_FOOTER;
        boolean insertNewLine = false;

        if(sunResourcesBuf != null) {
            int closeIndex = sunResourcesBuf.indexOf("</resources>");
            if(closeIndex == -1) {
                throw new IOException("Malformed XML");
            }
            header = sunResourcesBuf.substring(0, closeIndex);
            footer = sunResourcesBuf.substring(closeIndex);

            if(closeIndex > 0 && sunResourcesBuf.charAt(closeIndex-1) != '\n') {
                insertNewLine = true;
            }
        }

        int length = header.length() + footer.length() + 2;
        if(fragment != null) {
            length += fragment.length();
        }

        StringBuilder builder = new StringBuilder(length);
        builder.append(header);

        if(insertNewLine) {
            String lineSeparator = System.getProperty("line.separator");
            builder.append(lineSeparator != null ? lineSeparator : "\n");
        }

        if(fragment != null) {
            builder.append(fragment);
        }

        builder.append(footer);
        return builder.toString();
    }

    private static String readResourceFile(File sunResourcesXml) throws IOException {
        String content = null;
        if(sunResourcesXml.exists()) {
            sunResourcesXml = FileUtil.normalizeFile(sunResourcesXml);
            FileObject sunResourcesFO = FileUtil.toFileObject(sunResourcesXml);

            if(sunResourcesFO != null) {
                InputStream is = null;
                Reader reader = null;
                try {
                    long flen = sunResourcesFO.getSize();
                    if(flen > 1000000) {
                        throw new IOException(sunResourcesXml.getAbsolutePath() + " is too long to update.");
                    }

                    int length = (int) (2 * flen + 32);
                    char [] buf = new char[length];
                    is = new BufferedInputStream(sunResourcesFO.getInputStream());
                    String encoding = EncodingUtil.detectEncoding(is);
                    reader = new InputStreamReader(is, encoding);
                    int max = reader.read(buf);
                    if(max > 0) {
                        content = new String(buf, 0, max);
                    }
                } finally {
                    if(is != null) {
                        try { is.close(); } catch(IOException ex) { }
                    }
                    if(reader != null) {
                        try { reader.close(); } catch(IOException ex) { }
                    }
                }
            } else {
                throw new IOException("Unable to get FileObject for " + sunResourcesXml.getAbsolutePath());
            }
        }
        return content;
    }

    private static void writeResourceFile(final File sunResourcesXml, final String content) throws IOException {
        FileObject parentFolder = FileUtil.createFolder(sunResourcesXml.getParentFile());
        FileSystem fs = parentFolder.getFileSystem();
        writeResourceFile(fs, sunResourcesXml, content);
    }

    private static void writeResourceFile(FileSystem fs, final File sunResourcesXml, final String content) throws IOException {
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileLock lock = null;
                BufferedWriter writer = null;
                try {
                    FileObject sunResourcesFO = FileUtil.createData(sunResourcesXml);
                    lock = sunResourcesFO.lock();
                    writer = new BufferedWriter(new OutputStreamWriter(sunResourcesFO.getOutputStream(lock)));
                    writer.write(content);
                } finally {
                    if(writer != null) {
                        try { writer.close(); } catch(IOException ex) { }
                    }
                    if(lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        });
    }
}
