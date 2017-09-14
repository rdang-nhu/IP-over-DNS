#!/bin/bash
# Indique au système que l'argument qui suit est le programme utilisé pour exécuter ce fichier
# En règle générale, les "#" servent à mettre en commentaire le texte qui suit comme ici

sudo ip tuntap add mode tun dev tun1
sudo ip addr add 10.0.0.2/24 dev tun1
sudo ip link set dev tun1 up
sudo ifconfig tun1 mtu 1000

sudo ip addr add 9.0.0.2/24 dev enp1s0

exit 0
