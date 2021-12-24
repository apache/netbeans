package org.netbeans.api.java.source.ui.snippet;

import java.util.List;

public class MarkupTag {

    private String tagName;
    private List<MarkupTagAttribute> markUpTagAttributes;
    private boolean isTagApplicableToNextLine;


    public MarkupTag(String tagName, List<MarkupTagAttribute> markUpTagAttributes, boolean isTagApplicableToNextLine) {
        this.tagName = tagName;
        this.markUpTagAttributes = markUpTagAttributes;
        this.isTagApplicableToNextLine = isTagApplicableToNextLine;
    }

    public String getTagName() {
        return tagName;
    }

    public List<MarkupTagAttribute> getMarkUpTagAttributes() {
        return markUpTagAttributes;
    }
    public boolean isTagApplicableToNextLine() {
        return isTagApplicableToNextLine;
    }
}
