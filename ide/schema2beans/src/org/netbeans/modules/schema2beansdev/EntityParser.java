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

package org.netbeans.modules.schema2beansdev;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * EntityParser.java - parses the DTD file for entity declarations and creates new Reader
 * that replaces the entity references with values
 *
 * Created on June 11, 2005 AM
 * @author mkuchtiak
 */
public class EntityParser {
    private static final Pattern ENTITY_PATTERN = Pattern.compile("<!ENTITY\\s+%\\s+(\\S+)\\s+\"([^\"]*)\"\\s*>");
    private static final Pattern ENTITY_USE_PATTERN = Pattern.compile("%([\\S;]+);");

    private final Map<String,String> entityMap  = new HashMap<>();
    private String remainingText = "";

    public EntityParser() throws IOException {
    }

    /**
     * Parses file for ENTITY declaration, creates map with entities
     */
    public void parse(Reader reader) throws IOException {
        StringBuilder w = new StringBuilder();
        char[] buf = new char[4096];
        int read;
        while ((read = reader.read(buf)) != -1) {
            w.append(buf, 0, read);
        }

        String originalText = w.toString();

        StringBuffer buffer = new StringBuffer(originalText.length());
        Matcher entityMatcher = ENTITY_PATTERN.matcher(originalText);
        while(entityMatcher.find()) {
            addEntity(entityMatcher);
            entityMatcher.appendReplacement(buffer, "");
        }
        entityMatcher.appendTail(buffer);

        StringBuffer buffer2 = new StringBuffer(originalText.length());
        Matcher entityReplacementMatcher = ENTITY_USE_PATTERN.matcher(buffer);
        while(entityReplacementMatcher.find()) {
            String entity = entityReplacementMatcher.group(1);
            if(entityMap.containsKey(entity)) {
                entityReplacementMatcher.appendReplacement(buffer2, entityMap.get(entity));
            }
        }
        entityReplacementMatcher.appendTail(buffer2);

        remainingText = buffer2.toString();
    }

    private void addEntity(Matcher m) throws IOException {
        String key = m.group(1);
	String value = m.group(2);

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
        StringBuilder buf = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new StringReader(remainingText))) {
            String line;
            while ((line=br.readLine())!=null) {
                // removing line(s) with entity declaration
                if (line.contains("<!ENTITY ")) line = removeEntityDeclaration(line,br);
                // searches for entity reference and replace it with value
                int pos = line.indexOf("%");
                if (pos>=0) {
                    StringTokenizer tok = new StringTokenizer(line.substring(pos),";%");
                    while (tok.hasMoreTokens()) {
                        String key = tok.nextToken();
                        if (key.length()>0 && !containsBlank(key)) {
                            String value = (String)entityMap.get(key);
                            if(value.startsWith("http")) {
                                BufferedInputStream in = null;
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                try {
                                    in = new BufferedInputStream(new URL(value).openStream());
                                    byte[] buffer = new byte[8];
                                    int rc;
                                    while ((rc = in.read(buffer)) != -1) out.write(buffer, 0, rc);
                                    out.flush();
                                    value = new String(out.toByteArray(), StandardCharsets.UTF_8);
                                    value = value.replace("<?xml version='1.0' encoding='UTF-8'?>", "");
                                } finally {
                                    if(in != null) {
                                        in.close();
                                    }
                                    out.close();
                                }
                            }
                            if (value!=null) line = line.replace("%"+key+";",value);
                        }
                    }
                }
                if (line.length()>0) buf.append(line);
            }
        }
        return new StringReader(buf.toString());
    }
    
    /** Removing line(s) containing ENTITY declaration
     */ 
    private String removeEntityDeclaration(String line,BufferedReader br) throws IOException {
        int start = line.indexOf("<!ENTITY ");
        StringBuilder buf = new StringBuilder();
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
