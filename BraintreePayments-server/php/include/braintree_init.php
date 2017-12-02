<?php
session_start();
require_once("vendor/autoload.php");
if(file_exists(__DIR__ . "/../.env")) {
    $dotenv = new Dotenv\Dotenv(__DIR__ . "/../");
    $dotenv->load();
}
Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('z4vty64pyh76xxp3');
Braintree_Configuration::publicKey('j2k52vjh8bt74rw5');
Braintree_Configuration::privateKey('2cda6f6e484a2a3319c22509a14e6015');
?>
