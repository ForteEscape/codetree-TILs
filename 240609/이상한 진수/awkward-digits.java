import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String binary = br.readLine();
        String trinary = br.readLine();
        
        Set<Integer> set = new HashSet<>();

        if(binary.charAt(0) == '0') {
            binary = "1" + binary.substring(1);
            set.add(Integer.parseInt(binary, 2));
        } else {
            char[] data = binary.toCharArray();
            for(int i = 0; i < binary.length(); i++) {
                data[i] = data[i] == '0' ? '1' : '0';

                int result = Integer.parseInt(String.valueOf(data), 2);
                set.add(result);

                data[i] = data[i] == '0' ? '1' : '0';
            }
        }

        if(trinary.charAt(0) == '0') {
            for(int i = 1; i <= 2; i++) {
                trinary = String.valueOf(i) + trinary.substring(1);

                int res = Integer.parseInt(trinary, 3);
                if(set.contains(res)) {
                    System.out.println(res);
                    break;
                }
            }
        } else {
            char[] data = trinary.toCharArray();
            for(int i = 0; i < trinary.length(); i++) {
                char ori = data[i];
                
                for(int j = 0; j <= 2; j++) {
                    if(ori == (char)(j + '0')) {
                        continue;
                    }

                    data[i] = (char)(j + '0');

                    int res = Integer.parseInt(String.valueOf(data), 3);

                    if(set.contains(res)) {
                        System.out.println(res);
                        return;
                    }

                    data[i] = ori;
                }
            }
        }
    }
}