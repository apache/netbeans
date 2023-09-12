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

package org.netbeans.lib.ddl.util;

import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import java.text.MessageFormat;
import org.openide.util.NbBundle;

/** Reader for "plist" format. This format uses {} brackets to enclose dictionary
* data (Map) and () braces for array data (Collection). Returns Map with data.
*
* @author Slavek Psenicka
*/
public class PListReader {

    protected StreamTokenizer tokenizer = null;

    public static Map read(String file)
    throws FileNotFoundException, ParseException, IOException
    {
        PListReader reader = new PListReader(file);
        return reader.getData();
    }

    /** Constructor
    * Initializes reader with contents of file
    * @param file File to read
    */
    public PListReader(String file)
    throws FileNotFoundException, ParseException, IOException
    {
        BufferedReader buffreader = new BufferedReader(new FileReader(file));
        tokenizer = createTokenizer(buffreader);
    }

    /** Constructor
    * Initializes reader with contents of file
    * @param file File to read
    */
    public PListReader(File file)
    throws FileNotFoundException, ParseException, IOException
    {
        BufferedReader buffreader = new BufferedReader(new FileReader(file));
        tokenizer = createTokenizer(buffreader);
    }

    /** Constructor
    * Initializes reader with stream
    * @param stream Stream to read
    */
    public PListReader(InputStream stream)
    throws FileNotFoundException, ParseException, IOException
    {
        BufferedReader buffreader = new BufferedReader(new InputStreamReader(stream));
        tokenizer = createTokenizer(buffreader);
    }

    /** Prepares tokenizer for this format.
    * Enables both comment styles.
    * @param buffreader Reader
    * @return Newly created tokenizer
    */
    private StreamTokenizer createTokenizer(BufferedReader buffreader)
    {
        StreamTokenizer tok = new StreamTokenizer(buffreader);
        tok.slashStarComments(true);
        tok.slashSlashComments(true);
        tok.wordChars(95, 95);
        tok.wordChars(126, 126);
        tok.wordChars(33, 33);
        tok.wordChars(96, 96);
        tok.wordChars(39, 39);
        tok.wordChars(35, 35);
        tok.wordChars(36, 36);
        tok.wordChars(37, 37);
        tok.wordChars(94, 94);
        tok.wordChars(38, 38);
        tok.wordChars(42, 42);
        tok.wordChars(45, 45);
        tok.wordChars(43, 43);
        tok.wordChars(124, 124);
        tok.wordChars(63, 63);
        tok.wordChars(46, 46);
        tok.quoteChar(34);
        return tok;
    }

    /** Reads data from tokenizer.
    * Parses given data tokenizer and produces data.
    * @param tokenizer Used tokenizer
    * @return Parsed data structure.
    */
    private Object read(StreamTokenizer tokenizer)
    throws FileNotFoundException, ParseException, IOException
    {
        DictionaryNode node = new DictionaryNode(tokenizer);
        return node.getBindings();
    }

    /** Reads data from tokenizer.
    * Parses given data tokenizer and produces data.
    * @param tokenizer Used tokenizer
    * @return Parsed data structure.
    */
    public HashMap getData()
    throws FileNotFoundException, ParseException, IOException
    {
        return (HashMap)read(tokenizer);
    }

    /** Returns prepared tokenizer.
    * @return Prepared tokenizer.
    */
    public StreamTokenizer getTokenizer()
    {
        return tokenizer;
    }

    /** Inner superclass for nodes */
    abstract class Node
    {
        /** Parses expected character from stream.
        * Throws ParseException if expected character was not found.
        * @param tokenizer Used tokenizer
        * @param charcode Expected character code
        */
        public void parseChar(StreamTokenizer tokenizer, int charcode)
        throws IOException, ParseException
        {
            tokenizer.nextToken();
            if (tokenizer.ttype != charcode) {
                char[] charr = new char[1];
                charr[0] = (char)charcode;
                //throw new ParseException("expected '"+new String(charr)+"', found: "+tokenizer.toString(), tokenizer.lineno());
                throw new ParseException(
                    MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_Expected"), // NOI18N
                        new String[] {new String(charr), tokenizer.toString()}),
                    tokenizer.lineno());
            }
        }

        /** Parses expected character from stream.
        * Throws ParseException if expected character was not found.
        * @param tokenizer Used tokenizer
        * @param charcode Expected character code
        */
        public Object parseNumber(StreamTokenizer tokenizer)
        throws IOException
        {
            String s5 = Double.toString(tokenizer.nval);
            tokenizer.nextToken();
            if(tokenizer.ttype == -2) {
                while(tokenizer.ttype == -2) {
                    s5 = s5 + Double.toString(tokenizer.nval);
                    tokenizer.nextToken();
                }

                tokenizer.pushBack();
                return s5;
            }

            tokenizer.pushBack();
            double d = Math.rint(tokenizer.nval);
            if(d == tokenizer.nval) return Integer.valueOf((int)d);
            return Double.valueOf(tokenizer.nval);
        }
    }


    /**
    * Inner class for dictionary node.
    * @author Slavek Psenicka
    */
    class DictionaryNode extends Node
    {
        /** Read values */
        HashMap bindings;

        /* Constructor */
        public DictionaryNode(StreamTokenizer tokenizer)
        throws ParseException, IOException
        {
            bindings = new HashMap();
            parse(tokenizer);
        }

        /* Method for reading data from tokenizer
        * Prepares structure into bindings map.
        * @param tokenizer Used tokenizer
        */
        public void parse(StreamTokenizer tokenizer)
        throws ParseException, IOException
        {
            String key = null;
            Object object = null;

            try {

                // left bracket
                parseChar(tokenizer,123);

                while (true) {

                    // key or right bracket
                    tokenizer.nextToken();
                    switch(tokenizer.ttype) {
                    case StreamTokenizer.TT_WORD:
                        key = tokenizer.sval; break;
                    case 34:
                        key = tokenizer.sval; break;
                    case 125:
                        throw new EOFException();
                    default:
                        //throw new ParseException("unexpected key, found: "+tokenizer.toString(), tokenizer.lineno());
                        throw new ParseException( MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnexpectedKey"), // NOI18N
                                                    new String[] { tokenizer.toString() } ),
                                                tokenizer.lineno());
                    }

                    // =
                    parseChar(tokenizer,61);

                    // object
                    tokenizer.nextToken();
                    switch(tokenizer.ttype) {
                    case StreamTokenizer.TT_WORD:
                        object = tokenizer.sval; break;
                    case 34:
                        object = tokenizer.sval; break;
                    case 123:
                        tokenizer.pushBack();
                        object = (Object)new DictionaryNode(tokenizer).getBindings();
                        break;
                    case 40:
                        tokenizer.pushBack();
                        object = (Object)new ArrayNode(tokenizer).getBindings();
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        object = parseNumber(tokenizer);
                        break;
                    default:
                        throw new ParseException( MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ExpectedObject"), // NOI18N
                                                                            new String[] { tokenizer.toString() } ),
                                                                        tokenizer.lineno());
                    }

                    // ;
                    parseChar(tokenizer,59);
                    bindings.put(key,object);
                }

            } catch (EOFException e) {
            }
        }

        /** Returns value for specified key */
        public Object get(String s)
        {
            return bindings.get(s);
        }

        /** Returns all keys */
        public Set getKeys()
        {
            return bindings.keySet();
        }

        /** Returns whole binding map */
        public HashMap getBindings()
        {
            return bindings;
        }
    }


    /**
    * Inner class for array node.
    * @author Slavek Psenicka
    */
    class ArrayNode extends Node
    {
        /* Data */
        Vector bindings;

        /** Constructor */
        public ArrayNode(StreamTokenizer tokenizer)
        throws ParseException, IOException
        {
            bindings = new Vector();
            parse(tokenizer);
        }

        /* Method for reading data from tokenizer
        * Prepares structure into bindings map.
        * @param tokenizer Used tokenizer
        */
        public void parse(StreamTokenizer tokenizer)
        throws ParseException, IOException
        {
            Object object = null;

            try {

                // left bracket
                parseChar(tokenizer,40);

                while (true) {

                    // object
                    tokenizer.nextToken();
                    switch(tokenizer.ttype) {
                    case StreamTokenizer.TT_WORD:
                        object = tokenizer.sval; break;
                    case 34:
                        object = tokenizer.sval; break;
                    case 41:
                        throw new EOFException();
                    case 123:
                        tokenizer.pushBack();
                        object = (Object)new DictionaryNode(tokenizer).getBindings();
                        break;
                    case 40:
                        tokenizer.pushBack();
                        object = new ArrayNode(tokenizer).getBindings();
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        object = parseNumber(tokenizer);
                        break;
                    default:
                        throw new ParseException( MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_ExpectedObject"), // NOI18N
                                                    new String[] { tokenizer.toString() } ),
                                                tokenizer.lineno());
                    }

                    bindings.add(object);

                    // ,
                    tokenizer.nextToken();
                    switch(tokenizer.ttype) {
                    case 41:
                        throw new EOFException();
                    case 44:
                        break;
                    default:
                        throw new ParseException(
                            MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_Expected"), // NOI18N
                                new String[] {"','", tokenizer.toString()}), // NOI18N
                            tokenizer.lineno());
                            }
                }

            } catch (EOFException e) {
            }
        }

        /** Returns while binding array */
        public Vector getBindings()
        {
            return bindings;
        }
    }
}
