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

package org.netbeans.modules.maven.indexer.api;

/**
 * 
 * @author mkleint
 */
public final class QueryField {

    public static final String FIELD_ANY = "any";
    public static final String FIELD_GROUPID = "groupId";
    public static final String FIELD_ARTIFACTID = "artifactId";
    public static final String FIELD_VERSION = "version";
    /**
     * field for searching the class content of the artifact,
     * please note this search is prone to throw TooManyClausesException from
     * lucene if too generic term is used.
     */
    public static final String FIELD_CLASSES = "classes";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PACKAGING = "packaging";

    
    public static final int MATCH_EXACT = 0;
    public static final int MATCH_ANY = 1;
    
    public static final int OCCUR_MUST = 0;
    public static final int OCCUR_SHOULD = 1;
    
    private int match = MATCH_ANY;
    private String field = FIELD_ANY;
    private int occur = OCCUR_SHOULD;

    public int getOccur() {
        return occur;
    }

    public void setOccur(int occur) {
        this.occur = occur;
    }
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }
    
    
}
