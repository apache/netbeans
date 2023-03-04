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
