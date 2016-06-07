package org.srir.byzantine;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.srir.byzantine.message.AckMessage;
import org.srir.byzantine.message.OrderMessage;

//zdalny interfejs do obsługi operacji RMI dla algorytmu bizantyjskich generałow
public interface ByzantineRMI extends Remote{
	
	/**
	 * Wskazuje czy proces jest zakonczony czy nie.
	 * @return processIsDone : Boolean
	 * @throws RemoteException
	 */
	public boolean isDone() throws RemoteException;
	
	/**
	 * Dzialanie podjete przez proces na rozkaz
	 * @param message order
	 * @throws RemoteException
	 */
	public void receiveOrder(OrderMessage message) throws RemoteException;
	
	/**
	 * Dzialania podjete przez proces po otrzymaniu potwierdzenia dostarczenia wiadomosci
	 * @param message acknowledgment message
	 * @throws RemoteException
	 */
	public void receiveAck(AckMessage message) throws RemoteException;
	
	/**
	 * Metoda serwisowa, by sprawdzic, czy proces jest uszkodzony
	 * @return faulty status
	 * @throws RemoteException
	 */
	public boolean isFaulty() throws RemoteException;
	
	/**
	 * Ustawia wadliwy status procesu. Jest wykorzystywana przez klienta podczas uruchamianiu systemu komunikacyjnego.
	 * @param isFaulty
	 * @throws RemoteException
	 */
	public void setFaulty(boolean isFaulty) throws RemoteException;
		
    /**
     * Index aktualnego procesu
     * @return index
     * @throws RemoteException
     */
    public int getIndex() throws RemoteException;

    /**
     * Przywraca stan biezacego procesu pozwala uruchomic kilka przypadkow testowych bez restartu.
     * @throws RemoteException
     */
    public void reset() throws RemoteException;
}
