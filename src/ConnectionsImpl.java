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
    private ConcurrentHashMap<Integer,ConnectionHandler<String>> handlerMap;
    private LinkedBlockingDeque<User> userList;
    private LinkedBlockingQueue<String> messageList = new LinkedBlockingQueue<>();
    private LinkedList<String> filteredLists;

    public ConnectionsImpl(LinkedList<String> filteredWords) {
        this.handlerMap = new ConcurrentHashMap<>();
        this.userList = new LinkedBlockingDeque<>();
        this.filteredLists = filteredWords;
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
            if (user.getUserName().equals(userName));
                return user;
        }
        return null;
    }
    public ConnectionHandler getHandler(int connectId){
        return handlerMap.get(connectId);
    }


    public ConcurrentHashMap<Integer, ConnectionHandler<String>> getHandlerMap() {
        return handlerMap;
    }

    public LinkedBlockingDeque<User> getUserList() {
        return this.userList;
    }

    public boolean send(int connectionId, String msg) {
        if(!handlerMap.containsKey(connectionId)){
            return false;
        }
        ConnectionHandlerImpl handler = (ConnectionHandlerImpl) handlerMap.get(connectionId);
        handler.send(msg);
        return true;
    }

    @Override
    public void broadcast(String msg) {
        //Find out why we need this
    }

    @Override
    public void disconnect(int connectionId) {

    }

    public LinkedBlockingQueue<String> getMessageList() {
        return messageList;
    }

    public LinkedList<String> getFilteredLists() {
        return filteredLists;
    }

    public int getConnectionID(ConnectionHandlerImpl handler){
        for(Map.Entry<Integer, ConnectionHandler<String>> entry: handlerMap.entrySet()){
            if(handler == entry.getValue()) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public String filteredMsg(String msg){
        for(String word : filteredLists){
            msg.replace(word, "<filtered>");
        }
        return msg;
    }

}
