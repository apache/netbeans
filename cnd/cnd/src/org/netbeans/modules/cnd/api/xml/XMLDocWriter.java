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
