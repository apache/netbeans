<?php

     class ClassName {
               
               public function executeShowCategoryDetail(sfWebRequest $request) {

                         $this->pager = new sfDoctrinePager('ReferencesItems', 10);
                         $this->pager->getQuery()->from('ReferencesItems c')
                                 ->where("c.disabled is null")^
                                 ->andWhere("c.ref_category_id = ?", $this->getRoute()->getObject()->getId())
                                 ->orderBy("priority ASC");
                         $this->pager->setPage($request->getParameter('page', 1));
                         $this->pager->init();
               }
     }

?>