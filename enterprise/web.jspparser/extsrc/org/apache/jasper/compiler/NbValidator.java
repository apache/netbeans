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

package org.apache.jasper.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.jsp.tagext.PageData;

import org.apache.jasper.JasperException;

/** This class is similar to org.apache.jasper.compiler.Validator, it only
 * allows getting access to the XML view of the page.
 *
 * @author Petr Jiricka
 */
public class NbValidator {
    
    private static final Logger LOGGER = Logger.getLogger(NbValidator.class.getName());

    private static Method validateXmlViewM;
    private static Field bufF;

    static {
        initReflection();
    }
    
    private static void initReflection() {
        try {
            validateXmlViewM = Validator.class.getDeclaredMethod("validateXmlView", new Class[] {PageData.class, Compiler.class}); // NOI18N
            validateXmlViewM.setAccessible(true);
            bufF = PageDataImpl.class.getDeclaredField("buf"); // NOI18N
            bufF.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.INFO, null, e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }
    
    /** Returns the XML view of the page.
     */
    public static String validate(Compiler compiler, Node.Nodes page) throws JasperException {

	/*
	 * Visit the page/tag directives first, as they are global to the page
	 * and are position independent.
	 */
	page.visit(new Validator.DirectiveVisitor(compiler));

	// Determine the default output content type
	PageInfo pageInfo = compiler.getPageInfo();
	String contentType = pageInfo.getContentType();

	if (contentType == null || contentType.indexOf("charset=") < 0) { // NOI18N
	    boolean isXml = page.getRoot().isXmlSyntax();
	    String defaultType;
	    if (contentType == null) {
		defaultType = isXml? "text/xml": "text/html"; // NOI18N
	    } else {
		defaultType = contentType;
	    }

	    String charset = null;
	    if (isXml) {
		charset = "UTF-8"; // NOI18N
	    } else {
		if (!page.getRoot().isDefaultPageEncoding()) {
		    charset = page.getRoot().getPageEncoding();
		}
	    }

	    if (charset != null) {
		pageInfo.setContentType(defaultType + ";charset=" + charset); // NOI18N
	    } else {
		pageInfo.setContentType(defaultType);
	    }
	}

	/*
	 * Validate all other nodes.
	 * This validation step includes checking a custom tag's mandatory and
	 * optional attributes against information in the TLD (first validation
	 * step for custom tags according to JSP.10.5).
	 */
	page.visit(new Validator.ValidateVisitor(compiler));

	/*
	 * Invoke TagLibraryValidator classes of all imported tags
	 * (second validation step for custom tags according to JSP.10.5).
	 */
        // validateXmlView(new PageDataImpl(page, compiler), compiler);
        try {
            PageDataImpl pdi = new PageDataImpl(page, compiler);
            
            validateXmlViewM.invoke(null, new Object[] {pdi, compiler});
            
            /*
             * Invoke TagExtraInfo method isValid() for all imported tags 
             * (third validation step for custom tags according to JSP.10.5).
             */
            page.visit(new Validator.TagExtraInfoVisitor(compiler));
            
            StringBuilder sb = (StringBuilder)bufF.get(pdi);
            return sb.toString();
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new JasperException(e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof JasperException) {
                throw (JasperException)target;
            } else {
                LOGGER.log(Level.INFO, null, e);
                throw new JasperException(e.getMessage());
            }
        }
    }
}
