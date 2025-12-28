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

package org.netbeans.modules.javascript.cdnjs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representation of a version suitable for sorting/comparison. There are
 * no restrictions on the format of the version. Dot-separated versions
 * of arbitrary length (4.1.3.2), textual versions (alpha, beta) and their
 * mixtures (1.3.2-beta-b20) are all supported.
 *
 * @author Jan Stola
 */
public final class Version {
    /**
     * Set of suffixes that when appended to a version represent
     * an earlier version than the original one. For example, {@code alpha}
     * is a member of this set because {@code 1.0.3-alpha} is an earlier version
     * than {@code 1.0.3}.
     */
    private static final Set<String> PRE_VERSIONS = new HashSet<>();
    static {
        PRE_VERSIONS.add("pre"); // NOI18N
        PRE_VERSIONS.add("dev"); // NOI18N
        PRE_VERSIONS.add("alpha"); // NOI18N
        PRE_VERSIONS.add("beta"); // NOI18N
        PRE_VERSIONS.add("rc"); // NOI18N
    }

    /**
     * Parses a textual representation of a version.
     * 
     * @param versionName textual representation of a version.
     * @return {@code Version} object that corresponds to the given
     * textual representation.
     */
    public static Version parse(String versionName) {
        List<Fragment> list = new ArrayList<>();
        list.add(new Fragment(0, "")); // NOI18N
        boolean isNumber = false; // Are we parsing number fragment?
        boolean isText = false; // Are we parsing text fragment?
        long number = 0;
        StringBuilder textBuilder = new StringBuilder();
        // The trailing dot ensures that the last fragment
        // is ended and added into the list
        String version = versionName + "."; // NOI18N
        for (int i=0; i<version.length(); i++) {
            char c = version.charAt(i);
            boolean digit = ('0' <= c && c <= '9');
            boolean letter = ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
            if (isNumber && !digit) {
                // End of number fragment
                isNumber = false;
                list.add(new Fragment(number, "")); // NOI18N
                number = 0;
            } else if (isText && !letter) {
                // End of text fragment
                isText = false;
                String text = textBuilder.toString();
                if (PRE_VERSIONS.contains(text.toLowerCase())) {
                    Fragment previous = list.get(list.size()-1);
                    list.set(list.size()-1, new Fragment(previous.getNumber()-1, previous.getText()));
                    list.add(new Fragment(Long.MAX_VALUE, text));
                } else {
                    list.add(new Fragment(0, text));
                }
                textBuilder.setLength(0);
            }
            if (digit) {
                isNumber = true;
                number *= 10;
                number += (c - '0');
            } else if (letter) {
                isText = true;
                textBuilder.append(c);
            }
        }
        return new Version(list);
    }

    /** Fragments of this version. */
    private final Fragment[] fragments;

    /**
     * Creates a new {@code Version} from the given list of version fragments.
     * 
     * @param fragmentList list of fragments that represent the version.
     */
    private Version(List<Fragment> fragmentList) {
        fragments = fragmentList.toArray(new Fragment[0]);
    }

    /**
     * Returns the length of this version (i.e. number of fragments this
     * version consists of).
     * 
     * @return number of fragments this version consists of.
     */
    private int getFragmentCount() {
        return fragments.length;
    }

    /**
     * Returns the version fragment on the specified position.
     * 
     * @param index index of the fragment to return.
     * @return fragment on the specified position.
     */
    private Fragment getFragment(int index) {
        return fragments[index];
    }

    /**
     * Version fragment. Version fragment is a part of a version. It represents
     * either a list of consecutive digits or a list of consecutive letters
     * in the textual representation of the version. Note that neither
     * the {@code number} property nor {@code text} property of this objects
     * matches necessarily to the represented section of the version.
     */
    private static final class Fragment {
        /** Numerical value of this fragment. */
        private final long number;
        /** Textual value of this fragment. */
        private final String text;

        /**
         * Creates a new version fragment.
         * 
         * @param number numerical value of the fragment.
         * @param text textual value of the fragment.
         */
        Fragment(long number, String text) {
            assert text != null;
            this.number = number;
            this.text = text;
        }

        /**
         * Returns the numerical value of the fragment.
         * 
         * @return numerical value of the fragment.
         */
        long getNumber() {
            return number;
        }

        /**
         * Returns the textual value of the fragment.
         * 
         * @return textual value of the fragment.
         */
        String getText() {
            return text;
        }

        @Override
        public String toString() {
            return (text == null) ? Long.toString(number) : "(" + number + "," + text + ")"; // NOI18N
        }
    }

    /**
     * Comparator of {@code Version} objects.
     */
    public static final class Comparator implements java.util.Comparator<Version> {
        /** Instance of the comparator that sorts the versions in an ascending order. */
        private static final java.util.Comparator<Version> ASCENDING_INSTANCE = new Comparator();
        /** Instance of the comparator that sorts the version in a descending order. */
        private static final java.util.Comparator<Version> DESCENDING_INSTANCE = new java.util.Comparator<Version>() {
            @Override
            public int compare(Version version1, Version version2) {
                return ASCENDING_INSTANCE.compare(version2, version1);
            }
        };

        /**
         * Returns a shared instance of the comparator.
         * 
         * @param ascending if {@code true} then the returned comparator
         * sorts the versions in an ascending order, it sorts the versions
         * in a descending order otherwise.
         * @return shared instance of the comparator.
         */
        public static java.util.Comparator<Version> getInstance(boolean ascending) {
            return ascending ? ASCENDING_INSTANCE : DESCENDING_INSTANCE;
        }

        @Override
        public int compare(Version version1, Version version2) {
            int minLength = Math.min(version1.getFragmentCount(), version2.getFragmentCount());
            for (int i=0; i<minLength; i++) {
                Fragment fragment1 = version1.getFragment(i);
                Fragment fragment2 = version2.getFragment(i);
                long number1 = fragment1.getNumber();
                long number2 = fragment2.getNumber();
                if (number1 < number2) {
                    return -1;
                } else if (number1 > number2) {
                    return 1;
                } else {
                    // Numerical values are equal => compare the textual values
                    int textDiff = fragment1.getText().compareTo(fragment2.getText());
                    if (textDiff != 0) {
                        return textDiff;
                    }
                }
            }
            return version1.getFragmentCount() - version2.getFragmentCount();
        }

    }

}
