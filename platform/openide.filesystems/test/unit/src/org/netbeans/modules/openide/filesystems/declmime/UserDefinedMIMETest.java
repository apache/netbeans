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
package org.netbeans.modules.openide.filesystems.declmime;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UserDefinedMIMETest extends NbTestCase {

    public UserDefinedMIMETest(String name) {
        super(name);
    }
    
    public void testDefineAFileAndCheckType() throws Exception {
        FileObject type = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/type.inc");
        assertEquals("At first unknown", "content/unknown", type.getMIMEType());
        
        FileObject mimeRoot = FileUtil.getConfigFile("Services/MIMEResolver/");
        assertNotNull("Mime root found", mimeRoot);
        
        
        String txt = "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.1//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_1.dtd'>\n"
            + "<MIME-resolver>\n"
            + "  <file>\n"
            + "    <ext name='XXX'/>\n"
            + "    <ext name='inc'/>\n"
            + "    <resolver mime='text/x-h'/>\n"
            + "</file>\n"
            + "</MIME-resolver>\n";
        
        FileObject udmr = FileUtil.createData(mimeRoot, "user-defined-mime-resolver.xml");
        
        assertEquals("Still unknown", "content/unknown", type.getMIMEType());
        
        
        OutputStream os = udmr.getOutputStream();
        os.write(txt.getBytes(StandardCharsets.UTF_8));
        os.close();
        udmr.setAttribute("position", 555);
        udmr.setAttribute("user-defined-mime-resolver", Boolean.TRUE);
        
        assertEquals("Recognized well at the end", "text/x-h", type.getMIMEType());
    }
}
