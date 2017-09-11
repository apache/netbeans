/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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