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
package org.openide.filesystems;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;

public class FsMimeResolverTest extends NbTestCase {

    public FsMimeResolverTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testResolveXmlFileWithoutFreemarkerTags() throws Exception {
        createResolver();

        String content = "<?xml version=\"1.0\" encoding=\"${project.encoding}\"?> "
                        + "<beans xmlns=\"http://www.springframework.org/schema/beans\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"></beans>";
        FileObject fo = createXmlFile(content);
        assertEquals("text/x-springconfig+xml", fo.getMIMEType());
    }

    public void testResolveXmlFileWithFreemarkerTags() throws Exception {
        createResolver();

        String content = "<?xml version=\"1.0\" encoding=\"${project.encoding}\"?>"
                        + "<beans xmlns=\"http://www.springframework.org/schema/beans\""
                        + "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                        + "  <#list namespaces as namespace>"
                        + "    xmlns:${namespace.prefix}=\"${namespace.namespace}\""
                        + "  </#list>"
                        + "  <#if springVersion3??>"
                        + "    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd"
                        + "  <#else>"
                        + "    xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd"
                        + "  </#if>"
                        + "  <#list namespaces as namespace>"
                        + "    <#if namespace.namespace != \"http://www.springframework.org/schema/p\">"
                        + "      ${namespace.namespace} ${namespace.namespace}/${namespace.fileName}"
                        + "    </#if>"
                        + "  </#list>\">"
                        + "</beans>";
        FileObject fo = createXmlFile(content);
        assertEquals("text/x-springconfig+xml", fo.getMIMEType());
    }

    public void testResolveNormalXmlFile() throws Exception {
        createResolver();

        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                        + "<beans xmlns=\"http://www.springframework.org/schema/beans\" "
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"></beans>";
        FileObject fo = createXmlFile(content);
        assertEquals("text/x-springconfig+xml", fo.getMIMEType());
    }
    
    private void createResolver() throws Exception {
        FileObject resolver = FileUtil.createData(FileUtil.getConfigRoot(), "Services/MIMEResolver/resolver.xml");
        OutputStream os = resolver.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name=\"xml\"/>");
        ps.println("    <resolver mime=\"text/x-springconfig+xml\">");
        ps.println("      <xml-rule>");
        ps.println("        <element name=\"beans\" ns=\"http://www.springframework.org/schema/beans\"/>");
        ps.println("      </xml-rule>");
        ps.println("    </resolver>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
    }
    
    public void testNeverEndingRecognition() throws Exception {
        String txt = "<?xml version='1.0'?>"
        + "<#assign licenseFirst = '<!--'>"
        + "<#assign licensePrefix = ''>"
        + "<#assign licenseLast = '-->'>"
        + "<#include '../Licenses/license-${project.license}.txt'>"
        + "<!-- see http://www.phpunit.de/wiki/Documentation -->"
        + "<!--phpunit bootstrap='/path/to/bootstrap.php'"
        + "      colors='false'"
        + "      convertErrorsToExceptions='true'"
        + "      convertNoticesToExceptions='true'"
        + "      convertWarningsToExceptions='true'"
        + "      stopOnFailure='true'>"
        + "</phpunit-->"
        + "<phpunit colors='false' />"
        + "\n";
        FileObject fo = createXmlFile(txt);
        assertEquals("text/xml", fo.getMIMEType());
    }

    private FileObject createXmlFile(String content) throws Exception {
        FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("file.xml");
        FileLock lock = file.lock ();
        try {
            OutputStream out = file.getOutputStream(lock);
            try {
                out.write(content.getBytes());
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return file;
    }

}