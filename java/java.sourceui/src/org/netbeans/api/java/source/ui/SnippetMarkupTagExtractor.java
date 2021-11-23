package org.netbeans.api.java.source.ui;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SnippetMarkupTagExtractor {

    private int bufferPointer;
    private int bufferLength;
    private char currentChar;
    private char[] charBuffer;
    private static char MARKUP_TAG_START_CHAR = '@';

    public static void main(String[] args) {
        System.out.println("---------------------tag with single attribute----------------------");

        String tagLine = "@highlight regex = \"\\barg\\b\"";
        System.out.println(tagLine);
        List<MarkUpTag> extract = new SnippetMarkupTagExtractor().extract(tagLine);
        print(extract);

        System.out.println("---------------------tag with multiple attributes----------------------");

        tagLine = "//@highlight region=here regex = \"\\barg\\b\"";
        System.out.println(tagLine);
        extract = new SnippetMarkupTagExtractor().extract(tagLine);
        print(extract);

        System.out.println("-----------------multiple tags--------------------------");

        tagLine = "//@highlight region=here regex = \"\\barg\\b\" @link substring = println" ;
        System.out.println(tagLine);
        extract = new SnippetMarkupTagExtractor().extract(tagLine);
        print(extract);

        System.out.println("------------------Only Tag End with semi colon-------------------------");

        tagLine = "//@highlight: region=here regex = \"\\barg\\b\" @link substring = println" ;
        System.out.println(tagLine);
        extract = new SnippetMarkupTagExtractor().extract(tagLine);
        print(extract);

        System.out.println("------------------Tag attributes End with semi colon-------------------------");

        tagLine = "//@highlight region=here regex = \"\\barg\\b\": @link substring = println" ;
        System.out.println(tagLine);
        extract = new SnippetMarkupTagExtractor().extract(tagLine);//@highlight region=here regex = "\barg\b"
        print(extract);

        System.out.println("------------------Tag attributes with regex and replacement-------------------------");

        tagLine = "// @replace regex='\".*\"' replacement=\"...\"" ;
        System.out.println(tagLine);
        extract = new SnippetMarkupTagExtractor().extract(tagLine);
        print(extract);

    }



    public static void print(List<MarkUpTag> extract){
        for(MarkUpTag markUpTag : extract) {
            System.out.println("TagName = " + markUpTag.tagName);
            for (MarkUpTagAttribute attribute : markUpTag.markUpTagAttributes) {
                System.out.println("attribute name = " + attribute.getName());
                System.out.println("attribute value = " + attribute.getValue());
            }
        }
    }
    public List<MarkUpTag> extract(String tagLine) {

        int tagLineLength = tagLine.length();
        charBuffer = new char[tagLineLength + 1];
        tagLine.getChars(0, tagLineLength, charBuffer, 0);
        bufferLength = tagLineLength;
        bufferPointer = -1;

        nextChar();
        return extractTag();
    }

    private List<MarkUpTag> extractTag() {
        List<MarkUpTag> markUpTags = new ArrayList<>();
        while (bufferPointer < bufferLength) {
            if (currentChar == MARKUP_TAG_START_CHAR) {
                markUpTags.add(readMarkUpTag());
            } else {
                nextChar();
            }
        }

        return markUpTags;
    }

    private MarkUpTag readMarkUpTag() {
        nextChar();
        int nameBufferPointer = bufferPointer;
        String tagName = readMarkUpTagName();
        skipWhitespace();

        boolean isTagApplicableToNextLine = false;
        Set<MarkUpTagAttribute> markUpTagAttributes = new HashSet<>();

        if (currentChar == ':') {// @highlight: regex = "\barg\b", for this markup tag consider only the tag i.e.
                                // highlight and skipped all attributes
            isTagApplicableToNextLine = true;
            nextChar();
        } else {
            markUpTagAttributes = new HashSet(getAllMarkUpTagAttributes());
            skipWhitespace();
            if (currentChar == ':') {//This colon check is for end of the markup tag attributes
                isTagApplicableToNextLine = true;
                nextChar();
            }
        }

        MarkUpTag i = new MarkUpTag();
        i.nameLineOffset = nameBufferPointer;
        i.tagName = tagName;
        i.markUpTagAttributes = markUpTagAttributes;
        i.isTagApplicableToNextLine = isTagApplicableToNextLine;

        return i;
    }



    private List<MarkUpTagAttribute> getAllMarkUpTagAttributes() {
        List<MarkUpTagAttribute> attrs = new ArrayList<>();
        skipWhitespace();

        while (bufferPointer < bufferLength && Character.isUnicodeIdentifierStart(currentChar)) {
            StringBuilder value = new StringBuilder();
            int nameStartPos = bufferPointer;
            String attributeName = readAttributeName();
            skipWhitespace();
            int valueStartPos = -1;
            // what if value start with ==, instead of = ?, handled
            if (currentChar == '=') {
                nextChar();
                skipWhitespace();
                if (currentChar == '\'' || currentChar == '"') {
                    char charQuote = currentChar;
                    nextChar();
                    valueStartPos = bufferPointer;
                    while (bufferPointer < bufferLength && currentChar != charQuote) {
                        nextChar();
                    }
                    if (bufferPointer >= bufferLength) {
                        return null;//return meaning ful message here
                    }
                    addRemainingText(value, valueStartPos, bufferPointer - 1);
                    nextChar();
                } else {
                    valueStartPos = bufferPointer;
                    while (bufferPointer < bufferLength && !isValueTerminateWithoutQuote(currentChar)) {
                        nextChar();
                    }
                    addRemainingText(value, valueStartPos, bufferPointer - 1);
                }
                skipWhitespace();
            }
            //some attribute doesn't have value e.g. // @highlight region
            MarkUpTagAttribute markUpTagAttribute = new MarkUpTagAttribute(attributeName, nameStartPos, value.toString(), valueStartPos);


            attrs.add(markUpTagAttribute);
        }
        return attrs;
    }


    private void addRemainingText(StringBuilder b, int textStart, int textEnd) {
        if (textStart != -1) {
            if (textStart <= textEnd) {
                b.append(charBuffer, textStart, (textEnd - textStart) + 1);
            }
        }
    }

    private String readAttributeName() {
        return readName();
    }

    private String readMarkUpTagName() {
        return readName();
    }

    private String readName(){
        int start = bufferPointer;
        nextChar();
        while (bufferPointer < bufferLength && (Character.isUnicodeIdentifierPart(currentChar) || currentChar == '-')) {
            nextChar();
        }
        return new String(charBuffer, start, bufferPointer - start);
    }

    private boolean isValueTerminateWithoutQuote(char ch) {
        return ch == ':'
                || ch == '\t'
                || ch == ' '
                || ch == '"'
                || ch == '`'
                || ch == '\''
                || ch == '='
                || ch == '>'
                || ch == '<';
    }

    private void skipWhitespace() {
        while (bufferPointer < bufferLength && Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    private void nextChar() {
        currentChar = charBuffer[bufferPointer < bufferLength ? ++bufferPointer : bufferLength];
    }

}