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
package org.netbeans.modules.db.dataview.table;
import java.sql.Blob;
import java.sql.Clob;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import static org.netbeans.modules.db.dataview.table.SuperPatternFilter.MODE.LITERAL_FIND;
import org.netbeans.modules.db.dataview.util.LobHelper;


/**
 *
 * @author ahimanikya
 */
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
