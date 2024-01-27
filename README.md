# "SergeantStubby" Discord Bot

Before trying to use this code, please note that it is specifically built for the discord server over on ddclan.org, and is not designed to be modular between hell let loose or discord servers. 


# Features



## Discord Features


# Slash Commands  
## Fun/Useful Commands  
### Set Activity 
Sets Stubby's current Discord activity. 
- Command: `/set-activity`  
- Options: 
	 - Type: The type of activity to be shown. 
	 - Text: The text that the activity shows. 
### Ping 
Tests Sgt. Stubby's reaction time. 
- Command: `/ping`  
### SL-Roulette 
Selects a random person in your voice chat to be the Squad Leader for the next game. 
- Command: `/sl-roulette`  

## Dog-Tags  

### Set Dogtags 
Turns on/off the management of DD and DDR tags in a user's nickname based on server roles. 
- Command: `/set-dogtags`  
- Options: 
	 - Set: True means Stubby will automatically manage DD and DDR tags, False means not. 
### Set DD Promotion Message 
Turns on/off the congratulation auto-message that will post when a DDR gets promoted to DD. On timer. 
- Command: `/set-dd-promotion-message`  
- Options: 
	 - Set: True means Stubby will post a message welcoming the new members of DD in DD_General, False means not. 

## XP and Levelling 

### Level 
Get your current level from chatting in our Discord. 
- Command: `/level` 
### Set Member Levelling 
Turns on/off the levelling of the given member. 
- Command: `/set-member-levelling` 
- Options: 
	 - Set: True means Stubby will let the member collect XP and gain levels, False means they won't.
	 - Member: Set this to the server ping for a certain Discord member. 

## HLL Commands 

### Reset Seeding Leaderboard 
Resets all of the player's seeding time to 0 in the leaderboard. Current scores will not be saved. 
- Command: `/reset-seeding-leaderboard` 
- Options: 
	 - Confirmation: **True** - Yes, I want to reset the scores to 0. **False** - No, I want to keep the current scores. 
### Add Full VIP 
Adds a permanent VIP to the DD server with a given Steam ID and name. 
- Command: `/add-full-vip` 
- Options: 
	 - Name: The new member's username. 
	 - Steam ID: The new member's Steam ID. 
### Add Temp VIP 
Adds a temporary VIP to the DD server with a given name, Steam ID, and time. 
- Command: `/add-temp-vip`
- Options: 
	 - Name: The new member's username. 
	 - Steam ID: The new member's Steam ID. 
	- Time Unit: Hours, days, weeks, months, or years. 
	 - Duration: The number of hours/days/etc until the VIP expires. 
### Add Group VIP 
Adds (temp) VIP to players on the HLL server for a given amount of time. If already VIP, doesn't add. 
- Command: `/add-group-vip` 
- Options: 
	 - Time Unit: Hours, days, weeks, months, or years, or indefinite. 
	 - Duration: The number of hours/days/etc until the VIP expires (ignored if indefinite). 
	 - Number of Players: Will give the first (num) players who joined the server VIP. If larger than player count, adds all.

## Private Commands 
### Terminate 
Stops Stubby in its tracks. 
- Command: `/terminate` 
### Version 
Gives the current version of Stubby that is running. 
- Command: `/version` 
### Send Message 
Sends a message in the specified text channel as Stubby. 
- Command: `/send-message` 
- Options: 
	 - Channel ID: The ID of the channel to send a message to. 
	 - Message ID: The ID of the message to replicate. Command must be used in the same channel. 
### Execute SQL 
Sends an SQL query to Stubby's Database and returns any results. 
- Command: `/execute-sql` 
- Options: 
	 - SQL: The SQL of the query to be ran. Make sure it's formatted correctly.
