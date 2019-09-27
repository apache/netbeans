var query = FacetRecord.find({
        type: typePrefix+thisType+"_Type"
    });
    query.where('slideid').in(arr); 