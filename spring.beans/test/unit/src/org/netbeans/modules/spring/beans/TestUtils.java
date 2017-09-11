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

package org.netbeans.modules.spring.beans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        InputStream inputStream = new ByteArrayInputStream(string.getBytes("UTF-8"));
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
        return new String(outputStream.toByteArray(), "UTF-8");
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
