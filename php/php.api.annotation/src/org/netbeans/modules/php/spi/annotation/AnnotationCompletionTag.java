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
package org.netbeans.modules.php.spi.annotation;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.openide.util.Parameters;

/**
 * Class representing code-completion item.
 */
public class AnnotationCompletionTag {

    private final String name;
    private final String insertTemplate;
    private final String documentation;


    /**
     * Create new annotation tag without documentation.
     * @param name tag name; never {@code null}
     * @param insertTemplate text that it inserted to the source file; can't be {@code null}
     */
    public AnnotationCompletionTag(@NonNull String name, @NonNull String insertTemplate) {
        this(name, insertTemplate, null);
    }

    /**
     * Create new annotation tag with documentation.
     * @param name tag name; never {@code null}
     * @param insertTemplate text that it inserted to the source file; never {@code null}
     * @param documentation documentation of the tag, HTML allowed; can be {@code null}
     */
    public AnnotationCompletionTag(@NonNull String name, @NonNull String insertTemplate, @NullAllowed String documentation) {
        Parameters.notEmpty("name", name);
        Parameters.notEmpty("insertTemplate", insertTemplate);

        this.name = name;
        this.insertTemplate = insertTemplate;
        this.documentation = documentation;
    }

    /**
     * Get tag name.
     * @return tag name
     */
    public final String getName() {
        return name;
    }

    /**
     * Get text that it inserted to the source file.
     * @return text that it inserted to the source file
     */
    public final String getInsertTemplate() {
        return insertTemplate;
    }

    /**
     * Get documentation of the tag.
     * @return documentation of the tag; can be {@code null}
     */
    @CheckForNull
    public final String getDocumentation() {
        return documentation;
    }

    /**
     * Format parameter part of the signature of this tag.
     * @param formatter formatter to be used for formatting
     */
    public void formatParameters(HtmlFormatter formatter) {
    }

    @Override
    public boolean equals(@NullAllowed Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnotationCompletionTag other = (AnnotationCompletionTag) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.insertTemplate == null) ? (other.insertTemplate != null) : !this.insertTemplate.equals(other.insertTemplate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.insertTemplate != null ? this.insertTemplate.hashCode() : 0);
        return hash;
    }

}
