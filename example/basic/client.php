<?php
/**
 * PHP-Java-Bridge provides a TCP- or UDP-based connection between PHP (Client) and Java (Server)
 * 
 * licensed under the Apache License 2.0:
 * 
 * Permissions:
 *  - Commercial use
 *  - Modification
 *  - Distribution
 *  - Patent use
 *  - Private use
 * 
 * Limitiations:
 *  - Trademark use
 *  - Liability
 *  - Warranty
 * 
 * Conditions:
 *  - License and copyright notice
 *  - State changes
 * 
 * @link License: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/blob/master/LICENSE
 * @link GitHub Repository: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/
 * @link Wiki: https://github.com/Synt4xErr0r4/PHP-Java-Bridge/wiki
 * 
 * @version 1.0
 * @author SyntaxError404, 2020
 */

use phpjava\Bridge;
use phpjava\Packet;

require_once '../../src/php/Bridge.php';

$bridge = new Bridge(BRIDGE_UDP, '192.168.1.115', 8998, true, 'my super secret password');

$bridge->connect();

$request = new Packet(37);

$request->write(DATA_STRING_UTF8, "Hello World!");

$response = $bridge->sendPacket($request);

echo "Answer: " . $response->read(DATA_STRING_UTF8);

$bridge->disconnect();
$response->__destruct();

?>