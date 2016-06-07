package org.srir.byzantine;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.srir.byzantine.message.AckMessage;
import org.srir.byzantine.message.OrderMessage;


public class Byzantine extends UnicastRemoteObject implements ByzantineRMI, Runnable, Observer {

    private static final long serialVersionUID = 2526720373028386278L;
    private static Log LOGGER = LogFactory.getLog(Byzantine.class);
    private static final int TIME_OUT_MS = 500;

    private Map<String, ByzantineRMI> processCache;

    //Index aktualnego procesu
    private int index;

    //Liczba procesów uczestniczących w wymianie komunikatów
    private int numProcesses;

    //URL procesów w systemie
    private String[] urls;

    //Licznik komunikatów wysyłanych przez proces
    private int nextMessageId = 1;

    //lista rozkazow przychodzacych do decyzji
    //private Map<String, Order> orders = new HashMap<String, Order>();

    //ostateczna decyzja
    private Order finalOrder = null;

    //Kolejka przychodzących wiadomości
    private Map<List<Integer>, OrderMessage> incomingMessages = new HashMap<List<Integer>, OrderMessage>();

    //kolejka wychodzacych wiadomosci
    private List<OrderMessage> outgoingMessages = new LinkedList<OrderMessage>();

    private Map<List<Integer>, OrderSet> orderSets = new HashMap<List<Integer>, OrderSet>();

    private Map<Integer, Step> stepMap = new HashMap<Integer, Step>();

    private long lastOutcomingCheck = 0;

    private boolean firstMessageReceived = false;

    protected Byzantine(String[] urls, int index) throws RemoteException {
        super();
        processCache = new HashMap<String, ByzantineRMI>();

        this.index = index;
        this.urls = urls;
        this.numProcesses = urls.length;

        reset();
    }

    public void run() {
        //intentionally left blank
    }

    public boolean isDone() {
        return finalOrder != null;
    }

    public void receiveOrder(OrderMessage message) throws RemoteException {
        LOGGER.debug(echoIndex() + "otrzymano rozkaz od " + message.getSender() + ", rozkaz: " + message.getOrder());
        incomingMessages.put(message.getAlreadyProcessed(), message);
        sendAck(message);

        //ważne dla rund wymiany począwszy od 2, kiedy dowódca nie uczestniczy w procesie wymiany
        if (message.getSender() != 0) {
            LOGGER.debug(echoIndex() + "Zdrajcy: " + message.getMaxTraitors());
            Step step = stepMap.get(message.getMaxTraitors());
            if (step == null) {
                step = new Step(numProcesses, message.getMaxTraitors());
                stepMap.put(message.getMaxTraitors(), step);
            }
            step.addMessage(message);
            if (step.isReady()) {
                LOGGER.debug(echoIndex() + "Krok gotowy");
                processStep(step);
            } else if (step.isWaitingForMissedMessages()) {
                LOGGER.debug(echoIndex() + "Krok oczekuje");
                try {
                    Thread.sleep(Step.WAITING_TIME_OUT * 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (firstMessageReceived) {
                    processStep(step);
                }
            }

            //ważne dla rundach 0 i 1, gdy dowódca otrzymuje zlecenie od klienta (0)
            // Oraz rozpowszechnia je wśród poruczników (1)
        } else {
            firstMessageReceived = true;
            process(message);
            Step firstStep = stepMap.get(message.getMaxTraitors() - 1);
            if (firstStep != null && firstStep.isReady()) {
                LOGGER.debug(echoIndex() + "pierwszy Step ready");
                processStep(firstStep);
            }
        }
    }

    private void processStep(Step step) {
        if (step.isReady()) {
            LOGGER.debug(echoIndex() + "Krok gotowy");
            LOGGER.debug(echoIndex() + "Przetwarzaniem z krokiem " + step.getMaxTraitors() + " zdrajcow " +
                    "i " + step.getMessages().size() + " rozkazow");
            for (OrderMessage msg : step.getMessages()) {
                process(msg);
            }
            stepMap.remove(step.getMaxTraitors());
        } else {
            LOGGER.error(echoIndex() + "Nie mozna przetworzyc kroku od " + step.getMaxTraitors() + " zdrajcow: krok nie gotowy.");
            throw new RuntimeException();
        }
    }

  
    private void process(OrderMessage message) {
        LOGGER.debug(echoIndex() + "przetwarzanie " + message.toString() + " od " + message.getSender());

        Order order = message.getOrder();
        Integer maxTraitors = message.getMaxTraitors();
        List<Integer> alreadyProcessed = message.getAlreadyProcessed();

        //LOGGER.debug("***" + echoIndex() + ": Already processed list size - " + alreadyProcessed.size());
        if (alreadyProcessed.isEmpty()) // Initial case of the recursion (commander)
        {
        	LOGGER.debug(echoIndex() + ": Przesylanie rozkazow do innych...");
            finalOrder = order;
            broadcastOrder(maxTraitors, order, alreadyProcessed);
            return;
        }

        OrderSet dependentOrderSet = null;
        List<Integer> keyPreviousRecursionStep;
        if (alreadyProcessed.size() > 1) {
            keyPreviousRecursionStep = alreadyProcessed.subList(0, alreadyProcessed.size() - 1);
            dependentOrderSet = this.orderSets.get(keyPreviousRecursionStep);
        }

        if (maxTraitors != 0 /*&& null != dependentOrderSet*/) // Intermediate case of the recursion (lieutenant)
        {
            OrderSet orderSet = new OrderSet(maxTraitors, alreadyProcessed, order);
            if (alreadyProcessed.size() > 1) {
                orderSet.addObserver(dependentOrderSet);
            } else // The top of the recursion.
            {
                orderSet.addObserver(this);
            }
            this.orderSets.put(alreadyProcessed, orderSet);

            broadcastOrder(maxTraitors - 1, order, alreadyProcessed);
        } else // Bottom case of the recursion (lieutenant)
        {
            if (alreadyProcessed.size() == 1) // Initial case of the recursion (lieutenant) when f=0.
            {
                this.finalOrder = order;
            } else {
                dependentOrderSet.add(alreadyProcessed, order);
            }
        }
    }

    private void sendAck(OrderMessage orderMessage) {
        AckMessage message = getAckMessageTemplate(orderMessage.getSender());
        message.setAckId(message.getId());
        ByzantineRMI receiver = getProcess(urls[orderMessage.getSender()]);
        try {
            receiver.receiveAck(message);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveAck(AckMessage message) throws RemoteException {
        OrderMessage messageToRemove = null;
        for (OrderMessage om : outgoingMessages) {
            if (om.getId() == message.getAckId()) {
                messageToRemove = om;
                break;
            }
        }
        outgoingMessages.remove(messageToRemove);

        //Druga proba dostarczenia wiadomosci/rozkazu
        long now = System.currentTimeMillis();
        List<OrderMessage> messagesToRemove = new LinkedList<OrderMessage>();
        if (now - lastOutcomingCheck > TIME_OUT_MS) {
            lastOutcomingCheck = now;
            for (OrderMessage om : outgoingMessages) {
                if (now - om.getTimestamp() > TIME_OUT_MS) {
                    try {
                        ByzantineRMI dest = getProcess(urls[om.getReceiver()]);
                        dest.receiveOrder(om);
                        messagesToRemove.add(om);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            outgoingMessages.removeAll(messagesToRemove);
        }
    }

    private OrderMessage getOrderMessageTemplate(int receiver) {
        nextMessageId++;
        return new OrderMessage(nextMessageId - 1, index, receiver);
    }

    private AckMessage getAckMessageTemplate(int receiver) {
        nextMessageId++;
        return new AckMessage(nextMessageId - 1, index, receiver);
    }

    public void reset() {

    }

    public int getIndex() {
        return index;
    }

    public boolean isFaulty() throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    public void setFaulty(boolean isFaulty) throws RemoteException {
        // TODO Auto-generated method stub

    }

    /**
     * Zwraca proces określony przez URL.
     *
     * @param url process url
     * @return process
     */
    private ByzantineRMI getProcess(String url) {
        ByzantineRMI result = processCache.get(url);
        if (result == null) {
            try {
                result = (ByzantineRMI) Naming.lookup(url);
            } catch (RemoteException e1) {
                throw new RuntimeException(e1);
            } catch (MalformedURLException e2) {
                throw new RuntimeException(e2);
            } catch (NotBoundException e3) {
                throw new RuntimeException(e3);
            }
            processCache.put(url, result);
        }
        return result;
    }


    private void broadcastOrder(int maxTraitors, Order order, List<Integer> alreadyProcessed) {
        for (int i = 0; i < numProcesses; i++) {
            if (index != i && !alreadyProcessed.contains(i)) {
                ByzantineRMI destination = getProcess(urls[i]);

                OrderMessage messageCopy = getOrderMessageTemplate(i);
                messageCopy.setMaxTraitors(maxTraitors);
                messageCopy.setOrder(order);
                messageCopy.setAlreadyProcessed(new LinkedList<Integer>(alreadyProcessed));
                messageCopy.getAlreadyProcessed().add(index);

                try {
                    destination.receiveOrder(messageCopy);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private String echoIndex() {
        return "[" + index + "] ";
    }

    /**
     * Aktualizacja obserwatora w wyniku głosowania większościowego zestawem obserwowalnym i odpowiedniego klucza.
     */
    @SuppressWarnings("unchecked")
    public void update(Observable arg0, Object arg1) {
        //LOGGER.debug("---Update called-------------------------------------------");
        AbstractMap.SimpleEntry<List<Integer>, Order> se;
        if (arg1 instanceof AbstractMap.SimpleEntry<?, ?>) {
            try {
                se = (AbstractMap.SimpleEntry<List<Integer>, Order>) arg1;
                this.finalOrder = se.getValue();
                
                LOGGER.debug(echoIndex() + " - Wybor wiekszosciowy: " + finalOrder);
            } catch (Exception e) {
                LOGGER.debug("nieudane rzutowanie do SimpleEntry<List<Integer>, Order>.");
            }
        }
    }
}