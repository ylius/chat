# Chat Rooms

1. Download Host.class, ServerThread.class and Client.class in class folder

2. Open your terminal and choose one of the following two methods to run Host.class file:
- $ java Host<br>
This method runs the server at port 2222.
Should see following:
Usage: java MultiThreadChatServerSync <portNumber>
Now using port number = 2222
The system will ask to set the number of rooms.
- $ java Host portNumber<br>
Replace portNumber with a valid port number. This method runs the server at the assigned port.

3. Open other new windows of terminal and choose one of the following two methods to run Client.class file:
- $ java Client<br>
This method runs the server at port 2222.
The system will ask to enter a chat room number and your name. To quit the chat room enter /quit, everyone in the same chat room shall see a message notice.
- $ java Client portNumber<br>
portNumber should be the same as the assigned Host port number.

The default max number of clients each room is 6 and the default max number of rooms is 5.
