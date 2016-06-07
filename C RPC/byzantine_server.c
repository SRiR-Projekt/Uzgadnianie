#include "byzantine.h"

int orderFromInitiator;
int isOrderReceived ;

void * receiveorder_1_svc(ORDER_MESSAGE *argp, struct svc_req *rqstp)
{
	static char * result;

	if(argp == NULL)
	return;
	
	isOrderReceived = 1;
	orderFromInitiator = argp->orderValue;

	fprintf(stderr,"received order %d , my id is %d \n",orderFromInitiator,HOSTID);
		
	return (void *) &result;
}

