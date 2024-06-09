import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class Frontend extends ReceiverAdapter {
    JChannel jChannel;
    private ArrayList<Object> localState = new ArrayList<Object>();
    private ArrayList<Object> serverState = new ArrayList<Object>();
    private IAuction IA;
    private boolean status;

    public Frontend(IAuction server) throws Exception{
        jChannel = new JChannel();
        jChannel.name("FrontEnd");
        jChannel.setReceiver(this);
        jChannel.connect("Cluster");
        this.IA = server;
        this.status = true;
        if(localState.isEmpty()){
                serverState = IA.getAllHashtables();
                localState = serverState;
        }
        System.out.println("Frontend Starting...");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
            Runnable task = () -> {
                eventloop();
            };
        executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);

        Runnable task2 = () -> {
                echoFrontendState();
        };
        executor.scheduleAtFixedRate(task2, 0, 10, TimeUnit.SECONDS);
    }
    private void echoFrontendState(){
        //System.out.println("\nFrontEnd:\nLocal State: " + localState +"\n\nServer State:" + serverState);
    }

    //Send 
    private void eventloop(){
        List<Address> currMembers = jChannel.getView().getMembers();
        
        if(currMembers.size() == 1){
            System.out.println("No replicas availble, waiting...");
            status = false;
            try {
                IA.serverDead();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            status = true;
            IA.serverAlive();
            serverState = IA.getAllHashtables();
            //System.out.println("Frontend Sent:\nLocal: " +localState + "\nServer: "+serverState+"\n");
            if(localState.equals(serverState) == true){
                System.out.println("Data are the same");
            }else{
                //If two lists are not equivalent, then update locate and broadcast to replicas
                //System.out.println("\nServer State: " + serverState + "\nLocal: " + localState);
                localState = serverState;
                broadcastAllReplicas(localState);
            }
        } catch (Exception e) {
            System.err.println("Error in EventLoop: ");
            e.printStackTrace();
        }
    }
    private void broadcastAllReplicas(ArrayList<Object> objs){
        try {
            jChannel.send(null, serializeArrayList(objs));
        } catch (Exception e) {
            System.err.println("Error in Broadcast:");
            e.printStackTrace();
        }
    }

    public boolean getFrontEndStatus(){
        return status;
    }
    //Overrides
    @Override
    public void receive(Message msg){
        try{
            ArrayList<Object> temp = deserializeArrayList(msg.getBuffer());
            localState = temp;
        }catch(Exception e){
            System.err.println("Error in FrontEnd Receive:");
            e.printStackTrace();
        } 
    }
    @Override
    public void getState(OutputStream output) throws Exception{
        ObjectOutputStream out = new ObjectOutputStream(output);
        out.writeObject(localState);
        //System.out.println("Get State: " + jChannel.getName() + ", Local: " + localState);
    }
    @Override
    public void viewAccepted(View view) {
        System.out.println("New view: " + view);
    }
    //Frontend
    public static void main(String[] args){
        String name = "auction";
        try {
            AuctionItem AI1 = new AuctionItem(0, "Server", "Indepedent");
            IAuction stub = (IAuction) UnicastRemoteObject.exportObject(AI1, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            IAuction server = (IAuction) registry.lookup(name);
            new Frontend(server);

        } catch (Exception e) {
            System.err.println("Error in Main: ");
            e.printStackTrace();
        }
    }

    private byte[] serializeArrayList(ArrayList<Object> convertHTs){
        byte[] messageAsBytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(convertHTs);
            oos.flush();
            messageAsBytes = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return messageAsBytes;
    }
    @SuppressWarnings("unchecked")
    private ArrayList<Object> deserializeArrayList(byte[] data) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ArrayList<Object> temp = (ArrayList<Object>) ois.readObject();
        return temp;
    }
}
