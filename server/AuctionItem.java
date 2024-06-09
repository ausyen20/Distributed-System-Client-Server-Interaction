import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.security.KeyPair; 
import java.security.KeyPairGenerator; 
import java.security.PrivateKey; 
import java.security.PublicKey; 
import java.security.SecureRandom; 
import java.security.Signature;

public class AuctionItem implements IAuction, Serializable{

    ArrayList<Object> holdallHashtables = new ArrayList<>();

    Hashtable<Integer, AuctionItem> inactive_items = new Hashtable<Integer, AuctionItem>();
    private int itemid;
    private String title;
    private String description;

    public AuctionItem (int itemid, String title, String description){
        this.itemid = itemid;
        this.title = title;
        this.description = description;
    }
    //Get Item, based on ID if available in inactive_items
    public AuctionItem getSpec(int itemid, int clientid) throws RemoteException {
        if(checkID_InactiveItems(itemid) == false){
            System.out.println("Item ID not found!");
            return null;
        }
        AuctionItem gotItem = inactive_items.get(itemid);
        return gotItem;
    }
    //Store item based on entered parameters of id, title, description into inactive_items
    public void storeAuctionItem(int _itemid, String _title, String _description){
        if(checkID_InactiveItems(_itemid) == true){
            System.out.println("Duplicate key");
            return;
        }else{
            System.out.println("ID: " + _itemid + "\nTitle: "+ _title +"\nDes: "+ _description);
            AuctionItem item = new AuctionItem(_itemid, _title, _description);
            inactive_items.put(_itemid, item);
        }
    }
    //Check method: check if ID exist in inactive_items
    public boolean checkID_InactiveItems(int itemID){
        Enumeration<Integer> keys = inactive_items.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(key == itemID){
                System.out.println("Found");
                return true;
            }
        }
        return false;
    }


    //Getters
    public int getID(){
        return itemid;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }

    public Hashtable<Integer, AuctionItem> getStage1Items(){
        return new Hashtable<>(inactive_items);
    }
    
    public void setInactiveItems(Hashtable<Integer, AuctionItem> newInactiveItems){
        inactive_items = new Hashtable<>(newInactiveItems);
    }

    //Level 2, more parameters added to AuctionItem and crate new constructor
    private int active_id;
    private int start_price;
    private int reserve_price;
    private int active_uid;
    private String active_description;
    private ArrayList<Integer> auction_bids;
    Hashtable<Integer, AuctionItem> active_Auctions = new Hashtable<Integer, AuctionItem>();
    Hashtable<Integer, AuctionItem> active_BuyerInformations = new Hashtable<Integer, AuctionItem>();

    public AuctionItem(int active_id, String active_description, int start_price, int reserve_price, int active_uid){
        this.active_id = active_id;
        this.active_description = active_description;
        this.start_price = start_price;
        this.reserve_price = reserve_price;
        this.active_uid = active_uid;
        this.auction_bids = new ArrayList<Integer>();
    }
    //Buyer variables
    private String buyer_Name;
    private String buyer_Email;
    private int buyer_Biddings;
    private int buyer_BidItemID;
    //This constructor is used for Buyers
    public AuctionItem(int buyer_BidItemID, int buyer_Biddings, String buyer_Name, String buyer_Email){
        this.buyer_Name = buyer_Name;
        this.buyer_Email = buyer_Email;
        this.buyer_Biddings = buyer_Biddings;
        this.buyer_BidItemID = buyer_BidItemID;
    }

    public int GETbuyerItemID(){
        return buyer_BidItemID;
    }
    public int GEtbuyerBidding(){
        return buyer_Biddings;
    }
    public String GETbuyerName(){
        return buyer_Name;
    }
    public String GETbuyerEmail(){
        return buyer_Email;
    }
    //Store auction items based on enteted parameters, check if the item exist in inactive HT, then append a new AuctionItem into active_Auctions
    public int auction_Item(int auction_id, String auction_description, int auction_start, int auction_reserve){
        if(checkID_InactiveItems(auction_id) == false){
            System.out.println("Item ID not found!");
            return 0;
        }
        int uid = (auction_start + auction_reserve + auction_id);

        Enumeration<Integer> keys = active_Auctions.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(key == uid){
                uid += 100;
            }
        }
        AuctionItem itemActive = new AuctionItem(auction_id, auction_description, auction_start, auction_reserve, uid);
        active_Auctions.put(uid, itemActive);
        System.out.println("Auction ID: " + itemActive.getActiveID() + ", Des: " + itemActive.getActiveDescription() + ", UID: " + itemActive.getActiveUID() + ", Reserve:  " + itemActive.getActiveReserve());
        return uid;
    }
    //Checker method for check if ID exist in active_Auction
    public boolean checkID_ActiveAuctions(int itemID){
        Enumeration<Integer> keys = active_Auctions.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(active_Auctions.get(key).getActiveID() == itemID){
                System.out.println("Active Auction ID Found!");
                return true;
            }
        }
        return false;
    }
    //Checker method checking UID for active_Auctions
    public boolean checkUID_ActiveAuctions(int UID){
        Enumeration<Integer> keys = active_Auctions.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(key == UID){
                System.out.println("UID found in Active Auctions!");
                return true;
            }
        }
        return false;
    }
    //Buyer Bids on item with Infos
    public void bid_ActiveAuctions(int bidUID, int bid, String name, String email){
        if(checkUID_ActiveAuctions(bidUID) == false){
            System.out.println("Auction Item UID is not avaiable or found!");
            return;
        }
        Random rand = new Random();
        int buyer_UID = (rand.nextInt(100)*bidUID);

        Enumeration<Integer> uniqueIDS = active_BuyerInformations.keys();
        while(uniqueIDS.hasMoreElements()){
            int uniqueID = uniqueIDS.nextElement();
            if(uniqueID == buyer_UID){
                buyer_UID = (rand.nextInt(1000)*bidUID);
            }
        }

        AuctionItem new_buyer = new AuctionItem(bidUID, bid, name, email);
        active_BuyerInformations.put(buyer_UID, new_buyer);
        bid_OnItem(bidUID, bid);
    }   
    //Bidding method to bid for buyer on item id and provided bidding price
    public void bid_OnItem(int item_ID, int bidding){
        Enumeration<Integer> activeKeys = active_Auctions.keys();
        while(activeKeys.hasMoreElements()){
            int activekey = activeKeys.nextElement();
            if(activekey == item_ID){
                active_Auctions.get(activekey).addBiddings(bidding);
               
            }
        }
    }

    //Get Winner by quoting the Unique ID
    public String close_Auction(int UID){
        int max = 0;
        String buyerName = "";
        String buyerEmail = "";
        int reserve = 0;
        int removal_Key = 0;
        boolean get_right_bid = false;
        //Get Active's key == UID, get highest bidding and reserve price
        Enumeration<Integer> active_Keys = active_Auctions.keys();
        while(active_Keys.hasMoreElements()){
            int curr_key = active_Keys.nextElement();
            if(curr_key == UID){
                removal_Key = curr_key;
                max = get_MAX_val_from_Biddings(active_Auctions.get(curr_key).getBiddings());
                reserve = active_Auctions.get(curr_key).getActiveReserve();
            }
        }
        Enumeration<Integer> active_Buyers = active_BuyerInformations.keys();
        while(active_Buyers.hasMoreElements()){
            int curr_Buyer = active_Buyers.nextElement();
            if(UID == active_BuyerInformations.get(curr_Buyer).GETbuyerItemID() && max == active_BuyerInformations.get(curr_Buyer).GEtbuyerBidding()){
                buyerName = active_BuyerInformations.get(curr_Buyer).GETbuyerName();
                buyerEmail = active_BuyerInformations.get(curr_Buyer).GETbuyerEmail();
                get_right_bid = true;
            }
        }

        if(get_right_bid == true){
            Enumeration<Integer> removals = active_BuyerInformations.keys();
            while(removals.hasMoreElements()){
                int curr_removal = removals.nextElement();
                if(active_BuyerInformations.get(curr_removal).GETbuyerItemID() == UID){
                    active_BuyerInformations.remove(curr_removal);
                }
            }
        }

        int itemId = active_Auctions.get(UID).getActiveID();
        //System.out.println("Item ID: " + itemId);
        if(max < reserve){
            //System.out.println("Under bid");
            active_Auctions.remove(UID);
            inactive_items.remove(itemId);
            return "No bids has reached the reserve price!";
        }
        String winner = "The winner is " + buyerName + " | Email: "+ buyerEmail + " | Bid: " + String.valueOf(max);
        active_Auctions.remove(UID);
        inactive_items.remove(itemId);
        //System.out.println(winner);
        return winner;
    }
    

    //Getters
    public int getActiveID(){
        return active_id;
    }
    public String getActiveDescription(){
        return active_description;
    }
    public int getActiveStart(){
        return start_price;
    }
    public int getActiveReserve(){
        return reserve_price;
    }
    public int getActiveUID(){
        return active_uid;
    }
    public ArrayList<Integer> getBiddings(){
        return auction_bids;
    }
    public void addBiddings(int num){
        auction_bids.add(num);
    }
    public Hashtable<Integer, AuctionItem> getActive_Auctions(){
        return active_Auctions;
    }
    public int get_MAX_val_from_Biddings(ArrayList<Integer> arr){
        if(arr.isEmpty() == true){
            return 0;
        }
        int max = Collections.max(arr);
        return max;
    }
    public int getStartingPrice(int itemUID){
        int start = active_Auctions.get(itemUID).getActiveStart();
        return start;
    }

    //Stage 2
    private int uniqueID = 1;
    private int itemUniqueID = 1000;
    private int buyerID = 2000;
    public int curr_buyer_id;
    public int curr_seller_id;
    //New private variables
    private int selling_price;
    private int curr_bid;
    private int owner;
    private ArrayList<Integer> stage2_bids;
    private ArrayList<Integer> stage2_sellerPrices;
    public AuctionItem(int itemid, String title, String description, int selling_price, int owner){
        this.itemid = itemid;
        this.title = title;
        this.description = description;
        this.selling_price = selling_price;
        this.owner = owner;
        this.stage2_bids = new ArrayList<Integer>();
        this.stage2_sellerPrices = new ArrayList<Integer>();
        this.curr_bid = 0;
    }
    //All HTs
    Hashtable<Integer, String> allSellers = new Hashtable<Integer,String>();
    Hashtable<Integer, String> allBuyers = new Hashtable<Integer,String>();
    Hashtable<Integer, AuctionItem> Stage2_Items = new Hashtable<Integer, AuctionItem>();

    
    //Digital Signature
    private static final String SIGNING_ALGORITHM = "SHA256withRSA"; 
	private static final String RSA = "RSA"; 
    KeyPair keyPair;
    //Auction server creates digital signature
    //User verfiy digital signature
    public static KeyPair Generate_RSA_KeyPair() throws Exception { 
		SecureRandom secureRandom = new SecureRandom(); 
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA); 
		keyPairGenerator.initialize( 2048, secureRandom); 
		return keyPairGenerator.generateKeyPair(); 
	} 
    //Create Digital Signing
    public static byte[] Create_Digital_Sign( byte[] name, PrivateKey Key) throws Exception { 
		Signature signature = Signature.getInstance(SIGNING_ALGORITHM); 
		signature.initSign(Key); 
		signature.update(name); 
		return signature.sign(); 
	} 
    //Vertify Digital Signing
    public static boolean Verify_Digital_Sign( byte[] name, byte[] signatureToVerify, PublicKey key)throws Exception { 
		Signature signature = Signature.getInstance(SIGNING_ALGORITHM); 
        signature.initVerify(key); 
		signature.update(name);
		return signature.verify(signatureToVerify);
	}

    boolean start = false;
    Hashtable<Integer, byte[]> signatures = new Hashtable<Integer, byte[]>();
    Hashtable<Integer, byte[]> hashedMessages =  new Hashtable<Integer, byte[]>();
    //Digital signing via Name of Users
    public String digital_signing(String name, int curr_id, String bos) throws Exception{
        int coressponding_id = 0;
        String found_name = null;
        if(start == false){
            keyPair = Generate_RSA_KeyPair();
            start = true;
        }
        if(bos.equals("Buyer")){
            if(allBuyers.get(curr_id) != null && allBuyers.get(curr_id).equals(name)){
                coressponding_id = curr_id;
                found_name = name;
            }
        }else if(bos.equals("Seller")){
            if(allSellers.get(curr_id) != null && allSellers.get(curr_id).equals(name)){
                coressponding_id = curr_id;
                found_name = name;
            }
        }

        System.out.println("Signing: " + found_name + ", corr_id: " + coressponding_id);

        if(coressponding_id == 0 || found_name.equals(null)){
            System.out.println("ID not found in both buyers and sellers");
            return null;
        }
        byte[] signature = Create_Digital_Sign(found_name.getBytes(), keyPair.getPrivate());
        signatures.put(coressponding_id, signature);
        hashedMessages.put(coressponding_id, found_name.getBytes());
        return "Registered and Created Signature -> User ID:  " + coressponding_id + " | Orignal Message: " + found_name.getBytes() + " | Hashed Message: " + signature;
    }
    //Vertify based on id saved in the in the system
    public String vertify_Signing(int id_to_vertify, String bos) throws Exception{
        String actual_Name = null;
        byte[] name;
        byte[] sign;
        if(start == false){
            System.out.println("No key pairs");
            return null;
        }
        if(signatures.get(id_to_vertify) == null || hashedMessages.get(id_to_vertify) == null){
            System.out.println("Either signatures or hashed not found!");
            return null;
        }
        name = hashedMessages.get(id_to_vertify);
        sign = signatures.get(id_to_vertify);
        if(bos.equals("Seller")){
            actual_Name = allSellers.get(id_to_vertify);
        }else if(bos.equals("Buyer")){
            actual_Name = allBuyers.get(id_to_vertify);
        }
        if(actual_Name == null){
            return "false";
        }
        System.out.println("Key: " + keyPair);
        boolean vertification = Verify_Digital_Sign(name, sign, keyPair.getPublic());
        if(vertification == false){
            return "false";
        }
        String result = "Vertification Messsage:\nVertified User: "+ actual_Name + " | User ID: " + id_to_vertify + " | Digest Hashed: " +name + " | Recevied Hashed: " + sign + " | Vertification: " + vertification; 
        return result;
    }
    //Register Buyers and Sellers
    public String register_Seller(String name) throws Exception{
        Enumeration<Integer> sellerKeys = allSellers.keys();
        while(sellerKeys.hasMoreElements()){
            int key = sellerKeys.nextElement();
            if(allSellers.get(key).equals(name)){
                System.out.println("Same seller name found!");
                return null;
            }
        }
        int new_uid = getUniqueId();
        allSellers.put(new_uid, name);
        set_currSellerId(new_uid);

        System.out.println("Name: " + name + ", New ID: " + new_uid + ", currSellerID: " + curr_seller_id);
        String temp = digital_signing(name, new_uid, "Seller");
        return "Seller Registered! UID:" + new_uid + ", Name: " + name + ", Seller Id: " + get_currSellerId() + "\n" + temp;
    }
    public String register_Buyer(String name) throws Exception{
        Enumeration<Integer> buyerKeys = allBuyers.keys();
        while(buyerKeys.hasMoreElements()){
            int key = buyerKeys.nextElement();
            if(allBuyers.get(key).equals(name)){
                System.out.println("Same buyer name found!");
                return null;
            }
        }
        int new_uid = getBuyerId();
        allBuyers.put(new_uid,name);
        set_currBuyerId(new_uid);
        String temp = digital_signing(name, new_uid, "Buyer");
        return "Buyer Registered! UID: " + new_uid + ", Name: " + name + ", Buyer Id: " + get_currBuyerId() + "\n" + temp;
    }
    //Setter for Buyer and Seller
    public String set_Seller(String name){
        Enumeration<Integer> sellerKeys = allSellers.keys();
        while(sellerKeys.hasMoreElements()){
            int key = sellerKeys.nextElement();
            if(allSellers.get(key).equals(name)){
                System.out.println("Seller name found! UID: " + key + " name: " + name);
                set_currSellerId(key);
                return "Seller Sets to UID: " + key + ", name: " + name;
            }
        }
        return null;
    }
    public String set_Buyer(String name){
        Enumeration<Integer> buyerKeys = allBuyers.keys();
        while(buyerKeys.hasMoreElements()){
            int key = buyerKeys.nextElement();
            if(allBuyers.get(key).equals(name)){
                System.out.println("Buyer name found! UID: " + key + " name: " + name);
                set_currBuyerId(key);
                return "Buyer Sets to " + key;
            }
        }
        return null;
    }
    //For Double Auction only!
    public String set_Admin(){
        if(curr_seller_id != 0){
            set_currSellerId(0);
            return "System is set to Admin level: " + get_currSellerId() + ", enables Double Auction Matching!";
        }
        return "System is already in Admin level: " + get_currSellerId();
    }

    //Allow Seller to create an Auction item 
    public String Stage2_AuctionItem(String iTitle, String iDes, int sellP) throws Exception{
        int uidforItem = getItemUniqueId();
        int curr_seller = get_currSellerId();
        if(curr_seller == 0){
            return "This is Unauthorized Seller, please register first!";
        }
        String temp = vertify_Signing(curr_seller, "Seller");
        if(temp.equals("false")){
            temp = "Error: Authentication Fails!";
            return temp;
        }
        
        AuctionItem adding_item = new AuctionItem(uidforItem, iTitle, iDes, sellP, curr_seller);
        System.out.println("UID: " + uidforItem + ", Title: " + iTitle +", Des: " + iDes +", Sell_P: " +sellP + ", Owner: " + curr_seller);
        Stage2_Items.put(uidforItem, adding_item);
        return "UID: " + uidforItem + ", Title: " + iTitle +", Des: " + iDes +", Sell_P: " +sellP + ", Owner: " + curr_seller +"\n"+temp;
    }
    //Allow Buyer to Browse
    Hashtable<Integer, String> temp = new Hashtable<Integer, String>();
    public Hashtable<Integer, String> Stage2_Reverse_Browse(String item_name){
        temp.clear();
        Enumeration<Integer> itemKeys = Stage2_Items.keys();
        while(itemKeys.hasMoreElements()){
            int key = itemKeys.nextElement();
            String curr_title = Stage2_Items.get(key).get_itemTitle();
            String curr_description = Stage2_Items.get(key).get_itemDescription();
            String name = "(.*)"+item_name+"(.*)";
            if(curr_title.matches(name) || curr_description.matches(name)){
                String tempstr = "Item: " + item_name + " | UID : " + key  + " | Sell Price: " + Stage2_Items.get(key).get_sellPrice() + " | Current Bid: " + Stage2_Items.get(key).get_currBid();
                temp.put(key, tempstr);
            }
        }
        if(temp.isEmpty()){
            System.out.println("Temp is Empty!");
            return null;
        }
        return temp;
    }
    //Reverse Auction: Let Buyer side to bid on item 
    public String Stage2_Reverse_bid(int itemid ,int bid_price, String name, String email) throws Exception{ 
        Enumeration<Integer> keys = allBuyers.keys();
        String actual_Name = null;
        int buyer_uid = 0;
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            
            System.out.println("All Buyers: " + allBuyers.get(key));

            if(allBuyers.get(key).equals(name)){
                actual_Name = name;
                buyer_uid = key;
            }
        }
        System.out.println("All Buyers: " + allBuyers);

        if(actual_Name == null || buyer_uid == 0){
            return "false";
        }

        if(Stage2_Items.get(itemid).get_currBid() > bid_price){
            System.out.println("Curr bid > Bid price");
            return null;
        }
        if(curr_buyer_id == 0){
            return "Unauthorized Buyer, Please register a Buyer first!";
        }
        String temp = vertify_Signing(buyer_uid, "Buyer");
        if(temp.equals("false")){
            temp = "Error: Authentication Fails";
            return temp;
        }
        ArrayList<Integer> arr = Stage2_Items.get(itemid).get_Biddings();
        arr.add(bid_price);
        Stage2_Items.get(itemid).set_currBid(bid_price);
        System.out.println("Curr is " + Stage2_Items.get(itemid).get_currBid());
        System.out.println(arr);
        //temp.clear();

        //New Buyer to insert
        AuctionItem new_Buyer = new AuctionItem(itemid, bid_price, name, email);
        active_BuyerInformations.put(buyer_uid, new_Buyer);

        Enumeration<Integer> allbuyerKeys = active_BuyerInformations.keys();
        while(allbuyerKeys.hasMoreElements()){
            int currBuyerKey = allbuyerKeys.nextElement();
            System.out.println("Buyer Name: " + active_BuyerInformations.get(currBuyerKey).GETbuyerName() + " , Email: " + active_BuyerInformations.get(currBuyerKey).GETbuyerEmail() + " , Bid: " + active_BuyerInformations.get(currBuyerKey).GEtbuyerBidding());
        }

        System.out.println("Item: " + active_BuyerInformations.get(buyer_uid).GETbuyerItemID() + ", Bid: " + active_BuyerInformations.get(buyer_uid).GEtbuyerBidding() + ", Name: " + active_BuyerInformations.get(buyer_uid).GETbuyerName());
        return "Buyer has bidded on Item: " + itemid + ", Bid price: " + bid_price +"\n" + temp;
    }
    //Reverse Auction: Close Auction based on item Id
    public String Stage2_closeAuction(int itemid){
        if(Stage2_Items.get(itemid) == null){
            return "Unable to Close Auction, Item ID not found!";
        }
        System.out.println("CS: " + curr_seller_id + ", Own: " + Stage2_Items.get(itemid).get_Owner());
        if(curr_seller_id != Stage2_Items.get(itemid).get_Owner()){
            return "Current Seller ID not matched with Closing Auction Item's Owner, Please re-set!";
        }
        String name = "";
        String email = "";
        int removal_key = 0;
        int max = 0;
        int sell_price = 0;
        boolean right_bid = false;
        Enumeration<Integer> keys = Stage2_Items.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(Stage2_Items.get(key).get_itemid() == itemid){
                max = get_MAX_val_from_Biddings(Stage2_Items.get(itemid).get_Biddings());
                System.out.println(Stage2_Items.get(itemid).get_Biddings());
                System.out.println("Max: " + max);
                removal_key = key;
                sell_price = Stage2_Items.get(key).get_sellPrice();
            }
        }

        Enumeration<Integer> buyerKeys = active_BuyerInformations.keys();
        while(buyerKeys.hasMoreElements()){
            int buyerKey = buyerKeys.nextElement();
            if(active_BuyerInformations.get(buyerKey).GETbuyerItemID() == itemid && max == active_BuyerInformations.get(buyerKey).GEtbuyerBidding()){
                name = active_BuyerInformations.get(buyerKey).GETbuyerName();
                email = active_BuyerInformations.get(buyerKey).GETbuyerEmail();
                right_bid = true;

                System.out.println("Name: " + name + ", email: " + email + ", bid: " + active_BuyerInformations.get(buyerKey).GEtbuyerBidding() + ", Buyer Key: " + buyerKey + ", Item ID: " + itemid + ", Buyer's Item ID: " + active_BuyerInformations.get(buyerKey).GETbuyerItemID() );
            }
        }
        if(right_bid == true){
            Enumeration<Integer> removals = active_BuyerInformations.keys();
            while(removals.hasMoreElements()){
                int removal = removals.nextElement();

                if(active_BuyerInformations.get(removal).GETbuyerItemID() == itemid){
                    active_BuyerInformations.remove(removal);
                }
            }
        }
        if(max < sell_price){
            Stage2_Items.remove(removal_key);
            return "No bids has reached the selling price!";
        }
        String winner = "The winner is " + name + " | Email: " + email + " | Bid: "+ String.valueOf(max);
        Stage2_Items.remove(removal_key);
        temp.clear();
        return winner;
    }

    //Seller Side Double Auction Functions
    Hashtable<String, Integer> tempseller = new Hashtable<String, Integer>();
    Hashtable<String, Integer> tempbuyer = new Hashtable<String, Integer>();
    Boolean flag = false;
    //Check if there is enough sellers before matching
    public String Stage2_DoubleCheck(int itemid){
        if(allSellers.size() < 3){
            System.out.println("Do not have enough buyers or sellers in the Auction");
            flag = false;
            return null;
        }
        if(Stage2_Items.get(itemid) == null){
            return "Item ID not found, Please re-try!";
        }
        if(Stage2_Items.get(itemid).get_Owner() != 0){
            Stage2_Items.get(itemid).set_Onwer(0);
            curr_seller_id = 0;
        }
        flag = true;
        System.out.println("flag: " + flag);
        return "Item UID: " + itemid + ", set to be on Double Auction mode " + Stage2_Items.get(itemid).get_Owner();
    }
    //Let Seller join selling on an item, based on id
    public String Stage2_Seller_Join_Double(int itemid ,String jname ,int selling_p){
        if(Stage2_Items.get(itemid) == null){
            System.out.println("item not found");
            return null;
        }
        if(flag == false){
            System.out.println("flag is false");
            return null;
        }
        Enumeration<String> tkeys = tempseller.keys();
        while(tkeys.hasMoreElements()){
            String tkey = tkeys.nextElement();
            //System.out.println("tkey:" + tkey);
            if(tkey.equals(jname)){
                System.out.println("Duplicated name found, can not add sell price");
                return "Duplicate Seller! Pleaset let other Seller join this Auction. Re-try!";
            }
        }
        Enumeration<Integer> keys = allSellers.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            //System.out.println("Curr: " + allSellers.get(key));
            if(allSellers.get(key).equals(jname)){
                
                System.out.println("User found! -> " +  jname);
                ArrayList<Integer> sellertemp = Stage2_Items.get(itemid).get_SellerPrices();
                sellertemp.add(selling_p);
                System.out.println("Arr: " + sellertemp);
                tempseller.put(jname, selling_p);
                System.out.println(tempseller);
                return "Seller Joined Item ID: " + itemid + " <-- Seller ID: " + key + " | Seller Name: " + allSellers.get(key) + " | Sell Price: " + selling_p;
            }
        }
        return null;
    }
    //Buyer Double Auctions Functions
    Boolean bflag = false;
    public String Stage2_Buyer_DoubleCheck(int itemid){
        if(Stage2_Items.get(itemid) == null){
            System.out.println("Item not found!");
            return null;
        }
        if(active_BuyerInformations.size() < 3){
            System.out.println("Not enough Buyers");
            bflag = false;
            return "small";
        }
        bflag = true;
        return "Buyers are all set!";
    }
    //For Buyer to bid (Double)
    public String Stage2_Buyer_DoubleBid(int itemid, String name, String email, int bidprice) throws Exception{
        String actual_Name = null;
        int buyer_uid = 0;
        if(Stage2_Items.get(itemid).get_currBid() > bidprice){
            System.out.println("Curr bid> bid price");
            return null;    
        }
        Enumeration<String> tbkeys = tempbuyer.keys();
        while(tbkeys.hasMoreElements()){
            String tbkey = tbkeys.nextElement();
            if(tbkey.equals(name)){
                System.out.println("Duplicate Buyer Found! Do not allow.");
                return "Duplicate";
            }
        }
        Enumeration<Integer> keys = allBuyers.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            if(allBuyers.get(key).equals(name)){
                actual_Name = name;
                buyer_uid = key;
                tempbuyer.put(name, bidprice);
                System.out.println("tempb: " + tempbuyer);
            }
        }
        if(actual_Name == null || buyer_uid == 0){
            return "false";
        }
        
        String temp = vertify_Signing(buyer_uid, "Buyer");
        if(temp.equals("false")){
            temp = "Error: Authentication Fails";
            return temp;
        }
        ArrayList<Integer> arr = Stage2_Items.get(itemid).get_Biddings();
        arr.add(bidprice);
        Stage2_Items.get(itemid).set_currBid(bidprice);

        AuctionItem new_Buyer = new AuctionItem(itemid, bidprice, name, email);
        active_BuyerInformations.put(buyer_uid, new_Buyer);
        System.out.println("Item: " + active_BuyerInformations.get(buyer_uid).GETbuyerItemID() + ", Bid: " + active_BuyerInformations.get(buyer_uid).GEtbuyerBidding() + ", Name: " + active_BuyerInformations.get(buyer_uid).GETbuyerName());

        return "Double Aucton: Buyer " + buyer_uid + " has bidded on item: " + itemid + ", Bid Price: " + bidprice +"\n"+temp;
    }

    //Seller: Matching
    public String Stage2_MatchDouble(int itemid){
        if(Stage2_Items.get(itemid) == null){
            return null;
        }
        if(bflag == false || flag == false){
            System.out.println("Either Buyer or Seller not enough!");
            return "false";
        }
        if(curr_seller_id != 0){
            return "Not in Admin Level, can not initial Double Auction Matching! Current Seller ID: " + get_currSellerId();
        }
        ArrayList<Integer> sellers_bids = Stage2_Items.get(itemid).get_SellerPrices();
        ArrayList<Integer> buyers_bids = Stage2_Items.get(itemid).get_Biddings();
        System.out.println("Before:");
        System.out.println(sellers_bids);
        System.out.println(buyers_bids);
        System.out.println("After (sorted):");
        Collections.sort(sellers_bids);
        Collections.sort(buyers_bids, Collections.reverseOrder());
        System.out.println(sellers_bids);
        System.out.println(buyers_bids);
        
        int size = 0;
        if(sellers_bids.size() == buyers_bids.size()){
            size = sellers_bids.size();
        }else if(sellers_bids.size() > buyers_bids.size()){
            size = buyers_bids.size();
        }else if(sellers_bids.size() < buyers_bids.size()){
            size = sellers_bids.size();
        }
        int k = 0;
        ArrayList<Integer> seller_res = new ArrayList<Integer>();
        ArrayList<Integer> buyer_res = new ArrayList<Integer>();
        for(int i = 0; i < size; i++){
            int b = buyers_bids.get(i);
            int s = sellers_bids.get(i);
            if(b >= s){
                System.out.println("Buyer > Seller: " + buyers_bids.get(i) + " , " + sellers_bids.get(i) + ", K: " + k);
                buyer_res.add(b);
                seller_res.add(s);
                k++;
            }
        }
        String answer = "";
        for(int j = 0 ; j < seller_res.size(); j++){
            System.out.println("Buyer: " + buyer_res.get(j) + " | Seller: " + seller_res.get(j) + " | Optimised Profit: " + (buyer_res.get(j) - seller_res.get(j)));
            answer += "Buyer: " + buyer_res.get(j) + " | Seller: " + seller_res.get(j) + " | Optimised Profit: " + (buyer_res.get(j) - seller_res.get(j) + "| K: " + (j+1) + "\n");
        }
        answer += "K: " + k;
        Stage2_Items.remove(itemid);
        tempseller.clear();
        tempbuyer.clear();
        allSellers.clear();
        allBuyers.clear();

        Enumeration<Integer> buyerKeys = active_BuyerInformations.keys();
        while(buyerKeys.hasMoreElements()){
            int buyerKey = buyerKeys.nextElement();
            if(active_BuyerInformations.get(buyerKey).GETbuyerItemID() == itemid){
                System.out.println("Buyer: " + active_BuyerInformations.get(buyerKey).GETbuyerName() + " , " + active_BuyerInformations.get(buyerKey).GETbuyerItemID() + " , " + active_BuyerInformations.get(buyerKey).GEtbuyerBidding());
                active_BuyerInformations.remove(buyerKey);
            }
        }
        return answer;
    }

    //Reverse Double related Getters/Setters
    private int getUniqueId(){
        return uniqueID++;
    }
    private int getItemUniqueId(){
        return itemUniqueID++;
    }
    private int getBuyerId(){
        return buyerID++;
    }
    public void set_currSellerId(int num){
        curr_seller_id = num;
    }
    public void set_currBuyerId(int num){
        curr_buyer_id = num;
    }
    public int get_currSellerId(){
        return curr_seller_id;
    }
    public int get_currBuyerId(){
        return curr_buyer_id;
    }
    //Getters/Setters for AuctionItem instances
    private int get_itemid(){
        return itemid;
    }
    private String get_itemTitle(){
        return title;
    }
    private String get_itemDescription(){
        return description;
    }
    private int get_sellPrice(){
        return selling_price;
    }
    public int get_currBid(){
        return curr_bid;
    }
    public int retcurrbid(int itemid){
        return Stage2_Items.get(itemid).get_currBid();
    }
    private ArrayList<Integer> get_Biddings(){
        return stage2_bids;
    }
    private int get_Owner(){
        return owner;
    }
    private void set_Onwer(int num){
        owner = num;
    }
    private void set_currBid(int num){
        curr_bid = num;
    }
    private ArrayList<Integer> get_SellerPrices(){
        return stage2_sellerPrices;
    }
    
    public Hashtable<Integer, String> retSellers(){
        return allSellers;
    }
    public Hashtable<Integer, String> retBuyers(){
        return allBuyers;
    }
    public ArrayList<String> retst2Items(){
        ArrayList<String> fornow = new ArrayList<String>();;
        Enumeration<Integer> keys  = Stage2_Items.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            String temp = "UID: " + Stage2_Items.get(key).get_itemid() + " | Title: "+Stage2_Items.get(key).get_itemTitle() + "| Description:" + Stage2_Items.get(key).get_itemDescription() + "| Sell Price: " + Stage2_Items.get(key).get_sellPrice() + " | Owner: " + Stage2_Items.get(key).get_Owner();
            fornow.add(temp);
        }

        return fornow;
    }

    public ArrayList<String> retst1Items(){
        ArrayList<String> fornow2 = new ArrayList<String>();
        Enumeration<Integer> keys =  inactive_items.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            String temp = "UID: " + inactive_items.get(key).getID() + " | Title: " + inactive_items.get(key).getTitle() + " | Description: " + inactive_items.get(key).getDescription();
            fornow2.add(temp);
        }
        return fornow2;
    }
    
    private boolean serverStatus = false;

    public void serverAlive(){
        serverStatus = true;
    }
    public void serverDead(){
        serverStatus = false;
    }
    public boolean getServerStatus(){
        return serverStatus;
    }

    public ArrayList<Object> getAllHashtables(){
        holdallHashtables.clear();
        holdallHashtables.add(inactive_items);
        holdallHashtables.add(active_Auctions);
        holdallHashtables.add(active_BuyerInformations);
        holdallHashtables.add(allSellers);
        holdallHashtables.add(allBuyers);
        holdallHashtables.add(Stage2_Items);
        holdallHashtables.add(signatures);
        holdallHashtables.add(hashedMessages);
        holdallHashtables.add(temp);
        holdallHashtables.add(tempseller);
        holdallHashtables.add(tempbuyer);
        //Local Variables
        holdallHashtables.add(uniqueID);
        holdallHashtables.add(itemUniqueID);
        holdallHashtables.add(buyerID);
        holdallHashtables.add(curr_buyer_id);
        holdallHashtables.add(curr_seller_id);
        holdallHashtables.add(start);
        holdallHashtables.add(keyPair);
        holdallHashtables.add(serverStatus);

        return new ArrayList<>(holdallHashtables);
    }
    @SuppressWarnings("unchecked")
    public void setAllHashtables(ArrayList<Object> holdallHashtables){
        inactive_items = new Hashtable<>((Hashtable<Integer, AuctionItem>) holdallHashtables.get(0));
        active_Auctions =  new Hashtable<>((Hashtable<Integer, AuctionItem>) holdallHashtables.get(1));
        active_BuyerInformations = new Hashtable<>((Hashtable<Integer, AuctionItem>) holdallHashtables.get(2));
        allSellers = new Hashtable<>((Hashtable<Integer, String>) holdallHashtables.get(3));
        allBuyers = new Hashtable<>((Hashtable<Integer, String>) holdallHashtables.get(4));
        Stage2_Items = new Hashtable<>((Hashtable<Integer, AuctionItem>) holdallHashtables.get(5));
        signatures = new Hashtable<>((Hashtable<Integer, byte[]>) holdallHashtables.get(6));
        hashedMessages = new Hashtable<>((Hashtable<Integer, byte[]>) holdallHashtables.get(7));
        temp = new Hashtable<>((Hashtable<Integer, String>) holdallHashtables.get(8));
        tempseller = new Hashtable<>((Hashtable<String, Integer>) holdallHashtables.get(9));
        tempbuyer =  new Hashtable<>((Hashtable<String, Integer>) holdallHashtables.get(10));
        //Local Variables
        uniqueID = (int) holdallHashtables.get(11);
        itemUniqueID = (int) holdallHashtables.get(12);
        buyerID = (int) holdallHashtables.get(13);
        curr_buyer_id = (int) holdallHashtables.get(14);
        curr_seller_id = (int) holdallHashtables.get(15);
        start = (boolean) holdallHashtables.get(16);
        keyPair = (KeyPair) holdallHashtables.get(17);
        serverStatus = (boolean)holdallHashtables.get(18);
        //stage2_bids = (ArrayList<Integer>) holdallHashtables.get(18);
        //stage2_sellerPrices = (ArrayList<Integer>) holdallHashtables.get(19);
    }
}

    //Hashtable<Integer, AuctionItem> inactive_items
    //Hashtable<Integer, AuctionItem> active_Auctions
    //Hashtable<Integer, Buyer> active_BuyerInfos
    //Hashtable<Integer, String> allSellers
    //Hashtable<Integer, String> allBuyers 
    //Hashtable<Integer, AuctionItem> Stage2_Items 
    //Hashtable<Integer, byte[]> signatures
    //Hashtable<Integer, byte[]> hashedMessages
    //Hashtable<Integer, String> temp  //For browsing Item
    //Hashtable<String, Integer> tempseller
    //Hashtable<String, Integer> tempbuyer 