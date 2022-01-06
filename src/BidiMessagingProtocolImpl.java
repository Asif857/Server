import java.util.Iterator;
import java.util.LinkedList;


public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String>{
    private Connections connections = null;
    private int connectId=-1;
    private boolean terminate = false;
    public void start(int connectId, Connections connections) {
        this.connectId = connectId;
        this.connections=connections;
    }
    public void process(String message) {
        System.out.println(message);
        ConnectionsImpl connectionImpl = (ConnectionsImpl) connections;
        ConnectionHandlerImpl handler = (ConnectionHandlerImpl) connectionImpl.getHandler(connectId);
        User currUser = handler.getUser();
        int index =2;
        String opcode = message.substring(0,2);
        if (opcode.equals("01")){
            System.out.println("user has registered!");
            String userName = cutString(index,message);
            System.out.println("Username - " + userName);
            index = index + userName.length() + 1;
            if (connectionImpl.findUser(userName)!= null){
                connections.send(this.connectId,"1101");
                return;
            }
            String password = cutString(index,message);
            index = index + password.length() + 1;
            String birthday = cutString(index,message);
            User user = new User(userName,password,birthday);
            connectionImpl.addUser(user);
            connections.send(this.connectId,"1001");
            return;
        }

        else if (opcode.equals("02")){
            String userName = cutString(index,message);
            System.out.println(userName + " has logged in!");
            index = index + userName.length() + 1;
            String password = cutString(index,message);
            index = index + password.length() + 1;
            char captcha = message.charAt(index);
            User user = connectionImpl.findUser(userName);
            if (user!=null) {
                synchronized (user) {
                    if (!user.getPassword().equals(password) || captcha == '0' || user.getConnectionHandler() != null || handler.getUser() != null) {
                        connections.send(this.connectId, "1102");
                        return;
                    }
                    user.setConnectionHandler(handler);
                    user.notifyAll();
                }
            }
            else
            {
                connections.send(this.connectId, "1102");
                return;
            }
            handler.setUser(user);
            user.setConnectionHandler(handler);
            connections.send(this.connectId,"1002");
            while(!user.getReceivedMessages().isEmpty()){
                int userConnectID = connectionImpl.getConnectionID(user.getConnectionHandler());
                connections.send(userConnectID, user.getReceivedMessages().poll());
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
            System.out.println("User has logged out!");
            connections.send(this.connectId,"1003");
            this.terminate = true;
            connectionImpl.disconnect(connectId);
            return;
        }

        else if (opcode.equals("04")){
            char follow = message.charAt(index);
            index++;
            String username = message.substring(index, message.length()-1);
            User followUser = currUser.findFollowUser(username);
            User requestedUser = connectionImpl.findUser(username);
            System.out.println(requestedUser.getUserName());
            if(currUser == null || (follow == '0' && followUser != null)||(follow == '1' && followUser == null) ||requestedUser == null||requestedUser.getBlockedList().contains(currUser)||currUser.getBlockedList().contains(requestedUser)){
                connections.send(this.connectId, "1104");
                return;
            }
            if(follow == '0'){
                currUser.getFollowList().add(requestedUser);
                requestedUser.increaseFollowed();
                requestedUser.getFollowedByList().add(currUser);
            }
            else {
                currUser.getFollowList().remove(requestedUser);
                requestedUser.decreaseFollowed();
                requestedUser.getFollowedByList().remove(currUser);
            }
            System.out.println("FOLOWED THE USER!");
            connections.send(connectId, "1004" + requestedUser.getUserName()+'\0');
        }

        else if (opcode.equals("05")){
            if(currUser == null){
                connections.send(this.connectId, "1105");
                return;
            }
            String content = cutString(index, message);
            String filteredContent = connectionImpl.filterMsg(content);
            connectionImpl.getMessageList().add(filteredContent);
            LinkedList<String> usernameList = new LinkedList<>();
            for (int i=0; i<content.length()-1;i++){
                if(content.charAt(i) == '@'){
                    String username = cutString(i+1, content, ' ');
                    usernameList.add(username);
                }
            }
            for(User user: currUser.getFollowedByList()){
                if(!usernameList.contains(user.getUserName())){
                    usernameList.add(user.getUserName());
                }
            }
            for(String username: usernameList){
                User user = connectionImpl.findUser(username);
                if(user != null && !user.getBlockedList().contains(currUser) && !currUser.getBlockedList().contains(user)) {
                    if (user.getConnectionHandler() == null) {
                        user.getReceivedMessages().add("09" + '1' + currUser.getUserName() + '\0' + filteredContent + '\0');
                    }
                    else {
                        ConnectionHandlerImpl cHandler = (ConnectionHandlerImpl) user.getConnectionHandler();
                        int connectId = connectionImpl.getConnectionID(cHandler);
                            connectionImpl.send(connectId, "09" + '1' + currUser.getUserName() + '\0' + filteredContent +'\0');
                        }
                    }
                }
            connectionImpl.send(connectId, "1005");
            currUser.increasePostedMessages();
            return;
        }
        else if (opcode.equals("06")){
            String username = cutString(index, message);
            index += username.length() +1;
            String content = cutString(index, message);
            index += content.length() +1;
            String dateTime = cutString(index, message);
            User receivedUser = connectionImpl.findUser(username);
            if(currUser == null || receivedUser == null || !currUser.getFollowList().contains(receivedUser) || currUser.getBlockedList().contains(receivedUser) ||receivedUser.getBlockedList().contains(currUser)){
                connectionImpl.send(connectId, "1106");
                return;
            }
            String filteredContent = connectionImpl.filterMsg(content);
            connectionImpl.getMessageList().add(filteredContent);
            ConnectionHandlerImpl receivedHandler = (ConnectionHandlerImpl) receivedUser.getConnectionHandler();
            int receivedID  = connectionImpl.getConnectionID(receivedHandler);
            if (!connectionImpl.send(receivedID, "09"+ '0' + currUser.getUserName() + '\0' + filteredContent + '\0')){
                receivedUser.getReceivedMessages().add("09" + '0' + currUser.getUserName() + '\0' + filteredContent + '\0');
            }
            connectionImpl.send(connectId, "1006");
            return;
        }
        else if (opcode.equals("07")){
            if(currUser == null){
                connectionImpl.send(connectId, "1107");
                return;
            }
            String ack = "1007 ";
            Iterator iterator = connectionImpl.getUserList().iterator();
            while(iterator.hasNext()){
                User user = (User) iterator.next();
                if(!user.equals(currUser) && !currUser.getBlockedList().contains(user) && !user.getBlockedList().contains(currUser)) {
                    String age = Integer.toString(user.getAge());
                    String numPosts = Integer.toString(user.getPostedMessages());
                    String numFollowers = Integer.toString(user.getFollowed());
                    String numFollowing = Integer.toString(user.getFollowList().size());
                    ack += age + " " + numPosts + " " + numFollowers + " " + numFollowing + "\0";
                }
            }
            connectionImpl.send(connectId, ack);
            return;
        }
        else if (opcode.equals("08")){
            if(currUser == null){
                connectionImpl.send(connectId, "1108");
                return;
            }
            String content = cutString(index, message);
            LinkedList<User> users = new LinkedList();
            index = 0;
            while(index < content.length()){
                String name = cutString(index, content, '|');
                if(!name.equals("")) {
                    User user = connectionImpl.findUser(name);
                    if(user != null){
                        users.add(user);
                        index += name.length() + 1;
                    }
                    else{
                        connectionImpl.send(connectId, "1108");
                        return;
                    }
                }
            }
            String ack = "1008 ";
            for(User statUser : users){
                String age = Integer.toString(statUser.getAge());
                String numPosts = Integer.toString(statUser.getPostedMessages());
                String followers = Integer.toString(statUser.getFollowed());
                String following = Integer.toString(statUser.getFollowList().size());
                ack += age + " " + numPosts + " " + followers + " " + following + "\0";
            }
            System.out.println(ack);
            connectionImpl.send(connectId, ack);
            return;
        }
        else if (opcode.equals("12")){
            String username = cutString(index, message);
            User blockedUser = connectionImpl.findUser(username);
            if(currUser == null || blockedUser == null||currUser.getBlockedList().contains(blockedUser)){
                connectionImpl.send(connectId, "1112");
                return;
            }
            currUser.getBlockedList().add(blockedUser);
            connectionImpl.send(connectId, "1012");
            if(currUser.getFollowList().remove(blockedUser)){
                blockedUser.decreaseFollowed();
                currUser.getFollowedByList().remove(blockedUser);
            }
            if(blockedUser.getFollowList().remove(currUser)){
                currUser.decreaseFollowed();
                blockedUser.getFollowedByList().remove(currUser);
            }

        }



    }
    public boolean shouldTerminate() {
        return terminate;
    }
    private String cutString(int index,String string){
        String result = "";
        while (index<string.length() && string.charAt(index)!='\0'){
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
