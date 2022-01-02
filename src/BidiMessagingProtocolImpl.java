import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String>{
    private Connections connections = null;
    private int connectId=-1;
    public void start(int connectId, Connections connections) {
        this.connectId = connectId;
        this.connections=connections;
    }
    public void process(String message) throws ParseException {
        ConnectionsImpl connectionImpl = (ConnectionsImpl) connections;
        ConnectionHandlerImpl handler = (ConnectionHandlerImpl) connectionImpl.getHandler(connectId);
        User currUser = handler.getUser();
        int index =2;
        String opcode = message.substring(0,2);
        if (opcode.equals("01")){
            String userName = cutString(index,message);
            index = index + userName.length() + 2;
            if (connectionImpl.findUser(userName)==null){
                connections.send(this.connectId,"1101");
                return;
            }
            String password = cutString(index,message);
            index = index + password.length() + 2;
            String birthday = cutString(index,message);
            User user = new User(userName,password,birthday);
            connectionImpl.addUser(user);
            connections.send(this.connectId,"1001");
            return;
        }
        else if (opcode.equals("02")){
            String userName = cutString(index,message);
            index = index + userName.length() + 2;
            String password = cutString(index,message);
            index = index + password.length() + 2;
            char captcha = message.charAt(index);
            User user = connectionImpl.findUser(userName);
            if (user==null || !user.getPassword().equals(password) || captcha == '0' || user.getConnectionHandler()!=null || handler.getUser()!=null){
                connections.send(this.connectId,"1102");
                return;
            }
            user.setConnectionHandler(handler);
            handler.setUser(user);
            connections.send(this.connectId,"1002");
            while (!user.getReceivedMessages().isEmpty()){
                this.process(user.getReceivedMessages().poll());
            }
            return;
        }
        else if (opcode.equals("03")){
            if (handler.getUser()==null) {
                connections.send(this.connectId, "1103");
                return;
            }
            User user = handler.getUser();
            user.setConnectionHandler(null);
            handler.setUser(null);
            connections.send(this.connectId,"1003");
            return;
        }
        else if (opcode.equals("04")){//follow = 0, unfollow = 1
            char follow = message.charAt(index);
            index++;
            String Username = message.substring(index);
            User followUser = currUser.findFollowingUser(Username);
            User user = connectionImpl.findUser(Username);
            if (currUser==null || user==null || (follow == '0' && followUser!=null) || (follow == '1' && followUser==null) || follow!='0' || follow !='1'||user.getBlockedList().contains(currUser)||!(currUser.getBlockedList().contains(user)))
            {
                connections.send(this.connectId,"1004");
                return;
            }
            if (follow == '0'){
                currUser.getFollowList().add(user);
                followUser.increaseFollowed();
            }
            else if (follow == '1'){
                currUser.getFollowList().remove(user);
                followUser.decreaseFollowed();
            }
            connections.send(this.connectId,"1104" + followUser + "\0");
            return;
        }

        else if (opcode.equals("05")){
            if(currUser == null){
                connections.send(this.connectId, "1105");
                return;
            }
            String content = cutString(index, message);
            String filteredContent = connectionImpl.filterMsg(content);
            LinkedList<String> usernameList = new LinkedList<>();
            for (int i=0; i<content.length()-1;i++){
                if(i == '@'){
                    String username = cutString(i+1, content, ' ');
                    usernameList.add(username);
                }
            }
            for (User user : currUser.getFollowList()) {
                if (!usernameList.contains(user.getUserName()))
                    usernameList.add(user.getUserName());
            }
            connectionImpl.getMessageList().add(filteredContent);
            for (String userName : usernameList) { // Like connections.send
                User user = connectionImpl.findUser(userName); //might return null, if the user doesn't exist.
                if (user != null || !(user.getBlockedList().contains(currUser))||!(currUser.getBlockedList().contains(user))) {
                        if (user.getConnectionHandler() == null)
                            user.getReceivedMessages().add(filteredContent);
                        else {
                            int connectId = connectionImpl.getConnectionID(user.getConnectionHandler());//if no handlers, return -1;
                            connections.send(connectId, filteredContent);
                        }

                }
            }
            connections.send(this.connectId,"1005");
            return;

        }



        else if (opcode.equals("06")){
            String userName = cutString(index,message);
            index=index + userName.length()+2;
            String content = cutString(index,message);
            index = index + content.length()+2;
            String dateTime = cutString(index,message);
            User receivedUser = connectionImpl.findUser(userName); // will return null if doesn't exist.
            if (currUser==null||receivedUser==null||!currUser.getFollowList().contains(receivedUser)||receivedUser.getBlockedList().contains(currUser)||!(currUser.getBlockedList().contains(receivedUser))){
                connectionImpl.send(connectId,"1106");
                return;
            }
            String filteredContent = connectionImpl.filterMsg(content);
            connectionImpl.getMessageList().add(filteredContent);
            connections.send(connectionImpl.getConnectionID(receivedUser.getConnectionHandler()),filteredContent);
            connections.send(connectId,"1006");
            return;
        }
        else if (opcode.equals("07")){
        if (currUser==null){
            connections.send(connectId,"1107");
            return;
        }
        Iterator ita = connectionImpl.getUserList().iterator();
        String ack = "1007";
        while(ita.hasNext()){
            User user = (User) ita.next();
            if (!user.equals(currUser))
            ack += user.getAge() + " " + user.getPostedMessages().size() + " " + user.getFollowList().size() + " " + user.getFollows() + "\0";
        }
        connectionImpl.send(connectId,ack);
        return;
        }
        else if (opcode.equals("08")){
        if (currUser==null){
            connections.send(connectId,"1108");
        }
        String content = cutString(index,message);
        LinkedList<User> users = new LinkedList<>();
        while (index<content.length()){// if it managed to finish this, it means all the users are valid.
            String userName = this.cutString(index,content,'|');
            index += userName.length()+1;
            if (!userName.equals("")) {
                User user = connectionImpl.findUser(userName); // returns null if there is no such user.
                if (user == null){
                    connections.send(connectId,"1108");
                    return;
                }
                users.add(user);
            }
        }
            String ack = "1008";
            for (User user : users){
                ack += user.getAge() + " " + user.getPostedMessages().size() + " " + user.getFollowList().size() + " " + user.getFollows() + "\0";
            }
            connections.send(connectId,"ack");
            return;


        }
//        else if (opcode.equals("09")){
//
//        }
//        else if (opcode.equals("10")){
//
//        }
//        else if (opcode.equals("11")){
//
//        }
        else if (opcode.equals("12")){
            String userName = cutString(index,message);
            User blockedUser = connectionImpl.findUser(userName);//will be null if it doesn't exist.
            if (currUser == null || blockedUser==null|| currUser.getBlockedList().contains(blockedUser)){
                connectionImpl.send(connectId,"1112");
                return;
            }
            connectionImpl.send(connectId,"1012");
            currUser.getBlockedList().add(blockedUser);
            if(currUser.getFollowList().remove(blockedUser))
                currUser.decreaseFollowed();
            if(blockedUser.getFollowList().remove(currUser))
                currUser.decreaseFollowed();
        }



    }
    public boolean shouldTerminate() {
        return false;
    }
    private String cutString(int index,String string){
        String result = "";
        while (index<string.length() && string.charAt(index)!='/' && string.charAt(index+1)!= '0') {
            result = result + string.charAt(index);
            index++;
        }
        return result;
    }
    private String cutString(int index,String string, char stop){
        String result = "";
        while (index<string.length() && string.charAt(index)!= stop){
            result += string.charAt(index);
            index++;
        }
        return result;
    }
}
