#include "byzantine.h"

bool_t
xdr_ORDER_MESSAGE (XDR *xdrs, ORDER_MESSAGE *objp)
{
	register int32_t *buf;

	 if (!xdr_int (xdrs, &objp->orderValue))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->srcID))
		 return FALSE;
	 if (!xdr_pointer (xdrs, (char **)&objp->sourceIP, sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	return TRUE;
}
