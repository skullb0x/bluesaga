SETUP BLUE SAGA PROJECT IN ECLIPSE
==================================

1. Download Eclipse from https://eclipse.org/downloads/

2. Open Eclipse

3. Select in the menu File > Import

4. Choose General > Existing Projects into Workspace

5. Click Next 

6. Click the Browse button next to "Select root directory" and select the bluesaga folder

7. Select all projects in the list, then click "Finish"

SETUP SERVER PROJECT
====================

1. Right-click on the SERVER project in the Project Explorer and select "Properties"

2. Click "Java Build Path"

3. Click the "Projects" tab 

4. Check that the following projects are in the list: "COMMON" and "MAP_GENERATOR", if not then Add them

5. Click on the “Libraries” tab

6. Check that the following libraries are in the list:
bs_user.jar
slick.jar
sqlitejdbc-v056.jar

If some libraries are missing you can add them from the "JARS" folder in the project folder.

7. Also check that the JRE version is 1.8.x

8. Click OK to close the Properties window

9. In the SERVER project, open the file game > ServerSettings.java

10. You will need a SERVER_ID in order to host your own server, to get one go to this website:
http://www.bluesaga.org/myservers/new.php

You will need a Blue Saga account to access this page.

11. Fill in the form and press the CREATE SERVER button

12. The page will now give you a server id

13. Copy the number and replace the default value of SERVER_ID in the ServerSettings.java file

14. Replace the PORT number with the one of your server

15. Change the CLIENT_VERSION number so it is the same number as you entered on the website
This is the number you change every time you want players to get the latest update of your client.
It always need to be the same as on the website. Go to http://www.bluesaga.org/myservers to check the number you've entered.

15. Right-click on the SERVER project in the Project explorer

16. Select Export

17. Choose Java > Runnable JAR file

18. Select BlueSaga_Server in the Launch configuration dropdown

19. Choose an Export destination, easiest is to select the BPserver.jar file in the "release" folder in the SERVER folder.

20. Select the option "Copy required libraries into a sub-folder next to the generated JAR" in the Library handling setting

21. Click "Finish"


RUN SERVER
==========

1. Install Java 8 on your server

2. Upload the "release" folder in the SERVER project to your server 

3. Go into the release folder

4. Run server by typing: 
java -jar BPserver.jar

You should now see the server start showing some log messages as it initializes.

When you see the message 
"Server is ready and waiting for clients!" 

then the server is ready for connections

SETUP CLIENT PROJECT
====================

You must build a client of your own in order for players to connect to your server, 
this is so that you can make changes of your own and customize the game later.

1. Right-click the CLIENT project in the Project explorer

2. Select "Java Build Path"

3. Click on the "Projects" tab

4. Check that the "COMMON" project is in the list, if not add it

5. Click on the "Libraries" tab

6. Check that the following libraries are in the list
bs_user.jar
grandcentral.jar
jogg-0.0.7.jar
jorbis-0.0.15.jar
lwjgl.jar
slick.jar
sqlitejdbc-v056.jar

If some libraries are missing you can add them from the "JARS" folder in the project folder.

7. Also check that the JRE selected is 1.8.x

8. Click on the small arrow next to the lwjgl.jar library to show a list of settings

9. Double click on the "Native library location" and select the "JARS" folder in your blue-saga project folder

10. Close the Properties window

11. In the CLIENT project open up the game > ClientSettings.java file

12. Set the VERSION_NR to the same number you did on the website when creating the server. If you don't remember or want to force an update of the client, just go to http://www.bluesaga.org/myservers/ and click Edit next to your server to see or change the client version number.

13. Change the VERSION_NR to the same number you have on CLIENT_VERSION in your ServerSettings.java and in the server settings on the bluesaga website: http://www.bluesaga.org/myservers

14. Change the SERVER_IP to the ip of your server

15. Change the PORT to the port of your server, should be the same port you entered in your ServerSettings.java

16. Right-click on the CLIENT project in the Project explorer

17. Select Export

18. Choose Java > Runnable JAR file

19. Select BlueSaga_Client in the Launch configuration dropdown

20. Choose an Export destination, easiest is to select the gameData.jar file in the "release" folder in the CLIENT folder.

21. Select the option "Copy required libraries into a sub-folder next to the generated JAR" in the Library handling setting

22. Click "Finish"

MAKE CLIENT AVAILABLE FOR PLAYERS
=================================

1. Upload the gameData.jar file in the "release" folder in the CLIENT folder to a webhost

2. Go to http://www.bluesaga.org/myservers/ and click on "Edit" next to your server

3. Paste in the url to the gamedata.jar file you uploaded in the Client Url field

4. Write a short description of the latest update for the players to read, this text will be seen in the game launcher window

5. Click Save

6. Players will now be able to get your latest update and connect to your server with the Blue Saga launcher 