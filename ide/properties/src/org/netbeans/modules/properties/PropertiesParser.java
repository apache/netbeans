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


package org.netbeans.modules.properties;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;


/**
 * Parser of .properties files. It generates structure of comment-key-vaue property elements.
 *
 * @author Petr Jiricka, Petr Hamernik, Peter Zavadsky
 * @see PropertiesStructure
 * @see Element.ItemElem
 */
class PropertiesParser {

    /** PropertiesFileEntry for which source is this parser created. */
    PropertiesFileEntry pfe;

    /** Appropriate properties editor - used for creating the PositionRefs */
    PropertiesEditorSupport editor;

    /** Properties file reader. Input stream. */
    PropertiesReader propertiesReader;
    
    /** Flag if parsing should be stopped. */
    private boolean stop = false;

    
    /** 
     * Creates parser. Has to be {@link init} afterwards.
     * @param pfe FileEntry where the properties file is stored.
     */
    public PropertiesParser(PropertiesFileEntry pfe) {
        this.pfe   = pfe;
    }

    
    /** Inits parser.
     * @exception IOException if any i/o problem occured during reading */
    void initParser() throws IOException {
        editor = pfe.getPropertiesEditor();
        propertiesReader = createReader();
    }
    
    /** Creates new input stream from the file object.
     * Finds the properties data object, checks if the document is loaded,
     * if not is loaded and created a stream from the document.
     * @exception IOException if any i/o problem occured during reading
     */
    private PropertiesReader createReader() throws IOException {
        // Get loaded document, or load it if necessary.
        Document loadDoc = null;
        
        if(editor.isDocumentLoaded()) {
            loadDoc = editor.getDocument();
        } 
    
        if(loadDoc == null) {
            loadDoc = editor.openDocument(); 
        }
            
        final Document document = loadDoc;
        final String[] str = new String[1];

        // safely take the text from the document
        document.render(new Runnable() {
            public void run() {
                try {
                    str[0] = document.getText(0, document.getLength());
                } catch(BadLocationException ble) {
                    // Should be not possible.
                    ble.printStackTrace();
                }
            }
        });

        return new PropertiesReader(str[0]);
    }

    /** Parses .properties file specified by <code>pfe</code> and resets its properties
     * structure.
     * @return new properties structure or null if parsing failed
     */
    public PropertiesStructure parseFile() {
        try {
            PropertiesStructure propStructure = parseFileMain();
            
            return propStructure;
        } catch(IOException e) {
            // Parsing failed, return null.
            return null;
        }
    }
    
    /** Stops parsing. */
    public void stop() {
        stop = true;
        clean();
    }
    
    /** Provides clean up after finish parsing. */
    public void clean() {
        if(propertiesReader != null) {
            try {
                propertiesReader.close();
                propertiesReader = null;
            } catch(IOException ioe) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ioe);
            }
        }
    }

    /** Parses .properties file and creates <code>PropertiesStruture</code>. */
    private PropertiesStructure parseFileMain() throws IOException {

        Map<String,Element.ItemElem> items = new HashMap<String,Element.ItemElem>(25, 1.0F);

        PropertiesReader reader = null;
        
        while (true) {
            if (stop) {
                // Parsing stopped -> return immediatelly.
                return null;
            }
            
            reader = propertiesReader;
            if (reader == null) {
                // Parsing was stopped.
                return null;
            }
            Element.ItemElem element = readNextElem(reader);
            
            if (element == null) {
                break;
            } else {
                // add at the end of the list
                items.put(element.getKey(), element);
            }
        }
        
        return new PropertiesStructure(createBiasBounds(0, reader.position), items);
    }

    /**
     * Reads next element from input stream. 
     * @return next element or null if the end of the stream occurred */
    private Element.ItemElem readNextElem(PropertiesReader in) throws IOException {
        Element.CommentElem commE;
        Element.KeyElem keyE;
        Element.ValueElem valueE;

        int begPos = in.position;

        // read the comment
        int keyPos = begPos;
        FlaggedLine fl = in.readLineExpectComment();
        StringBuffer comment = new StringBuffer();
        boolean firstNull = true;
        while (fl != null) {
            firstNull = false;
            if(fl.flag) {
                //part of the comment
                comment.append(trimComment(fl.line));
                comment.append(fl.lineSep);
                keyPos = in.position;
            } else
                // not a part of a comment
                break;
            fl = in.readLineExpectComment();
        }

        // exit completely if null is returned the very first time
        if (firstNull) {
            return null;
        }

        String comHelp;
        comHelp = comment.toString();
        if(comment.length() > 0)
            if(comment.charAt(comment.length() - 1) == '\n')
                comHelp = comment.substring(0, comment.length() - 1);

        commE = new Element.CommentElem(createBiasBounds(begPos, keyPos), UtilConvert.loadConvert(comHelp));
        // fl now contains the line after the comment or  null if none exists


        if(fl == null) {
            keyE = null;
            valueE = null;
        } else {
            // read the key and the value
            // list of
            ArrayList<FlaggedLine> lines = new ArrayList<FlaggedLine>(2);
            fl.startPosition = keyPos;
            fl.stringValue = fl.line.toString();
            lines.add(fl);
            int nowPos;
            while (isPartialLine(fl.line)) {
                // do something with the previous line
                fl.stringValue = fl.stringValue.substring(0, fl.stringValue/*fix: was: line*/.length() - 1);
                // now the new line
                nowPos = in.position;
                fl = in.readLineNoFrills();
                if(fl == null) break;
                // delete the leading whitespaces
                int startIndex=0;
                for(startIndex=0; startIndex < fl.line.length(); startIndex++)
                    if(UtilConvert.whiteSpaceChars.indexOf(fl.line.charAt(startIndex)) == -1)
                        break;
                fl.stringValue = fl.line.substring(startIndex);
                fl.startPosition = nowPos + startIndex;
                lines.add(fl);
            }
            // now I have an ArrayList with strings representing lines and positions of the first non-whitespace character

            PositionMap positionMap = new PositionMap(lines);
            String line = positionMap.getString();

            // Find start of key
            int len = line.length();
            int keyStart;
            for(keyStart=0; keyStart<len; keyStart++) {
                if(UtilConvert.whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                    break;
            }
            
            // Find separation between key and value
            int separatorIndex;
            for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                char currentChar = line.charAt(separatorIndex);
                if(currentChar == '\\')
                    separatorIndex++;
                else if(UtilConvert.keyValueSeparators.indexOf(currentChar) != -1)
                    break;
            }

            // Skip over whitespace after key if any
            int valueIndex;
            for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)
                if(UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                    break;

            // Skip over one non whitespace key value separators if any
            if(valueIndex < len)
                if(UtilConvert.strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                    valueIndex++;

            // Skip over white space after other separators if any
            while (valueIndex < len) {
                if(UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                    break;
                valueIndex++;
            }
            String key = line.substring(keyStart, separatorIndex);
            String value = (separatorIndex < len) ? line.substring(valueIndex, len) : ""; // NOI18N

            if(key == null)
                // PENDING - should join with the next comment
                ;

            int currentPos = in.position;
            int valuePosFile = 0;
            
            try {
                valuePosFile = positionMap.getFilePosition(valueIndex);
            } catch (ArrayIndexOutOfBoundsException e) {
                valuePosFile = currentPos;
            }
            
            keyE   = new Element.KeyElem  (createBiasBounds(keyPos, valuePosFile), UtilConvert.loadConvert(key));
            valueE = new Element.ValueElem(createBiasBounds(valuePosFile, currentPos), UtilConvert.loadConvert(value));
        }
        
        return new Element.ItemElem(createBiasBounds(begPos, in.position), keyE, valueE, commE);
    }

    /** Remove leading comment markers. */
    private StringBuffer trimComment(StringBuffer line) {
        while (line.length() > 0) {
            char lead = line.charAt(0);
            if (lead == '#' || lead == '!') {
                line.deleteCharAt(0);
            } else {
                break;
            }
        }
        return line;
    }

    /** Utility method. Computes the real offset from the long value representing position in the parser.
     * @return the offset
     */
    private static int position(long p) {
        return (int)(p & 0xFFFFFFFFL);
    }

    /** Creates position bounds. For obtaining the real offsets is used
     * previous method position()
     * @param begin the begin in the internal position form
     * @param end the end in the internal position form
     * @return the bounds
     */
    private PositionBounds createBiasBounds(long begin, long end) {
        PositionRef posBegin = editor.createPositionRef(position(begin), Position.Bias.Forward);
        PositionRef posEnd = editor.createPositionRef(position(end), Position.Bias.Backward);
        return new PositionBounds(posBegin, posEnd);
    }

    /** 
     * Properties reader which allows reading from an input stream or from a string and remembers
     * its position in the document.
     */
    private static class PropertiesReader extends BufferedReader {

        /** Name constant of line separator system property. */
        private static final String LINE_SEPARATOR = "line.separator"; // NOI18N
        
        /** The character that someone peeked. */
        private int peekChar = -1;
        
        /** Position after the last character read. */
        public int position = 0;


        /** Creates <code>PropertiesReader</code> from buffer. */
        private PropertiesReader(String buffer) {
            super(new StringReader(buffer));
        }

        /** Creates <code>PropertiesReader</code> from another reader. */
        private PropertiesReader(Reader reader) {
            super(reader);
        }
        
        
        /** Read one character from the stream and increases the position.
         * @return the character or -1 if the end of the stream has been reached
         */
        public int read() throws IOException {
            int character = peek();
            peekChar = -1;
            if(character != -1)
                position++;

            return character;
        }

        /** Returns the next character without increasing the position. Subsequent calls
         * to peek() and read() will return the same character.
         * @return the character or -1 if the end of the stream has been reached
         */
        private int peek() throws IOException {
            if(peekChar == -1)
                peekChar = super.read();
            
            return peekChar;
        }

        /** Reads the next line and returns the flag as true if the line is a comment line.
         *  If the input is empty returns null
         *  Flag in the result is true if the line is a comment line
         */
        public FlaggedLine readLineExpectComment() throws IOException {
            int charRead = read();
            if(charRead == -1)
                // end of the reader reached
                return null;

            boolean decided = false;
            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int)'\n' && charRead != (int)'\r') {
                if(!decided)
                    if(UtilConvert.whiteSpaceChars.indexOf((char)charRead) == -1) {
                        // not a whitespace - decide now
                        fl.flag = (((char)charRead == '!') || ((char)charRead == '#'));
                        decided = true;
                    }
                fl.line.append((char)charRead);
                charRead = read();
            }

            if(!decided)
                // all were whitespaces
                fl.flag = true;

            // set the line separator
            if(charRead == (int)'\r')
                if(peek() == (int)'\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                } else
                    fl.lineSep = "\r"; // NOI18N
            else
                if(charRead == (int)'\n') 
                    fl.lineSep = "\n"; // NOI18N
                else
                    fl.lineSep = System.getProperty(LINE_SEPARATOR);

            return fl;
        }

        /** Reads the next line. 
         * @return <code>FlaggedLine</code> or null if the input is empty */
        public FlaggedLine readLineNoFrills() throws IOException {
            int charRead = read();
            if(charRead == -1)
                // end of the reader reached
                return null;

            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int)'\n' && charRead != (int)'\r') {
                fl.line.append((char)charRead);
                charRead = read();
            }

            // set the line separator
            if(charRead == (int)'\r')
                if(peek() == (int)'\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                } else
                    fl.lineSep = "\r"; // NOI18N
            else
                if(charRead == (int)'\n') // NOI18N
                    fl.lineSep = "\n"; // NOI18N
                else
                    fl.lineSep = System.getProperty(LINE_SEPARATOR);

            return fl;
        }

    } // End of nested class PropertiesReader.

    /**
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private static boolean isPartialLine (StringBuffer line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }

    /** Nested class which maps positions in a string to positions in the underlying file.
     * @see FlaggedLine */
    private static class PositionMap {

        /** List of <code>FlaggedLine</code>'s. */
        private List<FlaggedLine> list;
        
        
        /** Constructor - expects a list of FlaggedLine */
        PositionMap(List<FlaggedLine> lines) {
            list = lines;
        }
        

        /** Returns the string represented by the object */
        public String getString() {
            String allLines = list.get(0).stringValue;
            for (int part=1; part<list.size(); part++) {
                allLines += list.get(part).stringValue;
            }
            return allLines;
        }

        /** Returns position in the file for a position in a string
         * @param posString position in the string to find file position for
         * @return position in the file 
         * @exception ArrayIndexOutOfBoundsException if the requested position is outside 
         * the area represented by this object
         */                  
        public int getFilePosition(int posString) throws ArrayIndexOutOfBoundsException {
            // get the part
            int part;
            int lengthSoFar = 0;
            int lastLengthSoFar = 0;
            for (part=0; part < list.size(); part++) {
                lastLengthSoFar = lengthSoFar;
                lengthSoFar += list.get(part).stringValue.length();
                // brute patch - last (cr)lf should not be the part of the thing, other should
                if (part == list.size() - 1) {
                    if (lengthSoFar >= posString) {
                        break;
                    }
                } else {
                    if (lengthSoFar > posString) {
                        break;
                    }
                }
            }
            if (posString > lengthSoFar) {
                throw new ArrayIndexOutOfBoundsException("not in scope"); // NOI18N
            }
            return list.get(part).startPosition + posString - lastLengthSoFar;
        }
    } // End of nested class PositionMap.

    
    /** Helper nested class. */
    private static class FlaggedLine {

        /** Line buffer. */
        StringBuffer line;
        
        /** Flag. */
        boolean flag;
        
        /** Line separator. */
        String lineSep;
        
        /** Start position. */
        int startPosition;
        
        /** Value. */
        String stringValue;

        
        /** Constructor. */
        FlaggedLine() {
            line = new StringBuffer();
            flag = false;
            lineSep = "\n"; // NOI18N
            startPosition = 0;
        }
    } // End of nested class FlaggedLine.
}
