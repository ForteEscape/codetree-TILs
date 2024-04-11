import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception{
        // 여기에 코드를 작성해주세요.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int N = Integer.parseInt(br.readLine());

        List<Integer> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(br.readLine());
        for(int i = 0; i < N; i++) {
            list.add(Integer.parseInt(st.nextToken()));
        }

        Collections.sort(list, Comparator.comparingInt(e -> e));

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < N; i++) {
            sb.append(list.get(i)).append(" ");
        }
        System.out.println(sb);
    }
}