/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * "[Contributor] elects to include this software in this distributigon
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
