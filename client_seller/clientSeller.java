import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

public class clientSeller{
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
                System.out.println("Client seller dead");
            }else{
                status = true;
                System.out.println("Client seller alive");
            }

            String firstArgs = args[0];
            
            switch (firstArgs){
                case "Help":
                    System.out.println("This is Client Seller");
                    System.out.println("To Store an item (ID, title, description) -> java clientBuyer id title description \nE.g. java clientSeller 1 'Desk' 'Made from woods'" );
                    System.out.println("To Get an item based on ID -> java clientBuyer id client_id\nE.g. java clientSeller 1 1");
                    System.out.println("To auction an Item based on ID -> java clientBuyer id description start_price reserver_price\nE.g. java clientSeller 1 'Desk made from woods' 10 100");
                    System.out.println("To close an Auction based on UID -> java clientBuyer UID\nE.g. java clientSeller 111");
                    System.out.println("\nStage2 : Reverse and Double Auctions");
                    System.out.println("Reverse: 'St2Create' to Create Item, 'St2Register' to register a seller, 'St2Set' to re-set a seller , 'St2Close' to close an auction");
                    System.out.println("Double Auction: 'St2Double' to initial Double Auction");
                    return;
                
                case "Store":
                    System.out.println("Store Item in ID, Title, Description");
                    int id = Integer.parseInt(args[1]);
                    if(server.checkID_InactiveItems(id) == true){
                        System.out.println("Duplicate Item ID Found! Re-try!");
                        return;
                    }
                    String title = args[2];
                    String description = args[3];
                    System.out.println("ID: " + id + ", Title: " + title + ", Des: " + description);
                    server.storeAuctionItem(id, title, description);
                    return;
                        
                case "Get":
                    System.out.println("Get Item in ID, Client ID");
                    int getid = Integer.parseInt(args[1]);
                    int getCid = Integer.parseInt(args[2]);
                    if(server.checkID_InactiveItems(getid) == false){
                        System.out.println("Item ID not Found! Re-try");
                        return;
                    }
                    AuctionItem getItem = server.getSpec(getid, getCid);
                    System.out.println("Item ID: " + getItem.getID() + "\nTitle: " +getItem.getTitle() +"\nDescription: "+getItem.getDescription());
                    return;
                
                case "Auction":
                    System.out.println("Auction an Inactive Item");
                    int active_id = Integer.parseInt(args[1]);
                    if(server.checkID_InactiveItems(active_id) == false){
                        System.out.println("The inactive item id not found or incorrect, re-try!");
                        return;
                    }
                    String active_description = args[2];
                    int start_price = Integer.parseInt(args[3]);
                    int reserve_price = Integer.parseInt(args[4]);
                    int uid = server.auction_Item(active_id, active_description, start_price, reserve_price);
                    System.out.println("Item ID " + active_id + "| UID: " + uid);
                    return;

                case "Close":
                    System.out.println("Close Auction based on UID");
                    //Quoting the UID to close, and announce winner and highest bidding
                    int close_UID = Integer.parseInt(args[1]);
                    if(server.checkUID_ActiveAuctions(close_UID) == false){
                        System.out.println("UID not found, retry!");
                    }
                    String winner = server.close_Auction(close_UID);
                    System.out.println(winner);
                    return;
                //Reverse
                case "St2Create":
                    System.out.println("Stage 2 Create Auction Item");
                    String stage2_title = args[1];
                    String stage2_description = args[2];
                    int selling_price = Integer.parseInt(args[3]);
                    String create = server.Stage2_AuctionItem(stage2_title, stage2_description, selling_price);
                    System.out.println(create);
                    return;
                case "St2Close":
                    System.out.println("Stage 2 Close Auction Item on ID");
                    int st2close_uid = Integer.parseInt(args[1]);
                    String st2winner = server.Stage2_closeAuction(st2close_uid);
                    System.out.println(st2winner);
                    return;

                case "St2Register":
                    System.out.println("Stage 2 Register Seller via a name");
                    System.out.println("Enter a name to register as a seller:");
                    Scanner scan = new Scanner(System.in);
                    while(true){
                        String reg_name = scan.nextLine();
                        String resg = server.register_Seller(reg_name);
                        if(resg == null){
                            System.out.println("Duplicate Name found, Try-Again!");
                        }else{
                            System.out.println(resg);
                            break;
                        }
                    }
                    return;
                case "St2Set":
                    System.out.println("Stage 2 Set to Specific Seller via a name");
                    System.out.println("Enter a name to set to a registered Seller");
                    Scanner sscan = new Scanner(System.in);
                    while(true){
                        String set_name = sscan.nextLine();
                        String setn = server.set_Seller(set_name);
                        if(setn == null){
                            System.out.println("Name not found, Please re-try! ");
                        }else{
                            System.out.println(setn);
                            break;
                        }
                    }
                    return;
                //Double Auction
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
                case "St2Double":
                    System.out.println("Stage 2 Double Auction");
                    System.out.println("Please Use: 'Check', 'Join', 'Match'");
                    Scanner dscan = new Scanner(System.in);
                    String cmd = dscan.nextLine();
                    
                    switch (cmd) {
                        case "Check":
                            System.out.println("Enter Item UID that you wish to join Double Auction: ");
                            int join_uid = dscan.nextInt();

                            String result = server.Stage2_DoubleCheck(join_uid);
                            if(result == null){
                                System.out.println("Not Enough Buyer Registered in the System, need at least 3");
                            }
                            System.out.println(result);
                            
                            break;
                    
                        case "Join":
                            System.out.println("Each Seller will join by corresponding item UID, registered name and sell price:");
                            while(true){
                                int joined_uid = dscan.nextInt();
                                String joined_seller = dscan.next();
                                int joined_sp = dscan.nextInt();
                                
                                String answer = server.Stage2_Seller_Join_Double(joined_uid, joined_seller, joined_sp);
                                System.out.println("UID: " + joined_uid +", Name: " + joined_seller + ", Sp: " + joined_sp);
                                if(answer == null){
                                    System.out.println("Error: Either Item UID or Seller Name is not registered, Please re-try!\nItem ID, Seller Name & Sell Price:");
                                    
                                }else if(answer == "Duplicate"){
                                    System.out.println("Error: Same Seller can not join consecutively! Let other Sellers join, re-try!");
                                }else{
                                    System.out.println(answer);
                                    break;
                                }  
                            }
                            break;
                        case "Match":
                            System.out.println("Double Auction Matching: Aim to Optimized Seller's Profits ");
                            System.out.println("Enter the item id that needed matching: ");
                            int match = dscan.nextInt();
                            
                            String final_respond = server.Stage2_MatchDouble(match);
                            if(final_respond == null){
                                System.out.println("Item id is not found!");
                                return;
                            }
                            if(final_respond.equals("false")){
                                System.out.println("Either Buyers or Sellers have less than 3 users!");
                                return;
                            }
                            System.out.println(final_respond);
                            break;
                        default:
                            System.out.println("Use: 'Check', 'Join', 'Match' ");
                            break;
                    }
                    return;
                case "Admin":
                    System.out.println("Stage 2, Set to Seller to Admin ");
                    String admin = server.set_Admin();
                    System.out.println(admin);
                    return;
                default:
                    System.out.println("Invalid Commands, usage: java clientSeller Help");
                    return;
            }
        } catch (Exception e) {
                if(status == false){
                    System.out.println("\n***\nServer connection not available.... Please re-try later!\n***");
                }else{
                    e.printStackTrace();
                }
        }
    }
}