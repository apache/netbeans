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
package org.netbeans.modules.php.editor.parser.annotation;

import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LinkParsedLine implements AnnotationParsedLine {
    private final String description;

    public LinkParsedLine(final String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return LinkLineParser.ANNOTATION_NAME;
    }

    @Override
    public String getDescription() {
        return description;
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
