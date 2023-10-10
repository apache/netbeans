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
package test;

public class Test {

    public record R(int ff) {

    }

    /**
     * A simple program.
     *
     * {@link System#out}
     * {@snippet :
     * class HelloWorld {
     *     public static void main(String... args) {
     *         System.out.println("Hello World!");      // @highlight substring="println"
     * }
     * }
     * }
     */
    private static void method(R r) {
        int i = r.ff();
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *       for (var arg : args) {                 // @highlight substring = "args"
     *           if (!arg.isBlankarg()) {           // @highlight substring = "arg"
     *               System.out.println(arg);       // @highlight substring = "arg"  @highlight substring = "println"
     * }
     * }
     * }
     * }
     */
    public void highlightUsingSubstring() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args1) {
     *       for (var arg : args1) {                 // @highlight regex="[0-9]+"
     *           if (!arg.isBlankarg()) {           // @highlight regex = "\barg\b"
     *               System.out.println(args1);       // @highlight regex = "out"
     * }
     * }
     * }
     * }
     */
    public void highlightUsingRegex() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args1) {
     *       for (var arg : args1) {                 // @highlight regex="[0-9]+" @highlight substring="var"
     *           if (!arg.isBlankarg()) {           // @highlight regex = "\barg\b" @highlight substring = "()"
     *               System.out.println("outs");       // @highlight regex = "\bout\b" @highlight substring="System"
     * }
     * }
     * }
     * }
     */
    public void highlightUsingSubstringAndRegex() {
    }

    /**
     * {@snippet :
     *   public static void main(String... substring) {
     *       for (var regex : substring) {
     *           if (!arg.isBlank()) {
     *                System.out.println("italic");     // @highlight substring="italic" type="italic"
     *                System.out.println("bold");       // @highlight substring="bold"
     *                System.out.println("highlight");  // @highlight substring="highlight" type="highlighted"
     * // @highlight substring="italic" type="italic" @highlight substring="bold"  @highlight substring="highlight" type="highlighted":
     *                System.out.println("italic and highlight and bold");
     * // @highlight substring="italic-cum-highlight-cum-bold" @highlight substring="italic-cum-highlight-cum-bold" type="italic" @highlight substring="italic-cum-highlight-cum-bold" type="highlighted":
     *                System.out.println("italic-cum-highlight-cum-bold");
     *                System.out.println("Highlight all "+ substring + ":" + "subsubstringstring");// @highlight substring="substring"
     *
     *                System.out.println("no mark up tag");//to-do
     * // @highlight substring="substring" type = "italic"  @highlight substring="substring" type = "highlighted"  @highlight substring="substring" :
     *                System.out.println("Highlight/bold/italic all "+ substring + ":" + "subsubstringstring");
     *                System.out.println("Highlight all regular exp :regex:"+ regex + " :" + "regexregex" +" regex ");//@highlight regex = "\bregex\b"
     * // @highlight regex = "\bregex\b" @highlight regex = "\bregex\b" type="italic" @highlight regex = "\bregex\b" type="highlighted":
     *                System.out.println("Highlight/bold/italic all regular exp :regex:"+ regex + ":" + "regexregex"+" regex ");
     * }
     * }
     * }
     * }
     */
    public void highlightUsingSubstringRegexAndType() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *       for (var arg : args) {                 // @highlight region substring = "arg"
     *           if (!arg.isBlankarg()) {
     *               System.out.println(arg);
     *           }
     *       }                                      // @end
     *   }
     *   }
     *  {@snippet :
     *   public static void main(String... args) {
     *       for (var arg : args) {                 // @highlight region regex = "\barg\b"
     *           if (!arg.isBlankarg()) {
     *               System.out.println(arg);
     *           }
     *       }                                      // @end
     * }
     * }
     */
    public void highlightUsingMultipleSnippetTagInOneJavaDocWithRegion() {
    }

    /**
     * {@snippet :
     *   public static void \bmain\b(String... args) {// @highlight regex = "\bmain\b"
     * //@highlight substring="str" region = rg1 @highlight region=here substring = "arg" @highlight type="italic" substring="arg":
     *       for (var arg : args) {
     *           if (!arg.isBlargk()) {
     *               System.arg.println("arg");// @end region = here
     *                System.arg.println("arg");
     *                System.arg.println("tests");// @highlight substring = "tests" type = "highlighted"
     *               System.out.println("\barg\b"); // @highlight substring = "\barg\b" @end
     *               System.out.println("\barg\b"); // to-do
     * }
     * }
     * }
     * }
     */
    public void highlightUsingNestedRegions() {
    }

    /**
     * {@snippet :
     *   public static void \bmain\b(String... args) {
     *       for (var arg : args) {                         //@highlight substring="arg" region = rg1 type="highlighted":  @highlight substring = "is"::
     *           if (!arg.isBlargk()) {
     *               System.arg.println("arg");
     *                System.arg.println("arg");
     *                System.arg.println("tests");
     *                System.out.println("\barg\b");         // @highlight substring = "\barg\b" @end
     *               System.out.println("\barg\b");         // to-do
     * }
     * }
     * }
     * }
     */
    public void highlightUsingRegionsEndedWithDoubleColon() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *     System.out.println("args"); // highligh substring = "args"
     * }
     * }
     */
    public void noMarkupTagPresent() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *      // @highlight substring = "args" :
     *     System.out.println("args"); // to-do args
     * }
     * }
     */
    public void highlightTagSubstringApplyToNextLine() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *      // @highlight regex = ".*" :
     *     System.out.println("args"); // to-do args
     * }
     * }
     */
    public void highlightTagRegexWithAllCharacterChange() {
    }

    /**
     * {@snippet :
     *   public static void main(String... args) {
     *      // @highlight regex = "." :
     *     System.out.println("args"); // to-do args
     * }
     * }
     */
    public void highlightTagRegexWithAllCharacterChangeUsingDot() {
    }

}
