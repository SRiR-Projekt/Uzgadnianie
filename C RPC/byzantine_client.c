#include "byzantine.h"

char *host;
int HOSTID;
int ISBYZANTINE;
int initiator = 0;


char nodeIPs[4][100]={"192.168.0.1","192.168.0.2","192.168.0.3","192.168.0.4"};

int nodeIDS[4] = {1,2,3,4};
int orderToSend[3];
int lastOrderSent=1;


void*
clientFunc()
{
	CLIENT *clnt;
	void  *result_1;
	ORDER_MESSAGE  receiveorder_1_arg;
	ORDER_MESSAGE  *result_2;
	char *sendmeorderyougot_1_arg;
	
	while(1)
	{	
		if(initiator)
		{
			sendOrderToNextLevel();
		}
		else
		{
			int i ;
			for(i=0;i<5;i++)
			{
				sleep(2);
				if(isOrderReceived ==1)
				{
					break;	
				}
			}
			if(isOrderReceived)
			{
				GetTheOrdersOthersHaveGot();
				isOrderReceived = 0;
			}
		}
		sleep(10);
	}		
	return NULL;
}


void GetTheOrdersOthersHaveGot()
	{
		CLIENT *clnt;
		ORDER_MESSAGE  *result_2;
		int i;
		int ordersFromPeers[2];
		int j = 0;
		int majorityOrder;
		for(i=0;i<4;i++)
		{
			//dont send to initiator and self
			if((i != 0) && (i != (HOSTID -1)))
			{
				fprintf(stderr,"Asking node with IP:%s about the order it got\n",nodeIPs[i]);
				clnt = clnt_create (nodeIPs[i], BYZANTINE, BYZANTINEVERS, "udp");
				result_2 = sendmeorderyougot_1(NULL, clnt);
				if (result_2 == (ORDER_MESSAGE *) NULL)
					clnt_perror (clnt, "call failed");
				else
				{
					//received order from 
					ordersFromPeers[j]=result_2->orderValue;
					fprintf(stderr,"Data received node with IP:%s and the order is:%d\n",nodeIPs[i],result_2->orderValue);
				        j++; 
				}
			}	
		}	

		//find the majority of the orders you have received and declare that as the order you will follow
		if(orderFromInitiator == ordersFromPeers[0])
			//announce the majority order
			majorityOrder = orderFromInitiator;	
		else if(orderFromInitiator == ordersFromPeers[1])
			majorityOrder = orderFromInitiator;
		else
			majorityOrder= ordersFromPeers[1];

		fprintf(stderr,"Majority order is %d  and i will follow that\n ", majorityOrder);	
		
	}


void sendOrderToNextLevel()
	{
		CLIENT *clnt;
		fprintf(stderr,"%s","Initiating the send of order\n");
		
		ORDER_MESSAGE receiveorder_1_arg;
		int n = !lastOrderSent;
		lastOrderSent = n;
		fprintf(stderr,"Order to send = %d \n",n);
		orderToSend[0]=orderToSend[1]=orderToSend[2]=n;
		if(ISBYZANTINE)
		{
			fprintf(stderr,"I am byzantine so i will send confusing orders \n");
			orderToSend[1] = !n;
		}
		receiveorder_1_arg.srcID= HOSTID;
		//not using this in the sample
		receiveorder_1_arg.sourceIP =NULL;
		int i ;
		for(i=0;i<3;i++)
		{
			fprintf(stderr,"Sending order : %d to IP:%s\n", orderToSend[i],nodeIPs[i+1]);	
			clnt = clnt_create (nodeIPs[i+1], BYZANTINE, BYZANTINEVERS, "udp");
			if (clnt == NULL) {
			clnt_pcreateerror (host);
			exit (1);
			}
			receiveorder_1_arg.orderValue = orderToSend[i];
			void *result_1;
			result_1 = receiveorder_1(&receiveorder_1_arg, clnt);
			if (result_1 == (void *) NULL) {
				clnt_perror (clnt, "call failed");
			}
			clnt_destroy (clnt);
		}
	}



int
main (int argc, char *argv[])
{
	pthread_t client,server;
	
	int count;
	if (argc < 2) {
		printf ("usage: %s server_host\n", argv[0]);
		exit (1);
	}
	host = argv[1];
	while(1)
	{
		printf("Choose the host id of this node  (1,2,3 or 4} \n");
		fflush(stdin);
		scanf("%d",&HOSTID);
		if(HOSTID <5 && HOSTID >0)
		break;
	}
	printf("Please check if IP addres of this node is %s\n",nodeIPs[HOSTID-1]);		
	fflush(stdin);
	printf("Is this node Byzantine Enter 0=No 1=yes \n");
	fflush(stdin);
	scanf("%d",&ISBYZANTINE);
	fflush(stdin);
	//1st node is the initiator in this sample
	if(HOSTID == 1)
		initiator= 1;
	
	//start the server
	printf("starting the server host id is %d IsByZantine=%d IsInitiator =%d\n",HOSTID,ISBYZANTINE,initiator);
	count = pthread_create(&server,	NULL,serverFunc,NULL);
	sleep(1);
	//start the client
	printf("starting the client \n");
	count = pthread_create(&client,	NULL,clientFunc,NULL);
	sleep(1);

	while(1){};

exit (0);
}
