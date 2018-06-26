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

package org.netbeans.test.web.core.syntax;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.modules.web.core.syntax.gsf.JspLanguage;
import org.netbeans.modules.web.jspparser.JspParserImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Common ancestor for all test classes.
 */
public class TestBase2 extends CslTestBase {


    public TestBase2(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JspLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/x-jsp";
    }

    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected BaseKit getEditorKit(String mimeType) {
        return new JspKit(JspKit.JSP_MIME_TYPE);
    }

    public final void initParserJARs() throws MalformedURLException {
        String path = System.getProperty("jsp.parser.jars");
        String[] paths = PropertyUtils.tokenizePath(path);
        List<URL> list = new ArrayList();
        for (int i = 0; i< paths.length; i++) {
            String token = paths[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            list.add(f.toURI().toURL());
        }
        JspParserImpl.setParserJARs(list.toArray(new URL[list.size()]));
    }

    public final ClassPath createServletAPIClassPath() throws MalformedURLException, IOException {
        return createClassPath("web.project.jars");
    }
    
    public final ClassPath createClassPath(String property) 
        throws MalformedURLException, IOException 
    {
        String path = System.getProperty("web.project.jars");
        if ( path == null ){
            path = "";
        }
        String[] st = PropertyUtils.tokenizePath(path);
        List<FileObject> fos = new ArrayList<FileObject>();
        for (int i=0; i<st.length; i++) {
            String token = st[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file "+token);
            }
            FileObject fo = FileUtil.toFileObject(f);
            fos.add(FileUtil.getArchiveRoot(fo));
        }
        return ClassPathSupport.createClassPath(fos.toArray(new FileObject[fos.size()]));
    }
    
    protected void assertFileContentsMatches(String relFilePath, String newFileName,
            String content ) throws Exception 
    {
        File file = getDataFile(relFilePath);
        if (!file.exists()) {
            NbTestCase.fail("File " + file + " not found.");
        }

        File goldenFile = getDataFile( newFileName );
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(content);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);
        assertEquals(expected.trim(), content.trim());
    }
}
