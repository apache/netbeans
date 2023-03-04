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
 * HTMLTextItem.java
 *
 * Created on October 17, 2002, 8:00 PM
 */

package org.netbeans.performance.spi.html;

 /** Wrapper for items containing text.
  * @author Tim Boudreau
  */
public class HTMLTextItem extends AbstractHTML {
    String text;
    public HTMLTextItem(String s) {
        text = s;
    }

    public HTMLTextItem(Object o) {
        text = o.toString();
    }

    public HTMLTextItem(String s, int preferredWidth) {
        super (preferredWidth);
        text = s;
    }

    public void toHTML (StringBuffer sb) {
        sb.append(text + "\n");
    }

}
