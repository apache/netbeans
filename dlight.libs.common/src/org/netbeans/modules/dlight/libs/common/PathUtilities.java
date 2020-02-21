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

package org.netbeans.modules.dlight.libs.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import sun.nio.cs.ThreadLocalCoders;

/**
 *
 */
public class PathUtilities {

    private PathUtilities() {
    }
    
    /** Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static String getDirName(String path) {
        if (path == null) {
            return null;
        }        
        path = trimRightSlashes(path);
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return trimRightSlashes(path.substring(0, sep));
        }
        return null;
    }

    private static String trimRightSlashes(String path) {
        int length = path.length();
        while (length > 0 && (path.charAt(length-1) == '\\' || path.charAt(length-1) == '/')) {
            path = path.substring(0,length-1);
            length = path.length();
            break;
        }
        return path;
    }
        
    /** Same as the C library basename function: given a path, return
     * its filename.
     */
    public static String getBaseName(String path) {
        if (path == null) {
            return null; // making it consistent with getDirName
        }
        if (path.length()>0 && (path.charAt(path.length()-1) == '\\' || path.charAt(path.length()-1) == '/')) {
            path = path.substring(0,path.length()-1);
        }
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(sep + 1);
        }
        return path;
    }        

    /**
     * Normalizes a Unix path, not necessarily absolute
     */
    public static String normalizeUnixPath(String absPath) {
        String norm = normalize(absPath);
        if (norm.startsWith("/../")) { //NOI18N
            int pos = norm.lastIndexOf("/../"); //NOI18N
            // the path normalize returns can only start with several "/../" , 
            // (it is when there are more "/../" segments than path nesting level)
            // but can not contain it in the middle
            if (norm.endsWith("/")) { // NOI18N
                norm = norm.substring(pos + 3, norm.length() - 1);
            } else {
                norm = norm.substring(pos + 3);
            }
        } else if (norm.endsWith("/")) { // NOI18N
            norm = norm.substring(0, norm.length() - 1);
        }
        return norm;
    }
   
    
    // Normalize the given path string.  A normal path string has no empty
    // segments (i.e., occurrences of "//"), no segments equal to ".", and no
    // segments equal to ".." that are preceded by a segment not equal to "..".
    // In contrast to Unix-style pathname normalization, for URI paths we
    // always retain trailing slashes.
    //
    private static String normalize(String ps) {

	// Does this path need normalization?
	int ns = needsNormalization(ps);	// Number of segments
	if (ns < 0)
	    // Nope -- just return it
	    return ps;

	char[] path = ps.toCharArray();		// Path in char-array form

	// Split path into segments
	int[] segs = new int[ns];		// Segment-index array
	split(path, segs);

	// Remove dots
	removeDots(path, segs);

	// Prevent scheme-name confusion
	maybeAddLeadingDot(path, segs);

	// Join the remaining segments and return the result
	String s = new String(path, 0, join(path, segs));
	if (s.equals(ps)) {
	    // string was already normalized
	    return ps;
	}
	return s;
    }
    
    
    // Join the segments in the given path according to the given segment-index
    // array, ignoring those segments whose index entries have been set to -1,
    // and inserting slashes as needed.  Return the length of the resulting
    // path.
    //
    // Preconditions:
    //   segs[i] == -1 implies segment i is to be ignored
    //   path computed by split, as above, with '\0' having replaced '/'
    //
    // Postconditions:
    //   path[0] .. path[return value] == Resulting path
    //
    static private int join(char[] path, int[] segs) {
	int ns = segs.length;		// Number of segments
	int end = path.length - 1;	// Index of last char in path
	int p = 0;			// Index of next path char to write

	if (path[p] == '\0') {
	    // Restore initial slash for absolute paths
	    path[p++] = '/';
	}

	for (int i = 0; i < ns; i++) {
	    int q = segs[i];		// Current segment
	    if (q == -1)
		// Ignore this segment
		continue;

	    if (p == q) {
		// We're already at this segment, so just skip to its end
		while ((p <= end) && (path[p] != '\0'))
		    p++;
		if (p <= end) {
		    // Preserve trailing slash
		    path[p++] = '/';
		}
	    } else if (p < q) {
		// Copy q down to p
		while ((q <= end) && (path[q] != '\0'))
		    path[p++] = path[q++];
		if (q <= end) {
		    // Preserve trailing slash
		    path[p++] = '/';
		}
	    } else
		throw new InternalError(); // ASSERT false
	}

	return p;
    }

    // DEVIATION: If the normalized path is relative, and if the first
    // segment could be parsed as a scheme name, then prepend a "." segment
    //
    private static void maybeAddLeadingDot(char[] path, int[] segs) {

	if (path[0] == '\0')
	    // The path is absolute
	    return;

	int ns = segs.length;
	int f = 0;			// Index of first segment
	while (f < ns) {
	    if (segs[f] >= 0)
		break;
	    f++;
	}
	if ((f >= ns) || (f == 0))
	    // The path is empty, or else the original first segment survived,
	    // in which case we already know that no leading "." is needed
	    return;

	int p = segs[f];
	while ((p < path.length) && (path[p] != ':') && (path[p] != '\0')) p++;
	if (p >= path.length || path[p] == '\0')
	    // No colon in first segment, so no "." needed
	    return;

	// At this point we know that the first segment is unused,
	// hence we can insert a "." segment at that position
	path[0] = '.';
	path[1] = '\0';
	segs[0] = 0;
    }

    // Remove "." segments from the given path, and remove segment pairs
    // consisting of a non-".." segment followed by a ".." segment.
    //
    private static void removeDots(char[] path, int[] segs) {
	int ns = segs.length;
	int end = path.length - 1;

	for (int i = 0; i < ns; i++) {
	    int dots = 0;		// Number of dots found (0, 1, or 2)

	    // Find next occurrence of "." or ".."
	    do {
		int p = segs[i];
		if (path[p] == '.') {
		    if (p == end) {
			dots = 1;
			break;
		    } else if (path[p + 1] == '\0') {
			dots = 1;
			break;
		    } else if ((path[p + 1] == '.')
			       && ((p + 1 == end)
				   || (path[p + 2] == '\0'))) {
			dots = 2;
			break;
		    }
		}
		i++;
	    } while (i < ns);
	    if ((i > ns) || (dots == 0))
		break;

	    if (dots == 1) {
		// Remove this occurrence of "."
		segs[i] = -1;
	    } else {
		// If there is a preceding non-".." segment, remove both that
		// segment and this occurrence of ".."; otherwise, leave this
		// ".." segment as-is.
		int j;
		for (j = i - 1; j >= 0; j--) {
		    if (segs[j] != -1) break;
		}
		if (j >= 0) {
		    int q = segs[j];
		    if (!((path[q] == '.')
			  && (path[q + 1] == '.')
			  && (path[q + 2] == '\0'))) {
			segs[i] = -1;
			segs[j] = -1;
		    }
		}
	    }
	}
    }

    
    // Split the given path into segments, replacing slashes with nulls and
    // filling in the given segment-index array.
    //
    // Preconditions:
    //   segs.length == Number of segments in path
    //
    // Postconditions:
    //   All slashes in path replaced by '\0'
    //   segs[i] == Index of first char in segment i (0 <= i < segs.length)
    //
    static private void split(char[] path, int[] segs) {
	int end = path.length - 1;	// Index of last char in path
	int p = 0;			// Index of next char in path
	int i = 0;			// Index of current segment

	// Skip initial slashes
	while (p <= end) {
	    if (path[p] != '/') break;
	    path[p] = '\0';
	    p++;
	}

	while (p <= end) {

	    // Note start of segment
	    segs[i++] = p++;

	    // Find beginning of next segment
	    while (p <= end) {
		if (path[p++] != '/')
		    continue;
		path[p - 1] = '\0';

		// Skip redundant slashes
		while (p <= end) {
		    if (path[p] != '/') break;
		    path[p++] = '\0';
		}
		break;
	    }
	}

	if (i != segs.length)
	    throw new InternalError();	// ASSERT
    }
    
    
    // -- Path normalization --

    // The following algorithm for path normalization avoids the creation of a
    // string object for each segment, as well as the use of a string buffer to
    // compute the final result, by using a single char array and editing it in
    // place.  The array is first split into segments, replacing each slash
    // with '\0' and creating a segment-index array, each element of which is
    // the index of the first char in the corresponding segment.  We then walk
    // through both arrays, removing ".", "..", and other segments as necessary
    // by setting their entries in the index array to -1.  Finally, the two
    // arrays are used to rejoin the segments and compute the final result.
    //
    // This code is based upon src/solaris/native/java/io/canonicalize_md.c


    // Check the given path to see if it might need normalization.  A path
    // might need normalization if it contains duplicate slashes, a "."
    // segment, or a ".." segment.  Return -1 if no further normalization is
    // possible, otherwise return the number of segments found.
    //
    // This method takes a string argument rather than a char array so that
    // this test can be performed without invoking path.toCharArray().
    //
    static private int needsNormalization(String path) {
	boolean normal = true;
	int ns = 0;			// Number of segments
	int end = path.length() - 1;	// Index of last char in path
	int p = 0;			// Index of next char in path

	// Skip initial slashes
	while (p <= end) {
	    if (path.charAt(p) != '/') break;
	    p++;
	}
	if (p > 1) normal = false;

	// Scan segments
	while (p <= end) {

	    // Looking at "." or ".." ?
	    if ((path.charAt(p) == '.')
		&& ((p == end)
		    || ((path.charAt(p + 1) == '/')
			|| ((path.charAt(p + 1) == '.')
			    && ((p + 1 == end)
				|| (path.charAt(p + 2) == '/')))))) {
		normal = false;
	    }
	    ns++;

	    // Find beginning of next segment
	    while (p <= end) {
		if (path.charAt(p++) != '/')
		    continue;

		// Skip redundant slashes
		while (p <= end) {
		    if (path.charAt(p) != '/') break;
		    normal = false;
		    p++;
		}

		break;
	    }
	}

	return normal ? -1 : ns;
    }
    
    public static String unescapePath(String s) {
        if (s == null) {
            return s;
        }
        int n = s.length();
        if (n == 0) {
            return s;
        }
        if (s.indexOf('%') < 0) {
            return s;
        }

        StringBuffer sb = new StringBuffer(n);
        ByteBuffer bb = ByteBuffer.allocate(n);
        CharBuffer cb = CharBuffer.allocate(n);
        CharsetDecoder dec = ThreadLocalCoders.decoderFor("UTF-8") // NOI18N
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);

        // This is not horribly efficient, but it will do for now
        char c = s.charAt(0);
        boolean betweenBrackets = false;

        for (int i = 0; i < n;) {
            assert c == s.charAt(i);    // Loop invariant
            if (c == '[') {
                betweenBrackets = true;
            } else if (betweenBrackets && c == ']') {
                betweenBrackets = false;
            }
            if (c != '%' || betweenBrackets) {
                sb.append(c);
                if (++i >= n) {
                    break;
                }
                c = s.charAt(i);
                continue;
            }
            bb.clear();
            int ui = i;
            for (;;) {
                assert (n - i >= 2);
                bb.put(decode(s.charAt(++i), s.charAt(++i)));
                if (++i >= n) {
                    break;
                }
                c = s.charAt(i);
                if (c != '%') {
                    break;
                }
            }
            bb.flip();
            cb.clear();
            dec.reset();
            CoderResult cr = dec.decode(bb, cb, true);
            assert cr.isUnderflow();
            cr = dec.flush(cb);
            assert cr.isUnderflow();
            sb.append(cb.flip().toString());
        }
        return sb.toString();        
    }
    
    private static byte decode(char c1, char c2) {
        return (byte)(  ((decode(c1) & 0xf) << 4)
                      | ((decode(c2) & 0xf) << 0));
    }
    
    private static int decode(char c) {
        if ((c >= '0') && (c <= '9'))
            return c - '0';
        if ((c >= 'a') && (c <= 'f'))
            return c - 'a' + 10;
        if ((c >= 'A') && (c <= 'F'))
            return c - 'A' + 10;
        assert false;
        return -1;
    }

    public static String escapeHostOrUserForUseInURL(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (needToEscape(path.charAt(i))) {
                return escapeImpl(path, i).toString();
            }            
        }
        return path;
    }

    public static String escapePathForUseInURL(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (needToEscape(path.charAt(i))) {
                return escapeImpl(path, i).toString();
            }            
        }
        return path;        
    }
    private final static char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    
    private static void appendEscaped(StringBuilder sb, char c) {
        byte b = (byte) c;
        sb.append('%');
        sb.append(hexDigits[(b >> 4) & 0x0f]);
        sb.append(hexDigits[(b >> 0) & 0x0f]);
    }
    
    private static CharSequence escapeImpl(String path, int firstSpecial) {
        StringBuilder sb = (firstSpecial == 0) ? 
                new StringBuilder() : 
                new StringBuilder(path.subSequence(0, firstSpecial));
        for (int i = firstSpecial; i < path.length(); i++) {
            appendEscaped(sb, path.charAt(i));
        }
        return sb;
    }

    /**
     * Determines whether we need to escape the given character
     * See section 2.2. and below at  http://www.ietf.org/rfc/rfc2396.txt
     * reserved    = ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
     * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
     * delims      = "<" | ">" | "#" | "%" | <">
     * unwise      = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"
     * control     = <US-ASCII coded characters 00-1F and 7F hexadecimal>
     * space       = <US-ASCII coded character 20 hexadecimal>
     **/
    
    //<editor-fold defaultstate="collapsed" desc="ascii table to bolean">
    private static final boolean[] charsToEscape = new boolean[] {
        // Char  Dec  Oct  Hex
        // -------------------
        true,  // (nul)   0 0000 0x00
        true,  // (soh)   1 0001 0x01
        true,  // (stx)   2 0002 0x02
        true,  // (etx)   3 0003 0x03
        true,  // (eot)   4 0004 0x04
        true,  // (enq)   5 0005 0x05
        true,  // (ack)   6 0006 0x06
        true,  // (bel)   7 0007 0x07
        true,  // (bs)    8 0010 0x08
        true,  // (ht)    9 0011 0x09
        true,  // (nl)   10 0012 0x0a
        true,  // (vt)   11 0013 0x0b
        true,  // (np)   12 0014 0x0c
        true,  // (cr)   13 0015 0x0d
        true,  // (so)   14 0016 0x0e
        true,  // (si)   15 0017 0x0f
        true,  // (dle)  16 0020 0x10
        true,  // (dc1)  17 0021 0x11
        true,  // (dc2)  18 0022 0x12
        true,  // (dc3)  19 0023 0x13
        true,  // (dc4)  20 0024 0x14
        true,  // (nak)  21 0025 0x15
        true,  // (syn)  22 0026 0x16
        true,  // (etb)  23 0027 0x17
        true,  // (can)  24 0030 0x18
        true,  // (em)   25 0031 0x19
        true,  // (sub)  26 0032 0x1a
        true,  // (esc)  27 0033 0x1b
        true,  // (fs)   28 0034 0x1c
        true,  // (gs)   29 0035 0x1d
        true,  // (rs)   30 0036 0x1e
        true,  // (us)   31 0037 0x1f
        true,  // (sp)   32 0040 0x20
        true,  // !      33 0041 0x21
        true,  // "      34 0042 0x22
        true,  // #      35 0043 0x23
        true,  // $      36 0044 0x24
        true,  // %      37 0045 0x25
        true,  // &      38 0046 0x26
        true,  // '      39 0047 0x27
        true,  // (      40 0050 0x28
        true,  // )      41 0051 0x29
        true,  // *      42 0052 0x2a
        true,  // +      43 0053 0x2b
        true,  // ,      44 0054 0x2c
        false, // -      45 0055 0x2d // NB: here it differs from URI
        false, // .      46 0056 0x2e
        false, // /      47 0057 0x2f
        false, // 0      48 0060 0x30
        false, // 1      49 0061 0x31
        false, // 2      50 0062 0x32
        false, // 3      51 0063 0x33
        false, // 4      52 0064 0x34
        false, // 5      53 0065 0x35
        false, // 6      54 0066 0x36
        false, // 7      55 0067 0x37
        false, // 8      56 0070 0x38
        false, // 9      57 0071 0x39
        true,  // :      58 0072 0x3a
        true,  // ;      59 0073 0x3b
        true,  // <      60 0074 0x3c
        true,  // =      61 0075 0x3d
        true,  // >      62 0076 0x3e
        true,  // ?      63 0077 0x3f
        true,  // @      64 0100 0x40
        false, // A      65 0101 0x41
        false, // B      66 0102 0x42
        false, // C      67 0103 0x43
        false, // D      68 0104 0x44
        false, // E      69 0105 0x45
        false, // F      70 0106 0x46
        false, // G      71 0107 0x47
        false, // H      72 0110 0x48
        false, // I      73 0111 0x49
        false, // J      74 0112 0x4a
        false, // K      75 0113 0x4b
        false, // L      76 0114 0x4c
        false, // M      77 0115 0x4d
        false, // N      78 0116 0x4e
        false, // O      79 0117 0x4f
        false, // P      80 0120 0x50
        false, // Q      81 0121 0x51
        false, // R      82 0122 0x52
        false, // S      83 0123 0x53
        false, // T      84 0124 0x54
        false, // U      85 0125 0x55
        false, // V      86 0126 0x56
        false, // W      87 0127 0x57
        false, // X      88 0130 0x58
        false, // Y      89 0131 0x59
        false, // Z      90 0132 0x5a
        true,  // [      91 0133 0x5b
        true,  // \      92 0134 0x5c
        true,  // ]      93 0135 0x5d
        true,  // ^      94 0136 0x5e
        false, // _      95 0137 0x5f // NB: here it differs from URI
        true,  // `      96 0140 0x60
        false, // a      97 0141 0x61
        false, // b      98 0142 0x62
        false, // c      99 0143 0x63
        false, // d     100 0144 0x64
        false, // e     101 0145 0x65
        false, // f     102 0146 0x66
        false, // g     103 0147 0x67
        false, // h     104 0150 0x68
        false, // i     105 0151 0x69
        false, // j     106 0152 0x6a
        false, // k     107 0153 0x6b
        false, // l     108 0154 0x6c
        false, // m     109 0155 0x6d
        false, // n     110 0156 0x6e
        false, // o     111 0157 0x6f
        false, // p     112 0160 0x70
        false, // q     113 0161 0x71
        false, // r     114 0162 0x72
        false, // s     115 0163 0x73
        false, // t     116 0164 0x74
        false, // u     117 0165 0x75
        false, // v     118 0166 0x76
        false, // w     119 0167 0x77
        false, // x     120 0170 0x78
        false, // y     121 0171 0x79
        false, // z     122 0172 0x7a
        true,  // {     123 0173 0x7b
        true,  // |     124 0174 0x7c
        true,  // }     125 0175 0x7d
        true,  // ~     126 0176 0x7e
        true  // (del) 127 0177 0x7f
    };
    //</editor-fold>
    
    private static boolean needToEscape(char c) {
        if (c < charsToEscape.length) {
            return charsToEscape[c];
        }
        return false;
    }
    
}
