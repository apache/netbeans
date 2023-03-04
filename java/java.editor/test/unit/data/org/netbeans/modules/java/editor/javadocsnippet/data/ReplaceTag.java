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
/**
 *
 * @author aksinsin
 */
public class Test {
     /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");  // @replace regex='".*"' replacement="..."
     * }
     * }
     * }
     */
    public void testSingleLine_Replace_Regex() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");  // @replace regex=".*" replacement="..."
     * }
     * }
     * }
     */
    public void testSingleLine_Replace_RegexDotStar() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");  // @replace regex="." replacement="..."
     * }
     * }
     * }
     */
    public void testSingleLine_Replace_RegexDot() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");  // @replace substring="lo Worl" replacement="-replace-"
     * }
     * }
     * }
     */
    public void testSingleLine_Replace_Substring() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!" + "Good morning");  // @replace regex='".*"' replacement="...replace..." @replace regex='out' replacement="err"
     * }
     * }
     * }
     */
    public void testSingleLine_MultipleReplaceAnnotation_Regex() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!" + "Hello Morning");  // @replace substring="Hello" replacement="Hi"
     *         System.out.println("Hello World!" + "Hello Morning");  // @replace substring="Hello" replacement="Hi" @replace substring='Hi' replacement="Hiiiiii"
     * }
     * }
     * }
     */
    public void testSingleLine_MultipleReplaceAnnotation_Substring() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!" + "Good morning");  // @replace regex='".*"' replacement='"...replace..."'
     * }
     * }
     * }
     */
    public void testSingleLine_ReplaceAnnotation_Regex_DoubleQuote() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {// @replace region regex='".*"' replacement='"...replace..."'
     *         System.out.println("Hello World!" + "Good morning");  //to-do
     *         System.out.println("Hello World!" + "Good morning");  //"to-do"
     *         System.out.println("Hello World!" + "Good morning"); //@end
     * }
     * }
     * }
     */
    public void testRegion_ReplaceAnnotation_Regex() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {// @replace region regex="to-do" replacement='...replace...'
     *         System.out.println("to-do" + "Good morning");  //"to-do"
     *         System.out.println("Hello World!" + "to-do"); //@end
     * }
     * }
     * }
     */
    public void testRegion_ReplaceAnnotation_RegexInnComment() {
    }

    /**
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {// @replace region substring="not-important" replacement="print"
     *         System.out.println("not-important");  // @replace region = "here" substring="out" replacement="err"
     *         System.out.println("not-important" + "to-do");
     *         System.out.println("not-important"); //@end region= "here"
     *         System.out.not-important();//@end
     * }
     * }
     * }
     */
    public void testNestedRegion_ReplaceAnnotation_Substring() {
    }

    /**
     * {@snippet :
     * class HelloWorld1 {//@highlight regex="[0-9]+" type="highlighted"
     *     public static void main(String... args) {// @replace region regex="[0-9]+" replacement="num"
     *         System.out.println(9);  // @replace region = "here" regex="[A-Z]+" replacement="UPPER"
     *         System.out.println(99);
     *         System.out.println("ABC"); //@end region= "here"
     *         System.out.print(3);//@end
     * }
     * }
     * }
     */
    public void testNestedRegion_ReplaceAnnotation_Regex() {
    }

    /**
     * {@snippet :
     *   public static void \bmain\b(String... args) {	// @highlight regex = "\bmain\b"
     *       for (var arg : args) {                         //@highlight substring="str" region = rg1 @highlight region=here substring = "arg" @highlight type="italic" substring="arg"
     *           if (!arg.isBlargk()) {
     *               System.arg.println("arg");		// @end region = here
     *		 System.arg.println("arg");
     *		 System.arg.println("tests");           // @highlight substring = "tests" type = "highlighted" @replace substring="tests" replacement="replace" @replace substring="replace" replacement="tests"
     *		 System.arg.println("tests");           // @highlight substring = "tests" type = "highlighted" @replace substring="tests" replacement="replace" @replace substring="replace" replacement="replace-new"
     *		 System.out.println("\barg\b");         // @highlight substring = "\barg\b" @end
     *		 System.arg.println("tests"); 			// @replace substring="tests" replacement="replace" @highlight substring="replace"
     *		 System.out.println("\barg\b");         // to-do
     *		 System.out.println("argb"); //@highlight substring="arg" @highlight substring="b"
     * }
     * }
     * }
     * }
     */
    public void testNestedRegion_Highlight_And_replace() {
    }

    /**
     * {@snippet :
     *  public static void \bmain\b(String... args) {	// @highlight regex = "\bmain\b"
     *		 System.out.println("tests"); 	// @highlight substring="ystem.out"  @replace substring="tem" replacement="replace"
     * }
     * }
     */
    public void testHighlightAndReplace_cornercase() {
    }
}
