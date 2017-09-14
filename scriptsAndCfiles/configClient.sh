#!/bin/bash
# Indique au système que l'argument qui suit est le programme utilisé pour exécuter ce fichier
# En règle générale, les "#" servent à mettre en commentaire le texte qui suit comme ici

sudo ip tuntap add mode tun dev tun0
sudo ip link set dev tun0 up
sudo ip addr add 10.0.0.1/24 dev tun0
sudo ip route add default via 10.0.0.2
sudo ifconfig tun0 mtu 500

sudo ip addr add 9.0.0.1/24 dev enp2s0
sudo ip link set dev enp2s0 up

exit 0
