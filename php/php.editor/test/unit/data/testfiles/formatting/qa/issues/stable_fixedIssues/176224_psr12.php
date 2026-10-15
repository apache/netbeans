<p>
<?php echo link_to($certificate->getName(), "certficate",
            array(
                "id" => $certificate->getId(),
                "name" => $certificate->getNameSlug()
            ));
            ?>
</p>
