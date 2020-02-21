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

package org.netbeans.modules.cnd.utils.cache;

import java.util.Comparator;
import org.openide.util.CharSequences;

/**
 * Utility methods related to character sequences.
 */
public class CharSequenceUtils {

    public static final Comparator<CharSequence> ComparatorIgnoreCase = new CharSequenceComparatorIgnoreCase();

    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return indexOf(text, seq, 0);
    }

    /**
     * Implementation of {@link String#indexOf(char)} for character sequences.
     */
    public static int indexOf(CharSequence text, char c) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        if (fromIndex >= textLength) {
            return (seqLength == 0 ? textLength : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (seqLength == 0) {
            return fromIndex;
        }

        char first = seq.charAt(0);
        int max = textLength - seqLength;

        for (int i = fromIndex; i <= max; i++) {
            // look for first character
            if (text.charAt(i) != first) {
                while (++i <= max && text.charAt(i) != first) {}
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++) {}
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Implementation of {@link String#lastIndexOf(String)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return lastIndexOf(text, seq, text.length());
    }

    /**
     * Implementation of {@link String#lastIndexOf(String,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        int rightIndex = textLength - seqLength;
	if (fromIndex < 0) {
	    return -1;
	}
	if (fromIndex > rightIndex) {
	    fromIndex = rightIndex;
	}
	// empty string always matches
	if (seqLength == 0) {
	    return fromIndex;
	}

        int strLastIndex = seqLength - 1;
	char strLastChar = seq.charAt(strLastIndex);
	int min = seqLength - 1;
	int i = min + fromIndex;

    startSearchForLastChar:
	while (true) {
	    while (i >= min && text.charAt(i) != strLastChar) {
		i--;
	    }

	    if (i < min) {
		return -1;
	    }
	    int j = i - 1;
	    int start = j - (seqLength - 1);
	    int k = strLastIndex - 1;

	    while (j > start) {
	        if (text.charAt(j--) != seq.charAt(k--)) {
		    i--;
		    continue startSearchForLastChar;
		}
	    }
	    return start + 1;
	}
    }

    /**
     * Implementation of {@link String#lastIndexOf(int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch) {
	return lastIndexOf(text, ch, text.length() - 1);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        if (fromIndex > text.length() - 1) {
            fromIndex = text.length() - 1;
        }
	while (fromIndex >= 0) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex--;
	}
	return -1;
    }

    public static boolean contentEquals(CharSequence str1, CharSequence str2) {
        return CharSequences.comparator().compare(str1, str2) == 0;
    }
    
    /**
     * Implementation of {@link String#startsWith(String)} for character sequences.
     */
    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            if (text.charAt(x) != prefix.charAt(x))
                return false;
        }
        return true;
    }

    /** Same as startsWith, but ignores case */
    public static boolean startsWithIgnoreCase(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            final char c1 = text.charAt(x);
            final char c2 = prefix.charAt(x);
            if (c1 != c2) {
                if (Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Implementation of {@link String#endsWith(String)} for character sequences.
     */
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        int s_length = suffix.length();
        int text_length = text.length();
        if (s_length > text_length) {
            return false;
        }
        for (int x = 0; x < s_length; x++) {
            if (text.charAt(text_length - s_length + x) != suffix.charAt(x))
                return false;
        }
        return true;
    }

    public static String toString(CharSequence prefix, char separator, CharSequence postfix) {
        int prefLength = prefix.length();
        int postLength = postfix.length();
        char[] chars = new char[prefLength + 1 + postLength];
        int indx = 0;
        if (prefix instanceof String) {
            ((String)prefix).getChars(0, prefLength, chars, indx);
            indx = prefLength;
        } else {
            for (int i = 0; i < prefLength; i++) {
                chars[indx++] = prefix.charAt(i);
            }
        }
        chars[indx++] = separator;
        if (postfix instanceof String) {
            ((String)postfix).getChars(0, postLength, chars, indx);
        } else {
            for (int i = 0; i < postLength; i++) {
                chars[indx++] = postfix.charAt(i);
            }
        }
        return new String(chars);
    }
    
    public static boolean isNullOrEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static CharSequence concatenate(final char s1, final CharSequence s2) {
        return new CharSequence() {
            private final int l1 = 1;
            private final int l2 = l1+s2.length();
            @Override
            public int length() {
                return l2;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1;
                }
                return s2.charAt(index-l1);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).toString();
            }
            
        };
    }

    public static CharSequence concatenate(final CharSequence s1, final CharSequence s2) {
        return new CharSequence() {
            private final int l1 = s1.length();
            private final int l2 = l1+s2.length();
            @Override
            public int length() {
                return l2;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1.charAt(index);
                }
                return s2.charAt(index-l1);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).toString();
            }
            
        };
    }

    public static CharSequence concatenate(final char s1, final CharSequence s2, final CharSequence s3) {
        return new CharSequence() {
            private final int l1 = 1;
            private final int l2 = l1+s2.length();
            private final int l3 = l2+s3.length();

            @Override
            public int length() {
                return l3;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1;
                } else if (index < l2) {
                    return s2.charAt(index-l1);
                }
                return s3.charAt(index-l2);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).toString();
            }
        };
    }
    
    public static CharSequence concatenate(final char s1, final char s2, final CharSequence s3) {
        return new CharSequence() {
            private final int l1 = 1;
            private final int l2 = l1+1;
            private final int l3 = l2+s3.length();

            @Override
            public int length() {
                return l3;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1;
                } else if (index < l2) {
                    return s2;
                }
                return s3.charAt(index-l2);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).toString();
            }
        };
    }

    public static CharSequence concatenate(final CharSequence s1, final CharSequence s2, final CharSequence s3) {
        return new CharSequence() {
            private final int l1 = s1.length();
            private final int l2 = l1+s2.length();
            private final int l3 = l2+s3.length();

            @Override
            public int length() {
                return l3;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1.charAt(index);
                } else if (index < l2) {
                    return s2.charAt(index-l1);
                }
                return s3.charAt(index-l2);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).toString();
            }
        };
    }

    public static CharSequence concatenate(final CharSequence s1, final CharSequence s2, final CharSequence s3, final CharSequence s4) {
        return new CharSequence() {
            private final int l1 = s1.length();
            private final int l2 = l1+s2.length();
            private final int l3 = l2+s3.length();
            private final int l4 = l3+s4.length();

            @Override
            public int length() {
                return l4;
            }

            @Override
            public char charAt(int index) {
                if (index < l1) {
                    return s1.charAt(index);
                } else if (index < l2) {
                    return s2.charAt(index-l1);
                } else if (index < l3) {
                    return s3.charAt(index-l2);
                }
                return s4.charAt(index-l3);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).append(s4).subSequence(start, end);
            }

            @Override
            public String toString() {
                return new StringBuilder(length()).append(s1).append(s2).append(s3).append(s4).toString();
            }
        };
    }
    
    private static class CharSequenceComparatorIgnoreCase implements Comparator<CharSequence> {
        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            int n1 = o1.length();
            int n2 = o2.length();
            for (int i1 = 0,  i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
                char c1 = o1.charAt(i1);
                char c2 = o2.charAt(i2);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }
    }
}
