import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public String decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == ';') {
            return popString();
        }
        pushByte(nextByte);
        return null; //not a line yet
    }
    public byte[] encode(String message) {
        int index = 0;
        String first = message.substring(0,2);
        index += 2;
        String second = message.substring(2,4);
        index += 2;
        short opcodeClient = Short.valueOf(first);
        short opcodeServer = Short.valueOf(second);
        String zero = "\0";
        byte[] a = shortToBytes(opcodeClient);
        byte[] b = shortToBytes(opcodeServer);
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(a);
            outputStream.write(b);

            if(opcodeServer == 4){
                String username = cutString(index, message);
                outputStream.write(username.getBytes());
                outputStream.write(zero.getBytes());
            }
            else if(opcodeServer == 7){
                String age = cutString(index, message, ' ');
                index += age.length() + 1;
                byte[] w = stringToByte(age);
                String numPosts = cutString(index, message, ' ');
                index += numPosts.length()+1;
                byte[] y = stringToByte(numPosts);
                String numFollowers = cutString(index, message, ' ');
                index += numFollowers.length() + 1;
                byte[] z = stringToByte()
            }

            byte byteArray[] = outputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        short ops = bytesToShort(bytes);
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        if (ops<10)
            result = "0" + result;
        return result;
    }
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
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

    private byte[] stringToByte(String string){
        short a = Short.valueOf(string);
        return shortToBytes(a);
    }
}
