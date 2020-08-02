<?php

use phpjava\Bridge;
use phpjava\Packet;

require_once '../src/php/Bridge.php';

$udp = new Bridge(BRIDGE_UDP, '192.168.1.115', 8877, true, 'I like pizza!');

$udp->connect();

$outgoing = new Packet(0x7F);

$outgoing->write(DATA_BYTE, -0x80);
$outgoing->write(DATA_UNSIGNED_BYTE, 0xFF);
$outgoing->write(DATA_SHORT, -0x8000);
$outgoing->write(DATA_UNSIGNED_SHORT, 0xFFFF);
$outgoing->write(DATA_INT, -0x80000000);
$outgoing->write(DATA_UNSIGNED_INT, 0xFFFFFFFF);
$outgoing->write(DATA_LONG, PHP_INT_MIN);
$outgoing->write(DATA_FLOAT, 340282346638528859811704183484516925440);
$outgoing->write(DATA_DOUBLE, PHP_FLOAT_MAX);
$outgoing->write(DATA_STRING_UTF8, "Hello from PHP!");
$outgoing->write(DATA_STRING_ASCII, "I like ascii");
$outgoing->write(DATA_STRING_C, "I like c strings");
$outgoing->write(DATA_BYTE_ARRAY, [0x7F, -0x80, 0, 0xF, -0xF]);

$incoming = $udp->sendPacket($outgoing);

echo $incoming->getPacketID();
echo "<br>";
echo $incoming->read(DATA_BYTE) . "<br>";
echo $incoming->read(DATA_UNSIGNED_BYTE) . "<br>";
echo $incoming->read(DATA_SHORT) . "<br>";
echo $incoming->read(DATA_UNSIGNED_SHORT) . "<br>";
echo $incoming->read(DATA_INT) . "<br>";
echo $incoming->read(DATA_UNSIGNED_INT) . "<br>";
echo $incoming->read(DATA_LONG) . "<br>";
echo $incoming->read(DATA_FLOAT) . "<br>";
echo $incoming->read(DATA_DOUBLE) . "<br>";
echo $incoming->read(DATA_STRING_UTF8) . "<br>";
echo $incoming->read(DATA_STRING_ASCII) . "<br>";
echo $incoming->read(DATA_STRING_C) . "<br>";
echo print_r($incoming->read(DATA_BYTE_ARRAY), true) . "<br>";

$udp->disconnect();
$incoming->__destruct();
?>