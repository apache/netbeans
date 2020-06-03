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
package org.netbeans.modules.subversion.remote.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * 
 */
public class Annotations implements ISVNAnnotations {

    private final List<Annotation> annotations = new ArrayList<>();

    public Annotations() {
    }

    protected Annotation getAnnotation(int i) {
        return annotations.get(i);
    }

    @Override
    public void addAnnotation(Annotation annotation) {
       annotations.add(annotation);
    }

    @Override
    public long getRevision(int lineNumber) {
        return getAnnotation(lineNumber).revision;
    }

    @Override
    public String getAuthor(int lineNumber) {
       return getAnnotation(lineNumber).getAuthor();
    }

    @Override
    public Date getChanged(int lineNumber) {
       return getAnnotation(lineNumber).getChanged();
    }

    @Override
    public String getLine(int lineNumber) {
        return getAnnotation(lineNumber).getLine();
    }

    @Override
    public int numberOfLines() {
        return annotations.size();
    }
    
    public static class Annotation {

        private final long revision;
        private final String author;
        private final Date changed;
        private String line;

        public Annotation(long revision, String author, Date changed, String line) {
            this.revision = revision;
            this.author = author;
            this.changed = changed;
            this.line = line;
        }

        public String getAuthor() {
            return author;
        }

        public Date getChanged() {
            return changed;
        }

        public String getLine() {
           return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public long getRevision() {
            return revision;
        }
    }
}
