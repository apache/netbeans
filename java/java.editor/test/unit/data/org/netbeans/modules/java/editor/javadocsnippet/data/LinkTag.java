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
package test;

import java.util.ArrayList;

/**
 *
 * @author aksinsin
 */
public class Test {

    /**
     * A simple program
     *
     * {@snippet :
     *  private String s;// @link substring="String s" target="String"
     * }
     */
    private String s;

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");  // @link substring="System.out" target="System#out"
     * }
     * }
     * }
     */
    public void testLinkTag() {

    }

    /**
     * A simple program
     *
     * {@snippet :
     *   public static void main(String... args) {
     *       for (var arg : args) {                 // @link region regex = "\barg\b" target="String"
     *           if (!arg.isBlank()) {
     *               System.out.println(arg);
     *           }
     *       }                                      // @end
     * }
     * }
     */
    public void testLinkTag_With_RegexAndRegion() {

    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         // @link substring="System.out" target="System#out":
     *         System.out.println("Hello World!");
     * }
     * }
     * }
     */
    public void testLinkTag_AppliesToNextLine() {

    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println(ArrayList.class); // @link substring="System.out" target="System#out" @link substring="ArrayList.class" target="ArrayList"
     * }
     * }
     * }
     */
    public void testLink_MultipleTag_OnSameLine() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!"); // @link region = rg1 substring="System.out" target="System#out"
     *         System.out.println(ArrayList.class); // @link region = rg2 substring="ArrayList.class" target="ArrayList"
     *         System.out.println("Hello World"); System.out.println(ArrayList.class);
     *         System.err.println("err");
     *         System.err.println(ArrayList.class);// @end region=rg1 @end region=rg2
     * }
     * }
     * }
     */
    public void testLinkTag_With_RegionAttribute() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println(addExact(1,20)); // @link substring="addExact" target="#addExact"
     * }
     * }
     * }
     */
    public void testLinkTag_Ref_ToThisClass_UsingHash() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println(args[0]); // @link substring="args[0]" target="#s"
     * }
     * }
     * }
     */
    public void testLinkTag_FieldRef_ToThisClass_UsingHash() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         // @link substring="System.out" target="System#out": @highlight type="highlighted" substring = "out.println":
     *         System.out.println("Hello World!");
     * }
     * }
     * }
     */
    public void testLinkTag_AlongWith_HighlightTag() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         // @link substring="System.out" target="System#out": @replace substring = "tem.o" replacement="err.o":
     *         System.out.println("Hello World!");
     * }
     * }
     * }
     */
    public void testLinkTag_AlongWith_ReplaceTag() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         // @link substring="System.out" target="System#out": @replace substring = "out" replacement="replacedout" : @highlight type="highlighted" substring="replaced":
     *         System.out.println("Hello World!");
     * }
     * }
     * }
     */
    public void testLinkTag_AlongWith_SubStringAndReplaceTag() {
    }

    /**
     * A simple program
     *
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         // @link substring="System.out" target="System#out": @replace substring = "out" replacement=""
     *         System.out.println("Hello World!");
     * }
     * }
     * }
     */
    public void testLinkTag_EmptyReplacementValue() {
    }

    /**
     * Returns the sum of its arguments, throwing an exception if the result
     * overflows an {@code int}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public int addExact(int x, int y) {
        return x + y;
    }
}
