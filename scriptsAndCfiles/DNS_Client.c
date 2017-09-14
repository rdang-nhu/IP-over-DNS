#include <jni.h>
#include <stdio.h>
#include "DNS_Client.h"
#include<iostream>
#include <sys/types.h>
#include <sys/socket.h>
#include <linux/if.h>
#include <linux/if_tun.h>
#include <fcntl.h>  /* O_RDWR */
#include <string.h> /* memset(), memcpy() */
#include <stdlib.h> /* exit(), malloc(), free() */
#include <sys/ioctl.h> /* ioctl() */

JNIEXPORT void JNICALL
Java_DNS_1Client_ioctl(JNIEnv *env, jobject obj, jint descriptor, jint interf){
  struct ifreq ifr;
  memset(&ifr, 0, sizeof(ifr));
  ifr.ifr_flags = IFF_TUN;
  if((int)interf == 0){
    strncpy(ifr.ifr_name, "tun0", IFNAMSIZ);
  }
  if((int)interf == 1){
    strncpy(ifr.ifr_name, "tun1", IFNAMSIZ);
  }
  printf(" Bonjour\n ");
  std::cout << descriptor << std::endl;
  int err;
  if ( (err = ioctl(descriptor, TUNSETIFF, (void *) &ifr)) == -1 ) {
    perror("ioctl TUNSETIFF");exit(1);
  }
  return;
}
