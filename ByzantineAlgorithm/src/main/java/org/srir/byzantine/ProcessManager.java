package org.srir.byzantine;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessManager {
	private static final Log LOGGER = LogFactory.getLog(ProcessManager.class);
	
	private static final String RMI_PREFIX="rmi://";
	private ArrayList<ByzantineRMI> processes;
	private InetAddress inetAddress;
	
	//uruchamianie instancji serwera
	public void startServer()
	{
		//odczytanie konfiguracji sieci
		Configuration config;
		try
		{
			config = new PropertiesConfiguration("newtork.cfg");
		}
		catch (ConfigurationException e)
		{
			try
			{
				config = new PropertiesConfiguration("newtork.cfg.default");
			}
			catch (ConfigurationException e2)
			{
				LOGGER.error("Nie mozna odczytac konfiguracji sieci");
				throw new RuntimeException(e2);
			}
		}
		
		//utworzenie instacji InetAddress do rozwiązania lokalnego IP
		try
		{
			inetAddress = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
			LOGGER.error("Nie mozna utworzyc wystapienie IP resolvera");
			throw new RuntimeException(e);
		}
	}
}
