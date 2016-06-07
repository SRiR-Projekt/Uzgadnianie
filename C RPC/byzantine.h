#ifndef _BYZANTINE_H_RPCGEN
#define _BYZANTINE_H_RPCGEN

#include <rpc/rpc.h>
#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif


struct ORDER_MESSAGE {
	int orderValue;
	int srcID;
	char *sourceIP;
};
typedef struct ORDER_MESSAGE ORDER_MESSAGE;

#define BYZANTINE 0x44553344
#define BYZANTINEVERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define receiveOrder 1
extern int HOSTID;
extern int ISBYZANTINE;
extern int orderFromInitiator;
extern int initiator;
extern  void * receiveorder_1_svc(ORDER_MESSAGE *, struct svc_req *);

#define SendMeOrderYouGot 2
extern  ORDER_MESSAGE * sendmeorderyougot_1();
extern  ORDER_MESSAGE * sendmeorderyougot_1_svc();
extern int byzantine_1_freeresult ();
extern int serverFunc ();
#endif




