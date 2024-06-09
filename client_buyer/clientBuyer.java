import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

public class clientBuyer {
    static boolean status = false;
    public static void main(String[] args){
        if (args.length < 1) {
            System.out.println("Usage: java Client Help");
            return;
        }
        
        try {
            String name = "auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            IAuction server = (IAuction) registry.lookup(name);
            if(server.getServerStatus() == false){
                status = false;
                server = null;
            }else{
                status = true;
            }
            String firstArgs = args[0];
            
            switch (firstArgs) {
                case "Help":
                    System.out.println("This is Buyer Program");
                    System.out.println("To browse through the bidding list, Usage: java clientBuyer Browse");
                    System.out.println("To Bid on an item, need to bid based on the UID. Buyer will neeed to enter their Info.\nUsage: java clientBuyer Bid UID BID_Price Buyer_name Buyer_email");
                    System.out.println("E.g. java clientBuyer Bid 111 100 'Austin' '@gmail.com' ");
                    System.out.println("\nStage 2: Reverse and Double Auction:");
                    System.out.println("'Reverse' for inital Reverse Auction based on item id");
                    System.out.println("'St2Browse' to browse Item list ");
                    System.out.println("'Double' to initial Double Auction based on item id");
                    return;
            
                case "Browse":
                    System.out.println("Browse All Active Auction with Corresponding UID, Item ID & Highest Bidding");
                    Hashtable<Integer, AuctionItem> all_actives = server.getActive_Auctions();
                    Enumeration<Integer> keys = all_actives.keys();
                    while(keys.hasMoreElements()){
                        int key = keys.nextElement();
                        System.out.println("UID: " + key +  "  | Highest Bidding: " + server.get_MAX_val_from_Biddings(all_actives.get(key).getBiddings()));
                    }
                    return;

                case "Bid":
                    System.out.println("Bid on a item based on UID, Buyer's name & email");
                    //Bid item id, bid price, name , email
                    int bid_itemID = Integer.parseInt(args[1]);
                    if(server.checkUID_ActiveAuctions(bid_itemID) == false){
                        System.out.println("Auction UID not found, retry!");
                        return;
                    }
                    int bid = Integer.parseInt(args[2]);
                    int start_price = server.getStartingPrice(bid_itemID);
                    if(start_price > bid){
                        System.out.println("Starting Price is " + start_price + " > Bid is " + bid + ", Re-try a higher bid!");
                        return;
                    }
                    String buyer_name = args[3];
                    String buyer_email = args[4];

                    System.out.println("UID: " + bid_itemID + ", Bid: " + bid + ", Name: " + buyer_name + ", Email: " + buyer_email);
                    server.bid_ActiveAuctions(bid_itemID, bid, buyer_name, buyer_email);
                    return;
                case "St2Register":
                    System.out.println("Stage 2 Register Buyer via a name");
                    System.out.println("Enter a name to register as a Buyer:");
                    Scanner rscan = new Scanner(System.in);
                    while(true){
                        String reg_name = rscan.nextLine();
                        String resg = server.register_Buyer(reg_name);
                        if(resg == null){
                            System.out.println("Duplicate Name found, Try-Again!");
                        }else{
                            System.out.println(resg);
                            break;
                        }
                    }
                    return;

                case "St2Set":
                    System.out.println("Stage 2 Set to Specific Buyer via a name");
                    System.out.println("Enter a name to set to a registered Buyer");
                    Scanner sscan = new Scanner(System.in);
                    while(true){
                        String set_name = sscan.nextLine();
                        String setn = server.set_Buyer(set_name);
                        if(setn == null){
                            System.out.println("Name not found, Please re-try! ");
                        }else{
                            System.out.println(setn);
                            break;
                        }
                    }
                    return;
                case "Reverse":
                    Scanner scan = new Scanner(System.in);
                    System.out.println("Stage 2 , Reverse Auction: Browse Auction Item on related key words");
                    while(true){
                        System.out.println("Enter Item Name To Search:");
                        String searching_item = scan.nextLine();
                        Hashtable<Integer, String> temp = server.Stage2_Reverse_Browse(searching_item);
                        if(temp == null){
                            System.out.println("No Item founds");
                            return;
                        }
                        Enumeration<Integer> rkeys = temp.keys();
                        
                        while(rkeys.hasMoreElements()){
                            int currkey = rkeys.nextElement();
                            System.out.println(temp.get(currkey));
                        }
                        System.out.println("Enter the UID of item, Bid price, Buyer Name & Buyer Email:");
                        int biduid = scan.nextInt(); 
                        int bidding = scan.nextInt();
                        String bname = scan.next();
                        String bemail = scan.next();

                        String result = server.Stage2_Reverse_bid(biduid, bidding, bname, bemail);
                        System.out.println(biduid + ", " + bidding + ", " + bname + ", " + bemail);
                        if(result == null){
                            System.out.println("Your Bidding is not higher than the current bidding: " + server.retcurrbid(biduid) + ", Please re-try!" );
                        }else if(result.equals("false")){
                            System.out.println("Buyer name: " + bname + " has not yet been registered yet! Please register first!");
                            break;
                        }else{
                            System.out.println("Reverse Result:\n" + result);
                            break;
                        }
                    }
                    return;
                case "St2Browse":
                    System.out.println("Stage 2 Browse Function: View Auction Item lists");
                    System.out.println("Enter Items for viewing lists:");
                    Scanner bscan = new Scanner(System.in);
                    String browse = bscan.nextLine();
                    switch (browse) {
                        case "Items":
                            ArrayList<String> temp = server.retst2Items();
                            for(int i = 0; i<temp.size(); i++){
                                System.out.println(temp.get(i));
                            }
                            break;
                            case "Stage1":
                            ArrayList<String> temp2 = server.retst1Items();
                            for(int j = 0; j<temp2.size(); j++){
                                System.out.println(temp2.get(j));
                            }

                            break;
                        default:
                            System.out.println("Please Enter either 'Reverse' or 'Double'. Re-try!s");
                            break;
                    }
                    return;
                case "Double":
                    System.out.println("Stage 2 Double Auction");
                    System.out.println("Use: 'Check' for Check if an item exist or buyers are more than 3 & 'Bid' for Buyer to bid ");
                    Scanner dscan = new Scanner(System.in);
                    String dcmd = dscan.nextLine();
                    
                    switch (dcmd) {
                        case "Check":
                            System.out.println("Enter an Item id to check existence: ");
                            int id = dscan.nextInt();
                            
                            String checker = server.Stage2_Buyer_DoubleCheck(id);
                            if(checker == null){
                                System.out.println("Error: Item ID not exist ");
                                return;
                            }else if(checker.equals("small")){
                                System.out.println("Less than 3 Buyers on the current item bidding, Please add more buyers before matching!");
                                return;
                            }
                            System.out.println(checker);
                            break;
                        case "Bid":
                            System.out.println("Buyer Bid on Item ID");
                            while(true){
                                System.out.println("Enter the UID of item, Bid price, Buyer Name & Buyer Email:");
                                int d_uid = dscan.nextInt();
                                int dbid = dscan.nextInt();
                                String dname = dscan.next();
                                String demail = dscan.next();

                                String respond = server.Stage2_Buyer_DoubleBid(d_uid, dname, demail, dbid);
                                System.out.println(d_uid + ", " + dbid + ", " + dname + ", " + demail);
                                if(respond == null){
                                    System.out.println("Your Bidding is not higher than the current bidding: " + server.retcurrbid(d_uid) + ", Please re-try!");
                                }else if(respond.equals("false")){
                                        System.out.println("Buyer Name: " + dname + " not found or registered! Please try registering!");
                                        break;
                                }else if(respond.equals("Duplicate")){
                                        System.out.println("Buyer Name: " + dname + " already bid! Please let other buyers bid this item!");
                                }else{
                                    System.out.println("\nMatching Result:\n"+respond);
                                    break;
                                }
                            }
                            break;
                        default:
                            System.out.println("Re-try, Use 'Check' or 'Bid'! ");
                            break;
                    }
                    return;
                default:
                    System.out.println("Invalid commands, usage: java clientBuyer Help");
                    return;
            }
        } catch (Exception e){
            if(status == false){
                System.out.println("\n***\nServer connection not available.... Please re-try later!\n***");
            }else{
                e.printStackTrace();
            }
        }
    }
}
