package org.netbeans.api.java.source.ui;

import java.util.List;

class MarkUpTag {

    private String tagName;
    private List<MarkUpTagAttribute> markUpTagAttributes;
    private boolean isTagApplicableToNextLine;


    public MarkUpTag(String tagName, List<MarkUpTagAttribute> markUpTagAttributes, boolean isTagApplicableToNextLine) {
        this.tagName = tagName;
        this.markUpTagAttributes = markUpTagAttributes;
        this.isTagApplicableToNextLine = isTagApplicableToNextLine;
    }

    public String getTagName() {
        return tagName;
    }

    public List<MarkUpTagAttribute> getMarkUpTagAttributes() {
        return markUpTagAttributes;
    }
    public boolean isTagApplicableToNextLine() {
        return isTagApplicableToNextLine;
    }
}
