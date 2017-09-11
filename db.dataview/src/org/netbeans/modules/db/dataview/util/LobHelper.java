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
package org.netbeans.modules.db.dataview.util;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingx.renderer.StringValue;

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
