package messages;

import java.util.HashMap;
import java.util.Map;

public enum Reply {
	RPL_NONE(300,""),
    RPL_CUSTOM(200,"%1"),
	//301-400
	/* Error Replies */
	ERR_NOSUCHNICK (401, "%1 :No such nick/channel"),
	ERR_NOSUCHSERVER (402, "%1 :No such server"),
	ERR_NOSUCHCHANNEL (403, "%1 :No such channel"),
	ERR_CANNOTSENDTOCHAN (404, "%1 :Cannot send to channel"),
	ERR_TOOMANYCHANNELS (405, "%1 :You have joined too many channels"),
	ERR_WASNOSUCHNICK (406, "%1 :There was no such nickname"),
	ERR_TOOMANYTARGETS (407, "%1 :Duplicate recipients. No message delivered"),
	//408-410
	ERR_NORECIPIENT (411, ":No recipient given (%1)"),
	ERR_NOTEXTTOSEND (412, ":No text to send"),
	ERR_UNKNOWNCOMMAND (421, "%1 :Unknown command"),
	//422-431
    ERR_NONICKNAMEGIVEN(431, ":No nickname given"),
	ERR_ERRONEUSNICKNAME(432, "%1 :Erroneus nickname"),
	ERR_NICKNAMEINUSE(433, "%1 :Nickname is already in use"),
	ERR_USERNOTINCHANNEL(441, "%1 %2 :They aren't on that channel"),
	ERR_NOTONCHANNEL(442, "%1 :You're not on that channel"),
	ERR_USERONCHANNEL(443, "%1 %2 :is already on channel"),
	//444-450
	ERR_NOTREGISTERED(451, "You have not registered"),
	ERR_NEEDMOREPARAMS(452, "%1 :Not enough parameters"),
	ERR_ALREADYREGISTRED(453, "You may not reregister"),
	//454-466
	ERR_KEYSET(467, "%1 :Channel key already set"),
	//468-470 NONE
	ERR_CHANNELISFULL(471, "%1 :Cannot join channel (+l)"),
	ERR_UNKNOWNMODE(472, "%1 :is unknown mode char to me"),
	ERR_INVITEONLYCHAN(473, "%1 :Cannot join channel (+i)"),
	ERR_BANNEDFROMCHAN(474, "%1 :Cannot join channel (+b)"),
	ERR_BADCHANNELKEY(475, "%1 :Cannot join channel (+k)"),
	//476-481 NONE
	ERR_NOPRIVILEGES(481, ":Permission Denied- You're not an IRC operator"),
	ERR_CHANOPRIVSNEEDED(482, "%1 :You're not channel operator"),
	//483-502
	;
	
	private final int code;
	private final String msg;
	
	Reply(int code) {
		this(code, "FIXME: Default Message");
	}
	
	Reply(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

    public String createMessage(String[] params) {
		Map<String, String> placeholderMap = new HashMap<String, String>();
		for(int i = 0; i < params.length; i++) {
			placeholderMap.put("%"+(i+1), params[i]);
		}

		String finalMsg = msg;
		for(String placeholder : placeholderMap.keySet()) {
			finalMsg = finalMsg.replace(placeholder, placeholderMap.get(placeholder));
		}
        if (code > 300) finalMsg = code + " " + finalMsg;
		return finalMsg;
	}
}
