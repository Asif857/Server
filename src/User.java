import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class User {
    private String userName;
    private String password;
    private String birthday;
    private ConnectionHandler connectionHandler;
    private LinkedList<User> followList;
    private LinkedList<String> postedMessages;
    private LinkedList<String> pmMessages;
    private LinkedBlockingQueue<String> receivedMessages;
    private int followed;

    public User(String userName, String password, String birthday) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
        this.followList = new LinkedList<>();
        this.postedMessages = new LinkedList<>();
        this.receivedMessages = new LinkedBlockingQueue<>();
        this.followList = new LinkedList<>();
    }

    public LinkedList<User> getFollowList() {
        return followList;
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
    public User findFollowingUser(String userName){
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
    public long getAge() throws ParseException {
        SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/YYYY");
        Date now =new Date();
        Date birthday = dtf.parse(this.birthday);
        long diff = now.getTime() - birthday.getTime();
        TimeUnit time = TimeUnit.DAYS;
        long age = (time.convert(diff,TimeUnit.MILLISECONDS));
        return age/365;
    }
}
