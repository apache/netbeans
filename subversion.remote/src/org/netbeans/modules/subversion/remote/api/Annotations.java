/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
