<?php foreach($certificates_list as $certificate): ?>
<div class="certificates_list">
   <p><?php echo link_to($certificate->getName(), "certficate",
           array(
               "id" => $certificate->getId(),
               "name" => $certificate->getNameSlug()
           ));
       ?></p>
</div>
<?php endforeach; ?>