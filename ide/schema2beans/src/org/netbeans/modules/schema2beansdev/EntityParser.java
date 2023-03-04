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

package org.netbeans.modules.schema2beansdev;

import java.io.*;
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

    /** Creates a StringReader that removes all ENTITY declarations
     *  and replaces entity references with corresponding values
     */
    public Reader getReader() throws IOException {
        return new StringReader(remainingText);
    }

}
