<?php

class ClassName {

    public function createComponentIdeClustersResults() {
        return GeneralResults::create()
                ->setTitle("Summary")
                ->setData($this->getModel()->perIdeClusters($this->release))^;
    }

}