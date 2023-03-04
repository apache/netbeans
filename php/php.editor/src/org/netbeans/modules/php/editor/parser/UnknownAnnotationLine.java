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
package org.netbeans.modules.php.editor.parser;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.openide.util.Parameters;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnknownAnnotationLine implements AnnotationParsedLine {

    private final String name;
    private final String description;

    public UnknownAnnotationLine(final String name, final String description) {
        Parameters.notNull("name", name); //NOI18N
        this.name = name;
        this.description = description;
    }

    public UnknownAnnotationLine(final String name) {
        this(name, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        String result = "";
        if (description != null) {
            result = description;
        }
        return result;
    }

    @Override
    public Map<OffsetRange, String> getTypes() {
        return Collections.<OffsetRange, String>emptyMap();
    }

    @Override
    public boolean startsWithAnnotation() {
        return true;
    }

}
