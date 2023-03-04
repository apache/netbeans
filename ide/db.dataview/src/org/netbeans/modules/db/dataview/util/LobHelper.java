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
package org.netbeans.modules.db.dataview.util;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LobHelper {

    private static final Logger LOG = Logger.getLogger(LobHelper.class.getName());

    private static final Comparator<Blob> blobComparator
            = new Comparator<Blob>() {
                public int compare(Blob o1, Blob o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    try {
                        return Long.compare(o1.length(), o2.length());
                    } catch (SQLException ex) {
                        return 0;
                    }
                }
            };

    private static final Comparator<Clob> clobComparator
            = new Comparator<Clob>() {
                public int compare(Clob o1, Clob o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    String s1 = clobToString(o1);
                    String s2 = clobToString(o2);
                    return s1.compareToIgnoreCase(s2);
                }
            };

    private static final StringValue blobConverter
            = new StringValue() {

                @Override
                public String getString(Object o) {
                    if (o == null) {
                        return "";
                    } else if (o instanceof Blob) {
                        return blobToString((Blob) o);
                    } else {
                        return "<Illegal value>";
                    }
                }

            };

    private static final StringValue clobConverter
            = new StringValue() {

                @Override
                public String getString(Object o) {
                    if (o == null) {
                        return "";
                    } else if (o instanceof Clob) {
                        return clobToString((Clob) o);
                    } else {
                        return "<Illegal value>";
                    }
                }

            };

    public static Comparator<Blob> getBlobComparator() {
        return blobComparator;
    }

    public static Comparator<Clob> getClobComparator() {
        return clobComparator;
    }

    public static StringValue getBlobConverter() {
        return blobConverter;
    }

    public static StringValue getClobConverter() {
        return clobConverter;
    }

    public static String blobToString(Blob blob) {
        try {
            Long size = blob.length();
            StringBuilder stringValue = new StringBuilder();
            stringValue.append("<BLOB ");
            if (size < 1000) {
                stringValue.append(String.format("%1$d bytes", size));
            } else if (size < 1000000) {
                stringValue.append(String.format("%1$d kB", size / 1000));
            } else {
                stringValue.append(String.format("%1$d MB", size / 1000000));
            }
            stringValue.append(">");
            return stringValue.toString();
        } catch (SQLException ex) {
            return "<BLOB of unkown size>";
        }
    }

    public static String clobToString(Clob clobValue) {
        StringBuilder contentPart = new StringBuilder();
        try {
            long size = clobValue.length();
            long retrievalCount = Math.min(size, 255);
            String sampleContent = clobValue.getSubString(1, (int) retrievalCount);
            contentPart.append(sampleContent.replaceAll("[\n\r]+", " "));//NOI18N
            if (size > 255) {
                contentPart.append(" [...]");                           //NOI18N
            }
            return contentPart.toString();
        } catch (SQLException ex) {
            LOG.log(Level.INFO,
                    "Failed to retrieve CLOB content", //NOI18N
                    ex);
            return clobToDescription(clobValue);
        }
    }

    public static String clobToDescription(Clob clobValue) {
        StringBuilder clobDescription = new StringBuilder("<CLOB ");    //NOI18N

        try {
            long size = clobValue.length();
            if (size < 1000) {
                clobDescription.append(String.format("%1$d Chars", size)); //NOI18N
            } else if (size < 1000000) {
                clobDescription.append(String.format("%1$d kChars", size / 1000)); //NOI18N
            } else {
                clobDescription.append(String.format("%1$d MChars", size
                        / 1000000)); //NOI18N
            }
        } catch (SQLException ex) {
            clobDescription.append("of unknown size");                  //NOI18N
        }
        clobDescription.append(">");

        return clobDescription.toString();
    }
}
