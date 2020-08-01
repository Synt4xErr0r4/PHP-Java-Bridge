<?php

use phpjava\Bridge;
use phpjava\Packet;

require_once '../../src/php/Bridge.php';

$bridge = new Bridge(BRIDGE_UDP, '192.168.1.115', 8998, true, 'my super secret password');

$bridge->connect();

$request = new Packet(37);

$request->write(DATA_STRING_UTF8, "Hello World!");
$request->write(DATA_INT, 12345);

$response = $bridge->sendPacket($request);

echo "Answer: " . $response->read(DATA_STRING_UTF8);

$bridge->disconnect();

?>