package org.srir.byzantine;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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
			config = new PropertiesConfiguration("network.cfg");
		}
		catch (ConfigurationException e)
		{
			try
			{
				config = new PropertiesConfiguration("network.cfg.default");
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
		
		String[] urls = config.getStringArray("node.url");
		processes = new ArrayList<ByzantineRMI>();
		
		//bind lokalnych procesow oraz lokalizacja zdalnych
		int processIndex = 0;
		for (String url : urls)
		{
			try
			{
				ByzantineRMI process;
				if(isProcessLocal(url))
				{
					process = new Byzantine(urls, processIndex);
					LOGGER.debug("Proces " + processIndex + " jest lokalny.");
					new Thread((Byzantine)process).start();
					Naming.bind(url, process);
				}
				else
				{
					process = (ByzantineRMI)Naming.lookup(url);
					LOGGER.debug("Poszukiwanie procesow z URL " + url);
				}
				processIndex++;
				processes.add(process);
			}
			catch(RemoteException e1)
			{
				throw new RuntimeException(e1);
			}
			catch(AlreadyBoundException e2)
			{
				throw new RuntimeException(e2);
			}
			catch(NotBoundException e3)
			{
				throw new RuntimeException(e3);
			}
			catch(MalformedURLException e4)
			{
				throw new RuntimeException(e4);
			}
		}
	}
	
	private boolean isProcessLocal(String url)
	{
		return url.startsWith(RMI_PREFIX + inetAddress.getHostAddress()) ||
				url.startsWith(RMI_PREFIX + "localhost") ||
				url.startsWith(RMI_PREFIX + "127.0.0.1");
	}
	
	public List<ByzantineRMI> getProcesses()
	{
		return processes;
	}
}
