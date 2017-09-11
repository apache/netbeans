/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

/**
 *
 * @author ahimanikya
 */
import java.sql.Blob;
import java.sql.Clob;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import static org.netbeans.modules.db.dataview.table.SuperPatternFilter.MODE.LITERAL_FIND;
import org.netbeans.modules.db.dataview.util.LobHelper;

public class SuperPatternFilter extends RowFilter<TableModel, Integer> {

    Pattern pattern;
    String filterStr = "";
    MODE mode;
    private static final String UNKOWN_MODE = "unknown mode";
    private final int col;

    public static enum MODE {
        LITERAL_FIND, REGEX_FIND, LITERAL_MATCH, REGEX_MATCH
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SuperPatternFilter(final int col) {
        this.col = col;
        setFilterStr(null, LITERAL_FIND);
    }

    public boolean isFilterSetTo(final String rack, final MODE matchMode) {
        return filterStr.equals(rack) && mode == matchMode;
    }

    public void setFilterStr(final String filterStr, final MODE mode) {
        if (filterStr == null || this.filterStr.equals(filterStr) && this.mode == mode) {
            return;
        }
        this.filterStr = filterStr;
        this.mode = mode;
        switch (mode) {
            case LITERAL_FIND:
            case LITERAL_MATCH:
                break;
            case REGEX_FIND:
            case REGEX_MATCH:
                final String filterStr2;
                if (filterStr == null || filterStr.length() == 0) {
                    filterStr2 = ".*";
                } else {
                    filterStr2 = filterStr;
                }
                pattern = Pattern.compile(filterStr2, 0);
                break;
            default:
                throw new RuntimeException(UNKOWN_MODE);
        }
    }

    public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        return testValue(entry.getStringValue(col));
    }

    protected boolean testValue(final Object value) {
        if (value == null) {
            return false;
        }
        final String valueStr;
        if (value instanceof Blob) {
            valueStr = LobHelper.blobToString((Blob) value);
        } else if (value instanceof Clob) {
            valueStr = LobHelper.clobToString((Clob) value);
        } else {
            valueStr = value.toString();
        }
        switch (mode) {
            case LITERAL_FIND:
                if (filterStr == null || filterStr.length() == 0) {
                return true;
            } else {
                return valueStr.toUpperCase().contains(filterStr.toUpperCase());
            }
            case LITERAL_MATCH:
                if (filterStr == null || filterStr.length() == 0) {
                return true;
            } else {
                return filterStr.equals(valueStr);
            }
            case REGEX_FIND:
                return pattern.matcher(valueStr).find();
            case REGEX_MATCH:
                return pattern.matcher(valueStr).matches();
            default:
                throw new RuntimeException(UNKOWN_MODE);
        }
    }
}
