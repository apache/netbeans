package org.netbeans.api.java.source.ui;

import java.util.Objects;

public class MarkUpTagAttribute {

    private final String name;
    private final int nameStartPosition;
    private final String value;
    private final int valueStartPosition;

    public MarkUpTagAttribute(String name, int nameStartPosition, String value, int valueStartPosition) {
        this.name = name;
        this.nameStartPosition = nameStartPosition;
        this.value = value;
        this.valueStartPosition = valueStartPosition;
    }

    public String getName() {
        return name;
    }

    public int getNameStartPosition() {
        return nameStartPosition;
    }

    public String getValue() {
        return value;
    }

    public int getValueStartPosition() {
        return valueStartPosition;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarkUpTagAttribute other = (MarkUpTagAttribute) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
}