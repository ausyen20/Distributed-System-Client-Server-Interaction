import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;


interface IAuction extends Remote{
    public AuctionItem getSpec(int itemid, int clientid) throws RemoteException;
    public void storeAuctionItem(int _itemid, String _title, String _description) throws RemoteException;
    public boolean checkID_InactiveItems(int itemID) throws RemoteException;
    public int getID() throws RemoteException;
    public String getTitle() throws RemoteException;
    public String getDescription() throws RemoteException;

    //Stage 3
    public Hashtable<Integer, AuctionItem> getStage1Items() throws RemoteException;
    public void setInactiveItems(Hashtable<Integer, AuctionItem> newInactiveItems) throws RemoteException;
    public ArrayList<String> retst1Items() throws RemoteException;
    public void setAllHashtables(ArrayList<Object> holdallHashtables) throws RemoteException;
    public ArrayList<Object> getAllHashtables() throws RemoteException;
    public void serverAlive() throws RemoteException;
    public void serverDead() throws RemoteException;
    public boolean getServerStatus() throws RemoteException;

    public int auction_Item(int auction_id, String auction_description, int auction_start, int auction_reserve) throws RemoteException;
    public boolean checkID_ActiveAuctions(int itemID) throws RemoteException;
    public boolean checkUID_ActiveAuctions(int UID) throws RemoteException;
    public void bid_ActiveAuctions(int bidItemID, int bid, String name, String email) throws RemoteException;
    public void bid_OnItem(int item_ID, int bidding) throws RemoteException;
    public Hashtable<Integer, AuctionItem> getActive_Auctions() throws RemoteException;
    public int get_MAX_val_from_Biddings(ArrayList<Integer> arr) throws RemoteException;
    public int getStartingPrice(int itemUID) throws RemoteException;
    public String close_Auction(int UID) throws RemoteException;
    //Stage 2
    public String Stage2_AuctionItem(String iTitle, String iDes, int sellP) throws RemoteException, Exception; //* 
    public Hashtable<Integer, String> Stage2_Reverse_Browse(String item_name) throws RemoteException;
    public String Stage2_Reverse_bid(int itemid ,int bid_price, String name, String email) throws RemoteException, Exception;
    public String Stage2_closeAuction(int itemid) throws RemoteException;
    public int retcurrbid(int itemid) throws RemoteException;
    //Register and Sellers
    public String register_Seller(String name) throws RemoteException, Exception; // *
    public String register_Buyer(String name) throws RemoteException, Exception;
    public String set_Seller(String name) throws RemoteException;
    public String set_Buyer(String name) throws RemoteException;
    public ArrayList<String> retst2Items() throws RemoteException;
    public String set_Admin() throws RemoteException;

    public String Stage2_DoubleCheck(int itemid) throws RemoteException;
    public String Stage2_Seller_Join_Double(int itemid ,String name ,int selling_p) throws RemoteException;
    public String Stage2_Buyer_DoubleCheck(int itemid) throws RemoteException;
    public String Stage2_Buyer_DoubleBid(int itemid, String name, String email, int bidprice) throws RemoteException, Exception;
    public String Stage2_MatchDouble(int itemid) throws RemoteException;
}
