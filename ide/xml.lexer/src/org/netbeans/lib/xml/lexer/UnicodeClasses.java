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

package org.netbeans.lib.xml.lexer;

/**
 * Set of methods classifing class of character according to XML specs.
 * <p>
 * Code is copied from TAX library!
 *
 * @author  Libor Kramolis
 * @author  Petr Kuzel
 */
public class UnicodeClasses {

    /** Contains static methods only */
    UnicodeClasses () {
    }

    //
    // generated from XML recomendation
    //

    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Char
     */
    public static boolean isXMLChar (int c) {
        // #x0009
        if ( c == 0x0009 ) return true;
        
        // #x000a
        if ( c == 0x000a ) return true;
        
        // #x000d
        if ( c == 0x000d ) return true;
        
        // [ #x0020 - #xd7ff ]
        if ( c <  0x0020 ) return false;
        if ( c <= 0xd7ff ) return true;
        
        // [ #xe000 - #xfffd ]
        if ( c <  0xe000 ) return false;
        if ( c <= 0xfffd ) return true;
        
        // [ #x10000 - #x10ffff ]
        if ( c <  0x10000  ) return false;
        if ( c <= 0x10ffff ) return true;
        
        return false;
    }
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-NameChar
     */
    public static boolean isXMLNameChar (int c) {
        return ( ( isXMLLetter (c) )
        ||  ( isXMLDigit (c) )
        ||  ( c == '.' )
        ||  ( c == '-' )
        ||  ( c == '_' )
        ||  ( c == ':' )
        ||  ( isXMLCombiningChar (c) )
        ||  ( isXMLExtender (c) ) );
    }
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Name
     */
    public static boolean isXMLNameStartChar (int c) {
        return ( ( isXMLLetter (c) )
        || ( c == '_' )
        || ( c ==':' ) );
    }
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Letter
     */
    public static boolean isXMLLetter (int c) {
        return ( isXMLBaseChar (c) || isXMLIdeographic (c) );
    }
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-BaseChar
     */
    public static boolean isXMLBaseChar (int c) {
        // [ #x0041 - #x005a ]
        if ( c <  0x0041 ) return false;
        if ( c <= 0x005a ) return true;
        
        // [ #x0061 - #x007a ]
        if ( c <  0x0061 ) return false;
        if ( c <= 0x007a ) return true;
        
        // [ #x00c0 - #x00d6 ]
        if ( c <  0x00c0 ) return false;
        if ( c <= 0x00d6 ) return true;
        
        // [ #x00d8 - #x00f6 ]
        if ( c <  0x00d8 ) return false;
        if ( c <= 0x00f6 ) return true;
        
        // [ #x00f8 - #x00ff ]
        if ( c <  0x00f8 ) return false;
        if ( c <= 0x00ff ) return true;
        
        // [ #x0100 - #x0131 ]
        if ( c <  0x0100 ) return false;
        if ( c <= 0x0131 ) return true;
        
        // [ #x0134 - #x013e ]
        if ( c <  0x0134 ) return false;
        if ( c <= 0x013e ) return true;
        
        // [ #x0141 - #x0148 ]
        if ( c <  0x0141 ) return false;
        if ( c <= 0x0148 ) return true;
        
        // [ #x014a - #x017e ]
        if ( c <  0x014a ) return false;
        if ( c <= 0x017e ) return true;
        
        // [ #x0180 - #x01c3 ]
        if ( c <  0x0180 ) return false;
        if ( c <= 0x01c3 ) return true;
        
        // [ #x01cd - #x01f0 ]
        if ( c <  0x01cd ) return false;
        if ( c <= 0x01f0 ) return true;
        
        // [ #x01f4 - #x01f5 ]
        if ( c <  0x01f4 ) return false;
        if ( c <= 0x01f5 ) return true;
        
        // [ #x01fa - #x0217 ]
        if ( c <  0x01fa ) return false;
        if ( c <= 0x0217 ) return true;
        
        // [ #x0250 - #x02a8 ]
        if ( c <  0x0250 ) return false;
        if ( c <= 0x02a8 ) return true;
        
        // [ #x02bb - #x02c1 ]
        if ( c <  0x02bb ) return false;
        if ( c <= 0x02c1 ) return true;
        
        // #x0386
        if ( c == 0x0386 ) return true;
        
        // [ #x0388 - #x038a ]
        if ( c <  0x0388 ) return false;
        if ( c <= 0x038a ) return true;
        
        // #x038c
        if ( c == 0x038c ) return true;
        
        // [ #x038e - #x03a1 ]
        if ( c <  0x038e ) return false;
        if ( c <= 0x03a1 ) return true;
        
        // [ #x03a3 - #x03ce ]
        if ( c <  0x03a3 ) return false;
        if ( c <= 0x03ce ) return true;
        
        // [ #x03d0 - #x03d6 ]
        if ( c <  0x03d0 ) return false;
        if ( c <= 0x03d6 ) return true;
        
        // #x03da
        if ( c == 0x03da ) return true;
        
        // #x03dc
        if ( c == 0x03dc ) return true;
        
        // #x03de
        if ( c == 0x03de ) return true;
        
        // #x03e0
        if ( c == 0x03e0 ) return true;
        
        // [ #x03e2 - #x03f3 ]
        if ( c <  0x03e2 ) return false;
        if ( c <= 0x03f3 ) return true;
        
        // [ #x0401 - #x040c ]
        if ( c <  0x0401 ) return false;
        if ( c <= 0x040c ) return true;
        
        // [ #x040e - #x044f ]
        if ( c <  0x040e ) return false;
        if ( c <= 0x044f ) return true;
        
        // [ #x0451 - #x045c ]
        if ( c <  0x0451 ) return false;
        if ( c <= 0x045c ) return true;
        
        // [ #x045e - #x0481 ]
        if ( c <  0x045e ) return false;
        if ( c <= 0x0481 ) return true;
        
        // [ #x0490 - #x04c4 ]
        if ( c <  0x0490 ) return false;
        if ( c <= 0x04c4 ) return true;
        
        // [ #x04c7 - #x04c8 ]
        if ( c <  0x04c7 ) return false;
        if ( c <= 0x04c8 ) return true;
        
        // [ #x04cb - #x04cc ]
        if ( c <  0x04cb ) return false;
        if ( c <= 0x04cc ) return true;
        
        // [ #x04d0 - #x04eb ]
        if ( c <  0x04d0 ) return false;
        if ( c <= 0x04eb ) return true;
        
        // [ #x04ee - #x04f5 ]
        if ( c <  0x04ee ) return false;
        if ( c <= 0x04f5 ) return true;
        
        // [ #x04f8 - #x04f9 ]
        if ( c <  0x04f8 ) return false;
        if ( c <= 0x04f9 ) return true;
        
        // [ #x0531 - #x0556 ]
        if ( c <  0x0531 ) return false;
        if ( c <= 0x0556 ) return true;
        
        // #x0559
        if ( c == 0x0559 ) return true;
        
        // [ #x0561 - #x0586 ]
        if ( c <  0x0561 ) return false;
        if ( c <= 0x0586 ) return true;
        
        // [ #x05d0 - #x05ea ]
        if ( c <  0x05d0 ) return false;
        if ( c <= 0x05ea ) return true;
        
        // [ #x05f0 - #x05f2 ]
        if ( c <  0x05f0 ) return false;
        if ( c <= 0x05f2 ) return true;
        
        // [ #x0621 - #x063a ]
        if ( c <  0x0621 ) return false;
        if ( c <= 0x063a ) return true;
        
        // [ #x0641 - #x064a ]
        if ( c <  0x0641 ) return false;
        if ( c <= 0x064a ) return true;
        
        // [ #x0671 - #x06b7 ]
        if ( c <  0x0671 ) return false;
        if ( c <= 0x06b7 ) return true;
        
        // [ #x06ba - #x06be ]
        if ( c <  0x06ba ) return false;
        if ( c <= 0x06be ) return true;
        
        // [ #x06c0 - #x06ce ]
        if ( c <  0x06c0 ) return false;
        if ( c <= 0x06ce ) return true;
        
        // [ #x06d0 - #x06d3 ]
        if ( c <  0x06d0 ) return false;
        if ( c <= 0x06d3 ) return true;
        
        // #x06d5
        if ( c == 0x06d5 ) return true;
        
        // [ #x06e5 - #x06e6 ]
        if ( c <  0x06e5 ) return false;
        if ( c <= 0x06e6 ) return true;
        
        // [ #x0905 - #x0939 ]
        if ( c <  0x0905 ) return false;
        if ( c <= 0x0939 ) return true;
        
        // #x093d
        if ( c == 0x093d ) return true;
        
        // [ #x0958 - #x0961 ]
        if ( c <  0x0958 ) return false;
        if ( c <= 0x0961 ) return true;
        
        // [ #x0985 - #x098c ]
        if ( c <  0x0985 ) return false;
        if ( c <= 0x098c ) return true;
        
        // [ #x098f - #x0990 ]
        if ( c <  0x098f ) return false;
        if ( c <= 0x0990 ) return true;
        
        // [ #x0993 - #x09a8 ]
        if ( c <  0x0993 ) return false;
        if ( c <= 0x09a8 ) return true;
        
        // [ #x09aa - #x09b0 ]
        if ( c <  0x09aa ) return false;
        if ( c <= 0x09b0 ) return true;
        
        // #x09b2
        if ( c == 0x09b2 ) return true;
        
        // [ #x09b6 - #x09b9 ]
        if ( c <  0x09b6 ) return false;
        if ( c <= 0x09b9 ) return true;
        
        // [ #x09dc - #x09dd ]
        if ( c <  0x09dc ) return false;
        if ( c <= 0x09dd ) return true;
        
        // [ #x09df - #x09e1 ]
        if ( c <  0x09df ) return false;
        if ( c <= 0x09e1 ) return true;
        
        // [ #x09f0 - #x09f1 ]
        if ( c <  0x09f0 ) return false;
        if ( c <= 0x09f1 ) return true;
        
        // [ #x0a05 - #x0a0a ]
        if ( c <  0x0a05 ) return false;
        if ( c <= 0x0a0a ) return true;
        
        // [ #x0a0f - #x0a10 ]
        if ( c <  0x0a0f ) return false;
        if ( c <= 0x0a10 ) return true;
        
        // [ #x0a13 - #x0a28 ]
        if ( c <  0x0a13 ) return false;
        if ( c <= 0x0a28 ) return true;
        
        // [ #x0a2a - #x0a30 ]
        if ( c <  0x0a2a ) return false;
        if ( c <= 0x0a30 ) return true;
        
        // [ #x0a32 - #x0a33 ]
        if ( c <  0x0a32 ) return false;
        if ( c <= 0x0a33 ) return true;
        
        // [ #x0a35 - #x0a36 ]
        if ( c <  0x0a35 ) return false;
        if ( c <= 0x0a36 ) return true;
        
        // [ #x0a38 - #x0a39 ]
        if ( c <  0x0a38 ) return false;
        if ( c <= 0x0a39 ) return true;
        
        // [ #x0a59 - #x0a5c ]
        if ( c <  0x0a59 ) return false;
        if ( c <= 0x0a5c ) return true;
        
        // #x0a5e
        if ( c == 0x0a5e ) return true;
        
        // [ #x0a72 - #x0a74 ]
        if ( c <  0x0a72 ) return false;
        if ( c <= 0x0a74 ) return true;
        
        // [ #x0a85 - #x0a8b ]
        if ( c <  0x0a85 ) return false;
        if ( c <= 0x0a8b ) return true;
        
        // #x0a8d
        if ( c == 0x0a8d ) return true;
        
        // [ #x0a8f - #x0a91 ]
        if ( c <  0x0a8f ) return false;
        if ( c <= 0x0a91 ) return true;
        
        // [ #x0a93 - #x0aa8 ]
        if ( c <  0x0a93 ) return false;
        if ( c <= 0x0aa8 ) return true;
        
        // [ #x0aaa - #x0ab0 ]
        if ( c <  0x0aaa ) return false;
        if ( c <= 0x0ab0 ) return true;
        
        // [ #x0ab2 - #x0ab3 ]
        if ( c <  0x0ab2 ) return false;
        if ( c <= 0x0ab3 ) return true;
        
        // [ #x0ab5 - #x0ab9 ]
        if ( c <  0x0ab5 ) return false;
        if ( c <= 0x0ab9 ) return true;
        
        // #x0abd
        if ( c == 0x0abd ) return true;
        
        // #x0ae0
        if ( c == 0x0ae0 ) return true;
        
        // [ #x0b05 - #x0b0c ]
        if ( c <  0x0b05 ) return false;
        if ( c <= 0x0b0c ) return true;
        
        // [ #x0b0f - #x0b10 ]
        if ( c <  0x0b0f ) return false;
        if ( c <= 0x0b10 ) return true;
        
        // [ #x0b13 - #x0b28 ]
        if ( c <  0x0b13 ) return false;
        if ( c <= 0x0b28 ) return true;
        
        // [ #x0b2a - #x0b30 ]
        if ( c <  0x0b2a ) return false;
        if ( c <= 0x0b30 ) return true;
        
        // [ #x0b32 - #x0b33 ]
        if ( c <  0x0b32 ) return false;
        if ( c <= 0x0b33 ) return true;
        
        // [ #x0b36 - #x0b39 ]
        if ( c <  0x0b36 ) return false;
        if ( c <= 0x0b39 ) return true;
        
        // #x0b3d
        if ( c == 0x0b3d ) return true;
        
        // [ #x0b5c - #x0b5d ]
        if ( c <  0x0b5c ) return false;
        if ( c <= 0x0b5d ) return true;
        
        // [ #x0b5f - #x0b61 ]
        if ( c <  0x0b5f ) return false;
        if ( c <= 0x0b61 ) return true;
        
        // [ #x0b85 - #x0b8a ]
        if ( c <  0x0b85 ) return false;
        if ( c <= 0x0b8a ) return true;
        
        // [ #x0b8e - #x0b90 ]
        if ( c <  0x0b8e ) return false;
        if ( c <= 0x0b90 ) return true;
        
        // [ #x0b92 - #x0b95 ]
        if ( c <  0x0b92 ) return false;
        if ( c <= 0x0b95 ) return true;
        
        // [ #x0b99 - #x0b9a ]
        if ( c <  0x0b99 ) return false;
        if ( c <= 0x0b9a ) return true;
        
        // #x0b9c
        if ( c == 0x0b9c ) return true;
        
        // [ #x0b9e - #x0b9f ]
        if ( c <  0x0b9e ) return false;
        if ( c <= 0x0b9f ) return true;
        
        // [ #x0ba3 - #x0ba4 ]
        if ( c <  0x0ba3 ) return false;
        if ( c <= 0x0ba4 ) return true;
        
        // [ #x0ba8 - #x0baa ]
        if ( c <  0x0ba8 ) return false;
        if ( c <= 0x0baa ) return true;
        
        // [ #x0bae - #x0bb5 ]
        if ( c <  0x0bae ) return false;
        if ( c <= 0x0bb5 ) return true;
        
        // [ #x0bb7 - #x0bb9 ]
        if ( c <  0x0bb7 ) return false;
        if ( c <= 0x0bb9 ) return true;
        
        // [ #x0c05 - #x0c0c ]
        if ( c <  0x0c05 ) return false;
        if ( c <= 0x0c0c ) return true;
        
        // [ #x0c0e - #x0c10 ]
        if ( c <  0x0c0e ) return false;
        if ( c <= 0x0c10 ) return true;
        
        // [ #x0c12 - #x0c28 ]
        if ( c <  0x0c12 ) return false;
        if ( c <= 0x0c28 ) return true;
        
        // [ #x0c2a - #x0c33 ]
        if ( c <  0x0c2a ) return false;
        if ( c <= 0x0c33 ) return true;
        
        // [ #x0c35 - #x0c39 ]
        if ( c <  0x0c35 ) return false;
        if ( c <= 0x0c39 ) return true;
        
        // [ #x0c60 - #x0c61 ]
        if ( c <  0x0c60 ) return false;
        if ( c <= 0x0c61 ) return true;
        
        // [ #x0c85 - #x0c8c ]
        if ( c <  0x0c85 ) return false;
        if ( c <= 0x0c8c ) return true;
        
        // [ #x0c8e - #x0c90 ]
        if ( c <  0x0c8e ) return false;
        if ( c <= 0x0c90 ) return true;
        
        // [ #x0c92 - #x0ca8 ]
        if ( c <  0x0c92 ) return false;
        if ( c <= 0x0ca8 ) return true;
        
        // [ #x0caa - #x0cb3 ]
        if ( c <  0x0caa ) return false;
        if ( c <= 0x0cb3 ) return true;
        
        // [ #x0cb5 - #x0cb9 ]
        if ( c <  0x0cb5 ) return false;
        if ( c <= 0x0cb9 ) return true;
        
        // #x0cde
        if ( c == 0x0cde ) return true;
        
        // [ #x0ce0 - #x0ce1 ]
        if ( c <  0x0ce0 ) return false;
        if ( c <= 0x0ce1 ) return true;
        
        // [ #x0d05 - #x0d0c ]
        if ( c <  0x0d05 ) return false;
        if ( c <= 0x0d0c ) return true;
        
        // [ #x0d0e - #x0d10 ]
        if ( c <  0x0d0e ) return false;
        if ( c <= 0x0d10 ) return true;
        
        // [ #x0d12 - #x0d28 ]
        if ( c <  0x0d12 ) return false;
        if ( c <= 0x0d28 ) return true;
        
        // [ #x0d2a - #x0d39 ]
        if ( c <  0x0d2a ) return false;
        if ( c <= 0x0d39 ) return true;
        
        // [ #x0d60 - #x0d61 ]
        if ( c <  0x0d60 ) return false;
        if ( c <= 0x0d61 ) return true;
        
        // [ #x0e01 - #x0e2e ]
        if ( c <  0x0e01 ) return false;
        if ( c <= 0x0e2e ) return true;
        
        // #x0e30
        if ( c == 0x0e30 ) return true;
        
        // [ #x0e32 - #x0e33 ]
        if ( c <  0x0e32 ) return false;
        if ( c <= 0x0e33 ) return true;
        
        // [ #x0e40 - #x0e45 ]
        if ( c <  0x0e40 ) return false;
        if ( c <= 0x0e45 ) return true;
        
        // [ #x0e81 - #x0e82 ]
        if ( c <  0x0e81 ) return false;
        if ( c <= 0x0e82 ) return true;
        
        // #x0e84
        if ( c == 0x0e84 ) return true;
        
        // [ #x0e87 - #x0e88 ]
        if ( c <  0x0e87 ) return false;
        if ( c <= 0x0e88 ) return true;
        
        // #x0e8a
        if ( c == 0x0e8a ) return true;
        
        // #x0e8d
        if ( c == 0x0e8d ) return true;
        
        // [ #x0e94 - #x0e97 ]
        if ( c <  0x0e94 ) return false;
        if ( c <= 0x0e97 ) return true;
        
        // [ #x0e99 - #x0e9f ]
        if ( c <  0x0e99 ) return false;
        if ( c <= 0x0e9f ) return true;
        
        // [ #x0ea1 - #x0ea3 ]
        if ( c <  0x0ea1 ) return false;
        if ( c <= 0x0ea3 ) return true;
        
        // #x0ea5
        if ( c == 0x0ea5 ) return true;
        
        // #x0ea7
        if ( c == 0x0ea7 ) return true;
        
        // [ #x0eaa - #x0eab ]
        if ( c <  0x0eaa ) return false;
        if ( c <= 0x0eab ) return true;
        
        // [ #x0ead - #x0eae ]
        if ( c <  0x0ead ) return false;
        if ( c <= 0x0eae ) return true;
        
        // #x0eb0
        if ( c == 0x0eb0 ) return true;
        
        // [ #x0eb2 - #x0eb3 ]
        if ( c <  0x0eb2 ) return false;
        if ( c <= 0x0eb3 ) return true;
        
        // #x0ebd
        if ( c == 0x0ebd ) return true;
        
        // [ #x0ec0 - #x0ec4 ]
        if ( c <  0x0ec0 ) return false;
        if ( c <= 0x0ec4 ) return true;
        
        // [ #x0f40 - #x0f47 ]
        if ( c <  0x0f40 ) return false;
        if ( c <= 0x0f47 ) return true;
        
        // [ #x0f49 - #x0f69 ]
        if ( c <  0x0f49 ) return false;
        if ( c <= 0x0f69 ) return true;
        
        // [ #x10a0 - #x10c5 ]
        if ( c <  0x10a0 ) return false;
        if ( c <= 0x10c5 ) return true;
        
        // [ #x10d0 - #x10f6 ]
        if ( c <  0x10d0 ) return false;
        if ( c <= 0x10f6 ) return true;
        
        // #x1100
        if ( c == 0x1100 ) return true;
        
        // [ #x1102 - #x1103 ]
        if ( c <  0x1102 ) return false;
        if ( c <= 0x1103 ) return true;
        
        // [ #x1105 - #x1107 ]
        if ( c <  0x1105 ) return false;
        if ( c <= 0x1107 ) return true;
        
        // #x1109
        if ( c == 0x1109 ) return true;
        
        // [ #x110b - #x110c ]
        if ( c <  0x110b ) return false;
        if ( c <= 0x110c ) return true;
        
        // [ #x110e - #x1112 ]
        if ( c <  0x110e ) return false;
        if ( c <= 0x1112 ) return true;
        
        // #x113c
        if ( c == 0x113c ) return true;
        
        // #x113e
        if ( c == 0x113e ) return true;
        
        // #x1140
        if ( c == 0x1140 ) return true;
        
        // #x114c
        if ( c == 0x114c ) return true;
        
        // #x114e
        if ( c == 0x114e ) return true;
        
        // #x1150
        if ( c == 0x1150 ) return true;
        
        // [ #x1154 - #x1155 ]
        if ( c <  0x1154 ) return false;
        if ( c <= 0x1155 ) return true;
        
        // #x1159
        if ( c == 0x1159 ) return true;
        
        // [ #x115f - #x1161 ]
        if ( c <  0x115f ) return false;
        if ( c <= 0x1161 ) return true;
        
        // #x1163
        if ( c == 0x1163 ) return true;
        
        // #x1165
        if ( c == 0x1165 ) return true;
        
        // #x1167
        if ( c == 0x1167 ) return true;
        
        // #x1169
        if ( c == 0x1169 ) return true;
        
        // [ #x116d - #x116e ]
        if ( c <  0x116d ) return false;
        if ( c <= 0x116e ) return true;
        
        // [ #x1172 - #x1173 ]
        if ( c <  0x1172 ) return false;
        if ( c <= 0x1173 ) return true;
        
        // #x1175
        if ( c == 0x1175 ) return true;
        
        // #x119e
        if ( c == 0x119e ) return true;
        
        // #x11a8
        if ( c == 0x11a8 ) return true;
        
        // #x11ab
        if ( c == 0x11ab ) return true;
        
        // [ #x11ae - #x11af ]
        if ( c <  0x11ae ) return false;
        if ( c <= 0x11af ) return true;
        
        // [ #x11b7 - #x11b8 ]
        if ( c <  0x11b7 ) return false;
        if ( c <= 0x11b8 ) return true;
        
        // #x11ba
        if ( c == 0x11ba ) return true;
        
        // [ #x11bc - #x11c2 ]
        if ( c <  0x11bc ) return false;
        if ( c <= 0x11c2 ) return true;
        
        // #x11eb
        if ( c == 0x11eb ) return true;
        
        // #x11f0
        if ( c == 0x11f0 ) return true;
        
        // #x11f9
        if ( c == 0x11f9 ) return true;
        
        // [ #x1e00 - #x1e9b ]
        if ( c <  0x1e00 ) return false;
        if ( c <= 0x1e9b ) return true;
        
        // [ #x1ea0 - #x1ef9 ]
        if ( c <  0x1ea0 ) return false;
        if ( c <= 0x1ef9 ) return true;
        
        // [ #x1f00 - #x1f15 ]
        if ( c <  0x1f00 ) return false;
        if ( c <= 0x1f15 ) return true;
        
        // [ #x1f18 - #x1f1d ]
        if ( c <  0x1f18 ) return false;
        if ( c <= 0x1f1d ) return true;
        
        // [ #x1f20 - #x1f45 ]
        if ( c <  0x1f20 ) return false;
        if ( c <= 0x1f45 ) return true;
        
        // [ #x1f48 - #x1f4d ]
        if ( c <  0x1f48 ) return false;
        if ( c <= 0x1f4d ) return true;
        
        // [ #x1f50 - #x1f57 ]
        if ( c <  0x1f50 ) return false;
        if ( c <= 0x1f57 ) return true;
        
        // #x1f59
        if ( c == 0x1f59 ) return true;
        
        // #x1f5b
        if ( c == 0x1f5b ) return true;
        
        // #x1f5d
        if ( c == 0x1f5d ) return true;
        
        // [ #x1f5f - #x1f7d ]
        if ( c <  0x1f5f ) return false;
        if ( c <= 0x1f7d ) return true;
        
        // [ #x1f80 - #x1fb4 ]
        if ( c <  0x1f80 ) return false;
        if ( c <= 0x1fb4 ) return true;
        
        // [ #x1fb6 - #x1fbc ]
        if ( c <  0x1fb6 ) return false;
        if ( c <= 0x1fbc ) return true;
        
        // #x1fbe
        if ( c == 0x1fbe ) return true;
        
        // [ #x1fc2 - #x1fc4 ]
        if ( c <  0x1fc2 ) return false;
        if ( c <= 0x1fc4 ) return true;
        
        // [ #x1fc6 - #x1fcc ]
        if ( c <  0x1fc6 ) return false;
        if ( c <= 0x1fcc ) return true;
        
        // [ #x1fd0 - #x1fd3 ]
        if ( c <  0x1fd0 ) return false;
        if ( c <= 0x1fd3 ) return true;
        
        // [ #x1fd6 - #x1fdb ]
        if ( c <  0x1fd6 ) return false;
        if ( c <= 0x1fdb ) return true;
        
        // [ #x1fe0 - #x1fec ]
        if ( c <  0x1fe0 ) return false;
        if ( c <= 0x1fec ) return true;
        
        // [ #x1ff2 - #x1ff4 ]
        if ( c <  0x1ff2 ) return false;
        if ( c <= 0x1ff4 ) return true;
        
        // [ #x1ff6 - #x1ffc ]
        if ( c <  0x1ff6 ) return false;
        if ( c <= 0x1ffc ) return true;
        
        // #x2126
        if ( c == 0x2126 ) return true;
        
        // [ #x212a - #x212b ]
        if ( c <  0x212a ) return false;
        if ( c <= 0x212b ) return true;
        
        // #x212e
        if ( c == 0x212e ) return true;
        
        // [ #x2180 - #x2182 ]
        if ( c <  0x2180 ) return false;
        if ( c <= 0x2182 ) return true;
        
        // [ #x3041 - #x3094 ]
        if ( c <  0x3041 ) return false;
        if ( c <= 0x3094 ) return true;
        
        // [ #x30a1 - #x30fa ]
        if ( c <  0x30a1 ) return false;
        if ( c <= 0x30fa ) return true;
        
        // [ #x3105 - #x312c ]
        if ( c <  0x3105 ) return false;
        if ( c <= 0x312c ) return true;
        
        // [ #xac00 - #xd7a3 ]
        if ( c <  0xac00 ) return false;
        if ( c <= 0xd7a3 ) return true;
        
        return false;
    }
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Ideographic
     */
    public static boolean isXMLIdeographic (int c) {
        // #x3007
        if ( c == 0x3007 ) return true;
        
        // [ #x3021 - #x3029 ]
        if ( c <  0x3021 ) return false;
        if ( c <= 0x3029 ) return true;
        
        // [ #x4e00 - #x9fa5 ]
        if ( c <  0x4e00 ) return false;
        if ( c <= 0x9fa5 ) return true;
        
        return false;
    }
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-CombiningChar
     */
    public static boolean isXMLCombiningChar (int c) {
        // [ #x0300 - #x0345 ]
        if ( c <  0x0300 ) return false;
        if ( c <= 0x0345 ) return true;
        
        // [ #x0360 - #x0361 ]
        if ( c <  0x0360 ) return false;
        if ( c <= 0x0361 ) return true;
        
        // [ #x0483 - #x0486 ]
        if ( c <  0x0483 ) return false;
        if ( c <= 0x0486 ) return true;
        
        // [ #x0591 - #x05a1 ]
        if ( c <  0x0591 ) return false;
        if ( c <= 0x05a1 ) return true;
        
        // [ #x05a3 - #x05b9 ]
        if ( c <  0x05a3 ) return false;
        if ( c <= 0x05b9 ) return true;
        
        // [ #x05bb - #x05bd ]
        if ( c <  0x05bb ) return false;
        if ( c <= 0x05bd ) return true;
        
        // #x05bf
        if ( c == 0x05bf ) return true;
        
        // [ #x05c1 - #x05c2 ]
        if ( c <  0x05c1 ) return false;
        if ( c <= 0x05c2 ) return true;
        
        // #x05c4
        if ( c == 0x05c4 ) return true;
        
        // [ #x064b - #x0652 ]
        if ( c <  0x064b ) return false;
        if ( c <= 0x0652 ) return true;
        
        // #x0670
        if ( c == 0x0670 ) return true;
        
        // [ #x06d6 - #x06dc ]
        if ( c <  0x06d6 ) return false;
        if ( c <= 0x06dc ) return true;
        
        // [ #x06dd - #x06df ]
        if ( c <  0x06dd ) return false;
        if ( c <= 0x06df ) return true;
        
        // [ #x06e0 - #x06e4 ]
        if ( c <  0x06e0 ) return false;
        if ( c <= 0x06e4 ) return true;
        
        // [ #x06e7 - #x06e8 ]
        if ( c <  0x06e7 ) return false;
        if ( c <= 0x06e8 ) return true;
        
        // [ #x06ea - #x06ed ]
        if ( c <  0x06ea ) return false;
        if ( c <= 0x06ed ) return true;
        
        // [ #x0901 - #x0903 ]
        if ( c <  0x0901 ) return false;
        if ( c <= 0x0903 ) return true;
        
        // #x093c
        if ( c == 0x093c ) return true;
        
        // [ #x093e - #x094c ]
        if ( c <  0x093e ) return false;
        if ( c <= 0x094c ) return true;
        
        // #x094d
        if ( c == 0x094d ) return true;
        
        // [ #x0951 - #x0954 ]
        if ( c <  0x0951 ) return false;
        if ( c <= 0x0954 ) return true;
        
        // [ #x0962 - #x0963 ]
        if ( c <  0x0962 ) return false;
        if ( c <= 0x0963 ) return true;
        
        // [ #x0981 - #x0983 ]
        if ( c <  0x0981 ) return false;
        if ( c <= 0x0983 ) return true;
        
        // #x09bc
        if ( c == 0x09bc ) return true;
        
        // #x09be
        if ( c == 0x09be ) return true;
        
        // #x09bf
        if ( c == 0x09bf ) return true;
        
        // [ #x09c0 - #x09c4 ]
        if ( c <  0x09c0 ) return false;
        if ( c <= 0x09c4 ) return true;
        
        // [ #x09c7 - #x09c8 ]
        if ( c <  0x09c7 ) return false;
        if ( c <= 0x09c8 ) return true;
        
        // [ #x09cb - #x09cd ]
        if ( c <  0x09cb ) return false;
        if ( c <= 0x09cd ) return true;
        
        // #x09d7
        if ( c == 0x09d7 ) return true;
        
        // [ #x09e2 - #x09e3 ]
        if ( c <  0x09e2 ) return false;
        if ( c <= 0x09e3 ) return true;
        
        // #x0a02
        if ( c == 0x0a02 ) return true;
        
        // #x0a3c
        if ( c == 0x0a3c ) return true;
        
        // #x0a3e
        if ( c == 0x0a3e ) return true;
        
        // #x0a3f
        if ( c == 0x0a3f ) return true;
        
        // [ #x0a40 - #x0a42 ]
        if ( c <  0x0a40 ) return false;
        if ( c <= 0x0a42 ) return true;
        
        // [ #x0a47 - #x0a48 ]
        if ( c <  0x0a47 ) return false;
        if ( c <= 0x0a48 ) return true;
        
        // [ #x0a4b - #x0a4d ]
        if ( c <  0x0a4b ) return false;
        if ( c <= 0x0a4d ) return true;
        
        // [ #x0a70 - #x0a71 ]
        if ( c <  0x0a70 ) return false;
        if ( c <= 0x0a71 ) return true;
        
        // [ #x0a81 - #x0a83 ]
        if ( c <  0x0a81 ) return false;
        if ( c <= 0x0a83 ) return true;
        
        // #x0abc
        if ( c == 0x0abc ) return true;
        
        // [ #x0abe - #x0ac5 ]
        if ( c <  0x0abe ) return false;
        if ( c <= 0x0ac5 ) return true;
        
        // [ #x0ac7 - #x0ac9 ]
        if ( c <  0x0ac7 ) return false;
        if ( c <= 0x0ac9 ) return true;
        
        // [ #x0acb - #x0acd ]
        if ( c <  0x0acb ) return false;
        if ( c <= 0x0acd ) return true;
        
        // [ #x0b01 - #x0b03 ]
        if ( c <  0x0b01 ) return false;
        if ( c <= 0x0b03 ) return true;
        
        // #x0b3c
        if ( c == 0x0b3c ) return true;
        
        // [ #x0b3e - #x0b43 ]
        if ( c <  0x0b3e ) return false;
        if ( c <= 0x0b43 ) return true;
        
        // [ #x0b47 - #x0b48 ]
        if ( c <  0x0b47 ) return false;
        if ( c <= 0x0b48 ) return true;
        
        // [ #x0b4b - #x0b4d ]
        if ( c <  0x0b4b ) return false;
        if ( c <= 0x0b4d ) return true;
        
        // [ #x0b56 - #x0b57 ]
        if ( c <  0x0b56 ) return false;
        if ( c <= 0x0b57 ) return true;
        
        // [ #x0b82 - #x0b83 ]
        if ( c <  0x0b82 ) return false;
        if ( c <= 0x0b83 ) return true;
        
        // [ #x0bbe - #x0bc2 ]
        if ( c <  0x0bbe ) return false;
        if ( c <= 0x0bc2 ) return true;
        
        // [ #x0bc6 - #x0bc8 ]
        if ( c <  0x0bc6 ) return false;
        if ( c <= 0x0bc8 ) return true;
        
        // [ #x0bca - #x0bcd ]
        if ( c <  0x0bca ) return false;
        if ( c <= 0x0bcd ) return true;
        
        // #x0bd7
        if ( c == 0x0bd7 ) return true;
        
        // [ #x0c01 - #x0c03 ]
        if ( c <  0x0c01 ) return false;
        if ( c <= 0x0c03 ) return true;
        
        // [ #x0c3e - #x0c44 ]
        if ( c <  0x0c3e ) return false;
        if ( c <= 0x0c44 ) return true;
        
        // [ #x0c46 - #x0c48 ]
        if ( c <  0x0c46 ) return false;
        if ( c <= 0x0c48 ) return true;
        
        // [ #x0c4a - #x0c4d ]
        if ( c <  0x0c4a ) return false;
        if ( c <= 0x0c4d ) return true;
        
        // [ #x0c55 - #x0c56 ]
        if ( c <  0x0c55 ) return false;
        if ( c <= 0x0c56 ) return true;
        
        // [ #x0c82 - #x0c83 ]
        if ( c <  0x0c82 ) return false;
        if ( c <= 0x0c83 ) return true;
        
        // [ #x0cbe - #x0cc4 ]
        if ( c <  0x0cbe ) return false;
        if ( c <= 0x0cc4 ) return true;
        
        // [ #x0cc6 - #x0cc8 ]
        if ( c <  0x0cc6 ) return false;
        if ( c <= 0x0cc8 ) return true;
        
        // [ #x0cca - #x0ccd ]
        if ( c <  0x0cca ) return false;
        if ( c <= 0x0ccd ) return true;
        
        // [ #x0cd5 - #x0cd6 ]
        if ( c <  0x0cd5 ) return false;
        if ( c <= 0x0cd6 ) return true;
        
        // [ #x0d02 - #x0d03 ]
        if ( c <  0x0d02 ) return false;
        if ( c <= 0x0d03 ) return true;
        
        // [ #x0d3e - #x0d43 ]
        if ( c <  0x0d3e ) return false;
        if ( c <= 0x0d43 ) return true;
        
        // [ #x0d46 - #x0d48 ]
        if ( c <  0x0d46 ) return false;
        if ( c <= 0x0d48 ) return true;
        
        // [ #x0d4a - #x0d4d ]
        if ( c <  0x0d4a ) return false;
        if ( c <= 0x0d4d ) return true;
        
        // #x0d57
        if ( c == 0x0d57 ) return true;
        
        // #x0e31
        if ( c == 0x0e31 ) return true;
        
        // [ #x0e34 - #x0e3a ]
        if ( c <  0x0e34 ) return false;
        if ( c <= 0x0e3a ) return true;
        
        // [ #x0e47 - #x0e4e ]
        if ( c <  0x0e47 ) return false;
        if ( c <= 0x0e4e ) return true;
        
        // #x0eb1
        if ( c == 0x0eb1 ) return true;
        
        // [ #x0eb4 - #x0eb9 ]
        if ( c <  0x0eb4 ) return false;
        if ( c <= 0x0eb9 ) return true;
        
        // [ #x0ebb - #x0ebc ]
        if ( c <  0x0ebb ) return false;
        if ( c <= 0x0ebc ) return true;
        
        // [ #x0ec8 - #x0ecd ]
        if ( c <  0x0ec8 ) return false;
        if ( c <= 0x0ecd ) return true;
        
        // [ #x0f18 - #x0f19 ]
        if ( c <  0x0f18 ) return false;
        if ( c <= 0x0f19 ) return true;
        
        // #x0f35
        if ( c == 0x0f35 ) return true;
        
        // #x0f37
        if ( c == 0x0f37 ) return true;
        
        // #x0f39
        if ( c == 0x0f39 ) return true;
        
        // #x0f3e
        if ( c == 0x0f3e ) return true;
        
        // #x0f3f
        if ( c == 0x0f3f ) return true;
        
        // [ #x0f71 - #x0f84 ]
        if ( c <  0x0f71 ) return false;
        if ( c <= 0x0f84 ) return true;
        
        // [ #x0f86 - #x0f8b ]
        if ( c <  0x0f86 ) return false;
        if ( c <= 0x0f8b ) return true;
        
        // [ #x0f90 - #x0f95 ]
        if ( c <  0x0f90 ) return false;
        if ( c <= 0x0f95 ) return true;
        
        // #x0f97
        if ( c == 0x0f97 ) return true;
        
        // [ #x0f99 - #x0fad ]
        if ( c <  0x0f99 ) return false;
        if ( c <= 0x0fad ) return true;
        
        // [ #x0fb1 - #x0fb7 ]
        if ( c <  0x0fb1 ) return false;
        if ( c <= 0x0fb7 ) return true;
        
        // #x0fb9
        if ( c == 0x0fb9 ) return true;
        
        // [ #x20d0 - #x20dc ]
        if ( c <  0x20d0 ) return false;
        if ( c <= 0x20dc ) return true;
        
        // #x20e1
        if ( c == 0x20e1 ) return true;
        
        // [ #x302a - #x302f ]
        if ( c <  0x302a ) return false;
        if ( c <= 0x302f ) return true;
        
        // #x3099
        if ( c == 0x3099 ) return true;
        
        // #x309a
        if ( c == 0x309a ) return true;
        
        return false;
    }
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Digit
     */
    public static boolean isXMLDigit (int c) {
        // [ #x0030 - #x0039 ]
        if ( c <  0x0030 ) return false;
        if ( c <= 0x0039 ) return true;
        
        // [ #x0660 - #x0669 ]
        if ( c <  0x0660 ) return false;
        if ( c <= 0x0669 ) return true;
        
        // [ #x06f0 - #x06f9 ]
        if ( c <  0x06f0 ) return false;
        if ( c <= 0x06f9 ) return true;
        
        // [ #x0966 - #x096f ]
        if ( c <  0x0966 ) return false;
        if ( c <= 0x096f ) return true;
        
        // [ #x09e6 - #x09ef ]
        if ( c <  0x09e6 ) return false;
        if ( c <= 0x09ef ) return true;
        
        // [ #x0a66 - #x0a6f ]
        if ( c <  0x0a66 ) return false;
        if ( c <= 0x0a6f ) return true;
        
        // [ #x0ae6 - #x0aef ]
        if ( c <  0x0ae6 ) return false;
        if ( c <= 0x0aef ) return true;
        
        // [ #x0b66 - #x0b6f ]
        if ( c <  0x0b66 ) return false;
        if ( c <= 0x0b6f ) return true;
        
        // [ #x0be7 - #x0bef ]
        if ( c <  0x0be7 ) return false;
        if ( c <= 0x0bef ) return true;
        
        // [ #x0c66 - #x0c6f ]
        if ( c <  0x0c66 ) return false;
        if ( c <= 0x0c6f ) return true;
        
        // [ #x0ce6 - #x0cef ]
        if ( c <  0x0ce6 ) return false;
        if ( c <= 0x0cef ) return true;
        
        // [ #x0d66 - #x0d6f ]
        if ( c <  0x0d66 ) return false;
        if ( c <= 0x0d6f ) return true;
        
        // [ #x0e50 - #x0e59 ]
        if ( c <  0x0e50 ) return false;
        if ( c <= 0x0e59 ) return true;
        
        // [ #x0ed0 - #x0ed9 ]
        if ( c <  0x0ed0 ) return false;
        if ( c <= 0x0ed9 ) return true;
        
        // [ #x0f20 - #x0f29 ]
        if ( c <  0x0f20 ) return false;
        if ( c <= 0x0f29 ) return true;
        
        return false;
    }
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-Extender
     */
    public static boolean isXMLExtender (int c) {
        // #x00b7
        if ( c == 0x00b7 ) return true;
        
        // #x02d0
        if ( c == 0x02d0 ) return true;
        
        // #x02d1
        if ( c == 0x02d1 ) return true;
        
        // #x0387
        if ( c == 0x0387 ) return true;
        
        // #x0640
        if ( c == 0x0640 ) return true;
        
        // #x0e46
        if ( c == 0x0e46 ) return true;
        
        // #x0ec6
        if ( c == 0x0ec6 ) return true;
        
        // #x3005
        if ( c == 0x3005 ) return true;
        
        // [ #x3031 - #x3035 ]
        if ( c <  0x3031 ) return false;
        if ( c <= 0x3035 ) return true;
        
        // [ #x309d - #x309e ]
        if ( c <  0x309d ) return false;
        if ( c <= 0x309e ) return true;
        
        // [ #x30fc - #x30fe ]
        if ( c <  0x30fc ) return false;
        if ( c <= 0x30fe ) return true;
        
        return false;
    }
    
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml-names/#NT-NCNameChar
     */
    public static boolean isXMLNCNameChar (int c) {
        return ( ( isXMLLetter (c) )
        ||  ( isXMLDigit (c) )
        ||  ( c == '.' )
        ||  ( c == '-' )
        ||  ( c == '_' )
        ||  ( isXMLCombiningChar (c) )
        ||  ( isXMLExtender (c) ) );
    }
    
    /**
     * @see http://www.w3.org/TR/REC-xml-names/#NT-NCName
     */
    public static boolean isXMLNCNameStartChar (int c) {
        return ( ( isXMLLetter (c) )
        || ( c == '_' ) );
    }
    
    
    /**
     * @see http://www.w3.org/TR/REC-xml#NT-PubidLiteral
     */
    public static boolean isXMLPubidLiteral (char c) {
        //         System.out.println ("[UnicodeClasses.isXMLPubidLiteral] '" + c + "' = 0x" +
        //                             Integer.toHexString (c));
        
        // #x0020
        if ( c == ' ' ) return true;
        
        // #x000d
        if ( c == '\r' ) return true;
        
        // #x000a
        if ( c == '\n' ) return true;
        
        // -
        if ( c == '-' ) return true;
        
        // '
        if ( c == '\'' ) return true;
        
        // (
        if ( c == '(' ) return true;
        
        // )
        if ( c == ')' ) return true;
        
        // +
        if ( c == '+' ) return true;
        
        // ,
        if ( c == ',' ) return true;
        
        // .
        if ( c == '.' ) return true;
        
        // /
        if ( c == '/' ) return true;
        
        // :
        if ( c == ':' ) return true;
        
        // =
        if ( c == '=' ) return true;
        
        // ?
        if ( c == '?' ) return true;
        
        // ;
        if ( c == ';' ) return true;
        
        // !
        if ( c == '!' ) return true;
        
        // *
        if ( c == '*' ) return true;
        
        // #
        if ( c == '#' ) return true;
        
        // @
        if ( c == '@' ) return true;
        
        // $
        if ( c == '$' ) return true;
        
        // _
        if ( c == '_' ) return true;
        
        // %
        if ( c == '%' ) return true;
        
        // [ 0 - 9 ]
        if ( c <  '0' ) return false;
        if ( c <= '9' ) return true;
        
        // [ A - Z ]
        if ( c <  'A' ) return false;
        if ( c <= 'Z' ) return true;
        
        // [ a - z ]
        if ( c <  'a' ) return false;
        if ( c <= 'z' ) return true;
        
        return false;
    }
    
}
