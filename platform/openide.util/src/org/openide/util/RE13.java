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
package org.openide.util;

import java.util.*;


/** Implementation of translate regular expression methods.
 * Works on 1.3 though we could use 1.4's java.util.regex - this is faster anyway.
 * @author  Jaroslav Tulach
 */
final class RE13 implements BaseUtilities.RE {
    /** root of the match automata */
    private Object[] root;

    /** list of strings to convert to */
    private String[] results;

    /** Parses line of text to two parts: the key and the rest
     */
    public String[] readPair(String line) {
        int indx = line.indexOf('#');

        if (indx != -1) {
            line = line.substring(0, indx).trim();
        }

        indx = line.indexOf('=');

        if (indx == -1) {
            indx = line.indexOf(' ');
        }

        if (indx == -1) {
            return null;
        }

        return new String[] { line.substring(0, indx).trim(), line.substring(indx + 1).trim() };
    }

    public String convert(String pattern) {
        Object[] item = root;
        int resIndex = -1;
        int resLength = 0;

        int lenOfPattern = 0;
        int indx = 0;
ALL: 
        for (;;) {
            if (item.length == 0) {
                break;
            }

            // update the result if this item represents a result
            Object last = item[item.length - 1];

            if (last instanceof Integer) {
                // remember the number
                resIndex = ((Integer) last).intValue();
                resLength += lenOfPattern;
                lenOfPattern = 0;
            }

            if (indx >= pattern.length()) {
                // no next text to compare, stop the search
                break;
            }

            char f = pattern.charAt(indx++);

            // find next suitable item
            for (int i = 0; i < item.length; i++) {
                if (item[i] instanceof String && (((String) item[i]).charAt(0) == f)) {
                    // we have found the branch to possibly follow, now check
                    String s = (String) item[i];

                    for (int j = 1; j < s.length(); j++, indx++) {
                        if ((pattern.length() <= indx) || (pattern.charAt(indx) != s.charAt(j))) {
                            // well this is not the right path and there is 
                            // no better => evaluate in this node
                            break ALL;
                        }
                    }

                    // ok, correct convert path
                    item = (Object[]) item[i + 1];
                    lenOfPattern += s.length();

                    continue ALL;
                }
            }

            // no suitable continuation found, if this is end tree item
            // do convertion
            break;
        }

        if (resIndex != -1) {
            // do the conversion
            return results[resIndex] + pattern.substring(resLength);
        } else {
            // no conversion
            return pattern;
        }
    }

    /** Data structure to needed to store the */
    public void init(String[] original, String[] newversion) {
        ArrayList<Object> root = new ArrayList<Object>();

        for (int i = 0; i < original.length; i++) {
            placeString(root, original[i], i);
        }

        this.root = compress(root);
        this.results = newversion;
    }

    /** Places a string to the graph of other strings.
     * @param item list to add the string to
     * @param s string to place there
     * @param indx index to put at the end node
     */
    private static void placeString(List<Object> item, String s, int indx) {
        if (s.length() == 0) {
            item.add(indx);

            return;
        }

        char f = s.charAt(0);

        ListIterator<Object> it = item.listIterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof String) {
                // could be also Integer or array
                String pref = (String) o;

                if (f == pref.charAt(0)) {
                    // find the first difference
                    for (int i = 1; i < pref.length(); i++) {
                        if ((i >= s.length()) || (s.charAt(i) != pref.charAt(i))) {
                            // split in the i-th index
                            it.set(pref.substring(0, i));

                            // next is the list or null
                            List<Object> listForPref = (List<Object>) it.next();

                            List<Object> switchList = new ArrayList<Object>();
                            it.set(switchList);

                            switchList.add(pref.substring(i));
                            switchList.add(listForPref);

                            if (i >= s.length()) {
                                switchList.add(indx);
                            } else {
                                ArrayList<Object> terminalList = new ArrayList<Object>();
                                terminalList.add(indx);

                                switchList.add(s.substring(i));
                                switchList.add(terminalList);
                            }

                            return;
                        }
                    }

                    //
                    // the new string is longer than the existing recursive add
                    //
                    List<Object> switchList = nextList(it);
                    placeString(switchList, s.substring(pref.length()), indx);

                    return;
                }
            }
        }

        //
        // ok new prefix in this item
        //
        ArrayList<Object> id = new ArrayList<Object>();
        id.add(indx);

        item.add(s);
        item.add(id);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> nextList(final ListIterator<Object> it) {
        List<Object> switchList = (List<Object>) it.next();
        return switchList;
    }

    /** Compress tree of Lists into tree of Objects.
     */
    private static Object[] compress(List<Object> item) {
        Object[] arr = new Object[item.size()];

        Integer last = null;

        Iterator<?> it = item.iterator();
        int i = 0;

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof Integer) {
                if (last != null) {
                    throw new IllegalStateException();
                }

                last = (Integer) o;

                continue;
            }

            if (o instanceof String) {
                arr[i++] = ((String) o).intern();

                continue;
            }

            if (o instanceof List) {
                arr[i++] = compress((List) o);

                continue;
            }

            throw new IllegalStateException();
        }

        if (last != null) {
            // assigned integer to this object
            arr[arr.length - 1] = last;
        }

        return arr;
    }
}
