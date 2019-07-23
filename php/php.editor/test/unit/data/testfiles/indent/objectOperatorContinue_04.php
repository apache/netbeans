<?php
    class TestClass {

        public function executeIndex(sfWebRequest $request) {
            $this->news_list = Doctrine::getTable('News')
                    ->createQuery('a')
                    ->where('a.date_deleted is null')
                    ->execute();^
        }
    }
