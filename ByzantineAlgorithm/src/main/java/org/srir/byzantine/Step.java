package org.srir.byzantine;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.srir.byzantine.message.OrderMessage;

public class Step {

	private static Log LOGGER = LogFactory.getLog(Step.class);
	
	//timeout w ms, w celu oczekiwania na wolniejsze wiadomosci, po otrzymaniu minimalnej liczby tak aby moc kontynuowac
	public static long WAITING_TIME_OUT = 100;
	
	private StepState state = StepState.FORMS_POOL;
	private List<OrderMessage> messages;
	private int maxTraitors;
	private int numProcesses;
	private long startWaitingForMissedMessages = 0;
	
	public Step(int numProcesses, int maxTraitors)
	{
		this.messages = new LinkedList<OrderMessage>();
		this.maxTraitors = maxTraitors;
		this.numProcesses = numProcesses;
	}
	
	public boolean addMessage(OrderMessage message)
	{
		if (state == StepState.READY)
		{
			return false;
		}
		
		for (OrderMessage om : messages)
		{
			if(om.getId() == message.getId())
			{
				return false;
			}
		}
		
		messages.add(message);
		LOGGER.debug("Dodano wiadomosc do Step od " + maxTraitors + " zdrajcy. Rozkazy: " + messages.size());
		//max liczba rozkazow w rundzie komunikacji jest liczba: processors-traitors-commander-process itself
		if (messages.size() == numProcesses - maxTraitors - 2)
		{
			state = StepState.WAITS_FOR_TIME_OUT;
			startWaitingForMissedMessages = System.currentTimeMillis();
		}
		return true;
	}
	
	/**
	 * Proces jest gotow na przetwarzanie tylko wowczas, gdy jest w stanie gotowosci lub limit oczekiwania na nieodebranych wiadomosci wygasl
	 * @return true jezeli proces moze byc kontynuowany, false w przeciwnym wypadku
	 */
	public boolean isReady()
	{
		if(state == StepState.FORMS_POOL)
		{
			return false;
		}
		else if (state == StepState.READY)
		{
			return true;
		}
		else
		{
			long now = System.currentTimeMillis();
			if(now - startWaitingForMissedMessages > WAITING_TIME_OUT)
			{
				return true;
			}
			return false;
		}
	}
	
	public boolean isWaitingForMissedMessages()
	{
		return state == StepState.WAITS_FOR_TIME_OUT;
	}
	
	public List<OrderMessage> getMessages()
	{
		return messages;
	}
	
	public int getMaxTraitors()
	{
		return maxTraitors;
	}
}
