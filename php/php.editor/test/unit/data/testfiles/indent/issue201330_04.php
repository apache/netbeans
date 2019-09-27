<?php

class ClassName {

    public function createComponentIdeClustersResults() {
        return GeneralResults::create()
                ->setTitle("Summary")
                ->setData(self::getModel()->perIdeClusters($this->release))^;
    }

}