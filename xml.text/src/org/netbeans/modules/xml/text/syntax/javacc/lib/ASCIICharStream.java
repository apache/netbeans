/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.text.syntax.javacc.lib;

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 */

public final class ASCIICharStream implements CharStream
{
    public static final boolean staticFlag = false;
    int bufsize;
    int available;
    int tokenBegin;
    public int bufpos = -1;
    private int bufline[];
    private int bufcolumn[];

    private int column = 0;
    private int line = 1;

    private boolean prevCharIsCR = false;
    private boolean prevCharIsLF = false;

    private java.io.Reader inputStream;

    private char[] buffer;
    private int maxNextCharInd = 0;
    private int inBuf = 0;

    private final void ExpandBuff(boolean wrapAround)
    {
        char[] newbuffer = new char[bufsize + 2048];
        int newbufline[] = new int[bufsize + 2048];
        int newbufcolumn[] = new int[bufsize + 2048];

        try
        {
            if (wrapAround)
            {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                System.arraycopy(buffer, 0, newbuffer,
                                 bufsize - tokenBegin, bufpos);
                buffer = newbuffer;

                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
                bufline = newbufline;

                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
                bufcolumn = newbufcolumn;

                maxNextCharInd = (bufpos += (bufsize - tokenBegin));
            }
            else
            {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                buffer = newbuffer;

                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                bufline = newbufline;

                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                bufcolumn = newbufcolumn;

                maxNextCharInd = (bufpos -= tokenBegin);
            }
        }
        catch (Throwable t)
        {
            throw new Error(t.getMessage());
        }


        bufsize += 2048;
        available = bufsize;
        tokenBegin = 0;
    }

    private final void FillBuff() throws java.io.IOException
    {
        if (maxNextCharInd == available)
        {
            if (available == bufsize)
            {
                if (tokenBegin > 2048)
                {
                    bufpos = maxNextCharInd = 0;
                    available = tokenBegin;
                }
                else if (tokenBegin < 0)
                    bufpos = maxNextCharInd = 0;
                else
                    ExpandBuff(false);
            }
            else if (available > tokenBegin)
                available = bufsize;
            else if ((tokenBegin - available) < 2048)
                ExpandBuff(true);
            else
                available = tokenBegin;
        }

        int i;
        try {
            if ((i = inputStream.read(buffer, maxNextCharInd,
                                      available - maxNextCharInd)) == -1)
            {
                inputStream.close();
                throw new java.io.IOException();
            }
            else
                maxNextCharInd += i;
            return;
        }
        catch(java.io.IOException e) {
            --bufpos;
            backup(0);
            if (tokenBegin == -1)
                tokenBegin = bufpos;
            throw e;
        }
    }

    public final char BeginToken() throws java.io.IOException
    {
        tokenBegin = -1;
        char c = readChar();
        tokenBegin = bufpos;

        return c;
    }

    private final void UpdateLineColumn(char c)
    {
        column++;

        if (prevCharIsLF)
        {
            prevCharIsLF = false;
            line += (column = 1);
        }
        else if (prevCharIsCR)
        {
            prevCharIsCR = false;
            if (c == '\n')
            {
                prevCharIsLF = true;
            }
            else
                line += (column = 1);
        }

        switch (c)
        {
        case '\r' :
            prevCharIsCR = true;
            break;
        case '\n' :
            prevCharIsLF = true;
            break;
        case '\t' :
            column--;
            column += (8 - (column & 07));
            break;
        default :
            break;
        }

        bufline[bufpos] = line;
        bufcolumn[bufpos] = column;
    }

    public final char readChar() throws java.io.IOException
    {
        if (inBuf > 0)
        {
            --inBuf;
            return (char)((char)0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]);
        }

        if (++bufpos >= maxNextCharInd)
            FillBuff();

        char c = (char)((char)0xff & buffer[bufpos]);

        UpdateLineColumn(c);
        return (c);
    }

    /**
     * @deprecated 
     * @see #getEndColumn
     */

    public final int getColumn() {
        return bufcolumn[bufpos];
    }

    /**
     * @deprecated 
     * @see #getEndLine
     */

    public final int getLine() {
        return bufline[bufpos];
    }

    public final int getEndColumn() {
        return bufcolumn[bufpos];
    }

    public final int getEndLine() {
        return bufline[bufpos];
    }

    public final int getBeginColumn() {
        return bufcolumn[tokenBegin];
    }

    public final int getBeginLine() {
        return bufline[tokenBegin];
    }

    public final void backup(int amount) {

        inBuf += amount;
        if ((bufpos -= amount) < 0)
            bufpos += bufsize;
    }

    public ASCIICharStream(java.io.Reader dstream,int startline,int startcolumn,int buffersize)
    {
        inputStream = dstream;
        line = startline;
        column = startcolumn - 1;

        available = bufsize = buffersize;
        buffer = new char[buffersize];
        bufline = new int[buffersize];
        bufcolumn = new int[buffersize];
    }

    public ASCIICharStream(java.io.Reader dstream,int startline,int startcolumn)
    {
        this(dstream, startline, startcolumn, 4096);
    }
    public void ReInit(java.io.Reader dstream, int startline,
                       int startcolumn, int buffersize)
    {
        inputStream = dstream;
        line = startline;
        column = startcolumn - 1;

        if (buffer == null || buffersize != buffer.length)
        {
            available = bufsize = buffersize;
            buffer = new char[buffersize];
            bufline = new int[buffersize];
            bufcolumn = new int[buffersize];
        }
        prevCharIsLF = prevCharIsCR = false;
        tokenBegin = inBuf = maxNextCharInd = 0;
        bufpos = -1;
    }

    public void ReInit(java.io.Reader dstream, int startline,
                       int startcolumn)
    {
        ReInit(dstream, startline, startcolumn, 4096);
    }
    public ASCIICharStream(java.io.InputStream dstream,int startline,int startcolumn,int buffersize)
    {
        this(new java.io.InputStreamReader(dstream), startline, startcolumn, 4096);
    }

    public ASCIICharStream(java.io.InputStream dstream,int startline,int startcolumn)
    {
        this(dstream, startline, startcolumn, 4096);
    }

    public void ReInit(java.io.InputStream dstream, int startline,
                       int startcolumn, int buffersize)
    {
        ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, 4096);
    }
    public void ReInit(java.io.InputStream dstream, int startline,
                       int startcolumn)
    {
        ReInit(dstream, startline, startcolumn, 4096);
    }
    public final String GetImage()
    {
        if (bufpos >= tokenBegin)
            return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
        else
            return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                   new String(buffer, 0, bufpos + 1);
    }

    public final char[] GetSuffix(int len)
    {
        char[] ret = new char[len];

        if ((bufpos + 1) >= len)
            System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
        else
        {
            System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                             len - bufpos - 1);
            System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
        }

        return ret;
    }

    public void Done()
    {
        buffer = null;
        bufline = null;
        bufcolumn = null;
    }

    /**
     * Method to adjust line and column numbers for the start of a token.<BR>
     */
    public void adjustBeginLineColumn(int newLine, int newCol)
    {
        int start = tokenBegin;
        int len;

        if (bufpos >= tokenBegin)
        {
            len = bufpos - tokenBegin + inBuf + 1;
        }
        else
        {
            len = bufsize - tokenBegin + bufpos + 1 + inBuf;
        }

        int i = 0, j = 0, k = 0;
        int nextColDiff = 0, columnDiff = 0;

        while (i < len &&
                bufline[j = start % bufsize] == bufline[k = ++start % bufsize])
        {
            bufline[j] = newLine;
            nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
            bufcolumn[j] = newCol + columnDiff;
            columnDiff = nextColDiff;
            i++;
        }

        if (i < len)
        {
            bufline[j] = newLine++;
            bufcolumn[j] = newCol + columnDiff;

            while (i++ < len)
            {
                if (bufline[j = start % bufsize] != bufline[++start % bufsize])
                    bufline[j] = newLine++;
                else
                    bufline[j] = newLine;
            }
        }

        line = bufline[j];
        column = bufcolumn[j];
    }

}
