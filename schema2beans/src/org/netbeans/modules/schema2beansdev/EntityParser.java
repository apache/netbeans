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

package org.netbeans.modules.schema2beansdev;

import java.io.*;
import java.util.*;
/**
 * EntityParser.java - parses the DTD file for entity declarations and creates new Reader
 * that replaces the entity references with values
 *
 * Created on June 11, 2005 AM
 * @author mkuchtiak
 */
public class EntityParser {
    private java.util.Map entityMap;
    private final String text;
    public EntityParser(Reader reader) throws IOException {
        StringWriter w = new StringWriter();
        char[] buf = new char[4096];
        int read;
        while ((read = reader.read(buf)) != -1) {
            w.write(buf, 0, read);
        }
        this.text = w.toString();
        entityMap = new java.util.HashMap();
    }
    /** Parses file for ENTITY declaration, creates map with entities
     */
    public void parse() throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line = null;
        while ((line=br.readLine())!=null) {
            int startPos = line.indexOf("<!ENTITY ");
            if (startPos>=0) addEntity(br,line.substring(startPos+9));
        }
        br.close();
    }
    
    private void addEntity(BufferedReader br, String line) throws IOException {
        StringTokenizer tok = new StringTokenizer(line);
        if (!tok.hasMoreTokens()) return;
        String percentage = tok.nextToken();
        if (!"%".equals(percentage)) return; //incorrect ENTITY declaration (missing %)
        if (!tok.hasMoreTokens()) return; //incorrect ENTITY declaration (missing entity name)
	
	// cut the first part including entity key
        String key = tok.nextToken();
        int valueStartPos = line.indexOf(key)+key.length();
        String rest = line.substring(valueStartPos);
	
	// looking for starting quotes
	valueStartPos =  rest.indexOf("\"");
	if (valueStartPos<0) return;
	
	// looking for entity value
	rest = rest.substring(valueStartPos+1);
	String value = resolveValue (rest,br);

        // write ENTITY into map	
        if (value!=null) {
	    int refStart = value.indexOf("%");
	    int refEnd = value.indexOf(";");
	    if (refStart>=0 && refEnd>refStart) { //references other entity
		String entityKey = value.substring(refStart+1,refEnd);		
                String val = (String)entityMap.get(entityKey);
		if (val!=null) {
		    String newValue = value.substring(0,refStart)+val+value.substring(refEnd+1);
            // XXX should use config.messageOut instead
		    System.out.println("found ENTITY: % "+key+" \""+newValue+"\"");
		    entityMap.put(key,newValue);
		}
            } else {
                System.out.println("found ENTITY: % "+key+" \""+value+"\"");
                entityMap.put(key,value);
            }
        }
    }
    
    private String resolveValue(String lineRest, BufferedReader br) throws IOException {
	// looking for closing quotes
	int index = lineRest.indexOf("\"");
	if (index>=0) return lineRest.substring(0,index);	
	// value across multiple lines	
	StringBuffer buf = new StringBuffer(lineRest);
        buf.append("\n");
	int ch=br.read();
        while ( ch!=(int)'"' && ch!=(int)'>' && ch!=-1 ) {
	    buf.append((char)ch);
	    ch=br.read();
        }
	return buf.toString();
    }

    private boolean containsBlank(String s) {
        for (int i=0;i<s.length();i++) {
            if (' '==s.charAt(i)) return true;
        }
        return false;
    }    

    /** Creates a StringReader that removes all ENTITY declarations
     *  and replaces entity references with corresponding values
     */
    public Reader getReader() throws IOException {
        StringBuffer buf = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(text));
        String line = null;
        while ((line=br.readLine())!=null) {
            // removing line(s) with entity declaration
            if (line.indexOf("<!ENTITY ")>=0) line = removeEntityDeclaration(line,br); 
            // searches for entity reference and replace it with value
            int pos = line.indexOf("%");
            if (pos>=0) {
                StringTokenizer tok = new StringTokenizer(line.substring(pos),";%");
                while (tok.hasMoreTokens()) {
                    String key = tok.nextToken();
                    if (key.length()>0 && !containsBlank(key)) {
                        String value = (String)entityMap.get(key);
                        if (value!=null) line = line.replaceAll("%"+key+";",value);
                    }
                }
            }
            if (line.length()>0) buf.append(line);
        }
        br.close();
        return new StringReader(buf.toString());
    }
    
    /** Removing line(s) containing ENTITY declaration
     */ 
    private String removeEntityDeclaration(String line,BufferedReader br) throws IOException {
        int start = line.indexOf("<!ENTITY ");
        StringBuffer buf = new StringBuffer();
        if (start>0) buf.append(line.substring(0, start));
        int endPos = line.indexOf(">", start);
        if (endPos>0) {
            buf.append(line.substring(endPos+1));
            return buf.toString();
        }
        String ln=null;
        while (endPos<0 && (ln=br.readLine())!=null) {
            endPos = ln.indexOf(">");
            if (endPos>=0) {
                buf.append(ln.substring(endPos+1));
            }
        }
        return buf.toString();
    }
    
}
