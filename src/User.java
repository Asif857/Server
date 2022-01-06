import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
    private String userName;
    private String password;
    private String birthday;
    private ConnectionHandler connectionHandler;
    private LinkedList<User> followList;
    private LinkedList<String> postedMessages;
    private LinkedBlockingQueue<String> receivedMessages;
    private LinkedList<User> blockedList;
    private int followed = 0;

    public User(String userName, String password, String birthday) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
        this.followList = new LinkedList<>();
        this.postedMessages = new LinkedList<>();
        this.receivedMessages = new LinkedBlockingQueue<>();
        this.blockedList = new LinkedList<>();
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

    public LinkedBlockingQueue<String> getReceivedMessages() {
        return receivedMessages;
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

    public int getAge(){
        LocalDate date = LocalDate.now();
        LocalDate birthday = LocalDate.parse(this.birthday);
        return Period.between(birthday, date).getYears();
    }

    public LinkedList<User> getBlockedList() {
        return blockedList;
    }
}
