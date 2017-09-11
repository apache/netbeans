/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.matcher;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.search.MatchingObject.Def;
import org.netbeans.modules.search.TextRegexpUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jhavlin
 */
public class DefaultMatcher extends AbstractMatcher {

    /**
     * maximum size of file of unrecognized file that will be searched. Files of
     * uknown type that whose size exceed this limit will be considered binary
     * and will not be searched.
     */
    private static final int MAX_UNRECOGNIZED_FILE_SIZE = 5 * (1 << 20); //5 MiB
    /**
     * array of searchable application/x-<em>suffix</em> MIME-type suffixes
     */
    private static final Collection<String> searchableXMimeTypes;
    private static final Collection<String> searchableExtensions;

    static {
        searchableXMimeTypes = new HashSet<String>(17);
        searchableXMimeTypes.add("csh");                                //NOI18N
        searchableXMimeTypes.add("httpd-eruby");                        //NOI18N
        searchableXMimeTypes.add("httpd-php");                          //NOI18N
        searchableXMimeTypes.add("httpd-php-source");                   //NOI18N
        searchableXMimeTypes.add("javascript");                         //NOI18N
        searchableXMimeTypes.add("latex");                              //NOI18N
        searchableXMimeTypes.add("php");                                //NOI18N
        searchableXMimeTypes.add("sh");                                 //NOI18N
        searchableXMimeTypes.add("tcl");                                //NOI18N
        searchableXMimeTypes.add("tex");                                //NOI18N
        searchableXMimeTypes.add("texinfo");                            //NOI18N
        searchableXMimeTypes.add("troff");                              //NOI18N

        searchableExtensions = new HashSet<String>(); //TODO make configurable
        searchableExtensions.add("txt");                                //NOI18N
        searchableExtensions.add("log");                                //NOI18N
    }
    private AbstractMatcher realMatcher;
    private boolean trivial;

    public DefaultMatcher(SearchPattern searchPattern) {

        this.trivial = MatcherUtils.isTrivialPattern(searchPattern);
        if (trivial) {
            realMatcher = new TrivialFileMatcher();
        } else {
            boolean multiline = TextRegexpUtil.canBeMultilinePattern(
                    searchPattern.getSearchExpression());

            realMatcher = multiline
                    ? new MultiLineStreamMatcher(searchPattern)
                    : new SingleLineStreamMatcher(searchPattern);
        }
    }

    @Override
    public Def checkMeasuredInternal(FileObject file,
            SearchListener listener) {

        if (trivial) {
            return realMatcher.check(file, listener);
        } else if (isTextFile(file)) {
            try {
                return realMatcher.check(file, listener);
            } catch (Exception e) {
                listener.fileContentMatchingError(file.getPath(), e);
                return null;
            }
        } else {
            listener.fileSkipped(file, null, "Not a text file");
            return null;
        }
    }

    @Override
    public synchronized void terminate() {
        realMatcher.terminate();
    }

    /**
     * Checks whether the given file is a text file. The current implementation
     * does the check by the file's MIME-type.
     *
     * @param fileObj file to be checked
     * @return {@code true} if the file is a text file; {@code false} if it is a
     * binary file
     */
    private static boolean isTextFile(FileObject fileObj) {
        String mimeType = fileObj.getMIMEType();

        if (mimeType.equals("content/unknown")) {                       //NOI18N
            if (searchableExtensions.contains(fileObj.getExt().toLowerCase())) {
                return true;
            } else {
                return fileObj.getSize() <= MAX_UNRECOGNIZED_FILE_SIZE ||
                        hasTextContent(fileObj);
            }
        }

        if (mimeType.startsWith("text/")) {                             //NOI18N
            return true;
        }

        if (mimeType.startsWith("application/")) {                      //NOI18N
            final String subtype = mimeType.substring(12);
            return subtype.equals("rtf") //NOI18N
                    || subtype.equals("sgml") //NOI18N
                    || subtype.startsWith("xml-") //NOI18N
                    || subtype.endsWith("+xml") //NOI18N
                    || isApplicationXSource(subtype, fileObj);
        }

        return mimeType.endsWith("+xml");                               //NOI18N
    }

    /**
     * Check whether this is a text file with MIME type application/x-something.
     * See issue #88210.
     *
     * @param subtype of MIME type, after "application/", e.g. x-php
     * @param fo File object containing the data.
     */
    private static boolean isApplicationXSource(String subtype, FileObject fo) {
        return (subtype.startsWith("x-") //NOI18N
                && (searchableXMimeTypes.contains(subtype.substring(2))
                || hasTextContent(fo)));
    }

    /**
     * Try to detect whether the content of the file is textual.
     *
     * @return True if the file seems to be a text file, false if it seems to be
     * a binary file.
     */
    static boolean hasTextContent(FileObject fo) {
        byte[] bytes = new byte[1024]; // check the first kB of the file
        byte fe = (byte) 0xFE, ff = (byte) 0xFF, oo = (byte) 0x00,
                ef = (byte) 0xEF, bf = (byte) 0xBF, bb = (byte) 0xBB;
        try {
            InputStream is = fo.getInputStream();
            try {
                int read = is.read(bytes);
                if (read > 2 && bytes[0] == ef && bytes[1] == bb
                        && bytes[2] == bf) {
                    return true; //UTF-8 BOM
                } else if (read > 1 && ((bytes[0] == ff) && bytes[1] == fe
                        || (bytes[0] == fe && bytes[1] == ff))) {
                    return true; // UTF-16
                } else if (read > 3 && ((bytes[0] == oo && bytes[1] == oo
                        && bytes[2] == fe && bytes[3] == ff)
                        || (bytes[0] == ff && bytes[1] == fe
                        && bytes[2] == oo && bytes[3] == oo))) {
                    return true; //UTF-32
                } else {
                    for (int i = 0; i < read; i++) {
                        if (bytes[i] == oo) {
                            return false; // zero-byte found
                        }
                    }
                    return true; // no zero-byte found
                }
            } finally {
                is.close();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void setStrict(boolean strict) {
        super.setStrict(strict);
        realMatcher.setStrict(strict);
    }
}
