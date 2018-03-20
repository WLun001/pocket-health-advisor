<?php 
require_once ("include/braintree_init.php");
require_once 'vendor/braintree/braintree_php/lib/Braintree.php';

/*if(file_exists(__DIR__ . "/../.env")) {
    $dotenv = new Dotenv\Dotenv(__DIR__ . "/../");
    $dotenv->load();
}*/
echo ($clientToken = Braintree_ClientToken::generate());
?>