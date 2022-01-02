import java.util.LinkedList;

public class BidiMessagingProtocolImpl implements BidiMessagingProtocol<String>{
    private Connections connections = null;
    private int connectId=-1;
    public void start(int connectId, Connections connections) {
        this.connectId = connectId;
        this.connections=connections;
    }
    public void process(String message) {
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
            while(!user.getReceivedMessages().isEmpty()){
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

        else if (opcode.equals("04")){
            char follow = message.charAt(index);
            index++;
            String username = message.substring(index);
            User followUser = currUser.findFollowUser(username);
            if(currUser == null || (follow == '0' && followUser != null)||(follow == '1' && followUser == null) || follow != '0' ||follow != '1'){
                connections.send(this.connectId, "1104");
                return;
            }
            if(follow == '0'){
                currUser.getFollowList().add(followUser);
                followUser.increaseFollowed();
            }
            else {
                currUser.getFollowList().remove(followUser);
                followUser.decreaseFollowed();
            }
            connections.send(connectId, "1104" + followUser.getUserName() + "\0");
        }

        else if (opcode.equals("05")){
            if(currUser == null){
                connections.send(this.connectId, "1105");
                return;
            }
            String content = cutString(index, message);
            String filteredContent = connectionImpl.filteredMsg(content);
            connectionImpl.getMessageList().add(filteredContent);
            LinkedList<String> usernameList = new LinkedList<>();
            for (int i=0; i<content.length()-1;i++){
                if(i == '@'){
                    String username = cutString(i+1, content, ' ');
                    usernameList.add(username);
                }
            }
            for(User user: currUser.getFollowList()){
                if(!usernameList.contains(user.getUserName())){
                    usernameList.add(user.getUserName());
                }
            }
            for(String username: usernameList){
                User user = connectionImpl.findUser(username);
                if(user != null) {
                    if (user.getConnectionHandler() == null) {
                        user.getReceivedMessages().add(filteredContent);
                    } else {
                        ConnectionHandlerImpl cHandler = (ConnectionHandlerImpl) user.getConnectionHandler();
                        int connectId = connectionImpl.getConnectionID(cHandler);
                            connectionImpl.send(connectId, filteredContent);
                        }
                    }
                }
            connectionImpl.send(connectId, "1005");
            return;
        }
        else if (opcode.equals("06")){
            String username = cutString(index, message);
            index += username.length() +2;
            String content = cutString(index, message);
            index += content.length() +2;
            String dateTime = cutString(index, message);
            User recievedUser = connectionImpl.findUser(username);
            if(currUser == null || recievedUser == null || !currUser.getFollowList().contains(recievedUser)){
                connectionImpl.send(connectId, "1106");
                return;
            }
            String filteredContent = connectionImpl.filteredMsg(content);
            connectionImpl.getMessageList().add(filteredContent);
            ConnectionHandlerImpl recievedHandler = (ConnectionHandlerImpl) recievedUser.getConnectionHandler();
            int receivedID  = connectionImpl.getConnectionID(recievedHandler);
            connectionImpl.send(receivedID, filteredContent);
            connectionImpl.send(connectId, "1006");
            return;
        }
        else if (opcode.equals("07")){
            //Need to understand logstat
        }
        else if (opcode.equals("08")){
            String content = cutString(index, message);
            if(currUser == null){
                connectionImpl.send(connectId, "1108");
            }
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
            String ack = "1008";
            for(User statUser : users){

            }
        }
        else if (opcode.equals("09")){

        }
        else if (opcode.equals("10")){

        }
        else if (opcode.equals("11")){

        }
        else if (opcode.equals("12")){

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
