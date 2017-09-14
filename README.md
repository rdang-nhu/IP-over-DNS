# IP over DNS

In this project, we set up a DNS tunnel for IP packets. The final goal of such an approach is to provide full IP connectivity with only the ability to send recursives DNS queries through a local DNS resolver. The idea is to use a virtual TUN interface between the client and the server, and set a correct routing configuration on both computers. We also have to encode the IPpackets into DNS queries and answers and decode it on the other side. Then, on the server side, we need to set up a forwarding mechanism via NAT.

Concerning the technical aspects, the network programming is made in Java, as it was the mandatory programming language for the course. However, we could not find an equivalent for the ioctl() function, therefore we needed to use the JNI to call native functions.

We also had to use multithreading and concurrent data structures on both sides.

Finally, we managed to get a working tunnel, which allowed us to ping ip addresses behind a firewall. We could also initiate an ssh connection. However, it was to slow to provide a functional web connection.


