package org.srir.byzantine;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OrderSet extends Observable implements Observer{
	
	private static Log LOGGER = LogFactory.getLog(OrderSet.class);
	
	Map<List<Integer>,Order> orderset;
	public int size;
	
	
	/*
	 * Konstruktor instacji OrderSet oraz dodawanie wartosci od dowodcy
	 * @param size liczba rozkazow w set
	 * @param key poczatkowa wartosc (od commander)
	 * @param order poczatkowy rozkaz (od commander)
	 */
	public OrderSet(int size, List<Integer> key, Order order)
	{
		this.orderset = new HashMap<List<Integer>, Order>();
		this.orderset.put(key, order);
		this.size = size;
	}
	
	
	/*
	 * Dodanie wartosci od porucznikow, kiedy rozmiar zostal osiagniet z powiadomien obserwatorow w wyniku glosowania wieksciowego i odpowiedniego klucza
	 */
	public void add(List<Integer> key, Order order)
	{
		this.orderset.put(key, order);
		
		if(this.orderset.size() > this.size)
		{
			LOGGER.debug("Powiadamianie innych porucznikow....");
			List<Integer> initialKey = this.orderset.keySet().iterator().next();
			AbstractMap.SimpleEntry<List<Integer>, Order> se = new AbstractMap.SimpleEntry<List<Integer>, Order>(initialKey, this.majority());
			this.setChanged();
			this.notifyObservers(se);
		}
	}
	
	//ustalanie jaka wartosc wystepuje najczesciej w zestawie
	private Order majority()
	{
		return Order.getMostFrequentOrder(new ArrayList<Order>(this.orderset.values()));
	}
	
	@SuppressWarnings("unchecked")
	public void update(Observable arg0, Object arg1)
	{
		AbstractMap.SimpleEntry<List<Integer>, Order> se;
		if(arg1 instanceof AbstractMap.SimpleEntry<?, ?>)
		{
			try
			{
				se = (AbstractMap.SimpleEntry<List<Integer>, Order>) arg1;
				this.add(se.getKey(), se.getValue());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
