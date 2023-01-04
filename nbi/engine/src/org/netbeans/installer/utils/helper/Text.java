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

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;

/**
 *
 * @author Kirill Sorokin
 */
public class Text {
    private String text = "";
    private ContentType contentType = ContentType.PLAIN_TEXT;
    
    public Text() {
        // does nothing
    }
    
    public Text(final String text, final ContentType contentType) {
        this.text = text;
        this.contentType = contentType;
    }
    
    public String getText() {
        return text;
    }
    
    public ContentType getContentType() {
        return contentType;
    }
    
    public static enum ContentType {
        PLAIN_TEXT,
        HTML;
        
        public static ContentType parseContentType(final String string) throws UnrecognizedObjectException {
            if (string.equals("text/plain")) {
                return PLAIN_TEXT;
            }
            
            if (string.equals("text/html")) {
                return HTML;
            }
            
            throw new UnrecognizedObjectException("Cannot recognize content type");
        }
        
        public String getExtension() {
            switch (this) {
                case PLAIN_TEXT:
                    return ".txt";
                case HTML:
                    return ".html";
                default:
                    return "";
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case PLAIN_TEXT:
                    return "text/plain";
                case HTML:
                    return "text/html";
                default:
                    return "";
            }
        }
    }
}
