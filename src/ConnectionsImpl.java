import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionsImpl<T> implements Connections<T>{
    private ConcurrentHashMap<Integer,ConnectionHandler<T>> handlerMap;
    private LinkedBlockingDeque<User> userList;

    public ConnectionsImpl() {
        this.handlerMap = new ConcurrentHashMap<>();
        this.userList = new LinkedBlockingDeque<>();
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


    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getHandlerMap() {
        return handlerMap;
    }

    public LinkedBlockingDeque<User> getUserList() {
        return userList;
    }

    public boolean send(int connectionId, T msg) {
        if(!handlerMap.containsKey(connectionId)){
            return false;
        }
        ConnectionHandlerImpl handler = (ConnectionHandlerImpl) handlerMap.get(connectionId);
        handler.send(msg);
        return true;
    }

    @Override
    public void broadcast(T msg) {
        //Find out why we need this
    }

    @Override
    public void disconnect(int connectionId) {

    }
}
