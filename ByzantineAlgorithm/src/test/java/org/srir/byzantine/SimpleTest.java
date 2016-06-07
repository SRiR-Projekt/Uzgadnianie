package org.srir.byzantine;

import org.srir.byzantine.message.OrderMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleTest{
    
    private TestSetup setup;

    @Before
    public void init(){
        setup = new TestSetup();
        setup.init();
    }

    /**
     * Three processes start simultaneously
     */
    @Test
    public void testSimple(){
        ByzantineRMI commanderProcess = (ByzantineRMI) setup.getProcesses().get(0);
        TestBThread thread1 = new TestBThread(commanderProcess);
        new Thread(thread1).start();
        
        ByzantineRMI lieutenantProcess1 = (ByzantineRMI) setup.getProcesses().get(1);
        TestBThread thread2 = new TestBThread(lieutenantProcess1);
        new Thread(thread2).start();
        ByzantineRMI lieutenantProcess2 = (ByzantineRMI) setup.getProcesses().get(2);
        TestBThread thread3 = new TestBThread(lieutenantProcess2);
        new Thread(thread3).start();
        ByzantineRMI lieutenantProcess3 = (ByzantineRMI) setup.getProcesses().get(3);
        TestBThread thread4 = new TestBThread(lieutenantProcess3);
        new Thread(thread4).start();

        int maxTraitors = 1;
        Order order = Order.ATAK;        
        
        try{
            commanderProcess.reset();
            lieutenantProcess1.reset();
            lieutenantProcess2.reset();
            lieutenantProcess3.reset();
            
            // Gives new order to himself, like a root in a graph is it's own parent.
            // The already processed stays empty.
            // Both indicate that this process is the commander.
            OrderMessage message = new OrderMessage(0, commanderProcess.getIndex(), commanderProcess.getIndex());
            message.setMaxTraitors(maxTraitors);
            message.setOrder(order);
            commanderProcess.receiveOrder(message);
            
            // Start all processes and the commander last, so that everyone is able to receive a message.
            new Thread(thread2).start();
            new Thread(thread3).start();
            new Thread(thread4).start();
            new Thread(thread1).start();
            
            Thread.sleep(10000);
            Assert.assertTrue(commanderProcess.isDone());
            Assert.assertTrue(lieutenantProcess1.isDone());
            Assert.assertTrue(lieutenantProcess2.isDone());
            Assert.assertTrue(lieutenantProcess3.isDone());

        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }
    
    /**
     * Three processes start simultaneously
     */
    
    @Test
    public void testOneTraitor(){
        ByzantineRMI commanderProcess = (ByzantineRMI) setup.getProcesses().get(0);
        TestBThread thread1 = new TestBThread(commanderProcess);
        new Thread(thread1).start();
        ByzantineRMI lieutenantProcess1 = (ByzantineRMI) setup.getProcesses().get(1);
        TestBThread thread2 = new TestBThread(lieutenantProcess1);
        new Thread(thread2).start();
        ByzantineRMI lieutenantProcess2 = (ByzantineRMI) setup.getProcesses().get(2);
        TestBThread thread3 = new TestBThread(lieutenantProcess2);
        new Thread(thread3).start();
        ByzantineRMI lieutenantProcess3 = (ByzantineRMI) setup.getProcesses().get(3);
        TestBThread thread4 = new TestBThread(lieutenantProcess3);
        new Thread(thread4).start();

        int maxTraitors = 1;
        Order order = Order.ODWROT;        
        
        try{
            commanderProcess.reset();
            lieutenantProcess1.reset();
            lieutenantProcess2.reset();
            lieutenantProcess3.reset();
            
            // Gives new order to himself, like a root in a graph is it's own parent.
            // The already processed stays empty.
            // Both indicate that this process is the commander.
            OrderMessage message = new OrderMessage(0, commanderProcess.getIndex(), commanderProcess.getIndex());
            message.setMaxTraitors(maxTraitors);
            message.setOrder(order);
            commanderProcess.receiveOrder(message);
            
            // Start all processes and the commander last, so that everyone is able to receive a message.
            new Thread(thread2).start();
            new Thread(thread3).start();
            new Thread(thread4).start();
            new Thread(thread1).start();
            
            Thread.sleep(10000);
            Assert.assertTrue(commanderProcess.isDone());
            Assert.assertTrue(lieutenantProcess1.isDone());
            Assert.assertTrue(lieutenantProcess2.isDone());
            Assert.assertTrue(lieutenantProcess3.isDone());

        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }  
}