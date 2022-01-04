import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionsImpl<T> implements Connections<String>{
    private ConcurrentHashMap<Integer,ConnectionHandler<T>> handlerMap;
    private LinkedBlockingDeque<User> userList;
    private LinkedBlockingQueue<String> messageList;
    private LinkedList<String> filterList;

    public ConnectionsImpl(LinkedList filterList) {
        this.handlerMap = new ConcurrentHashMap<>();
        this.userList = new LinkedBlockingDeque<>();
        this.messageList = new LinkedBlockingQueue<>();
        this.filterList = filterList;
    }

    public void addHandlerMap(int connectId,ConnectionHandler connect){
        this.handlerMap.putIfAbsent(connectId,connect);
    }

    public void addUser(User user){
        this.userList.add(user);
    }

    public User findUser(String userName){
        Iterator ita = userList.iterator();
        while (ita.hasNext()){
            User user = (User) ita.next();
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        return null;
    }
    public ConnectionHandler getHandler(int connectId){
        return handlerMap.get(connectId);
    }
    public int getConnectionID(ConnectionHandler handler) {
        for (Map.Entry<Integer, ConnectionHandler<T>> entry : handlerMap.entrySet()) {
            if (handler == entry.getValue()) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getHandlerMap() {
        return handlerMap;
    }

    public LinkedBlockingDeque<User> getUserList() {
        return this.userList;
    }

    public boolean send(int connectionId, String msg) {
        if (!handlerMap.containsKey(connectionId))
        return false;
        ConnectionHandlerImpl handler = (ConnectionHandlerImpl) handlerMap.get(connectionId);
        handler.send(msg);
        return true;
    }
    public String filterMsg(String msg) {
        for (String filter : filterList){
                msg.replace(filter,"<filtered>");
        }
        return msg;
    }

    @Override
    public void broadcast(String msg) {
        //Find out why we need this
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler handler = this.getHandler(connectionId);
        try {
            handler.close();
            this.getHandlerMap().remove(connectionId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingQueue<String> getMessageList() {
        return messageList;
    }
}
