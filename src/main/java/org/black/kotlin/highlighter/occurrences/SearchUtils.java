package org.black.kotlin.highlighter.occurrences;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexander.Baratynski
 */
public class SearchUtils {

    public static List<SearchFilter> getBeforeResolveFilters() {
        List<SearchFilter> filters = new ArrayList<SearchFilter>();
        filters.add(new NonImportFilter());
        filters.add(new ReferenceFilter());
        
        return filters;
    }
    
    public static List<? extends SearchFilterAfterResolve> getAfterResolveFilters() {
        return Lists.newArrayList(new ResolvedReferenceFilter());
    }
    
}
