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

public class TableImpl implements Table {

    private final ParseResult parseResult;

    public TableImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String defaultName) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(defaultName)); // NOI18N
        parser.expectString("schema", parser.defaultValue("")); // NOI18N
        parser.expectString("catalog", parser.defaultValue("")); // NOI18N
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return parseResult.get("name", String.class); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        return parseResult.get("catalog", String.class); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        return parseResult.get("schema", String.class); // NOI18N
    }

    public void setUniqueConstraint(int index, UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint getUniqueConstraint(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUniqueConstraint(UniqueConstraint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint[] getUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint newUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
