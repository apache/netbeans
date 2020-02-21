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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.spi.CndDocumentCodeStyleProvider;

/**
 *
 */
public class CLangFormatTestCase extends EditorBase {

    public CLangFormatTestCase(String testMethodName) {
        super(testMethodName);
    }

  @Override
  protected synchronized BaseDocument getDocument() {
    BaseDocument doc = super.getDocument();
    if (doc.getProperty(CndDocumentCodeStyleProvider.class) == null) {
      CndDocumentCodeStyleProvider provider = new CndDocumentCodeStyleProvider() {
        @Override
        public String getCurrentCodeStyle(String mimeType, Document doc) {
          return "BasedOnStyle: LLVM";
        }
      };
      doc.putProperty(CndDocumentCodeStyleProvider.class, provider);
    }
    return doc;
  }
    
    public void testLLVM_Style() {
        setLoadDocumentText(
                "int main() {\n" 
              + "    B::A<T...>{}.loop(std::forward<F>(body), std::make<sizeof...(T)> {}, std::forward<T>(objects)...);\n" 
              + "    int a{1};\n"
              + "    return {0};\n"
              + "}\n");
        getDocument();
        reformat();
        assertDocumentText("Incorrect rvalue reference",
                "int main() {\n"
              + "  B::A<T...>{}.loop(std::forward<F>(body), std::make<sizeof...(T)>{},\n"
              + "                    std::forward<T>(objects)...);\n"
              + "  int a{1};\n"
              + "  return {0};\n"
              + "}\n");
    }
    
}
