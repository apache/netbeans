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
package org.openide.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/** A text format similar to <code>MessageFormat</code>
 * but using string rather than numeric keys.
 * You might use use this formatter like this:
 * <pre>MapFormat.format("Hello {name}", map);</pre>
 * Or to have more control over it:
 * <pre>
 * Map m = new HashMap ();
 * m.put ("KEY", "value");
 * MapFormat f = new MapFormat (m);
 * f.setLeftBrace ("__");
 * f.setRightBrace ("__");
 * String result = f.format ("the __KEY__ here");
 * </pre>
 *
 * @author Slavek Psenicka
 * @see MessageFormat
 */
public class MapFormat extends Format {
    private static final int BUFSIZE = 255;

    /** Array with to-be-skipped blocks */

    //private RangeList skipped;
    static final long serialVersionUID = -7695811542873819435L;

    /** Locale region settings used for number and date formatting */
    private Locale locale = Locale.getDefault();

    /** Left delimiter */
    private String ldel = "{"; // NOI18N

    /** Right delimiter */
    private String rdel = "}"; // NOI18N

    /** Used formatting map */
    private Map<String, ?> argmap;

    /** Offsets to {} expressions */
    private int[] offsets;

    /** Keys enclosed by {} brackets */
    private String[] arguments;

    /** Max used offset */
    private int maxOffset;

    /** Should be thrown an exception if key was not found? */
    private boolean throwex = false;

    /** Exactly match brackets? */
    private boolean exactmatch = true;

    /**
    * Constructor.
    * For common work use  <code>format(pattern, arguments) </code>.
    * @param arguments keys and values to use in the format
    */
    public MapFormat(Map<String, ?> arguments) {
        super();
        setMap(arguments);
    }

    /**
    * Designated method. It gets the string, initializes HashFormat object
    * and returns converted string. It scans  <code>pattern</code>
    * for {} brackets, then parses enclosed string and replaces it
    * with argument's  <code>get()</code> value.
    * @param pattern String to be parsed.
    * @param arguments Map with key-value pairs to replace.
    * @return Formatted string
    */
    public static String format(String pattern, Map arguments) {
        MapFormat temp = new MapFormat(arguments);

        return temp.format(pattern);
    }

    // unused so removed --jglick

    /**
    * Search for comments and quotation marks.
    * Prepares internal structures.
    * @param pattern String to be parsed.
    * @param lmark Left mark of to-be-skipped block.
    * @param rmark Right mark of to-be-skipped block or null if does not exist (// comment).
    private void process(String pattern, String lmark, String rmark)
    {
        int idx = 0;
        while (true) {
            int ridx = -1, lidx = pattern.indexOf(lmark,idx);
            if (lidx >= 0) {
                if (rmark != null) {
                    ridx = pattern.indexOf(rmark,lidx + lmark.length());
                } else ridx = pattern.length();
            } else break;
            if (ridx >= 0) {
                skipped.put(new Range(lidx, ridx-lidx));
                if (rmark != null) idx = ridx+rmark.length();
                else break;
            } else break;
        }
    }
    */
    /** Returns the value for given key. Subclass may define its own beahvior of
    * this method. For example, if key is not defined, subclass can return &lt;not defined&gt;
    * string.
    *
    * @param key Key.
    * @return Value for this key.
    */
    protected Object processKey(String key) {
        return argmap.get(key);
    }

    /**
    * Scans the pattern and prepares internal variables.
    * @param newPattern String to be parsed.
    * @exception IllegalArgumentException if number of arguments exceeds BUFSIZE or
    * parser found unmatched brackets (this exception should be switched off
    * using setExactMatch(false)).
    * @return parsed string
    */
    public String processPattern(String newPattern) throws IllegalArgumentException {
        int idx = 0;
        int offnum = -1;
        StringBuffer outpat = new StringBuffer();
        offsets = new int[BUFSIZE];
        arguments = new String[BUFSIZE];
        maxOffset = -1;

        //skipped = new RangeList();
        // What was this for??
        //process(newPattern, "\"", "\""); // NOI18N
        while (true) {
            int ridx = -1;
            int lidx = newPattern.indexOf(ldel, idx);

            /*
            Range ran = skipped.getRangeContainingOffset(lidx);
            if (ran != null) {
                outpat.append(newPattern.substring(idx, ran.getEnd()));
                idx = ran.getEnd(); continue;
            }
             */
            if (lidx >= 0) {
                ridx = newPattern.indexOf(rdel, lidx + ldel.length());
            } else {
                break;
            }

            if (++offnum >= BUFSIZE) {
                throw new IllegalArgumentException(
                    NbBundle.getBundle(MapFormat.class).getString("MSG_TooManyArguments")
                );
            }

            if (ridx < 0) {
                if (exactmatch) {
                    throw new IllegalArgumentException(
                        NbBundle.getBundle(MapFormat.class).getString("MSG_UnmatchedBraces") + " " + lidx
                    );
                } else {
                    break;
                }
            }

            outpat.append(newPattern.substring(idx, lidx));
            offsets[offnum] = outpat.length();
            arguments[offnum] = newPattern.substring(lidx + ldel.length(), ridx);
            idx = ridx + rdel.length();
            maxOffset++;
        }

        outpat.append(newPattern.substring(idx));

        return outpat.toString();
    }

    /**
    * Formats object.
    * @param obj Object to be formatted into string
    * @return Formatted object
    */
    private String formatObject(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Number) {
            return NumberFormat.getInstance(locale).format(obj); // fix
        } else if (obj instanceof Date) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(obj); //fix
        } else if (obj instanceof String) {
            return (String) obj;
        }

        return obj.toString();
    }

    /**
    * Formats the parsed string by inserting table's values.
    * @param pat a string pattern
    * @param result Buffer to be used for result.
     * @param fpos position
    * @return Formatted string
    */
    public StringBuffer format(Object pat, StringBuffer result, FieldPosition fpos) {
        String pattern = processPattern((String) pat);
        int lastOffset = 0;

        for (int i = 0; i <= maxOffset; ++i) {
            int offidx = offsets[i];
            result.append(pattern.substring(lastOffset, offsets[i]));
            lastOffset = offidx;

            String key = arguments[i];
            String obj;
            if (key.length() > 0) {
                obj = formatObject(processKey(key));
            } else {
                // else just copy the left and right braces
                result.append(this.ldel);
                result.append(this.rdel);
                continue;
            }

            if (obj == null) {
                // try less-greedy match; useful for e.g. PROP___PROPNAME__ where
                // 'PROPNAME' is a key and delims are both '__'
                // this does not solve all possible cases, surely, but it should catch
                // the most common ones
                String lessgreedy = ldel + key;
                int fromright = lessgreedy.lastIndexOf(ldel);

                if (fromright > 0) {
                    String newkey = lessgreedy.substring(fromright + ldel.length());
                    String newsubst = formatObject(processKey(newkey));

                    if (newsubst != null) {
                        obj = lessgreedy.substring(0, fromright) + newsubst;
                    }
                }
            }

            if (obj == null) {
                if (throwex) {
                    throw new IllegalArgumentException(
                        MessageFormat.format(
                            NbBundle.getBundle(MapFormat.class).getString("MSG_FMT_ObjectForKey"),
                            new Object[] { new Integer(key) }
                        )
                    );
                } else {
                    obj = ldel + key + rdel;
                }
            }

            result.append(obj);
        }

        result.append(pattern.substring(lastOffset));

        return result;
    }

    /**
    * Parses the string. Does not yet handle recursion (where
    * the substituted strings contain %n references.)
    */
    public Object parseObject(String text, ParsePosition status) {
        return parse(text);
    }

    /**
    * Parses the string. Does not yet handle recursion (where
    * the substituted strings contain {n} references.)
    * @param source string to parse
    * @return New format.
    */
    public String parse(String source) {
        StringBuffer sbuf = new StringBuffer(source);
        Iterator<String> key_it = argmap.keySet().iterator();

        //skipped = new RangeList();
        // What was this for??
        //process(source, "\"", "\""); // NOI18N
        while (key_it.hasNext()) {
            String it_key = key_it.next();
            String it_obj = formatObject(argmap.get(it_key));
            int it_idx = -1;

            do {
                it_idx = sbuf.toString().indexOf(it_obj, ++it_idx);

                if (it_idx >= 0 /* && !skipped.containsOffset(it_idx) */    ) {
                    sbuf.replace(it_idx, it_idx + it_obj.length(), ldel + it_key + rdel);

                    //skipped = new RangeList();
                    // What was this for??
                    //process(sbuf.toString(), "\"", "\""); // NOI18N
                }
            } while (it_idx != -1);
        }

        return sbuf.toString();
    }

    /** Test whether formatter will throw exception if object for key was not found.
    * If given map does not contain object for key specified, it could
    * throw an exception. Returns true if throws. If not, key is left unchanged.
    * @return true if throws.
    */
    public boolean willThrowExceptionIfKeyWasNotFound() {
        return throwex;
    }

    /** Specify whether formatter will throw exception if object for key was not found.
    * If given map does not contain object for key specified, it could
    * throw an exception. If does not throw, key is left unchanged.
    * @param flag If true, formatter throws IllegalArgumentException.
    */
    public void setThrowExceptionIfKeyWasNotFound(boolean flag) {
        throwex = flag;
    }

    /** Test whether both brackets are required in the expression.
    * If not, use setExactMatch(false) and formatter will ignore missing right
    * bracket. Advanced feature.
    * @return true if both brackets are required
    */
    public boolean isExactMatch() {
        return exactmatch;
    }

    /** Specify whether both brackets are required in the expression.
    * If not, use setExactMatch(false) and formatter will ignore missing right
    * bracket. Advanced feature.
    * @param flag If true, formatter will ignore missing right bracket (default = false)
    */
    public void setExactMatch(boolean flag) {
        exactmatch = flag;
    }

    /** Returns string used as left brace.
     * @return string used as left brace
     */
    public String getLeftBrace() {
        return ldel;
    }

    /** Sets string used as left brace
    * @param delimiter Left brace.
    */
    public void setLeftBrace(String delimiter) {
        ldel = delimiter;
    }

    /** Returns string used as right brace.
     *  @return string used as right brace
     */
    public String getRightBrace() {
        return rdel;
    }

    /** Sets string used as right brace
    * @param delimiter Right brace.
    */
    public void setRightBrace(String delimiter) {
        rdel = delimiter;
    }

    /** Returns argument map.
     * @return argument map
     */
    public Map getMap() {
        return argmap;
    }

    /** Sets argument map
    * This map should contain key-value pairs with key values used in
    * formatted string expression. If value for key was not found, formatter leave
    * key unchanged (except if you've set setThrowExceptionIfKeyWasNotFound(true),
    * then it fires IllegalArgumentException.
    *
    * @param map the argument map
    */
    public void setMap(Map<String, ?> map) {
        argmap = map;
    }

    // commented out because unused --jglick

    /**
    * Range of expression in string.
    * Used internally to store information about quotation marks and comments
    * in formatted string.
    *
    * @author   Slavek Psenicka
    * @version  1.0, March 11. 1999
    *
    class Range extends Object
    {
        /** Offset of expression *
        private int offset;

        /** Length of expression *
        private int length;

        /** Constructor *
        public Range(int off, int len)
        {
            offset = off;
            length = len;
        }

        /** Returns offset *
        public int getOffset()
        {
            return offset;
        }

        /** Returns length of expression *
        public int getLength()
        {
            return length;
        }

        /** Returns final position of expression *
        public int getEnd()
        {
            return offset+length;
        }

        public String toString()
        {
            return "("+offset+", "+length+")"; // NOI18N
        }
    }

    /**
    * List of ranges.
    * Used internally to store information about quotation marks and comments
    * in formatted string.
    *
    * @author   Slavek Psenicka
    * @version  1.0, March 11. 1999
    *
    class RangeList
    {
        /** Map with Ranges *
        private HashMap hmap;

        /** Constructor *
        public RangeList()
        {
            hmap = new HashMap();
        }

        /** Returns true if offset is enclosed by any Range object in list *
        public boolean containsOffset(int offset)
        {
            return (getRangeContainingOffset(offset) != null);
        }

        /** Returns enclosing Range object in list for given offset *
        public Range getRangeContainingOffset(int offset)
        {
            if (hmap.size() == 0) return null;
            int offit = offset;
            while (offit-- >= 0) {
                Integer off = new Integer(offit);
                if (hmap.containsKey(off)) {
                    Range ran = (Range)hmap.get(off);
                    if (ran.getEnd() - offset > 0) return ran;
                }
            }

            return null;
        }

        /** Puts new range into list *
        public void put(Range range)
        {
            hmap.put(new Integer(range.getOffset()), range);
        }

        public String toString()
        {
            return hmap.toString();
        }
    }
     */
}
