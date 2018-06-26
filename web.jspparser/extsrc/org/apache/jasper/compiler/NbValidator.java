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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.tagext.PageData;

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
