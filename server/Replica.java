import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class Replica extends ReceiverAdapter {
    JChannel jchannel;
    private ArrayList<Object> replicaState = new ArrayList<Object>();
    private Address feNode;
    int counter = 0;

    public Replica(String nodeName) throws Exception{
        jchannel = new JChannel();
        jchannel.name(nodeName);
        jchannel.setReceiver(this);
        jchannel.connect("Cluster");
        
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
                echoReplicaState();
            };
        executor.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }
    private void echoReplicaState(){
        System.out.println("(Replica) Channel: " + jchannel.getName() + ", State: " + replicaState);
        List<Address> temp = jchannel.getView().getMembers();
        if(counter == 0){
            feNode = temp.get(0);
            counter++;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception{
        ObjectInputStream in = new ObjectInputStream(input);
        ArrayList<Object> setStateArr = (ArrayList<Object>) in.readObject();
        replicaState = setStateArr;
        //System.out.println("Set State Channel: " + jchannel.getName() + ", State: " + replicaState);
    }
    @Override
    public void receive(Message msg){
        try {
            ArrayList<Object> temp = deserializeArrayList(msg.getBuffer());
            List<Address> list = jchannel.getView();
            if(temp.equals(replicaState)){
                //If Local and Echo's Buffer is the same, then don't update State
            }else{
                Address tempFE = findFrontEnd(list, feNode);
                if(tempFE.equals(msg.getSrc())){
                    replicaState = temp;
                    jchannel.send(feNode, serializeArrayList(temp));
                }else{
                    //Don't Accept message if its not from Frontend
                }
            }
        }catch (Exception e) {
            System.err.println("Error in Replica: ");
            e.printStackTrace();
        }
    }
    @Override
    public void viewAccepted(View view) {
        System.out.println("New view: " + view);
    }
    
    private Address findFrontEnd(List<Address> list, Address frontend){
        int counter = 0;
        Address temp = null;
        while(list.size() > counter){
            Address currAddr =  list.get(counter);
            //System.out.println("Frontend: " + frontend + ", List: " + list);
            if(frontend.equals(currAddr)){
                temp = currAddr;
                //System.out.println("Frontend Found! + " + temp);
                return temp;
            }
            counter++;
        }
        return null;
    }
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Usage: java Replica <node Name>");
            System.exit(1);
        }
        String replicaName = args[0];
        new Replica(replicaName);
    }

    //Serialize and De-serialize ArrayLists
    private byte[] serializeArrayList(ArrayList<Object> convertObjs){
        byte[] messageAsBytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(convertObjs);
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
        //System.out.println("Channel: " + jchannel.getName() + ", Deserialize: " + replicaState);
    }
}