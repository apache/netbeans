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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.apache.jasper.compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;

/**
 *
 * @author Petr Jiricka
 */
public class ExtractPageData {

    private final JspCompilationContext ctxt;

    private final Options options;
    private final CompilerHacks compHacks;

    private boolean isXml;
    private String sourceEnc;

    /** Creates a new instance of ExtractPageData */
    public ExtractPageData(JspCompilationContext ctxt) {
        this.ctxt = ctxt;
        options = ctxt.getOptions();
        compHacks = new CompilerHacks(ctxt);
    }
    
    
    public boolean isXMLSyntax() throws JasperException, FileNotFoundException, IOException {
        if (sourceEnc == null) {
            extractPageData();
        }
        return isXml;
    }

    public String getEncoding() throws JasperException, FileNotFoundException, IOException {
        if (sourceEnc == null) {
            extractPageData();
        }
        return sourceEnc;
    }

    
    private void extractPageData() throws JasperException, FileNotFoundException, IOException {
        
        // the following also sets up ErrorDispatcher and PageInfo in the compiler
        Compiler comp = compHacks.getCompiler();
        PageInfo pageInfo = comp.getPageInfo();
        
        JspConfig jspConfig = options.getJspConfig();
        JspProperty jspProperty = jspConfig.findJspProperty(ctxt.getJspFile());

        /*
         * If the current uri is matched by a pattern specified in
         * a jsp-property-group in web.xml, initialize pageInfo with
         * those properties.
         */
        pageInfo.setELIgnored(JspUtil.booleanValue(jspProperty.isELIgnored()));
        pageInfo.setScriptingInvalid(JspUtil.booleanValue(jspProperty.isScriptingInvalid()));
        if (jspProperty.getIncludePrelude() != null) {
            pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
        }
        if (jspProperty.getIncludeCoda() != null) {
            pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
        }
        /*String javaFileName = ctxt.getServletJavaFileName();

        // Setup the ServletWriter
        String javaEncoding = ctxt.getOptions().getJavaEncoding();
	OutputStreamWriter osw = null; 
	try {
	    osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
					 javaEncoding);
	} catch (UnsupportedEncodingException ex) {
            errDispatcher.jspError("jsp.error.needAlternateJavaEncoding", javaEncoding);
	}

	ServletWriter writer = new ServletWriter(new PrintWriter(osw));
        ctxt.setWriter(writer);*/

        // Reset the temporary variable counter for the generator.
        JspUtil.resetTemporaryVariableName();

	// Parse the file
	ParserControllerProxy parserCtl = new ParserControllerProxy(ctxt, comp);
	parserCtl.extractSyntaxAndEncoding(ctxt.getJspFile());
        
        isXml = parserCtl.isXml;
        sourceEnc = parserCtl.sourceEnc;
    }
    
}
