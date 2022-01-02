import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;

public class User {
    private String userName;
    private String password;
    private String birthday;
    private ConnectionHandler connectionHandler;
    private LinkedList<User> followList;
    private LinkedList<String> postedMessages;
    private LinkedList<String> receivedMessages;
    private int followed = 0;

    public User(String userName, String password, String birthday) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
        this.followList = new LinkedList<>();
        this.postedMessages = new LinkedList<>();
        this.receivedMessages = new LinkedList<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public LinkedList<User> getFollowList() {
        return followList;
    }

    public User findFollowUser(String username){
        Iterator ita = followList.iterator();
        while (ita.hasNext()){
            User user = (User) ita.next();
            if (user.getUserName().equals(userName));
            return user;
        }
        return null;
    }

    public LinkedList<String> getPostedMessages() {
        return postedMessages;
    }

    public LinkedList<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void receiveMessage(String message){
        this.receivedMessages.add(message);
    }

    public int getFollowed() {
        return followed;
    }

    public void increaseFollowed(){
        this.followed += 1;
    }

    public void decreaseFollowed(){
        this.followed -= 1;
    }
}
