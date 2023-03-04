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

package org.netbeans.modules.schema2beansdev;

import java.io.*;

import org.netbeans.modules.schema2beans.*;

/**
 *
 *	This class implement the Document Definition handler in order to build
 *	the internal tree representation of the DD DTD.
 *
 */
public class DocDefParser extends GeneralParser implements SchemaParser {

    static class MissingEndOfEltException extends RuntimeException {
        String propName;

        public MissingEndOfEltException(String propName) {
            this.propName = propName;
        }
    }
    
    private static final int WORD_NO_CONTEXT	= 0;
    private static final int WORD_CHECK		= 1;
    private static final int WORD_COMMENT	= 2;
    private static final int WORD_ELEMENT1	= 3;
    private static final int WORD_ELEMENT	= 4;
    private static final int WORD_ATTLIST1	= 5;
    private static final int WORD_ATTLIST	= 6;
    private static final int WORD_PI		= 7;
    private static final int WORD_ENTITY1	= 10;
    private static final int WORD_ENTITY	= 11;
    
    static String errHeader = "DTD parsing failed: ";	// NOI18N
    
    //	Buffer used to read the file by chunks
    private char 		buffer[] = new char[BUFFER_SIZE];
    
    //	Current size of the buffer
    private int			bufSize;
    
    //	Reading offset in the buffer while parsing
    private int			bufScan;
    
    protected static int BUFFER_SIZE	=	4096;
    
    //	Handler to callback with the tokens found in the DTD.
    private DocDefHandler	handler;
    
    private GenBeans.Config config = null;
    
    public DocDefParser() {
    }
    
    public DocDefParser(GenBeans.Config config, DocDefHandler handler) {
        this.config = config;
        this.filename = config.getFilename();
        this.schemaIn = config.getFileIn();
        this.handler = handler;
    }
    
    protected @Override void startupReader() throws java.io.IOException {
        if (schemaIn == null) {
            schemaIn = new FileInputStream(filename);
        }
        EntityParser entityParser = new EntityParser();
        try (Reader r = new InputStreamReader(schemaIn)) {
            entityParser.parse(r);
        }
        reader = entityParser.getReader();
    }
    
    public void setHandler(DocDefHandler handler) {
        this.handler = handler;
    }
    
    public DocDefHandler getHandler() {
        return this.handler;
    }
    
    protected boolean checkBuffer() throws IOException {
        if (this.bufScan >= this.bufSize) {
            //	Buffer either empty or already parsed - get more from the file
            this.bufSize = reader.read(this.buffer);
            if (this.bufSize == -1)
                return false;
            this.bufScan = 0;
        }
        return true;
    }
    
    /**
     *	Returns the next character of the parsed file.
     */
    protected char getNext() throws IOException {
        if (this.checkBuffer())
            return this.buffer[this.bufScan++];
        else
            return '\0';
    }
    
    /**
     *	Get the next character without moving the parser offset.
     */
    protected char peekNext() throws IOException {
        if (this.checkBuffer())
            return this.buffer[this.bufScan];
        else
            return '\0';
    }
    
    /**
     *	Return the instance value associated with an element
     */
    private static int getInstanceValue(char c) {
        switch(c) {
            case '*':
                return Common.TYPE_0_N;
            case '+':
                return Common.TYPE_1_N;
            case '?':
                return Common.TYPE_0_1;
            default:
                //  We assume this default value if nothing is specified
                return Common.TYPE_1;
        }
    }
    
    /**
     *  Find out the type of the current word
     */
    private int processWord(StringBuffer curWord, int wordContext) throws SchemaParseException{
        String 	word = curWord.toString();
        int	len = word.length();
        
        if (len >0) {
            //	We have some word to play with
            switch (wordContext) {
                case WORD_CHECK:
                    if (word.startsWith("--")) {	// NOI18N
                        if (len > 2)
                            word = curWord.substring(2);
                        else
                            word = "";	// NOI18N
                        
                        this.handler.startElement(word, word, Common.COMMENT);
                        wordContext = WORD_COMMENT;
                    } else if (word.equals("ELEMENT"))	// NOI18N
                        wordContext = WORD_ELEMENT1;
                    else if (word.equals("ATTLIST"))	// NOI18N
                        wordContext = WORD_ATTLIST1;
                    else if (word.equals("ENTITY"))	// NOI18N
                        wordContext = WORD_ENTITY1;
                    else {
                        //System.err.println("Error: found an unknown '<!' sequence (" + word + ")");	// NOI18N
                        throw new SchemaParseException("Error: found an unknown '<!' sequence (" + word + ")");	// NOI18N
                    }
                    break;
                case WORD_COMMENT:
                    this.handler.element(word, word, 0);
                    break;
                case WORD_ELEMENT1:
                    this.handler.startElement(word, word, Common.ELEMENT);
                    wordContext = WORD_ELEMENT;
                    break;
                case WORD_ATTLIST1:
                    this.handler.startElement(word, word, Common.ATTLIST);
                    wordContext = WORD_ATTLIST;
                    break;
                case WORD_ENTITY1:
                    wordContext = WORD_ENTITY;
                    break;
                case WORD_ENTITY:
                    break;
                case WORD_ELEMENT:
                case WORD_ATTLIST:
                    // Find out the instance value (*, ? or +)
                    int instance = this.getInstanceValue(word.charAt(len-1));
                    //	Get rid of the extra character
                    if (instance != Common.TYPE_1)
                        word = curWord.substring(0, len-1);
                    
                    try {
                        this.handler.element(word, word, instance);
                    } catch(MissingEndOfEltException e) {
                        if (wordContext == WORD_ATTLIST) {
                            //
                            //  The TreeBuilder is done with the previous
                            //  attribute and would expect an end of ATTLIST
                            //  declaration.
                            //  We might have several attributes declared on the
                            //  same ATTLIST declaration.
                            //  Let's continue assuming so, the TreeBuilder
                            //  checks the attribute semantic and will throw
                            // if this is not the case.
                            //
                            this.handler.startElement(e.propName, e.propName,
                                    Common.ATTLIST);
                            this.handler.element(word, word, instance);
                        }
                    }
                    
                    break;
                default:
            }
            curWord.delete(0, len);
        }
        return wordContext;
    }
    
    /**
     *	Parse the document, calling back the handler
     */
    void parse() throws IOException, SchemaParseException {
        char		c;
        StringBuffer 	curWord = new StringBuffer();
        int		wordContext = WORD_NO_CONTEXT;
        int		level = 0;
        
        while ((c=this.getNext()) != '\0') {
            switch(c) {
                case '<':
                    //	Check if we have <! or <--
                    char c1 = this.getNext();
                    if (c1 == '!') {
                        //	Check if the next word is reserved
                        if (wordContext != WORD_NO_CONTEXT
                                && wordContext != WORD_COMMENT) {
                            System.err.println("Error: found a '<!' sequence within another '<!' sequence");	// NOI18N
                            throw new SchemaParseException("Warning: found a '<!' sequence within another '<!' sequence");	// NOI18N
                        }
                        if (wordContext != WORD_COMMENT)
                            wordContext = WORD_CHECK;
                    } else if (c1 == '?') {
                        wordContext = WORD_PI;
                    } else {
                        curWord.append(c);
                        curWord.append(c1);
                    }
                    break;
                case '>':
                    //	Might be the end of a comment or <!element
                    switch (wordContext) {
                        case WORD_NO_CONTEXT:
                            //System.err.println("Error: Found '>' without '<!'");// NOI18N
                            throw new SchemaParseException("Error: Found '>' without '<!'");	// NOI18N
                        case WORD_PI:
                            String 	word = curWord.toString();
                            int		len = word.length();
                            if (word.endsWith("?")) {	// NOI18N
                                //	Ignore any PI
                                curWord.delete(0, len);
                                wordContext = WORD_NO_CONTEXT;
                            } else
                                curWord.append(c);
                            break;
                        case WORD_COMMENT:
                            word = curWord.toString();
                            len = word.length();
                            if (word.endsWith("--")) {	// NOI18N
                                this.handler.endElement();
                                curWord.delete(0, len);
                                wordContext = WORD_NO_CONTEXT;
                            } else
                                curWord.append(c);
                            break;
                        case WORD_ENTITY:
                            wordContext = WORD_NO_CONTEXT;
                            break;
                        default:
                            wordContext = this.processWord(curWord,
                                    wordContext);
                            this.handler.endElement();
                            wordContext = WORD_NO_CONTEXT;
                    }
                    break;
                case '(':
                    if (wordContext == WORD_ELEMENT
                            || wordContext == WORD_ATTLIST) {
                        wordContext = this.processWord(curWord, wordContext);
                        this.handler.startGroupElements();
                    } else
                        curWord.append(c);
                    break;
                case ')':
                    wordContext = this.processWord(curWord, wordContext);
                    if (wordContext == WORD_ELEMENT
                            || wordContext == WORD_ATTLIST) {
                        int instance = this.getInstanceValue(this.peekNext());
                        //	Get rid of the extra character
                        if (instance != Common.TYPE_1)
                            this.getNext();
                        this.handler.endGroupElements(instance);
                    } else
                        curWord.append(c);
                    break;
                case '|':
                    wordContext = this.processWord(curWord, wordContext);
                    if (wordContext == WORD_ELEMENT
                            || wordContext == WORD_ATTLIST)
                        this.handler.character(c);
                    else
                        curWord.append(c);
                    break;
                case '\n':
                case '\r':
                case '\t':
                case ' ':
                case ',':
                    wordContext = this.processWord(curWord, wordContext);
                    break;
                    //
                default:
                    curWord.append(c);
            }
        }
        
        if (wordContext != WORD_NO_CONTEXT)
            System.out.println("Warning: unexpected EOF");	// NOI18N
    }
    
    /**
     *  Start the DTD parsing (called by GenBeans class)
     */
    public void process() throws java.io.IOException, Schema2BeansException {
        if (this.filename == null && this.schemaIn == null)
            throw new IllegalArgumentException(Common.getMessage(
                    "FilenameNotSpecified_msg", errHeader));
        
        if (this.handler == null)
            throw new IllegalArgumentException(Common.getMessage(
                    "HandlerNotSpecified_msg", errHeader));
        
        if (config.isTraceParse()) {
            config.messageOut.println("Parsing file " + this.filename.toString() +	// NOI18N
                    " with handler " + this.handler.getClass());	// NOI18N
        }
        
        try {
            startupReader();
            this.handler.startDocument(config.getDocRoot());
            this.parse();
            shutdownReader();
            this.handler.endDocument();
        } catch(FileNotFoundException e) {
            config.messageOut.println("Error: file " + this.filename.toString() + " not found");	// NOI18N
            throw e;
        /*
        } catch (IllegalStateException e) {
        throw e;
    } catch (RuntimeException e) {
            TraceLogger.error(e);
        throw e;
         */
        }
    }
}
