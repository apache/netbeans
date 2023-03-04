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
package org.netbeans.modules.html.editor.lib.api;

public final class ProblemDescription {

    public static final int INFORMATION = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;
    public static final int FATAL = 3;
    public static final int INTERNAL_ERROR = 4;
    
    private String key;
    private String text;
    private int from;
    private int to;
    private int type;

    public static ProblemDescription create(String key, String text, int type, int from, int to) {
        return new ProblemDescription(key, text, type, from, to);
    }

    private ProblemDescription(String key, String text, int type, int from, int to) {
        assert from >= 0;
        assert to >= 0;
        assert from <= to;
        
        this.key = key;
        this.text = text;
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public String getKey() {
        return key;
    }

    public int getType() {
        return type;
    }

    public int getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public int getTo() {
        return to;
    }

    @Override
    public String toString() {
        return dump(null);
    }

    public String dump(String code) {
        String ttype = "";
        switch (getType()) {
            case INFORMATION:
                ttype = "Information"; //NOI18N
                break;
            case WARNING:
                ttype = "Warning"; //NOI18N
                break;
            case ERROR:
                ttype = "Error"; //NOI18N
                break;
            case FATAL:
                ttype = "Fatal Error"; //NOI18N
                break;
            case INTERNAL_ERROR:
                ttype = "Internal Error"; //NOI18N
                break;
        }
        String nodetext = code == null ? "" : code.substring(getFrom(), getTo());
        return ttype + ":" + getKey() + " [" + getFrom() + " - " + getTo() + "]: '" + nodetext + (getText() != null ? "'; msg=" + getText() : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProblemDescription other = (ProblemDescription) obj;
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        if (this.from != other.from) {
            return false;
        }
        if (this.to != other.to) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 23 * hash + this.from;
        hash = 23 * hash + this.to;
        hash = 23 * hash + this.type;
        return hash;
    }
}
