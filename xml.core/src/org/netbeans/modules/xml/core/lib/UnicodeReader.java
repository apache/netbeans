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

package org.netbeans.modules.xml.core.lib;

import java.io.*;

/**
 * Generic unicode textreader, which will use BOM mark
 * to identify the encoding to be used. If BOM is not found
 * then use a given default or system encoding.
 */
public class UnicodeReader extends Reader {
   PushbackInputStream internalIn;
   InputStreamReader   internalIn2;
   String              defaultEncoding;

   private static final int BOM_SIZE = 4;

   /**
    *
    * @param in  inputstream to be read
    * @param defaultEnc default encoding if stream does not have 
    *                   BOM marker. Give NULL to use system-level default.
    */
   public UnicodeReader(InputStream in, String encoding) {
      internalIn = new PushbackInputStream(in, BOM_SIZE);
      this.defaultEncoding = encoding;
   }

   public String getDefaultEncoding() {
      return defaultEncoding;
   }

   /**
    * Get stream encoding or NULL if stream is uninitialized.
    * Call init() or read() method to initialize it.
    */
   public String getEncoding() {
      if (internalIn2 == null) return null;
      return internalIn2.getEncoding();
   }

   /**
    * Read-ahead four bytes and check for BOM marks. Extra bytes are
    * unread back to the stream, only BOM bytes are skipped.
    */
   protected void init() throws IOException {
      if (internalIn2 != null) return;

      String encoding;
      byte bom[] = new byte[BOM_SIZE];
      int n, unread;
      n = internalIn.read(bom, 0, bom.length);

      if ( (bom[0] == (byte)0x00) && (bom[1] == (byte)0x00) &&
                  (bom[2] == (byte)0xFE) && (bom[3] == (byte)0xFF) ) {
         encoding = "UTF-32BE";
         unread = n - 4;
      } else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) &&
                  (bom[2] == (byte)0x00) && (bom[3] == (byte)0x00) ) {
         encoding = "UTF-32LE";
         unread = n - 4;
      } else if (  (bom[0] == (byte)0xEF) && (bom[1] == (byte)0xBB) &&
            (bom[2] == (byte)0xBF) ) {
         encoding = "UTF-8";
         unread = n - 3;
      } else if ( (bom[0] == (byte)0xFE) && (bom[1] == (byte)0xFF) ) {
         encoding = "UTF-16BE";
         unread = n - 2;
      } else if ( (bom[0] == (byte)0xFF) && (bom[1] == (byte)0xFE) ) {
         encoding = "UTF-16LE";
         unread = n - 2;
      } else {
         // Unicode BOM mark not found, unread all bytes
         encoding = defaultEncoding;
         unread = n;
      }    
      //System.out.println("read=" + n + ", unread=" + unread);

      if (unread > 0) internalIn.unread(bom, (n - unread), unread);

      // Use given encoding
      if (encoding == null) {
         internalIn2 = new InputStreamReader(internalIn);
      } else {
         internalIn2 = new InputStreamReader(internalIn, encoding);
      }
   }

   public void close() throws IOException {
      if(internalIn != null) {
          internalIn.close();
          internalIn = null;
      }
      if(internalIn2 != null) {
          internalIn2.close();
          internalIn2 = null;
      }      
   }

   public int read(char[] cbuf, int off, int len) throws IOException {
      init();
      return internalIn2.read(cbuf, off, len);
   }

}
