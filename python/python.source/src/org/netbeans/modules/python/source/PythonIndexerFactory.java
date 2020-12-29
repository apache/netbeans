/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.source;

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

public class PythonIndexerFactory extends EmbeddingIndexerFactory {    

    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        if(PythonIndexer.isIndexable(indexable, snapshot)) {
            return new PythonIndexer();
        }
        return null;
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> indexables, Context context) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> arg0, Context arg1) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getIndexerName() {
        return PythonIndexer.NAME;
    }

    @Override
    public int getIndexVersion() {
        return PythonIndexer.VERSION;
    }
}
