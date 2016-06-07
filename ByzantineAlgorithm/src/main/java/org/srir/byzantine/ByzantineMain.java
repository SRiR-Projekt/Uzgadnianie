package org.srir.byzantine;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;

@SuppressWarnings("deprecation")
public class ByzantineMain {
	//klasa główna odpowiedzialna za zainicjonowanie RMI registry oraz uruchomienie serwera
	
	public static void main(String[] args)
	{
		//RMIregistry inicjalizacja
		try
		{
			LocateRegistry.createRegistry(1099);
		}
		catch(RemoteException e)
		{
			e.printStackTrace();
		}
		
		//stworzenie i instalacja security manager
		if(System.getSecurityManager() == null)
		{
			System.setSecurityManager(new RMISecurityManager ());
		}
		
		new ProcessManager().startServer();
	}
}
