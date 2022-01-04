import java.util.LinkedList;

public class TPCServerMain {
    public static void main(String[] args){
        LinkedList filterwords = new LinkedList();
        Server server = TPCServer.TPCServer(7777,
                BidiMessagingProtocolImpl::new,
                MessageEncoderDecoderImpl::new,
                filterwords);
        server.serve();
    }

}
