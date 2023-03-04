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
package org.netbeans.modules.web.wizards;

public class FileType {

    private String name,  suffix;

    private FileType(String name, String suffix) {
        this.name = name;
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }
    public static final FileType SERVLET =
            new FileType("servlet", "java");
    public static final FileType FILTER =
            new FileType("filter", "java");
    public static final FileType LISTENER =
            new FileType("listener", "java");
    public static final FileType JSP =
            new FileType("jsp", "jsp");
    public static final FileType JSF =
            new FileType("jsf", "jsp");
    public static final FileType JSPDOC =
            new FileType("jspdoc", "jspx");
    public static final FileType JSPF =
            new FileType("jspf", "jspf");
    public static final FileType TAG =
            new FileType("tag_file", "tag");
    public static final FileType TAGLIBRARY =
            new FileType("tag_library", "tld");
    public static final FileType TAG_HANDLER =
            new FileType("tag_handler", "java");
    public static final FileType HTML =
            new FileType("html", "html");
    public static final FileType XHTML =
            new FileType("xhtml", "xhtml");
    public static final FileType CSS =
            new FileType("css", "css");
    public static final FileType JS =
            new FileType("javascript","js");
    
    public static String IS_XML = "isXml";          // NOI18N
    public static String IS_SEGMENT = "isSegment";  // NOI18N
    public static String IS_FACELETS= "isFacelerts";// NOI18N
} 

