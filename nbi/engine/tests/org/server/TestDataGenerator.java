/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.*;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.exceptions.UnexpectedExceptionError;

/**
 *
 * @author Danila_Dugurov
 */
public class TestDataGenerator {
  
  public static final int K_BYTE = 1024;
  public static final int M_BYTE = 1024 * K_BYTE;
  
  File dirToGenerate;
  
  public static final String[] testFiles = new String[] {
    "smallest.data",
    "small.data",
    "smallaverage.data",
    "bigaverage.data",
    "big.data",
    "bigest.data"};
  
  public static final int[] testFileSizes = new int[] {
    K_BYTE - 435,
    M_BYTE - 237 * K_BYTE - 1,
    10 * M_BYTE - 139 * K_BYTE - 23,
    50 * M_BYTE - K_BYTE - 758,
    100 * M_BYTE - 3,
    200 * M_BYTE
  };
  
  public static final byte MAGIC_BYTE = (byte) 201;
  
  public static final byte[] buffer = new byte[4096];
  
  static {
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = MAGIC_BYTE;
    }
  }
  
  public static final URL[] testUrls = new URL[testFiles.length];
  
  static {
    try {
      for (int i = 0 ; i < testUrls.length; i++) {
        testUrls[i] = new URL("http://localhost:" + WithServerTestCase.PORT + "/" + testFiles[i]);
      }
    } catch (MalformedURLException mustNotHappend) {
      throw new UnexpectedExceptionError("wrong urls!", mustNotHappend);
    }
  }
  
  public TestDataGenerator(String dirToGenerate) {
    this.dirToGenerate = new File(dirToGenerate);
    this.dirToGenerate.mkdirs();
  }
  
  public void generateTestData() throws IOException {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      if (file.exists()) continue;
      fillWithMagicBytes(file, index);
    }
  }
  
  private void fillWithMagicBytes(File testFile, int index) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(testFile));
    int alreadyWritten = 0;
    while (alreadyWritten < testFileSizes[index]) {
      int writeCount = alreadyWritten + buffer.length <= testFileSizes[index] ? buffer.length : testFileSizes[index] - alreadyWritten;
      out.write(buffer, 0, writeCount);
      alreadyWritten += writeCount;
    }
    out.flush();
    out.close();
  }
  
  public void deleteTestData() {
    for (int index = 0; index < testFiles.length; index++) {
      final File file = new File(dirToGenerate, testFiles[index]);
      file.delete();
    }
  }
}
