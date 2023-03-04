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
package org.netbeans.modules.php.latte.completion;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public interface LatteDocumentation {

    String getHeader();
    String getContent();

    public static final class DummyDocumentation implements LatteDocumentation {
        private final String itemName;
        private final String content;

        public DummyDocumentation(String itemName, String content) {
            this.itemName = itemName;
            this.content = content;
        }

        @Override
        public String getHeader() {
            return new StringBuilder().append("<h2>").append(itemName).append("</h2>").toString(); //NOI18N
        }

        @Override
        public String getContent() {
            return content;
        }
    }

    public static final class Factory {

        @NbBundle.Messages("NoDoc=No documentation.")
        public static LatteDocumentation createFromBundle(String bundleKey, String itemName) {
            assert bundleKey != null;
            String content;
            try {
                content = NbBundle.getMessage(Factory.class, bundleKey);
            } catch (MissingResourceException ex) {
                content = Bundle.NoDoc();
            }
            return new LatteDocumentation.DummyDocumentation(itemName, content);
        }
    }

}
