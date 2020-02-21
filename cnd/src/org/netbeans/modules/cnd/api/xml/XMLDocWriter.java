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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
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

package org.netbeans.modules.cnd.api.xml;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Drive the writing of an XML document.
 * <p>
 * While one can implement the {@link XMLEncoder} interface directly,
 * the recommended practice
 * is to define one or more specialized <code>XMLEncoder</code>s for the
 * expected top-level elements and delegate to their {@link XMLEncoder#encode}.
 */
abstract public class XMLDocWriter implements XMLEncoder {

    private int indentChars = 2;
    private String lineSeparator = System.getProperty("line.separator"); // NOI18N

    private XMLEncoderStream encoderStream;
    private String comment = null;

    public XMLDocWriter() {
    }

    protected void setMasterComment(String comment) {
        this.comment = comment;
    }
    
    /**
     * Set number of spaces to be used for each indent level.
     */
    public void setLineSeparator(String separator) {
	this.lineSeparator = separator;
    } 

    /**
     * Set number of spaces to be used for each indent level.
     */
    public void setIndentChars(int indentChars) {
	this.indentChars = indentChars;
    } 

    /**
     * Return the XML encoding string.
     * <p>
     * The typical value is "UTF-8".
     * <br>
     * The default implementation handles US/Chinese/Japanese.
     */
    protected String encoding() {
//	String lang = System.getenv("LANG");	// NOI18N
	String encoding = "UTF-8";		// NOI18N
        // See IZ 119431
//	if (lang != null) {
//	    if (lang.equals("zh") ||		// NOI18N
//		lang.equals("zh.GBK") ||	// NOI18N
//		lang.equals("zh_CN.EUC") ||	// NOI18N
//		lang.equals("zh_CN.GB18030") ||	// NOI18N
//		lang.equals("zh_CN") ||		// NOI18N
//		lang.equals("zh_CN.GBK")) {	// NOI18N
//
//		encoding = "EUC-JP";		// NOI18N
//
//	    } else if (lang.equals("ja") ||	// NOI18N
//		       lang.equals("ja_JP.eucJP")) { // NOI18N
//
//		encoding = "EUC-JP";		// NOI18N
//	    } else {
//		encoding = "UTF-8";		// NOI18N
//	    }
//	}
	return encoding;
    } 

    /**
     * Put out
     *	<?xml version="1.0" encoding="UTF-8"?>
     * (Or the correct encoding)
     */
    private void writeHeader() {
	String version = "1.0";		// NOI18N
	encoderStream.println
	("<?xml version=\"" + version + "\" encoding=\"" + encoding() + "\"?>"); // NOI18N
        if (comment != null) {
            encoderStream.println("<!--" + comment + "-->"); // NOI18N
        }
    } 

    /**
     * Put out 
     *	<!DOCTYPE ... >
     * LATER though ...
     */
    private void writeDoctype() {
    } 

    private void writeTail() {
    } 

    /**
     * Drive the writing of an XML document.
     * <p>
     * Will put the following to the stream:
     * <pre>
     &lt;?xml version="1.0" encoding="&lt;the-appropriate-encoding&gt;"?&gt;
     call {@link #encode}
     * </pre>
     * <p>
     * <i>the-appropriate-encoding</i> is whatever is returned by 
     * {@link #encoding}.
     */
    public void write(OutputStream os) throws IOException {
	try {
	    encoderStream = new XMLEncoderStream(os, indentChars, encoding(), lineSeparator);
	    writeHeader();
	    writeDoctype();
	    encode(encoderStream);
	} finally {
	    writeTail();
	    encoderStream.close();
	} 
    }
}
