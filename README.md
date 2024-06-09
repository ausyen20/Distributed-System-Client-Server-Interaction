Simple Client-Server Trading Item & Auctions

Description:

The project is to simulate Client & Server Interaction using JGroups & RMI registry with Java. The program uses terminals to treat as individual clients and a server. (Example: client1 uses terminal1) 
The interaction is divided into buyers, sellers and a server. Where the sellers and buyers are simulated with client terminals for interactions. And a server is to handle the requests & databases. 
As a client made a request with appropriate format via terminal, it will forward the request and server will respond accordingly with comfirmation message.
The server also does error checking if there is any syntax different from the format imposed from the client by the server.

Structure:
The project uses JChannel from JGroups. When a client or server is launched via terminal, the client will be member joining a cluster of the local host (i.e. getRegistry("localhost")).
The Interface IAuction is used to delcare the methods which can be used by either clients or sellers.
The Frontend, which it acts as Main Replica. The Replica, which it acting as actual Replica in holding the database information propagate by the Frontend. 
The client can prompt buyers and sellers can prompt joining the cluster via client_buyer or client_seller. The server can prompt using AuctionItem.

More Information can be found at JGroups: http://www.jgroups.org/index.html
