#include "byzantine.h"

int orderFromInitiator;
int isOrderReceived ;

void * receiveorder_1_svc(ORDER_MESSAGE *argp, struct svc_req *rqstp)
{
	static char * result;

	if(argp == NULL)
	return;
	
	//save the order received from the initiator, and mark  the flag that the order has been received
	isOrderReceived = 1;
	orderFromInitiator = argp->orderValue;

	fprintf(stderr,"received order %d , my id is %d \n",orderFromInitiator,HOSTID);
		
	return (void *) &result;
}

ORDER_MESSAGE * sendmeorderyougot_1_svc(void *argp, struct svc_req *rqstp)
{
	static ORDER_MESSAGE  result;
	//i am byzantine so i will send random values

	if(ISBYZANTINE)
	{
		fprintf(stderr,"I am byzantine so i will send random values\n");		
		result.orderValue = !orderFromInitiator;
	}
	else
		result.orderValue = orderFromInitiator;

	result.sourceIP = NULL;
	result.srcID = HOSTID;

	return &result;
}
