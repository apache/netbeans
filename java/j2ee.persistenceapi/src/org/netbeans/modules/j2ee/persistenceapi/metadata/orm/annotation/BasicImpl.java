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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class BasicImpl implements Basic {

    private final String name;
    private final Column column;
    private final String temporal;
    private final ParseResult parseResult;

    public BasicImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String name, Column column, String temporal) {
        this.name = name;
        this.column = column;
        this.temporal = temporal;
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectPrimitive("optional", Boolean.class, parser.defaultValue(true));
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
    }

    public void setFetch(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getFetch() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setOptional(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isOptional() {
        return parseResult.get("optional", Boolean.class); // NOI18N
    }

    public void setColumn(Column value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Column getColumn() {
        return column;
    }

    public Column newColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setLob(Lob value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Lob getLob() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Lob newLob() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTemporal(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getTemporal() {
        return temporal;
    }

    public void setEnumerated(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getEnumerated() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
