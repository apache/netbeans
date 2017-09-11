/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "sql")
public class SQLHistoryEntry {

    private String url;
    private String sql;
    private Date date;

    protected SQLHistoryEntry() {
    }

    public SQLHistoryEntry(String url, String sql, Date date) {
        this.url = url;
        this.sql = sql;
        this.date = date;
    }

    @XmlTransient
    public Date getDate() {
        return date;
    }

    protected void setDate(Date date) {
        this.date = date;
    }

    @XmlValue
    public String getSql() {
        return sql;
    }

    protected void setSql(String sql) {
        if (sql != null) {
            this.sql = sql.trim();
        } else {
            this.sql = sql;
        }
    }

    @XmlAttribute
    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute(name = "date")
    protected String getDateXMLVariant() {
        if (this.date == null) {
            return null;
        } else {
            return Long.toString(date.getTime());
        }
    }

    protected void setDateXMLVariant(String value) {
        try {
            date = new Date(Long.parseLong(value));
        } catch (NumberFormatException nfe) {
            // #152486 - previously date stored in text format
            try {
                date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).parse(value);
            } catch (ParseException pe) {
                // # 152486; Date stored is not parsable, so reset the date to the current timestamp
                date = new Date();
            }
        }

    }

    @Override
    public String toString() {
        return "SQLHistoryEntry{" + "url=" + url + ", sql=" + sql + ", date=" + date + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SQLHistoryEntry other = (SQLHistoryEntry) obj;
        if ((this.url == null) ? (other.url != null) : !this.url.toLowerCase().equals(other.url.toLowerCase())) {
            return false;
        }
        if ((this.sql == null) ? (other.sql != null) : !this.sql.toLowerCase().equals(other.sql.toLowerCase())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.sql != null ? this.sql.toLowerCase().hashCode() : 0);
        return hash;
    }
}
