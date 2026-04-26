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
package org.netbeans.modules.testng.spi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 *
 * @author lukas
 */
public final class XMLSuiteSupport {
    private static final Logger LOGGER = Logger.getLogger(XMLSuiteSupport.class.getName());

    private XMLSuiteSupport() {
    }
    
    public static File createSuiteforMethod(File targetFolder, String projectName, String pkgName, String className, String methodName) {
        if (!targetFolder.isDirectory()) {
            throw new IllegalArgumentException(targetFolder.getAbsolutePath() + " is not a directory"); //NOI18N
        }
        String packageName = pkgName.trim();
        String fqClassName = packageName.isEmpty() ? className : packageName + "." + className;
        XmlSuite xmlSuite = new XmlSuite();
        // Maintain parity with testng-6.14.3:org.testng.xml.LaunchSuite.ClassesAndMethodsSuite
        xmlSuite.setName("Custom suite");   //NO18N
        XmlTest xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName(projectName);
        XmlClass xmlClass = new XmlClass(fqClassName);
        if (methodName != null) {
            xmlClass.setIncludedMethods(Collections.singletonList(new XmlInclude(methodName)));
        }
        xmlTest.setXmlClasses(Collections.singletonList(xmlClass));
        String xml = xmlSuite.toXml();
        // Maintain parity with testng-6.14.3:org.testng.xml.LaunchSuite.ClassesAndMethodsSuite
        String fileName = "temp-testng-customsuite.xml";    //NOI18N

        Path f = targetFolder.toPath().resolve(fileName);
        try {
            f = Files.writeString(f, xml, StandardOpenOption.CREATE);
            FileUtil.refreshFor(targetFolder);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to write TestNG XMLSuite to file.", ex);
        }
        return f.toFile();
    }
}
