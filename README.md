## Blackjack

     ### user clicks connect to server
          ToDo: Button Connect
     Games that are avaible shown on textfeild
          ToDo: Connect to new peer 
     The house becomes the player waiting for a connections
          A player connects to another player
          
          
     
     
     
     
     
     ToDo: change key word search button to a 'ready to play' button 
          game will start when all conneced users are 'ready to play'
               
     user clicks 'Hold' button
          ToDo: End of user turn when user has gone over 21, got 21 or clicks 'hold"
     
     ToDo: Host.class will be a player
           CentralServer will be the dealer 
           GUI will handle connect to CentralServer, ready to play, hit, and hold 
### Per to per blackjack game. A user can connect with friends to play blackjack over at tcp connection. 

     
     Create and shuffle a deck of cards
         Create multiple BlackjackHands, userHand1 to userHandN and dealerHand
         Deal two cards into each hand
         if dealer has blackjack
             User loses and the game ends now
         If user has blackjack
             User wins and the game ends now
         Repeat:
             Ask whether user wants to hit or stand
             if user stands:
                 break out of loop
             if user hits:
                 Give user a card
                 if userHand.getBlackjackValue() > 21:
                     User loses and the game ends now
         while  dealerHand.getBlackJackValue() <= 16 :
             Give dealer a card
             if dealerHand.getBlackjackValue() > 21:
                 User wins and game ends now
         if dealerHand.getBlackjackValue() >= userHand.getBlackjackValue()
             User loses
         else
             User wins
