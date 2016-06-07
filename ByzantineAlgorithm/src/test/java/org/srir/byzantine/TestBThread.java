package org.srir.byzantine;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestBThread implements Runnable{
    private final static Log LOGGER = LogFactory.getLog(TestBThread.class);

    private ByzantineRMI process;
    
    public TestBThread(ByzantineRMI process){
        this.process = process;
    }
    
    public void run() {
    	try {
			LOGGER.debug("***Dziala proces: " + process.getIndex());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
}