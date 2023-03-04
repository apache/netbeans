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

package org.netbeans.modules.spring.beans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Andrei Badea
 */
public class TestUtils {

    private TestUtils() {}

    public static String createXMLConfigText(String snippet) {
        return "<?xml version='1.0' encoding='UTF-8'?>" +
                "<beans xmlns='http://www.springframework.org/schema/beans' " +
                "       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
                "       xmlns:p='http://www.springframework.org/schema/p' " +
                "       xsi:schemaLocation='http://www.springframework.org/schema/beans " +
                "       http://www.springframework.org/schema/beans/spring-beans-2.5.xsd'>" +
                snippet +
                "</beans>";
    }
    
    public static String createXMLConfigText(String snippet, boolean includePNamespace) {
        if(includePNamespace) {
            return createXMLConfigText(snippet);
        }
        
        return "<?xml version='1.0' encoding='UTF-8'?>" +
                "<beans xmlns='http://www.springframework.org/schema/beans' " +
                "       xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
                "       xsi:schemaLocation='http://www.springframework.org/schema/beans " +
                "       http://www.springframework.org/schema/beans/spring-beans-2.5.xsd'>" +
                snippet +
                "</beans>";
    }

    public static BaseDocument createSpringXMLConfigDocument(String content) throws Exception {
        Class<?> kitClass = CloneableEditorSupport.getEditorKit(SpringConstants.CONFIG_MIME_TYPE).getClass();
        BaseDocument doc = new BaseDocument(kitClass, false);
        doc.putProperty(Language.class, XMLTokenId.language());
        doc.insertString(0, content, null);
        return doc;
    }

    public static void copyStringToFile(String string, File path) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        try {
            copyStreamToFile(inputStream, path);
        } finally {
            inputStream.close();
        }
    }

    public static String copyFileToString(File path) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileInputStream inputStream = new FileInputStream(path);
        try {
            FileUtil.copy(inputStream, outputStream);
        } finally {
            inputStream.close();
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void copyStreamToFile(InputStream inputStream, File path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path, false);
        try {
            FileUtil.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
        }
    }
}
