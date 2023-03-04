<?php

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

// options.put(FmtOptions.BLANK_LINES_BEFORE_CLASS, 2);
// (FmtOptions.BLANK_LINES_AFTER_CLASS = 1);
#[Type1(1)]
class AttributeSyntax1 {
}


#[Type1(1)] // comment
class AttributeSyntax2 {
}



/**
 * comment
 */
#[Type1(1)]
class AttributeSyntax3 {
}


#[Type1(1)]
/**
 * comment1
 */
class AttributeSyntax4 {
}


/**
 * comment1
 */
#[Type1(1)]
/**
 * comment2
 */
class AttributeSyntax5 {
}



#[Type1(1)]
// comment
class AttributeSyntax6 {
}
// comment
#[Type1]
class AttributeSyntax7 {
}


#[Type1(1)]
#[Type1(2)]

class AttributeSyntax8 {
}


#[Type1(1)]

class AttributeSyntax9 extends ParentClass{
}


#[Type1(1)]
class AttributeSyntax10 extends ParentClass implements TestInterface{
}

