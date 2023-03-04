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

package org.netbeans.editor;

/**
* Token-id with the fixed token image. The image text is provided
* in constructor and can be retrieved by <tt>getImage()</tt>.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseImageTokenID extends BaseTokenID implements ImageTokenID {

    private final String image;

    /** Construct new imag-token-id if the name is the same as the image. */
    public BaseImageTokenID(String nameAndImage) {
        this(nameAndImage, nameAndImage);
    }

    public BaseImageTokenID(String name, String image) {
        super(name);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, int numericID) {
        this(nameAndImage, numericID, nameAndImage);
    }

    public BaseImageTokenID(String name, int numericID, String image) {
        super(name, numericID);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, TokenCategory category) {
        this(nameAndImage, category, nameAndImage);
    }

    public BaseImageTokenID(String name, TokenCategory category, String image) {
        super(name, category);
        this.image = image;
    }

    public BaseImageTokenID(String nameAndImage, int numericID, TokenCategory category) {
        this(nameAndImage, numericID, category, nameAndImage);
    }

    public BaseImageTokenID(String name, int numericID, TokenCategory category, String image) {
        super(name, numericID, category);
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String toString() {
        return super.toString() + ", image='" + getImage() + "'"; // NOI18N
    }

}
