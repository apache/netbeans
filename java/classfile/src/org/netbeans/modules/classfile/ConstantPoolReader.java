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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.*;

/**
 * A Java class file constant pool reader.  This class is
 * used rather than java.io.DataInputStream as it is
 * optimized for constant pool reading only.
 *
 * @author Thomas Ball
 */
public final class ConstantPoolReader extends FilterInputStream implements DataInput {

    public ConstantPoolReader(InputStream in) {
	super(in);
    }

    public void readFully(byte[] b) throws IOException {
	readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
	InputStream input = in;
	int n = 0;
	while (n < len) {
	    int count = input.read(b, off + n, len - n);
	    if (count < 0)
		throw new EOFException();
	    n += count;
	}
    }

    public int skipBytes(int n) throws IOException {
	InputStream input = in;
	int total = 0;
	int cur = 0;

	while ((total<n) && ((cur = (int) input.skip(n-total)) > 0)) {
	    total += cur;
	}

	return total;
    }

    public boolean readBoolean() throws IOException {
	int ch = in.read();
	if (ch < 0)
	    throw new EOFException();
	return (ch != 0);
    }

    public byte readByte() throws IOException {
	int ch = in.read();
	if (ch < 0)
	    throw new EOFException();
	return (byte)(ch);
    }

    public int readUnsignedByte() throws IOException {
	int ch = in.read();
	if (ch < 0)
	    throw new EOFException();
	return ch;
    }

    public short readShort() throws IOException {
	InputStream input = in;
	int ch1 = input.read();
	int ch2 = input.read();
	if ((ch1 | ch2) < 0)
	     throw new EOFException();
	return (short)((ch1 << 8) + ch2);
    }

    public int readUnsignedShort() throws IOException {
	InputStream input = in;
	int ch1 = input.read();
	int ch2 = input.read();
	if ((ch1 | ch2) < 0)
	     throw new EOFException();
	return (ch1 << 8) + ch2;
    }

    public char readChar() throws IOException {
	return (char)readUnsignedShort();
    }

    public int readInt() throws IOException {
	InputStream input = in;
	int ch1 = input.read();
	int ch2 = input.read();
	int ch3 = input.read();
	int ch4 = input.read();
	if ((ch1 | ch2 | ch3 | ch4) < 0)
	     throw new EOFException();
	return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }

    public long readLong() throws IOException {
	return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public float readFloat() throws IOException {
	return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
	return Double.longBitsToDouble(readLong());
    }

    private char[] lineBuffer;

    public String readLine() throws IOException {
	InputStream in = this.in;
	char buf[] = lineBuffer;

	if (buf == null) {
	    buf = lineBuffer = new char[128];
	}

	int room = buf.length;
	int offset = 0;
	int c;

loop:	while (true) {
	    switch (c = in.read()) {
	      case -1:
	      case '\n':
		break loop;

	      case '\r':
		int c2 = in.read();
		if ((c2 != '\n') && (c2 != -1)) {
		    if (!(in instanceof PushbackInputStream)) {
			in = this.in = new PushbackInputStream(in);
		    }
		    ((PushbackInputStream)in).unread(c2);
		}
		break loop;

	      default:
		if (--room < 0) {
		    buf = new char[offset + 128];
		    room = buf.length - offset - 1;
		    System.arraycopy(lineBuffer, 0, buf, 0, offset);
		    lineBuffer = buf;
		}
		buf[offset++] = (char) c;
		break;
	    }
	}
	if ((c == -1) && (offset == 0)) {
	    return null;
	}
	return String.copyValueOf(buf, 0, offset);
    }

    // NOT threadsafe: for performance
    static char[] str = new char[1024];
    byte[] bytearr = new byte[1024];

    public String readUTF() throws IOException {
        int utflen = readUnsignedShort();
	if (utflen > bytearr.length)
	    bytearr = new byte[utflen];

 	readFully(bytearr, 0, utflen);
	return readUTF(bytearr, utflen);
    }

    byte[] readRawUTF() throws IOException {
        int utflen = readUnsignedShort();
	byte[] buf = new byte[utflen];
	readFully(buf, 0, utflen);
	return buf;
    }

    static synchronized String readUTF(byte[] src, int utflen) {
	int i = 0;
	int strlen = 0;

	if (utflen > str.length)
	    str = new char[utflen];

        while (i < utflen) {
            int b = src[i++] & 0xFF;
            if (b >= 0xE0) {
                b = (b & 0x0F) << 12;
                b |= (src[i++] & 0x3F) << 6;
                b |= (src[i++] & 0x3F);
            } else if (b >= 0xC0) {
                b = (b & 0x1F) << 6;
                b |= (src[i++] & 0x3F);
            }
            str[strlen++] = (char)b;
        }

        // The number of chars produced may be less than utflen
        return new String(str, 0, strlen);
    }
}
