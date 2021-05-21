<?php
     /*^
     class referencesActions extends sfActions {

               /**
                * Executes index action
                *
                * @param sfRequest $request A request object
                */
               public function executeIndex(sfWebRequest $request) {
                         $this->categories_list = Doctrine::getTable("ReferencesCategory")
                                 ->createQuery("c")
                                 ->where("disabled is null")
                                 ->orderBy("priority ASC")
                                 ->execute();
               }
     }

?>