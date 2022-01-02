import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public byte[] encode(String message){
    int index = 0;
    String first = message.substring(index,2);
    index += 2;
    String second = message.substring(index,4);
    index +=2;
    short opcodeClient = Short.valueOf(first);
    short opcodeServer = Short.valueOf(second);
    String zero = "\0";
    byte[] a = shortToBytes(opcodeClient);
    byte[] b = shortToBytes(opcodeServer);
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(a);
            outputStream.write(b);
            //we have the first 2 shorts as bytes.
            if (opcodeServer==4){
                String userName = cutString(index,message);
                outputStream.write(userName.getBytes()); //uses UTF-8 by default.
                outputStream.write(zero.getBytes());
            }
            else if (opcodeServer==7) {
                while (index < message.length()) {
                    byte[] ans;
                    for (int i = 0; i < 3; i++) {
                        String len = cutString(index, message, ' ');
                        index += len.length()+1;
                        ans = shortToBytes(Short.valueOf(len));
                        outputStream.write(ans);
                    }
                    ans = shortToBytes(Short.valueOf(cutString(index,message)));
                    index+=2; //last iteration has \0 in it.
                    outputStream.write(ans);
                }
            }
            else if(opcodeServer ==8){
                while (index < message.length()) {
                    byte[] ans;
                    for (int i = 0; i < 3; i++) {
                        String len = cutString(index, message, ' ');
                        index += len.length()+1;
                        ans = shortToBytes(Short.valueOf(len));
                        outputStream.write(ans);
                    }
                    ans = shortToBytes(Short.valueOf(cutString(index,message)));
                    index+=2; //last iteration has \0 in it.
                    outputStream.write(ans);
                }
            }
            byte byteArray[]=outputStream.toByteArray();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; //won't happen
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


}
