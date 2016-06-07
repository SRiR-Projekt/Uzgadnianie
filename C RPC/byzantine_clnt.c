#include <memory.h>
#include "byzantine.h"

static struct timeval TIMEOUT = { 25, 0 };
int N=2; 
int resultReceived = 0;
static ORDER_MESSAGE dataFromPeers[2];

void * receiveorder_1(ORDER_MESSAGE *argp, CLIENT *clnt)
{
	static char clnt_res;

	memset((char *)&clnt_res, 0, sizeof(clnt_res));

	if (clnt_call (clnt, receiveOrder,
		(xdrproc_t) xdr_ORDER_MESSAGE, (caddr_t) argp,
		(xdrproc_t) xdr_void, (caddr_t) &clnt_res,
		TIMEOUT) != RPC_SUCCESS) {
		
		return (NULL);
	}
	return ((void *)&clnt_res);
}


ORDER_MESSAGE * sendmeorderyougot_1(void *argp, CLIENT *clnt)
{
	 ORDER_MESSAGE clnt_res;
	
	fprintf(stderr,"sending sendmeorderyougot message\n");
	
	memset((char *)&clnt_res, '0', sizeof(clnt_res));

		
	if (clnt_call (clnt, SendMeOrderYouGot,
		(xdrproc_t) xdr_void, (caddr_t) argp,
		(xdrproc_t) xdr_ORDER_MESSAGE, (caddr_t) &clnt_res,
		TIMEOUT) != RPC_SUCCESS) {
		return (NULL);
	}
	return (&clnt_res);
}