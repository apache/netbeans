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

package org.netbeans.modules.versioning.diff;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import javax.swing.text.Document;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Provides utility methods related to diffing.
 *
 * @author Marian Petras
 * @since 1.9.1
 */
public final class DiffUtils {

    private DiffUtils() {}

    /**
     * Makes an HTML display name for the given node, if the node should be
     * rendered with a non-default font and/or color. The returned string
     * (if non-{@code null}) does not contain the leading
     * {@code &quot;&lt;html&gt;&quot;} string that in many cases neccessary
     * if the HTML markup should be interpreted during rendering.
     *
     * @param  node  node to make a display name for
     * @param  fileModified  {@code true} if the file represented by the node
     *                       is modified (contains unsaved changes)
     * @param  selected  if the node is selected (in a table, in a tree, ...)
     * @return  string with HTML markups included,
     *          or {@code null} if no HTML markup is neccessary
     * @since 1.9.1
     */
    public static String getHtmlDisplayName(Node node,
                                            boolean fileModified,
                                            boolean selected) {
        boolean bold = fileModified;
        boolean colored = !selected;

        if (!bold && !colored) {
            return null;            //no HTML markup necessary
        }

        String normal = colored ? node.getHtmlDisplayName()
                                : node.getName();
        return bold ? makeBold(normal)
                    : normal;
    }

    private static String makeBold(String text) {
        return new StringBuilder(text.length() + 7)
               .append("<b>").append(text).append("</b>")               //NOI18N
               .toString();
    }

    /**
     * @since 1.9.1
     */
    public static DataObject[] setupsToDataObjects(AbstractDiffSetup[] setups) {
        return setupsToDataObjects(setups, false);
    }

    /**
     * @param  exactPositions  if {@code true}, the returned array will be of
     *                         the same length as the passed array the
     *                         {@code DataObject}s in it will be exactly the
     *                         same positions as the corresponding {@code Setup}
     *                         objects in the passed array;
     *                         if {@code false} the returned array will only
     *                         contain non-{@code null} {@code DataObject}s
     * @since 1.9.1
     */
    public static DataObject[] setupsToDataObjects(AbstractDiffSetup[] setups,
                                                   boolean exactPositions) {
        if (setups == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        DataObject[] dataObjects = new DataObject[setups.length];

        int count = 0;
        for (int i = 0; i < setups.length; i++) {
            AbstractDiffSetup setup = setups[i];
            if (setup == null) {
                continue;
            }
            DataObject dataObj = getDataObject(setup);
            if (dataObj == null) {
                continue;
            }
            int resultIndex = exactPositions ? i : count++;
            dataObjects[resultIndex] = dataObj;
        }
        return exactPositions
               ? dataObjects
               : CollectionUtils.shortenArray(dataObjects, count);
    }

    /**
     * @since 1.9.1
     */
    public static DataObject getDataObject(AbstractDiffSetup setup) {
        return getDataObject(setup.getSecondSource());
    }

    /**
     * @since 1.9.1
     */
    public static DataObject getDataObject(StreamSource source) {
        if (!source.isEditable()) {
            return null;
        }

        Lookup lookup = source.getLookup();

        FileObject fileObj = lookup.lookup(FileObject.class);
        if (fileObj != null) {
            try {
                return DataObject.find(fileObj);
            } catch (Exception ex) {
                // OK, we will try another approach (below)
            }
        }

        Document streamDoc = lookup.lookup(Document.class);
        if (streamDoc != null) {
            Object o = streamDoc.getProperty(Document.StreamDescriptionProperty);
            if (o instanceof DataObject) {
                return (DataObject) o;
            }
        }

        return null;
    }

    /**
     * <p>Finds {@code EditorCookie}s for the given diff setups.
     * For each setup, this routine tries to find an instance of
     * {@code EditorCookie.Observable}. If it fails, it tries to find at least
     * an instance of plain {@code EditorCookie} (non-observable).
     * </p><p>
     * {@code EditorCookie}s in the returned array are stored in the order such
     * that an {@code EditorCookie} for a given diff setup is located at the
     * same index as the setup for which the {@code EditorCookie} was found.
     * If no {@code EditorCookie} was found for a given diff setup, the
     * resulting array contains {@code null} at the corresponding index.</p>
     *
     * @param  setups  diff setups to find {@code EditorCookie}s for
     * @return  array of {@code EditorCookie}s and/or {@code null}s
     * @exception  java.lang.IllegalArgumentException
     *             if the passed array of diff setups is {@code null}
     * @since 1.9.1
     */
    public static EditorCookie[] setupsToEditorCookies(AbstractDiffSetup[] setups) {
        if (setups == null) {
            throw new IllegalArgumentException("null");
        }

        EditorCookie[] editorCookies = new EditorCookie[setups.length];
        for (int i = 0; i < setups.length; i++) {
            editorCookies[i] = getEditorCookie(setups[i]);
        }
        return editorCookies;
    }

    /**
     * @since 1.9.1
     */
    public static EditorCookie getEditorCookie(AbstractDiffSetup setup) {
        return getEditorCookie(setup.getSecondSource());
    }

    /**
     * @since 1.9.1
     */
    public static EditorCookie getEditorCookie(StreamSource source) {
        if (!source.isEditable()) {
            return null;
        }

        Lookup lookup = source.getLookup();

        EditorCookie plain = null;

        FileObject fileObj = lookup.lookup(FileObject.class);
        if (fileObj != null) {
            try {
                EditorCookie editorCookie = getEditorCookie(
                                                DataObject.find(fileObj), true);
                if (editorCookie instanceof EditorCookie.Observable) {
                    return (EditorCookie.Observable) editorCookie;
                }

                plain = editorCookie;
            } catch (Exception e) {
                // fallback to other means of obtaining the source
            }
        }

        Document streamDoc = lookup.lookup(Document.class);
        if (streamDoc != null) {
            return chooseBetterEditorCookie(getEditorCookie(streamDoc), plain);
        }

        return plain;       //null or non-null
    }

    /**
     * @since 1.9.1
     */
    private static EditorCookie getEditorCookie(DataObject dataObj,
                                                boolean tryDocument) {
        if (dataObj == null) {
            return null;
        }

        EditorCookie plain = null;

        EditorCookie editorCookie = dataObj.getCookie(EditorCookie.class);
        if (editorCookie instanceof EditorCookie.Observable) {
            return (EditorCookie.Observable) editorCookie;
        }

        plain = editorCookie;

        if (tryDocument && (editorCookie != null)) {
            try {
                return chooseBetterEditorCookie(
                           getEditorCookie(editorCookie.openDocument()), plain);
            } catch (IOException ex) {
                //ok, will fall to the final return below
            }
        }

        return plain;       //null or non-null
    }

    /**
     * @since 1.9.1
     */
    public static EditorCookie getEditorCookie(Document doc) {
        if (doc == null) {
            return null;
        }

        DataObject dataObj = (DataObject) doc.getProperty(
                                            Document.StreamDescriptionProperty);
        if (dataObj == null) {
            return null;
        }

        EditorCookie plain = null;

        if (dataObj instanceof MultiDataObject) {
            MultiDataObject multiDataObj = (MultiDataObject) dataObj;
            for (MultiDataObject.Entry entry : multiDataObj.secondaryEntries()) {
                if (entry instanceof CookieSet.Factory) {
                    CookieSet.Factory factory = (CookieSet.Factory) entry;
                    EditorCookie ec = factory.createCookie(EditorCookie.class);
                    if (ec.getDocument() == doc) {
                        if (ec instanceof EditorCookie.Observable) {
                            return (EditorCookie.Observable) ec;
                        }

                        if (plain == null) {
                            plain = ec;
                        }
                    }
                }
            }
        }

        return chooseBetterEditorCookie(getEditorCookie(dataObj, false), plain);
    }

    /**
     * <p>Selects better of the given two {@code EditorCookie}s.
     * The first argument is supposed to refer a just obtained
     * {@code EditorCookie} (or {@null}), which has not been probed yet.
     * The second argument is supposed to refer an earlier probed
     * {@code EditorCookie}. It is also assumed that the second argument
     * does not refer an observable {@code EditorCookie} as there would be no
     * need for calling this method in such a case.</p>
     *
     * <p>The criteria for choosing better {@code EditorCookie} are:</p>
     * <ol>
     *     <li>if the first argument refers an instance of
     *         {@code EditorCookie.Observable}, then it is returned</li>
     *     <li>otherwise, if the second argument refers an instance of
     *         {@code EditorCookie} (non-observable), then it is returned</li>
     *     <li>otherwise, the first argument is returned,
     *         whether {@code null} or {@code non-null}</li>
     * </ol>
     *
     * @param  unchecked  editor cookie that was just obtained from some routine
     *                    and has not been examined yet
     *                    (observable/plain/{@code null})
     * @param  firstPlain  the best {@code EditorCookie} (non-observable)
     *                     we have so far - may be {@code null}
     * @return  better one of the given {@code EditorCookie}s,
     *          or {@code null} if both arguments were {@code null}
     */
    private static EditorCookie chooseBetterEditorCookie(EditorCookie unchecked,
                                                         EditorCookie firstPlain) {
        assert !(firstPlain instanceof EditorCookie.Observable);

        EditorCookie result;

        if (unchecked instanceof EditorCookie.Observable) {
            result = (EditorCookie.Observable) unchecked;
        } else if (firstPlain != null) {
            result = firstPlain;
        } else {
            result = unchecked;     //null or non-null
        }

        return result;
    }

    public static void cleanThoseUnmodified(EditorCookie[] editorCookies) {
        for (int i = 0; i < editorCookies.length; i++) {
            EditorCookie editorCookie = editorCookies[i];
            if (editorCookie == null) {
                continue;
            }
            if (!editorCookie.isModified()) {
                editorCookies[i] = null;
            }
        }
    }

    public static void cleanThoseWithEditorPaneOpen(
                                                EditorCookie[] editorCookies) {
        for (int i = 0; i < editorCookies.length; i++) {
            EditorCookie editorCookie = editorCookies[i];
            if (editorCookie == null) {
                continue;
            }
            if (editorCookie.getOpenedPanes() != null) {
                editorCookies[i] = null;
            }
        }
    }
    
    /**
     * 
     * @param file1 first file to compare
     * @param file2 second file to compare
     * @return differences between the two files, can be null if the content cannot be acquired
     * @throws IOException 
     */
    public static Difference[] getDifferences (File file1, File file2) throws IOException {
        DiffProvider diffProvider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffProvider == null) {
            return null;
        }
        Reader currentReader = null, previousReader = null;
        try {
            currentReader = Utils.createReader(file1);
            previousReader = Utils.createReader(file2);
            return diffProvider.computeDiff(currentReader, previousReader);
        } finally {
            if (currentReader != null) {
                try {
                    currentReader.close();
                } catch (IOException ex) {}
            }
            if (previousReader != null) {
                try {
                    previousReader.close();
                } catch (IOException ex) {}
            }
        }
    }
    
    /**
     * 
     * @param originalLineNumber 0-based
     * @return 0-based line number in the previous file
     * @throws IOException 
     */
    public static int getMatchingLine (File currentFile, File previousFile, int originalLineNumber) throws IOException {
        Difference[] diffs = getDifferences(currentFile, previousFile);
        if (diffs == null) {
            return -1;
        } else {
            return getMatchingLine(diffs, originalLineNumber);
        }
    }
    
    public static int getMatchingLine (Reader currentReader, Reader previousReader, int originalLineNumber) throws IOException {
        DiffProvider diffProvider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffProvider == null || currentReader == null || previousReader == null) {
            return -1;
        }
        Difference[] differences = diffProvider.computeDiff(currentReader, previousReader);
        return getMatchingLine(differences, originalLineNumber);
    }

    private static int getMatchingLine (Difference[] differences, int originalLineNumber) {
        int line = ++originalLineNumber;
        for (Difference diff : differences) {
            boolean end = false;
            switch (diff.getType()) {
                case Difference.ADD:
                    if (diff.getFirstStart() >= originalLineNumber) {
                        end = true;
                        break;
                    }
                    line += diff.getSecondEnd() - diff.getSecondStart() + 1;
                    break;
                case Difference.DELETE:
                    if (diff.getFirstStart() <= originalLineNumber && diff.getFirstEnd() >= originalLineNumber) {
                        line = diff.getSecondStart();
                        end = true;
                        break;
                    } else if (diff.getFirstEnd() >= originalLineNumber) {
                        end = true;
                        break;
                    }
                    line -= diff.getFirstEnd() - diff.getFirstStart() + 1;
                    break;
                case Difference.CHANGE:
                    if (diff.getFirstStart() <= originalLineNumber && diff.getFirstEnd() >= originalLineNumber) {
                        line = diff.getSecondStart();
                        end = true;
                        break;
                    } else if (diff.getFirstEnd() >= originalLineNumber) {
                        end = true;
                        break;
                    }
                    line -= diff.getFirstEnd() - diff.getFirstStart();
                    line += diff.getSecondEnd() - diff.getSecondStart();
                    break;
            }
            if (end) {
                break;
            }
        }
        return line - 1;
    }

}
