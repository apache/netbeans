package org.black.kotlin.highlighter.netbeans;

import org.black.kotlin.language.KotlinLanguageHierarchy;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 * Custom class for NetBeans {@link TokenId} class.
 * @author Александр
 */
public class KotlinTokenId implements TokenId {

    private final String name;
    private final String primaryCategory;
    private final int id;
    
    /**
     * Constructor.
     * @param name token name.
     * @param primaryCategory token category.
     * @param id token id.
     */
    public KotlinTokenId(String name, String primaryCategory, int id){
        this.name = name;
        this.primaryCategory = primaryCategory;
        this.id = id;
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return id;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static Language<KotlinTokenId> getLanguage(){
        return new KotlinLanguageHierarchy().language();
    }
    
}
