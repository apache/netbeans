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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import static org.openide.util.NbBundle.getBundle;

/**
 * A text format similar to <code>MessageFormat</code> but using string rather
 * than numeric keys. You might use use this formatter like this:
 * <pre>MapFormat.format("Hello {name}", map);</pre> Or to have more control
 * over it:
 * <pre>
 * Map m = new HashMap ();
 * m.put ("KEY", "value");
 * MapFormat f = new MapFormat (m);
 * f.setLeftBrace ("__");
 * f.setRightBrace ("__");
 * String result = f.format ("the __KEY__ here");
 * </pre>
 *
 * @author Slavek Psenicka, Lukasz Bownik
 * @see MessageFormat
 */
public class MapFormat extends Format {

    static final long serialVersionUID = -7695811542873819436L;

    private String leftDelimiter = "{";
    private String rightDelimiter = "}";
    private Map<String, Object> argumentsMap;
    private boolean throwExceptionWhenKeyValueNotFound = false;
    private boolean delimitersMustMatchExactly = true;

    /**
     * Constructor. For common work use  <code>format(pattern, arguments)
     * </code>.
     *
     * @param arguments keys and values to use in the format
     */
    public MapFormat(final Map<String, Object> arguments) {

        this.argumentsMap = arguments;
    }

    /**
     * Designated method. It gets the string, initializes HashFormat object and
     * returns converted string. It scans  <code>pattern</code> for {} brackets,
     * then parses enclosed string and replaces it with argument's
     * <code>get()</code> value.
     *
     * @param pattern String to be parsed.
     * @param arguments Map with key-value pairs to replace.
     * @return Formatted string
     */
    public static String format(final String pattern, final Map<String, Object> arguments) {

        return new MapFormat(arguments).format(pattern);
    }

    /**
     * Returns the value for given key. Subclass may define its own beahvior of
     * this method. For example, if key is not defined, subclass can return
     * <not defined>
     * string.
     *
     * @param key Key.
     * @return Value for this key.
     */
    protected Object processKey(final String key) {

        return this.argumentsMap.get(key);
    }

    /**
     * Formats the parsed string by inserting table's values.
     *
     * @param pat a string pattern
     * @param result Buffer to be used for result.
     * @param fpos position
     * @return Formatted string
     */
    @Override
    public StringBuffer format(final Object pat, final StringBuffer result,
            final FieldPosition fpos) {

        final String pattern = pat.toString();
        int index = 0;

        while (true) {
            final int leftDelimiterIndex = pattern.indexOf(this.leftDelimiter, index);
            if (found(leftDelimiterIndex)) {
                final int indexAfterLeftDelimiter = leftDelimiterIndex
                        + this.leftDelimiter.length();

                final int rightDelimiterIndex = pattern.indexOf(this.rightDelimiter,
                        indexAfterLeftDelimiter);
                if (found(rightDelimiterIndex)) {
                    result.append(pattern, index, leftDelimiterIndex);

                    final String key = pattern.substring(indexAfterLeftDelimiter, rightDelimiterIndex);

                    if (key.isEmpty()) {
                        result.append(this.leftDelimiter).append(this.rightDelimiter);
                    } else {
                        final CharSequence value = formatObject(processKey(key));
                        if (value != null) {
                            result.append(value);
                        } else {
                            final String potentiallyRepeated = this.leftDelimiter + key;
                            int leftDelimiterIndexFromRight
                                    = potentiallyRepeated.lastIndexOf(this.leftDelimiter);

                            final String newKey
                                    = potentiallyRepeated.substring(leftDelimiterIndexFromRight
                                            + this.leftDelimiter.length());
                            final String newValue = formatObject(processKey(newKey));

                            if (newValue != null) {
                                result.append(potentiallyRepeated, 0,
                                        leftDelimiterIndexFromRight).append(newValue);
                            } else {
                                if (this.throwExceptionWhenKeyValueNotFound) {
                                    throwArgumentNotFound(key);
                                } else {
                                    result.append(this.leftDelimiter).append(key).
                                            append(this.rightDelimiter);
                                }
                            }
                        }
                    }
                } else {
                    if (this.delimitersMustMatchExactly) {
                        throwUnmatchedBraces(index);
                    } else {
                        return result.append(pattern, index, pattern.length());
                    }
                }
                index = rightDelimiterIndex + this.rightDelimiter.length();
            } else {
                return result.append(pattern, index, pattern.length());
            }
        }
    }

    /**
     *
     */
    private static void throwArgumentNotFound(final String key) {

        throw new IllegalArgumentException(
                MessageFormat.format(getFromBundle("MSG_FMT_ObjectForKey"),
                        new Object[]{key}));
    }

    /**
     *
     */
    private static void throwUnmatchedBraces(final int index) {

        throw new IllegalArgumentException(
                getFromBundle("MSG_UnmatchedBraces") + " " + index);
    }

    /**
     *
     */
    private static boolean found(final int index) {

        return index > -1;
    }

    /**
     * Formats object.
     *
     * @param obj Object to be formatted into string
     * @return Formatted object
     */
    private String formatObject(final Object obj) {

        if (obj == null) {
            return null;
        } else if (obj instanceof Number) {
            return NumberFormat.getInstance(Locale.getDefault()).format(obj);
        } else if (obj instanceof Date) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, 
                    DateFormat.SHORT, Locale.getDefault()).format(obj);
        } else {
            return obj.toString();
        }
    }

    /**
     * Parses the string.Does not yet handle recursion (where the substituted
     * strings contain %n references.)
     *
     * @param text
     * @param status
     */
    @Override
    public Object parseObject(final String text, final ParsePosition status) {

        return parse(text);
    }

    /**
     * Parses the string.Does not yet handle recursion (where the substituted
     * strings contain {n} references.)
     *
     * @param source
     * @return New format.
     */
    public String parse(final String source) {

        final StringBuilder buffer = new StringBuilder(source);

        this.argumentsMap.forEach((key, value) -> {
            final String obj = formatObject(value);
            int index = -1;

            do {
                index = buffer.indexOf(obj, ++index);

                if (index > -1) {
                    buffer.replace(index, index + obj.length(),
                            leftDelimiter + key + rightDelimiter);
                }
            } while (index != -1);
        });

        return buffer.toString();
    }

    /**
     * Test whether formatter will throw exception if object for key was not
     * found. If given map does not contain object for key specified, it could
     * throw an exception. Returns true if throws. If not, key is left
     * unchanged.
     */
    public boolean willThrowExceptionIfKeyWasNotFound() {

        return this.throwExceptionWhenKeyValueNotFound;
    }

    /**
     * Specify whether formatter will throw exception if object for key was not
     * found. If given map does not contain object for key specified, it could
     * throw an exception. If does not throw, key is left unchanged.
     *
     * @param value If true, formatter throws IllegalArgumentException.
     */
    public void setThrowExceptionIfKeyWasNotFound(final boolean value) {

        this.throwExceptionWhenKeyValueNotFound = value;
    }

    /**
     * Test whether both brackets are required in the expression. If not, use
     * setExactMatch(false) and formatter will ignore missing right bracket.
     * Advanced feature.
     */
    public boolean isExactMatch() {

        return this.delimitersMustMatchExactly;
    }

    /**
     * Specify whether both brackets are required in the expression. If not, use
     * setExactMatch(false) and formatter will ignore missing right bracket.
     * Advanced feature.
     *
     * @param value If false, formatter will ignore missing right bracket
     * (default = true)
     */
    public void setExactMatch(final boolean value) {

        this.delimitersMustMatchExactly = value;
    }

    /**
     * Returns string used as left brace
     */
    public String getLeftBrace() {

        return this.leftDelimiter;
    }

    /**
     * Sets string used as left brace
     *
     * @param delimiter Left brace.
     */
    public void setLeftBrace(final String delimiter) {

        this.leftDelimiter = delimiter;
    }

    /**
     * Returns string used as right brace
     */
    public String getRightBrace() {

        return this.rightDelimiter;
    }

    /**
     * Sets string used as right brace
     *
     * @param delimiter Right brace.
     */
    public void setRightBrace(final String delimiter) {

        this.rightDelimiter = delimiter;
    }

    /**
     * Returns argument map
     *
     * @return
     */
    public Map<String, Object> getMap() {

        return this.argumentsMap;
    }

    /**
     * Sets argument map This map should contain key-value pairs with key values
     * used in formatted string expression. If value for key was not found,
     * formatter leave key unchanged (except if you've set
     * setThrowExceptionIfKeyWasNotFound(true), then it fires
     * IllegalArgumentException.
     *
     * @param map the argument map
     */
    public void setMap(final Map<String, Object> map) {

        this.argumentsMap = map;
    }

    /**
     * 
     */
    private static String getFromBundle(final String key) {

        return getBundle(MapFormat.class).getString(key);
    }
}
