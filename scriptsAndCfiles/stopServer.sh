#!/bin/bash
# Indique au système que l'argument qui suit est le programme utilisé pour exécuter ce fichier
# En règle générale, les "#" servent à mettre en commentaire le texte qui suit comme ici

sudo ip tuntap del mode tun dev tun1
#sudo ip route del default via 10.0.0.2
sudo ip addr del 9.0.0.2/24 dev enp1s0


exit 0
